package io.github.idoomful.assassinsduels.match;

import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.arena.ArenaDuelType;
import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class MatchManager {
    private static final TreeMap<Integer, ArrayList<Match>> matches = new TreeMap<>(Collections.reverseOrder());

    public static TreeMap<Integer, ArrayList<Match>> getMatches() {
        return matches;
    }

    public static void updateMatches() {
        getMatches().clear();
        DMain.getInstance().getSQL().getArenas().forEach((name, arena) -> {
            if(!getMatches().containsKey(arena.getSettings().getPriority())) {
                getMatches().put(arena.getSettings().getPriority(), new ArrayList<>());
            }

            getMatches().get(arena.getSettings().getPriority()).add(new AdaptiveMatch(arena));
        });
    }

    public static Match getAvailableMatch(ArenaDuelType type) {
        for(Map.Entry<Integer, ArrayList<Match>> pair : matches.entrySet()) {
            for(int i = 0; i < pair.getValue().size(); i++) {
                Match match = pair.getValue().get(i);

                if(match.getDuellerCount() < match.getArena().getSettings().getMaxPlayers()
                        && match.getArena().getSettings().getDuelType() == type
                        && match.getStatus() == MatchStatus.STANDBY
                ) {
                    return matches.get(pair.getKey()).get(i);
                }
            }
        }

        return null;
    }

    public static ArenaDuelType getDuelTypeFor(int players) {
        try {
            return ArenaDuelType.valueOf("VS" + players);
        } catch (IllegalArgumentException e) {
            return ArenaDuelType.VS1;
        }
    }

    public static boolean areTeammates(Player attacker, Player attacked) {
        DuelsPlayer dattacker = DuelsPlayer.fetch(attacker.getUniqueId());
        DuelsPlayer dattacked = DuelsPlayer.fetch(attacked.getUniqueId());
        if(dattacked == null || dattacker == null) return false;
        return dattacker.getTeammates().contains(dattacked) || dattacked.getTeammates().contains(dattacker);
    }

    public static Match findMatch(String arenaName) {
        for(Map.Entry<Integer, ArrayList<Match>> pair : getMatches().entrySet()) {
            for(int i = 0; i < pair.getValue().size(); i++) {
                Match match = pair.getValue().get(i);
                int priority = match.getArena().getSettings().getPriority();

                if(match.getArena().getName().equalsIgnoreCase(arenaName))
                    return getMatches().get(priority).get(i);
            }
        }

        return null;
    }

    public static boolean hasMatch(String arenaName) {
        return findMatch(arenaName) != null;
    }
}
