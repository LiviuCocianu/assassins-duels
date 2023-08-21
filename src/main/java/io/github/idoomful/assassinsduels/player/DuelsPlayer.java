package io.github.idoomful.assassinsduels.player;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.idoomful.assassinscurrencycore.utils.ConfigPair;
import io.github.idoomful.assassinscurrencycore.utils.Economy;
import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.match.Bet;
import io.github.idoomful.assassinsduels.match.DuellerGroup;
import io.github.idoomful.assassinsduels.match.Match;
import io.github.idoomful.assassinsduels.match.MatchManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DuelsPlayer {
    private final Player player;
    private final ArrayList<ConfigPair<Integer, String>> bets;
    private int wins, loses;

    private final ArrayList<DuelsPlayer> teammates;
    private DuelsPlayer teamInvitation, duelInvitation;

    public DuelsPlayer(Player player, ArrayList<ConfigPair<Integer, String>> bets, int wins, int loses) {
        this.player = player;
        this.bets = bets;
        this.wins = wins;
        this.loses = loses;

        this.teammates = new ArrayList<>();
        this.teamInvitation = null;
        this.duelInvitation = null;
    }

    public enum InviteType {
        TEAM, DUEL
    }

    public static DuelsPlayer fetch(UUID uuid) {
        return DMain.getInstance().getPlayers().get(uuid);
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return player.getName();
    }

    public UUID getUUID() {
        return player.getUniqueId();
    }

    public ArrayList<ConfigPair<Integer, String>> getMadeBets() {
        return bets;
    }

    public String betsToJSON() {
        ArrayList<String> bets = new ArrayList<>();
        getMadeBets().forEach(bet -> bets.add(new Gson().toJson(bet)));
        return new Gson().toJson(bets);
    }

    public static ArrayList<ConfigPair<Integer, String>> betsFromJSONList(String json) {
        Gson gson = new Gson();
        ArrayList<ConfigPair<Integer, String>> output = new ArrayList<>();

        ArrayList<String> jsonList = gson.fromJson(json, new TypeToken<ArrayList<String>>(){}.getType());
        jsonList.forEach(bet -> output.add(gson.fromJson(bet, new TypeToken<ConfigPair<Integer, String>>(){}.getType())));
        return output;
    }

    public int getWins() {
        return wins;
    }

    public int getLoses() {
        return loses;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLoses(int loses) {
        this.loses = loses;
    }

    public ArrayList<DuelsPlayer> getTeammates() {
        return teammates;
    }

    public Match anyMatch() {
        for(ArrayList<Match> matchList : MatchManager.getMatches().values()) {
            for(int i = 0; i < matchList.size(); i++) {
                Match match = matchList.get(i);

                if(match.getEveryone().stream().anyMatch(ev -> ev.getName().equals(getName()))) {
                    return MatchManager.getMatches().get(match.getArena().getSettings().getPriority()).get(i);
                }
            }
        }

        return null;
    }

    public boolean isPartOfMatch() {
        return anyMatch() != null;
    }

    public Bet anyBets() {
        for (ArrayList<Match> matchList : MatchManager.getMatches().values()) {
            for(Match match : matchList) {
                for (Map.Entry<Integer, DuellerGroup> pair2 : match.getGroups().entrySet()) {
                    if (pair2.getValue().getBets().containsKey(getUUID())) {
                        return pair2.getValue().getBets().get(getUUID());
                    }
                }
            }
        }

        return null;
    }

    public void withdrawBets() {
        for(ArrayList<Match> matchList : MatchManager.getMatches().values()) {
            for(Match match : matchList) {
                for (Map.Entry<Integer, DuellerGroup> pair2 : match.getGroups().entrySet()) {
                    if (pair2.getValue().getBets().containsKey(getUUID())) {
                        match.getGroups().get(pair2.getKey()).getBets().remove(getUUID());
                    }
                }
            }
        }
    }

    public void returnBets() {
        if(!getMadeBets().isEmpty()) {
            for(ConfigPair<Integer, String> pair : getMadeBets()){
                getPlayer().getInventory().addItem(Economy.Currency.getMarkedItem(pair.getValue(), pair.getKey()));
            }

            getMadeBets().clear();
        }
    }

    public boolean isSpectator() {
        for(ArrayList<Match> matchList : MatchManager.getMatches().values()) {
            for(Match match : matchList) {
                boolean b = match.getSpectators().stream().anyMatch(sp -> sp.getName().equals(getName()));
                if(b) return true;
            }
        }

        return false;
    }

    public boolean isDueller() {
        for(ArrayList<Match> matchList : MatchManager.getMatches().values()) {
            for (Match match : matchList) {
                boolean b = match.getDuellers().stream().anyMatch(du -> du.getName().equals(getName()));
                if(b) return true;
            }
        }

        return false;
    }

    public DuelsPlayer getTeamLeader() {
        for(DuelsPlayer dp : DMain.getInstance().getPlayers().values()) {
            if(dp.hasTeam() && dp.getTeammates().stream().anyMatch(tm -> tm.getName().equals(getName()))) return dp;
        }

        return null;
    }

    public boolean hasBets() {
        return anyBets() != null;
    }

    public DuelsPlayer getInvitation(InviteType type) {
        if(type == InviteType.TEAM) return teamInvitation;
        else if(type == InviteType.DUEL) return duelInvitation;
        else return null;
    }

    public boolean isInvited(InviteType type) {
        if(type == InviteType.TEAM) return teamInvitation != null;
        else if(type == InviteType.DUEL) return duelInvitation != null;
        else return false;
    }

    public void setInvitationOf(DuelsPlayer invited, InviteType type) {
        if(type == InviteType.TEAM) teamInvitation = invited;
        else if(type == InviteType.DUEL) duelInvitation = invited;
        DMain.getInstance().getPlayers().put(getUUID(), this);
    }

    public void clearInvitation(InviteType type) {
        if(type == InviteType.TEAM) this.teamInvitation = null;
        else if(type == InviteType.DUEL) this.duelInvitation = null;
        DMain.getInstance().getPlayers().put(getUUID(), this);
    }

    public void acceptTeamInvitation() {
        getInvitation(InviteType.TEAM).getTeammates().add(this);
        DMain.getInstance().getPlayers().put(getInvitation(InviteType.TEAM).getUUID(), getInvitation(InviteType.TEAM));
        clearInvitation(InviteType.TEAM);
    }

    public boolean hasTeam() {
        return !teammates.isEmpty();
    }

    public boolean isPartOfTeam() {
        for(Map.Entry<UUID, DuelsPlayer> pair : DMain.getInstance().getPlayers().entrySet()) {
            if(pair.getValue().getTeammates().stream().anyMatch(teammate -> teammate.getName().equals(getName()))) {
                return true;
            }
        }

        return false;
    }

    public DuelsPlayer anyTeamleader() {
        if(isPartOfTeam()) {
            for(Map.Entry<UUID, DuelsPlayer> pair : DMain.getInstance().getPlayers().entrySet()) {
                if(pair.getValue().getTeammates().stream().anyMatch(teammate -> teammate.getName().equals(getName()))) {
                    return pair.getValue();
                }
            }
        }

        return null;
    }

    public void leaveTeam() {
        if(isPartOfTeam())
            for(DuelsPlayer dpl : DMain.getInstance().getPlayers().values()) {
                dpl.getTeammates().remove(this);
            }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DuelsPlayer that = (DuelsPlayer) o;
        return wins == that.wins &&
                loses == that.loses &&
                player.equals(that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, wins, loses);
    }
}
