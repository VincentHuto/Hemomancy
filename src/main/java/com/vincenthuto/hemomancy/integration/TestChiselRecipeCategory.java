
package com.vincenthuto.hemomancy.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.recipe.serializer.ChiselRecipe;
import com.vincenthuto.hutoslib.client.screen.GuiButtonTextured;
import com.vincenthuto.hutoslib.common.item.HLItemInit;

//GlStateManager;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

public class TestChiselRecipeCategory implements IRecipeCategory<ChiselRecipe> {
	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "test_runic_chisel_station");
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;

	public TestChiselRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 110);
		localizedName = I18n.get("hemomancy.jei.test_runic_chisel_station");
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
	public Class<? extends ChiselRecipe> getRecipeClass() {
		return ChiselRecipe.class;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setIngredients(ChiselRecipe recipe, IIngredients ingredients) {
		List<List<ItemStack>> list = new ArrayList<>();
		list.add(Arrays.asList(recipe.getIngredient1().getItems()));
		list.add(Arrays.asList(recipe.getIngredient2().getItems()));
		list.add(Arrays.asList(Ingredient.of(HLItemInit.TAG_KNAPPERS).getItems()));

		ingredients.setInputLists(VanillaTypes.ITEM, list);
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return new TextComponent(localizedName);
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
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull ChiselRecipe recipe,
			@Nonnull IIngredients ingredients) {
		
		recipeLayout.getItemStacks().init(10, true, 3, 10);
		recipeLayout.getItemStacks().set(10, ingredients.getInputs(VanillaTypes.ITEM).get(2));

		recipeLayout.getItemStacks().init(0, true, 22, 0);
		recipeLayout.getItemStacks().set(0, new ItemStack(BlockInit.runic_chisel_station.get()));
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
			recipeLayout.getItemStacks().init(secondaryIn, true, 3, 54);
			recipeLayout.getItemStacks().set(secondaryIn, ingredients.getInputs(VanillaTypes.ITEM).get(1));

			recipeLayout.getItemStacks().init(outputRune, false, 114, 40);
			recipeLayout.getItemStacks().set(outputRune, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
		}
	}

}
