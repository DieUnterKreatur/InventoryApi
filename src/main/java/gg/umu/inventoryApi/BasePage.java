package gg.umu.inventoryApi;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.ObjIntConsumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import com.destroystokyo.paper.profile.PlayerProfile;

import gg.umu.inventoryApi.util.ItemStackUtil;
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
     *      add all u Content and then use the Call method :o
     */
    protected HashMap<Integer, PlayerProfile> playerProfiles = new HashMap<>();
    /*
     * protected HashMap<Integer,ItemDisplayModel> itemDisplays = new HashMap<>();
     */
    @Setter
    @Getter
    protected Object owner;
    protected int page = 0;
    protected int pageCount;

    protected BasePage(GuiYML guiYML, Object owner) {
        this.guiYML = guiYML;
        this.owner = owner;
        if (guiYML.getName() != null) {
            name = guiYML.getName();
        }
    }

    protected void createInventory() {
        inventory = Bukkit.getServer().createInventory(this, guiYML.getPageSize().getValue(), name);
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

    // Used for for Lists Iterate trough them
    /**
     *
     * @param <T>
     * @param list
     * @param itemSlotYML
     * @param callable    (T item, int index)
     * @return return 0 null if it dont has an Item Slo
     */
    protected <T> int listHandler(List<T> list, ItemSlotYML itemSlotYML, ObjIntConsumer<T> callable) {
        if (itemSlotYML.getSlots() == null) {
            return 0;
        }
        int index = itemSlotYML.getSlots()[0];
        int listSize = (itemSlotYML.getSlots()[1] - itemSlotYML.getSlots()[0]) + 1;
        list = getSubList(list, listSize);
        for (var item : list) {
            callable.accept(item, index);
            index++;
        }
        return listSize;
    }

    protected void changeAmount(String action, int amount) {
        getItemSlot(amount).ifPresentOrElse(itemSlot -> {
            if (itemSlot.getSlot() == null) {
                log.error(action + " : works only with single slots");
                return;
            }
            inventory.getItem(itemSlot.getSlot()).setAmount(amount);
        }, () -> log.error("action {} not found in getItemSlot", action));
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

    protected Optional<ItemSlotYML> getItemSlot(String action) {
        return guiYML.getItemSlots().stream().filter(slot -> slot.getAction().equals(action))
                .findFirst();
    }

    protected Optional<ItemSlotYML> getItemSlot(int slot) {
        return guiYML.getItemSlots().stream().filter(item -> isInSlot(item, slot)).findFirst();
    }

    /**
     * Changes the state of a slot and updates the inventory.
     *
     * @param slot  Slot number
     * @param state New state
     */
    protected void changeState(int slot, String state) {
        getItemSlot(slot).ifPresentOrElse(itemSlot -> {
            var states = itemSlot.getStates();
            if (!states.containsKey(state)) {
                log.error("State '{}' does not exist for slot {}", state, slot);
                return;
            }
            ItemStack itemStack = createItem(itemSlot, state);
            inventory.setItem(slot, itemStack);
            slotStates.replace(slot, state);
        }, () -> log.error("Slot {} not found in getItemSlot", slot));
    }

    protected void renderPlayerProfile() {
        playerProfiles.forEach((id, playerProfile) -> {
            var itemStack = inventory.getItem(id);
            if (itemStack != null && itemStack.getItemMeta() instanceof SkullMeta skullMeta ) {
                skullMeta.setPlayerProfile(playerProfile);
                itemStack.setItemMeta(skullMeta);
                inventory.setItem(id, itemStack);
            }
        });
        playerProfiles.clear();
    }

    protected void renderItemDisplay(ItemStack itemStack) {
    }

    /*
     * protected void renderItemDisplay() {
     * for (Map.Entry<Integer, ItemDisplayModel> entry : itemDisplays.entrySet()) {
     * ItemStack itemStack = inventory.getItem(entry.getKey());
     * if (itemStack != null) {
     * ItemMeta itemMeta = itemStack.getItemMeta();
     * itemMeta.displayName(entry.getValue().getName());
     * if (entry.getValue().getLore() != null) {
     * itemMeta.lore(entry.getValue().getLore());
     * }
     * itemStack.setItemMeta(itemMeta);
     * inventory.setItem(entry.getKey(), itemStack);
     * }
     * }
     * itemDisplays.clear();
     * }
     */
    // Handels the SiteLogic. Refreshs the page / with opening a new inventory
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
        if (history.size() - 2 > 0) {
            gui.openPage(player, history.get(history.size() - 2));

        }
    }

    private ItemStack createItem(ItemSlotYML itemSlot, String state) {
        StatesYML statesYML = itemSlot.getStates().get(state);
        return ItemStackUtil.createItemStack(statesYML);
    }

    private void loadDefault() {
        for (ItemSlotYML itemSlot : guiYML.getItemSlots()) {
            final String state = "default";
            if (itemSlot.getStates().containsKey(state)) {
                ItemStack itemStack = createItem(itemSlot, state);
                if (itemSlot.getSlot() == null) {
                    for (int i = itemSlot.getSlots()[0]; i <= itemSlot.getSlots()[1]; i++) {
                        inventory.setItem(i, itemStack);
                        slotStates.put(i, state);
                    }
                } else {
                    inventory.setItem(itemSlot.getSlot(), itemStack);
                    slotStates.put(itemSlot.getSlot(), state);
                }
            }
        }
    }

    public void clickHandler(int slot, Player player, InventoryAction inventoryAction) {
        Optional<ItemSlotYML> optionalItemSlot = getItemSlot(slot);
        if (optionalItemSlot.isEmpty()) {
            return;
        }
        var itemSlot = optionalItemSlot.get();
        String action = itemSlot.getAction();
        switch (action) {
            case "nextPage":
            case "lastPage":
                buttonHandler(action, player);
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
        if (itemSlotYML.getSlot() != null) {
            return itemSlotYML.getSlot() == slot;
        }
        return itemSlotYML.getSlots()[0] <= slot && itemSlotYML.getSlots()[1] >= slot;
    }

    protected abstract void actionHandler(ItemSlotYML itemSlot, int slot, Player player,
            InventoryAction inventoryAction);

    protected abstract void render();
}
