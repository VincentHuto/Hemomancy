package com.vincenthuto.hemomancy.integration;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

	private static final ResourceLocation ID = new ResourceLocation(Hemomancy.MOD_ID, "main");

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		@SuppressWarnings("unused")
		ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
	}

	@Nonnull
	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

}