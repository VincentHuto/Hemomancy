package com.vincenthuto.hemomancy.common.brewing;

import net.minecraft.world.item.ItemStack;

public record BrewingMatch(String operation, ItemStack input, ItemStack catalyst,
                           ItemStack catalyst2, ItemStack result, int blood, int ticks) {
    public BrewingMatch {
        input = input.copyWithCount(1);
        catalyst = catalyst.copyWithCount(1);
        catalyst2 = catalyst2.isEmpty() ? ItemStack.EMPTY : catalyst2.copyWithCount(1);
        result = result.copy();
    }
}
