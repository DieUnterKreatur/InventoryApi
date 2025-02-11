package gg.umu.inventoryApi;

public enum PageSize {
    XS(9),
    S(18),
    M(27),
    L(36),
    XL(45),
    XXL(54);
    private int pageSize;
    PageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    public int getValue() {
        return pageSize;
    }
}

