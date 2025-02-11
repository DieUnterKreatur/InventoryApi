package gg.umu.inventoryApi.ymls;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemSlotYML {
    private String action;
    private Integer slot;
    private int[] slots;
    private HashMap<String, StatesYML> states;
    private HashMap<String, String> texts;

}
