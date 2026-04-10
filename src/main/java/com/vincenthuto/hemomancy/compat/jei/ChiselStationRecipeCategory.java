
package com.vincenthuto.hemomancy.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * JEI recipe category for the Runic Chisel Station — fully programmatic rendering.
 */
public class ChiselStationRecipeCategory implements IRecipeCategory<ChiselRecipe> {

	// ── Layout constants ──
	private static final int BG_W = 150;
	private static final int BG_H = 100;

	// Slot positions
	private static final int INPUT1_SLOT_X = 5;
	private static final int INPUT1_SLOT_Y = 14;
	private static final int INPUT2_SLOT_X = 5;
	private static final int INPUT2_SLOT_Y = 38;
	private static final int OUTPUT_SLOT_X = 128;
	private static final int OUTPUT_SLOT_Y = 24;

	// Grid display constants matching the 8x8 rune pattern
	private static final int GRID_SIZE = 8;
	private static final int CELL_SIZE = 8;
	private static final int GRID_X = 42;
	private static final int GRID_Y = 8;

	// Colors — matching Juiceinator / Somatic Loom dark theme
	private static final int BG_COLOR = 0xFF0A0204;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
	private static final int LABEL_COLOR = 0xFF888888;

	// Pattern cell colors
	private static final int COLOR_EMPTY = 0xFF1A1010;
	private static final int COLOR_CELL_BORDER = 0xFF2A1414;

	private final IDrawable background;
	private final IDrawable icon;

	public ChiselStationRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(BG_W, BG_H);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(BlockInit.runic_chisel_station.get()));
	}

	// ═══════════════════════════════════════════════════════════════
	//  Drawing
	// ═══════════════════════════════════════════════════════════════

	@Override
	public void draw(ChiselRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX,
			double mouseY) {

		float time = System.nanoTime() / 1_000_000_000f;

		// ── Dark background ──
		gfx.fill(0, 0, BG_W, BG_H, BG_COLOR);
		// Subtle radial glow centered on the pattern grid
		int glowCX = GRID_X + (GRID_SIZE * CELL_SIZE) / 2;
		int glowCY = GRID_Y + (GRID_SIZE * CELL_SIZE) / 2;
		float pulse = 0.5f + 0.5f * Mth.sin(time * 1.8f);
		for (int ring = 40; ring > 0; ring -= 4) {
			float t = ring / 40f;
			int alpha = (int) (14 * pulse * (1f - t));
			int red = (int) (30 * (1f - t));
			if (alpha <= 0) continue;
			gfx.fill(glowCX - ring, glowCY - ring, glowCX + ring, glowCY + ring,
					(alpha << 24) | (red << 16));
		}

		// ── Outer border ──
		drawBorder(gfx, 0, 0, BG_W, BG_H, BORDER_OUTER, BORDER_INNER);

		// ── Slot frames ──
		drawSlotFrame(gfx, INPUT1_SLOT_X - 1, INPUT1_SLOT_Y - 1);
		drawSlotFrame(gfx, INPUT2_SLOT_X - 1, INPUT2_SLOT_Y - 1);
		drawSlotFrame(gfx, OUTPUT_SLOT_X - 1, OUTPUT_SLOT_Y - 1);
		// Reddish tint on output slot
		gfx.fill(OUTPUT_SLOT_X, OUTPUT_SLOT_Y, OUTPUT_SLOT_X + 16, OUTPUT_SLOT_Y + 16, 0x20FF4444);

		// ── Pattern grid ──
		drawPatternGrid(recipe, gfx, time);

		// ── Progress arrow (input → grid → output) ──
		int arrowX = 108;
		int arrowY = GRID_Y + (GRID_SIZE * CELL_SIZE) / 2 - 4;
		int arrowW = 16;
		int arrowH = 8;
		// Track
//		gfx.fill(arrowX, arrowY, arrowX + arrowW, arrowY + arrowH, 0x40200808);
//		gfx.fill(arrowX, arrowY + 3, arrowX + arrowW, arrowY + 5, 0x30400808);
		// Arrowhead
		int headBaseX = arrowX + arrowW;
		int midY = arrowY + arrowH / 2;
		int headLen = 5;
		for (int i = 0; i < headLen; i++) {
			int spread = headLen - 1 - i;
			//gfx.fill(headBaseX + i, midY - spread, headBaseX + i + 1, midY + spread + 1, 0x30600808);
		}
		// Animated fill
		float period = 3f;
		float animProgress = (time % period) / period;
		int filledW = (int) (arrowW * animProgress);
		if (filledW > 0) {
			for (int col = 0; col < filledW; col++) {
				float p = 0.7f + 0.3f * Mth.sin(time * 4f + col * 0.15f);
				int r = (int) (180 * p);
				int g = (int) (20 * p);
				int b = (int) (15 * p);
				int a = (int) (200 * p);
//				gfx.fill(arrowX + col, arrowY + 1, arrowX + col + 1, arrowY + arrowH - 1,
//						(a << 24) | (r << 16) | (g << 8) | b);
			}
		}

		// ── Text labels ──
		Font font = Minecraft.getInstance().font;

		// Slot labels
		gfx.drawString(font, Component.literal("In"), INPUT1_SLOT_X, INPUT1_SLOT_Y - 10, LABEL_COLOR, false);

		// Result name below the grid
		String resultName = I18n.get(recipe.getResultItem().getDescriptionId());
		int nameWidth = font.width(resultName);
		int centerX = GRID_X + (GRID_SIZE * CELL_SIZE) / 2 - nameWidth / 2;
		gfx.drawString(font, resultName, centerX, GRID_Y + GRID_SIZE * CELL_SIZE + 6, 0xFFDAA520, false);

		// Tier label at bottom left
		String tierLabel = "Tier: " + recipe.getTier();
		gfx.drawString(font, tierLabel, 5, BG_H - 14, LABEL_COLOR, false);

		// Rune type label at bottom left
		String typeLabel = "Type: " + recipe.getRuneType().name();
	//	gfx.drawString(font, typeLabel, 5, BG_H - 24, LABEL_COLOR, false);
	}

	private void drawPatternGrid(ChiselRecipe recipe, GuiGraphics gfx, float time) {
		byte[][] pattern = recipe.getPattern();
		if (pattern == null) {
			return;
		}

		// Subtle outline around the entire grid area
		int gridW = GRID_SIZE * CELL_SIZE;
		int gridH = GRID_SIZE * CELL_SIZE;
		gfx.fill(GRID_X - 1, GRID_Y - 1, GRID_X + gridW + 1, GRID_Y + gridH + 1, BORDER_INNER);

		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				int x = GRID_X + j * CELL_SIZE;
				int y = GRID_Y + i * CELL_SIZE;

				// Draw cell border
				gfx.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, COLOR_CELL_BORDER);

				// Draw inner cell
				boolean filled = pattern[i][j] != 0;
				if (filled) {
					// Subtle per-cell pulse for filled cells
					float cellPulse = 0.85f + 0.15f * Mth.sin(time * 3f + i * 0.4f + j * 0.3f);
					int r = (int) (139 * cellPulse);
					int g = 0;
					int b = 0;
					int a = (int) (255 * cellPulse);
					gfx.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1,
							(a << 24) | (r << 16) | (g << 8) | b);
				} else {
					gfx.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, COLOR_EMPTY);
				}
			}
		}
	}

	// ═══════════════════════════════════════════════════════════════
	//  Drawing helpers
	// ═══════════════════════════════════════════════════════════════

	/** Draws a beveled border rectangle. */
	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h, int light, int dark) {
		gfx.fill(x, y, x + w, y + 1, light);
		gfx.fill(x, y + h - 1, x + w, y + h, dark);
		gfx.fill(x, y, x + 1, y + h, light);
		gfx.fill(x + w - 1, y, x + w, y + h, dark);
	}

	/** Draws an 18x18 item slot frame. */
	private void drawSlotFrame(GuiGraphics gfx, int x, int y) {
		gfx.fill(x, y, x + 18, y + 18, SLOT_BG);
		gfx.fill(x, y, x + 18, y + 1, SLOT_BORDER_DARK);
		gfx.fill(x, y, x + 1, y + 18, SLOT_BORDER_DARK);
		gfx.fill(x, y + 17, x + 18, y + 18, SLOT_BORDER_LIGHT);
		gfx.fill(x + 17, y, x + 18, y + 18, SLOT_BORDER_LIGHT);
	}

	// ═══════════════════════════════════════════════════════════════
	//  IRecipeCategory overrides
	// ═══════════════════════════════════════════════════════════════

	@SuppressWarnings("removal")
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
		return Component.translatable("hemomancy.jei.chisel_station");
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
			builder.addSlot(RecipeIngredientRole.INPUT, INPUT1_SLOT_X, INPUT1_SLOT_Y)
					.addIngredients(VanillaTypes.ITEM_STACK, list.get(0));
		}
		if (list.size() >= 2) {
			builder.addSlot(RecipeIngredientRole.INPUT, INPUT2_SLOT_X, INPUT2_SLOT_Y)
					.addIngredients(VanillaTypes.ITEM_STACK, list.get(1));
		}

		// Output slot on the right
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItem());
	}

}
