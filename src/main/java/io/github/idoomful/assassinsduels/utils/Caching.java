package io.github.idoomful.assassinsduels.utils;

import io.github.idoomful.assassinsduels.DMain;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Caching<K, V> {
    private final HashMap<K, V> cacheMap = new HashMap<>();

    public HashMap<K, V> getCacheMap() {
        return cacheMap;
    }

    public static void eraseCache(Player player) {
        DMain.getInstance().getSetupStepsCaching().getCacheMap().remove(player.getUniqueId());
        DMain.getInstance().getPositionCountCaching().getCacheMap().remove(player.getUniqueId());
        DMain.getInstance().getArenaCaching().getCacheMap().remove(player.getUniqueId());
        if(DMain.getInstance().getChatModes().findUser(player))
            DMain.getInstance().getChatModes().removeUser(player);
    }
}
