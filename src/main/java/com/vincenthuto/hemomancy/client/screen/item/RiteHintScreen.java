package com.vincenthuto.hemomancy.client.screen.item;

import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;

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

/**
 * A read-only screen opened from a {@link com.vincenthuto.hemomancy.common.item.RiteHintItem}
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

	private final ResourceLocation riteId;
	private CardinalRiteRecipe rite;

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
		super(Component.translatable("item.hemomancy.rite_hint"));
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
		}
	}

	// ══════════════════════════════════════════════════════════════
	//  Rendering
	// ══════════════════════════════════════════════════════════════

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		this.renderBackground(gfx, mouseX, mouseY, partial);

		// Slowly rotate when not dragging
		if (!dragging) {
			rotationAngle += partial * 0.3f;
		}

		// ── Background panel ──
		gfx.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, BG_COLOR);
		ScreenDrawUtils.drawBorder(gfx, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, BORDER_OUTER, BORDER_INNER);

		if (rite == null) {
			gfx.drawCenteredString(font, Component.literal("Unknown Rite").withStyle(s -> s.withColor(0xFF666666)),
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
		gfx.drawCenteredString(font, "Drag to rotate",
				modelX + modelAreaW / 2, guiTop + GUI_HEIGHT - 14, 0x44888888);

		// Info panel
		drawInfoPanel(gfx, infoX, contentTop, infoW, contentH, mouseX, mouseY);

		gfx.pose().popPose();

		super.render(gfx, mouseX, mouseY, partial);
	}

	// ── 3D Multiblock Preview ──

	private void drawRiteModel(GuiGraphics gfx, int areaX, int areaY, int areaW, int areaH, float partial) {
		if (rite.getPattern() == null) return;

		List<BlockPosBlockPair> blockPairs = rite.getPattern().getBlockPosBlockList();
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
			} catch (Exception e) {
				// Silently skip blocks that can't be rendered
			}

			pose.popPose();
		}

		bufferSource.endBatch();
		pose.popPose();
	}

	// ── Info Panel ──

	private void drawInfoPanel(GuiGraphics gfx, int panelX, int panelY, int panelW, int panelH,
							   int mouseX, int mouseY) {
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
		String name = rite.getRiteName();
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
		String desc = rite.getRiteDescription();
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
		String typeStr = HLTextUtils.toProperCase(type.getSerializedName()) + " (" + type.getSize() + "x" + type.getSize() + ")";
		gfx.drawString(font, Component.literal("Type: ").withStyle(s -> s.withColor(LABEL_COLOR))
				.append(Component.literal(typeStr).withStyle(s -> s.withColor(ACCENT_COLOR))), panelX, y, 0);
		y += lineH;

		// ── Blood cost ──
		gfx.drawString(font, Component.literal("Blood Cost: ").withStyle(s -> s.withColor(LABEL_COLOR))
				.append(Component.literal((int) rite.getBloodCost() + " mL").withStyle(s -> s.withColor(COST_COLOR))), panelX, y, 0);
		y += lineH;

		// ── Cast time ──
		int ticks = type.getCastingDurationTicks();
		float seconds = ticks / 20f;
		gfx.drawString(font, Component.literal("Cast Time: ").withStyle(s -> s.withColor(LABEL_COLOR))
				.append(Component.literal(String.format("%.1fs", seconds)).withStyle(s -> s.withColor(VALUE_COLOR))), panelX, y, 0);
		y += lineH + 6;

		// ── How to perform ──
		gfx.drawString(font, Component.literal("How to Perform:").withStyle(s -> s.withColor(TITLE_COLOR)), panelX, y, 0);
		y += lineH;
		String instructions = "Build the structure shown on the left using the required materials. "
				+ "Stand at the center and press the Blood Craft keybind (default: V) while looking at any block in the pattern.";
		for (String line : ScreenDrawUtils.wrapText(font, instructions, panelW)) {
			gfx.drawString(font, Component.literal(line)
					.withStyle(s -> s.withColor(DESC_COLOR)), panelX, y, 0);
			y += lineH;
		}
		y += 6;

		// ── Result item ──
		ItemStack result = rite.getResult();
		if (result != null && !result.isEmpty()) {
			gfx.drawString(font, Component.literal("Result:").withStyle(s -> s.withColor(LABEL_COLOR)), panelX, y, 0);
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
		if (rite.getPattern() != null) {
			Map<Block, Integer> blockCounts = rite.getPattern().getBlockCount(false);
			if (!blockCounts.isEmpty()) {
				gfx.drawString(font, Component.literal("Materials:").withStyle(s -> s.withColor(LABEL_COLOR)), panelX, y, 0);
				y += lineH;

				for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
					Block block = entry.getKey();
					if (block == null || block == Blocks.AIR) continue;
					int count = entry.getValue();

					ItemStack blockStack = new ItemStack(block);
					if (!blockStack.isEmpty()) {
						gfx.renderItem(blockStack, panelX + 2, y);
						String countPrefix = " x" + count + "  ";
						List<String> matLines = ScreenDrawUtils.wrapText(font,
								countPrefix + blockStack.getHoverName().getString(), panelW - 20);
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
		int y = 0;
		int lineH = 12;

		// Name
		String name = rite.getRiteName();
		if (name == null || name.isEmpty()) {
			String ritePath = rite.getId().getPath();
			if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
			name = HLTextUtils.toProperCase(ritePath.replace("_", " "));
		}
		y += ScreenDrawUtils.wrapText(font, name, panelW).size() * lineH;
		y += 4 + 1 + 6;

		// Description
		String desc = rite.getRiteDescription();
		if (desc != null && !desc.isEmpty()) {
			y += ScreenDrawUtils.wrapText(font, desc, panelW).size() * lineH + 4;
		}

		y += lineH; // type
		y += lineH; // blood cost
		y += lineH + 6; // cast time

		// Instructions
		y += lineH; // "How to Perform:"
		String instructions = "Build the structure shown on the left using the required materials. "
				+ "Stand at the center and press the Blood Craft keybind (default: V) while looking at any block in the pattern.";
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
		if (rite.getPattern() != null) {
			Map<Block, Integer> blockCounts = rite.getPattern().getBlockCount(false);
			if (!blockCounts.isEmpty()) {
				y += lineH;
				for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
					Block block = entry.getKey();
					if (block == null || block == Blocks.AIR) continue;
					ItemStack blockStack = new ItemStack(block);
					if (!blockStack.isEmpty()) {
						String countPrefix = " x" + entry.getValue() + "  ";
						List<String> matLines = ScreenDrawUtils.wrapText(font,
								countPrefix + blockStack.getHoverName().getString(), panelW - 20);
						y += Math.max(18, matLines.size() * lineH + 4);
					}
				}
			}
		}

		return y;
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
		String label = visibleLayer < 0 ? "All" : "Y:" + (visibleLayer + 1);
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
