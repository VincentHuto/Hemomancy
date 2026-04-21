package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
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
 * Static rendering and hit-testing helpers for the <em>Blood Crafting</em>
 * browser tab, shared by both the Harbinger and Unstained progress screens.
 * <p>
 * All mutable tab state lives in {@link CraftingTabState}; the shared screen
 * geometry / font / player-degree context lives in {@link ProgressScreenContext}.
 */
public final class CraftingTabView {

	private CraftingTabView() {}

	private static final int LAYER_BTN_SIZE = 16;

	// ────────────────────────────────────────────────────────────
	//  Top-level draw call
	// ────────────────────────────────────────────────────────────

	/**
	 * Draws the full Crafting tab content (sidebar + 3D model + info panel).
	 */
	public static void draw(GuiGraphics gfx, ProgressScreenContext ctx,
							CraftingTabState state, int mouseX, int mouseY, float partial) {
		if (state.craftingRecipes.isEmpty()) {
			gfx.drawCenteredString(ctx.font(), "No Blood Crafting recipes found",
					ctx.guiLeft() + ctx.guiWidth() / 2,
					ctx.guiTop()  + ctx.guiHeight() / 2, 0xFF666666);
			return;
		}

		int contentX = ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 6;
		int contentW  = ctx.guiWidth() - ProgressScreenContext.TIER_SIDEBAR_W - 10;

		if (state.selectedCraftingTier == null) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawTierSidebar(gfx, ctx, state, mouseX, mouseY);
			gfx.drawCenteredString(ctx.font(), "Select a tier",
					contentX + contentW / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}

		List<BloodStructureRecipe> tierRecipes =
				state.craftingByTier.getOrDefault(state.selectedCraftingTier, List.of());
		if (tierRecipes.isEmpty()) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawTierSidebar(gfx, ctx, state, mouseX, mouseY);
			gfx.drawCenteredString(ctx.font(), "No recipes in this tier",
					contentX + contentW / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}
		if (state.selectedCraftingIndexInTier >= tierRecipes.size())
			state.selectedCraftingIndexInTier = 0;
		BloodStructureRecipe recipe = tierRecipes.get(state.selectedCraftingIndexInTier);

		int modelAreaW = contentW / 2;
		int modelX  = contentX;
		int infoX   = contentX + modelAreaW + 10;
		int infoW   = contentW - modelAreaW - 20;

		// 3D model first (z ≈ 300)
		drawModel(gfx, state, modelX + 10, ctx.guiTop() + 30,
				modelAreaW - 20, ctx.guiHeight() - 60, recipe);

		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);

		drawTierSidebar(gfx, ctx, state, mouseX, mouseY);
		ScreenDrawUtils.drawLayerButtons(gfx, ctx.font(),
				ctx.layerBtnX(), ctx.layerBtnCenterY(),
				state.craftingMaxLayer, state.craftingVisibleLayer,
				state.tabColor, mouseX, mouseY);
		drawInfoPanel(gfx, ctx, state, recipe, infoX, ctx.guiTop() + 30, infoW);
		gfx.drawCenteredString(ctx.font(), "Drag to rotate",
				modelX + modelAreaW / 2, ctx.guiTop() + ctx.guiHeight() - 18, 0x44888888);

		gfx.pose().popPose();
	}

	// ────────────────────────────────────────────────────────────
	//  Tier sidebar
	// ────────────────────────────────────────────────────────────

	public static void drawTierSidebar(GuiGraphics gfx, ProgressScreenContext ctx,
									   CraftingTabState state, int mouseX, int mouseY) {
		int sx  = ctx.guiLeft() + 4;
		int sy  = ctx.guiTop()  + 24;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;

		gfx.drawString(ctx.font(), Component.literal("Tiers")
				.withStyle(s -> s.withColor(state.tabColor).withBold(true)), sx + 2, sy, 0);
		sy += 14;

		gfx.fill(sx, sy, sx + sw, sy + 1, state.separatorColor);
		sy += 4;

		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

		sy -= state.craftingSidebarScroll;

		for (int i = 0; i < CraftingTabState.TIER_NAMES.length; i++) {
			String tierName = CraftingTabState.TIER_NAMES[i];
			boolean locked   = state.enableDegreeLock && ctx.playerDegree() < CraftingTabState.TIER_DEGREE_REQ[i];
			boolean selected = tierName.equals(state.selectedCraftingTier);
			List<BloodStructureRecipe> recipes =
					state.craftingByTier.getOrDefault(tierName, List.of());

			boolean hovered = mouseX >= sx && mouseX <= sx + sw
					&& mouseY >= sy && mouseY <= sy + rowH
					&& mouseY >= clipTop && mouseY <= clipBottom;

			int bg = selected ? state.rowBgSelected : (hovered && !locked ? state.rowBgHovered : state.rowBgNormal);
			gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

			int bc = locked ? 0xFF333333 : (selected ? state.tabColor : 0xFF555555);
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
				int textCol = selected ? state.nameColor : 0xFF999999;
				gfx.drawString(ctx.font(), label, sx + 4, sy + (rowH - 8) / 2, textCol, false);
			}

			if (selected && !locked) {
				sy += rowH + 2;
				for (int j = 0; j < recipes.size(); j++) {
					BloodStructureRecipe r = recipes.get(j);
					boolean recSel = (j == state.selectedCraftingIndexInTier);
					boolean recHov = mouseX >= sx + 4 && mouseX <= sx + sw - 4
							&& mouseY >= sy && mouseY <= sy + 16
							&& mouseY >= clipTop && mouseY <= clipBottom;

					int recBg = recSel ? state.rowBgSelected : (recHov ? state.rowBgHovered : 0x00000000);
					gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);
					if (recSel) gfx.fill(sx + 2, sy, sx + 3, sy + 16, state.tabColor);

					String recPath = r.getId().getPath();
					if (recPath.contains("/")) recPath = recPath.substring(recPath.lastIndexOf('/') + 1);
					String recName = HLTextUtils.toProperCase(recPath.replace("_", " "));
					int recCol = recSel ? state.nameColor : 0xFF888888;
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
			if (state.craftingSidebarScroll > 0)
				gfx.drawCenteredString(ctx.font(), "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
			if (state.craftingSidebarScroll < contentH - visibleH)
				gfx.drawCenteredString(ctx.font(), "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  3D model renderer
	// ────────────────────────────────────────────────────────────

	/**
	 * Renders the blood-structure multiblock as an isometric 3D preview.
	 * Also updates {@link CraftingTabState#craftingMaxLayer}.
	 */
	public static void drawModel(GuiGraphics gfx, CraftingTabState state,
								 int areaX, int areaY, int areaW, int areaH,
								 BloodStructureRecipe recipe) {
		if (recipe.getPattern() == null) return;
		List<BlockPosBlockPair> blockPairs = recipe.getPattern().getBlockPosBlockList();
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

		state.craftingMaxLayer = maxY - minY;

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
		pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(state.craftingRotationAngle));

		float offX = -(minX + sizeX / 2f);
		float offY = -(minY + sizeY / 2f);
		float offZ = -(minZ + sizeZ / 2f);

		MultiBufferSource.BufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();

		for (BlockPosBlockPair pair : blockPairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos pos = pair.getPos();
			int relY = pos.getY() - minY;
			if (state.craftingVisibleLayer >= 0 && relY > state.craftingVisibleLayer) continue;

			pose.pushPose();
			pose.translate(pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ);
			boolean dimmed = state.craftingVisibleLayer >= 0 && relY < state.craftingVisibleLayer;
			try {
				if (dimmed) {
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
				}
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
									 CraftingTabState state, BloodStructureRecipe recipe,
									 int panelX, int panelY, int panelW) {
		int clipTop    = panelY;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 8;
		int visibleH   = clipBottom - clipTop;

		int totalH   = measureInfoPanelHeight(ctx.font(), recipe, panelW);
		int maxScroll = Math.max(0, totalH - visibleH);
		if (state.craftingInfoScroll > maxScroll) state.craftingInfoScroll = maxScroll;

		gfx.enableScissor(panelX - 2, clipTop, panelX + panelW + 2, clipBottom);

		int y     = panelY - state.craftingInfoScroll;
		int lineH = 12;

		// Name
		String namePath = recipe.getId().getPath();
		if (namePath.contains("/")) namePath = namePath.substring(namePath.lastIndexOf('/') + 1);
		String name = HLTextUtils.toProperCase(namePath.replace("_", " "));
		for (String line : ScreenDrawUtils.wrapText(ctx.font(), name, panelW)) {
			gfx.drawString(ctx.font(), Component.literal(line)
					.withStyle(s -> s.withColor(state.tabColor & 0xFFFFFF).withBold(true)), panelX, y, 0);
			y += lineH;
		}
		y += 4;
		gfx.fill(panelX, y, panelX + panelW, y + 1, state.separatorColor);
		y += 6;

		// Blood cost
		gfx.drawString(ctx.font(), Component.literal("Blood Cost: ")
				.withStyle(s -> s.withColor(0x888888))
				.append(Component.literal((int) recipe.getBloodCost() + " mL")
						.withStyle(s -> s.withColor(0xAA4444))),
				panelX, y, 0);
		y += lineH + 4;

		// Held item
		ItemStack heldItem = recipe.getHeldItem();
		if (heldItem != null && !heldItem.isEmpty()) {
			gfx.drawString(ctx.font(), Component.literal("Held Item:")
					.withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
			y += lineH;
			gfx.renderItem(heldItem, panelX, y);
			gfx.renderItemDecorations(ctx.font(), heldItem, panelX, y);
			List<String> heldLines = ScreenDrawUtils.wrapText(ctx.font(),
					heldItem.getHoverName().getString(), panelW - 20);
			for (int li = 0; li < heldLines.size(); li++) {
				int ix = li == 0 ? panelX + 20 : panelX + 4;
				gfx.drawString(ctx.font(), Component.literal(heldLines.get(li))
						.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
			}
			y += Math.max(20, heldLines.size() * lineH + 4);
		}

		// Hit block
		Block hitBlock = recipe.getHitBlock();
		if (hitBlock != null && hitBlock != Blocks.AIR) {
			gfx.drawString(ctx.font(), Component.literal("Activate on:")
					.withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
			y += lineH;
			ItemStack hitStack = new ItemStack(hitBlock);
			if (!hitStack.isEmpty()) {
				gfx.renderItem(hitStack, panelX, y);
				List<String> hitLines = ScreenDrawUtils.wrapText(ctx.font(),
						hitStack.getHoverName().getString(), panelW - 20);
				for (int li = 0; li < hitLines.size(); li++) {
					int ix = li == 0 ? panelX + 20 : panelX + 4;
					gfx.drawString(ctx.font(), Component.literal(hitLines.get(li))
							.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
				}
				y += Math.max(20, hitLines.size() * lineH + 4);
			}
		}
		y += 4;

		// Result
		ItemStack result = recipe.getResult();
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
		if (recipe.getPattern() != null) {
			Map<Block, Integer> blockCounts = recipe.getPattern().getBlockCount(false);
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
			if (state.craftingInfoScroll > 0)
				gfx.drawCenteredString(ctx.font(), "\u25B2",
						panelX + panelW / 2, clipTop, 0xAAFFFFFF);
			if (state.craftingInfoScroll < maxScroll)
				gfx.drawCenteredString(ctx.font(), "\u25BC",
						panelX + panelW / 2, clipBottom - 10, 0xAAFFFFFF);
		}
	}

	/** Measures the total (un-clipped) height of the crafting info panel. */
	public static int measureInfoPanelHeight(net.minecraft.client.gui.Font font,
											  BloodStructureRecipe recipe, int panelW) {
		int y     = 0;
		int lineH = 12;

		String namePath = recipe.getId().getPath();
		if (namePath.contains("/")) namePath = namePath.substring(namePath.lastIndexOf('/') + 1);
		String name = HLTextUtils.toProperCase(namePath.replace("_", " "));
		y += ScreenDrawUtils.wrapText(font, name, panelW).size() * lineH + 4 + 1 + 6;
		y += lineH + 4; // blood cost

		ItemStack heldItem = recipe.getHeldItem();
		if (heldItem != null && !heldItem.isEmpty()) {
			y += lineH;
			y += Math.max(20, ScreenDrawUtils.wrapText(font,
					heldItem.getHoverName().getString(), panelW - 20).size() * lineH + 4);
		}

		Block hitBlock = recipe.getHitBlock();
		if (hitBlock != null && hitBlock != Blocks.AIR) {
			y += lineH;
			ItemStack hitStack = new ItemStack(hitBlock);
			if (!hitStack.isEmpty()) {
				y += Math.max(20, ScreenDrawUtils.wrapText(font,
						hitStack.getHoverName().getString(), panelW - 20).size() * lineH + 4);
			}
		}
		y += 4;

		ItemStack result = recipe.getResult();
		if (result != null && !result.isEmpty()) {
			y += lineH;
			y += Math.max(20, ScreenDrawUtils.wrapText(font,
					result.getHoverName().getString(), panelW - 20).size() * lineH + 4);
		}
		y += 6;

		if (recipe.getPattern() != null) {
			Map<Block, Integer> blockCounts = recipe.getPattern().getBlockCount(false);
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

	/** Returns the tier name clicked in the crafting sidebar, or {@code null}. */
	public static String tierUnder(ProgressScreenContext ctx,
								   CraftingTabState state, double mx, double my) {
		int sx  = ctx.guiLeft() + 4;
		int sy  = ctx.guiTop()  + 24 + 14 + 4;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;
		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		if (my < clipTop || my > clipBottom) return null;
		sy -= state.craftingSidebarScroll;

		for (int i = 0; i < CraftingTabState.TIER_NAMES.length; i++) {
			String tierName = CraftingTabState.TIER_NAMES[i];
			boolean selected = tierName.equals(state.selectedCraftingTier);
			List<BloodStructureRecipe> recipes =
					state.craftingByTier.getOrDefault(tierName, List.of());
			if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + rowH
					&& my >= clipTop && my <= clipBottom) return tierName;
			if (selected) sy += rowH + 2 + recipes.size() * 18;
			sy += rowH + 2;
		}
		return null;
	}

	/** Returns the recipe index clicked within the selected crafting tier, or {@code -1}. */
	public static int recipeUnder(ProgressScreenContext ctx,
								   CraftingTabState state, double mx, double my) {
		if (state.selectedCraftingTier == null) return -1;
		List<BloodStructureRecipe> recipes =
				state.craftingByTier.getOrDefault(state.selectedCraftingTier, List.of());
		if (recipes.isEmpty()) return -1;

		int sx  = ctx.guiLeft() + 4;
		int sy  = ctx.guiTop()  + 24 + 14 + 4;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;
		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		if (my < clipTop || my > clipBottom) return -1;
		sy -= state.craftingSidebarScroll;

		for (int i = 0; i < CraftingTabState.TIER_NAMES.length; i++) {
			String tierName = CraftingTabState.TIER_NAMES[i];
			boolean selected = tierName.equals(state.selectedCraftingTier);
			List<BloodStructureRecipe> tierRecipes =
					state.craftingByTier.getOrDefault(tierName, List.of());
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
											   CraftingTabState state, double mx, double my) {
		if (state.craftingMaxLayer <= 0) return false;
		int cy = ctx.layerBtnCenterY();
		return ScreenDrawUtils.isOverLayerButton(mx, my, ctx.layerBtnX(), cy - LAYER_BTN_SIZE - 14);
	}

	/** Returns {@code true} if the mouse is over the layer-down button. */
	public static boolean isOverLayerDownButton(ProgressScreenContext ctx,
												 CraftingTabState state, double mx, double my) {
		if (state.craftingMaxLayer <= 0) return false;
		int cy = ctx.layerBtnCenterY();
		return ScreenDrawUtils.isOverLayerButton(mx, my, ctx.layerBtnX(), cy + 14);
	}
}
