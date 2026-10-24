package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.world.MnemonicBlueprintPattern;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * A read-only rite detail screen opened from a filled {@link MnemonicBlueprintItem}.
 * that displays a single cardinal rite's 3D structure and info panel — similar to the
 * RITES tab in {@link HarbingerProgressScreen},
 * but focused on a single rite without the tier sidebar.
 * <p>
 * The screen looks like a weathered blood-stained parchment (grey/red tones).
 */
public class RiteHintScreen extends Screen {

	// ── Appearance constants ──
	private static final int BG_COLOR = 0xEE1A0A08;
	private static final int BORDER_OUTER = 0xFF4A1010;
	private static final int BORDER_INNER = 0xFF331010;
	private static final int TITLE_COLOR = 0xFFCC4444;
	private static final int DESC_COLOR = 0xFF999999;
	private static final int LABEL_COLOR = 0xFF888888;
	private static final int VALUE_COLOR = 0xFFAAAAAA;
	private static final int COST_COLOR = 0xFFAA4444;
	private static final int ACCENT_COLOR = 0xFFBB88CC;

	// ── Layout ──
	private static final int GUI_WIDTH = 340;
	private static final int GUI_HEIGHT = 220;
	private static final int LAYER_BTN_SIZE = 16;
	private static final long MATERIAL_CYCLE_MILLIS = 2000L;

	private final ResourceLocation riteId;
	private CardinalRiteRecipe rite;
	private CardinalRiteFloorDefinition floor;

	// 3D model state
	private float rotationAngle = 45f; // default diagonal view
	private int visibleLayer = -1; // -1 = all layers
	private int maxLayer = 0;
	private boolean dragging = false;
	private double dragLastX;

	// Info panel scroll
	private int infoScroll = 0;

	// Computed layout
	private int guiLeft, guiTop;

	private RiteHintScreen(ResourceLocation riteId) {
		super(Component.translatable("item.hemomancy.mnemonic_blueprint"));
		this.riteId = riteId;
	}

	/**
	 * Opens the rite hint screen for the given rite recipe.
	 * Called client-side only.
	 */
	public static void open(ResourceLocation riteId) {
		Minecraft.getInstance().setScreen(new RiteHintScreen(riteId));
	}

	@Override
	protected void init() {
		super.init();
		this.guiLeft = (this.width - GUI_WIDTH) / 2;
		this.guiTop = (this.height - GUI_HEIGHT) / 2;

		// Look up the rite recipe from the level
		if (minecraft != null && minecraft.level != null) {
			this.rite = CardinalRiteRecipe.getRiteByLocation(minecraft.level, riteId);
			if (this.rite == null) this.floor = CardinalRiteFloorRegistry.get(riteId).orElse(null);
		}
	}

	/** Suppress the 1.21.1 menu_blur post-effect from Screen#renderBackground. */
	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// intentionally empty
	}

	// ══════════════════════════════════════════════════════════════
	//  Rendering
	// ══════════════════════════════════════════════════════════════

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		// Do NOT call renderBackground() — it applies blur. This is not a pause screen.
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();

		// Slowly rotate when not dragging
		if (!dragging) {
			rotationAngle += partial * 0.3f;
		}

		// ── Background panel ──
		gfx.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, BG_COLOR);
		ScreenDrawUtils.drawBorder(gfx, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, BORDER_OUTER, BORDER_INNER);

		if (rite == null && floor == null) {
			gfx.drawCenteredString(font, Component.translatable("screen.hemomancy.rite_hint.unknown_rite").withStyle(s -> s.withColor(0xFF666666)),
					guiLeft + GUI_WIDTH / 2, guiTop + GUI_HEIGHT / 2 - 4, 0);
			super.render(gfx, mouseX, mouseY, partial);
			return;
		}

		// Layout: left half = 3D model, right half = info panel
		int modelAreaW = GUI_WIDTH / 2;
		int modelX = guiLeft;
		int infoX = guiLeft + modelAreaW + 6;
		int infoW = GUI_WIDTH - modelAreaW - 16;
		int contentTop = guiTop + 6;
		int contentH = GUI_HEIGHT - 12;

		// ── 3D model (at z=300 so blocks render behind 2D overlays) ──
		drawRiteModel(gfx, modelX + 6, contentTop, modelAreaW - 12, contentH, partial);

		// ── 2D overlays at z=400 ──
		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);

		// Layer buttons
		drawLayerButtons(gfx, mouseX, mouseY, modelX + modelAreaW - LAYER_BTN_SIZE - 8, guiTop + GUI_HEIGHT / 2);

		// Drag hint
		gfx.drawCenteredString(font, Component.translatable("screen.hemomancy.crafting_tab_view.drag_to_rotate"),
				modelX + modelAreaW / 2, guiTop + GUI_HEIGHT - 14, 0x44888888);

		// Info panel
		drawInfoPanel(gfx, infoX, contentTop, infoW, contentH, mouseX, mouseY);

		gfx.pose().popPose();

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		super.render(gfx, mouseX, mouseY, partial);
	}

	// ── 3D Multiblock Preview ──

	private void drawRiteModel(GuiGraphics gfx, int areaX, int areaY, int areaW, int areaH, float partial) {
		List<BlockPosBlockPair> blockPairs = previewPattern().displayBlockPairs(materialCycleIndex());
		if (blockPairs.isEmpty()) return;

		// Determine bounding box
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPosBlockPair pair : blockPairs) {
			BlockPos pos = pair.getPos();
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			minX = Math.min(minX, pos.getX()); maxX = Math.max(maxX, pos.getX());
			minY = Math.min(minY, pos.getY()); maxY = Math.max(maxY, pos.getY());
			minZ = Math.min(minZ, pos.getZ()); maxZ = Math.max(maxZ, pos.getZ());
		}
		if (minX > maxX) return;

		maxLayer = maxY - minY;

		float sizeX = maxX - minX + 1;
		float sizeY = maxY - minY + 1;
		float sizeZ = maxZ - minZ + 1;
		float maxDim = Math.max(sizeX, Math.max(sizeY, sizeZ));

		float scale = Math.min(areaW, areaH) / (maxDim * 1.8f);
		int centerX = areaX + areaW / 2;
		int centerY = areaY + areaH / 2;

		PoseStack pose = gfx.pose();
		pose.pushPose();

		pose.translate(centerX, centerY, 300);
		pose.scale(scale, -scale, scale);
		pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(30));
		pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationAngle));

		float offX = -(minX + sizeX / 2f);
		float offY = -(minY + sizeY / 2f);
		float offZ = -(minZ + sizeZ / 2f);

		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

		for (BlockPosBlockPair pair : blockPairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos pos = pair.getPos();

			int relativeY = pos.getY() - minY;
			if (visibleLayer >= 0 && relativeY > visibleLayer) continue;

			pose.pushPose();
			pose.translate(pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ);

			boolean dimmed = visibleLayer >= 0 && relativeY < visibleLayer;

			try {
				Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
						block.defaultBlockState(), pose, bufferSource,
						dimmed ? 0x60006 : LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
			} catch (RuntimeException ignored) {
				// Silently skip blocks that can't be rendered
			}

			pose.popPose();
		}

		bufferSource.endBatch();
		pose.popPose();
	}

	// ── Info Panel ──

	private long materialCycleIndex() {
		return System.currentTimeMillis() / MATERIAL_CYCLE_MILLIS;
	}

	private ItemStack materialStackFor(Block block) {
		if (block == BlockInit.engram_block.get()) {
			return new ItemStack(ItemInit.engram_stamp.get());
		}
		return new ItemStack(block);
	}

	private String materialLabelFor(MultiblockPatternKey key, ItemStack currentStack) {
		if (key.isTag()) {
			return key.displayLabel() + " (" + currentStack.getHoverName().getString() + ")";
		}
		return currentStack.getHoverName().getString();
	}

	private void drawInfoPanel(GuiGraphics gfx, int panelX, int panelY, int panelW, int panelH,
							   int mouseX, int mouseY) {
		if (floor != null) {
			drawFloorInfoPanel(gfx, panelX, panelY, panelW, panelH);
			return;
		}
		int clipTop = panelY;
		int clipBottom = panelY + panelH;
		int visibleH = clipBottom - clipTop;

		int totalH = measureInfoPanelHeight(panelW);
		int maxScroll = Math.max(0, totalH - visibleH);
		if (infoScroll > maxScroll) infoScroll = maxScroll;

		gfx.enableScissor(panelX - 2, clipTop, panelX + panelW + 2, clipBottom);

		int y = panelY - infoScroll;
		int lineH = 12;

		// ── Rite name ──
		String name = rite.displayName().getString();
		if (name == null || name.isEmpty()) {
			String ritePath = rite.getId().getPath();
			if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
			name = HLTextUtils.toProperCase(ritePath.replace("_", " "));
		}
		for (String titleLine : ScreenDrawUtils.wrapText(font, name, panelW)) {
			gfx.drawString(font, Component.literal(titleLine)
					.withStyle(s -> s.withColor(TITLE_COLOR).withBold(true)), panelX, y, 0);
			y += lineH;
		}
		y += 4;

		// ── Separator ──
		gfx.fill(panelX, y, panelX + panelW, y + 1, BORDER_INNER);
		y += 6;

		// ── Description ──
		String desc = rite.displayDescription().getString();
		if (desc != null && !desc.isEmpty()) {
			for (String line : ScreenDrawUtils.wrapText(font, desc, panelW)) {
				gfx.drawString(font, Component.literal(line)
						.withStyle(s -> s.withColor(DESC_COLOR).withItalic(true)), panelX, y, 0);
				y += lineH;
			}
			y += 4;
		}

		// ── Type ──
		CardinalRiteType type = rite.getRiteType();
		gfx.drawString(font, Component.translatable("screen.hemomancy.rite_hint.type",
				CardinalRiteFloorDefinition.tierWithSize(type).withStyle(s -> s.withColor(ACCENT_COLOR))).withStyle(s -> s.withColor(LABEL_COLOR)), panelX, y, 0);
		y += lineH;

		// ── Blood cost ──
		gfx.drawString(font, Component.translatable("screen.hemomancy.rite_hint.upfront_blood",
				HemoText.translatable("screen.hemomancy.rite_hint.ml", (int) rite.getBloodCost()).withStyle(s -> s.withColor(COST_COLOR))).withStyle(s -> s.withColor(LABEL_COLOR)), panelX, y, 0);
		y += lineH;

		// ── Cast time ──
		int ticks = rite.getCeremony() == null
				? type.getCastingDurationTicks()
				: rite.getCeremony().targetDurationTicks();
		float seconds = ticks / 20f;
		gfx.drawString(font, Component.translatable("screen.hemomancy.rite_hint.ceremony_time",
				HemoText.translatable("screen.hemomancy.rite_hint.seconds", String.format("%.1f", seconds)).withStyle(s -> s.withColor(VALUE_COLOR))).withStyle(s -> s.withColor(LABEL_COLOR)), panelX, y, 0);
		y += lineH + 6;

		// ── How to perform ──
		gfx.drawString(font, Component.translatable("screen.hemomancy.rite_hint.how_to_perform").withStyle(s -> s.withColor(TITLE_COLOR)), panelX, y, 0);
		y += lineH;
		String instructions = performanceInstructions();
		for (String line : ScreenDrawUtils.wrapText(font, instructions, panelW)) {
			gfx.drawString(font, Component.literal(line)
					.withStyle(s -> s.withColor(DESC_COLOR)), panelX, y, 0);
			y += lineH;
		}
		y += 6;

		// ── Result item ──
		ItemStack result = rite.getResult();
		if (result != null && !result.isEmpty()) {
			gfx.drawString(font, Component.translatable("screen.hemomancy.rite_hint.result").withStyle(s -> s.withColor(LABEL_COLOR)), panelX, y, 0);
			y += lineH;

			gfx.renderItem(result, panelX, y);
			gfx.renderItemDecorations(font, result, panelX, y);
			List<String> resultLines = ScreenDrawUtils.wrapText(font, result.getHoverName().getString(), panelW - 20);
			for (int li = 0; li < resultLines.size(); li++) {
				int ix = li == 0 ? panelX + 20 : panelX + 4;
				gfx.drawString(font, Component.literal(resultLines.get(li))
						.withStyle(s -> s.withColor(VALUE_COLOR)), ix, y + 4 + li * lineH, 0);
			}
			y += Math.max(20, resultLines.size() * lineH + 4);
		}

		y += 6;

		// ── Materials list ──
		{
			List<MnemonicBlueprintPattern.MaterialCount> materials = previewPattern().materialCounts(false);
			if (!materials.isEmpty()) {
				gfx.drawString(font, Component.translatable("screen.hemomancy.rite_hint.materials").withStyle(s -> s.withColor(LABEL_COLOR)), panelX, y, 0);
				y += lineH;

				long cycleIndex = materialCycleIndex();
				for (MnemonicBlueprintPattern.MaterialCount material : materials) {
					MultiblockPatternKey key = material.key();
					Block block = key.displayBlock(cycleIndex);
					if (block == null || block == Blocks.AIR) continue;
					int count = material.count();

					ItemStack blockStack = materialStackFor(block);
					if (!blockStack.isEmpty()) {
						gfx.renderItem(blockStack, panelX + 2, y);
						String countPrefix = " x" + count + "  ";
						List<String> matLines = ScreenDrawUtils.wrapText(font,
								countPrefix + materialLabelFor(key, blockStack), panelW - 20);
						for (int li = 0; li < matLines.size(); li++) {
							gfx.drawString(font, Component.literal(matLines.get(li))
									.withStyle(s -> s.withColor(VALUE_COLOR)), panelX + 20, y + 4 + li * lineH, 0);
						}
						y += Math.max(18, matLines.size() * lineH + 4);
					}
				}
			}
		}

		gfx.disableScissor();

		// Scroll indicators
		if (totalH > visibleH) {
			if (infoScroll > 0) {
				gfx.drawCenteredString(font, "\u25B2", panelX + panelW / 2, clipTop, 0xAAFFFFFF);
			}
			if (infoScroll < maxScroll) {
				gfx.drawCenteredString(font, "\u25BC", panelX + panelW / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	private int measureInfoPanelHeight(int panelW) {
		if (floor != null) return measureFloorInfoPanelHeight(panelW);
		int y = 0;
		int lineH = 12;

		// Name
		String name = rite.displayName().getString();
		if (name == null || name.isEmpty()) {
			String ritePath = rite.getId().getPath();
			if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
			name = HLTextUtils.toProperCase(ritePath.replace("_", " "));
		}
		y += ScreenDrawUtils.wrapText(font, name, panelW).size() * lineH;
		y += 4 + 1 + 6;

		// Description
		String desc = rite.displayDescription().getString();
		if (desc != null && !desc.isEmpty()) {
			y += ScreenDrawUtils.wrapText(font, desc, panelW).size() * lineH + 4;
		}

		y += lineH; // type
		y += lineH; // blood cost
		y += lineH + 6; // cast time

		// Instructions
		y += lineH; // "How to Perform:"
		String instructions = performanceInstructions();
		y += ScreenDrawUtils.wrapText(font, instructions, panelW).size() * lineH + 6;

		// Result
		ItemStack result = rite.getResult();
		if (result != null && !result.isEmpty()) {
			y += lineH;
			List<String> resultLines = ScreenDrawUtils.wrapText(font, result.getHoverName().getString(), panelW - 20);
			y += Math.max(20, resultLines.size() * lineH + 4);
		}

		y += 6;

		// Materials
		{
			List<MnemonicBlueprintPattern.MaterialCount> materials = previewPattern().materialCounts(false);
			if (!materials.isEmpty()) {
				y += lineH;
				long cycleIndex = materialCycleIndex();
				for (MnemonicBlueprintPattern.MaterialCount material : materials) {
					MultiblockPatternKey key = material.key();
					Block block = key.displayBlock(cycleIndex);
					if (block == null || block == Blocks.AIR) continue;
					ItemStack blockStack = materialStackFor(block);
					if (!blockStack.isEmpty()) {
						String countPrefix = " x" + material.count() + "  ";
						List<String> matLines = ScreenDrawUtils.wrapText(font,
								countPrefix + materialLabelFor(key, blockStack), panelW - 20);
						y += Math.max(18, matLines.size() * lineH + 4);
					}
				}
			}
		}

		return y;
	}

	private MnemonicBlueprintPattern previewPattern() {
		return floor != null ? MnemonicBlueprintPattern.fromFloor(floor)
				: MnemonicBlueprintPattern.fromRite(rite);
	}

	private void drawFloorInfoPanel(GuiGraphics gfx, int panelX, int panelY, int panelW, int panelH) {
		int totalH = measureFloorInfoPanelHeight(panelW);
		int maxScroll = Math.max(0, totalH - panelH);
		infoScroll = Math.min(infoScroll, maxScroll);
		gfx.enableScissor(panelX - 2, panelY, panelX + panelW + 2, panelY + panelH);
		int y = panelY - infoScroll;
		int lineH = 12;
		Component style = CardinalRiteFloorDefinition.styleName(floor.style());
		Component tier = CardinalRiteFloorDefinition.tierName(floor.tier());
		String title = text("screen.hemomancy.rite_hint.ritual_floor", tier, style);
		for (String line : ScreenDrawUtils.wrapText(font, title, panelW)) {
			gfx.drawString(font, Component.literal(line).withStyle(s -> s.withColor(TITLE_COLOR).withBold(true)),
					panelX, y, 0);
			y += lineH;
		}
		y += 4;
		gfx.fill(panelX, y, panelX + panelW, y + 1, BORDER_INNER);
		y += 6;
		for (String line : ScreenDrawUtils.wrapText(font,
				text("screen.hemomancy.rite_hint.a_reusable_ritual_foundation"), panelW)) {
			gfx.drawString(font, Component.literal(line).withStyle(s -> s.withColor(DESC_COLOR).withItalic(true)),
					panelX, y, 0);
			y += lineH;
		}
		y += 4;
		int width = floor.pattern().getBlockPattern().getWidth();
		int height = floor.pattern().getBlockPattern().getHeight();
		int depth = floor.pattern().getBlockPattern().getDepth();
		y = drawFloorValue(gfx, panelX, y, Component.translatable("screen.hemomancy.rite_hint.style"), style);
		y = drawFloorValue(gfx, panelX, y, Component.translatable("screen.hemomancy.rite_hint.tier"), tier);
		y = drawFloorValue(gfx, panelX, y, Component.translatable("screen.hemomancy.rite_hint.dimensions"), HemoText.translatable("screen.hemomancy.rite_hint.x_x", width, height, depth));
		int space = 2 * floor.half() + 1;
		y = drawFloorValue(gfx, panelX, y, Component.translatable("screen.hemomancy.rite_hint.braziers_count_anywhere_in"),
				HemoText.translatable("screen.hemomancy.rite_hint.the_x_x_cube_the_floor", space, space, space));
		y += 6;
		gfx.drawString(font, Component.translatable("screen.hemomancy.rite_hint.how_to_build").withStyle(s -> s.withColor(TITLE_COLOR)), panelX, y, 0);
		y += lineH;
		for (String line : ScreenDrawUtils.wrapText(font,
				text("screen.hemomancy.rite_hint.build_the_displayed_floor"),
				panelW)) {
			gfx.drawString(font, Component.literal(line).withStyle(s -> s.withColor(DESC_COLOR)), panelX, y, 0);
			y += lineH;
		}
		y += 6;
		List<MnemonicBlueprintPattern.MaterialCount> materials = previewPattern().materialCounts(false);
		if (!materials.isEmpty()) {
			gfx.drawString(font, Component.translatable("screen.hemomancy.rite_hint.materials").withStyle(s -> s.withColor(LABEL_COLOR)), panelX, y, 0);
			y += lineH;
			long cycleIndex = materialCycleIndex();
			for (MnemonicBlueprintPattern.MaterialCount material : materials) {
				Block block = material.key().displayBlock(cycleIndex);
				if (block == null || block == Blocks.AIR) continue;
				ItemStack stack = materialStackFor(block);
				if (stack.isEmpty()) continue;
				gfx.renderItem(stack, panelX + 2, y);
				List<String> lines = ScreenDrawUtils.wrapText(font,
						" x" + material.count() + "  " + materialLabelFor(material.key(), stack), panelW - 20);
				for (int index = 0; index < lines.size(); index++) {
					gfx.drawString(font, Component.literal(lines.get(index)).withStyle(s -> s.withColor(VALUE_COLOR)),
							panelX + 20, y + 4 + index * lineH, 0);
				}
				y += Math.max(18, lines.size() * lineH + 4);
			}
		}
		gfx.disableScissor();
		if (totalH > panelH) {
			if (infoScroll > 0) gfx.drawCenteredString(font, "\u25B2", panelX + panelW / 2, panelY, 0xAAFFFFFF);
			if (infoScroll < maxScroll) gfx.drawCenteredString(font, "\u25BC", panelX + panelW / 2,
					panelY + panelH - 10, 0xAAFFFFFF);
		}
	}

	private int drawFloorValue(GuiGraphics gfx, int x, int y, Component label, Component value) {
		gfx.drawString(font, label.copy().withStyle(s -> s.withColor(LABEL_COLOR))
				.append(value.copy().withStyle(s -> s.withColor(ACCENT_COLOR))), x, y, 0);
		return y + 12;
	}

	private int measureFloorInfoPanelHeight(int panelW) {
		Component style = CardinalRiteFloorDefinition.styleName(floor.style());
		Component tier = CardinalRiteFloorDefinition.tierName(floor.tier());
		int height = ScreenDrawUtils.wrapText(font, text("screen.hemomancy.rite_hint.ritual_floor", tier, style), panelW).size() * 12;
		height += 4 + 1 + 6;
		height += ScreenDrawUtils.wrapText(font,
				text("screen.hemomancy.rite_hint.a_reusable_ritual_foundation"), panelW).size() * 12 + 4;
		height += 4 * 12 + 6 + 12;
		height += ScreenDrawUtils.wrapText(font,
				text("screen.hemomancy.rite_hint.build_the_displayed_floor"),
				panelW).size() * 12 + 6;
		List<MnemonicBlueprintPattern.MaterialCount> materials = previewPattern().materialCounts(false);
		if (!materials.isEmpty()) {
			height += 12;
			long cycleIndex = materialCycleIndex();
			for (MnemonicBlueprintPattern.MaterialCount material : materials) {
				Block block = material.key().displayBlock(cycleIndex);
				if (block == null || block == Blocks.AIR) continue;
				ItemStack stack = materialStackFor(block);
				if (!stack.isEmpty()) {
					height += Math.max(18, ScreenDrawUtils.wrapText(font,
							" x" + material.count() + "  " + materialLabelFor(material.key(), stack), panelW - 20)
							.size() * 12 + 4);
				}
			}
		}
		return height;
	}

	private String performanceInstructions() {
		if (rite.isUnstained()) {
			return text("screen.hemomancy.rite_hint.unstained_instructions");
		}
		String focusMode = rite.getCeremony() == null ? "" : rite.getCeremony().focusMode();
		if ("living_staff".equals(focusMode)) {
			return layeredInstructions()
					+ text("screen.hemomancy.rite_hint.shove_the_living_staff")
					+ ceremonyInstructions();
		}
		if (rite.getRequiredDegree() < 1) {
			return layeredInstructions()
					+ text("screen.hemomancy.rite_hint.right_click_with_a_sanguine_formation")
					+ ceremonyInstructions();
		}
		return layeredInstructions()
				+ text("screen.hemomancy.rite_hint.use_the_declared_focus") + ceremonyInstructions();
	}

	private Component minimumFloorName() {
		return CardinalRiteFloorRegistry.get(rite.getFloorId()).<Component>map(CardinalRiteFloorDefinition::styleAndTier)
				.orElseGet(() -> Component.literal(rite.getFloorId().getPath().replace('_', ' ')));
	}

	private String layeredInstructions() {
		if (!rite.hasLayeredStation()) {
			return text("screen.hemomancy.rite_hint.build_the_structure_shown");
		}
		if (rite.isWorkingCraft()) {
			return text("screen.hemomancy.rite_hint.working_floor_instructions", minimumFloorName())
					+ (rite.getBrazierSignature().isEmpty() ? "" : text("screen.hemomancy.rite_hint.place_the_exact_offerings"));
		}
		String structure = rite.getRequiredStructure() == null ? text("screen.hemomancy.rite_hint.no_upper_structure")
				: rite.shouldConsumeRequiredStructure()
						? text("screen.hemomancy.rite_hint.build_the_shown_upper_structure")
						: text("screen.hemomancy.rite_hint.build_the_shown_reusable_upper_structure");
		String offerings = rite.getBrazierSignature().isEmpty()
				? ""
				: text("screen.hemomancy.rite_hint.place_the_exact_offerings");
		return text("screen.hemomancy.rite_hint.build_at_least_the_floor", minimumFloorName()) + structure + offerings;
	}

	private String ceremonyInstructions() {
		if (rite.getCeremony() == null) return "";
		if (rite.isWorkingCraft()) {
			return text("screen.hemomancy.rite_hint.working_craft_ceremony", rite.getSubjectFeedMl());
		}
		int anchors = rite.getCeremony().anchors().size();
		long requiredSigils = rite.getCeremony().supportSockets().stream()
				.filter(com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition.SupportSocket::required)
				.count();
		String anchorText = anchors == 0 ? ""
				: text("screen.hemomancy.rite_hint.feed_its_anchors", anchors);
		String sigilText = rite.getCeremony().supportSockets().isEmpty() ? ""
				: requiredSigils == 0
						? text("screen.hemomancy.rite_hint.support_sigils_are_optional")
						: requiredSigils == 1 ? text("screen.hemomancy.rite_hint.awaken_the_required_support_sigil", requiredSigils)
						: text("screen.hemomancy.rite_hint.awaken_all_required_support_sigils", requiredSigils);
		String ordealText = rite.getCeremony().waves().isEmpty()
				&& rite.getCeremony().guaranteedWaves().isEmpty()
						? text("screen.hemomancy.rite_hint.no_ordeal_follows_the_seal")
						: text("screen.hemomancy.rite_hint.seal_the_daemon_then_endure");
		return anchorText + sigilText + ordealText;
	}

	private static String text(String key, Object... args) {
		return Component.translatable(key, args).getString();
	}

	// ── Layer Buttons ──

	private void drawLayerButtons(GuiGraphics gfx, int mouseX, int mouseY, int bx, int cy) {
		if (maxLayer <= 0) return;

		int bs = LAYER_BTN_SIZE;

		// Up button
		int upY = cy - bs - 14;
		boolean upHov = mouseX >= bx && mouseX <= bx + bs && mouseY >= upY && mouseY <= upY + bs;
		drawNavButton(gfx, bx, upY, bs, bs, "\u25B2", upHov);

		// Down button
		int downY = cy + 14;
		boolean downHov = mouseX >= bx && mouseX <= bx + bs && mouseY >= downY && mouseY <= downY + bs;
		drawNavButton(gfx, bx, downY, bs, bs, "\u25BC", downHov);

		// Layer indicator
		String label = visibleLayer < 0 ? text("screen.hemomancy.rite_hint.all_layers") : "Y:" + (visibleLayer + 1);
		gfx.drawCenteredString(font, label, bx + bs / 2, cy - 4, 0xFFAAAAAA);
	}

	private void drawNavButton(GuiGraphics gfx, int x, int y, int w, int h, String symbol, boolean hovered) {
		gfx.fill(x, y, x + w, y + h, hovered ? 0xCC331111 : 0x99220808);
		int bc = hovered ? 0xFF884444 : 0xFF553333;
		gfx.fill(x, y, x + w, y + 1, bc);
		gfx.fill(x, y + h - 1, x + w, y + h, bc);
		gfx.fill(x, y, x + 1, y + h, bc);
		gfx.fill(x + w - 1, y, x + w, y + h, bc);
		gfx.drawCenteredString(font, symbol, x + w / 2, y + (h - 8) / 2, hovered ? 0xFFFFFFFF : 0xFFAAAAAA);
	}

	// ══════════════════════════════════════════════════════════════
	//  Input Handling
	// ══════════════════════════════════════════════════════════════

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			// Check layer buttons
			int bx = guiLeft + GUI_WIDTH / 2 - LAYER_BTN_SIZE - 8;
			int cy = guiTop + GUI_HEIGHT / 2;

			if (maxLayer > 0) {
				int upY = cy - LAYER_BTN_SIZE - 14;
				if (mouseX >= bx && mouseX <= bx + LAYER_BTN_SIZE && mouseY >= upY && mouseY <= upY + LAYER_BTN_SIZE) {
					if (visibleLayer == -1) visibleLayer = maxLayer;
					else if (visibleLayer < maxLayer) visibleLayer++;
					else visibleLayer = -1;
					return true;
				}

				int downY = cy + 14;
				if (mouseX >= bx && mouseX <= bx + LAYER_BTN_SIZE && mouseY >= downY && mouseY <= downY + LAYER_BTN_SIZE) {
					if (visibleLayer == -1) visibleLayer = 0;
					else if (visibleLayer > 0) visibleLayer--;
					else visibleLayer = -1;
					return true;
				}
			}

			// Start drag on model area (left half)
			if (mouseX >= guiLeft && mouseX <= guiLeft + GUI_WIDTH / 2
					&& mouseY >= guiTop && mouseY <= guiTop + GUI_HEIGHT) {
				dragging = true;
				dragLastX = mouseX;
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			dragging = false;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (dragging && button == 0) {
			rotationAngle += (float) (mouseX - dragLastX) * 0.8f;
			dragLastX = mouseX;
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		// Scroll info panel (right half)
		if (mouseX > guiLeft + GUI_WIDTH / 2) {
			infoScroll -= (int) (scrollY * 12);
			if (infoScroll < 0) infoScroll = 0;
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
