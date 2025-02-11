package gg.umu.inventoryApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ObjIntConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import com.destroystokyo.paper.profile.PlayerProfile;

import gg.umu.inventoryApi.util.SuperPlayer;
import gg.umu.inventoryApi.ymls.GuiYML;
import gg.umu.inventoryApi.ymls.ItemSlotYML;
import gg.umu.inventoryApi.ymls.StatesYML;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BasePage implements InventoryHolder {
    private Inventory inventory;
    protected String name;
    private final GuiYML guiYML;
    private HashMap<Integer, String> slotStates = new HashMap<>();
    /**
     * @see
     * add all u Content and then use the Call method :o
    */
    protected HashMap<Integer, PlayerProfile> playerProfiles = new HashMap<>();
/*     protected HashMap<Integer,ItemDisplayModel> itemDisplays = new HashMap<>();  */
    @Setter
    @Getter
    protected SuperPlayer owner;
    protected int page = 0;
    protected int pageCount;
    protected BasePage(GuiYML guiYML, SuperPlayer owner) {
        this.guiYML = guiYML;
        this.owner = owner;
        if(guiYML.getName() != null) {
            name = guiYML.getName();
        }
    }

    protected void createInventory() {
        inventory = Bukkit.getServer().createInventory(this, guiYML.getPageSize().getValue() , name.toString());
        loadDefault();
    }

    public boolean isRemovable() {
        return guiYML.isRemovable();
    }
    private <T> List<T> getSubList(List<T> list, int listSize) {
        int startInd = listSize * page;
        int endInd = startInd + listSize < list.size() ? startInd + listSize : list.size();
        return list.subList(startInd, endInd);
    }
    //Used for for Lists Iterate trough them
    /**
     *
     * @param <T>
     * @param list
     * @param itemSlotYML
     * @param callable (T item, int index)
     * @return return 0 null if it dont has an Item Slo
     */
    protected <T> int listHandler(List<T> list, ItemSlotYML itemSlotYML, ObjIntConsumer<T> callable) {
        if (itemSlotYML.getSlots() == null) {
            return 0;
        }
        int index = itemSlotYML.getSlots()[0];
        int listSize = (itemSlotYML.getSlots()[1] - itemSlotYML.getSlots()[0]) + 1;
        list = getSubList(list, listSize);
        for(var item : list) {
            callable.accept(item, index);
            index++;
        }
        return listSize;
    }

    public Inventory getInventory() {
        if (inventory == null) {
            log.warn("inventory is null;");
        }
        return inventory;
    }

    protected String getState(int slot) {
        return slotStates.get(slot);
    }

    protected ItemSlotYML getItemSlot(String action) {
        Optional<ItemSlotYML> itemSlot = guiYML.getItemSlots().stream().filter(slot -> slot.getAction().equals(action)).findFirst();
        if (itemSlot.isPresent()) {
            return itemSlot.get();
        }

        log.warn("Id not Found " + action + "in The YML");
        return null;
    }

    protected ItemSlotYML getItemSlot(int slot) {
        Optional<ItemSlotYML> itemSlot = guiYML.getItemSlots().stream().filter(item -> isInSlot(item, slot)).findFirst();
        if (itemSlot.isPresent()) {
            return itemSlot.get();
        }
        return null;
    }

    protected void changeState(int slot, String state) {
        ItemSlotYML itemSlot = getItemSlot(slot);
        if(itemSlot.getStates().get(state) != null) {
            ItemStack itemStack = createItem(itemSlot, state);
            inventory.setItem(slot, itemStack);
            slotStates.replace(slot, state);
            return;
        }
        log.error("State doesn't  exist " + state);
    }
    // has to be rewriten
    protected void renderPlayerProfile() {
        for (Map.Entry<Integer, PlayerProfile> entry : playerProfiles.entrySet()) {
            ItemStack itemStack = inventory.getItem(entry.getKey());
            if (itemStack != null && itemStack.getType().equals(Material.SKULL_ITEM)) {
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                skullMeta.setPlayerProfile(entry.getValue());
                itemStack.setItemMeta(skullMeta);
                inventory.setItem(entry.getKey(), itemStack);
            }
        }
        playerProfiles.clear();
    }

/*     protected void renderItemDisplay() {
        for (Map.Entry<Integer, ItemDisplayModel> entry : itemDisplays.entrySet()) {
            ItemStack itemStack = inventory.getItem(entry.getKey());
            if (itemStack != null) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(entry.getValue().getName());
                if (entry.getValue().getLore() != null) {
                    itemMeta.lore(entry.getValue().getLore());
                }
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(entry.getKey(), itemStack);
            }
        }
        itemDisplays.clear();
    }
 */
    //Handels the SiteLogic. Refreshs the page / with opening a new inventory
    private void buttonHandler(String button, Player player) {
        if (button.equals("nextPage") && page < pageCount - 1) {
            page++;
        } else if (button.equals("lastPage") && page > 0) {
            page--;
        } else {
            return;
        }
        render();
        Gui.getInstance().refreshPage(player, this);
    }

    private void returnButton(Player player) {
        Gui gui = Gui.getInstance();
        List<BasePage> history = gui.getHistories().get(player);
        if (history.size() -2 > 0) {
            gui.openPage(player, history.get(history.size() -2));

        }
    }

    private ItemStack createItem(ItemSlotYML itemSlot,String state) {
        StatesYML statesYML = itemSlot.getStates().get(state);
        ItemStack itemStack = new ItemStack(statesYML.getMaterial());
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (statesYML.getAmount() != null) {
            itemStack.setAmount(statesYML.getAmount());
        }
        if (statesYML.getName() != null) {
            itemMeta.setDisplayName(statesYML.getName());
        } else {
            itemMeta.setDisplayName(" ");
        }
        if (statesYML.isEnchantment()) {
            itemMeta.addEnchant(Enchantment.LURE , 1 , false);
        }
        if (statesYML.getLore() != null) {
            itemMeta.setLore(statesYML.getLore().stream().map(Object::toString).toList());
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void loadDefault() {
        for (ItemSlotYML itemSlot : guiYML.getItemSlots()) {
            final String state = "default";
            if(itemSlot.getStates().containsKey(state)){
                ItemStack itemStack = createItem(itemSlot, state);
                if (itemSlot.getSlot() == null) {
                    for (int i = itemSlot.getSlots()[0]; i <= itemSlot.getSlots()[1]; i++) {
                        inventory.setItem(i, itemStack);
                        slotStates.put(i, state);
                    }
                } else{
                    inventory.setItem(itemSlot.getSlot(), itemStack);
                    slotStates.put(itemSlot.getSlot(), state);
                }
            }
        }
    }

    public void clickHandler(int slot, Player player, InventoryAction inventoryAction) {
        ItemSlotYML itemSlot = getItemSlot(slot);
        if (itemSlot == null) {
            return;
        }
        switch (itemSlot.getAction()) {
            case "nextPage":
            case "lastPage":
                buttonHandler(itemSlot.getAction(), player);
                return;
            case "returnPage":
                returnButton(player);
                return;

            default:
                break;
        }
        actionHandler(itemSlot, slot, player, inventoryAction);
    }

    private boolean isInSlot(ItemSlotYML itemSlotYML, int slot) {
        if(itemSlotYML.getSlot() != null) {
            return itemSlotYML.getSlot() == slot;
        }
        return itemSlotYML.getSlots()[0] <= slot && itemSlotYML.getSlots()[1] >= slot;
    }

    protected abstract void actionHandler(ItemSlotYML itemSlot,int slot, Player player, InventoryAction inventoryAction);

    protected abstract void render();
}
