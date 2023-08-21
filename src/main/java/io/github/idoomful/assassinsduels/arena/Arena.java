package io.github.idoomful.assassinsduels.arena;

public class Arena {
    private String name;
    private final ArenaSettings settings;

    public Arena(String name, ArenaSettings settings) {
        this.name = name;
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public ArenaSettings getSettings() {
        return settings;
    }

    public void setName(String name) {
        this.name = name;
    }
}
