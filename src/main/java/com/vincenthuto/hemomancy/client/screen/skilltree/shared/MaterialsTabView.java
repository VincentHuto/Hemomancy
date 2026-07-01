package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Shared renderer for the panning material atlas used by
 * {@link HarbingerProgressScreen} and {@link UnstainedProgressScreen}.
 */
public final class MaterialsTabView {
	private static final int NODE_GAP_X = 56;
	private static final int NODE_GAP_Y = 54;
	private static final int CONTENT_PAD = 80;
	private static final int HUB_HALF = 22;
	private static final int COL_NODE_BG = 0xCC0C0808;
	private static final int COL_PREVIEW_BG = 0xDD050303;
	private static final int INFO_PANEL_SHADOW = 0xAA000000;
	private static final float INFO_PANEL_Z = 400.0F;
	private static final float MATERIAL_TOOLTIP_Z = INFO_PANEL_Z + 500.0F;

	private MaterialsTabView() {
	}

	public static void buildLayout(List<MaterialAtlasNode> nodes,
			Map<MaterialAtlasNode, int[]> positionsOut,
			int[] boundsOut,
			int nodeSize,
			MaterialAtlasPath path) {
		positionsOut.clear();
		boundsOut[0] = 0;
		boundsOut[1] = 0;
		if (nodes.isEmpty()) {
			return;
		}

		LinkedHashMap<String, List<MaterialAtlasNode>> byBucket = groupByBucket(path, nodes);
		int hubX = MaterialAtlasSpec.hubX(path);
		int hubY = MaterialAtlasSpec.hubY(path);

		int minX = hubX - CONTENT_PAD;
		int minY = hubY - CONTENT_PAD;
		int maxX = hubX + CONTENT_PAD;
		int maxY = hubY + CONTENT_PAD;

		for (MaterialAtlasBucket bucket : MaterialAtlasSpec.buckets(path)) {
			List<MaterialAtlasNode> bucketNodes = byBucket.get(bucket.id());
			if (bucketNodes == null || bucketNodes.isEmpty()) {
				continue;
			}
			bucketNodes.sort(Comparator.comparingInt(n -> n.atlasEntry().order()));
			MaterialAtlasNode rootNode = rootNode(bucket, bucketNodes);
			List<MaterialAtlasNode> branchNodes = new ArrayList<>(bucketNodes);
			if (rootNode != null) {
				branchNodes.remove(rootNode);
				positionsOut.put(rootNode, new int[] {bucket.centerX(), bucket.centerY()});
				minX = Math.min(minX, bucket.centerX() - nodeSize - CONTENT_PAD / 2);
				minY = Math.min(minY, bucket.centerY() - nodeSize - CONTENT_PAD / 2);
				maxX = Math.max(maxX, bucket.centerX() + nodeSize + CONTENT_PAD / 2);
				maxY = Math.max(maxY, bucket.centerY() + nodeSize + CONTENT_PAD / 2);
			}

			double angle = Math.atan2(bucket.centerY() - hubY, bucket.centerX() - hubX);
			double ux = Math.cos(angle);
			double uy = Math.sin(angle);
			double tx = -uy;
			double ty = ux;
			int cols = Math.max(1, Math.min(5, (int) Math.ceil(Math.sqrt(branchNodes.size() * 1.25))));

			for (int i = 0; i < branchNodes.size(); i++) {
				MaterialAtlasNode node = branchNodes.get(i);
				int x;
				int y;
				if (node.atlasEntry().hasExplicitPosition()) {
					x = node.atlasEntry().nodeX();
					y = node.atlasEntry().nodeY();
				} else {
					int row = i / cols;
					int col = i % cols;
					int rowCount = Math.min(cols, branchNodes.size() - row * cols);
					double lateral = (col - (rowCount - 1) * 0.5D) * NODE_GAP_X + jitter(node.entry().name(), 9);
					double radial = NODE_GAP_Y + row * NODE_GAP_Y + ((i & 1) == 0 ? 0 : 11);
					x = (int) Math.round(bucket.centerX() + tx * lateral + ux * radial);
					y = (int) Math.round(bucket.centerY() + ty * lateral + uy * radial);
				}
				positionsOut.put(node, new int[] {x, y});
				minX = Math.min(minX, x - nodeSize - CONTENT_PAD / 2);
				minY = Math.min(minY, y - nodeSize - CONTENT_PAD / 2);
				maxX = Math.max(maxX, x + nodeSize + CONTENT_PAD / 2);
				maxY = Math.max(maxY, y + nodeSize + CONTENT_PAD / 2);
			}

			minX = Math.min(minX, Math.min(bucket.centerX(), bucket.plaqueX()) - CONTENT_PAD);
			minY = Math.min(minY, Math.min(bucket.centerY(), bucket.plaqueY()) - CONTENT_PAD);
			maxX = Math.max(maxX, Math.max(bucket.centerX(), bucket.plaqueX()) + CONTENT_PAD);
			maxY = Math.max(maxY, Math.max(bucket.centerY(), bucket.plaqueY()) + CONTENT_PAD);
		}

		if (minX < 0 || minY < 0) {
			int offX = minX < 0 ? -minX : 0;
			int offY = minY < 0 ? -minY : 0;
			for (int[] pos : positionsOut.values()) {
				pos[0] += offX;
				pos[1] += offY;
			}
			maxX += offX;
			maxY += offY;
		}
		boundsOut[0] = Math.max(1, maxX + CONTENT_PAD);
		boundsOut[1] = Math.max(1, maxY + CONTENT_PAD);
	}

	public static void drawAtlasTrace(MaterialAtlasTraceLayerCache traceCache,
			GuiGraphics gfx,
			ProgressScreenContext ctx,
			PanZoomState state) {
		traceCache.render(gfx, ctx, state);
	}

	public static void drawNodes(GuiGraphics gfx, Font font,
			MaterialAtlasPath path,
			List<MaterialAtlasNode> nodes,
			Map<MaterialAtlasNode, int[]> positions,
			PanZoomState state,
			int guiLeft,
			int guiTop,
			int nodeSize,
			EnumNodeShape nodeShape,
			int accentColor,
			MaterialAtlasNode selectedNode,
			int selectedGlow,
			int defaultBorder) {
		float time = (System.currentTimeMillis() % 100000L) / 1000.0F;
		int hn = state.halfNode(nodeSize);
		drawAtlasHub(gfx, font, path, state, guiLeft, guiTop);
		drawBucketPlaques(gfx, font, path, nodes, state, guiLeft, guiTop);

		for (var e : positions.entrySet()) {
			MaterialAtlasNode node = e.getKey();
			int[] pos = e.getValue();
			int nx = state.sx(guiLeft, pos[0]);
			int ny = state.sy(guiTop, pos[1]);
			MaterialAtlasBucket bucket = node.atlasEntry().bucket();
			boolean selected = node == selectedNode && node.visibility() == MaterialVisibility.UNLOCKED;
			int bucketColor = bucket.color();

			if (node.visibility() == MaterialVisibility.NEXT_PREVIEW) {
				drawVeiledNode(gfx, font, nodeShape, nx, ny, hn, bucketColor, state.zoom);
				continue;
			}

			if (selected) {
				float pulse = 0.5F + 0.5F * Mth.sin(time * 2.6F);
				int ga = (int) (46 * pulse);
				NodeShapeRenderer.drawFill(gfx, nodeShape, nx, ny, hn + 4,
						(ga << 24) | (selectedGlow & 0x00FFFFFF));
			}

			NodeShapeRenderer.drawFill(gfx, nodeShape, nx, ny, hn, COL_NODE_BG);
			NodeShapeRenderer.drawOutline(gfx, nodeShape, nx, ny, hn, selected ? accentColor : bucketColor);
			if (state.zoom >= 0.5F) {
				ItemStack stack = node.entry().iconStack().get();
				if (stack != null && !stack.isEmpty()) {
					ScreenDrawUtils.renderScaledItem(gfx, stack, nx, ny, hn);
				}
			}
		}
	}

	private static void drawAtlasHub(GuiGraphics gfx, Font font, MaterialAtlasPath path,
			PanZoomState state, int guiLeft, int guiTop) {
		int cx = state.sx(guiLeft, MaterialAtlasSpec.hubX(path));
		int cy = state.sy(guiTop, MaterialAtlasSpec.hubY(path));
		int half = Math.max(8, Math.round(HUB_HALF * state.zoom));
		int bg = (0xCC << 24) | (path.accentColor() & 0x001A1A1A);
		gfx.fill(cx - half, cy - half, cx + half, cy + half, bg);
		ScreenDrawUtils.drawSimpleBorder(gfx, cx - half, cy - half, half * 2, half * 2, path.accentColor());
		if (state.zoom >= 0.48F) {
			gfx.drawCenteredString(font, Component.literal(path.hubLabel())
					.withStyle(s -> s.withColor(path.accentColor()).withBold(true)),
					cx, cy - 4, 0);
		}
	}

	private static void drawBucketPlaques(GuiGraphics gfx, Font font, MaterialAtlasPath path,
			List<MaterialAtlasNode> nodes, PanZoomState state, int guiLeft, int guiTop) {
		if (state.zoom < 0.42F) {
			return;
		}
		Set<String> visibleBuckets = new HashSet<>();
		for (MaterialAtlasNode node : nodes) {
			visibleBuckets.add(node.atlasEntry().bucket().id());
		}
		for (MaterialAtlasBucket bucket : MaterialAtlasSpec.buckets(path)) {
			if (!visibleBuckets.contains(bucket.id())) {
				continue;
			}
			int x = state.sx(guiLeft, bucket.plaqueX());
			int y = state.sy(guiTop, bucket.plaqueY());
			int color = bucket.color();
			if (state.zoom >= 0.56F) {
				int textW = font.width(bucket.label());
				int textX = x - textW / 2;
				int textY = y - 4;
				int lineColor = (0xAA << 24) | (color & 0x00FFFFFF);
				gfx.drawString(font, bucket.label(), textX + 1, textY + 1, 0x99000000, false);
				gfx.drawString(font, bucket.label(), textX, textY, color, false);
				gfx.fill(textX, textY + 10, textX + textW, textY + 11, lineColor);
			}
		}
	}

	private static void drawVeiledNode(GuiGraphics gfx, Font font, EnumNodeShape nodeShape,
			int nx, int ny, int hn, int bucketColor, float zoom) {
		NodeShapeRenderer.drawFill(gfx, nodeShape, nx, ny, hn, COL_PREVIEW_BG);
		NodeShapeRenderer.drawOutline(gfx, nodeShape, nx, ny, hn,
				(0xDD << 24) | (bucketColor & 0x00FFFFFF));
		NodeShapeRenderer.drawFill(gfx, nodeShape, nx, ny, Math.max(2, hn - 4), 0x77000000);
		if (zoom >= 0.5F) {
			gfx.drawCenteredString(font, "?", nx, ny - 4, 0xFF664444);
		}
	}

	public static void drawInfoPanel(GuiGraphics gfx, Font font, MaterialEntry mat,
			int guiLeft, int guiTop, int guiWidth,
			int accentColor, int dividerColor, int bgColor,
			MiniRecipeRenderer.Theme recipeTheme) {
		int panelW = 160;
		int panelX = guiLeft + guiWidth - panelW - 8;
		int panelY = guiTop + 30;
		int maxW = panelW - 16;

		List<String> descLines = ScreenDrawUtils.wrapText(font, mat.description(), maxW);
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
		pose.translate(0.0F, 0.0F, INFO_PANEL_Z);
		gfx.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, INFO_PANEL_SHADOW);
		gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, solidBg);
		ScreenDrawUtils.drawSimpleBorder(gfx, panelX, panelY, panelW, panelH, accentColor);

		int tx = panelX + 6;
		int ty = panelY + 6;
		ItemStack stack = mat.iconStack().get();
		if (stack != null && !stack.isEmpty()) {
			gfx.renderItem(stack, tx, ty);
		}
		for (int li = 0; li < nameLines.size(); li++) {
			int nx = li == 0 ? tx + 20 : tx + 4;
			gfx.drawString(font, Component.literal(nameLines.get(li))
							.withStyle(s -> s.withColor(accentColor).withBold(true)),
					nx, ty + 4 + li * 10, 0, false);
		}
		ty += nameRowH;
		gfx.drawString(font, Component.literal(mat.category())
						.withStyle(s -> s.withColor(0xFF888888).withItalic(true)),
				tx, ty, 0, false);
		ty += 12;
		gfx.fill(tx, ty, panelX + panelW - 6, ty + 1, dividerColor);
		ty += 5;
		for (String line : descLines) {
			gfx.drawString(font, line, tx, ty, 0xFF999999, false);
			ty += 10;
		}
		if (foundRecipe != null) {
			ty += 3;
			gfx.fill(tx, ty, panelX + panelW - 6, ty + 1, dividerColor);
			ty += 4;
			MiniRecipeRenderer.draw(gfx, font, foundRecipe, tx, ty, maxW, accentColor, recipeTheme);
		}
		pose.popPose();
	}

	public static void drawTooltip(GuiGraphics gfx, Font font,
			Map<MaterialAtlasNode, int[]> positions,
			PanZoomState state, int guiLeft, int guiTop,
			int guiW, int guiH, int nodeSize,
			EnumNodeShape nodeShape, int accentColor,
			int recipePrompt, int mouseX, int mouseY) {
		boolean inside = mouseX >= guiLeft && mouseX < guiLeft + guiW
				&& mouseY >= guiTop && mouseY < guiTop + guiH;
		if (!inside) {
			return;
		}
		int hn = state.halfNode(nodeSize);
		for (var e : positions.entrySet()) {
			MaterialAtlasNode node = e.getKey();
			int[] pos = e.getValue();
			int nx = state.sx(guiLeft, pos[0]);
			int ny = state.sy(guiTop, pos[1]);
			if (!NodeShapeRenderer.isInside(nodeShape, mouseX, mouseY, nx, ny, hn)) {
				continue;
			}

			var tip = new ArrayList<Component>();
			if (node.visibility() == MaterialVisibility.NEXT_PREVIEW) {
				tip.add(Component.literal("Unknown material")
						.withStyle(s -> s.withColor(node.atlasEntry().bucket().color()).withBold(true)));
				tip.add(Component.literal(node.atlasEntry().gate().requirementText())
						.withStyle(s -> s.withColor(0xFFAA6666)));
			} else {
				MaterialEntry mat = node.entry();
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
			}

			var pose = gfx.pose();
			pose.pushPose();
			pose.translate(0.0F, 0.0F, MATERIAL_TOOLTIP_Z);
			gfx.renderTooltip(font, tip, Optional.empty(), mouseX, mouseY);
			pose.popPose();
			break;
		}
	}

	public static MaterialAtlasNode nodeUnder(Map<MaterialAtlasNode, int[]> positions,
			PanZoomState state, int guiLeft, int guiTop,
			int nodeSize, EnumNodeShape nodeShape,
			double mx, double my) {
		int hn = state.halfNode(nodeSize);
		for (var e : positions.entrySet()) {
			int[] p = e.getValue();
			int nx = state.sx(guiLeft, p[0]);
			int ny = state.sy(guiTop, p[1]);
			if (NodeShapeRenderer.isInside(nodeShape, mx, my, nx, ny, hn)) {
				return e.getKey();
			}
		}
		return null;
	}

	private static MaterialAtlasNode rootNode(MaterialAtlasBucket bucket, List<MaterialAtlasNode> nodes) {
		for (MaterialAtlasNode node : nodes) {
			if (isBucketRoot(node)) {
				return node;
			}
		}
		return null;
	}

	private static boolean isBucketRoot(MaterialAtlasNode node) {
		return node.entry().name().equals(node.atlasEntry().bucket().rootMaterialId());
	}

	private static LinkedHashMap<String, List<MaterialAtlasNode>> groupByBucket(MaterialAtlasPath path,
			List<MaterialAtlasNode> nodes) {
		LinkedHashMap<String, List<MaterialAtlasNode>> map = new LinkedHashMap<>();
		for (MaterialAtlasBucket bucket : MaterialAtlasSpec.buckets(path)) {
			map.put(bucket.id(), new ArrayList<>());
		}
		for (MaterialAtlasNode node : nodes) {
			map.computeIfAbsent(node.atlasEntry().bucket().id(), k -> new ArrayList<>()).add(node);
		}
		return map;
	}

	private static int jitter(String id, int maxAbs) {
		int range = maxAbs * 2 + 1;
		int raw = Math.floorMod(id.hashCode(), range);
		return raw - maxAbs;
	}
}
