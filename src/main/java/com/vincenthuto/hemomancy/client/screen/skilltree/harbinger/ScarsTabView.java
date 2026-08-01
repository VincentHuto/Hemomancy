package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.shared.RecipeMapInspectorLayout;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.EnumNodeShape;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.NodeShapeRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class ScarsTabView {
	private static final EnumNodeShape NODE_SHAPE = EnumNodeShape.SQUARE;
	private static final int NODE_BG = 0xCC1A0505;
	private static final int LOCKED_BORDER = 0xFF333333;
	private static final float DETAILS_Z = 400.0F;
	private static final float TOOLTIP_Z = 900.0F;
	private static float animationTime;

	private ScarsTabView() {}

	static void drawNodes(GuiGraphics gfx, ProgressScreenContext ctx, ScarsTabState state,
	                     PanZoomState panZoom, int playerDegree) {
		animationTime += 0.016f;
		int halfNode = panZoom.halfNode(ScarsTabController.NODE_SIZE);
		for (ScarTreeEntry entry : state.entries) {
			ScarTreeLayout.Point point = state.positions.get(entry.id().toString());
			if (point == null) continue;
			int x = panZoom.sx(ctx.guiLeft(), point.x());
			int y = panZoom.sy(ctx.guiTop(), point.y());
			int tendencyColor = tendencyColor(entry);
			boolean known = state.knownScarIds.contains(entry.id());
			boolean active = state.activeScarIds.contains(entry.id());
			ScarNodeVisualState visual = ScarNodeVisualState.resolve(
					ScarsTabController.isTierLocked(entry.tier(), playerDegree), known, active);

			if (visual == ScarNodeVisualState.KNOWN || visual == ScarNodeVisualState.ACTIVE) {
				float speed = visual == ScarNodeVisualState.ACTIVE ? 3.0f : 2.0f;
				int maxAlpha = visual == ScarNodeVisualState.ACTIVE ? 85 : 42;
				float pulse = 0.5f + 0.5f * Mth.sin(animationTime * speed + entry.id().hashCode() * 0.1f);
				NodeShapeRenderer.drawFill(gfx, NODE_SHAPE, x, y,
						halfNode + (visual == ScarNodeVisualState.ACTIVE ? 5 : 3),
						(withAlpha(tendencyColor, (int) (maxAlpha * pulse))));
			}
			if (entry.id().toString().equals(state.selectedScarId())) {
				float pulse = 0.5f + 0.5f * Mth.sin(animationTime * 3.0f);
				NodeShapeRenderer.drawFill(gfx, NODE_SHAPE, x, y, halfNode + 7,
						withAlpha(tendencyColor, (int) (65 * pulse)));
			}

			int border = switch (visual) {
				case LOCKED -> LOCKED_BORDER;
				case UNLEARNED -> dim(tendencyColor, 0.30f);
				case KNOWN, ACTIVE -> tendencyColor;
			};
			NodeShapeRenderer.drawFill(gfx, NODE_SHAPE, x, y, halfNode,
					visual == ScarNodeVisualState.LOCKED ? 0xCC0D0303 : NODE_BG);
			NodeShapeRenderer.drawOutline(gfx, NODE_SHAPE, x, y, halfNode, border);
			if (visual == ScarNodeVisualState.LOCKED) {
				NodeShapeRenderer.drawFill(gfx, NODE_SHAPE, x, y, halfNode - 1, 0xBB000000);
				if (panZoom.zoom >= 0.5f) gfx.drawCenteredString(ctx.font(), "?", x, y - 4, 0xFF111111);
				continue;
			}

			if (panZoom.zoom >= 0.5f) ScreenDrawUtils.renderScaledItem(gfx, entry.result(), x, y, halfNode);
			if (panZoom.zoom >= 0.7f) {
				int labelColor = visual == ScarNodeVisualState.UNLEARNED ? dim(tendencyColor, 0.42f) : tendencyColor;
				int labelY = y + halfNode + 3;
				for (String line : ScreenDrawUtils.wrapText(ctx.font(), entry.result().getHoverName().getString(),
						Math.max(28, (int) (78 * panZoom.zoom)))) {
					gfx.drawCenteredString(ctx.font(), line, x, labelY, labelColor);
					labelY += ctx.font().lineHeight;
				}
			}
		}
	}

	static ScarTreeEntry nodeUnder(ProgressScreenContext ctx, ScarsTabState state, PanZoomState panZoom,
	                              double mouseX, double mouseY) {
		int halfNode = panZoom.halfNode(ScarsTabController.NODE_SIZE);
		for (ScarTreeEntry entry : state.entries) {
			ScarTreeLayout.Point point = state.positions.get(entry.id().toString());
			if (point == null) continue;
			int x = panZoom.sx(ctx.guiLeft(), point.x());
			int y = panZoom.sy(ctx.guiTop(), point.y());
			if (NodeShapeRenderer.isInside(NODE_SHAPE, mouseX, mouseY, x, y, halfNode)) return entry;
		}
		return null;
	}

	static void drawTooltip(GuiGraphics gfx, ProgressScreenContext ctx, ScarsTabState state,
	                        PanZoomState panZoom, int playerDegree, int mouseX, int mouseY) {
		if (mouseX < ctx.guiLeft() || mouseX >= ctx.guiLeft() + ctx.guiWidth()
				|| mouseY < ctx.guiTop() || mouseY >= ctx.guiTop() + ctx.guiHeight()) return;
		ScarTreeEntry entry = nodeUnder(ctx, state, panZoom, mouseX, mouseY);
		if (entry == null) return;
		boolean locked = ScarsTabController.isTierLocked(entry.tier(), playerDegree);
		boolean known = state.knownScarIds.contains(entry.id());
		boolean active = state.activeScarIds.contains(entry.id());
		int color = tendencyColor(entry);
		List<Component> lines = new ArrayList<>();
		if (locked) {
			lines.add(Component.literal("???").withStyle(s -> s.withColor(0x555555).withBold(true)));
			lines.add(Component.literal("Requires: Degree " + requiredDegree(entry.tier()))
					.withStyle(s -> s.withColor(0xAA4444)));
		} else {
			lines.add(entry.result().getHoverName().copy().withStyle(s -> s.withColor(color).withBold(true)));
			lines.add(Component.literal(active ? "Active" : known ? "Known" : "Unlearned")
					.withStyle(s -> s.withColor(active ? 0x55DD88 : known ? 0x66BB66 : 0xAA6666)));
			lines.add(Component.literal("Tier " + roman(entry.tier())).withStyle(s -> s.withColor(0xAAAAAA)));
			lines.add(Component.literal("Tendency: " + proper(entry.tendency().name())).withStyle(s -> s.withColor(color)));
		}
		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, TOOLTIP_Z);
		gfx.renderTooltip(ctx.font(), lines, Optional.empty(), mouseX, mouseY);
		gfx.pose().popPose();
	}

	static void drawDetails(GuiGraphics gfx, ProgressScreenContext ctx, ScarTreeEntry entry,
	                        ScarsTabState state, int playerDegree) {
		int panelW = detailPanelWidth(ctx.guiWidth());
		int panelH = Math.min(250, Math.max(190, ctx.guiHeight() - 36));
		int panelX = ctx.guiLeft() + ctx.guiWidth() - panelW - 8;
		int panelY = ctx.guiTop() + 26;
		int color = tendencyColor(entry);
		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, DETAILS_Z);
		gfx.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, 0xAA000000);
		gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF1A0505);
		ScreenDrawUtils.drawSimpleBorder(gfx, panelX, panelY, panelW, panelH, color);
		boolean locked = ScarsTabController.isTierLocked(entry.tier(), playerDegree);
		if (locked) {
			gfx.drawString(ctx.font(), Component.literal("???").withStyle(s -> s.withColor(0x555555).withBold(true)),
					panelX + 8, panelY + 8, 0, false);
			gfx.drawString(ctx.font(), "Locked", panelX + 8, panelY + 26, 0xFF555555, false);
			gfx.drawString(ctx.font(), "Requires: Degree " + requiredDegree(entry.tier()),
					panelX + 8, panelY + 38, 0xFFAA4444, false);
			gfx.pose().popPose();
			return;
		}

		int patternW = Math.min(126, panelW / 3);
		drawPatternGrid(gfx, entry, panelX + 8, panelY + 28, patternW - 12, panelH - 40, color);
		int textX = panelX + patternW + 6;
		int textW = panelW - patternW - 14;
		int y = panelY + 7;
		gfx.renderItem(entry.result(), textX, y);
		for (String line : ScreenDrawUtils.wrapText(ctx.font(), entry.result().getHoverName().getString(), textW - 22)) {
			gfx.drawString(ctx.font(), Component.literal(line).withStyle(s -> s.withColor(color).withBold(true)),
					textX + 20, y + 4, 0, false);
			y += 10;
		}
		y = Math.max(y + 8, panelY + 28);
		gfx.fill(textX, y, panelX + panelW - 7, y + 1, dim(color, 0.5f));
		y += 6;
		String status = state.activeScarIds.contains(entry.id()) ? "Active"
				: state.knownScarIds.contains(entry.id()) ? "Known" : "Unlearned";
		gfx.drawString(ctx.font(), "Status: " + status, textX, y, 0xFF77BB88, false);
		y += 11;
		gfx.drawString(ctx.font(), "Tendency: " + proper(entry.tendency().name()), textX, y, color, false);
		y += 11;
		gfx.drawString(ctx.font(), "Tier: " + roman(entry.tier()), textX, y, 0xFFBBBBBB, false);
		y += 14;
		gfx.drawString(ctx.font(), "Ingredients:", textX, y, 0xFF888888, false);
		y += 10;
		y = drawIngredient(gfx, ctx, entry.recipe().getIngredient1(), textX, y, textW);
		y = drawIngredient(gfx, ctx, entry.recipe().getIngredient2(), textX, y, textW);
		y += 3;
		gfx.fill(textX, y, panelX + panelW - 7, y + 1, dim(color, 0.5f));
		y += 5;
		String lore = ScarLoreData.getLore(ScarsTabView.scarRecipeKey(entry.recipe()));
		for (String line : ScreenDrawUtils.wrapText(ctx.font(), lore, textW)) {
			if (y + 10 > panelY + panelH - 5) break;
			gfx.drawString(ctx.font(), Component.literal(line).withStyle(s -> s.withColor(0xFF7799AA).withItalic(true)),
					textX, y, 0, false);
			y += 10;
		}
		gfx.pose().popPose();
	}

	static int detailPanelWidth(int guiWidth) {
		return RecipeMapInspectorLayout.expandedPanelWidth(guiWidth);
	}

	private static void drawPatternGrid(GuiGraphics gfx, ScarTreeEntry entry,
	                                    int areaX, int areaY, int areaW, int areaH, int color) {
		byte[][] pattern = entry.recipe().getPattern();
		if (pattern == null) return;
		int cell = Math.max(2, Math.min(areaW, areaH) / 8);
		int size = cell * 8;
		int x = areaX + (areaW - size) / 2;
		int y = areaY + (areaH - size) / 2;
		gfx.fill(x - 2, y - 2, x + size + 2, y + size + 2, 0xFF0A0404);
		for (int row = 0; row < 8 && row < pattern.length; row++) {
			for (int column = 0; column < 8 && column < pattern[row].length; column++) {
				int cellX = x + column * cell;
				int cellY = y + row * cell;
				int fill = pattern[row][column] == 0 ? 0xFF120808 : color;
				gfx.fill(cellX + 1, cellY + 1, cellX + cell - 1, cellY + cell - 1, fill);
				ScreenDrawUtils.drawSimpleBorder(gfx, cellX, cellY, cell, cell,
						pattern[row][column] == 0 ? 0xFF221111 : dim(color, 0.7f));
			}
		}
	}

	private static int drawIngredient(GuiGraphics gfx, ProgressScreenContext ctx, Ingredient ingredient,
	                                  int x, int y, int width) {
		if (ingredient == null || ingredient.isEmpty() || ingredient.getItems().length == 0) return y;
		ItemStack stack = ingredient.getItems()[0];
		gfx.renderItem(stack, x, y);
		List<String> names = ScreenDrawUtils.wrapText(ctx.font(), stack.getHoverName().getString(), width - 20);
		for (int i = 0; i < names.size(); i++) {
			gfx.drawString(ctx.font(), names.get(i), x + 20, y + 4 + i * 10, 0xFFDDDDDD, false);
		}
		return y + Math.max(19, names.size() * 10 + 4);
	}

	static String scarRecipeKey(com.vincenthuto.hemomancy.common.recipe.ScarRecipe recipe) {
		String path = recipe.getId().getPath();
		return path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
	}

	private static int tendencyColor(ScarTreeEntry entry) {
		ParticleColor color = entry.tendency().getColor();
		return 0xFF000000 | ((int) color.getRed() << 16) | ((int) color.getGreen() << 8) | (int) color.getBlue();
	}

	private static int withAlpha(int color, int alpha) {
		return (color & 0x00FFFFFF) | (Mth.clamp(alpha, 0, 255) << 24);
	}

	private static int dim(int color, float factor) {
		int red = (int) (((color >> 16) & 0xFF) * factor);
		int green = (int) (((color >> 8) & 0xFF) * factor);
		int blue = (int) ((color & 0xFF) * factor);
		return 0xFF000000 | (red << 16) | (green << 8) | blue;
	}

	private static int requiredDegree(int tier) { return tier >= 3 ? 5 : 4; }
	private static String roman(int tier) { return tier == 1 ? "I" : tier == 2 ? "II" : "III"; }
	private static String proper(String value) {
		String lower = value.toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}
}
