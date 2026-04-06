
package com.vincenthuto.hemomancy.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.ChiselRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ChiselStationRecipeCategory implements IRecipeCategory<ChiselRecipe> {

	private static final ResourceLocation CHISEL_OVERLAY = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/chiseloverlay.png");

	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;

	// Grid display constants matching the 8x8 rune pattern
	private static final int GRID_SIZE = 8;
	private static final int CELL_SIZE = 8;
	private static final int GRID_X = 52;
	private static final int GRID_Y = 10;

	// Colors for the pattern cells
	private static final int COLOR_FILLED = 0xFF8B0000; // dark red for chiseled cells
	private static final int COLOR_EMPTY = 0xFF606060;  // gray for empty cells
	private static final int COLOR_BORDER = 0xFF2A2A2A; // border around each cell

	public ChiselStationRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 100);
		localizedName = I18n.get("hemomancy.jei.chisel_station");
		overlay = guiHelper.createDrawable(CHISEL_OVERLAY, 0, 0, 150, 100);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(BlockInit.runic_chisel_station.get()));
	}

	@Override
	public void draw(ChiselRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
			double mouseY) {
		overlay.draw(graphics);
		drawPatternGrid(recipe, graphics);

		Font font = Minecraft.getInstance().font;

		// Draw the result item name below the grid
		String resultName = I18n.get(recipe.getResultItem().getDescriptionId());
		int nameWidth = font.width(resultName);
		int centerX = GRID_X + (GRID_SIZE * CELL_SIZE) / 2 - nameWidth / 2;
		graphics.drawString(font, resultName, centerX, GRID_Y + GRID_SIZE * CELL_SIZE + 6, 0xFFDAA520, false);

		// Draw tier label
		String tierLabel = "Tier: " + recipe.getTier();
		graphics.drawString(font, tierLabel, 5, 75, 0xFF808080, false);
	}

	private void drawPatternGrid(ChiselRecipe recipe, GuiGraphics graphics) {
		byte[][] pattern = recipe.getPattern();
		if (pattern == null) {
			return;
		}

		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				int x = GRID_X + j * CELL_SIZE;
				int y = GRID_Y + i * CELL_SIZE;

				// Draw border
				graphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, COLOR_BORDER);

				// Draw inner cell (1px border)
				boolean filled = pattern[i][j] != 0;
				int color = filled ? COLOR_FILLED : COLOR_EMPTY;
				graphics.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, color);
			}
		}
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public RecipeType<ChiselRecipe> getRecipeType() {
		return JEIPlugin.chisel_station_recipe_type;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.literal(localizedName);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ChiselRecipe recipe, IFocusGroup focuses) {
		// Input ingredient slots on the left
		List<List<ItemStack>> list = new ArrayList<>();
		for (Ingredient ingr : recipe.getIngredients()) {
			if (ingr != null && ingr != Ingredient.EMPTY && ingr.getItems().length > 0) {
				list.add(Arrays.asList(ingr.getItems()));
			}
		}

		if (list.size() >= 1) {
			builder.addSlot(RecipeIngredientRole.INPUT, 5, 20).addIngredients(VanillaTypes.ITEM_STACK, list.get(0));
		}
		if (list.size() >= 2) {
			builder.addSlot(RecipeIngredientRole.INPUT, 5, 45).addIngredients(VanillaTypes.ITEM_STACK, list.get(1));
		}

		// Output slot on the right
		builder.addSlot(RecipeIngredientRole.OUTPUT, 128, 30).addIngredient(VanillaTypes.ITEM_STACK,
				recipe.getResultItem());
	}

}
