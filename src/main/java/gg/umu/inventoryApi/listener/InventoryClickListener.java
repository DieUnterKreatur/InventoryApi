package gg.umu.inventoryApi.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import gg.umu.inventoryApi.BasePage;
import gg.umu.inventoryApi.Gui;

public class InventoryClickListener implements Listener{

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        BasePage page = Gui.getInstance().getPlayerPage(player);
        if (page == null) {
            return;
        }

        if (page.isRemovable()) {
            page.clickHandler(event.getSlot(), player, event.getAction());
            return;
        }
        event.setCancelled(true);
        page.clickHandler(event.getSlot(), player, event.getAction());
    }
}
 