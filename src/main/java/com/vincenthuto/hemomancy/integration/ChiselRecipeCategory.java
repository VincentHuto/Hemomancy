
package com.vincenthuto.hemomancy.integration;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.recipe.ChiselRecipe;
import com.vincenthuto.hutoslib.client.screen.GuiButtonTextured;
import com.vincenthuto.hutoslib.common.item.HLItemInit;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ChiselRecipeCategory implements IRecipeCategory<ChiselRecipe> {
	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "test_runic_chisel_station");
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;

	public ChiselRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 110);
		localizedName = I18n.get("hemomancy.jei.runic_chisel_station");
		overlay = guiHelper.createDrawable(new ResourceLocation("hemomancy", "textures/gui/chiseloverlay.png"), 0, 0,
				150, 110);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM,
				new ItemStack(BlockInit.runic_chisel_station.get()));

	}

	@Nonnull
	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends ChiselRecipe> getRecipeClass() {
		return ChiselRecipe.class;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return  Component.translatable(localizedName);
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
	int centerX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) - guiWidth + 200 / 2;
	int centerY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2) - guiHeight + 300 / 2;

	@Override
	public void draw(ChiselRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		overlay.draw(matrixStack);

		for (int i = 0; i < buttonList.size(); i++) {
			buttonList.get(i).renderButton(matrixStack, (int) mouseX, (int) mouseY, 10);
		}

	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ChiselRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 4, 11).addIngredients(VanillaTypes.ITEM,
				Arrays.asList(Ingredient.of(HLItemInit.TAG_KNAPPERS).getItems()));
		builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 22, 0).addIngredient(VanillaTypes.ITEM,
				new ItemStack(BlockInit.runic_chisel_station.get()));
		
		buttonList.clear();
		int inc = 0;
		for (int i = 0; i < runeButtonArray.length; i++) {
			for (int j = 0; j < runeButtonArray[i].length; j++) {
				buttonList.add(runeButtonArray[i][j] = new GuiButtonTextured(GUI_Chisel, inc,
						left + guiWidth - (guiWidth - 38 - (j * 8)), top + guiHeight - (160 - (i * 8)), 8, 8, 176, 0,
						false, null, null));
				inc++;
			}
		}

		for (int l = 0; l < runeButtonArray.length; l++) {
			for (int m = 0; m < runeButtonArray[l].length; m++) {
				if (recipe.getPattern()[l][m] == 1) {
					runeButtonArray[l][m].setState(true);
				}
			}
		}
		if (!recipe.getIngredient1().isEmpty() && !recipe.getIngredient2().isEmpty()) {
			builder.addSlot(RecipeIngredientRole.INPUT, 4, 33).addIngredients(VanillaTypes.ITEM,
					Arrays.asList(recipe.getIngredient1().getItems()));
			builder.addSlot(RecipeIngredientRole.INPUT, 4, 55).addIngredients(VanillaTypes.ITEM,
					Arrays.asList(recipe.getIngredient2().getItems()));
			builder.addSlot(RecipeIngredientRole.OUTPUT, 115, 41).addIngredient(VanillaTypes.ITEM,
					recipe.getResultItem());

		} else if (!recipe.getIngredient1().isEmpty() && recipe.getIngredient2().isEmpty()) {
			builder.addSlot(RecipeIngredientRole.INPUT, 4, 33).addIngredients(VanillaTypes.ITEM,
					Arrays.asList(recipe.getIngredient1().getItems()));
			builder.addSlot(RecipeIngredientRole.OUTPUT, 115, 44).addIngredient(VanillaTypes.ITEM,
					recipe.getResultItem());
		}

	}

}
