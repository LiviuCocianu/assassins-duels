package io.github.idoomful.assassinsduels.arena;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.idoomful.assassinsduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;

public class ArenaPosition {
    private final double x, y, z;
    private final float yaw, pitch;
    private final String world;

    public ArenaPosition(double x, double y, double z, String world) {
        this.x = x + 0.500;
        this.y = y;
        this.z = z + 0.500;
        this.world = world;
        this.yaw = 0;
        this.pitch = 0;
    }

    public ArenaPosition(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.world = location.getWorld().getName();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }
    public float getYaw() {
        return yaw;
    }
    public float getPitch() {
        return pitch;
    }
    public String getWorld() {
        return world;
    }

    public Location getLocation() {
        Location loc = new Location(Bukkit.getWorld(getWorld()), getX(), getY(), getZ());
        loc.setYaw(getYaw());
        loc.setPitch(getPitch());
        return loc;
    }

    @Override
    public String toString() {
        return "(" + getWorld() + ") "
                + Utils.round(getX(), 2) + ", "
                + Utils.round(getY(), 2) + ", "
                + Utils.round(getZ(), 2);
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }

    public static ArenaPosition fromJSON(String json) {
        return new Gson().fromJson(json, ArenaPosition.class);
    }

    public static ArrayList<ArenaPosition> fromJSONList(String json) {
        Gson gson = new Gson();
        ArrayList<ArenaPosition> output = new ArrayList<>();
        ArrayList<String> jsonList = gson.fromJson(json, new TypeToken<ArrayList<String>>(){}.getType());
        jsonList.forEach(pos -> output.add(gson.fromJson(pos, ArenaPosition.class)));

        return output;
    }
}
