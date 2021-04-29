
package com.huto.hemomancy.integration.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.gui.GuiButtonTextured;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.recipe.RecipeChiselStation;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public class ChiselRecipeCategory implements IRecipeCategory<RecipeChiselStation> {
	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "runic_chisel_station");
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;

	public ChiselRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 110);
		localizedName = I18n.format("hemomancy.jei.chisel_station");
		overlay = guiHelper.createDrawable(new ResourceLocation("hemomancy", "textures/gui/chiseloverlay.png"), 0, 0,
				150, 110);
		icon = guiHelper.createDrawableIngredient(new ItemStack(BlockInit.runic_chisel_station.get()));

	}

	@Nonnull
	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends RecipeChiselStation> getRecipeClass() {
		return RecipeChiselStation.class;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setIngredients(RecipeChiselStation recipe, IIngredients ingredients) {
		List<List<ItemStack>> list = new ArrayList<>();
		for (Ingredient ingr : recipe.getInputs()) {
			list.add(Arrays.asList(ingr.getMatchingStacks()));
		}
		ingredients.setInputLists(VanillaTypes.ITEM, list);
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
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

	public GuiButtonTextured[][] runeButtonArray = new GuiButtonTextured[8][8];
	protected List<Button> buttonList = Lists.<Button>newArrayList();
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	private static final ResourceLocation GUI_Chisel = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/chisel_station.png");
	int centerX = (Minecraft.getInstance().getMainWindow().getScaledWidth() / 2) - guiWidth + 200 / 2;
	int centerY = (Minecraft.getInstance().getMainWindow().getScaledHeight() / 2) - guiHeight + 300 / 2;

	@Override
	public void draw(RecipeChiselStation recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		overlay.draw(matrixStack);
		GlStateManager.pushMatrix();

		GlStateManager.translatef(centerX, centerY, 10);

		for (int i = 0; i < buttonList.size(); i++) {
			buttonList.get(i).renderWidget(matrixStack, (int) mouseX, (int) mouseY, 10);
		}
		GlStateManager.popMatrix();

		/*
		 * fontRenderer.func_238418_a_(new StringTextComponent("Runes to Activate"), 20,
		 * (int) (fontRenderer.FONT_HEIGHT) - 8, 130, 10);
		 * fontRenderer.func_238418_a_(new
		 * StringTextComponent(recipe.getActivatedRunes().toString()), -20, (int)
		 * (fontRenderer.FONT_HEIGHT), 150, 0);
		 */

	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull RecipeChiselStation recipe,
			@Nonnull IIngredients ingredients) {
		recipeLayout.getItemStacks().init(0, true, 22, 0);
		recipeLayout.getItemStacks().set(0, new ItemStack(BlockInit.runic_chisel_station.get()));
		buttonList.clear();
		int inc = 0;
		for (int i = 0; i < runeButtonArray.length; i++) {
			for (int j = 0; j < runeButtonArray.length; j++) {
				buttonList.add(runeButtonArray[i][j] = new GuiButtonTextured(GUI_Chisel, inc,
						left + guiWidth - (guiWidth - 38 - (i * 8)), top + guiHeight - (160 - (j * 8)), 8, 8, 176, 0,
						false, null, null));
				inc++;
			}
		}
		for (int l = 0; l < runeButtonArray.length; l++) {
			for (int m = 0; m < runeButtonArray.length; m++) {
				for (int k = 0; k < recipe.getActivatedRunes().size(); k++) {
					if (runeButtonArray[l][m].getId() == recipe.getActivatedRunes().get(k)) {
						runeButtonArray[l][m].setState(true);
					}
				}
			}
		}

		int runeIn = 1;
		int secondaryIn = 2;
		int outputRune = 3;
		if (ingredients.getInputs(VanillaTypes.ITEM).size() == 1) {
			recipeLayout.getItemStacks().init(runeIn, true, 3, 32);
			recipeLayout.getItemStacks().set(runeIn, ingredients.getInputs(VanillaTypes.ITEM).get(0));
			recipeLayout.getItemStacks().init(outputRune, false, 114, 40);
			recipeLayout.getItemStacks().set(outputRune, ingredients.getOutputs(VanillaTypes.ITEM).get(0));

		} else if (ingredients.getInputs(VanillaTypes.ITEM).size() > 1) {
			recipeLayout.getItemStacks().init(runeIn, true, 3, 32);
			recipeLayout.getItemStacks().set(runeIn, ingredients.getInputs(VanillaTypes.ITEM).get(0));
			recipeLayout.getItemStacks().init(secondaryIn, true, 3, 53);
			recipeLayout.getItemStacks().set(secondaryIn, ingredients.getInputs(VanillaTypes.ITEM).get(1));
			recipeLayout.getItemStacks().init(outputRune, false, 114, 40);
			recipeLayout.getItemStacks().set(outputRune, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
		}
	}

}
