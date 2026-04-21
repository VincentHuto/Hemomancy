package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Static rendering and hit-testing helpers for the <em>Cardinal Rites</em>
 * browser tab of {@link HarbingerProgressScreen}.
 * <p>
 * All mutable tab state lives in {@link RitesTabState}; the shared screen
 * geometry / font / player-degree context lives in {@link ProgressScreenContext}.
 * The main screen calls {@link #draw} once per frame.
 */
public final class RitesTabView {

	private RitesTabView() {}

	// ── Tab accent colour (mirrors HarbingerProgressScreen.Tab.RITES) ──
	static final int TAB_COLOR = 0xFF8844CC;

	// ── Shared layer-button size (matches ScreenDrawUtils) ────────
	private static final int LAYER_BTN_SIZE = 16;

	// ────────────────────────────────────────────────────────────
	//  Degree gate
	// ────────────────────────────────────────────────────────────

	/**
	 * Minimum initiatory degree required to browse a given rite tier.
	 */
	public static int minDegree(CardinalRiteType type) {
		return switch (type) {
			case MINOR   -> 0;
			case LESSER  -> 1;
			case GREATER -> 3;
			case GRAND   -> 5;
		};
	}

	// ────────────────────────────────────────────────────────────
	//  Top-level draw call
	// ────────────────────────────────────────────────────────────

	/**
	 * Draws the full Rites tab content (sidebar + 3D model + info panel).
	 * Called inside a scissor region from the main screen.
	 */
	public static void draw(GuiGraphics gfx, ProgressScreenContext ctx,
							RitesTabState state, int mouseX, int mouseY, float partial) {
		if (state.riteRecipes.isEmpty()) {
			gfx.drawCenteredString(ctx.font(), "No Cardinal Rites found",
					ctx.guiLeft() + ctx.guiWidth() / 2,
					ctx.guiTop()  + ctx.guiHeight() / 2, 0xFF666666);
			return;
		}

		int contentX = ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 6;
		int contentW  = ctx.guiWidth() - ProgressScreenContext.TIER_SIDEBAR_W - 10;

		if (state.selectedRiteTier == null) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawTierSidebar(gfx, ctx, state, mouseX, mouseY);
			gfx.drawCenteredString(ctx.font(), "Select a tier",
					contentX + contentW / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}

		List<CardinalRiteRecipe> tierRites =
				state.ritesByTier.getOrDefault(state.selectedRiteTier, List.of());
		if (tierRites.isEmpty()) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawTierSidebar(gfx, ctx, state, mouseX, mouseY);
			gfx.drawCenteredString(ctx.font(), "No rites in this tier",
					contentX + contentW / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}
		if (state.selectedRiteIndexInTier >= tierRites.size()) state.selectedRiteIndexInTier = 0;
		CardinalRiteRecipe rite = tierRites.get(state.selectedRiteIndexInTier);

		int modelAreaW = contentW / 2;
		int modelX  = contentX;
		int infoX   = contentX + modelAreaW + 10;
		int infoW   = contentW - modelAreaW - 20;

		// 3D model rendered first (z ≈ 300)
		drawModel(gfx, state, ctx, modelX + 10, ctx.guiTop() + 30,
				modelAreaW - 20, ctx.guiHeight() - 60, partial);

		// All 2D overlays above the model
		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);

		drawTierSidebar(gfx, ctx, state, mouseX, mouseY);
		ScreenDrawUtils.drawLayerButtons(gfx, ctx.font(),
				ctx.layerBtnX(), ctx.layerBtnCenterY(),
				state.riteMaxLayer, state.riteVisibleLayer,
				TAB_COLOR, mouseX, mouseY);
		drawInfoPanel(gfx, ctx, state, rite, infoX, ctx.guiTop() + 30, infoW, mouseX, mouseY);
		gfx.drawCenteredString(ctx.font(), "Drag to rotate",
				modelX + modelAreaW / 2, ctx.guiTop() + ctx.guiHeight() - 18, 0x44888888);

		gfx.pose().popPose();
	}

	// ────────────────────────────────────────────────────────────
	//  Tier sidebar
	// ────────────────────────────────────────────────────────────

	public static void drawTierSidebar(GuiGraphics gfx, ProgressScreenContext ctx,
									   RitesTabState state, int mouseX, int mouseY) {
		int sx  = ctx.guiLeft() + 4;
		int sy  = ctx.guiTop()  + 24;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;

		gfx.drawString(ctx.font(), Component.literal("Rite Tiers")
				.withStyle(s -> s.withColor(TAB_COLOR).withBold(true)), sx + 2, sy, 0);
		sy += 14;

		gfx.fill(sx, sy, sx + sw, sy + 1, 0xFF332244);
		sy += 4;

		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

		sy -= state.riteSidebarScroll;

		for (CardinalRiteType type : CardinalRiteType.values()) {
			boolean locked   = ctx.playerDegree() < minDegree(type);
			boolean selected = (type == state.selectedRiteTier);
			List<CardinalRiteRecipe> recipes =
					state.ritesByTier.getOrDefault(type, List.of());

			boolean hovered = mouseX >= sx && mouseX <= sx + sw
					&& mouseY >= sy && mouseY <= sy + rowH
					&& mouseY >= clipTop && mouseY <= clipBottom;

			int bg = selected ? 0xDD120818 : (hovered && !locked ? 0xBB100616 : 0x990C0410);
			gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

			int bc = locked ? 0xFF333333 : (selected ? TAB_COLOR : 0xFF555555);
			gfx.fill(sx, sy,           sx + sw, sy + 1,     bc);
			gfx.fill(sx, sy + rowH - 1, sx + sw, sy + rowH, bc);
			gfx.fill(sx, sy,           sx + 1,  sy + rowH,  bc);
			gfx.fill(sx + sw - 1, sy, sx + sw,  sy + rowH,  bc);

			String sizeLabel = type.getSize() + "x" + type.getSize();
			String tierLabel = HLTextUtils.toProperCase(type.getSerializedName());

			if (locked) {
				gfx.fill(sx + 1, sy + 1, sx + sw - 1, sy + rowH - 1, 0xBB000000);
				gfx.drawString(ctx.font(), "[X] " + tierLabel + " (Locked)",
						sx + 4, sy + (rowH - 8) / 2, 0xFF444444, false);
			} else {
				int textCol = selected ? 0xFFDDBBEE : 0xFF999999;
				gfx.drawString(ctx.font(),
						tierLabel + " " + sizeLabel + " (" + recipes.size() + ")",
						sx + 4, sy + (rowH - 8) / 2, textCol, false);
			}

			if (selected && !locked) {
				sy += rowH + 2;
				for (int j = 0; j < recipes.size(); j++) {
					CardinalRiteRecipe r = recipes.get(j);
					boolean recSel = (j == state.selectedRiteIndexInTier);
					boolean recHov = mouseX >= sx + 4 && mouseX <= sx + sw - 4
							&& mouseY >= sy && mouseY <= sy + 16
							&& mouseY >= clipTop && mouseY <= clipBottom;

					int recBg = recSel ? 0xCC180818 : (recHov ? 0xAA140614 : 0x00000000);
					gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);
					if (recSel) gfx.fill(sx + 2, sy, sx + 3, sy + 16, TAB_COLOR);

					String recName = r.getRiteName();
					if (recName == null || recName.isEmpty()) {
						String ritePath = r.getId().getPath();
						if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
						recName = HLTextUtils.toProperCase(ritePath.replace("_", " "));
					}
					recName = ScreenDrawUtils.truncateText(ctx.font(), recName, sw - 16);
					int recCol = recSel ? 0xFFDDBBEE : 0xFF888888;
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
			if (state.riteSidebarScroll > 0)
				gfx.drawCenteredString(ctx.font(), "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
			if (state.riteSidebarScroll < contentH - visibleH)
				gfx.drawCenteredString(ctx.font(), "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  3D model renderer
	// ────────────────────────────────────────────────────────────

	/**
	 * Renders the multiblock pattern as an isometric 3D preview.
	 * Also updates {@link RitesTabState#riteMaxLayer} from the structure bounds.
	 */
	public static void drawModel(GuiGraphics gfx, RitesTabState state,
								 ProgressScreenContext ctx,
								 int areaX, int areaY, int areaW, int areaH, float partial) {
		if (state.selectedRiteTier == null) return;
		List<CardinalRiteRecipe> tierRites =
				state.ritesByTier.getOrDefault(state.selectedRiteTier, List.of());
		if (state.selectedRiteIndexInTier >= tierRites.size()) return;
		CardinalRiteRecipe rite = tierRites.get(state.selectedRiteIndexInTier);
		drawModelForRite(gfx, state, rite, areaX, areaY, areaW, areaH);
	}

	private static void drawModelForRite(GuiGraphics gfx, RitesTabState state,
										  CardinalRiteRecipe rite,
										  int areaX, int areaY, int areaW, int areaH) {
		if (rite.getPattern() == null) return;
		List<BlockPosBlockPair> blockPairs = rite.getPattern().getBlockPosBlockList();
		if (blockPairs.isEmpty()) return;

		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPosBlockPair pair : blockPairs) {
			BlockPos pos = pair.getPos();
			Block block  = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			minX = Math.min(minX, pos.getX()); maxX = Math.max(maxX, pos.getX());
			minY = Math.min(minY, pos.getY()); maxY = Math.max(maxY, pos.getY());
			minZ = Math.min(minZ, pos.getZ()); maxZ = Math.max(maxZ, pos.getZ());
		}
		if (minX > maxX) return;

		state.riteMaxLayer = maxY - minY;

		float sizeX = maxX - minX + 1;
		float sizeY = maxY - minY + 1;
		float sizeZ = maxZ - minZ + 1;
		float maxDim = Math.max(sizeX, Math.max(sizeY, sizeZ));
		float scale  = Math.min(areaW, areaH) / (maxDim * 1.8f);

		int centerX = areaX + areaW / 2;
		int centerY = areaY + areaH / 2;

		PoseStack pose = gfx.pose();
		pose.pushPose();
		pose.translate(centerX, centerY, 300);
		pose.scale(scale, -scale, scale);
		pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(30));
		pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(state.riteRotationAngle));

		float offX = -(minX + sizeX / 2f);
		float offY = -(minY + sizeY / 2f);
		float offZ = -(minZ + sizeZ / 2f);

		MultiBufferSource.BufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();

		for (BlockPosBlockPair pair : blockPairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos pos = pair.getPos();
			int relY = pos.getY() - minY;
			if (state.riteVisibleLayer >= 0 && relY > state.riteVisibleLayer) continue;

			pose.pushPose();
			pose.translate(pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ);
			boolean dimmed = state.riteVisibleLayer >= 0 && relY < state.riteVisibleLayer;
			try {
				Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
						block.defaultBlockState(), pose, buf,
						dimmed ? 0x60006 : LightTexture.FULL_BRIGHT,
						OverlayTexture.NO_OVERLAY);
			} catch (Exception ignored) {}
			pose.popPose();
		}

		buf.endBatch();
		pose.popPose();
	}

	// ────────────────────────────────────────────────────────────
	//  Info panel
	// ────────────────────────────────────────────────────────────

	public static void drawInfoPanel(GuiGraphics gfx, ProgressScreenContext ctx,
									 RitesTabState state, CardinalRiteRecipe rite,
									 int panelX, int panelY, int panelW,
									 int mouseX, int mouseY) {
		int clipTop    = panelY;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 8;
		int visibleH   = clipBottom - clipTop;

		int totalH   = measureInfoPanelHeight(ctx.font(), ctx.playerDegree(), rite, panelW);
		int maxScroll = Math.max(0, totalH - visibleH);
		if (state.riteInfoScroll > maxScroll) state.riteInfoScroll = maxScroll;

		gfx.enableScissor(panelX - 2, clipTop, panelX + panelW + 2, clipBottom);

		int y     = panelY - state.riteInfoScroll;
		int lineH = 12;

		// Name
		String name = rite.getRiteName();
		if (name == null || name.isEmpty()) {
			String p = rite.getId().getPath();
			if (p.contains("/")) p = p.substring(p.lastIndexOf('/') + 1);
			name = HLTextUtils.toProperCase(p.replace("_", " "));
		}
		for (String line : ScreenDrawUtils.wrapText(ctx.font(), name, panelW)) {
			gfx.drawString(ctx.font(), Component.literal(line)
					.withStyle(s -> s.withColor(0xCC66DD).withBold(true)), panelX, y, 0);
			y += lineH;
		}
		y += 4;
		gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF442244);
		y += 6;

		// Description
		String desc = rite.getRiteDescription();
		if (desc != null && !desc.isEmpty()) {
			for (String line : ScreenDrawUtils.wrapText(ctx.font(), desc, panelW)) {
				gfx.drawString(ctx.font(), Component.literal(line)
						.withStyle(s -> s.withColor(0x999999).withItalic(true)), panelX, y, 0);
				y += lineH;
			}
			y += 4;
		}

		// Type
		CardinalRiteType type = rite.getRiteType();
		String typeStr = HLTextUtils.toProperCase(type.getSerializedName())
				+ " (" + type.getSize() + "x" + type.getSize() + ")";
		gfx.drawString(ctx.font(), Component.literal("Type: ")
				.withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(typeStr).withStyle(s -> s.withColor(0xBB88CC))),
				panelX, y, 0);
		y += lineH;

		// Blood cost
		gfx.drawString(ctx.font(), Component.literal("Blood Cost: ")
				.withStyle(s -> s.withColor(0x888888))
				.append(Component.literal((int) rite.getBloodCost() + " mL")
						.withStyle(s -> s.withColor(0xAA4444))),
				panelX, y, 0);
		y += lineH;

		// Required degree
		int reqDeg = minDegree(type);
		if (reqDeg > 0) {
			EnumInitiatoryDegree needed = EnumInitiatoryDegree.byNumber(reqDeg);
			String degName = needed != null ? needed.getTitle() : ("Degree " + reqDeg);
			int degColor = ctx.playerDegree() >= reqDeg ? 0xFF88CC88 : 0xFFCC4444;
			gfx.drawString(ctx.font(), Component.literal("Requires: ")
					.withStyle(s -> s.withColor(0x888888))
					.append(Component.literal(degName).withStyle(s -> s.withColor(degColor))),
					panelX, y, 0);
			y += lineH;
		}

		// Cast time
		float seconds = type.getCastingDurationTicks() / 20f;
		gfx.drawString(ctx.font(), Component.literal("Cast Time: ")
				.withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(String.format("%.1fs", seconds))
						.withStyle(s -> s.withColor(0xAAAA88))),
				panelX, y, 0);
		y += lineH + 6;

		// Result item
		ItemStack result = rite.getResult();
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
		y += 6;

		// Block materials list
		if (rite.getPattern() != null) {
			Map<Block, Integer> blockCounts = rite.getPattern().getBlockCount(false);
			if (!blockCounts.isEmpty()) {
				gfx.drawString(ctx.font(), Component.literal("Materials:")
						.withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
				y += lineH;
				for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
					Block block = entry.getKey();
					if (block == null || block == Blocks.AIR) continue;
					ItemStack bs = new ItemStack(block);
					if (!bs.isEmpty()) {
						gfx.renderItem(bs, panelX + 2, y);
						String prefix = " x" + entry.getValue() + "  ";
						List<String> matLines = ScreenDrawUtils.wrapText(ctx.font(),
								prefix + bs.getHoverName().getString(), panelW - 20);
						for (int li = 0; li < matLines.size(); li++) {
							gfx.drawString(ctx.font(), Component.literal(matLines.get(li))
									.withStyle(s -> s.withColor(0xAAAAAA)),
									panelX + 20, y + 4 + li * lineH, 0);
						}
						y += Math.max(18, matLines.size() * lineH + 4);
					}
				}
			}
		}

		gfx.disableScissor();

		if (totalH > visibleH) {
			if (state.riteInfoScroll > 0)
				gfx.drawCenteredString(ctx.font(), "\u25B2",
						panelX + panelW / 2, clipTop, 0xAAFFFFFF);
			if (state.riteInfoScroll < maxScroll)
				gfx.drawCenteredString(ctx.font(), "\u25BC",
						panelX + panelW / 2, clipBottom - 10, 0xAAFFFFFF);
		}
	}

	/** Measures the total (un-clipped) height of the rite info panel. */
	public static int measureInfoPanelHeight(net.minecraft.client.gui.Font font,
											  int playerDegree,
											  CardinalRiteRecipe rite, int panelW) {
		int y     = 0;
		int lineH = 12;

		String name = rite.getRiteName();
		if (name == null || name.isEmpty()) {
			String p = rite.getId().getPath();
			if (p.contains("/")) p = p.substring(p.lastIndexOf('/') + 1);
			name = HLTextUtils.toProperCase(p.replace("_", " "));
		}
		y += ScreenDrawUtils.wrapText(font, name, panelW).size() * lineH + 4 + 1 + 6;

		String desc = rite.getRiteDescription();
		if (desc != null && !desc.isEmpty())
			y += ScreenDrawUtils.wrapText(font, desc, panelW).size() * lineH + 4;

		y += lineH; // type
		y += lineH; // blood cost

		CardinalRiteType type = rite.getRiteType();
		int reqDeg = minDegree(type);
		if (reqDeg > 0) y += lineH;

		y += lineH + 6; // cast time

		ItemStack result = rite.getResult();
		if (result != null && !result.isEmpty()) {
			y += lineH;
			y += Math.max(20, ScreenDrawUtils.wrapText(font,
					result.getHoverName().getString(), panelW - 20).size() * lineH + 4);
		}
		y += 6;

		if (rite.getPattern() != null) {
			Map<Block, Integer> blockCounts = rite.getPattern().getBlockCount(false);
			if (!blockCounts.isEmpty()) {
				y += lineH;
				for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
					Block block = entry.getKey();
					if (block == null || block == Blocks.AIR) continue;
					ItemStack bs = new ItemStack(block);
					if (!bs.isEmpty()) {
						String prefix = " x" + entry.getValue() + "  ";
						y += Math.max(18, ScreenDrawUtils.wrapText(font,
								prefix + bs.getHoverName().getString(), panelW - 20).size() * lineH + 4);
					}
				}
			}
		}
		return y;
	}

	// ────────────────────────────────────────────────────────────
	//  Hit testing
	// ────────────────────────────────────────────────────────────

	/** Returns the {@link CardinalRiteType} clicked in the sidebar, or {@code null}. */
	public static CardinalRiteType tierUnder(ProgressScreenContext ctx,
											  RitesTabState state, double mx, double my) {
		int sx  = ctx.guiLeft() + 4;
		int sy  = ctx.guiTop()  + 24 + 14 + 4;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;
		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		if (my < clipTop || my > clipBottom) return null;
		sy -= state.riteSidebarScroll;

		for (CardinalRiteType type : CardinalRiteType.values()) {
			boolean selected = (type == state.selectedRiteTier);
			List<CardinalRiteRecipe> recipes =
					state.ritesByTier.getOrDefault(type, List.of());
			if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + rowH
					&& my >= clipTop && my <= clipBottom) return type;
			if (selected) sy += rowH + 2 + recipes.size() * 18;
			sy += rowH + 2;
		}
		return null;
	}

	/** Returns the recipe index clicked within the selected tier, or {@code -1}. */
	public static int recipeUnder(ProgressScreenContext ctx,
								   RitesTabState state, double mx, double my) {
		if (state.selectedRiteTier == null) return -1;
		List<CardinalRiteRecipe> recipes =
				state.ritesByTier.getOrDefault(state.selectedRiteTier, List.of());
		if (recipes.isEmpty()) return -1;

		int sx  = ctx.guiLeft() + 4;
		int sy  = ctx.guiTop()  + 24 + 14 + 4;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;
		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		if (my < clipTop || my > clipBottom) return -1;
		sy -= state.riteSidebarScroll;

		for (CardinalRiteType type : CardinalRiteType.values()) {
			boolean selected = (type == state.selectedRiteTier);
			List<CardinalRiteRecipe> tierRecipes =
					state.ritesByTier.getOrDefault(type, List.of());
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

	/** Returns {@code true} if the mouse is over the layer-up button. */
	public static boolean isOverLayerUpButton(ProgressScreenContext ctx,
											   RitesTabState state, double mx, double my) {
		if (state.riteMaxLayer <= 0) return false;
		int cy = ctx.layerBtnCenterY();
		return ScreenDrawUtils.isOverLayerButton(mx, my,
				ctx.layerBtnX(), cy - LAYER_BTN_SIZE - 14);
	}

	/** Returns {@code true} if the mouse is over the layer-down button. */
	public static boolean isOverLayerDownButton(ProgressScreenContext ctx,
												 RitesTabState state, double mx, double my) {
		if (state.riteMaxLayer <= 0) return false;
		int cy = ctx.layerBtnCenterY();
		return ScreenDrawUtils.isOverLayerButton(mx, my,
				ctx.layerBtnX(), cy + 14);
	}
}
