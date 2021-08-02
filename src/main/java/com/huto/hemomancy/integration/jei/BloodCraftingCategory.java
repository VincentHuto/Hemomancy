
package com.huto.hemomancy.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.recipe.BaseBloodCraftingRecipe;
import com.hutoslib.client.render.block.render.RenderMultiBlockInGui;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.item.ItemStack;

public class BloodCraftingCategory implements IRecipeCategory<BaseBloodCraftingRecipe> {
	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "blood_crafting");
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;

	public BloodCraftingCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 110);
		localizedName = I18n.format("hemomancy.jei.blood_crafting");
		overlay = guiHelper.createDrawable(new ResourceLocation("hemomancy", "textures/gui/bloodcraftingoverlay.png"),
				0, 0, 150, 110);
		icon = guiHelper.createDrawableIngredient(new ItemStack(ItemInit.sanguine_formation.get()));

	}

	@Nonnull
	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends BaseBloodCraftingRecipe> getRecipeClass() {
		return BaseBloodCraftingRecipe.class;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@SuppressWarnings("serial")
	@Override
	public void setIngredients(BaseBloodCraftingRecipe recipe, IIngredients ingredients) {
		List<List<ItemStack>> list = new ArrayList<>();
		List<ItemStack> heldStack = new ArrayList<ItemStack>() {
			{
				add(new ItemStack(recipe.getHeldItem()));
			}
		};
		List<ItemStack> hitStack = new ArrayList<ItemStack>() {
			{
				add(new ItemStack(recipe.getHitBlock().asItem()));
			}
		};
		Collections.addAll(list, heldStack, hitStack);
		ingredients.setInputLists(VanillaTypes.ITEM, list);
		ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(recipe.getCreation().getItem()));
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	int centerX = (Minecraft.getInstance().getMainWindow().getScaledWidth() / 2) - guiWidth / 2;
	int centerY = (Minecraft.getInstance().getMainWindow().getScaledHeight() / 2) - guiHeight / 2;

	@Override
	public void draw(BaseBloodCraftingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		overlay.draw(matrixStack);
		GlStateManager.pushMatrix();
		GlStateManager.translatef(centerX + 50, centerY + 100, 10);
		RenderMultiBlockInGui.renderPatternInGUI(matrixStack, Minecraft.getInstance(), recipe.getBundledPattern());
		Minecraft.getInstance().fontRenderer.func_238418_a_(new StringTextComponent("Held Item"), -50,
				(int) (Minecraft.getInstance().fontRenderer.FONT_HEIGHT) - 22, 150, 0);
		Minecraft.getInstance().fontRenderer.func_238418_a_(new StringTextComponent("Hit Block"), -50,
				(int) (Minecraft.getInstance().fontRenderer.FONT_HEIGHT) + 25, 150, 0);
		GlStateManager.popMatrix();

	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull BaseBloodCraftingRecipe recipe,
			@Nonnull IIngredients ingredients) {
		/*
		 * recipeLayout.getItemStacks().init(0, true, 22, 0);
		 * recipeLayout.getItemStacks().set(0, new
		 * ItemStack(ItemInit.sanguine_formation.get()));
		 */
		if (ingredients.getInputs(VanillaTypes.ITEM).size() > 1) {
			recipeLayout.getItemStacks().init(1, true, 4, 30);
			recipeLayout.getItemStacks().set(1, ingredients.getInputs(VanillaTypes.ITEM).get(0));
			recipeLayout.getItemStacks().init(2, true, 4, 50);
			recipeLayout.getItemStacks().set(2, ingredients.getInputs(VanillaTypes.ITEM).get(1));
			recipeLayout.getItemStacks().init(3, false, 114, 40);
			recipeLayout.getItemStacks().set(3, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
		}
	}

}
