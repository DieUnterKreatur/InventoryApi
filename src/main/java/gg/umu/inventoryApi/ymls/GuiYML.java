package gg.umu.inventoryApi.ymls;

import java.util.List;

import gg.umu.inventoryApi.PageSize;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GuiYML {
    private String name;
    private PageSize pageSize;
    private boolean removable;
    private List<ItemSlotYML> itemSlots;
}
