package com.vincenthuto.hemomancy.common.capability.player.unstained;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumNodeShape;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Supplier;

public enum EnumClarityStage {
    AWAKENED(0, 0f, null),
    DISCERNING(1, 25f, null),
    VIGILANT(2, 50f, null),
    RESOLUTE(3, 75f, null),
    ENLIGHTENED(4, 100f, null);

    private final int level;
    private final float minClarity;
    @Nullable
    private Supplier<ItemStack> iconItem;
    @Nullable
    private ResourceLocation iconTexture;
    private EnumNodeShape nodeShape = EnumNodeShape.DIAMOND;

    EnumClarityStage(int level, float minClarity,
                     @Nullable Supplier<ItemStack> iconItem) {
        this.level = level;
        this.minClarity = minClarity;
        this.iconItem = iconItem;
    }

    public int getLevel() {
        return level;
    }

    public String getTranslationKey() {
        return "hemomancy.clarity." + name().toLowerCase(Locale.ROOT);
    }

    public Component getDisplayName() {
        return Component.translatable(getTranslationKey());
    }

    public float getMinClarity() {
        return minClarity;
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

    /** Returns the clarity stage corresponding to the given clarity value. */
    public static EnumClarityStage byClarity(float clarity) {
        EnumClarityStage result = AWAKENED;
        for (EnumClarityStage stage : values()) {
            if (clarity >= stage.minClarity) {
                result = stage;
            }
        }
        return result;
    }
}
