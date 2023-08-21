package io.github.idoomful.assassinsduels.events;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import io.github.idoomful.assassinscurrencycore.data.SQL.TransactionLog;
import io.github.idoomful.assassinscurrencycore.utils.ConfigPair;
import io.github.idoomful.assassinscurrencycore.utils.CurrencyUtils;
import io.github.idoomful.assassinscurrencycore.utils.Economy;
import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.arena.Arena;
import io.github.idoomful.assassinsduels.arena.ArenaDuelType;
import io.github.idoomful.assassinsduels.arena.ArenaPosition;
import io.github.idoomful.assassinsduels.configuration.MessagesYML;
import io.github.idoomful.assassinsduels.configuration.SettingsYML;
import io.github.idoomful.assassinsduels.gui.BettingGUI;
import io.github.idoomful.assassinsduels.match.*;
import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import io.github.idoomful.assassinsduels.utils.Caching;
import io.github.idoomful.assassinsduels.utils.Events;
import io.github.idoomful.assassinsduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class EventsClass implements Listener {
    private final DMain main;

    public EventsClass(DMain main) {
        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);

        Events.listen(main, AsyncPlayerChatEvent.class, e -> {
            if(main.getChatModes().findUser(e.getPlayer())) {
                Player player = e.getPlayer();
                UUID uuid = player.getUniqueId();
                String message = e.getMessage();

                e.setCancelled(true);

                switch(main.getChatModes().getContext(player)) {
                    case ARENA_CREATE:
                        if(message.equalsIgnoreCase("-cancel")) {
                            Caching.eraseCache(player);
                            player.sendMessage(MessagesYML.SETUP_CANCEL.withPrefix(player));
                            return;
                        }

                        if(main.getSetupStepsCaching().getCacheMap().get(uuid) == 1) {
                            try {
                                ArenaDuelType type = ArenaDuelType.valueOf(message.toUpperCase());
                                main.getArenaCaching().getCacheMap().get(uuid).getSettings().setDuelType(type);
                                main.getArenaCaching().getCacheMap().get(uuid).getSettings().setMaxPlayers(type.getMult() * 2);
                                main.getSetupStepsCaching().getCacheMap().put(uuid, 2);
                                main.getPositionCountCaching().getCacheMap().put(uuid, 0);
                                player.sendMessage(MessagesYML.ARENA_SETUP_2.withPrefix(player)
                                        .replace("$type$", message)
                                        .replace("$count$", (type.getMult() * 2) + ""));
                            } catch(IllegalArgumentException ie) {
                                player.sendMessage(MessagesYML.Errors.INVALID_ARENA_TYPE.withPrefix(player));
                            }
                        } else if(main.getSetupStepsCaching().getCacheMap().get(uuid) == 2) {
                            try {
                                if(message.equalsIgnoreCase("-set")) {
                                    Arena arena = main.getArenaCaching().getCacheMap().get(uuid);
                                    arena.getSettings().setSpectatorsPosition(new ArenaPosition(player.getLocation()));

                                    main.getSetupStepsCaching().getCacheMap().put(uuid, 3);
                                    main.getPositionCountCaching().getCacheMap().put(uuid, 1);

                                    String count = (arena.getSettings().getDuelType().getMult() * 2) + "";

                                    player.sendMessage(MessagesYML.ARENA_SETUP_3.withPrefix(player)
                                            .replace("$type$", arena.getSettings().getDuelType().name())
                                            .replace("$count$", count)
                                    );
                                } else e.setCancelled(false);
                            } catch(IllegalArgumentException ie) {
                                player.sendMessage(MessagesYML.Errors.INVALID_ARENA_TYPE.withPrefix(player));
                            }
                        } else if(main.getSetupStepsCaching().getCacheMap().get(uuid) == 3) {
                            try {
                                if(message.equalsIgnoreCase("-set")) {
                                    Arena arena = main.getArenaCaching().getCacheMap().get(uuid);
                                    final int cachedPosCount = main.getPositionCountCaching().getCacheMap().get(uuid);

                                    if(cachedPosCount == arena.getSettings().getMaxPlayers()) {
                                        main.getArenaCaching().getCacheMap().get(uuid).getSettings().getPositions().add(new ArenaPosition(player.getLocation()));
                                        main.getArenaCaching().getCacheMap().get(uuid).getSettings().setPriority(1);

                                        main.getSQL().newArenaEntry(main.getArenaCaching().getCacheMap().get(uuid));

                                        Caching.eraseCache(player);
                                        player.sendMessage(MessagesYML.SETUP_COMPLETE.withPrefix(player).replace("$name$", arena.getName()));
                                    } else {
                                        main.getPositionCountCaching().getCacheMap().put(uuid, cachedPosCount + 1);

                                        final int remaining = arena.getSettings().getMaxPlayers() - cachedPosCount;

                                        main.getArenaCaching().getCacheMap().get(uuid).getSettings().getPositions().add(new ArenaPosition(player.getLocation()));
                                        player.sendMessage(MessagesYML.POSITION_SAVED.withPrefix(player)
                                                .replace("$count$", remaining + ""));
                                    }
                                } else e.setCancelled(false);
                            } catch(IllegalArgumentException ie) {
                                player.sendMessage(MessagesYML.Errors.INVALID_ARENA_TYPE.withPrefix(player));
                            }
                        }
                        break;
                }
            }
        });

        Events.listen(main, InventoryCloseEvent.class, e -> {
            if(e.getInventory().getHolder() instanceof BettingGUI) {
                for(int i = 0; i < e.getInventory().getContents().length; i++) {
                    ItemStack item = e.getInventory().getContents()[i];
                    if(item == null) continue;
                    if(!NBTEditor.contains(item, "CurrencyId")) continue;

                    e.getPlayer().getInventory().addItem(item);
                    e.getInventory().setItem(i, null);
                }
            }
        });

        Events.listen(main, PlayerCommandPreprocessEvent.class, e -> {
            Player pl = e.getPlayer();
            String command = e.getMessage();
            DuelsPlayer dp = DuelsPlayer.fetch(pl.getUniqueId());

            if(dp.isPartOfMatch() && dp.isDueller() && !pl.hasPermission("assassinsduels.bypass.blocked-cmds")) {
                if(SettingsYML.Duels.WHITELIST_COMMANDS.getStringList(pl).stream().noneMatch(command::contains)) {
                    pl.sendMessage(MessagesYML.Errors.COMMANDS_NOT_ALLOWED.withPrefix(pl));
                    e.setCancelled(true);
                }
            }
        });

        Events.listen(main, InventoryClickEvent.class, e -> {
            if(e.getClickedInventory() == null) return;
            if(e.getCurrentItem() == null) return;
            if(e.getCurrentItem().getType() == Material.AIR) return;

            boolean clickedBottom = e.getView().getBottomInventory().equals(e.getClickedInventory())
                    && e.getView().getTopInventory().getHolder() instanceof BettingGUI;

            if(e.getClickedInventory().getHolder() instanceof BettingGUI || (clickedBottom && e.isShiftClick())) {
                if(!NBTEditor.contains(e.getCurrentItem(), "CurrencyId")) {
                    // If the player confirmed the bet
                    if(e.getCurrentItem().isSimilar(SettingsYML.Duels.BettingGUI.ITEMS.getItem("o"))) {
                        Player player = (Player) e.getWhoClicked();
                        DuelsPlayer dp = DuelsPlayer.fetch(player.getUniqueId());
                        Match match = dp.anyMatch();

                        ArrayList<ConfigPair<Integer, String>> bets = new ArrayList<>();

                        for(ItemStack money : e.getView().getTopInventory()) {
                            if(money == null) continue;
                            if(NBTEditor.contains(money, "CurrencyId")) {
                                String currency = NBTEditor.getString(money, "CurrencyId");
                                bets.add(new ConfigPair<>(money.getAmount(), currency));
                            }
                        }

                        int betGroupID = main.getPlayerBets().getCacheMap().get(player.getUniqueId());
                        DuellerGroup group = match.getGroups().get(betGroupID);

                        group.getBets().put(player.getUniqueId(), new Bet(bets, group, player.getUniqueId()));
                        match.getGroups().put(betGroupID, group);

                        DuelsPlayer leader = group.getLeader();
                        player.sendMessage(MessagesYML.BET_SUBMIT.withPrefix(player)
                                .replace("$name$", leader.getName()));

                        CurrencyUtils.logTransaction(new TransactionLog(
                                player.getName(),
                                "AssassinsDuels",
                                "bet money on " + leader.getName(),
                                bets
                        ).currenciesWithdraw(true));

                        e.setCurrentItem(null);
                        for(int i = 0; i < e.getView().getTopInventory().getContents().length; i++) {
                            ItemStack item = e.getView().getTopInventory().getContents()[i];
                            if(item == null) continue;
                            e.getView().getTopInventory().setItem(i, null);
                        }

                        Bukkit.getScheduler().scheduleSyncDelayedTask(main, player::closeInventory, 5);
                    }

                    e.setCancelled(true);
                }
            }
        });

        Events.listen(main, PlayerJoinEvent.class, e -> {
            UUID uuid = e.getPlayer().getUniqueId();
            main.getPlayers().put(uuid, main.getSQL().getPlayer(e.getPlayer()));
            DuelsPlayer dp = DuelsPlayer.fetch(uuid);

            if(!dp.getMadeBets().isEmpty()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                    CurrencyUtils.logTransaction(new TransactionLog(
                            e.getPlayer().getName(),
                            "AssassinsDuels",
                            "returned duel bets on join",
                            dp.getMadeBets()
                    ).currenciesAdded(true));

                    dp.returnBets();
                    main.getPlayers().put(uuid, dp);
                    main.getSQL().setPlayer(dp);
                    e.getPlayer().sendMessage(MessagesYML.RETURNED_BETS.withPrefix(e.getPlayer()));
                }, 10);
            }
        });

        Events.listen(main, PlayerQuitEvent.class, e -> {
            UUID uuid = e.getPlayer().getUniqueId();
            DuelsPlayer dp = DuelsPlayer.fetch(uuid);

            if(dp.isPartOfMatch()) {
                Match match = dp.anyMatch();

                if(dp.isDueller()) match.kickDueller(e.getPlayer(), true, true);
                else if(dp.isSpectator()) match.kickSpectator(e.getPlayer(), true);
            }

            dp.leaveTeam();

            main.getSQL().setPlayer(dp);
            main.getPlayers().remove(uuid);
            Caching.eraseCache(e.getPlayer());
        });
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        DuelsPlayer dp = DuelsPlayer.fetch(player.getUniqueId());

        if(dp.isPartOfMatch() && dp.isDueller()) {
            Match match = dp.anyMatch();
            if(match.getStatus() == MatchStatus.STARTING
                    || (match.getStatus() == MatchStatus.PAUSED && match.getPreviousStatus() == MatchStatus.STARTING)
            ) {
                e.setTo(e.getFrom());
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if(e.getEntity().hasMetadata("NPC") || e.getDamager().hasMetadata("NPC")) return;

        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player attacker = (Player) e.getDamager();
            Player attacked = (Player) e.getEntity();

            DuelsPlayer dp = DuelsPlayer.fetch(attacked.getUniqueId());

            if(dp.isPartOfMatch()) {
                Match match = dp.anyMatch();

                if(MatchManager.areTeammates(attacker, attacked) || match.getStatus() == MatchStatus.PAUSED) {
                    if(match.getStatus() != MatchStatus.PAUSED) attacker.sendMessage(MessagesYML.Errors.TEAMMATE_HIT.withPrefix(attacker));
                    else attacker.sendMessage(MessagesYML.Errors.PAUSE_HIT.withPrefix(attacker));

                    e.setCancelled(true);
                }
            }
        }
    }

    // Remove player from spectator if they teleport somewhere else
    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        DuelsPlayer dp = DuelsPlayer.fetch(player.getUniqueId());

        if(dp == null) return;

        if(dp.isSpectator() && e.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            Match match = dp.anyMatch();
            Location spec = match.getArena().getSettings().getSpectatorsPosition().getLocation();

            if(spec.getWorld().getNearbyEntities(spec, 3, 3, 3).stream().noneMatch(en -> {
                if(en instanceof Player) {
                    Player pl = (Player) en;
                    return pl.getName().equals(player.getName());
                }

                return false;
            })) {
                if(dp.hasBets()) {
                    player.sendMessage(MessagesYML.Errors.BET_CANNOT_LEAVE.withPrefix(player));
                    e.setCancelled(true);
                    return;
                }

                match.kickSpectator(player, false);
                player.sendMessage(MessagesYML.LEFT_SPECTATOR.withPrefix(player)
                        .replace("$arena$", match.getArena().getName()));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if(e.getEntity().hasMetadata("NPC")) return;

        Player player = e.getEntity();
        DuelsPlayer dp = DuelsPlayer.fetch(player.getUniqueId());

        if (dp.isPartOfMatch()) {
            player.spigot().respawn();

            Match match = dp.anyMatch();

            if (match.getStatus() == MatchStatus.ONGOING) {
                match.broadcast(dpl -> MessagesYML.PLAYER_ELIMINATED.color(dpl.getPlayer()).replace("$player$", player.getName()));
                match.getEveryone().forEach(ev -> SettingsYML.SFX.ELIMINATION_SOUND.playSound(ev.getPlayer()));
            }

            match.joinSpectator(player);
        }
    }
}
