package com.vincenthuto.hemomancy.common.capability.player.unstained;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

public enum EnumPurityStage {
    CORRUPTED(0, "Corrupted", 0f, 0f, null),
    TAINTED(1, "Tainted", 25f, 0.10f, null),
    CLEANSING(2, "Cleansing", 50f, 0.25f, null),
    ABSOLVED(3, "Absolved", 75f, 0.50f, null),
    PURIFIED(4, "Purified", 100f, 1.0f, null);

    private final int level;
    private final String title;
    private final float minPurity;
    /** Multiplier reduction to blood manipulation effectiveness at this stage. */
    private final float bloodMagicPenalty;
    @Nullable
    private Supplier<ItemStack> iconItem;

    EnumPurityStage(int level, String title, float minPurity, float bloodMagicPenalty,
                    @Nullable Supplier<ItemStack> iconItem) {
        this.level = level;
        this.title = title;
        this.minPurity = minPurity;
        this.bloodMagicPenalty = bloodMagicPenalty;
        this.iconItem = iconItem;
    }

    public int getLevel() {
        return level;
    }

    public String getTitle() {
        return title;
    }

    public float getMinPurity() {
        return minPurity;
    }

    public float getBloodMagicPenalty() {
        return bloodMagicPenalty;
    }

    /** Sets the icon item supplier (called after item registration). */
    public void setIconItem(Supplier<ItemStack> icon) {
        this.iconItem = icon;
    }

    /** Returns the icon item to render inside this stage's node, or null if none. */
    @Nullable
    public ItemStack getIconItem() {
        return iconItem != null ? iconItem.get() : null;
    }

    /** Returns the purity stage corresponding to the given purity value. */
    public static EnumPurityStage byPurity(float purity) {
        EnumPurityStage result = CORRUPTED;
        for (EnumPurityStage stage : values()) {
            if (purity >= stage.minPurity) {
                result = stage;
            }
        }
        return result;
    }
}
