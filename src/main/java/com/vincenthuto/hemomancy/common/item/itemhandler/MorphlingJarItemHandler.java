package com.vincenthuto.hemomancy.common.item.itemhandler;

import net.minecraft.world.item.ItemStack;

public class MorphlingJarItemHandler extends StackBackedItemHandler {
    public MorphlingJarItemHandler(ItemStack itemStack, int size) {
        super(itemStack, size);
    }

    public boolean hasSameBackingStack(MorphlingJarItemHandler other) {
        return other != null && itemStack == other.itemStack;
    }
}
