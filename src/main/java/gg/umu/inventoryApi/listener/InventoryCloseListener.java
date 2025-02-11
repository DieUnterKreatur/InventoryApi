package gg.umu.inventoryApi.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import gg.umu.inventoryApi.Gui;
public class InventoryCloseListener implements Listener{
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Gui.getInstance().removePage(player);
    }
}
