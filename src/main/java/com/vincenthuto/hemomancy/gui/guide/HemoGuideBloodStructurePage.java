package com.vincenthuto.hemomancy.gui.guide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.recipe.RecipeBaseBloodCrafting;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuideMultiblockPage;
import com.vincenthuto.hutoslib.client.screen.guide.TomeLib;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class HemoGuideBloodStructurePage extends GuiGuideMultiblockPage {
	RecipeBaseBloodCrafting recipe;

	public HemoGuideBloodStructurePage(int pageNumIn, String catagoryIn, RecipeBaseBloodCrafting recipe) {
		super(pageNumIn, catagoryIn, recipe.getMultiblockPattern());
		this.recipe = recipe;
	}

	public HemoGuideBloodStructurePage(int pageNumIn, String catagoryIn, String titleIn, RecipeBaseBloodCrafting recipe) {
		super(pageNumIn, catagoryIn, titleIn, recipe.getMultiblockPattern());
		this.recipe = recipe;

	}

	public HemoGuideBloodStructurePage(int pageNumIn, String catagoryIn, String titleIn, String textIn,
			RecipeBaseBloodCrafting recipe) {
		super(pageNumIn, catagoryIn, titleIn, textIn, recipe.getMultiblockPattern());
		this.recipe = recipe;

	}

	public HemoGuideBloodStructurePage(int pageNumIn, String catagoryIn, String titleIn, String subtitleIn, String textIn,
			RecipeBaseBloodCrafting recipe) {
		super(pageNumIn, catagoryIn, titleIn, subtitleIn, textIn, recipe.getMultiblockPattern());
		this.recipe = recipe;

	}

	public HemoGuideBloodStructurePage(int pageNumIn, String categoryIn, String titleIn, String subtitleIn, String textIn,
			ItemStack iconIn, RecipeBaseBloodCrafting recipe) {
		super(pageNumIn, categoryIn, titleIn, subtitleIn, textIn, iconIn,recipe.getMultiblockPattern());
		this.recipe = recipe;

	}

	public HemoGuideBloodStructurePage(int pageNumIn, String categoryIn, String titleIn, ItemStack iconIn,
			RecipeBaseBloodCrafting recipe) {
		super(pageNumIn, categoryIn, titleIn, "", "", iconIn, recipe.getMultiblockPattern());
		this.recipe = recipe;

	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
		super.render(matrices, mouseX, mouseY, partialTicks);
		float guiHeight = 228, guiWidth = 174;
		float left = width / 2 - guiWidth / 2;
		float top = height / 2 - guiHeight / 2;
		Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(new ItemStack(recipe.getHeldItem()),
				(int) (left - guiWidth + 180), (int) (top + guiHeight - 160) );
		Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(new ItemStack(recipe.getHitBlock()),
				(int) (left - guiWidth + 196), (int) (top + guiHeight - 160) );

	}

	@Override
	public TomeLib getOwnerTome() {
		return new HemoLib();
	}

}
