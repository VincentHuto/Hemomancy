package com.vincenthuto.hemomancy.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.init.ItemInit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class RecallerRecipes {

	public static final DeferredRegister<RecipeRecaller> RECALLERRECIPES = DeferredRegister.create(RecipeRecaller.class,
			Hemomancy.MOD_ID);
	public static Supplier<IForgeRegistry<RecipeRecaller>> RECALLER_TYPE_REGISTRY = RECALLERRECIPES
			.makeRegistry("recaller_recipe_types", () -> new RegistryBuilder<RecipeRecaller>()
					.setMaxID(Integer.MAX_VALUE - 1).setDefaultKey(new ResourceLocation(Hemomancy.MOD_ID, "null")));
	public static final List<RecipeRecaller> recallerRecipies = new ArrayList<>();

	@SuppressWarnings("serial")
	public static final RegistryObject<RecipeRecaller> memory_blood_absorption = RECALLERRECIPES.register(
			"memory_blood_absorption",
			() -> registerRecallerRecipe(new ResourceLocation(Hemomancy.MOD_ID, "memory_blood_absorption"),
					new HashMap<EnumBloodTendency, Float>() {
						{
							put(EnumBloodTendency.ANIMUS, 0f);
							put(EnumBloodTendency.MORTEM, 0f);
							put(EnumBloodTendency.DUCTILIS, 0f);
							put(EnumBloodTendency.FERRIC, 0f);
							put(EnumBloodTendency.LUX, 0f);
							put(EnumBloodTendency.TENEBRIS, 0f);
							put(EnumBloodTendency.FLAMMEUS, 0f);
							put(EnumBloodTendency.CONGEATIO, 0f);
						}
					}, new ItemStack(ItemInit.memory_blood_absorption.get(), 1)));

	@SuppressWarnings("serial")
	public static final RegistryObject<RecipeRecaller> memory_blood_rush = RECALLERRECIPES.register("memory_blood_rush",
			() -> registerRecallerRecipe(new ResourceLocation(Hemomancy.MOD_ID, "memory_blood_rush"),
					new HashMap<EnumBloodTendency, Float>() {
						{
							put(EnumBloodTendency.ANIMUS, 0f);
							put(EnumBloodTendency.MORTEM, 2f);
							put(EnumBloodTendency.DUCTILIS, 0.5f);
							put(EnumBloodTendency.FERRIC, 0.25f);
							put(EnumBloodTendency.LUX, 0f);
							put(EnumBloodTendency.TENEBRIS, 1f);
							put(EnumBloodTendency.FLAMMEUS, 2f);
							put(EnumBloodTendency.CONGEATIO, 1f);
						}
					}, new ItemStack(ItemInit.memory_blood_rush.get(), 1)));

	public static List<RecipeRecaller> getRecallerRecipies() {
		return recallerRecipies;
	}

	public static RecipeRecaller registerRecallerRecipe(ResourceLocation rl, Map<EnumBloodTendency, Float> tends,
			ItemStack output) {
		RecipeRecaller recipe = new RecipeRecaller(rl, tends, output);
		recallerRecipies.add(recipe);
		return recipe;
	}

}
