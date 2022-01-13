package com.vincenthuto.hemomancy.integration;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.recipe.JuiceinatorDataRecipe;

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
	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);
		registry.addRecipes(JuiceinatorDataRecipe.getAllRecipes(world), JuiceinatorRecipeCategory.UID);

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(BlockInit.juiceinator.get()), JuiceinatorRecipeCategory.UID);

	}

	@Nonnull
	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

}