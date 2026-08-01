package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

record ScarTreeEntry(ScarRecipe recipe, ResourceLocation id, ScarDefinition definition, ItemStack result,
		EnumBloodTendency tendency, int tier, boolean sideBranch) {
	ScarTreeEntry {
		result = result.copy();
	}
}
