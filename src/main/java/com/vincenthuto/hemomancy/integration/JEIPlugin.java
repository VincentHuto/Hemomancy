package com.vincenthuto.hemomancy.integration;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.recipe.BloodCraftingRecipes;
import com.vincenthuto.hemomancy.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.recipe.JuiceinatorRecipe;
import com.vincenthuto.hemomancy.recipe.RecallerRecipe;
import com.vincenthuto.hemomancy.recipe.RecipeBaseBloodCrafting;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

	private static final ResourceLocation ID = new ResourceLocation(Hemomancy.MOD_ID, "main");
	public static final RecipeType<JuiceinatorRecipe> juiceinator_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"juiceinator", JuiceinatorRecipe.class);
	public static final RecipeType<RecipeBaseBloodCrafting> blood_crafting_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "blood_crafting", RecipeBaseBloodCrafting.class);
	public static final RecipeType<RecallerRecipe> recaller_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"recaller", RecallerRecipe.class);
	public static final RecipeType<ChiselRecipe> runic_chisel_station_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"runic_chisel_station", ChiselRecipe.class);

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new JuiceinatorRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new BloodCraftingCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new RecallerRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new ChiselRecipeCategory(registry.getJeiHelpers().getGuiHelper()));

	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);
		registry.addRecipes(juiceinator_recipe_type, JuiceinatorRecipe.getAllRecipes(world));
		registry.addRecipes(blood_crafting_recipe_type, BloodCraftingRecipes.RECIPES);
		registry.addRecipes(recaller_recipe_type, RecallerRecipe.getAllRecipes(world));
		registry.addRecipes(runic_chisel_station_recipe_type, ChiselRecipe.getAllRecipes(world));

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(BlockInit.juiceinator.get()), juiceinator_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(ItemInit.sanguine_formation.get()), blood_crafting_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.visceral_artificial_recaller.get()), recaller_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.runic_chisel_station.get()),
				runic_chisel_station_recipe_type);

	}

	@Nonnull
	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

}