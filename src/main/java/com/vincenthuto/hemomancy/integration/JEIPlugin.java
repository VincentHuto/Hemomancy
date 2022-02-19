package com.vincenthuto.hemomancy.integration;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.recipe.BloodCraftingRecipes;
import com.vincenthuto.hemomancy.recipe.JuiceinatorDataRecipe;
import com.vincenthuto.hemomancy.recipe.RecallerRecipes;
import com.vincenthuto.hemomancy.recipe.serializer.ChiselRecipe;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
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

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new JuiceinatorRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
	//	registry.addRecipeCategories(new ChiselRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new BloodCraftingCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new RecallerRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new TestChiselRecipeCategory(registry.getJeiHelpers().getGuiHelper()));

	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);
		registry.addRecipes(JuiceinatorDataRecipe.getAllRecipes(world), JuiceinatorRecipeCategory.UID);
	//	registry.addRecipes(ChiselRecipes.runeRecipies, ChiselRecipeCategory.UID);
		registry.addRecipes(BloodCraftingRecipes.RECIPES, BloodCraftingCategory.UID);
		registry.addRecipes(RecallerRecipes.recallerRecipies, RecallerRecipeCategory.UID);
		registry.addRecipes(ChiselRecipe.getAllRecipes(world), TestChiselRecipeCategory.UID);

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(BlockInit.juiceinator.get()), JuiceinatorRecipeCategory.UID);
		//registry.addRecipeCatalyst(new ItemStack(BlockInit.runic_chisel_station.get()), ChiselRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ItemInit.sanguine_formation.get()), BloodCraftingCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.visceral_artificial_recaller.get()),
				RecallerRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.runic_chisel_station.get()), TestChiselRecipeCategory.UID);

	}

	@Nonnull
	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

}