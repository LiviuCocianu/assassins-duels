package io.github.idoomful.assassinsduels.setup;

import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.utils.Events;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.UUID;

public class Mode {
    private final ArrayList<UUID> users = new ArrayList<>();

    public Mode(DMain main) {
        Events.listen(main, PlayerQuitEvent.class, e -> {
            users.remove(e.getPlayer().getUniqueId());
        });
    }

    public ArrayList<UUID> getUsers() {
        return users;
    }

    public boolean findUser(Player user) {
        return users.contains(user.getUniqueId());
    }

    public void addUser(Player user) {
        users.add(user.getUniqueId());
    }

    public void removeUser(Player user) {
        users.remove(user.getUniqueId());
    }
}
