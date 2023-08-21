package io.github.idoomful.assassinsduels.data.SQL;

import com.google.gson.Gson;
import io.github.idoomful.assassinscurrencycore.utils.ConfigPair;
import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.arena.ArenaDuelType;
import io.github.idoomful.assassinsduels.arena.ArenaPosition;
import io.github.idoomful.assassinsduels.arena.ArenaSettings;
import io.github.idoomful.assassinsduels.match.AdaptiveMatch;
import io.github.idoomful.assassinsduels.match.MatchManager;
import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import io.github.idoomful.assassinsduels.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Lite<T extends JavaPlugin> {
    enum DataType {
        STRING, INTEGER, DOUBLE, FLOAT
    }

    private Connection con = null;
    private final T plugin;

    // TODO set these up
    private final String DATABASE = "data";
    private final String ARENAS = "arenas";
    private final String PLAYERS = "players";

    public Lite(T plugin) {
        this.plugin = plugin;

        setup();
        getConnection();
    }

    public boolean isConnectionActive() {
        try {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Connection getConnection() {
        try {
            if(isConnectionActive()) return con;
            else con = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + File.separator + DATABASE + ".db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    private void setup() {
        try(
                Connection connection = getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.execute("CREATE TABLE IF NOT EXISTS `" + ARENAS + "`(" +
                    "`name` varchar(16)," +
                    "`type` varchar(16)," +
                    "`max` unsigned tinyint(200)," +
                    "`priority` smallint(10000)," +
                    "`spectatorsPos` json," +
                    "`positions` json" +
                    ");");

            statement.execute("CREATE TABLE IF NOT EXISTS `" + PLAYERS + "`(" +
                    "`name` varchar(16)," +
                    "`bets` json," +
                    "`wins` unsigned mediumint(1000000)," +
                    "`loses` unsigned mediumint(1000000)" +
                    ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void newEntry(String table, String values) {
        String columns = table.equals(PLAYERS)
                ? "(name,bets,wins,loses)"
                : table.equals(ARENAS)
                ? "(name,type,max,priority,spectatorsPos,positions)"
                : "";

        try(PreparedStatement ps = con.prepareStatement("INSERT INTO `" + table + "` " + columns + " VALUES (" + values + ")")) {
            ps.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeEntry(String table, String condition) {
        try(PreparedStatement ps = con.prepareStatement("DELETE FROM `" + table + "` WHERE " + condition)) {
            ps.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void exists(String table, String condition, Callback<Boolean> result) {
        try(PreparedStatement ps = con.prepareStatement("SELECT 1 FROM `" + table + "` WHERE " + condition)) {
            ResultSet rs = ps.executeQuery();
            result.done(rs.next());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void get(String table, String condition, String column, DataType type, Callback<Object> result) {
        try(PreparedStatement ps = con.prepareStatement("SELECT `" + column + "` FROM `" + table + "` WHERE " + condition)) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                switch(type) {
                    case STRING: result.done(rs.getString(1)); break;
                    case INTEGER: result.done(rs.getInt(1)); break;
                    case FLOAT: result.done(rs.getFloat(1)); break;
                    case DOUBLE: result.done(rs.getDouble(1)); break;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void set(String table, HashMap<String, String> values, String condition) {
        StringBuilder vals = new StringBuilder();

        int i = 0;
        for(Map.Entry<String, String> pair : values.entrySet()) {
            vals.append(pair.getKey()).append("='").append(pair.getValue()).append("'");
            if(i < values.size() - 1) vals.append(",");
            i++;
        }

        try(PreparedStatement ps = con.prepareStatement("UPDATE `" + table + "` SET " + vals.toString() + " WHERE " + condition)) {
            ps.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void newArenaEntry(Arena input) {
        String values = "'" + input.getName() + "', '" +
                input.getSettings().getDuelType().name() + "', '" +
                input.getSettings().getMaxPlayers() + "', '" +
                input.getSettings().getPriority() + "', '" +
                input.getSettings().spectatorPositionToJSON() + "', '" +
                input.getSettings().positionsToJSON() + "'";

        newEntry(ARENAS, values);

        if(!MatchManager.getMatches().containsKey(1)) {
            MatchManager.getMatches().put(1, new ArrayList<>());
        }

        MatchManager.getMatches().get(1).add(new AdaptiveMatch(input));
    }

    public void newPlayerEntry(DuelsPlayer input) {
        String values = "'" + input.getName() + "', '" +
                input.betsToJSON() + "', '" +
                input.getWins() + "', '" +
                input.getLoses() + "'";

        newEntry(PLAYERS, values);

        DMain.getInstance().getPlayers().put(input.getUUID(), input);
    }

    public Arena getArena(String name) {
        AtomicReference<ArenaDuelType> type = new AtomicReference<>();
        AtomicInteger max = new AtomicInteger();
        AtomicInteger priority = new AtomicInteger();
        AtomicReference<String> spectatorPos = new AtomicReference<>();
        AtomicReference<String> positions = new AtomicReference<>();

        get(ARENAS, cond("name", name), "type", DataType.STRING, res -> type.set(ArenaDuelType.valueOf((String) res)));
        get(ARENAS, cond("name", name), "max", DataType.INTEGER, res -> max.set((Integer) res));
        get(ARENAS, cond("name", name), "priority", DataType.INTEGER, res -> priority.set((Integer) res));
        get(ARENAS, cond("name", name), "spectatorsPos", DataType.STRING, res -> spectatorPos.set((String) res));
        get(ARENAS, cond("name", name), "positions", DataType.STRING, res -> positions.set((String) res));

        return new Arena(name, new ArenaSettings(
                type.get(),
                max.get(),
                priority.get(),
                ArenaPosition.fromJSONList(positions.get()),
                ArenaPosition.fromJSON(spectatorPos.get())
        ));
    }

    public HashMap<String, Arena> getArenas() {
        HashMap<String, Arena> output = new HashMap<>();

        try(PreparedStatement ps = con.prepareStatement("SELECT * FROM `" + ARENAS + "`")) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) output.put(rs.getString(1), getArena(rs.getString(1)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return output;
    }

    public void setArena(String name, Arena arena) {
        HashMap<String, String> values = new HashMap<>();
        values.put("name", arena.getName());
        values.put("type", arena.getSettings().getDuelType().name());
        values.put("max", arena.getSettings().getMaxPlayers() + "");
        values.put("priority", arena.getSettings().getPriority() +  "");
        values.put("spectatorsPos", arena.getSettings().spectatorPositionToJSON());
        values.put("positions", arena.getSettings().positionsToJSON());

        set(ARENAS, values, cond("name", name));
    }

    public DuelsPlayer getPlayer(Player player) {
        AtomicReference<ArrayList<ConfigPair<Integer, String>>> bets = new AtomicReference<>();
        AtomicInteger wins = new AtomicInteger();
        AtomicInteger loses = new AtomicInteger();

        if(!playerExists(player.getName())) {
            newPlayerEntry(new DuelsPlayer(player, new ArrayList<>(), 0, 0));
            return new DuelsPlayer(player, new ArrayList<>(), 0, 0);
        }

        get(PLAYERS, cond("name", player.getName()), "bets", DataType.STRING, res -> bets.set(DuelsPlayer.betsFromJSONList((String) res)));
        get(PLAYERS, cond("name", player.getName()), "wins", DataType.INTEGER, res -> wins.set((Integer) res));
        get(PLAYERS, cond("name", player.getName()), "loses", DataType.INTEGER, res -> loses.set((Integer) res));

        return new DuelsPlayer(player, bets.get(), wins.get(), loses.get());
    }

    public void setPlayer(DuelsPlayer player) {
        HashMap<String, String> values = new HashMap<>();
        values.put("name", player.getName());
        values.put("bets", player.betsToJSON());
        values.put("wins", player.getWins() + "");
        values.put("loses", player.getLoses() + "");

        set(PLAYERS, values, cond("name", player.getName()));
    }

    public void setDuelType(String arena, ArenaDuelType type) {
        HashMap<String, String> values = new HashMap<>();
        values.put("type", type.name());
        values.put("max", (type.getMult() * 2) + "");

        set(ARENAS, values, cond("name", arena));
    }

    public void setMadeBets(String name, ArrayList<ConfigPair<Integer, String>> bets) {
        HashMap<String, String> values = new HashMap<>();
        ArrayList<String> output = new ArrayList<>();
        bets.forEach(bet -> output.add(new Gson().toJson(bet)));
        values.put("bets", new Gson().toJson(output));

        set(PLAYERS, values, cond("name", name));
    }

    public void deleteArena(String name) {
        removeEntry(ARENAS, cond("name", name));
    }

    public void deletePlayer(String name) {
        removeEntry(PLAYERS, cond("name", name));
    }

    public boolean arenaExists(String name) {
        AtomicBoolean result = new AtomicBoolean();
        exists(ARENAS, cond("name", name), result::set);
        return result.get();
    }

    public boolean playerExists(String name) {
        AtomicBoolean result = new AtomicBoolean();
        exists(PLAYERS, cond("name", name), result::set);
        return result.get();
    }

    private String cond(String key, String value) {
        return key + "='" + value + "'";
    }
}
