
package com.huto.hemomancy.integration;

import javax.annotation.Nonnull;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.integration.jei.BloodCraftingCategory;
import com.huto.hemomancy.integration.jei.ChiselRecipeCategory;
import com.huto.hemomancy.integration.jei.RecallerRecipeCategory;
import com.huto.hemomancy.recipe.ModBloodCraftingRecipes;
import com.huto.hemomancy.recipe.ModChiselRecipes;
import com.huto.hemomancy.recipe.ModRecallerRecipes;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
	private static final ResourceLocation ID = new ResourceLocation(Hemomancy.MOD_ID, "main");

	@Override
	public void registerItemSubtypes(@Nonnull ISubtypeRegistration registry) {
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new ChiselRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new BloodCraftingCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new RecallerRecipeCategory(registry.getJeiHelpers().getGuiHelper()));

	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		registry.addRecipes(ModChiselRecipes.runeRecipies, ChiselRecipeCategory.UID);
		registry.addRecipes(ModBloodCraftingRecipes.RECIPES, BloodCraftingCategory.UID);
		registry.addRecipes(ModRecallerRecipes.recallerRecipies, RecallerRecipeCategory.UID);

	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {

		registry.addRecipeCatalyst(new ItemStack(BlockInit.runic_chisel_station.get()), ChiselRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ItemInit.sanguine_formation.get()), BloodCraftingCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.visceral_artificial_recaller.get()),
				RecallerRecipeCategory.UID);

	}

	@Nonnull
	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}
}