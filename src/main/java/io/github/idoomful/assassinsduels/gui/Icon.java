package io.github.idoomful.assassinsduels.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Icon {
    private final ItemStack item;
    private BiConsumer<ItemStack, Player> action;

    public Icon(ItemStack item) {
        this.item = item;
    }

    public Icon(ItemStack item, BiConsumer<ItemStack, Player> action) {
        this.item = item;
        this.action = action;
    }

    public void setAction(BiConsumer<ItemStack, Player> action) {
        this.action = action;
    }

    public ItemStack getItem() {
        return item;
    }

    public void execute(Player player) {
        action.accept(item, player);
    }

    public static int getSlot(HashMap<Integer, Icon> icons, ItemStack icon) {
        for(Map.Entry<Integer, Icon> entry : icons.entrySet()) {
            if(entry.getValue().getItem().equals(icon)) return entry.getKey();
        }
        return -1;
    }
}
