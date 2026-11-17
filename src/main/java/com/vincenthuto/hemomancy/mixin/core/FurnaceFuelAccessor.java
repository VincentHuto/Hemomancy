package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Furnace Veins' Sculpting (core-loop round two P4) feeds a furnace blood as fuel while the verb is held: it reads and
 * raises the burn time and the recipe type the furnace cooks by. Nothing else is touched; the furnace's own tick cooks.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public interface FurnaceFuelAccessor {
	@Accessor("litTime")
	int hemomancy$getLitTime();

	@Accessor("litTime")
	void hemomancy$setLitTime(int litTime);

	@Accessor("litDuration")
	int hemomancy$getLitDuration();

	@Accessor("litDuration")
	void hemomancy$setLitDuration(int litDuration);

	@Accessor("recipeType")
	RecipeType<? extends AbstractCookingRecipe> hemomancy$getRecipeType();
}
