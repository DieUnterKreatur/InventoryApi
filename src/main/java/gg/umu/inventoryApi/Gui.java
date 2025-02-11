package gg.umu.inventoryApi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Gui {
    private static Gui gui;
    private Gui() {

    }
    
    public static Gui getInstance() {
        if(gui == null) {
            gui = new Gui();
        }
        return gui;
    }

    private HashMap<UUID, BasePage> pages = new HashMap<>();
    @Getter
    private HashMap<UUID, List<BasePage>> histories = new HashMap<>();

    public void openPage(Player player, BasePage page) {
        if (player == null || page == null || page.getInventory() == null) {
            log.warn("Player:" + player + " or Page:" + page + " are null or inventory:"+ page.getInventory());
            return; 
        }
        if (histories.containsKey(player.getUniqueId())) {
            player.openInventory(page.getInventory());
            pages.put(player.getUniqueId(), page);
            histories.get(player.getUniqueId()).add(page);
            return; 
        }
        player.openInventory(page.getInventory());
        pages.put(player.getUniqueId(), page);
        List<BasePage> history = new LinkedList<>();
        history.add(page);
        histories.put(player.getUniqueId(), history);
    }

    public void removePage(Player player) {
        if(pages.containsKey(player.getUniqueId())) {
            pages.remove(player.getUniqueId());
        }
    }
    
    public BasePage getPlayerPage(Player player) {
        return pages.get(player.getUniqueId());
    }
    
    public void refreshPage(Player player, BasePage page) {
        player.openInventory(page.getInventory());
    }

    public void clearHistory(Player player) {
        histories.remove(player.getUniqueId());
    }
}
