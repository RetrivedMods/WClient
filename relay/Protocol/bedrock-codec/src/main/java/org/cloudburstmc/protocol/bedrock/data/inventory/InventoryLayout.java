package org.cloudburstmc.protocol.bedrock.data.inventory;

public enum InventoryLayout {
    NONE,
    /**
     * SURVIVAL before v924
     */
    INVENTORY_ONLY,
    /**
     * RECIPE_BOOK before v924
     */
    DEFAULT,
    /**
     * CREATIVE before v924
     */
    RECIPE_BOOK_ONLY;

    public static final InventoryLayout[] VALUES = values();
}