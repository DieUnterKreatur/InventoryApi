package gg.umu.inventoryApi.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import gg.umu.inventoryApi.ymls.StatesYML;

public class ItemStackUtil {
    public static ItemStack createItemStack(StatesYML statesYML) {
        ItemStack itemStack = new ItemStack(statesYML.getMaterial());
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (statesYML.getAmount() != null) {
            itemStack.setAmount(statesYML.getAmount());
        }

        itemMeta.setDisplayName(statesYML.getName() != null ? statesYML.getName() : " ");

        if (statesYML.isEnchantment()) {
            itemMeta.addEnchant(Enchantment.LURE, 1, false);
        }
        if (statesYML.getLore() != null) {
            itemMeta.setLore(statesYML.getLore().stream().map(Object::toString).toList());
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
