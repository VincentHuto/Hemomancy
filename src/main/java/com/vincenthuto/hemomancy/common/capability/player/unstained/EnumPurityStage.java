package com.vincenthuto.hemomancy.common.capability.player.unstained;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumNodeShape;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Supplier;

public enum EnumPurityStage {
    CORRUPTED(0, 0f, 0f, null),
    TAINTED(1, 25f, 0.10f, null),
    CLEANSING(2, 50f, 0.25f, null),
    ABSOLVED(3, 75f, 0.50f, null),
    PURIFIED(4, 100f, 1.0f, null);

    private final int level;
    private final float minPurity;
    /** Multiplier reduction to blood manipulation effectiveness at this stage. */
    private final float bloodMagicPenalty;
    @Nullable
    private Supplier<ItemStack> iconItem;
    @Nullable
    private ResourceLocation iconTexture;
    private EnumNodeShape nodeShape = EnumNodeShape.DIAMOND;

    EnumPurityStage(int level, float minPurity, float bloodMagicPenalty,
                    @Nullable Supplier<ItemStack> iconItem) {
        this.level = level;
        this.minPurity = minPurity;
        this.bloodMagicPenalty = bloodMagicPenalty;
        this.iconItem = iconItem;
    }

    public int getLevel() {
        return level;
    }

    public String getTranslationKey() {
        return "hemomancy.purity." + name().toLowerCase(Locale.ROOT);
    }

    public Component getDisplayName() {
        return Component.translatable(getTranslationKey());
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

    /** Sets the texture icon (any standard ResourceLocation). */
    public void setIconTexture(ResourceLocation texture) {
        this.iconTexture = texture;
    }

    /** Returns the icon item to render inside this stage's node, or null if none. */
    @Nullable
    public ItemStack getIconItem() {
        return iconItem != null ? iconItem.get() : null;
    }

    /** Returns the texture icon to render inside this stage's node, or null if none. */
    @Nullable
    public ResourceLocation getIconTexture() {
        return iconTexture;
    }

    /** Sets the node shape for this stage. */
    public void setNodeShape(EnumNodeShape shape) {
        this.nodeShape = shape;
    }

    /** Returns the node shape for this stage. Defaults to DIAMOND. */
    public EnumNodeShape getNodeShape() {
        return nodeShape;
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
