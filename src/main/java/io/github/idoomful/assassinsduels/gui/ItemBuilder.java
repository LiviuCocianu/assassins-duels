package io.github.idoomful.assassinsduels.gui;

import dev.dbassett.skullcreator.SkullCreator;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import io.github.idoomful.assassinsduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
    @SuppressWarnings("deprecation")
    public static ItemStack build(String value) {
        ItemStack result = new ItemStack(Material.STONE, 1);
        String ID = "";

        String[] data = value.split(" ");

        final String[] enchants = { "protection", "fire_protection", "feather_falling", "blast_protection",
                "projectile_protection", "respiration", "aqua_affinity", "thorns", "depth_strider", "sharpness",
                "smite", "bane_of_arthropods", "knockback", "fire_aspect", "looting", "efficiency", "silk_touch",
                "unbreaking", "fortune", "power", "punch", "flame", "infinity", "luck_of_the_sea", "lure", "mending",
                "vanishing_curse", "binding_curse", "frost_walker", "sweeping" };

        final ArrayList<Enchantment> enchantIDs = new ArrayList<>(Arrays.asList(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE,
                Enchantment.PROTECTION_FALL, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE,
                Enchantment.OXYGEN, Enchantment.WATER_WORKER, Enchantment.THORNS, Enchantment.DEPTH_STRIDER,
                Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD, Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK,
                Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS, Enchantment.DIG_SPEED, Enchantment.SILK_TOUCH,
                Enchantment.DURABILITY, Enchantment.LOOT_BONUS_BLOCKS, Enchantment.ARROW_DAMAGE,
                Enchantment.ARROW_KNOCKBACK, Enchantment.ARROW_FIRE, Enchantment.ARROW_INFINITE, Enchantment.LUCK,
                Enchantment.LURE));

        if (!Utils.usesVersionBetween("1.4.x", "1.8.x")) {
            enchantIDs.addAll(Arrays.asList(
                    Enchantment.MENDING, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE,
                    Enchantment.FROST_WALKER, Enchantment.SWEEPING_EDGE
            ));
        }

        for (String aData : data) {
            // TODO Check for ID
            if (aData.startsWith("id:")) {
                String id = aData.substring(aData.indexOf(":") + 1);

                if (id.contains(":")) {
                    String[] separate = id.split(":");

                    id = separate[0];
                    int damage = Integer.parseInt(separate[1]);

                    result.setType(IdentifierToMaterial.getMaterialByID(id));

                    try {
                        if (damage > 9999) {
                            result.setDurability((short) 1);
                            continue;
                        }
                        result.setDurability((short) damage);

                    } catch (NumberFormatException e) {
                        result.setDurability((short) 1);
                    }
                } else {
                    ID = id;

                    if(ID.equalsIgnoreCase("splash_potion")) {
                        result.setType(IdentifierToMaterial.getMaterialByID("potion"));
                    } else {
                        result.setType(IdentifierToMaterial.getMaterialByID(id));
                    }
                    continue;
                }
            }
            // TODO Check for amount
            if (aData.startsWith("amount:")) {
                String amount = aData.split(":")[1];
                try {
                    int AMOUNT = Integer.parseInt(amount);
                    if (AMOUNT > 64) {
                        result.setAmount(64);
                    }
                    result.setAmount(AMOUNT);
                } catch (NumberFormatException e) {
                    result.setAmount(1);
                }
            }

            // TODO Check for "player"
            if (aData.startsWith("player:") && (Utils.usesVersionBetween("1.4.x", "1.12.x")
                    ? result.getType().toString().equals("SKULL_ITEM")
                    : result.getType().toString().equals("PLAYER_HEAD")
            ) && (!Utils.usesVersionBetween("1.4.x", "1.12.x") || result.getDurability() == 3)
            ) {
                String playername = aData.split(":")[1];

                SkullMeta sm = (SkullMeta) result.getItemMeta();
                assert sm != null;

                if(Utils.usesVersionBetween("1.4.x", "1.11.x")) sm.setOwner(playername);
                else sm.setOwningPlayer(Bukkit.getOfflinePlayer(playername));

                result.setItemMeta(sm);
            }

            // TODO Check for "pattern"
            if (aData.startsWith("pattern:") && result.getType() == Material.BANNER) {
                String pattern = aData.split(":")[1];
                String type = pattern.split(",")[0];
                String color = pattern.split(",")[1];

                BannerMeta bm = (BannerMeta) result.getItemMeta();
                assert bm != null;

                bm.addPattern(new Pattern(DyeColor.valueOf(color.toUpperCase()), PatternType.valueOf(type.toUpperCase())));

                result.setItemMeta(bm);
            }

            // TODO Check for "urlCode"
            if(aData.startsWith("urlCode:") && (Utils.usesVersionBetween("1.4.x", "1.12.x")
                    ? result.getType().toString().equals("SKULL_ITEM")
                    : result.getType().toString().equals("PLAYER_HEAD")
            ) && (!Utils.usesVersionBetween("1.4.x", "1.12.x") || result.getDurability() == 3)
            ) {
                String code = aData.split(":")[1];

                if(code.startsWith("ey")) result = SkullCreator.itemWithBase64(result, code);
                else result = SkullCreator.itemWithUrl(result, code);
            }

            // TODO Check for "color"
            if (aData.startsWith("color:") && (result.getType().equals(Material.LEATHER_HELMET)
                    || result.getType().equals(Material.LEATHER_CHESTPLATE)
                    || result.getType().equals(Material.LEATHER_LEGGINGS)
                    || result.getType().equals(Material.LEATHER_BOOTS))) {
                String[] colors = aData.split(":")[1].split(",");
                LeatherArmorMeta lam = (LeatherArmorMeta) result.getItemMeta();
                assert lam != null;

                try {
                    int red = Integer.parseInt(colors[0]);
                    int green = Integer.parseInt(colors[1]);
                    int blue = Integer.parseInt(colors[2]);
                    try {
                        lam.setColor(Color.fromRGB(red, green, blue));
                        result.setItemMeta(lam);
                    } catch (IllegalArgumentException e) {
                        lam.setColor(Color.fromRGB(0, 0, 0));
                        result.setItemMeta(lam);
                    }

                } catch (NumberFormatException e) {
                    lam.setColor(Color.fromRGB(0, 0, 0));
                    result.setItemMeta(lam);
                }
            }

            // TODO Check for name
            if (aData.startsWith("name:")) {
                ItemMeta im = result.getItemMeta();
                assert im != null;

                String name = aData.substring(aData.indexOf(":") + 1);
                name = Utils.color(name.replace("_", " "));

                im.setDisplayName(name);
                result.setItemMeta(im);
            }

            // TODO Check for lore
            if (aData.startsWith("lore:")) {
                List<String> lore = new ArrayList<>();
                ItemMeta im = result.getItemMeta();
                assert im != null;

                if(aData.contains("|")) {
                    String[] lines = aData.substring(aData.indexOf(":") + 1).split("\\|");

                    for (String line : lines) {
                        String action = line.replace("_", " ");
                        lore.add(Utils.color(action));
                    }
                } else {
                    final String line = aData.substring(aData.indexOf(":") + 1);
                    lore.add(Utils.color(line.replace("_", " ")));
                }

                im.setLore(lore);
                result.setItemMeta(im);
            }

            // TODO Check for enchantments
            for (int n = 0; n < enchants.length; n++) {
                ItemMeta im = result.getItemMeta();
                assert im != null;

                if (aData.startsWith(enchants[n] + ":")) {
                    String level = aData.substring(aData.indexOf(":") + 1);

                    try {
                        int LEVEL = Integer.parseInt(level);
                        im.addEnchant(enchantIDs.get(n), LEVEL, true);
                        result.setItemMeta(im);
                    } catch (NumberFormatException e) {
                        im.addEnchant(Enchantment.DURABILITY, 1, true);
                        result.setItemMeta(im);
                    }
                }
            }

            // TODO Check for "hideFlags"
            if (aData.equalsIgnoreCase("hideFlags")) {
                ItemMeta im = result.getItemMeta();
                im.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS,
                        ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE);
                result.setItemMeta(im);
            }

            // TODO Check for "unbreakable"
            if (aData.equalsIgnoreCase("unbreakable")) {
                ItemMeta im = result.getItemMeta();

                if (!Utils.usesVersionBetween("1.4.x", "1.8.x"))
                    im.setUnbreakable(true);

                result.setItemMeta(im);

                if (Utils.usesVersionBetween("1.4.x", "1.8.x"))
                    result = NBTEditor.set(result, (byte) 1, "Unbreakable");
            }

            // TODO Check for potion effects
            if (aData.startsWith("effect:") && result.getType().equals(Material.POTION)) {
                String[] v = aData.split("/");
                String effect = v[0].split(":")[1];
                String effectPower = v[1];
                String effectDuration = v[2];

                short power;
                int duration;

                try {
                    short POWER = Short.parseShort(effectPower);
                    if (POWER > 256) power = 256;
                    else power = POWER;
                } catch (NumberFormatException e) {
                    power = 1;
                }

                try {
                    int DURATION = Integer.parseInt(effectDuration);
                    duration = Math.min(DURATION, 999999);
                } catch (NumberFormatException e) {
                    duration = 120;
                }

                PotionMeta im = (PotionMeta) result.getItemMeta();

                if (!value.contains("name:")) im.setDisplayName(Utils.color("&dCustom potion"));

                if (effect.contains(",")) {
                    String[] effectList = effect.split(",");
                    for (String effect2 : effectList) addEffect(effect2, im, power, duration);
                } else {
                    addEffect(effect, im, power, duration);

                    if(ID.equalsIgnoreCase("splash_potion")) {
                        PotionEffect first = im.getCustomEffects().stream().findAny().get();
                        String effStr = first.getType().getName();

                        String eff = effStr.equals("HARM")
                                ? "INSTANT_DAMAGE"
                                : effStr.equals("HEAL") ? "INSTANT_HEAL" : effStr;

                        Potion pot = new Potion(PotionType.valueOf(eff), power);
                        pot.setSplash(true);

                        if(!eff.contains("INSTANT_DAMAGE") && !eff.contains("HEAL"))
                            pot.setHasExtendedDuration(duration == 1);

                        pot.apply(result);
                        continue;
                    }
                }

                result.setItemMeta(im);
            }
        }
        return result;
    }

    private static void addEffect(String effect, PotionMeta im, short power, int duration) {
        if (!Utils.usesVersionBetween("1.4.x", "1.8.x")) {
            if (effect.equalsIgnoreCase("levitation")) {
                im.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, power), false);
                return;
            }

            if (effect.equalsIgnoreCase("glowing")) {
                im.addCustomEffect(new PotionEffect(PotionEffectType.GLOWING, duration, power), false);
                return;
            }
        }

        switch (effect) {
            case "speed": im.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, duration, power), false); return;
            case "slowness": im.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, duration, power), false); return;
            case "haste": im.addCustomEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration, power), false); return;
            case "mining_fatigue": im.addCustomEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, duration, power), false); return;
            case "strength": im.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, power), false); return;
            case "instant_health": im.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, duration, power), false); return;
            case "instant_damage": im.addCustomEffect(new PotionEffect(PotionEffectType.HARM, duration, power), false); return;
            case "jump_boost": im.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, duration, power), false); return;
            case "nausea": im.addCustomEffect(new PotionEffect(PotionEffectType.CONFUSION, duration, power), false); return;
            case "regeneration": im.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, power), false); return;
            case "resistance": im.addCustomEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, power), false); return;
            case "fire_resistance": im.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, power), false); return;
            case "water_breathing": im.addCustomEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, duration, power), false); return;
            case "invisibility": im.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, power), false); return;
            case "blindness": im.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, power), false); return;
            case "night_vision": im.addCustomEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration, power), false); return;
            case "hunger": im.addCustomEffect(new PotionEffect(PotionEffectType.HUNGER, duration, power), false); return;
            case "weakness": im.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, power), false); return;
            case "poison": im.addCustomEffect(new PotionEffect(PotionEffectType.POISON, duration, power), false); return;
            case "wither": im.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, duration, power), false); return;
            case "health_boost": im.addCustomEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, duration, power), false); return;
            case "absorption": im.addCustomEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, power), false); return;
            case "saturation": im.addCustomEffect(new PotionEffect(PotionEffectType.SATURATION, duration, power), false);
        }
    }
}
