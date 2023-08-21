package io.github.idoomful.assassinsduels;

import io.github.idoomful.assassinscurrencycore.utils.ConfigPair;
import io.github.idoomful.assassinscurrencycore.utils.Economy;
import io.github.idoomful.assassinsduels.arena.Arena;
import io.github.idoomful.assassinsduels.match.*;
import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import io.github.idoomful.assassinsduels.setup.ChatMode;
import io.github.idoomful.assassinsduels.commands.CommandsClass;
import io.github.idoomful.assassinsduels.configuration.ConfigManager;
import io.github.idoomful.assassinsduels.data.SQL.Lite;
import io.github.idoomful.assassinsduels.events.EventsClass;
import io.github.idoomful.assassinsduels.utils.Caching;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DMain extends JavaPlugin {
    private final String version = getDescription().getVersion();

    private static DMain plugin;
    private ConfigManager<DMain> conf;
    private Lite<DMain> sql;
    private boolean hasPAPI;

    private ChatMode chatModes;
    private Caching<UUID, Arena> arenaCaching;
    private Caching<UUID, Integer> setupStepsCaching;
    private Caching<UUID, Integer> positionCountCaching;
    private Caching<UUID, Integer> playerBets;

    private final HashMap<UUID, DuelsPlayer> players = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        conf = new ConfigManager<>(this);
        sql = new Lite<>(this);
        new EventsClass(this);
        new CommandsClass(this);

        hasPAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        if(hasPAPI) {
            getLogger().info("'PlaceholderAPI' was found, hooking our expansion for this plugin..");
            PAPI papi = new PAPI(this);

            if(papi.canRegister()) {
                papi.register();
                getLogger().info("'PlaceholderAPI' has been hooked successfully, now you can use our placeholders!");
            }
        } else {
            getLogger().warning("'PlaceholderAPI' was not found, our PlaceholderAPI expansion for this plugin won't be available..");
        }

        chatModes = new ChatMode(this);
        arenaCaching = new Caching<>();
        setupStepsCaching = new Caching<>();
        positionCountCaching = new Caching<>();
        playerBets = new Caching<>();

        MatchManager.updateMatches();

        Bukkit.getOnlinePlayers().forEach(pl ->
            getPlayers().put(pl.getUniqueId(), getSQL().getPlayer(pl))
        );
    }

    @Override
    public void onDisable() {
        MatchManager.getMatches().forEach((priority, list) -> list.forEach(Match::cancel));

        for(Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            DuelsPlayer dp = getPlayers().get(uuid);

            if(dp == null) continue;

            getSQL().setPlayer(getPlayers().get(uuid));
        }
    }

    public static DMain getInstance() {
        return plugin;
    }
    public String getVersion() {
        return version;
    }
    public ConfigManager<DMain> getConfigs() {
        return conf;
    }
    public Lite<DMain> getSQL() {
        return sql;
    }
    public boolean hasPAPI() {
        return hasPAPI;
    }

    public ChatMode getChatModes() {
        return chatModes;
    }
    public Caching<UUID, Arena> getArenaCaching() {
        return arenaCaching;
    }
    public Caching<UUID, Integer> getSetupStepsCaching() {
        return setupStepsCaching;
    }
    public Caching<UUID, Integer> getPositionCountCaching() {
        return positionCountCaching;
    }
    public Caching<UUID, Integer> getPlayerBets() {
        return playerBets;
    }

    public HashMap<UUID, DuelsPlayer> getPlayers() {
        return players;
    }
}
