package io.github.idoomful.assassinsduels.configuration;

import io.github.idoomful.assassinscurrencycore.utils.Sounds;
import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.gui.ItemBuilder;
import io.github.idoomful.assassinsduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public enum SettingsYML {
    _OPTIONS("");

    String path;
    FileConfiguration settings;

    SettingsYML(String output) {
        settings = DMain.getInstance().getConfigs().getFile("settings");
        this.path = output;
    }

    public void reload() {
        DMain.getInstance().getConfigs().reloadConfigs();
        settings = DMain.getInstance().getConfigs().getFile("settings");
    }

    public enum Teams {
        INVITATION_EXPIRY("invitation-expiry");

        public String path;
        public FileConfiguration settings;

        Teams(String path) {
            this.path = "teams." + path;
            this.settings = SettingsYML._OPTIONS.settings;
        }

        public int getInt() {
            return settings.getInt(path);
        }
    }

    public enum Duels {
        TELEPORT_ON_KICK("teleport-on-kick.location"),
        TELEPORT_ON_KICK_ON("teleport-on-kick.enabled"),
        COMMANDS_ON_KICK("commands-on-kick.commands"),
        COMMANDS_ON_KICK_ON("commands-on-kick.enabled"),
        TIME_BEFORE_MATCH("time-before-match"),
        COUNTDOWN_START_FROM("countdown-start-from"),
        TIME_BEFORE_END("time-before-end"),
        WHITELIST_COMMANDS("whitelist-commands");

        public String path;
        public FileConfiguration settings;

        Duels(String path) {
            this.path = "duels." + path;
            this.settings = SettingsYML._OPTIONS.settings;
        }

        public int getInt() {
            return settings.getInt(path);
        }
        public boolean getBoolean() {
            return settings.getBoolean(path);
        }
        public List<String> getStringList(Player player) {
            return Utils.placeholder(player, settings.getStringList(path));
        }

        public Location getLocation() {
            World world = Bukkit.getWorld(settings.getString(path).split(" ")[0]);
            double x = Double.parseDouble(settings.getString(path).split(" ")[1]);
            double y = Double.parseDouble(settings.getString(path).split(" ")[2]);
            double z = Double.parseDouble(settings.getString(path).split(" ")[3]);

            return new Location(world, x, y, z);
        }

        public enum BettingGUI {
            TITLE("title"),
            ROWS("rows"),
            ITEMS("items"),
            LAYOUT("layout");

            public String path;
            public FileConfiguration settings;

            BettingGUI(String path) {
                this.path = "duels.betting-gui." + path;
                this.settings = SettingsYML._OPTIONS.settings;
            }

            public String getString(Player player) {
                return Utils.placeholder(player, settings.getString(path));
            }
            public int getInt() {
                return settings.getInt(path);
            }
            public List<String> getStringList(Player player) {
                return Utils.placeholder(player, settings.getStringList(path));
            }
            public List<String> getSymbols() {
                return new ArrayList<>(settings.getConfigurationSection(path).getKeys(false));
            }
            public List<String> getItemList() {
                List<String> output = new ArrayList<>();
                getSymbols().forEach(s -> output.add(s + " " + settings.getString(path + "." + s)));
                return output;
            }
            public ItemStack getItem(String symbol) {
                return ItemBuilder.build(settings.getString(path + "." + symbol));
            }
        }
    }

    public enum SFX {
        DUEL_END_SOUND("duel-end-sound"),
        COUNTDOWN_SOUND("countdown-sound"),
        MATCH_START_SOUND("match-start-sound"),
        DUEL_INVITE_RECEIVE_SOUND("duel-invite-receive-sound"),
        TEAM_INVITE_RECEIVE_SOUND("team-invite-receive-sound"),
        INVITE_DECLINE_SOUND("invite-decline-sound"),
        INVITE_ACCEPT_SOUND("invite-accept-sound"),
        ELIMINATION_SOUND("elimination-sound");

        public String path;
        public FileConfiguration settings;

        SFX(String path) {
            this.path = "sound-effects." + path;
            this.settings = SettingsYML._OPTIONS.settings;
        }

        public void playSound(Player player) {
            String[] args = settings.getString(path).split(" ");

            Sound sound = Sounds.valueOf(args[0]).getSound();
            float volume = Float.parseFloat(args[1]);
            float pitch = Float.parseFloat(args[2]);
            int repeat = 1, delay = 0;

            if(args.length >= 4) repeat = Integer.parseInt(args[3]);
            if(args.length >= 5) delay = Integer.parseInt(args[4]);

            AtomicInteger counter = new AtomicInteger(repeat);
            AtomicInteger ID = new AtomicInteger();

            ID.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(DMain.getInstance(), () -> {
                if(counter.get() > 0) {
                    player.playSound(player.getLocation(), sound, volume, pitch);
                    counter.decrementAndGet();
                } else Bukkit.getScheduler().cancelTask(ID.get());
            }, 0, delay));
        }
    }

    /*public enum Section {
        SECTION("section");

        public String path;
        public FileConfiguration settings;

        Section(String path) {
            this.path = "section." + path;
            this.settings = SettingsYML._OPTIONS.settings;
        }

        public String getString(Player player) {
            return Utils.placeholder(player, settings.getString(path));
        }
        public List<String> getStringList(Player player) {
            return Utils.placeholder(player, settings.getStringList(path));
        }
        public boolean getBoolean() {
            return settings.getBoolean(path);
        }
        public int getInt() {
            return settings.getInt(path);
        }
        public float getFloat() {
            return (float) settings.getDouble(path);
        }
        public ItemStack getItem() {
            return ItemBuilder.build(settings.getString(path));
        }
    }*/
}
