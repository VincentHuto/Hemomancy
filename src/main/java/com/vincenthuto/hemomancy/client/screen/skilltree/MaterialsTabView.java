package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Shared renderer for the "Materials &amp; Processes" tab that appears in both
 * {@link SkillTreeScreen} (blood faction) and {@link UnstainedProgressScreen}
 * (Unstained faction).
 *
 * <p>Callers supply three things that differ between the two screens:
 * <ul>
 *   <li>The list of {@link MaterialEntry} objects to show</li>
 *   <li>The node shape to use for the icons</li>
 *   <li>The accent color and recipe-renderer theme</li>
 * </ul>
 */
public final class MaterialsTabView {

	// Layout constants
	public static final int MAT_GAP_X = 70;
	public static final int MAT_GAP_Y = 60;
	public static final int MAT_COLS  = 5;

	// Node rendering colours (common across both screens)
	private static final int COL_NODE_BG = 0xCC0C0808;

	private MaterialsTabView() {}

	// ── Layout ──────────────────────────────────────────────────

	/**
	 * Computes content-space node positions and fills {@code positionsOut},
	 * also writing the total content width/height into {@code boundsOut[0..1]}.
	 *
	 * @param entries     entries to lay out
	 * @param positionsOut map to populate (entry → int[]{x, y})
	 * @param boundsOut   int[2] — boundsOut[0] = contentW, boundsOut[1] = contentH
	 * @param nodeSize    NODE_SIZE from the parent screen
	 */
	public static void buildLayout(List<MaterialEntry> entries,
								   Map<MaterialEntry, int[]> positionsOut,
								   int[] boundsOut, int nodeSize) {
		positionsOut.clear();
		boundsOut[0] = 0;
		boundsOut[1] = 0;
		if (entries.isEmpty()) return;

		LinkedHashMap<String, List<MaterialEntry>> byCategory = groupByCategory(entries);

		int y = 30;
		for (var catEntry : byCategory.entrySet()) {
			y += 20;
			List<MaterialEntry> catItems = catEntry.getValue();
			for (int i = 0; i < catItems.size(); i++) {
				int col = i % MAT_COLS;
				int row = i / MAT_COLS;
				int x = 20 + col * MAT_GAP_X;
				int ny = y + row * MAT_GAP_Y;
				positionsOut.put(catItems.get(i), new int[]{x, ny});
				boundsOut[0] = Math.max(boundsOut[0], x + nodeSize + 20);
				boundsOut[1] = Math.max(boundsOut[1], ny + nodeSize + 24);
			}
			int rows = (catItems.size() + MAT_COLS - 1) / MAT_COLS;
			y += rows * MAT_GAP_Y + 10;
		}
	}

	// ── Drawing ──────────────────────────────────────────────────

	/**
	 * Draws all category headers and material nodes for one frame.
	 *
	 * @param gfx           graphics context
	 * @param font          font renderer
	 * @param entries       ordered list of entries to display
	 * @param positions     map from entry → content-space int[]{x, y}
	 * @param state         current pan/zoom state (used for sx/sy/halfNode)
	 * @param guiLeft       screen X of the GUI left edge
	 * @param guiTop        screen Y of the GUI top edge
	 * @param nodeSize      NODE_SIZE from the parent screen
	 * @param nodeShape     which shape to use for node icons
	 * @param accentColor   category header color
	 * @param selectedEntry currently selected entry (maybe null)
	 * @param selectedGlow  ARGB glow color for the selected node (low-alpha)
	 * @param defaultBorder default node border color
	 */
	public static void drawNodes(GuiGraphics gfx, Font font,
								 List<MaterialEntry> entries,
								 Map<MaterialEntry, int[]> positions,
								 PanZoomState state, int guiLeft, int guiTop,
								 int nodeSize, EnumNodeShape nodeShape,
								 int accentColor,
								 MaterialEntry selectedEntry, int selectedGlow,
								 int defaultBorder) {
		float time = System.nanoTime() / 1_000_000_000f;
		int hn = state.halfNode(nodeSize);

		// Category headers
		LinkedHashMap<String, List<MaterialEntry>> byCategory = groupByCategory(entries);
		int catY = 30;
		for (var catEntry : byCategory.entrySet()) {
			int scrY = state.sy(guiTop, catY);
			int scrX = state.sx(guiLeft, 20);
			gfx.drawString(font,
					Component.literal(catEntry.getKey())
							.withStyle(s -> s.withColor(accentColor).withBold(true)),
					scrX, scrY, 0, false);
			catY += 20;
			int rows = (catEntry.getValue().size() + MAT_COLS - 1) / MAT_COLS;
			catY += rows * MAT_GAP_Y + 10;
		}

		// Nodes
		for (var e : positions.entrySet()) {
			MaterialEntry mat = e.getKey();
			int[] pos = e.getValue();
			int nx = state.sx(guiLeft, pos[0]);
			int ny = state.sy(guiTop, pos[1]);
			boolean isSelected = mat == selectedEntry;

			// Pulsing selection glow
			if (isSelected) {
				float pulse = 0.5f + 0.5f * Mth.sin(time * 2.5f);
				int ga = (int)(45 * pulse);
				int glowColor = (ga << 24) | (selectedGlow & 0x00FFFFFF);
				NodeShapeRenderer.drawFill(gfx, nodeShape, nx, ny, hn + 4, glowColor);
			}

			NodeShapeRenderer.drawFill(gfx, nodeShape, nx, ny, hn, COL_NODE_BG);
			int border = isSelected ? accentColor : defaultBorder;
			NodeShapeRenderer.drawOutline(gfx, nodeShape, nx, ny, hn, border);

			if (state.zoom >= 0.5f) {
				ItemStack stack = mat.iconStack().get();
				if (stack != null && !stack.isEmpty()) {
					ScreenDrawUtils.renderScaledItem(gfx, stack, nx, ny, hn);
				}
			}
		}
	}

	/**
	 * Draws the detail panel for the selected material entry.
	 *
	 * @param gfx           graphics context
	 * @param font          font renderer
	 * @param mat           selected material
	 * @param guiLeft       screen X of the GUI left edge
	 * @param guiTop        screen Y of the GUI top edge
	 * @param guiWidth      GUI width in screen pixels
	 * @param accentColor   panel border and name color
	 * @param dividerColor  horizontal divider line color
	 * @param bgColor       panel background ARGB
	 * @param recipeTheme   MiniRecipeRenderer theme to use
	 */
	public static void drawInfoPanel(GuiGraphics gfx, Font font, MaterialEntry mat,
									 int guiLeft, int guiTop, int guiWidth,
									 int accentColor, int dividerColor, int bgColor,
									 MiniRecipeRenderer.Theme recipeTheme) {
		int panelW = 160;
		int panelX = guiLeft + guiWidth - panelW - 8;
		int panelY = guiTop + 30;
		int maxW = panelW - 16;

		List<String> descLines = ScreenDrawUtils.wrapText(font, mat.description(), maxW);
		// Name sits next to the 16px icon, so available width is maxW - 20
		List<String> nameLines = ScreenDrawUtils.wrapText(font, mat.displayName(), maxW - 20);
		int nameRowH = Math.max(22, nameLines.size() * 10 + 4);

		RecipeLookup.FoundRecipe foundRecipe = mat.hasRecipe()
				? RecipeLookup.find(mat.iconStack().get()) : null;
		int recipeSection = MiniRecipeRenderer.estimateHeight(foundRecipe) > 0
				? MiniRecipeRenderer.estimateHeight(foundRecipe) + 8 : 0;

		int panelH = 6 + nameRowH + 12 + 1 + 5 + descLines.size() * 10 + recipeSection + 8;

		// Background
		gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, bgColor);

		// Border
		ScreenDrawUtils.drawSimpleBorder(gfx, panelX, panelY, panelW, panelH, accentColor);

		int tx = panelX + 6;
		int ty = panelY + 6;

		// Item icon + name row (name wraps if too long)
		ItemStack stack = mat.iconStack().get();
		if (stack != null && !stack.isEmpty()) {
			gfx.renderItem(stack, tx, ty);
		}
		for (int li = 0; li < nameLines.size(); li++) {
			int nx = li == 0 ? tx + 20 : tx + 4;
			gfx.drawString(font,
					Component.literal(nameLines.get(li))
							.withStyle(s -> s.withColor(accentColor).withBold(true)),
					nx, ty + 4 + li * 10, 0, false);
		}
		ty += nameRowH;

		// Category
		gfx.drawString(font,
				Component.literal(mat.category())
						.withStyle(s -> s.withColor(0xFF888888).withItalic(true)),
				tx, ty, 0, false);
		ty += 12;

		// Divider
		gfx.fill(tx, ty, panelX + panelW - 6, ty + 1, dividerColor);
		ty += 5;

		// Description
		for (String line : descLines) {
			gfx.drawString(font, line, tx, ty, 0xFF999999, false);
			ty += 10;
		}

		// Recipe preview
		if (foundRecipe != null) {
			ty += 3;
			gfx.fill(tx, ty, panelX + panelW - 6, ty + 1, dividerColor);
			ty += 4;
			MiniRecipeRenderer.draw(gfx, font, foundRecipe, tx, ty, maxW, accentColor, recipeTheme);
		}
	}

	/**
	 * Draws hover tooltips for material nodes under the mouse.
	 *
	 * @param gfx           graphics context
	 * @param font          font renderer
	 * @param positions     map from entry → content-space int[]{x, y}
	 * @param state         current pan/zoom state
	 * @param guiLeft       screen X of the GUI left edge
	 * @param guiTop        screen Y of the GUI top edge
	 * @param guiW          GUI width (for bounds check)
	 * @param guiH          GUI height (for bounds check)
	 * @param nodeSize      NODE_SIZE from the parent screen
	 * @param nodeShape     which shape to test hit detection against
	 * @param accentColor   tooltip name highlight color
	 * @param recipePrompt  prompt color for "click to view" line
	 * @param mouseX        mouse X in screen space
	 * @param mouseY        mouse Y in screen space
	 */
	public static void drawTooltip(GuiGraphics gfx, Font font,
								   Map<MaterialEntry, int[]> positions,
								   PanZoomState state, int guiLeft, int guiTop,
								   int guiW, int guiH, int nodeSize,
								   EnumNodeShape nodeShape, int accentColor,
								   int recipePrompt, int mouseX, int mouseY) {
		boolean inside = mouseX >= guiLeft && mouseX < guiLeft + guiW
				&& mouseY >= guiTop && mouseY < guiTop + guiH;
		if (!inside) return;

		int hn = state.halfNode(nodeSize);
		for (var e : positions.entrySet()) {
			MaterialEntry mat = e.getKey();
			int[] pos = e.getValue();
			int nx = state.sx(guiLeft, pos[0]);
			int ny = state.sy(guiTop, pos[1]);
			if (!NodeShapeRenderer.isInside(nodeShape, mouseX, mouseY, nx, ny, hn)) continue;

			var tip = new java.util.ArrayList<Component>();
			tip.add(Component.literal(mat.displayName())
					.withStyle(s -> s.withColor(accentColor).withBold(true)));
			tip.add(Component.literal(mat.category())
					.withStyle(s -> s.withColor(0xFF888888).withItalic(true)));
			tip.add(Component.literal(mat.description())
					.withStyle(s -> s.withColor(0xFF999999)));
			if (mat.hasRecipe()) {
				tip.add(Component.literal("Click to view details")
						.withStyle(s -> s.withColor(recipePrompt)));
			}

			gfx.renderTooltip(font, tip, Optional.empty(), mouseX, mouseY);
			break;
		}
	}

	/**
	 * Returns the {@link MaterialEntry} under the given screen-space mouse position,
	 * or {@code null} if none.
	 */
	public static MaterialEntry nodeUnder(Map<MaterialEntry, int[]> positions,
										  PanZoomState state, int guiLeft, int guiTop,
										  int nodeSize, EnumNodeShape nodeShape,
										  double mx, double my) {
		int hn = state.halfNode(nodeSize);
		for (var e : positions.entrySet()) {
			int[] p = e.getValue();
			int nx = state.sx(guiLeft, p[0]);
			int ny = state.sy(guiTop, p[1]);
			if (NodeShapeRenderer.isInside(nodeShape, mx, my, nx, ny, hn)) return e.getKey();
		}
		return null;
	}

	// ── Private helpers ──────────────────────────────────────────

	private static LinkedHashMap<String, List<MaterialEntry>> groupByCategory(List<MaterialEntry> entries) {
		LinkedHashMap<String, List<MaterialEntry>> map = new LinkedHashMap<>();
		for (MaterialEntry e : entries) {
			map.computeIfAbsent(e.category(), k -> new java.util.ArrayList<>()).add(e);
		}
		return map;
	}
}
