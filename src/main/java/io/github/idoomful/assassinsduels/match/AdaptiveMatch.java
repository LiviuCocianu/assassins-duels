package io.github.idoomful.assassinsduels.match;

import io.github.idoomful.assassinscurrencycore.data.SQL.TransactionLog;
import io.github.idoomful.assassinscurrencycore.utils.CurrencyUtils;
import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.arena.Arena;
import io.github.idoomful.assassinsduels.arena.ArenaDuelType;
import io.github.idoomful.assassinsduels.configuration.MessagesYML;
import io.github.idoomful.assassinsduels.configuration.SettingsYML;
import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import io.github.idoomful.assassinsduels.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class AdaptiveMatch implements Match {
    private final Arena arena;
    private MatchStatus status;
    private final HashMap<Integer, DuellerGroup> groups;
    private final ArrayList<DuelsPlayer> spectators;

    private AtomicInteger seconds = new AtomicInteger(SettingsYML.Duels.TIME_BEFORE_MATCH.getInt());
    private AtomicInteger repeaterID = new AtomicInteger();
    private MatchStatus previous = MatchStatus.STANDBY;

    public AdaptiveMatch(Arena arena) {
        this.arena = arena;
        this.groups = new HashMap<>();
        groups.put(1, new DuellerGroup(this, new ArrayList<>(), new HashMap<>()));
        groups.put(2, new DuellerGroup(this, new ArrayList<>(), new HashMap<>()));

        this.spectators = new ArrayList<>();
        this.status = MatchStatus.STANDBY;
    }

    @Override
    public MatchStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    @Override
    public HashMap<Integer, DuellerGroup> getGroups() {
        return groups;
    }

    @Override
    public ArrayList<DuelsPlayer> getSpectators() {
        return spectators;
    }

    @Override
    public Arena getArena() {
        return arena;
    }

    @Override
    public MatchStatus getPreviousStatus() {
        return previous;
    }

    @Override
    public boolean joinDueller(Player player) {
        DuelsPlayer dp = DMain.getInstance().getSQL().getPlayer(player);

        if(getDuellerCount() < getArena().getSettings().getMaxPlayers() && !isDueller(player)) {
            int groupMax = getArena().getSettings().getDuelType().getMult();
            int position = getDuellerCount();

            for(DuellerGroup group : getGroups().values()) {
                if(group.getMembers().size() == groupMax) continue;
                if(group.contains(player)) continue;

                group.getMembers().add(dp);
                break;
            }

            player.teleport(getArena().getSettings().getPositions().get(position).getLocation());
            return true;
        }

        return false;
    }

    @Override
    public boolean joinSpectator(Player player) {
        if(!isSpectator(player)) {
            if(isDueller(player)) kickDueller(player, false, true);
            else player.teleport(getArena().getSettings().getSpectatorsPosition().getLocation());

            getSpectators().add(DuelsPlayer.fetch(player.getUniqueId()));
            Utils.sendActionText(player, MessagesYML.PLAYER_SPECTATOR.color(player));
            return true;
        }

        return false;
    }

    @Override
    public void kickDueller(Player player, boolean teleport, boolean check) {
        DuelsPlayer dp = DuelsPlayer.fetch(player.getUniqueId());

        if(player.isOnline()) {
            if(teleport) SettingsYML.Duels.TELEPORT_ON_KICK.getLocation().getBlock();

            if(teleport && SettingsYML.Duels.TELEPORT_ON_KICK_ON.getBoolean())
                player.teleport(SettingsYML.Duels.TELEPORT_ON_KICK.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            else player.teleport(getArena().getSettings().getSpectatorsPosition().getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DMain.getInstance(), () -> {
                if(SettingsYML.Duels.COMMANDS_ON_KICK_ON.getBoolean()) {
                    Utils.placeholder(player, SettingsYML.Duels.COMMANDS_ON_KICK.getStringList(player)).forEach(line -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.replace("$player$", player.getName()));
                    });
                }
            }, 20);

            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
        }

        for(Map.Entry<Integer, DuellerGroup> pair : getGroups().entrySet()) {
            DuellerGroup gr = pair.getValue();

            if(!gr.contains(player)) continue;

            if(gr.getLeader() != null && gr.getLeader().getName().equals(dp.getName())) {
                gr.setLeader(null);
            }

            boolean removed = gr.getMembers().removeIf(pl -> pl.getName().equals(player.getName()));

            if(removed) {
                if(check) gr.getLosers().add(player);
                getGroups().put(pair.getKey(), gr);
                break;
            }
        }

        if(check) {
            for (Map.Entry<Integer, DuellerGroup> pair : getGroups().entrySet()) {
                int order = pair.getKey();
                DuellerGroup loserGroup = pair.getValue();
                DuellerGroup winnerGroup = getGroups().get(order == 1 ? 2 : 1);

                if (loserGroup.getMembers().isEmpty() && loserGroup.getLeader() == null) {
                    loserGroup.getLosers().forEach(lo ->
                            loserGroup.getMembers().add(DuelsPlayer.fetch(lo.getUniqueId())));
                    winnerGroup.getLosers().forEach(wi ->
                            winnerGroup.getMembers().add(DuelsPlayer.fetch(wi.getUniqueId())));

                    loserGroup.getLosers().clear();
                    winnerGroup.getLosers().clear();

                    ArrayList<OfflinePlayer> winningBetters = new ArrayList<>();

                    // Get a list of all the players that bet for the winning team
                    for (Map.Entry<UUID, Bet> bets : winnerGroup.getBets().entrySet()) {
                        winningBetters.add(Bukkit.getPlayer(bets.getKey()));
                    }

                    if (getArena().getSettings().getDuelType() == ArenaDuelType.VS1) {
                        if(winnerGroup.getLeader() != null) {
                            if (winnerGroup.getBets().isEmpty()) {
                                broadcast(dpl -> MessagesYML.PLAYER_WON.color(dpl.getPlayer())
                                        .replace("$name$", winnerGroup.getLeader().getName()));
                            } else {
                                broadcast(dpl -> MessagesYML.PLAYER_WON_BETS.color(dpl.getPlayer())
                                        .replace("$name$", winnerGroup.getLeader().getName()));
                            }
                        }
                    } else {
                        ArrayList<String> names = new ArrayList<>();
                        winnerGroup.getMembers().forEach(me -> names.add(me.getName()));
                        String members = Utils.listStrings(names);

                        if (winnerGroup.getBets().isEmpty()) {
                            broadcast(dpl -> MessagesYML.TEAM_WON.color(dpl.getPlayer())
                                    .replace("$teamlist$", members));
                        } else {
                            broadcast(dpl -> MessagesYML.TEAM_WON_BETS.color(dpl.getPlayer())
                                    .replace("$teamlist$", members));
                        }
                    }

                    // Distribute the bets of the losing betters to the winning betters
                    if (!loserGroup.getBets().isEmpty()) {
                        // If there is no one to give the bets to, give them back to
                        // the losing betters
                        if(winningBetters.isEmpty()) {
                            for (Map.Entry<UUID, Bet> bets : new HashSet<>(loserGroup.getBets().entrySet())) {
                                OfflinePlayer pl = Bukkit.getOfflinePlayer(bets.getKey());

                                CurrencyUtils.logTransaction(new TransactionLog(
                                        pl.getName(),
                                        "AssassinsDuels",
                                        "gave the duel bets back to the owner because there was no one to give the bets to",
                                        bets.getValue().getMoney()
                                ).currenciesAdded(true));

                                bets.getValue().give(pl);
                            }
                        } else {
                            for (Map.Entry<UUID, Bet> bets : new HashSet<>(loserGroup.getBets().entrySet())) {
                                OfflinePlayer pl = Bukkit.getOfflinePlayer(bets.getKey());

                                CurrencyUtils.logTransaction(new TransactionLog(
                                        pl.getName(),
                                        "AssassinsDuels",
                                        "shared the duel bets with " + pl.getName(),
                                        bets.getValue().getMoney()
                                ).currenciesAdded(true));

                                bets.getValue().share(winningBetters);
                            }
                        }
                    }

                    // Give back the bets for the winning team back to the original betters
                    if (!winnerGroup.getBets().isEmpty()) {
                        for (Map.Entry<UUID, Bet> bets : new HashSet<>(winnerGroup.getBets().entrySet())) {
                            OfflinePlayer pl = Bukkit.getOfflinePlayer(bets.getKey());

                            if(pl.isOnline()) {
                                CurrencyUtils.logTransaction(new TransactionLog(
                                        pl.getName(),
                                        "AssassinsDuels",
                                        "returned the duel won amount to betting winner",
                                        bets.getValue().getMoney()
                                ).currenciesAdded(true));
                            } else {
                                CurrencyUtils.logTransaction(new TransactionLog(
                                        pl.getName(),
                                        "AssassinsDuels",
                                        "returned the duel won amount to offline betting winner in database",
                                        bets.getValue().getMoney()
                                ).currenciesAdded(true));
                            }

                            bets.getValue().give(pl);
                        }
                    }

                    // Update stats for each team
                    winnerGroup.getMembers().forEach(wg -> {
                        DuelsPlayer dpl = DuelsPlayer.fetch(wg.getUUID());
                        dpl.setWins(dpl.getWins() + 1);
                    });

                    loserGroup.getMembers().forEach(wg -> {
                        DuelsPlayer dpl = DuelsPlayer.fetch(wg.getUUID());
                        dpl.setLoses(dpl.getLoses() + 1);
                    });

                    // Sound effect for winning
                    getEveryone().forEach(dpl -> {
                        SettingsYML.SFX.DUEL_END_SOUND.playSound(dpl.getPlayer());
                    });

                    setStatus(MatchStatus.FINISHED);

                    // Kick everyone from the duel now that it's over
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DMain.getInstance(), this::cancel,
                            SettingsYML.Duels.TIME_BEFORE_END.getInt() * 20);
                }
            }
        }
    }

    @Override
    public void kickSpectator(Player player, boolean teleport) {
        if(teleport) {
            SettingsYML.Duels.TELEPORT_ON_KICK.getLocation().getBlock();
            DuelsPlayer.fetch(player.getUniqueId()).getPlayer().teleport(SettingsYML.Duels.TELEPORT_ON_KICK.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

        getSpectators().remove(DuelsPlayer.fetch(player.getUniqueId()));
    }

    @Override
    public void broadcast(Function<DuelsPlayer, String> message) {
        getEveryone().forEach(dp -> dp.getPlayer().sendMessage(message.apply(dp)));
    }

    @Override
    public int getGroupIdFor(DuelsPlayer dp) {
        for(Map.Entry<Integer, DuellerGroup> pair : getGroups().entrySet()) {
            if(pair.getValue().contains(dp.getPlayer())) return pair.getKey();
        }

        return -1;
    }

    @Override
    public void start() {
        getDuellers().forEach(du -> du.getPlayer().setGameMode(GameMode.SURVIVAL));

        setStatus(MatchStatus.STARTING);

        broadcast(dp -> MessagesYML.MATCH_STARTING.withPrefix(dp.getPlayer())
                .replace("$seconds$", seconds.get() + ""));

        getDuellers().forEach(dp -> {
            if(dp.getPlayer().hasPermission("assassinsduels.command.skip")) {
                dp.getPlayer().sendMessage(MessagesYML.SKIP_NOTIFY.withPrefix(dp.getPlayer()));
            }
        });

        countdown();
    }

    @Override
    public void cancel() {
        for(DuelsPlayer dp : getEveryone()) {
            Player player = dp.getPlayer();

            if(dp.isDueller()) kickDueller(player, true, false);
            else if(dp.isSpectator()) {
                if(dp.hasBets()) {
                    if(dp.hasBets()) dp.anyBets().give(player);
                }

                kickSpectator(dp.getPlayer(), true);
            }
        }

        seconds.set(SettingsYML.Duels.TIME_BEFORE_MATCH.getInt());
        repeaterID.set(0);
        previous = MatchStatus.STANDBY;

        getGroups().put(1, new DuellerGroup(this, new ArrayList<>(), new HashMap<>()));
        getGroups().put(2, new DuellerGroup(this, new ArrayList<>(), new HashMap<>()));

        setStatus(MatchStatus.STANDBY);
    }

    @Override
    public boolean pause() {
        if(getStatus() == MatchStatus.STARTING) {
            Bukkit.getScheduler().cancelTask(repeaterID.get());
        } else if(getStatus() == MatchStatus.PAUSED) {
            setStatus(previous);
            if(previous == MatchStatus.STARTING) countdown();
            return false;
        }

        previous = getStatus();
        setStatus(MatchStatus.PAUSED);
        return true;
    }

    @Override
    public void skipCountdown() {
        seconds.set(1);
    }

    private void countdown() {
        repeaterID.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(DMain.getInstance(), () -> {
            seconds.set(seconds.get() - 1);

            if(seconds.get() > 0) {
                if(seconds.get() <= SettingsYML.Duels.COUNTDOWN_START_FROM.getInt()) {
                    broadcast(dp -> MessagesYML.MATCH_COUNTDOWN.color(dp.getPlayer()).replace("$seconds$", seconds.get() + ""));
                    getEveryone().forEach(dp -> SettingsYML.SFX.COUNTDOWN_SOUND.playSound(dp.getPlayer()));
                }
            } else {
                setStatus(MatchStatus.ONGOING);
                Bukkit.getScheduler().cancelTask(repeaterID.get());

                getSpectators().forEach(sp -> sp.getPlayer().closeInventory());

                broadcast(dp -> MessagesYML.MATCH_START.withPrefix(dp.getPlayer()));
                getEveryone().forEach(dp -> SettingsYML.SFX.MATCH_START_SOUND.playSound(dp.getPlayer()));
            }
        }, 0,20));
    }
}
