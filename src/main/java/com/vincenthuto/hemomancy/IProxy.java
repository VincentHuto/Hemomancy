package com.vincenthuto.hemomancy;

import com.vincenthuto.hemomancy.recipe.ChiselRecipe;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public interface IProxy {

	default void openBinderGui() {
	}

	default void openJarGui() {
	}

	default void openStaffGui() {
	}

	default void openManipGui() {

	}

	default void openGuideGui() {

	}

	default void openVeinGui() {

	}

	default void registerHandlers() {
	}

	default void openPatternGui(RegistryObject<Item> rune, ChiselRecipe recipe) {

	}

}
