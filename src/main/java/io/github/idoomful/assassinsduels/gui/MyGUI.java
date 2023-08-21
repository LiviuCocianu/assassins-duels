package io.github.idoomful.assassinsduels.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface MyGUI {
    Inventory getInventory();
    void openInventory(Player player);
}
