package io.github.idoomful.assassinsduels;

import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class PAPI extends PlaceholderExpansion {
    private final DMain plugin;

    public PAPI(DMain main) {
        plugin = main;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "assassinsduels";
    }

    @Override
    public @NotNull String getAuthor() {
        return "iDoomful";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        switch(identifier) {
            case "wins":
                return DuelsPlayer.fetch(player.getUniqueId()).getWins() + "";
            case "loses":
                return DuelsPlayer.fetch(player.getUniqueId()).getLoses() + "";
        }

        return null;
    }
}
