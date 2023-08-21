package io.github.idoomful.assassinsduels.setup;

import io.github.idoomful.assassinsduels.DMain;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ChatMode extends Mode {
    public enum Context {
        NONE, ARENA_CREATE
    }

    private final HashMap<UUID, Context> contexts = new HashMap<>();

    public ChatMode(DMain main) {
        super(main);
    }

    public void addUser(Player user, Context context) {
        contexts.put(user.getUniqueId(), context);
        super.addUser(user);
    }

    public void removeUser(Player user) {
        contexts.remove(user.getUniqueId());
        super.removeUser(user);
    }

    public Context getContext(Player user) {
        if(!findUser(user)) return Context.NONE;
        return contexts.get(user.getUniqueId());
    }
}
