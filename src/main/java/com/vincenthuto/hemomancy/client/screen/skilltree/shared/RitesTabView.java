package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Static rendering and hit-testing helpers for the <em>Cardinal Rites</em>
 * browser tab, shared by both the Harbinger and Unstained progress screens.
 * <p>
 * All mutable tab state lives in {@link RitesTabState}; the shared screen
 * geometry / font / player-degree context lives in {@link ProgressScreenContext}.
 * The main screen calls {@link #draw} once per frame.
 */
public final class RitesTabView {

	private RitesTabView() {}

	// ── Shared layer-button size (matches ScreenDrawUtils) ────────
	private static final int LAYER_BTN_SIZE = 16;
	private static final long MATERIAL_CYCLE_MILLIS = 2000L;
	private static final double SURFACE_DECORATION_Y_OFFSET = 0.015D;

	// ────────────────────────────────────────────────────────────
	//  Degree gate
	// ────────────────────────────────────────────────────────────

	/**
	 * Minimum initiatory degree required to browse legacy rite-size groups.
	 */
	@Deprecated
	public static int minDegree(CardinalRiteType type) {
		return switch (type) {
			case MINOR   -> 0;
			case LESSER  -> 1;
			case GREATER -> 3;
			case GRAND   -> 5;
		};
	}

	private static int rankupTextColor() {
		float pulse = (Mth.sin((System.currentTimeMillis() % 4000L) / 4000.0F * (float) (Math.PI * 2.0)) + 1.0F) * 0.5F;
		return lerpArgb(0xFFFF3030, 0xFFFFD45A, pulse);
	}

	private static int lerpArgb(int from, int to, float amount) {
		int a = Mth.lerpInt(amount, from >>> 24, to >>> 24);
		int r = Mth.lerpInt(amount, from >> 16 & 0xFF, to >> 16 & 0xFF);
		int g = Mth.lerpInt(amount, from >> 8 & 0xFF, to >> 8 & 0xFF);
		int b = Mth.lerpInt(amount, from & 0xFF, to & 0xFF);
		return a << 24 | r << 16 | g << 8 | b;
	}

	private static int withAlpha(int argb, int alpha) {
		return alpha << 24 | argb & 0x00FFFFFF;
	}

	private static void drawScrollingTitle(GuiGraphics gfx, ProgressScreenContext ctx,
										   String title, int x, int y, int width, int color) {
		Component styledTitle = Component.literal(title)
				.withStyle(s -> s.withColor(color).withBold(true));
		int titleWidth = ctx.font().width(title);
		if (titleWidth <= width) {
			gfx.drawString(ctx.font(), styledTitle, x, y, 0);
			return;
		}

		int gap = 24;
		int travel = width + titleWidth + gap;
		float scroll = (System.currentTimeMillis() % (travel * 45L)) / 45.0F;
		int drawX = x + width - Mth.floor(scroll);
		gfx.enableScissor(x, y - 2, x + width, y + 12);
		gfx.drawString(ctx.font(), styledTitle, drawX, y, 0);
		gfx.drawString(ctx.font(), styledTitle, drawX + travel, y, 0);
		gfx.disableScissor();
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
			gfx.drawCenteredString(ctx.font(), "Select a degree",
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
			gfx.drawCenteredString(ctx.font(), "No rites at this degree",
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
				state.tabColor, mouseX, mouseY);
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

		if (state.showIchorianSigils) {
			sy = drawIchorianDropdown(gfx, ctx, state, sx, sy, sw);
		}
		gfx.drawString(ctx.font(), Component.literal("Rite Degrees")
				.withStyle(s -> s.withColor(state.tabColor).withBold(true)), sx + 2, sy, 0);
		sy += 14;

		gfx.fill(sx, sy, sx + sw, sy + 1, state.separatorColor);
		sy += 4;

		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

		sy -= state.riteSidebarScroll;

		for (int type : RecipeDegreeGates.LEVELS) {
			boolean selected = (state.selectedRiteTier != null && type == state.selectedRiteTier);
			List<CardinalRiteRecipe> recipes =
					state.ritesByTier.getOrDefault(type, List.of());

			if (state.hideEmptyTiers && recipes.isEmpty()) continue;

			boolean locked   = state.enableDegreeLock && ctx.playerDegree() < type;

			boolean hovered = mouseX >= sx && mouseX <= sx + sw
					&& mouseY >= sy && mouseY <= sy + rowH
					&& mouseY >= clipTop && mouseY <= clipBottom;

			int bg = selected ? state.rowBgSelected
					: (hovered && !locked ? state.rowBgHovered : state.rowBgNormal);
			gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

			int bc = locked ? 0xFF333333 : (selected ? state.tabColor : 0xFF555555);
			gfx.fill(sx, sy,           sx + sw, sy + 1,     bc);
			gfx.fill(sx, sy + rowH - 1, sx + sw, sy + rowH, bc);
			gfx.fill(sx, sy,           sx + 1,  sy + rowH,  bc);
			gfx.fill(sx + sw - 1, sy, sx + sw,  sy + rowH,  bc);

			String tierLabel = type == 0 ? "No Degree" : "Degree " + type;

			if (locked) {
				gfx.fill(sx + 1, sy + 1, sx + sw - 1, sy + rowH - 1, 0xBB000000);
				gfx.drawString(ctx.font(), "[X] " + tierLabel + " (Locked)",
						sx + 4, sy + (rowH - 8) / 2, 0xFF444444, false);
			} else {
				int textCol = selected ? state.nameColor : 0xFF999999;
				gfx.drawString(ctx.font(),
						tierLabel + " (" + recipes.size() + ")",
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

					int recBg = recSel ? state.rowBgSelected
							: (recHov ? state.rowBgHovered : 0x00000000);
					gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);
					if (recSel) gfx.fill(sx + 2, sy, sx + 3, sy + 16, state.tabColor);

					String recName = r.getRiteName();
					if (recName == null || recName.isEmpty()) {
						String ritePath = r.getId().getPath();
						if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
						recName = HLTextUtils.toProperCase(ritePath.replace("_", " "));
					}
					recName = ScreenDrawUtils.truncateText(ctx.font(), recName, sw - 16);
					int recCol = recSel ? state.nameColor : 0xFF888888;
					if (r.isRankup()) {
						recCol = rankupTextColor();
						int glowCol = withAlpha(recCol, 0x70);
						gfx.drawString(ctx.font(), recName, sx + 7, sy + 4, glowCol, false);
						gfx.drawString(ctx.font(), recName, sx + 9, sy + 4, glowCol, false);
						gfx.drawString(ctx.font(), recName, sx + 8, sy + 3, glowCol, false);
						gfx.drawString(ctx.font(), recName, sx + 8, sy + 5, glowCol, false);
					}
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

	private static int drawIchorianDropdown(GuiGraphics gfx, ProgressScreenContext ctx,
			RitesTabState state, int x, int y, int width) {
		gfx.fill(x, y, x + width, y + 18, state.rowBgNormal);
		gfx.drawString(ctx.font(), "Ichorian Sigils " + (state.ichorianSigilsExpanded ? "▲" : "▼"),
				x + 3, y + 5, 0xFFE8B75A, false);
		y += 20;
		if (!state.ichorianSigilsExpanded) return y + 2;
		Minecraft minecraft = Minecraft.getInstance();
		var knowledge = minecraft.player == null ? null
				: HemoCapabilityAccess.getIchorianKnowledge(minecraft.player).orElse(null);
		List<IchorianSigilDefinition> definitions = IchorianSigilRegistry.all().stream()
				.sorted(java.util.Comparator.comparingInt(IchorianSigilDefinition::tier)
						.thenComparing(definition -> definition.id().toString()))
				.toList();
		for (IchorianSigilDefinition sigil : definitions) {
			boolean known = knowledge != null && knowledge.isKnown(sigil.id());
			int partial = knowledge == null ? 0 : knowledge.discoveredNodeCount(sigil.id());
			if (!known && partial == 0) continue;
			gfx.fill(x + 2, y, x + width - 2, y + 14, 0x99100616);
			int color = 0xFF000000 | sigil.color();
			gfx.fill(x + 4, y + 3, x + 10, y + 10, color);
			String label = known ? sigil.name() + " · " + sigil.bloodCostMl() + "ml"
					: "Unknown shape · " + partial + "/" + sigil.nodes().size();
			gfx.drawString(ctx.font(), label, x + 13, y + 3, known ? 0xFFE9D8AE : 0xFF9B879F, false);
			int pipX = x + 13;
			for (int i = 0; i < sigil.nodes().size() && pipX + i * 3 < x + width - 4; i++) {
				boolean seen = knowledge != null && knowledge.hasDiscoveredNode(sigil.id(), i);
				gfx.fill(pipX + i * 3, y + 12, pipX + i * 3 + 2, y + 13,
						seen ? color : 0xFF392D3B);
			}
			y += 16;
		}
		return y + 4;
	}

	private static int ichorianDropdownHeight(RitesTabState state) {
		if (!state.showIchorianSigils) return 0;
		if (!state.ichorianSigilsExpanded) return 22;
		Minecraft minecraft = Minecraft.getInstance();
		var knowledge = minecraft.player == null ? null
				: HemoCapabilityAccess.getIchorianKnowledge(minecraft.player).orElse(null);
		long visible = IchorianSigilRegistry.all().stream().filter(sigil ->
				knowledge != null && (knowledge.isKnown(sigil.id())
						|| knowledge.discoveredNodeCount(sigil.id()) > 0)).count();
		return 24 + (int) visible * 16;
	}

	public static boolean isOverIchorianDropdown(ProgressScreenContext ctx, RitesTabState state,
			double mouseX, double mouseY) {
		if (!state.showIchorianSigils) return false;
		int x = ctx.guiLeft() + 4;
		int y = ctx.guiTop() + 24;
		int width = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 18;
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
		if (rite.getPreviewPattern() == null) return;
		List<BlockPosBlockPair> blockPairs = rite.getPreviewPattern().getDisplayBlockPosBlockList(materialCycleIndex());
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
			if (isSurfaceDecorationBlock(block)) {
				pose.translate(0.0D, SURFACE_DECORATION_Y_OFFSET, 0.0D);
			}
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

	private static boolean isSurfaceDecorationBlock(Block block) {
		return block == BlockInit.engram_block.get()
				|| block == BlockInit.befouling_ash_trail.get()
				|| block == BlockInit.smouldering_ash_trail.get()
				|| block == BlockInit.active_befouling_ash_trail.get()
				|| block == BlockInit.active_smouldering_ash_trail.get()
				|| block == BlockInit.virid_salis_trail.get();
	}

	// ────────────────────────────────────────────────────────────
	//  Info panel
	// ────────────────────────────────────────────────────────────

	private static ItemStack materialStackFor(Block block) {
		if (block == BlockInit.engram_block.get()) {
			return new ItemStack(ItemInit.engram_stamp.get());
		}
		return new ItemStack(block);
	}

	private static long materialCycleIndex() {
		return System.currentTimeMillis() / MATERIAL_CYCLE_MILLIS;
	}

	private static String materialLabelFor(MultiblockPatternKey key, ItemStack currentStack) {
		if (key.isTag()) {
			return key.displayLabel() + " (" + currentStack.getHoverName().getString() + ")";
		}
		return currentStack.getHoverName().getString();
	}

	private static List<FormattedCharSequence> wrappedLines(net.minecraft.client.gui.Font font, String text, int width) {
		return font.split(Component.literal(text), Math.max(1, width));
	}

	public static void drawInfoPanel(GuiGraphics gfx, ProgressScreenContext ctx,
									 RitesTabState state, CardinalRiteRecipe rite,
									 int panelX, int panelY, int panelW,
									 int mouseX, int mouseY) {
		int clipTop    = panelY;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 8;
		int visibleH   = clipBottom - clipTop;

		int totalH   = measureInfoPanelHeight(ctx.font(), ctx.playerDegree(), rite, panelW, state.enableDegreeLock);
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
		drawScrollingTitle(gfx, ctx, name, panelX, y, panelW-17, state.nameColor);
		y += lineH;
		if (rite.hasLayeredStation()) {
			String floor = rite.getFloorId().getPath().replace('_', ' ');
			gfx.drawString(ctx.font(), Component.literal("Minimum Floor: ")
					.withStyle(s -> s.withColor(0x888888))
					.append(Component.literal(HLTextUtils.toProperCase(floor) + " (higher same-style tiers valid)")
							.withStyle(s -> s.withColor(0xBB7777))), panelX, y, 0);
			y += lineH;
			String structure = rite.getRequiredStructure() == null ? "None"
					: rite.shouldConsumeRequiredStructure() ? "Consumed on success" : "Reusable";
			gfx.drawString(ctx.font(), Component.literal("Upper Structure: ")
					.withStyle(s -> s.withColor(0x888888))
					.append(Component.literal(structure).withStyle(s -> s.withColor(0xAAAAAA))),
					panelX, y, 0);
			y += lineH;
			gfx.drawString(ctx.font(), Component.literal("Lit Brazier Offerings: ")
					.withStyle(s -> s.withColor(0x888888))
					.append(Component.literal(String.valueOf(rite.getBrazierSignature().stream()
							.mapToInt(CardinalRiteRecipe.BrazierRequirement::count).sum()))
							.withStyle(s -> s.withColor(0xCC9966))), panelX, y, 0);
			y += lineH;
		}
		y += 4;
		gfx.fill(panelX, y, panelX + panelW, y + 1, state.separatorColor);
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

		// Rite form controls structure size / cast time, not progression access.
		CardinalRiteType type = rite.getRiteType();
		String typeStr = HLTextUtils.toProperCase(type.getSerializedName())
				+ " (" + type.getSize() + "x" + type.getSize() + ")";
		gfx.drawString(ctx.font(), Component.literal("Rite Form: ")
				.withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(typeStr).withStyle(s -> s.withColor(state.tabColor & 0xFFFFFF))),
				panelX, y, 0);
		y += lineH;

		// Upfront blood cost (interactive ceremonies feed their declared anchors instead).
		String bloodLabel = rite.hasInteractiveCeremony() && rite.getBloodCost() == 0.0D
				? "None"
				: (int) rite.getBloodCost() + " mL";
		gfx.drawString(ctx.font(), Component.literal("Upfront Blood: ")
				.withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(bloodLabel)
						.withStyle(s -> s.withColor(0xAA4444))),
				panelX, y, 0);
		y += lineH;

		// Required degree (only shown when degree locking is active)
		if (state.enableDegreeLock) {
			int reqDeg = RecipeDegreeGates.getRequiredDegree(rite);
			if (reqDeg > 0) {
				String degName = RecipeDegreeGates.requirementLabel(rite);
				boolean meets = Minecraft.getInstance().player != null
						&& RecipeDegreeGates.playerMeets(Minecraft.getInstance().player, rite);
				int degColor = meets ? 0xFF88CC88 : 0xFFCC4444;
				gfx.drawString(ctx.font(), Component.literal("Requires: ")
						.withStyle(s -> s.withColor(0x888888))
						.append(Component.literal(degName).withStyle(s -> s.withColor(degColor))),
						panelX, y, 0);
				y += lineH;
			}
		}

		// Cast time
		float seconds = (rite.hasInteractiveCeremony()
				? rite.getCeremony().targetDurationTicks()
				: type.getCastingDurationTicks()) / 20f;
		gfx.drawString(ctx.font(), Component.literal("Cast Time: ")
				.withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(String.format("%.1fs", seconds))
						.withStyle(s -> s.withColor(0xAAAA88))),
				panelX, y, 0);
		y += lineH + 6;

		if (rite.hasInteractiveCeremony()) {
			gfx.drawString(ctx.font(), Component.literal("Ceremony:")
					.withStyle(s -> s.withColor(state.tabColor & 0xFFFFFF).withBold(true)),
					panelX, y, 0);
			y += lineH;
			for (String summary : ceremonySummaryLines(rite)) {
				for (String line : ScreenDrawUtils.wrapText(ctx.font(), summary, panelW)) {
					gfx.drawString(ctx.font(), Component.literal(line)
							.withStyle(s -> s.withColor(0xAAAAAA)), panelX, y, 0);
					y += lineH;
				}
			}
			y += 6;
		}

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
		if (rite.getPreviewPattern() != null) {
			List<MultiblockPattern.MaterialCount> materials = rite.getPreviewPattern().getMaterialCounts(false);
			if (!materials.isEmpty()) {
				gfx.drawString(ctx.font(), Component.literal("Materials:")
						.withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
				y += lineH;
				int materialTextX = panelX + 20;
				int materialWrapW = panelW - 24;
				long cycleIndex = materialCycleIndex();
				for (MultiblockPattern.MaterialCount material : materials) {
					MultiblockPatternKey key = material.key();
					Block block = key.displayBlock(cycleIndex);
					if (block == null || block == Blocks.AIR) continue;
					ItemStack bs = materialStackFor(block);
					if (!bs.isEmpty()) {
						gfx.renderItem(bs, panelX + 2, y);
						String prefix = " x" + material.count() + "  ";
						List<FormattedCharSequence> matLines = wrappedLines(ctx.font(),
								prefix + materialLabelFor(key, bs), materialWrapW);
						for (int li = 0; li < matLines.size(); li++) {
							gfx.drawString(ctx.font(), matLines.get(li),
									materialTextX, y + 4 + li * lineH, 0xAAAAAA);
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
											  CardinalRiteRecipe rite, int panelW,
											  boolean enableDegreeLock) {
		int y     = 0;
		int lineH = 12;

		String name = rite.getRiteName();
		if (name == null || name.isEmpty()) {
			String p = rite.getId().getPath();
			if (p.contains("/")) p = p.substring(p.lastIndexOf('/') + 1);
			name = HLTextUtils.toProperCase(p.replace("_", " "));
		}
		y += lineH + 4 + 1 + 6;

		String desc = rite.getRiteDescription();
		if (desc != null && !desc.isEmpty())
			y += ScreenDrawUtils.wrapText(font, desc, panelW).size() * lineH + 4;

		y += lineH; // rite form
		if (rite.hasLayeredStation()) y += lineH * 3;
		y += lineH; // blood cost

		if (enableDegreeLock) {
			int reqDeg = RecipeDegreeGates.getRequiredDegree(rite);
			if (reqDeg > 0) y += lineH;
		}

		y += lineH + 6; // cast time

		if (rite.hasInteractiveCeremony()) {
			y += lineH; // ceremony heading
			for (String summary : ceremonySummaryLines(rite)) {
				y += ScreenDrawUtils.wrapText(font, summary, panelW).size() * lineH;
			}
			y += 6;
		}

		ItemStack result = rite.getResult();
		if (result != null && !result.isEmpty()) {
			y += lineH;
			y += Math.max(20, ScreenDrawUtils.wrapText(font,
					result.getHoverName().getString(), panelW - 20).size() * lineH + 4);
		}
		y += 6;

		if (rite.getPreviewPattern() != null) {
			List<MultiblockPattern.MaterialCount> materials = rite.getPreviewPattern().getMaterialCounts(false);
			if (!materials.isEmpty()) {
				y += lineH;
				int materialWrapW = panelW - 24;
				long cycleIndex = materialCycleIndex();
				for (MultiblockPattern.MaterialCount material : materials) {
					MultiblockPatternKey key = material.key();
					Block block = key.displayBlock(cycleIndex);
					if (block == null || block == Blocks.AIR) continue;
					ItemStack bs = materialStackFor(block);
					if (!bs.isEmpty()) {
						String prefix = " x" + material.count() + "  ";
						y += Math.max(18, wrappedLines(font,
								prefix + materialLabelFor(key, bs), materialWrapW).size() * lineH + 4);
					}
				}
			}
		}
		return y;
	}

	private static List<String> ceremonySummaryLines(CardinalRiteRecipe rite) {
		var ceremony = rite.getCeremony();
		if (ceremony == null) return List.of();
		long rings = ceremony.anchors().stream()
				.mapToInt(com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition.Anchor::ring)
				.distinct().count();
		long requiredSigils = ceremony.supportSockets().stream()
				.filter(com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition.SupportSocket::required)
				.count();
		String focus = switch (ceremony.focusMode()) {
			case "temple_medium" -> "prebuilt temple focus + iron nugget (4 health)";
			case "hematic_medium" -> "Cardinal Focus + disposable iron nugget";
			case "living_staff" -> "Living Staff planted in the Cardinal Focus";
			default -> ceremony.focusMode().isBlank() ? "rite-specific" : ceremony.focusMode();
		};
		String boundary = ceremony.anchors().isEmpty()
				? "Boundary: small visual ring; no anchors to fill"
				: "Boundary: " + ceremony.anchors().size() + " blood-fed anchors across "
						+ rings + (rings == 1 ? " ring" : " rings")
						+ " (" + ceremony.anchorBloodCostMl() + " mL total)";
		String support = ceremony.supportSockets().isEmpty()
				? "Inscriptions: none"
				: "Inscriptions: " + ceremony.supportSockets().size() + " support sigils; "
						+ requiredSigils + " required";
		String ordeal = ceremony.waves().isEmpty() && ceremony.guaranteedWaves().isEmpty()
				? "Ordeal: none"
				: "Ordeal: enabled; still interval " + ceremony.stillIntervalTicks() + " ticks";
		String atmosphere = "Atmosphere: " + ceremony.atmosphere().fog() + " fog"
				+ (ceremony.atmosphere().lightning() ? ", rite lightning" : "")
				+ (ceremony.atmosphere().dome() ? ", boundary dome" : "");
		return List.of(
				"Activation: " + focus,
				boundary,
				support,
				"Helpers: " + ceremony.requiredHelpers() + " required",
				ordeal,
				atmosphere,
				"Failure: " + ceremony.failureProfile().replace('_', ' '),
				"Progression lesson: " + progressionLesson(rite.getRequiredDegree()));
	}

	private static String progressionLesson(int degree) {
		return switch (degree) {
			case 0 -> "consent and awakening on a prebuilt temple threshold";
			case 1 -> "build the first floor, seat an iron medium, and feed four anchors";
			case 2 -> "take up the Living Staff and prepare the first brazier offering";
			case 3 -> "repeat Lesser practice under the first faint ritual fog";
			case 4 -> "inscribe the first support sigil and coordinate multiple offerings";
			case 5 -> "sustain eight anchors through lightning and the first ordeal";
			case 6 -> "raise a domed Grand boundary and accept required aid and safety sigils";
			default -> "coordinate a full Grand rite with multiple helpers, sigils, and a lengthy ordeal";
		};
	}

	/** @deprecated Use {@link #measureInfoPanelHeight(net.minecraft.client.gui.Font, int, CardinalRiteRecipe, int, boolean)} */
	@Deprecated
	public static int measureInfoPanelHeight(net.minecraft.client.gui.Font font,
											  int playerDegree,
											  CardinalRiteRecipe rite, int panelW) {
		return measureInfoPanelHeight(font, playerDegree, rite, panelW, true);
	}

	// ────────────────────────────────────────────────────────────
	//  Hit testing
	// ────────────────────────────────────────────────────────────

	/** Returns the required degree clicked in the sidebar, or {@code null}. */
	public static Integer tierUnder(ProgressScreenContext ctx,
											  RitesTabState state, double mx, double my) {
		int sx  = ctx.guiLeft() + 4;
		int sy  = ctx.guiTop()  + 24 + ichorianDropdownHeight(state) + 14 + 4;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;
		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		if (my < clipTop || my > clipBottom) return null;
		sy -= state.riteSidebarScroll;

		for (int type : RecipeDegreeGates.LEVELS) {
			boolean selected = (state.selectedRiteTier != null && type == state.selectedRiteTier);
			List<CardinalRiteRecipe> recipes =
					state.ritesByTier.getOrDefault(type, List.of());
			if (state.hideEmptyTiers && recipes.isEmpty()) continue;
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
		int sy  = ctx.guiTop()  + 24 + ichorianDropdownHeight(state) + 14 + 4;
		int sw  = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;
		int clipTop    = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		if (my < clipTop || my > clipBottom) return -1;
		sy -= state.riteSidebarScroll;

		for (int type : RecipeDegreeGates.LEVELS) {
			boolean selected = (state.selectedRiteTier != null && type == state.selectedRiteTier);
			List<CardinalRiteRecipe> tierRecipes =
					state.ritesByTier.getOrDefault(type, List.of());
			if (state.hideEmptyTiers && tierRecipes.isEmpty()) continue;
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
