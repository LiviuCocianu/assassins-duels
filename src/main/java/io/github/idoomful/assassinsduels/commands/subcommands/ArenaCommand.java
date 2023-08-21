package io.github.idoomful.assassinsduels.commands.subcommands;

import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.arena.Arena;
import io.github.idoomful.assassinsduels.arena.ArenaSettings;
import io.github.idoomful.assassinsduels.configuration.MessagesYML;
import io.github.idoomful.assassinsduels.match.DuellerGroup;
import io.github.idoomful.assassinsduels.match.Match;
import io.github.idoomful.assassinsduels.match.MatchManager;
import io.github.idoomful.assassinsduels.match.MatchStatus;
import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import io.github.idoomful.assassinsduels.setup.ChatMode;
import io.github.idoomful.assassinsduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArenaCommand implements SubcommandModel {
    @Override
    public void execute(DMain plugin, CommandSender sender, String[] args) {
        final String pluginName = plugin.getDescription().getName();
        final String pluginNameLower = pluginName.toLowerCase();

        Player arg = sender instanceof Player ? (Player) sender : null;

        if(args.length > 1) {
            switch(args[1]) {
                case "create":
                    if(sender.hasPermission(pluginNameLower + ".command.arena.create")) {
                        if(args.length > 2) {
                            if(sender instanceof Player) {
                                Player player = (Player) sender;
                                String name = args[2];

                                if(plugin.getSQL().arenaExists(name)) {
                                    player.sendMessage(MessagesYML.Errors.ARENA_EXISTS.withPrefix(player)
                                            .replace("$name$", name));
                                    return;
                                }

                                sender.sendMessage(MessagesYML.ARENA_SETUP.withPrefix(player));
                                sender.sendMessage(MessagesYML.ARENA_SETUP_1.withPrefix(player));

                                plugin.getChatModes().addUser(player, ChatMode.Context.ARENA_CREATE);
                                plugin.getArenaCaching().getCacheMap().put(player.getUniqueId(), new Arena(name, new ArenaSettings()));
                                plugin.getSetupStepsCaching().getCacheMap().put(player.getUniqueId(), 1);
                            } else {
                                sender.sendMessage(MessagesYML.Errors.NOT_PLAYER.withPrefix(null));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena create <name>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "delete":
                    if(sender.hasPermission(pluginNameLower + ".command.arena.delete")) {
                        if(args.length > 2) {
                            String name = args[2];
                            if(plugin.getSQL().arenaExists(name)) {
                                plugin.getSQL().deleteArena(name);
                                sender.sendMessage(MessagesYML.ARENA_DELETED.withPrefix(arg).replace("$name$", name));
                            } else {
                                sender.sendMessage(MessagesYML.Errors.INVALID_ARENA.withPrefix(arg).replace("$name$", name));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena delete <name>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "setpriority":
                    if(sender.hasPermission(pluginNameLower + ".command.arena.setpriority")) {
                        if(args.length > 2) {
                            String name = args[2];
                            if(plugin.getSQL().arenaExists(name)) {
                                if(args.length > 3) {
                                    try {
                                        int priority = Integer.parseInt(args[3]);

                                        if(priority > 10000) {
                                            sender.sendMessage(MessagesYML.Errors.NUMBER_LIMIT.withPrefix(arg));
                                            return;
                                        }

                                        if(priority < 0) {
                                            sender.sendMessage(MessagesYML.Errors.NO_NEGATIVE.withPrefix(arg));
                                            return;
                                        }

                                        Arena arena = plugin.getSQL().getArena(name);
                                        arena.getSettings().setPriority(priority);
                                        plugin.getSQL().setArena(name, arena);

                                        MatchManager.getMatches().clear();
                                        MatchManager.updateMatches();

                                        sender.sendMessage(MessagesYML.ARENA_PRIORITY.withPrefix(arg)
                                                .replace("$name$", name)
                                                .replace("$priority$", priority + "")
                                        );
                                    } catch(NumberFormatException ne) {
                                        sender.sendMessage(MessagesYML.Errors.NOT_NUMBER.withPrefix(arg));
                                    }
                                } else {
                                    sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                            .replace("$format$", "/duels arena setpriority " + name + " <number>"));
                                }
                            } else {
                                sender.sendMessage(MessagesYML.Errors.INVALID_ARENA.withPrefix(arg).replace("$name$", name));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena setpriority <arena> <number>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "rename":
                    if(sender.hasPermission(pluginNameLower + ".command.arena.rename")) {
                        if(args.length > 2) {
                            String name = args[2];
                            if(plugin.getSQL().arenaExists(name)) {
                                if(args.length > 3) {
                                    Arena arena = plugin.getSQL().getArena(name);

                                    List<String> arenaNames = new ArrayList<>(plugin.getSQL().getArenas().keySet());

                                    if(arenaNames.contains(name)) {
                                        sender.sendMessage(MessagesYML.Errors.ARENA_EXISTS.withPrefix(arg)
                                                .replace("$name$", name));
                                        return;
                                    }

                                    arena.setName(args[3]);
                                    plugin.getSQL().setArena(name, arena);

                                    MatchManager.getMatches().remove(arena.getSettings().getPriority());
                                    MatchManager.updateMatches();

                                    sender.sendMessage(MessagesYML.ARENA_RENAME.withPrefix(arg)
                                            .replace("$name$", name)
                                            .replace("$rename$", args[3])
                                    );
                                } else {
                                    sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                            .replace("$format$", "/duels arena rename " + name + " <name>"));
                                }
                            } else {
                                sender.sendMessage(MessagesYML.Errors.INVALID_ARENA.withPrefix(arg).replace("$name$", name));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena rename <arena> <name>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "info":
                    if(sender.hasPermission(pluginNameLower + ".command.arena.info")) {
                        if(args.length > 2) {
                            String name = args[2];
                            boolean targetExists = Bukkit.getServer().getOnlinePlayers().stream().anyMatch(pl -> pl.getName().equalsIgnoreCase(args[2]));

                            if(plugin.getSQL().arenaExists(name) || targetExists) {
                                Arena arena;

                                if(targetExists) {
                                    Player target = Bukkit.getPlayer(args[2]);
                                    DuelsPlayer dtarget = DuelsPlayer.fetch(target.getUniqueId());

                                    if(dtarget.isPartOfMatch()) arena = dtarget.anyMatch().getArena();
                                    else {
                                        sender.sendMessage(MessagesYML.Errors.PLAYER_NOT_IN_MATCH.withPrefix(arg));
                                        return;
                                    }
                                } else {
                                    arena = plugin.getSQL().getArena(name);
                                }

                                ArrayList<String> positions = new ArrayList<>();
                                arena.getSettings().getPositions().forEach(pos -> positions.add(pos.toString()));

                                ArrayList<String> specPos = new ArrayList<>();
                                specPos.add(arena.getSettings().getSpectatorsPosition().toString());

                                for(String line : MessagesYML.Lists.ARENA_INFO.getStringList(arg)) {
                                    String posList = Utils.listStrings(positions);
                                    String posSpec = Utils.listStrings(specPos);

                                    sender.sendMessage(line
                                            .replace("$arena$", arena.getName())
                                            .replace("$type$", arena.getSettings().getDuelType().name())
                                            .replace("$max$", arena.getSettings().getMaxPlayers() + "")
                                            .replace("$priority$", arena.getSettings().getPriority() + "")
                                            .replace("$posList$", posList)
                                            .replace("$posSpec$", posSpec)
                                    );
                                }
                            } else {
                                sender.sendMessage(MessagesYML.Errors.INVALID_ARENA.withPrefix(arg).replace("$name$", name));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena info <name>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "matchinfo":
                    if(sender.hasPermission(pluginNameLower + ".command.arena.matchinfo")) {
                        if(args.length > 2) {
                            String name = args[2];

                            boolean targetExists = Bukkit.getServer().getOnlinePlayers().stream().anyMatch(pl -> pl.getName().equalsIgnoreCase(args[2]));

                            if (plugin.getSQL().arenaExists(name) || targetExists) {
                                Match match = MatchManager.findMatch(name);

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

                                if (match == null) {
                                    sender.sendMessage(MessagesYML.Errors.NO_MATCH.withPrefix(arg));
                                    return;
                                }

                                Arena arena = match.getArena();

                                String status = MessagesYML.Syntax.MatchStatus.valueOf(match.getStatus().name()).color(arg);
                                int max = arena.getSettings().getMaxPlayers();

                                final DuellerGroup group1 = match.getGroups().get(1);
                                final DuellerGroup group2 = match.getGroups().get(2);

                                int out1 = match.getStatus() == MatchStatus.STANDBY
                                        ? 0 : (max / 2) - group1.getMembers().size();
                                int out2 = match.getStatus() == MatchStatus.STANDBY
                                        ? 0 : (max / 2) - group2.getMembers().size();

                                for (String line : MessagesYML.Lists.MATCH_INFO.getStringList(arg)) {
                                    sender.sendMessage(line
                                            .replace("$arena$", arena.getName())
                                            .replace("$players$", match.getDuellerCount() + "")
                                            .replace("$max$", max + "")
                                            .replace("$spectators$", match.getSpectatorCount() + "")
                                            .replace("$status$", status)
                                            .replace("$out1$", out1 + "")
                                            .replace("$out2$", out2 + "")
                                            .replace("$bets1$", group1.getBets().size() + "")
                                            .replace("$bets2$", group2.getBets().size() + "")
                                    );
                                }
                            } else {
                                sender.sendMessage(MessagesYML.Errors.INVALID_ARENA.withPrefix(arg).replace("$name$", name));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena matchinfo <name>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "cancel":
                    if(sender.hasPermission(pluginNameLower + ".command.arena.cancel")) {
                        if(args.length > 2) {
                            String name = args[2];

                            boolean targetExists = Bukkit.getServer().getOnlinePlayers().stream().anyMatch(pl -> pl.getName().equalsIgnoreCase(args[2]));

                            if(plugin.getSQL().arenaExists(name) || targetExists) {
                                if(MatchManager.hasMatch(name)) {
                                    Match match = MatchManager.findMatch(name);

                                    if(targetExists) {
                                        Player target = Bukkit.getPlayer(args[2]);
                                        DuelsPlayer dtarget = DuelsPlayer.fetch(target.getUniqueId());

                                        if(dtarget.isPartOfMatch()) {
                                            match = dtarget.anyMatch();
                                        } else {
                                            sender.sendMessage(MessagesYML.Errors.PLAYER_NOT_IN_MATCH.withPrefix(arg));
                                            return;
                                        }
                                    }

                                    assert match != null;

                                    if(match.getStatus() == MatchStatus.ONGOING || match.getStatus() == MatchStatus.PAUSED) {
                                        match.broadcast(dp -> {
                                            String message = MessagesYML.CANCELED_MATCH.withPrefix(dp.getPlayer());
                                            if(arg != null) message = message.replace("$name$", arg.getName());
                                            return message;
                                        });

                                        match.cancel();
                                    } else {
                                        sender.sendMessage(MessagesYML.Errors.NOT_ONGOING.withPrefix(arg));
                                    }
                                } else {
                                    sender.sendMessage(MessagesYML.Errors.NO_MATCH.withPrefix(arg));
                                }
                            } else {
                                sender.sendMessage(MessagesYML.Errors.INVALID_ARENA.withPrefix(arg).replace("$name$", name));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena cancel <arena>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "pause":
                    if(sender.hasPermission(pluginNameLower + ".command.arena.pause")) {
                        if(args.length > 2) {
                            String name = args[2];

                            boolean targetExists = Bukkit.getServer().getOnlinePlayers().stream().anyMatch(pl -> pl.getName().equalsIgnoreCase(args[2]));

                            if (plugin.getSQL().arenaExists(name) || targetExists) {
                                if (MatchManager.hasMatch(name)) {
                                    Match match = MatchManager.findMatch(name);

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

                                    if(match.getStatus() == MatchStatus.STARTING
                                            || match.getStatus() == MatchStatus.ONGOING
                                            || match.getStatus() == MatchStatus.PAUSED
                                    ) {
                                        boolean state = match.pause();

                                        match.broadcast(dp -> {
                                            String paused = MessagesYML.PAUSED_MATCH.withPrefix(dp.getPlayer());
                                            String resumed = MessagesYML.RESUMED_MATCH.withPrefix(dp.getPlayer());

                                            if(arg != null) paused = paused.replace("$name$", arg.getName());
                                            return state ? paused : resumed;
                                        });
                                    } else {
                                        sender.sendMessage(MessagesYML.Errors.NOT_ONGOING.withPrefix(arg));
                                    }
                                } else {
                                    sender.sendMessage(MessagesYML.Errors.NO_MATCH.withPrefix(arg));
                                }
                            } else {
                                sender.sendMessage(MessagesYML.Errors.INVALID_ARENA.withPrefix(arg).replace("$name$", name));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena pause <arena>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "skip":
                    if (sender.hasPermission(pluginNameLower + ".command.arena.skip")) {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            DuelsPlayer dp = DuelsPlayer.fetch(player.getUniqueId());

                            if(dp.isPartOfMatch()) {
                                Match match = dp.anyMatch();

                                if(!dp.isSpectator()) {
                                    if(match.getStatus() == MatchStatus.STARTING) {
                                        match.skipCountdown();
                                        match.broadcast(dpl -> MessagesYML.SKIPPED_COUNTDOWN.withPrefix(dpl.getPlayer())
                                                .replace("$name$", player.getName()));
                                    } else {
                                        player.sendMessage(MessagesYML.Errors.NOT_STARTING.withPrefix(player));
                                    }
                                } else {
                                    player.sendMessage(MessagesYML.Errors.NOT_DUELLER.withPrefix(player));
                                }
                            } else {
                                player.sendMessage(MessagesYML.Errors.NOT_IN_MATCH.withPrefix(player));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.NOT_PLAYER.withPrefix(null));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "list":
                    if (sender.hasPermission(pluginNameLower + ".command.arena.list")) {
                        ArrayList<String> toProcess = new ArrayList<>(plugin.getSQL().getArenas().keySet());

                        main: for(int i = 0; i < toProcess.size(); i++) {
                            String arenaName = toProcess.get(i);

                            for(ArrayList<Match> matchList : MatchManager.getMatches().values()) {
                                for(Match match : matchList) {
                                    if(match.getArena().getName().equals(arenaName) &&
                                            (match.getStatus() == MatchStatus.STARTING
                                            || match.getStatus() == MatchStatus.ONGOING)
                                    ) {
                                        toProcess.set(i, Utils.color("&n" + arenaName));
                                        continue main;
                                    }
                                }
                            }
                        }

                        if(toProcess.isEmpty()) {
                            sender.sendMessage(MessagesYML.Errors.NO_ARENAS.withPrefix(arg));
                            return;
                        }

                        String list = Utils.listStrings(toProcess);
                        sender.sendMessage(MessagesYML.ARENA_LIST.withPrefix(arg)
                                .replace("$list$", list)
                                .replace("$total$", toProcess.size() + "")
                        );
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "kick":
                    if (sender.hasPermission(pluginNameLower + ".command.arena.kick")) {
                        if (args.length > 2) {
                            if (Bukkit.getServer().getOnlinePlayers().stream().noneMatch(pl -> pl.getName().equalsIgnoreCase(args[2]))) {
                                sender.sendMessage(MessagesYML.Errors.NOT_ONLINE.withPrefix(arg).replace("$player$", args[2]));
                                return;
                            }

                            Player target = Bukkit.getPlayer(args[2]);
                            DuelsPlayer dtarget = DuelsPlayer.fetch(target.getUniqueId());

                            if(dtarget.isPartOfMatch()) {
                                Match match = dtarget.anyMatch();
                                if(dtarget.isDueller()) {
                                    match.kickDueller(target, true, true);
                                    sender.sendMessage(MessagesYML.KICKED_DUEL_PLAYER.withPrefix(arg)
                                            .replace("$name$", target.getName()));
                                } else if(dtarget.isSpectator()) {
                                    match.kickSpectator(target, true);
                                    sender.sendMessage(MessagesYML.KICKED_SPECTATOR_PLAYER.withPrefix(arg)
                                            .replace("$name$", target.getName()));
                                }
                            } else {
                                target.sendMessage(MessagesYML.Errors.PLAYER_NOT_IN_MATCH.withPrefix(arg));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena kick <player>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "join":
                    if (sender.hasPermission(pluginNameLower + ".command.arena.join")) {
                        if (args.length > 3) {
                            if (Bukkit.getServer().getOnlinePlayers().stream().noneMatch(pl -> pl.getName().equalsIgnoreCase(args[2]))) {
                                sender.sendMessage(MessagesYML.Errors.NOT_ONLINE.withPrefix(arg).replace("$player$", args[2]));
                                return;
                            }

                            if(!plugin.getSQL().arenaExists(args[3])) {
                                sender.sendMessage(MessagesYML.Errors.INVALID_ARENA.withPrefix(arg));
                                return;
                            }

                            Player target = Bukkit.getPlayer(args[2]);
                            DuelsPlayer dtarget = DuelsPlayer.fetch(target.getUniqueId());

                            if(!dtarget.isDueller()) {
                                Match match = MatchManager.findMatch(args[3]);
                                assert match != null;

                                match.joinDueller(target);
                                sender.sendMessage(MessagesYML.JOINED_DUEL_PLAYER.withPrefix(arg)
                                        .replace("$name$", target.getName())
                                        .replace("$arena$", args[3])
                                );
                            } else {
                                target.sendMessage(MessagesYML.Errors.ALREADY_PLAYING.withPrefix(arg));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena join <player> <arena>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "start":
                    if (sender.hasPermission(pluginNameLower + ".command.arena.start")) {
                        if (args.length > 2) {
                            if(!plugin.getSQL().arenaExists(args[2])) {
                                sender.sendMessage(MessagesYML.Errors.INVALID_ARENA.withPrefix(arg));
                                return;
                            }

                            Match match = MatchManager.findMatch(args[2]);
                            assert match != null;

                            if(match.getStatus() != MatchStatus.STARTING && match.getStatus() != MatchStatus.ONGOING) {
                                if(match.getDuellerCount() < 2) {
                                    sender.sendMessage(MessagesYML.Errors.NO_PARTICIPANTS.withPrefix(arg));
                                    return;
                                }

                                match.start();
                                match.broadcast(dp -> MessagesYML.STARTED_MATCH.withPrefix(dp.getPlayer()));
                            }
                        } else {
                            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                    .replace("$format$", "/duels arena start <arena>"));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
            }
        } else {
            sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                    .replace("$format$", "/duels arena <create/delete/setpriority/rename> ..."));
        }
    }
}
