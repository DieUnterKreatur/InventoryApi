package gg.umu.inventoryApi.ymls;

import java.util.List;

import org.bukkit.Material;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatesYML {
    private Material material;
    private Integer amount;
    private boolean enchantment;
    private String name;
    private List<String> lore;
}