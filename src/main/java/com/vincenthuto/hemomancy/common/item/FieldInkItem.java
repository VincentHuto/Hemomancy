package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoDefinition;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * An ink item that carries a {@link MemoDefinition.MemoPath} affinity.
 * Field notes must be filled with an ink whose path matches the content the
 * player intends to record (Harbinger or Unstained). Holding the path on the
 * item itself removes the need for hard-coded {@code ItemInit} checks in
 * {@link com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoHelper}.
 */
public class FieldInkItem extends Item {

    private final MemoDefinition.MemoPath inkPath;

    public FieldInkItem(Properties properties, MemoDefinition.MemoPath inkPath) {
        super(properties);
        this.inkPath = inkPath;
    }

    /** The ink-path affinity baked into this ink item. Never {@code null}. */
    public MemoDefinition.MemoPath getInkPath() {
        return inkPath;
    }

    /**
     * Convenience helper — returns the {@link MemoDefinition.MemoPath} for the
     * given stack if it is a {@link FieldInkItem}, otherwise {@code null}.
     */
    public static MemoDefinition.MemoPath getPathFromStack(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof FieldInkItem ink) {
            return ink.getInkPath();
        }
        return null;
    }
}
