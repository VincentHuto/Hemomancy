package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Shared renderer for the "Materials &amp; Processes" tab that appears in both
 * {@link HarbingerProgressScreen} (blood faction) and {@link UnstainedProgressScreen}
 * (Unstained faction).
 *
 * <p>Categories are arranged as visually distinct <b>clusters</b> packed into a
 * two-column grid. Each cluster has a tinted background panel, a thin accent
 * border, and a centered header label, giving a more compact and aesthetic feel
 * compared to a simple row-per-category list.
 *
 * <p>Callers supply three things that differ between the two screens:
 * <ul>
 *   <li>The list of {@link MaterialEntry} objects to show</li>
 *   <li>The node shape to use for the icons</li>
 *   <li>The accent color and recipe-renderer theme</li>
 * </ul>
 */
public final class MaterialsTabView {

	// ── Cluster-grid layout constants (content-space) ───────────

	/** Max item columns inside a single cluster. */
	private static final int CLUSTER_ITEM_COLS = 4;
	/** Horizontal distance between node centres within a cluster. */
	private static final int NODE_GAP_X = 56;
	/** Vertical distance between node centres within a cluster. */
	private static final int NODE_GAP_Y = 54;
	/** Number of cluster columns in the overall layout. */
	private static final int GRID_COLS = 2;
	/** Horizontal gap between cluster columns. */
	private static final int CLUSTER_SPACING_X = 28;
	/** Vertical gap between stacked clusters. */
	private static final int CLUSTER_SPACING_Y = 22;
	/** Content-space padding around outermost node centres inside a cluster. */
	private static final int CLUSTER_PAD = 28;
	/** Extra vertical space above the first node row for the cluster header. */
	private static final int CLUSTER_HEADER_H = 20;
	/** Left / top margin for the entire layout. */
	private static final int ORIGIN = 20;

	// Node rendering colours (common across both screens)
	private static final int COL_NODE_BG = 0xCC0C0808;
	private static final int INFO_PANEL_SHADOW = 0xAA000000;

	private MaterialsTabView() {}

	// ── Layout ──────────────────────────────────────────────────

	/**
	 * Computes content-space node positions arranged in a two-column cluster
	 * grid and fills {@code positionsOut}, also writing the total content
	 * width/height into {@code boundsOut[0..1]}.
	 *
	 * @param entries      entries to lay out
	 * @param positionsOut map to populate (entry → int[]{x, y})
	 * @param boundsOut    int[2] — boundsOut[0] = contentW, boundsOut[1] = contentH
	 * @param nodeSize     NODE_SIZE from the parent screen
	 */
	public static void buildLayout(List<MaterialEntry> entries,
								   Map<MaterialEntry, int[]> positionsOut,
								   int[] boundsOut, int nodeSize) {
		positionsOut.clear();
		boundsOut[0] = 0;
		boundsOut[1] = 0;
		if (entries.isEmpty()) return;

		LinkedHashMap<String, List<MaterialEntry>> byCategory = groupByCategory(entries);
		List<String> cats = new ArrayList<>(byCategory.keySet());

		// Standard cluster inner width (based on max columns, for consistency)
		int clusterInnerW = (CLUSTER_ITEM_COLS - 1) * NODE_GAP_X;
		int fullClusterW  = clusterInnerW + 2 * CLUSTER_PAD;

		// Compute per-cluster column counts and heights
		int[] colCounts   = new int[cats.size()];
		int[] clusterHeights = new int[cats.size()];
		for (int ci = 0; ci < cats.size(); ci++) {
			int count = byCategory.get(cats.get(ci)).size();
			int cols  = Math.min(count, CLUSTER_ITEM_COLS);
			int rows  = (count + cols - 1) / cols;
			colCounts[ci]      = cols;
			clusterHeights[ci] = CLUSTER_HEADER_H + 2 * CLUSTER_PAD
					+ (rows > 1 ? (rows - 1) * NODE_GAP_Y : 0);
		}

		// Two-column greedy packing: always place the next cluster in the
		// shortest column to keep the layout balanced.
		int[] colHeights = new int[GRID_COLS];
		int[] colX       = new int[GRID_COLS];
		for (int i = 0; i < GRID_COLS; i++) {
			colHeights[i] = ORIGIN;
			colX[i]       = ORIGIN + i * (fullClusterW + CLUSTER_SPACING_X);
		}

		for (int ci = 0; ci < cats.size(); ci++) {
			String cat = cats.get(ci);
			List<MaterialEntry> items = byCategory.get(cat);
			int cols = colCounts[ci];

			// Pick the shortest column
			int shortest = 0;
			for (int i = 1; i < GRID_COLS; i++) {
				if (colHeights[i] < colHeights[shortest]) shortest = i;
			}

			int clusterLeft = colX[shortest];
			int clusterTop  = colHeights[shortest];

			// Center items horizontally inside the cluster
			int usedW   = cols > 1 ? (cols - 1) * NODE_GAP_X : 0;
			int offsetX = (clusterInnerW - usedW) / 2;

			for (int i = 0; i < items.size(); i++) {
				int col = i % cols;
				int row = i / cols;
				int x = clusterLeft + CLUSTER_PAD + offsetX + col * NODE_GAP_X;
				int y = clusterTop  + CLUSTER_HEADER_H + CLUSTER_PAD + row * NODE_GAP_Y;
				positionsOut.put(items.get(i), new int[]{x, y});
			}

			colHeights[shortest] = clusterTop + clusterHeights[ci] + CLUSTER_SPACING_Y;
		}

		// Final content bounds (for centreOn / clamp)
		for (int i = 0; i < GRID_COLS; i++) {
			boundsOut[0] = Math.max(boundsOut[0], colX[i] + fullClusterW + ORIGIN);
			boundsOut[1] = Math.max(boundsOut[1], colHeights[i] + ORIGIN);
		}
	}

	// ── Drawing ──────────────────────────────────────────────────

	/**
	 * Draws cluster backgrounds, category headers, and material nodes for one
	 * frame.
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
		 float animTime = 0f;

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		int hn = state.halfNode(nodeSize);

		// ── Cluster backgrounds & headers ───────────────────────
		LinkedHashMap<String, List<MaterialEntry>> byCategory = groupByCategory(entries);

		int bgColor     = (0x18 << 24) | (accentColor & 0x00FFFFFF);
		int borderColor = (0x40 << 24) | (accentColor & 0x00FFFFFF);

		// Half the full cluster width (content-space) — used to ensure even
		// small clusters get the same visual width as large ones.
		int fullHalfW = (CLUSTER_ITEM_COLS - 1) * NODE_GAP_X / 2 + CLUSTER_PAD;

		for (var catEntry : byCategory.entrySet()) {
			String catName = catEntry.getKey();
			List<MaterialEntry> catItems = catEntry.getValue();

			// Derive cluster bounding box from node positions
			int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
			for (MaterialEntry item : catItems) {
				int[] pos = positions.get(item);
				if (pos == null) continue;
				minX = Math.min(minX, pos[0]);
				minY = Math.min(minY, pos[1]);
				maxX = Math.max(maxX, pos[0]);
				maxY = Math.max(maxY, pos[1]);
			}
			if (minX == Integer.MAX_VALUE) continue;

			// Use the centre of node positions + full cluster half-width so
			// small clusters (fewer than CLUSTER_ITEM_COLS items) still get
			// the same background width as fully populated ones.
			int centerX = (minX + maxX) / 2;
			int sx1 = state.sx(guiLeft, centerX - fullHalfW);
			int sy1 = state.sy(guiTop,  minY - CLUSTER_PAD - CLUSTER_HEADER_H);
			int sx2 = state.sx(guiLeft, centerX + fullHalfW);
			int sy2 = state.sy(guiTop,  maxY + CLUSTER_PAD);

			// Background panel
			gfx.fill(sx1, sy1, sx2, sy2, bgColor);

			// Border
			ScreenDrawUtils.drawSimpleBorder(gfx, sx1, sy1, sx2 - sx1, sy2 - sy1, borderColor);

			// Category header (hidden at very low zoom where text would be illegible)
			if (state.zoom >= 0.4f) {
				int headerX = (sx1 + sx2) / 2;
				int headerY = sy1 + Math.max(2, (int)(5 * state.zoom));
				gfx.drawCenteredString(font,
						Component.literal(catName)
								.withStyle(s -> s.withColor(accentColor).withBold(true)),
						headerX, headerY, 0);
			}
		}

		// ── Nodes ───────────────────────────────────────────────
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

		int solidBg = 0xFF000000 | (bgColor & 0x00FFFFFF);

		var pose = gfx.pose();
		pose.pushPose();
		pose.translate(0.0F, 0.0F, 400.0F);
		// Background. This must be opaque and above the pan/zoom material canvas
		// because item renders and labels can carry their own GUI depth.
		gfx.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, INFO_PANEL_SHADOW);
		gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, solidBg);

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
		pose.popPose();
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
