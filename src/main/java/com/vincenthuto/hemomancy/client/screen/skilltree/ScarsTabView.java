package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.List;

import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Static rendering and hit-testing helpers for the <em>Cerebral Scarring</em>
 * browser tab of {@link HarbingerProgressScreen}.
 * <p>
 * All mutable tab state lives in {@link ScarsTabState}; the shared screen
 * geometry / font / player-degree context lives in {@link ProgressScreenContext}.
 */
public final class ScarsTabView {

	private ScarsTabView() {}

	// ── Tab accent colour (mirrors HarbingerProgressScreen.Tab.SCARS) ──
	static final int TAB_COLOR = 0xFF44AACC;

	// ────────────────────────────────────────────────────────────
	//  Top-level draw call
	// ────────────────────────────────────────────────────────────

	/**
	 * Draws the full Scars tab content (sidebar + pattern grid + info panel).
	 */
	public static void draw(GuiGraphics gfx, ProgressScreenContext ctx,
							ScarsTabState state, int mouseX, int mouseY, float partial) {
		if (state.scarRecipes.isEmpty()) {
			gfx.drawCenteredString(ctx.font(), "No Scar recipes found",
					ctx.guiLeft() + ctx.guiWidth() / 2,
					ctx.guiTop()  + ctx.guiHeight() / 2, 0xFF666666);
			return;
		}

		int contentX = ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 6;
		int contentW  = ctx.guiWidth() - ProgressScreenContext.TIER_SIDEBAR_W - 10;

		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);

		drawTierSidebar(gfx, ctx, state, mouseX, mouseY);

		if (state.selectedScarTier == null) {
			gfx.drawCenteredString(ctx.font(), "Select a tier",
					contentX + contentW / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}

		List<ScarRecipe> tierRecipes =
				state.chiselByTier.getOrDefault(state.selectedScarTier, List.of());
		if (tierRecipes.isEmpty()) {
			gfx.drawCenteredString(ctx.font(), "No recipes in this tier",
					contentX + contentW / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}
		if (state.selectedScarIndexInTier >= tierRecipes.size())
			state.selectedScarIndexInTier = 0;
		ScarRecipe recipe = tierRecipes.get(state.selectedScarIndexInTier);

		int patternAreaW = contentW / 2;
		int patternX = contentX;
		int infoX    = contentX + patternAreaW + 10;
		int infoW    = contentW - patternAreaW - 20;

		drawPatternGrid(gfx, ctx, recipe, patternX + 10, ctx.guiTop() + 30,
				patternAreaW - 20, ctx.guiHeight() - 60);
		drawInfoPanel(gfx, ctx, recipe, infoX, ctx.guiTop() + 30, infoW);

		gfx.pose().popPose();
	}

	// ────────────────────────────────────────────────────────────
	//  Pattern grid
	// ────────────────────────────────────────────────────────────

	/**
	 * Draws the 8×8 chisel pattern grid for a scar recipe.
	 */
	public static void drawPatternGrid(GuiGraphics gfx, ProgressScreenContext ctx,
									   ScarRecipe recipe,
									   int areaX, int areaY, int areaW, int areaH) {
		byte[][] pattern = recipe.getPattern();
		if (pattern == null) return;

		int gridDim  = Math.min(areaW, areaH);
		int cellSize = gridDim / 8;
		int gridW    = cellSize * 8;
		int gridH    = cellSize * 8;
		int gridX    = areaX + (areaW - gridW) / 2;
		int gridY    = areaY + (areaH - gridH) / 2;

		gfx.fill(gridX - 2, gridY - 2, gridX + gridW + 2, gridY + gridH + 2, 0xFF0A0404);
		gfx.fill(gridX - 1, gridY - 1, gridX + gridW + 1, gridY + gridH + 1,
				TAB_COLOR & 0x33FFFFFF);

		int filledColor  = TAB_COLOR;
		int emptyBg      = 0xFF120808;
		int emptyBorder  = 0xFF221111;

		for (int row = 0; row < 8 && row < pattern.length; row++) {
			for (int col = 0; col < 8 && col < pattern[row].length; col++) {
				int cx = gridX + col * cellSize;
				int cy = gridY + row * cellSize;

				if (pattern[row][col] != 0) {
					float time  = System.nanoTime() / 1_000_000_000f;
					float pulse = 0.7f + 0.3f * (float) Math.sin(time * 2.0 + row * 0.5 + col * 0.3);
					int r = (int) (((filledColor >> 16) & 0xFF) * pulse);
					int g = (int) (((filledColor >> 8)  & 0xFF) * pulse);
					int b = (int) ( (filledColor & 0xFF)        * pulse);
					int cellCol = 0xFF000000 | (r << 16) | (g << 8) | b;

					gfx.fill(cx + 1, cy + 1, cx + cellSize - 1, cy + cellSize - 1, cellCol);
					gfx.fill(cx, cy,               cx + cellSize, cy + 1,           filledColor);
					gfx.fill(cx, cy + cellSize - 1, cx + cellSize, cy + cellSize,    filledColor);
					gfx.fill(cx, cy,               cx + 1,        cy + cellSize,     filledColor);
					gfx.fill(cx + cellSize - 1, cy, cx + cellSize, cy + cellSize,    filledColor);
				} else {
					gfx.fill(cx + 1, cy + 1, cx + cellSize - 1, cy + cellSize - 1, emptyBg);
					gfx.fill(cx, cy,               cx + cellSize, cy + 1,           emptyBorder);
					gfx.fill(cx, cy + cellSize - 1, cx + cellSize, cy + cellSize,    emptyBorder);
					gfx.fill(cx, cy,               cx + 1,        cy + cellSize,     emptyBorder);
					gfx.fill(cx + cellSize - 1, cy, cx + cellSize, cy + cellSize,    emptyBorder);
				}
			}
		}

		gfx.drawCenteredString(ctx.font(), "Scar Pattern (8\u00d78)",
				gridX + gridW / 2, gridY + gridH + 6, 0xFF888888);
	}

	// ────────────────────────────────────────────────────────────
	//  Info panel
	// ────────────────────────────────────────────────────────────

	public static void drawInfoPanel(GuiGraphics gfx, ProgressScreenContext ctx,
									 ScarRecipe recipe,
									 int panelX, int panelY, int panelW) {
		int y     = panelY;
		int lineH = 12;

		String namePath = recipe.getId().getPath();
		if (namePath.contains("/")) namePath = namePath.substring(namePath.lastIndexOf('/') + 1);
		String name = HLTextUtils.toProperCase(namePath.replace("_", " "));
		for (String line : ScreenDrawUtils.wrapText(ctx.font(), name, panelW)) {
			gfx.drawString(ctx.font(), Component.literal(line)
					.withStyle(s -> s.withColor(TAB_COLOR).withBold(true)), panelX, y, 0);
			y += lineH;
		}
		y += 4;
		gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF224444);
		y += 6;

		gfx.drawString(ctx.font(), Component.literal("Tier: ")
				.withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(String.valueOf(recipe.getTier()))
						.withStyle(s -> s.withColor(TAB_COLOR))),
				panelX, y, 0);
		y += lineH + 4;

		gfx.drawString(ctx.font(), Component.literal("Type: ")
				.withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(recipe.getScarType().name())
						.withStyle(s -> s.withColor(0xDDDDDD))),
				panelX, y, 0);
		y += lineH + 4;

		gfx.drawString(ctx.font(), Component.literal("Ingredients:")
				.withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
		y += lineH;

		y = drawIngredient(gfx, ctx, recipe.getIngredient1(), panelX, y, panelW, lineH);
		y = drawIngredient(gfx, ctx, recipe.getIngredient2(), panelX, y, panelW, lineH);
		y += 6;

		ItemStack result = recipe.getResultItem();
		if (result != null && !result.isEmpty()) {
			gfx.drawString(ctx.font(), Component.literal("Result:")
					.withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
			y += lineH;
			gfx.renderItem(result, panelX, y);
			gfx.renderItemDecorations(ctx.font(), result, panelX, y);
			List<String> resultLines = ScreenDrawUtils.wrapText(ctx.font(),
					result.getHoverName().getString(), panelW - 20);
			for (int li = 0; li < resultLines.size(); li++) {
				int ix = li == 0 ? panelX + 20 : panelX + 4;
				gfx.drawString(ctx.font(), Component.literal(resultLines.get(li))
						.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
			}
			y += Math.max(20, resultLines.size() * lineH + 4);
		}
		y += 8;

		gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF224444);
		y += 6;
		String loreText = ScarLoreData.getLore(scarRecipeKey(recipe));
		for (String line : ScreenDrawUtils.wrapText(ctx.font(), loreText, panelW)) {
			gfx.drawString(ctx.font(), Component.literal(line)
					.withStyle(s -> s.withColor(0xFF557788).withItalic(true)), panelX, y, 0);
			y += lineH;
		}
	}

	private static int drawIngredient(GuiGraphics gfx, ProgressScreenContext ctx,
									   Ingredient ing, int panelX, int y, int panelW, int lineH) {
		if (ing == null || ing.isEmpty()) return y;
		ItemStack[] items = ing.getItems();
		if (items.length == 0) return y;
		ItemStack display = items[0];
		gfx.renderItem(display, panelX + 2, y);
		gfx.renderItemDecorations(ctx.font(), display, panelX + 2, y);
		List<String> lines = ScreenDrawUtils.wrapText(ctx.font(),
				display.getHoverName().getString(), panelW - 24);
		for (int li = 0; li < lines.size(); li++) {
			int ix = li == 0 ? panelX + 22 : panelX + 4;
			gfx.drawString(ctx.font(), Component.literal(lines.get(li))
					.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
		}
		return y + Math.max(20, lines.size() * lineH + 4);
	}

	// ────────────────────────────────────────────────────────────
	//  Tier sidebar
	// ────────────────────────────────────────────────────────────

	public static void drawTierSidebar(GuiGraphics gfx, ProgressScreenContext ctx,
									   ScarsTabState state, int mouseX, int mouseY) {
		int sx  = ctx.guiLeft() + 4;
		int sy  = ctx.guiTop()  + 24;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;

		gfx.drawString(ctx.font(), Component.literal("Scar Tiers")
				.withStyle(s -> s.withColor(TAB_COLOR).withBold(true)), sx + 2, sy, 0);
		sy += 14;

		gfx.fill(sx, sy, sx + sw, sy + 1, 0xFF224444);
		sy += 4;

		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

		sy -= state.scarSidebarScroll;

		for (int i = 0; i < ScarsTabState.TIER_NAMES.length; i++) {
			String tierName = ScarsTabState.TIER_NAMES[i];
			boolean locked   = ctx.playerDegree() < ScarsTabState.TIER_DEGREE_REQ[i];
			boolean selected = tierName.equals(state.selectedScarTier);
			List<ScarRecipe> recipes =
					state.chiselByTier.getOrDefault(tierName, List.of());

			boolean hovered = mouseX >= sx && mouseX <= sx + sw
					&& mouseY >= sy && mouseY <= sy + rowH
					&& mouseY >= clipTop && mouseY <= clipBottom;

			int bg = selected ? 0xDD0A1818 : (hovered && !locked ? 0xBB081414 : 0x99061010);
			gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

			int bc = locked ? 0xFF333333 : (selected ? TAB_COLOR : 0xFF555555);
			gfx.fill(sx, sy,           sx + sw, sy + 1,     bc);
			gfx.fill(sx, sy + rowH - 1, sx + sw, sy + rowH, bc);
			gfx.fill(sx, sy,           sx + 1,  sy + rowH,  bc);
			gfx.fill(sx + sw - 1, sy, sx + sw,  sy + rowH,  bc);

			if (locked) {
				gfx.fill(sx + 1, sy + 1, sx + sw - 1, sy + rowH - 1, 0xBB000000);
				gfx.drawString(ctx.font(), "[X] " + tierName + " (Locked)",
						sx + 4, sy + (rowH - 8) / 2, 0xFF444444, false);
			} else {
				String label = tierName + " (" + recipes.size() + ")";
				int textCol = selected ? 0xFFAADDEE : 0xFF999999;
				gfx.drawString(ctx.font(), label, sx + 4, sy + (rowH - 8) / 2, textCol, false);
			}

			if (selected && !locked) {
				sy += rowH + 2;
				for (int j = 0; j < recipes.size(); j++) {
					ScarRecipe r = recipes.get(j);
					boolean recSel = (j == state.selectedScarIndexInTier);
					boolean recHov = mouseX >= sx + 4 && mouseX <= sx + sw - 4
							&& mouseY >= sy && mouseY <= sy + 16
							&& mouseY >= clipTop && mouseY <= clipBottom;

					int recBg = recSel ? 0xCC102020 : (recHov ? 0xAA0A1818 : 0x00000000);
					gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);
					if (recSel) gfx.fill(sx + 2, sy, sx + 3, sy + 16, TAB_COLOR);

					String recPath = r.getId().getPath();
					if (recPath.contains("/")) recPath = recPath.substring(recPath.lastIndexOf('/') + 1);
					String recName = HLTextUtils.toProperCase(recPath.replace("_", " "));
					int recCol = recSel ? 0xFFAADDEE : 0xFF888888;
					gfx.drawString(ctx.font(), recName, sx + 8, sy + 4, recCol, false);
					sy += 18;
				}
			}
			sy += rowH + 2;
		}

		gfx.disableScissor();

		int contentH = state.sidebarContentH();
		int visibleH = ctx.tierSidebarVisibleH();
		if (contentH > visibleH) {
			if (state.scarSidebarScroll > 0)
				gfx.drawCenteredString(ctx.font(), "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
			if (state.scarSidebarScroll < contentH - visibleH)
				gfx.drawCenteredString(ctx.font(), "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Hit testing
	// ────────────────────────────────────────────────────────────

	/** Returns the tier name clicked in the scars sidebar, or {@code null}. */
	public static String tierUnder(ProgressScreenContext ctx,
								   ScarsTabState state, double mx, double my) {
		int sx  = ctx.guiLeft() + 4;
		int sy  = ctx.guiTop()  + 24 + 14 + 4;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;
		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		if (my < clipTop || my > clipBottom) return null;
		sy -= state.scarSidebarScroll;

		for (int i = 0; i < ScarsTabState.TIER_NAMES.length; i++) {
			String tierName = ScarsTabState.TIER_NAMES[i];
			boolean selected = tierName.equals(state.selectedScarTier);
			List<ScarRecipe> recipes = state.chiselByTier.getOrDefault(tierName, List.of());
			if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + rowH
					&& my >= clipTop && my <= clipBottom) return tierName;
			if (selected) sy += rowH + 2 + recipes.size() * 18;
			sy += rowH + 2;
		}
		return null;
	}

	/** Returns the recipe index clicked within the selected scar tier, or {@code -1}. */
	public static int recipeUnder(ProgressScreenContext ctx,
								   ScarsTabState state, double mx, double my) {
		if (state.selectedScarTier == null) return -1;
		List<ScarRecipe> recipes = state.chiselByTier.getOrDefault(state.selectedScarTier, List.of());
		if (recipes.isEmpty()) return -1;

		int sx  = ctx.guiLeft() + 4;
		int sy  = ctx.guiTop()  + 24 + 14 + 4;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;
		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		if (my < clipTop || my > clipBottom) return -1;
		sy -= state.scarSidebarScroll;

		for (int i = 0; i < ScarsTabState.TIER_NAMES.length; i++) {
			String tierName = ScarsTabState.TIER_NAMES[i];
			boolean selected = tierName.equals(state.selectedScarTier);
			List<ScarRecipe> tierRecipes = state.chiselByTier.getOrDefault(tierName, List.of());
			sy += rowH + 2;
			if (selected) {
				for (int j = 0; j < tierRecipes.size(); j++) {
					if (mx >= sx + 4 && mx <= sx + sw - 4
							&& my >= sy && my <= sy + 16
							&& my >= clipTop && my <= clipBottom) return j;
					sy += 18;
				}
				return -1;
			}
		}
		return -1;
	}

	// ────────────────────────────────────────────────────────────
	//  Utility
	// ────────────────────────────────────────────────────────────

	/** Returns the trailing path segment of a scar recipe's ResourceLocation. */
	public static String scarRecipeKey(ScarRecipe recipe) {
		String path = recipe.getId().getPath();
		return path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
	}
}
