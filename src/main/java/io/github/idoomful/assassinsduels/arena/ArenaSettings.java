package io.github.idoomful.assassinsduels.arena;

import com.google.gson.Gson;

import java.util.ArrayList;

public class ArenaSettings {
    private ArenaDuelType duelType;
    private int maxPlayers;
    private int priority;
    private ArrayList<ArenaPosition> positions;
    private ArenaPosition spectatorsPosition;

    public ArenaSettings(ArenaDuelType duelType, int maxPlayers, int priority, ArrayList<ArenaPosition> positions, ArenaPosition spectatorsPosition) {
        this.duelType = duelType;
        this.maxPlayers = maxPlayers;
        this.priority = priority;
        this.positions = positions;
        this.spectatorsPosition = spectatorsPosition;
    }

    public ArenaSettings() {
        this.duelType = ArenaDuelType.VS1;
        this.maxPlayers = 2;
        this.positions = new ArrayList<>();
        this.spectatorsPosition = null;
    }

    public ArenaDuelType getDuelType() {
        return duelType;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public ArrayList<ArenaPosition> getPositions() {
        return positions;
    }

    public int getPriority() {
        return priority;
    }

    public void setDuelType(ArenaDuelType duelType) {
        this.duelType = duelType;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setPositions(ArrayList<ArenaPosition> positions) {
        this.positions = positions;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setSpectatorsPosition(ArenaPosition spectatorsPosition) {
        this.spectatorsPosition = spectatorsPosition;
    }

    public ArenaPosition getSpectatorsPosition() {
        return spectatorsPosition;
    }

    public String positionsToJSON() {
        ArrayList<String> posJSONs = new ArrayList<>();
        getPositions().forEach(pos -> posJSONs.add(pos.toJSON()));
        return new Gson().toJson(posJSONs);
    }

    public String spectatorPositionToJSON() {
        return new Gson().toJson(getSpectatorsPosition());
    }
}
