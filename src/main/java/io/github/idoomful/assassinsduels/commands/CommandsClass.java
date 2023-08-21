package io.github.idoomful.assassinsduels.commands;

import io.github.idoomful.assassinscurrencycore.utils.ConfigPair;
import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.commands.subcommands.ArenaCommand;
import io.github.idoomful.assassinsduels.commands.subcommands.DuelCommand;
import io.github.idoomful.assassinsduels.commands.subcommands.SubcommandModel;
import io.github.idoomful.assassinsduels.commands.subcommands.TeamCommand;
import io.github.idoomful.assassinsduels.gui.BettingGUI;
import io.github.idoomful.assassinsduels.match.*;
import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import io.github.idoomful.assassinsduels.configuration.MessagesYML;
import io.github.idoomful.assassinsduels.configuration.SettingsYML;
import io.github.idoomful.assassinsduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CommandsClass {
    private final ArrayList<UUID> matchWarnings = new ArrayList<>();
    private final HashMap<String, SubcommandModel> subcommands = new HashMap<>();

    public CommandsClass(DMain plugin) {
        final String pluginName = plugin.getDescription().getName();
        final String pluginNameLower = pluginName.toLowerCase();

        // Command initialization
        final CommandSettings settings = new CommandSettings(pluginNameLower)
                .setPermissionMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(null))
                .setAliases(Utils.Array.of("duels", "aduels", "asduels"));

        new ModularCommand(settings, (sender, args) -> {
            Player arg = sender instanceof Player ? (Player) sender : null;

            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                MessagesYML.Lists.PLAYER_HELP.getStringList(arg).forEach(sender::sendMessage);
                return;
            }

            if(!sender.hasPermission(pluginNameLower + ".command")) {
                sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                return;
            }

            switch(args[0]) {
                case "testbet":
                    if(sender.hasPermission(pluginNameLower + ".developer")) {
                        if(sender instanceof Player) {
                            if(args.length > 2) {
                                Player player = (Player) sender;
                                Match match = DuelsPlayer.fetch(player.getUniqueId()).anyMatch();
                                ArrayList<ConfigPair<Integer, String>> bets = new ArrayList<>();
                                DuellerGroup group = match.getGroups().get(1);

                                Integer amount = Integer.parseInt(args[1]);
                                String currency = args[2];

                                if(!group.getBets().containsKey(player.getUniqueId())) {
                                    bets.add(new ConfigPair<>(amount, currency));
                                    group.getBets().put(player.getUniqueId(), new Bet(bets, group, player.getUniqueId()));
                                } else {
                                    group.getBets().get(player.getUniqueId()).getMoney().add(new ConfigPair<>(amount, currency));
                                }
                            }
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "testshare":
                    if(sender.hasPermission(pluginNameLower + ".developer")) {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            Match match = DuelsPlayer.fetch(player.getUniqueId()).anyMatch();

                            ArrayList<OfflinePlayer> spectators = new ArrayList<>();
                            match.getSpectators().forEach(dp -> spectators.add(dp.getPlayer()));

                            Bet bet = match.getGroups().get(1).getBets().get(player.getUniqueId());
                            bet.share(spectators);
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "adminhelp":
                    if(sender.hasPermission(pluginNameLower + ".command.adminhelp")) {
                        MessagesYML.Lists.ADMIN_HELP.getStringList(arg).forEach(sender::sendMessage);
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "arena":
                    if(sender.hasPermission(pluginNameLower + ".command.arena")) {
                        if(!subcommands.containsKey("arena")) subcommands.put("arena", new ArenaCommand());
                        subcommands.get("arena").execute(plugin, sender, args);
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "leave":
                    if(sender.hasPermission(pluginNameLower + ".command.leave")) {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            DuelsPlayer dp = DuelsPlayer.fetch(player.getUniqueId());

                            if(dp.isPartOfMatch()) {
                                Match match = dp.anyMatch();

                                if(!dp.isSpectator()) {
                                    player.sendMessage(MessagesYML.Errors.NOT_SPECTATOR.withPrefix(arg));
                                    return;
                                }

                                if(dp.hasBets()) {
                                    player.sendMessage(MessagesYML.Errors.BET_CANNOT_LEAVE.withPrefix(arg));
                                    return;
                                }

                                match.kickSpectator(player, true);
                                player.sendMessage(MessagesYML.LEFT_SPECTATOR.withPrefix(player)
                                        .replace("$arena$", match.getArena().getName()));
                            } else {
                                player.sendMessage(MessagesYML.Errors.NOT_IN_MATCH.withPrefix(arg));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.NOT_PLAYER.withPrefix(null));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "spectate":
                    if (sender.hasPermission(pluginNameLower + ".command.spectate")) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            DuelsPlayer dp = DuelsPlayer.fetch(player.getUniqueId());

                            if(args.length > 1) {
                                if(!dp.isPartOfMatch()) {
                                    String name = args[1];

                                    if(plugin.getSQL().arenaExists(name)) {
                                        Match match = MatchManager.findMatch(name);
                                        boolean targetExists = Bukkit.getServer().getOnlinePlayers().stream().anyMatch(pl -> pl.getName().equalsIgnoreCase(args[1]));

                                        if (targetExists) {
                                            Player target = Bukkit.getPlayer(args[2]);
                                            DuelsPlayer dtarget = DuelsPlayer.fetch(target.getUniqueId());

                                            if (dtarget.isPartOfMatch()) {
                                                match = dtarget.anyMatch();
                                            } else {
                                                sender.sendMessage(MessagesYML.Errors.PLAYER_NOT_IN_MATCH.withPrefix(arg));
                                                return;
                                            }
                                        }

                                        assert match != null;

                                        if(MatchManager.hasMatch(name)) {
                                            if((match.getStatus() == MatchStatus.STANDBY || match.getStatus() == MatchStatus.FINISHED)
                                                    && !matchWarnings.contains(player.getUniqueId())) {
                                                player.sendMessage(MessagesYML.NO_MATCH_WARNING.withPrefix(arg));
                                                matchWarnings.add(player.getUniqueId());

                                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                                    matchWarnings.remove(player.getUniqueId());
                                                }, 10 * 20);
                                            } else {
                                                match.joinSpectator(player);
                                                player.sendMessage(MessagesYML.JOINED_SPECTATOR.withPrefix(arg));
                                                matchWarnings.remove(player.getUniqueId());
                                            }
                                        } else {
                                            player.sendMessage(MessagesYML.Errors.NO_MATCH.withPrefix(arg));
                                        }
                                    } else {
                                        sender.sendMessage(MessagesYML.Errors.INVALID_ARENA.withPrefix(arg).replace("$name$", name));
                                    }
                                } else {
                                    if(dp.isSpectator()) {
                                        player.sendMessage(MessagesYML.Errors.ALREADY_SPECTATOR.withPrefix(arg));
                                    }
                                }
                            } else {
                                sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                        .replace("$format$", "/duels spectate <arena>"));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.NOT_PLAYER.withPrefix(null));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "team":
                    if (sender.hasPermission(pluginNameLower + ".command.team")) {
                        if(!subcommands.containsKey("team")) subcommands.put("team", new TeamCommand());
                        subcommands.get("team").execute(plugin, sender, args);
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "duel":
                    if(sender.hasPermission(pluginNameLower + ".command.duel")) {
                        if(!subcommands.containsKey("duel")) subcommands.put("duel", new DuelCommand());
                        subcommands.get("duel").execute(plugin, sender, args);
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "bet":
                    if(sender.hasPermission(pluginNameLower + ".command.bet")) {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            DuelsPlayer dp = DuelsPlayer.fetch(player.getUniqueId());

                            if(args.length > 1) {
                                if (Bukkit.getServer().getOnlinePlayers().stream().noneMatch(pl -> pl.getName().equalsIgnoreCase(args[1]))) {
                                    player.sendMessage(MessagesYML.Errors.NOT_ONLINE.withPrefix(player).replace("$player$", args[1]));
                                    return;
                                }

                                if(player.getName().equals(args[1])) {
                                    player.sendMessage(MessagesYML.Errors.EXECUTE_ON_THEMSELVES.withPrefix(player));
                                    return;
                                }

                                if(!dp.isSpectator()) {
                                    player.sendMessage(MessagesYML.Errors.BET_NOT_IN_MATCH.withPrefix(player));
                                    return;
                                }

                                Match match = dp.anyMatch();
                                Player target = Bukkit.getPlayer(args[1]);
                                DuelsPlayer dt = DuelsPlayer.fetch(target.getUniqueId());

                                if(!dt.isDueller()) {
                                    player.sendMessage(MessagesYML.Errors.PLAYER_NOT_DUELLER.withPrefix(player));
                                    return;
                                }

                                if(dp.hasBets()) {
                                    player.sendMessage(MessagesYML.Errors.ALREADY_BET.withPrefix(player));
                                    return;
                                }

                                if(match.getStatus() != MatchStatus.STARTING) {
                                    player.sendMessage(MessagesYML.Errors.CANNOT_BET.withPrefix(player)
                                            .replace("$seconds$", SettingsYML.Duels.TIME_BEFORE_MATCH.getInt() + ""));
                                    return;
                                }

                                plugin.getPlayerBets().getCacheMap().put(player.getUniqueId(), match.getGroupIdFor(dt));

                                new BettingGUI(player);
                            } else {
                                sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                        .replace("$format$", "/duels bet <player>"));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.NOT_PLAYER.withPrefix(null));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "removeentry":
                case "deleteentry":
                    if(sender.hasPermission(pluginNameLower + ".command.removeentry")
                            || sender.hasPermission(pluginNameLower + ".command.deleteentry")) {
                        if(args.length == 2) {
                            if(!plugin.getSQL().playerExists(args[1])) {
                                sender.sendMessage(MessagesYML.Errors.INVALID_PLAYER.withPrefix(arg).replace("$name$", args[1]));
                                return;
                            }

                            plugin.getSQL().deletePlayer(args[1]);
                            sender.sendMessage(MessagesYML.ENTRY_DELETED.withPrefix(arg).replace("$name$", args[1]));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "reload":
                    if(sender.hasPermission(pluginNameLower + ".command.reload")) {
                        plugin.getConfigs().reloadConfigs();
                        MessagesYML.RELOAD.reload();
                        SettingsYML._OPTIONS.reload();
                        sender.sendMessage(MessagesYML.RELOAD.withPrefix(arg));
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "version":
                case "ver":
                case "v":
                    if(sender.hasPermission(pluginNameLower + ".command.version")) {
                        sender.sendMessage(pluginName + " version: " + plugin.getVersion());
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                default:
                    MessagesYML.Lists.PLAYER_HELP.getStringList(arg).forEach(sender::sendMessage);
            }
        });
    }
}