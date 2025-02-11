package gg.umu.inventoryApi;

import org.bukkit.plugin.java.JavaPlugin;

import gg.umu.inventoryApi.listener.InventoryCloseListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App extends JavaPlugin {
    @Override
    public void onEnable() {
        log.info(getName() + "is started");
        registerListeners();
    }
 
    public void registerListeners() {
        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new InventoryCloseListener(), this);
    }
}
