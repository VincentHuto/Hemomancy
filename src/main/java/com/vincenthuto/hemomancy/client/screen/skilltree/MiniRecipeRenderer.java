package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.skilltree.RecipeLookup.FoundRecipe;
import com.vincenthuto.hemomancy.client.screen.skilltree.RecipeLookup.RecipeKind;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;
import com.vincenthuto.hemomancy.common.recipe.GhastlyAlembicRecipe;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Renders a compact recipe preview inside the material info panels.
 * Draws slot frames, item stacks, arrows, and recipe-type-specific details
 * using the same dark theme as the JEI renderers.
 */
public final class MiniRecipeRenderer {

	// ── Color theme for slot rendering ──
	public record Theme(int slotBg, int slotDark, int slotLight, int arrowColor,
						int labelDim, int costColor, int patternFilled, int patternEmpty) {}

	/** Blood-faction theme — dark red/crimson, matching the SkillTreeScreen. */
	public static final Theme BLOOD = new Theme(
			0xFF1A0808, 0xFF0D0303, 0xFF3A1212,
			0xFF606060, 0xFF606870, 0xFFAA4444,
			0xFF8B0000, 0xFF303030
	);

	/** Unstained theme — dark blue/silver/white, matching the UnstainedProgressScreen. */
	public static final Theme UNSTAINED = new Theme(
			0xFF0C1020, 0xFF081018, 0xFF304060,
			0xFF7090B0, 0xFF8098B0, 0xFF6080B0,
			0xFF4060A0, 0xFF1A2030
	);

	// ── Slot size constants ──
	private static final int SLOT_SIZE     = 18;
	private static final int MINI_SLOT     = 14;

	private MiniRecipeRenderer() {}

	/**
	 * Estimates the height the recipe preview will occupy.
	 * Returns 0 if no recipe is provided.
	 */
	public static int estimateHeight(@Nullable FoundRecipe found) {
		if (found == null) return 0;
		return switch (found.kind()) {
			case VANILLA_CRAFTING -> 58;
			case GHASTLY_ALEMBIC      -> 34;
			case MEMORY_WEAVING   -> 40;
			case BLOOD_STRUCTURE  -> 44;
			case CHISEL           -> 56;
			case INCUBATOR        -> 38;
		};
	}

	/**
	 * Draws the mini recipe at the given position. Returns the height consumed.
	 *
	 * @param gfx         graphics context
	 * @param font        font renderer
	 * @param found       the recipe to render
	 * @param x           left edge of the drawing area
	 * @param y           top edge of the drawing area
	 * @param maxW        maximum width available
	 * @param accentColor accent color for labels / borders
	 * @param theme       color theme for slot backgrounds and borders
	 * @return height in pixels consumed by the drawing
	 */
	public static int draw(GuiGraphics gfx, Font font, FoundRecipe found,
						   int x, int y, int maxW, int accentColor, Theme theme) {
		if (found == null) return 0;

		// Section header
		String typeLabel = getTypeLabel(found.kind());
		gfx.drawString(font, typeLabel, x, y, accentColor, false);
		y += 11;

		return 11 + switch (found.kind()) {
			case VANILLA_CRAFTING -> drawVanillaCrafting(gfx, font, (CraftingRecipe) found.recipe(), x, y, maxW, theme);
			case GHASTLY_ALEMBIC      -> drawGhastlyAlembic(gfx, font, (GhastlyAlembicRecipe) found.recipe(), x, y, maxW, theme);
			case MEMORY_WEAVING   -> drawMemoryWeaving(gfx, font, (MemoryWeavingRecipe) found.recipe(), x, y, maxW, theme);
			case BLOOD_STRUCTURE  -> drawBloodStructure(gfx, font, (BloodStructureRecipe) found.recipe(), x, y, maxW, theme);
			case CHISEL           -> drawChisel(gfx, font, (ChiselRecipe) found.recipe(), x, y, maxW, theme);
			case INCUBATOR        -> drawIncubator(gfx, font, (IncubatorRecipe) found.recipe(), x, y, maxW, theme);
		};
	}

	/**
	 * Overload that defaults to the BLOOD theme for backward compatibility.
	 */
	public static int draw(GuiGraphics gfx, Font font, FoundRecipe found,
						   int x, int y, int maxW, int accentColor) {
		return draw(gfx, font, found, x, y, maxW, accentColor, BLOOD);
	}

	// ═══════════════════════════════════════════════════════════════
	//  Per-recipe-type layout methods
	// ═══════════════════════════════════════════════════════════════

	private static int drawVanillaCrafting(GuiGraphics gfx, Font font, CraftingRecipe recipe,
										   int x, int y, int maxW, Theme theme) {
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		int gridW = 3, gridH = 3;
		if (recipe instanceof ShapedRecipe shaped) {
			gridW = shaped.getWidth();
			gridH = shaped.getHeight();
		}

		// Draw compact grid
		int slotS = MINI_SLOT;
		long tick = getAnimTick();
		for (int row = 0; row < gridH; row++) {
			for (int col = 0; col < gridW; col++) {
				int idx = row * gridW + col;
				int sx = x + col * slotS;
				int sy = y + row * slotS;
				drawMiniSlot(gfx, sx, sy, slotS, theme);
				if (idx < ingredients.size()) {
					ItemStack[] items = ingredients.get(idx).getItems();
					if (items.length > 0) {
						ItemStack display = items[(int) ((tick / 30) % items.length)];
						renderMiniItem(gfx, display, sx + 1, sy + 1, slotS - 2);
					}
				}
			}
		}

		// Arrow
		int arrowX = x + gridW * slotS + 4;
		int arrowY = y + (gridH * slotS) / 2 - 3;
		drawArrow(gfx, arrowX, arrowY, theme);

		// Output slot
		int outX = arrowX + 14;
		int outY = y + (gridH * slotS) / 2 - SLOT_SIZE / 2;
		drawSlot(gfx, outX, outY, theme);
		ItemStack result = Minecraft.getInstance().level != null
				? recipe.getResultItem(Minecraft.getInstance().level.registryAccess())
				: ItemStack.EMPTY;
		renderSlotItem(gfx, result, outX + 1, outY + 1);

		return gridH * slotS + 2;
	}

	private static int drawGhastlyAlembic(GuiGraphics gfx, Font font, GhastlyAlembicRecipe recipe,
									   int x, int y, int maxW, Theme theme) {
		long tick = getAnimTick();

		// Optional catalyst slot — top-left, small, with "+" label
		int startX = x;
		if (recipe.requiresCatalyst()) {
			// Small catalyst label
			gfx.drawString(font, "+", x, y, 0xFF886644, false);
			int cslotX = x + 6;
			drawSlot(gfx, cslotX, y, theme);
			ItemStack[] cItems = recipe.getCatalyst().getItems();
			if (cItems.length > 0) {
				renderSlotItem(gfx, cItems[(int) ((tick / 30) % cItems.length)], cslotX + 1, y + 1);
			}
			startX = cslotX + SLOT_SIZE + 2;
		}

		// Input slot
		drawSlot(gfx, startX, y, theme);
		Ingredient ingredient = recipe.getIngredient();
		if (ingredient != null) {
			ItemStack[] items = ingredient.getItems();
			if (items.length > 0) {
				renderSlotItem(gfx, items[(int) ((tick / 30) % items.length)], startX + 1, y + 1);
			}
		}

		// Fire hint (small flame)
		int flameX = startX + SLOT_SIZE + 3;
		int flameY = y + 4;
		drawFlameIcon(gfx, flameX, flameY);

		// Arrow
		int arrowX = flameX + 12;
		int arrowY = y + SLOT_SIZE / 2 - 3;
		drawArrow(gfx, arrowX, arrowY, theme);

		// Output slot
		int outX = arrowX + 14;
		drawSlot(gfx, outX, y, theme);
		renderSlotItem(gfx, recipe.getResultItem(registryAccess()), outX + 1, y + 1);

		// XP and time info
		float xp = recipe.getExperience();
		int cookTime = recipe.getCookingTime();
		if (xp > 0 || cookTime > 0) {
			String info = "";
			if (xp > 0) info += String.format("%.1f XP", xp);
			if (cookTime > 0) {
				if (!info.isEmpty()) info += "  ";
				info += (cookTime / 20) + "s";
			}
			gfx.drawString(font, info, outX + SLOT_SIZE + 3, y + 5, theme.labelDim(), false);
		}

		return SLOT_SIZE + 2;
	}

	private static int drawMemoryWeaving(GuiGraphics gfx, Font font, MemoryWeavingRecipe recipe,
										  int x, int y, int maxW, Theme theme) {
		long tick = getAnimTick();

		// Catalyst input slot (if present)
		int cx = x;
		Ingredient ingredient = recipe.getIngredient();
		if (ingredient != null && ingredient != Ingredient.EMPTY && ingredient.getItems().length > 0) {
			drawSlot(gfx, cx, y, theme);
			ItemStack[] items = ingredient.getItems();
			renderSlotItem(gfx, items[(int) ((tick / 30) % items.length)], cx + 1, y + 1);
			cx += SLOT_SIZE + 2;
		}

		// Tendency pips (compact colored squares, 2 rows of 4)
		Map<EnumBloodTendency, Float> tendencies = recipe.getTendency();
		int pipX = cx + 2;
		int pipY = y + 2;
		int pipSize = 5;
		int pipGap = 1;
		int pipsPerRow = 4;
		int pipIdx = 0;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			float val = tendencies.getOrDefault(tend, 0f);
			if (val > 0.01f) {
				int color = tend.getColor().getColor() | 0xFF000000;
				int row = pipIdx / pipsPerRow;
				int col = pipIdx % pipsPerRow;
				int px = pipX + col * (pipSize + pipGap);
				int py = pipY + row * (pipSize + pipGap);
				gfx.fill(px, py, px + pipSize, py + pipSize, color);
				pipIdx++;
			}
		}

		// Arrow
		int pipsOnTopRow = Math.min(pipIdx, pipsPerRow);
		int arrowX = Math.max(cx + 20, pipX + pipsOnTopRow * (pipSize + pipGap) + 4);
		int arrowY = y + SLOT_SIZE / 2 - 3;
		drawArrow(gfx, arrowX, arrowY, theme);

		// Output
		int outX = arrowX + 14;
		drawSlot(gfx, outX, y, theme);
		renderSlotItem(gfx, recipe.getResultItem(registryAccess()), outX + 1, y + 1);

		// Blood cost
		float bloodCost = recipe.getBloodCost();
		if (bloodCost > 0) {
			gfx.drawString(font, String.format("%.0f Blood", bloodCost),
					x, y + SLOT_SIZE + 2, theme.costColor(), false);
		}

		return SLOT_SIZE + 12;
	}

	private static int drawBloodStructure(GuiGraphics gfx, Font font, BloodStructureRecipe recipe,
										   int x, int y, int maxW, Theme theme) {
		// Held item slot
		int cx = x;
		drawSlot(gfx, cx, y, theme);
		if (recipe.getHeldItem() != null && !recipe.getHeldItem().isEmpty()) {
			renderSlotItem(gfx, recipe.getHeldItem(), cx + 1, y + 1);
		}
		cx += SLOT_SIZE + 1;

		// "+" separator
		gfx.drawString(font, "+", cx, y + 5, theme.labelDim(), false);
		cx += 8;

		// Hit block slot
		drawSlot(gfx, cx, y, theme);
		if (recipe.getHitBlock() != null) {
			ItemStack hitStack = new ItemStack(recipe.getHitBlock().asItem());
			if (!hitStack.isEmpty()) {
				renderSlotItem(gfx, hitStack, cx + 1, y + 1);
			}
		}
		cx += SLOT_SIZE + 2;

		// Pattern block count hint
		if (recipe.getPattern() != null && recipe.getPattern().getSymbolList() != null) {
			java.util.Map<Block, Integer> blockCounts = recipe.getPattern().getBlockCount(false);
			int blockTypes = 0;
			for (var entry : blockCounts.entrySet()) {
				if (entry.getKey() != null && entry.getKey() != Blocks.AIR) blockTypes++;
			}
			if (blockTypes > 0) {
				gfx.drawString(font, "+" + blockTypes + " blk", cx, y + 5, theme.labelDim(), false);
			}
		}

		// Arrow on second row
		int row2Y = y + SLOT_SIZE + 3;
		drawArrow(gfx, x, row2Y + 2, theme);

		// Output slot
		int outX = x + 14;
		drawSlot(gfx, outX, row2Y, theme);
		if (recipe.getResult() != null && !recipe.getResult().isEmpty()) {
			renderSlotItem(gfx, recipe.getResult(), outX + 1, row2Y + 1);
		}

		// Blood cost
		float cost = recipe.getBloodCost();
		if (cost > 0) {
			gfx.drawString(font, String.format("%.0f Blood", cost),
					outX + SLOT_SIZE + 3, row2Y + 5, theme.costColor(), false);
		}

		return SLOT_SIZE + 3 + SLOT_SIZE + 2;
	}

	private static int drawChisel(GuiGraphics gfx, Font font, ChiselRecipe recipe,
								   int x, int y, int maxW, Theme theme) {
		long tick = getAnimTick();

		// Input slots
		int cx = x;
		drawSlot(gfx, cx, y, theme);
		Ingredient ing1 = recipe.getIngredient1();
		if (ing1 != null && ing1.getItems().length > 0) {
			ItemStack[] items = ing1.getItems();
			renderSlotItem(gfx, items[(int) ((tick / 30) % items.length)], cx + 1, y + 1);
		}

		cx += SLOT_SIZE + 1;
		drawSlot(gfx, cx, y, theme);
		Ingredient ing2 = recipe.getIngredient2();
		if (ing2 != null && ing2.getItems().length > 0) {
			ItemStack[] items = ing2.getItems();
			renderSlotItem(gfx, items[(int) ((tick / 30) % items.length)], cx + 1, y + 1);
		}

		// 8x8 pattern miniature (2px per cell)
		byte[][] pattern = recipe.getPattern();
		if (pattern != null) {
			int patY = y + SLOT_SIZE + 3;
			int cellSize = 2;
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					int px = x + col * cellSize;
					int py = patY + row * cellSize;
					boolean filled = pattern[row][col] != 0;
					gfx.fill(px, py, px + cellSize, py + cellSize,
							filled ? theme.patternFilled() : theme.patternEmpty());
				}
			}

			// Arrow next to pattern
			int arrowX = x + 8 * cellSize + 4;
			int arrowY = patY + 8 * cellSize / 2 - 3;
			drawArrow(gfx, arrowX, arrowY, theme);

			// Output slot
			int outX = arrowX + 14;
			int outY = patY + 8 * cellSize / 2 - SLOT_SIZE / 2;
			drawSlot(gfx, outX, outY, theme);
			ItemStack result = recipe.getResultItem();
			if (result != null && !result.isEmpty()) {
				renderSlotItem(gfx, result, outX + 1, outY + 1);
			}

			// Tier label
			gfx.drawString(font, "T" + recipe.getTier(), outX + SLOT_SIZE + 2, outY + 5, theme.labelDim(), false);

			return SLOT_SIZE + 3 + 8 * cellSize + 2;
		}

		return SLOT_SIZE + 2;
	}

	private static int drawIncubator(GuiGraphics gfx, Font font, IncubatorRecipe recipe,
									  int x, int y, int maxW, Theme theme) {
		long tick = getAnimTick();
		NonNullList<Ingredient> catalysts = recipe.getCatalysts();

		// Catalyst slots in a row
		int cx = x;
		for (int i = 0; i < catalysts.size() && i < 4; i++) {
			drawSlot(gfx, cx, y, theme);
			ItemStack[] items = catalysts.get(i).getItems();
			if (items.length > 0) {
				renderSlotItem(gfx, items[(int) ((tick / 30) % items.length)], cx + 1, y + 1);
			}
			cx += SLOT_SIZE + 1;
		}

		// Arrow
		int arrowX = cx + 2;
		int arrowY = y + SLOT_SIZE / 2 - 3;
		drawArrow(gfx, arrowX, arrowY, theme);

		// Output slot
		int outX = arrowX + 14;
		drawSlot(gfx, outX, y, theme);
		ItemStack result = recipe.getResultItemStack();
		if (result != null && !result.isEmpty()) {
			renderSlotItem(gfx, result, outX + 1, y + 1);
		}

		return SLOT_SIZE + 2;
	}

	// ═══════════════════════════════════════════════════════════════
	//  Drawing helpers
	// ═══════════════════════════════════════════════════════════════

	/** Draws a standard 18x18 slot frame at (x, y) using the given theme. */
	private static void drawSlot(GuiGraphics gfx, int x, int y, Theme theme) {
		gfx.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, theme.slotDark());
		gfx.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, theme.slotBg());
		gfx.fill(x + SLOT_SIZE - 1, y + 1, x + SLOT_SIZE, y + SLOT_SIZE, theme.slotLight());
		gfx.fill(x + 1, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, theme.slotLight());
	}

	/** Draws a compact slot frame at (x, y) with given size using the given theme. */
	private static void drawMiniSlot(GuiGraphics gfx, int x, int y, int size, Theme theme) {
		gfx.fill(x, y, x + size, y + size, theme.slotDark());
		gfx.fill(x + 1, y + 1, x + size - 1, y + size - 1, theme.slotBg());
		gfx.fill(x + size - 1, y + 1, x + size, y + size, theme.slotLight());
		gfx.fill(x + 1, y + size - 1, x + size, y + size, theme.slotLight());
	}

	/** Renders a 16x16 item in a standard slot at the given position. */
	private static void renderSlotItem(GuiGraphics gfx, ItemStack stack, int x, int y) {
		if (stack == null || stack.isEmpty()) return;
		gfx.renderItem(stack, x, y);
	}

	/** Renders an item scaled to fit a mini slot (smaller than 16x16). */
	private static void renderMiniItem(GuiGraphics gfx, ItemStack stack, int x, int y, int size) {
		if (stack == null || stack.isEmpty()) return;
		float scale = size / 16.0f;
		PoseStack pose = gfx.pose();
		pose.pushPose();
		pose.translate(x, y, 0);
		pose.scale(scale, scale, 1);
		gfx.renderItem(stack, 0, 0);
		pose.popPose();
	}

	/** Draws a simple right-pointing arrow glyph using the given theme. */
	private static void drawArrow(GuiGraphics gfx, int x, int y, Theme theme) {
		int c = theme.arrowColor();
		gfx.fill(x, y + 3, x + 8, y + 4, c);
		gfx.fill(x + 6, y + 1, x + 7, y + 6, c);
		gfx.fill(x + 7, y + 2, x + 8, y + 5, c);
		gfx.fill(x + 8, y + 3, x + 9, y + 4, c);
	}

	/** Draws a small flame icon for Ghastly Alembic recipes. */
	private static void drawFlameIcon(GuiGraphics gfx, int x, int y) {
		float time = System.nanoTime() / 1_000_000_000f;
		for (int row = 0; row < 8; row++) {
			float rowT = (float) row / 8;
			float flicker = 0.6f + 0.4f * Mth.sin(time * 6f + row * 0.5f);
			int width = (int) (3 + 2 * (1f - rowT));
			for (int col = -width; col <= width; col++) {
				float dist = Math.abs(col) / (float) Math.max(1, width);
				float a = (1f - rowT) * (1f - dist * 0.5f) * flicker;
				int r = (int) (255 * a);
				int g = (int) (Mth.clamp(120 * rowT * a, 0, 200));
				int alpha = (int) (200 * a);
				if (alpha < 10) continue;
				int px = x + 4 + col;
				int py = y + 8 - row;
				gfx.fill(px, py, px + 1, py + 1,
						(alpha << 24) | (r << 16) | (g << 8) | 0x08);
			}
		}
	}

	/** Returns a label describing the recipe type. */
	private static String getTypeLabel(RecipeKind kind) {
		return switch (kind) {
			case VANILLA_CRAFTING -> "Crafting Recipe:";
			case GHASTLY_ALEMBIC      -> "Ghastly Alembic Recipe:";
			case MEMORY_WEAVING   -> "Memory Weaving:";
			case BLOOD_STRUCTURE  -> "Blood Structure:";
			case CHISEL           -> "Chisel Recipe:";
			case INCUBATOR        -> "Incubation Recipe:";
		};
	}

	/** Returns a tick-like counter for cycling ingredient displays. */
	private static long getAnimTick() {
		var level = Minecraft.getInstance().level;
		return level != null ? level.getGameTime() : 0;
	}

	/** Safe registry access — returns the client level's registry or null. */
	@Nullable
	private static net.minecraft.core.RegistryAccess registryAccess() {
		var level = Minecraft.getInstance().level;
		return level != null ? level.registryAccess() : null;
	}
}
