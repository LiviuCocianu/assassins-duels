package io.github.idoomful.assassinsduels.match;

import io.github.idoomful.assassinsduels.arena.ArenaDuelType;
import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DuellerGroup {
    private final Match match;
    private final ArrayList<DuelsPlayer> members;
    private DuelsPlayer leader;
    private final ArrayList<Player> losers;
    private final HashMap<UUID, Bet> bets;

    public DuellerGroup(Match match, ArrayList<DuelsPlayer> members, HashMap<UUID, Bet> bets) {
        this.match = match;
        this.members = members;
        this.leader = null;
        this.losers = new ArrayList<>();
        this.bets = bets;
    }

    public ArrayList<DuelsPlayer> getMembers() {
        return members;
    }

    public DuelsPlayer getLeader() {
        return leader;
    }

    public void setLeader(DuelsPlayer leader) {
        this.leader = leader;
    }

    public boolean contains(Player player) {
        return members.stream().anyMatch(dueller -> dueller.getName().equals(player.getName()));
    }

    public HashMap<UUID, Bet> getBets() {
        return bets;
    }

    public Match getMatch() {
        return match;
    }

    public ArrayList<Player> getLosers() {
        return losers;
    }
}
