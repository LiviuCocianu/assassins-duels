package io.github.idoomful.assassinsduels.match;

import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import io.github.idoomful.assassinsduels.arena.Arena;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public interface Match {
    MatchStatus getStatus();
    void setStatus(MatchStatus status);
    HashMap<Integer, DuellerGroup> getGroups();
    default ArrayList<DuelsPlayer> getEveryone() {
        ArrayList<DuelsPlayer> all = new ArrayList<>(getDuellers());
        all.addAll(getSpectators());
        return all;
    }

    default ArrayList<DuelsPlayer> getDuellers() {
        ArrayList<DuelsPlayer> members = new ArrayList<>();
        getGroups().forEach((id, group) -> members.addAll(group.getMembers()));
        return members;
    }

    ArrayList<DuelsPlayer> getSpectators();
    default int getDuellerCount() {
        return getDuellers().size();
    }
    default int getSpectatorCount() {
        return getSpectators().size();
    }
    default int getTotalCount() {
        return getDuellerCount() + getSpectatorCount();
    }
    Arena getArena();
    MatchStatus getPreviousStatus();
    default boolean isDueller(Player player) {
        return getDuellers().stream().anyMatch(dueller -> dueller.getName().equals(player.getName()));
    }
    default boolean isSpectator(Player player) {
        return getSpectators().stream().anyMatch(spec -> spec.getName().equals(player.getName()));
    }
    boolean joinDueller(Player player);
    boolean joinSpectator(Player player);
    void kickDueller(Player player, boolean teleport, boolean check);
    void kickSpectator(Player player, boolean teleport);
    void broadcast(Function<DuelsPlayer, String> message);
    int getGroupIdFor(DuelsPlayer dp);
    void start();
    void cancel();
    boolean pause();
    void skipCountdown();
}
