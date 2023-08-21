package io.github.idoomful.assassinsduels.gui;

import io.github.idoomful.assassinscurrencycore.gui.InventoryBuilder;
import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.configuration.SettingsYML;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BettingGUI implements MyGUI, InventoryHolder {
    Inventory inventory;

    public BettingGUI(Player player) {
        inventory = Bukkit.createInventory(this,
                SettingsYML.Duels.BettingGUI.ROWS.getInt() * 9,
                SettingsYML.Duels.BettingGUI.TITLE.getString(player)
        );

        InventoryBuilder builder = new InventoryBuilder(inventory);
        builder.setConfigItemList(SettingsYML.Duels.BettingGUI.ITEMS.getItemList(), player);
        builder.setConfigItemArrangement(SettingsYML.Duels.BettingGUI.LAYOUT.getStringList(player));

        openInventory(player);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void openInventory(Player player) {
        player.openInventory(inventory);
    }
}
