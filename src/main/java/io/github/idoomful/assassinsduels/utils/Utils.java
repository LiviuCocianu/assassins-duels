package io.github.idoomful.assassinsduels.utils;

import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.configuration.MessagesYML;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static List<String> color(List<String> input) {
        return input.stream().map(Utils::color).collect(Collectors.toList());
    }

    public static String placeholder(Player player, String input) {
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && player != null)
            return PlaceholderAPI.setPlaceholders(player, input);
        else return color(input);
    }

    public static List<String> placeholder(Player player, List<String> input) {
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && player != null)
            return PlaceholderAPI.setPlaceholders(player, input);
        else return color(input);
    }

    public static String listStrings(ArrayList<String> elements) {
        StringBuilder list = new StringBuilder();

        if(elements.size() == 1) {
            return MessagesYML.Syntax.ELEMENT_PREFIX.color(null) + elements.get(0);
        } else if(elements.isEmpty()) {
            return "";
        } else {
            for(int i = 0; i < elements.size(); i++) {
                list.append(MessagesYML.Syntax.ELEMENT_PREFIX.color(null))
                        .append(elements.get(i));

                if(i < elements.size() - 1) {
                    list.append(MessagesYML.Syntax.SEPARATOR.color(null));
                }
            }

            return list.toString();
        }
    }

    public static void executeCommands(Player player, List<String> commands, HashMap<String, String> placeholders) {
        for(String cmd : commands) {
            String processedCmd = cmd;

            for(String placeholder : placeholders.keySet())
                processedCmd = processedCmd.replace(placeholder, placeholders.get(placeholder));

            if(processedCmd.startsWith("[message]")) {
                String message = processedCmd.replace("[message]", "");
                message = message.trim();

                if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                    player.sendMessage(PlaceholderAPI.setPlaceholders(player, message));
                else player.sendMessage(Utils.color(message));
            } else if(processedCmd.matches("^\\[delay=(\\d+)].+")) {
                Matcher matcher = Pattern.compile("^\\[delay=(\\d+)]").matcher(processedCmd);
                while(matcher.find()) {
                    int delay = Integer.parseInt(matcher.group(1));

                    String finalProcessedCmd = processedCmd;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DMain.getInstance(), () -> {
                        String command = finalProcessedCmd.replaceAll("^\\[delay=(\\d+)]", "");
                        command = command.trim();

                        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, command));
                        else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }, delay);
                }
            } else {
                if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, processedCmd));
                else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
            }
        }
    }

    public static class Array {
        public static <A> A[] of(A ... el) {
            return el;
        }
    }

    public static String formatNumber(int num) {
        NumberFormat output = NumberFormat.getInstance();
        output.setGroupingUsed(true);
        return output.format(num).replace(",", ".");
    }

    public static String getTimestamp(double seconds, boolean essFormat) {
        String output = "";
        double secs, minutes = 0, hours = 0, days = 0;

        if(seconds > 60) {
            minutes = Math.floor(seconds / 60);
            secs = seconds % 60;
        } else secs = seconds;

        if(minutes % 60 == 0.0 && Math.floor(minutes / 60) == 24.0) {
            minutes = 59;
        } else if(minutes > 60) {
            hours = Math.floor(minutes / 60);
            minutes = minutes % 60;
        }

        if(hours > 24) {
            days = Math.floor(hours / 24);
            hours = Math.floor(hours / 24) == 0 ? 23 : hours % 24;
        } else if(hours == 24.0 && seconds <= 86400) {
            hours = 23;
        }

        if(days > 0) {
            output = output + Math.round(days) + "d";
            if(essFormat) output = output + "_";
            else output = output + " ";
        }

        if(hours > 0) {
            output = output + Math.round(hours) + "h";
            if(essFormat) output = output + "_";
            else output = output + " ";
        }

        if(minutes > 0) {
            output = output + Math.round(minutes) + "m";
            if(essFormat) output = output + "_";
            else output = output + " ";
        }

        if(secs > 0) {
            output = output + Math.round(secs) + "s";
        }

        return output;
    }

    public static boolean isWeakSimilar(ItemStack item1, ItemStack item2) {
        if(item1.getType().equals(item2.getType())) {
            return item2.getItemMeta().getDisplayName().equalsIgnoreCase(item2.getItemMeta().getDisplayName());
        }
        return false;
    }

    static Class<?> getNMSclass(String name) throws ClassNotFoundException {
        final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + name);
    }

    static Class<?> getCraftClass(String name) throws ClassNotFoundException {
        final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
    }

    public static FileConfiguration getConfigOf(String plugin, String configName) {
        File file = new File(DMain.getInstance().getDataFolder().getPath()
                .replace(DMain.getInstance().getDescription().getName(), plugin + "/" + configName + ".yml"));
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void sendActionText(Player player, String message){
        try {
            Class<?> playOutChat = getNMSclass("PacketPlayOutChat");
            Class<?> ichat = getNMSclass("IChatBaseComponent");
            Class<?> chatComp = getNMSclass("ChatMessage");

            Object baseComp = chatComp.getConstructor(String.class, Object[].class).newInstance(message, new Object[] {});
            Object packet = playOutChat.getConstructor(ichat, byte.class).newInstance(baseComp, (byte) 2);

            Class<?> craftPlayer = getCraftClass("entity.CraftPlayer");
            Class<?> packetClass = getNMSclass("Packet");
            Object handle = craftPlayer.cast(player).getClass().getMethod("getHandle").invoke(craftPlayer.cast(player));
            Object conObj = handle.getClass().getField("playerConnection").get(handle);
            conObj.getClass().getMethod("sendPacket", packetClass).invoke(conObj, packet);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static String integerToRoman(int input) {
        if (input < 1 || input > 3999) return "Invalid";
        StringBuilder s = new StringBuilder();

        while (input >= 1000) {
            s.append("M");
            input -= 1000;
        }
        while (input >= 900) {
            s.append("CM");
            input -= 900;
        }
        while (input >= 500) {
            s.append("D");
            input -= 500;
        }
        while (input >= 400) {
            s.append("CD");
            input -= 400;
        }
        while (input >= 100) {
            s.append("C");
            input -= 100;
        }
        while (input >= 90) {
            s.append("XC");
            input -= 90;
        }
        while (input >= 50) {
            s.append("L");
            input -= 50;
        }
        while (input >= 40) {
            s.append("XL");
            input -= 40;
        }
        while (input >= 10) {
            s.append("X");
            input -= 10;
        }
        while (input >= 9) {
            s.append("IX");
            input -= 9;
        }
        while (input >= 5) {
            s.append("V");
            input -= 5;
        }
        while (input >= 4) {
            s.append("IV");
            input -= 4;
        }
        while (input >= 1) {
            s.append("I");
            input -= 1;
        }
        return s.toString();
    }

    public static long randomRange(String range) {
        try {
            return Long.parseLong(range);
        } catch(NumberFormatException e) {
            int first = Integer.parseInt(range.split("-")[0]);
            int second = Integer.parseInt(range.split("-")[1]);

            return new Random().nextInt((second - first) + 1) + first;
        }
    }

    public static void playSoundRadius(Location loc, Sound sound, float volume, float pitch, float radius) {
        loc.getWorld().getNearbyEntities(loc, radius, radius, radius).stream()
                .filter(en -> en instanceof Player)
                .forEach(pl -> {
                    ((Player) pl).playSound(pl.getLocation(), sound, volume, pitch);
                });
    }

    public static void knockEntityAway(LivingEntity affected, Location source, double speed, double y) {
        Vector v = affected.getLocation().toVector().subtract(source.toVector()).normalize().setY(y);
        affected.setVelocity(v.multiply(speed));
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static boolean usesVersionBetween(String start, String end) {
        String serverVersion = Bukkit.getServer().getVersion();
        Matcher matcher = Pattern.compile("\\d\\.\\d+\\.?\\d?").matcher(serverVersion);

        if(matcher.find()) serverVersion = matcher.group();

        int[][] patchVersions = new int[16 + 1][];

        patchVersions[4] = new int[] {6, 7};
        patchVersions[5] = new int[] {1, 2};
        patchVersions[6] = new int[] {2, 4};
        patchVersions[7] = new int[] {2, 5, 8, 9, 10};
        patchVersions[8] = new int[] {0, 3, 4, 5, 6, 7, 8};
        patchVersions[9] = new int[] {0, 2, 4};
        patchVersions[10] = new int[] {0, 2};
        patchVersions[11] = new int[] {0, 1, 2};
        patchVersions[12] = new int[] {0, 1, 2};
        patchVersions[13] = new int[] {0, 1, 2};
        patchVersions[14] = new int[] {0, 1, 2, 3, 4};
        patchVersions[15] = new int[] {0, 1, 2};
        patchVersions[16] = new int[] {0, 2};

        for(int browsedMinorVer = 4; browsedMinorVer < patchVersions.length; browsedMinorVer++) {
            for(int j = 0; j < patchVersions[browsedMinorVer].length; j++) {
                int browsedPatchVer = patchVersions[browsedMinorVer][j];

                int minorVerV1 = Integer.parseInt(start.split("\\.")[1]);
                int minorVerV2 = Integer.parseInt(end.split("\\.")[1]);

                final String[] startSplit = start.split("\\.");
                final String[] endSplit = end.split("\\.");

                int patchVerStart = Integer.parseInt(startSplit.length == 2 || startSplit[2].equals("x") ? "0" : startSplit[2]);
                int patchVerEnd = Integer.parseInt(endSplit.length == 2 || endSplit[2].equals("x") ? "0" : endSplit[2]);

                int lastPatch = patchVersions[browsedMinorVer][patchVersions[browsedMinorVer].length - 1];

                boolean limitsOtherVersionsPatches = (browsedMinorVer == minorVerV1) && !(patchVerStart <= browsedPatchVer && patchVerEnd <= lastPatch);

                if(browsedPatchVer != 0) {
                    if((browsedMinorVer >= minorVerV1) && (browsedMinorVer <= minorVerV2)) {
                        if(limitsOtherVersionsPatches) continue;
                        String match = "1." + browsedMinorVer + "." + browsedPatchVer;
                        if(serverVersion.equals(match)) return true;
                    }
                } else {
                    if(limitsOtherVersionsPatches) continue;

                    if(browsedMinorVer >= minorVerV1 && browsedMinorVer <= minorVerV2) {
                        String match = "1." + browsedMinorVer;
                        if(serverVersion.equals(match)) return true;
                    }
                }
            }
        }

        return false;
    }
}
