package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodStructureHintItem;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
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
 * A read-only screen opened from a
 * {@link BloodStructureHintItem} that
 * displays a single blood structure recipe's 3D multiblock layout and info
 * panel — analogous to {@link RiteHintScreen} but for blood structure recipes
 * rather than cardinal rite recipes.
 * <p>
 * The screen is styled as a weathered fragment of Harbinger parchment.
 */
public class BloodStructureHintScreen extends Screen {

	// ── Appearance constants ──
	private static final int BG_COLOR = 0xEE0A1208;
	private static final int BORDER_OUTER = 0xFF104A18;
	private static final int BORDER_INNER = 0xFF103318;
	private static final int TITLE_COLOR = 0xFF44CC66;
	private static final int DESC_COLOR = 0xFF999999;
	private static final int LABEL_COLOR = 0xFF888888;
	private static final int VALUE_COLOR = 0xFFAAAAAA;
	private static final int COST_COLOR = 0xFFAA4444;
	private static final int ACCENT_COLOR = 0xFF88CCBB;

	// ── Layout ──
	private static final int GUI_WIDTH = 340;
	private static final int GUI_HEIGHT = 220;
	private static final int LAYER_BTN_SIZE = 16;
	private static final long MATERIAL_CYCLE_MILLIS = 2000L;

	private final ResourceLocation structureId;
	private BloodStructureRecipe structure;

	// 3D model state
	private float rotationAngle = 45f;
	private int visibleLayer = -1;
	private int maxLayer = 0;
	private boolean dragging = false;
	private double dragLastX;

	// Info panel scroll
	private int infoScroll = 0;

	// Computed layout
	private int guiLeft, guiTop;

	private BloodStructureHintScreen(ResourceLocation structureId) {
		super(Component.translatable("item.hemomancy.blood_structure_hint"));
		this.structureId = structureId;
	}

	/**
	 * Opens the blood structure hint screen for the given recipe.
	 * Called client-side only.
	 */
	public static void open(ResourceLocation structureId) {
		Minecraft.getInstance().setScreen(new BloodStructureHintScreen(structureId));
	}

	@Override
	protected void init() {
		super.init();
		this.guiLeft = (this.width - GUI_WIDTH) / 2;
		this.guiTop = (this.height - GUI_HEIGHT) / 2;

		if (minecraft != null && minecraft.level != null) {
			this.structure = BloodStructureRecipe.getStructureByLocation(minecraft.level, structureId);
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

		if (!dragging) {
			rotationAngle += partial * 0.3f;
		}

		// ── Background panel ──
		gfx.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, BG_COLOR);
		ScreenDrawUtils.drawBorder(gfx, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, BORDER_OUTER, BORDER_INNER);

		if (structure == null) {
			gfx.drawCenteredString(font,
					Component.literal("Unknown Structure").withStyle(s -> s.withColor(0xFF666666)),
					guiLeft + GUI_WIDTH / 2, guiTop + GUI_HEIGHT / 2 - 4, 0);
			super.render(gfx, mouseX, mouseY, partial);
			return;
		}

		int modelAreaW = GUI_WIDTH / 2;
		int modelX = guiLeft;
		int infoX = guiLeft + modelAreaW + 6;
		int infoW = GUI_WIDTH - modelAreaW - 16;
		int contentTop = guiTop + 6;
		int contentH = GUI_HEIGHT - 12;

		// ── 3D model ──
		drawStructureModel(gfx, modelX + 6, contentTop, modelAreaW - 12, contentH, partial);

		// ── 2D overlays ──
		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);

		drawLayerButtons(gfx, mouseX, mouseY, modelX + modelAreaW - LAYER_BTN_SIZE - 8, guiTop + GUI_HEIGHT / 2);

		gfx.drawCenteredString(font, "Drag to rotate",
				modelX + modelAreaW / 2, guiTop + GUI_HEIGHT - 14, 0x44888888);

		drawInfoPanel(gfx, infoX, contentTop, infoW, contentH, mouseX, mouseY);

		gfx.pose().popPose();

		super.render(gfx, mouseX, mouseY, partial);
	}

	// ── 3D Multiblock Preview ──

	private void drawStructureModel(GuiGraphics gfx, int areaX, int areaY, int areaW, int areaH, float partial) {
		if (structure.getPattern() == null) return;

		List<BlockPosBlockPair> blockPairs = structure.getPattern().getDisplayBlockPosBlockList(materialCycleIndex());
		if (blockPairs.isEmpty()) return;

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
				// Silently skip blocks that cannot be rendered
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

	private String materialLabelFor(MultiblockPatternKey key, ItemStack currentStack) {
		if (key.isTag()) {
			return key.displayLabel() + " (" + currentStack.getHoverName().getString() + ")";
		}
		return currentStack.getHoverName().getString();
	}

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

		// ── Structure name (derived from result item) ──
		String name = deriveStructureName();
		for (String titleLine : ScreenDrawUtils.wrapText(font, name, panelW)) {
			gfx.drawString(font, Component.literal(titleLine)
					.withStyle(s -> s.withColor(TITLE_COLOR).withBold(true)), panelX, y, 0);
			y += lineH;
		}
		y += 4;

		// ── Separator ──
		gfx.fill(panelX, y, panelX + panelW, y + 1, BORDER_INNER);
		y += 6;

		// ── Blood cost ──
		gfx.drawString(font, Component.literal("Blood Cost: ").withStyle(s -> s.withColor(LABEL_COLOR))
				.append(Component.literal((int) structure.getBloodCost() + " mL").withStyle(s -> s.withColor(COST_COLOR))),
				panelX, y, 0);
		y += lineH;

		// ── Held item ──
		ItemStack heldItem = structure.getHeldItem();
		if (heldItem != null && !heldItem.isEmpty()) {
			gfx.drawString(font, Component.literal("Hold in Hand:").withStyle(s -> s.withColor(LABEL_COLOR)),
					panelX, y, 0);
			y += lineH;
			gfx.renderItem(heldItem, panelX, y);
			gfx.renderItemDecorations(font, heldItem, panelX, y);
			List<String> heldLines = ScreenDrawUtils.wrapText(font, heldItem.getHoverName().getString(), panelW - 20);
			for (int li = 0; li < heldLines.size(); li++) {
				int ix = li == 0 ? panelX + 20 : panelX + 4;
				gfx.drawString(font, Component.literal(heldLines.get(li))
						.withStyle(s -> s.withColor(VALUE_COLOR)), ix, y + 4 + li * lineH, 0);
			}
			y += Math.max(20, heldLines.size() * lineH + 4);
		}

		// ── Hit block ──
		Block hitBlock = structure.getHitBlock();
		if (hitBlock != null && hitBlock != Blocks.AIR) {
			gfx.drawString(font, Component.literal("Activate Block:").withStyle(s -> s.withColor(LABEL_COLOR)),
					panelX, y, 0);
			y += lineH;
			ItemStack hitStack = new ItemStack(hitBlock);
			gfx.renderItem(hitStack, panelX, y);
			gfx.renderItemDecorations(font, hitStack, panelX, y);
			List<String> hitLines = ScreenDrawUtils.wrapText(font, hitStack.getHoverName().getString(), panelW - 20);
			for (int li = 0; li < hitLines.size(); li++) {
				int ix = li == 0 ? panelX + 20 : panelX + 4;
				gfx.drawString(font, Component.literal(hitLines.get(li))
						.withStyle(s -> s.withColor(VALUE_COLOR)), ix, y + 4 + li * lineH, 0);
			}
			y += Math.max(20, hitLines.size() * lineH + 4);
		}

		// ── Instructions ──
		y += 4;
		gfx.drawString(font, Component.literal("How to Perform:").withStyle(s -> s.withColor(TITLE_COLOR)),
				panelX, y, 0);
		y += lineH;
		String instructions = structure.isUnstained()
				? "Build the structure shown on the left. "
						+ "Hold the required item, look at the activate block in the pattern, "
						+ "then press the Blood Craft keybind (default: V)."
				: "Build the structure shown on the left. "
						+ "Equip Blood Projection, hold the required catalyst in your offhand, "
						+ "then project into the activate block until the formation is filled.";
		for (String line : ScreenDrawUtils.wrapText(font, instructions, panelW)) {
			gfx.drawString(font, Component.literal(line)
					.withStyle(s -> s.withColor(DESC_COLOR)), panelX, y, 0);
			y += lineH;
		}
		y += 6;

		// ── Result item ──
		ItemStack result = structure.getResult();
		if (result != null && !result.isEmpty()) {
			gfx.drawString(font, Component.literal("Result:").withStyle(s -> s.withColor(LABEL_COLOR)),
					panelX, y, 0);
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

		y += 4;

		// ── Materials list ──
		if (structure.getPattern() != null) {
			List<MultiblockPattern.MaterialCount> materials = structure.getPattern().getMaterialCounts(false);
			if (!materials.isEmpty()) {
				gfx.drawString(font, Component.literal("Materials:").withStyle(s -> s.withColor(LABEL_COLOR)),
						panelX, y, 0);
				y += lineH;

				long cycleIndex = materialCycleIndex();
				for (MultiblockPattern.MaterialCount material : materials) {
					MultiblockPatternKey key = material.key();
					Block block = key.displayBlock(cycleIndex);
					if (block == null || block == Blocks.AIR) continue;
					int count = material.count();
					ItemStack blockStack = new ItemStack(block);
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

		String name = deriveStructureName();
		y += ScreenDrawUtils.wrapText(font, name, panelW).size() * lineH + 4 + 1 + 6;

		// Blood cost
		y += lineH;

		// Held item
		ItemStack heldItem = structure.getHeldItem();
		if (heldItem != null && !heldItem.isEmpty()) {
			y += lineH;
			List<String> heldLines = ScreenDrawUtils.wrapText(font, heldItem.getHoverName().getString(), panelW - 20);
			y += Math.max(20, heldLines.size() * lineH + 4);
		}

		// Hit block
		Block hitBlock = structure.getHitBlock();
		if (hitBlock != null && hitBlock != Blocks.AIR) {
			y += lineH;
			ItemStack hitStack = new ItemStack(hitBlock);
			List<String> hitLines = ScreenDrawUtils.wrapText(font, hitStack.getHoverName().getString(), panelW - 20);
			y += Math.max(20, hitLines.size() * lineH + 4);
		}

		// Instructions
		y += 4 + lineH;
		String instructions = structure.isUnstained()
				? "Build the structure shown on the left. "
						+ "Hold the required item, look at the activate block in the pattern, "
						+ "then press the Blood Craft keybind (default: V)."
				: "Build the structure shown on the left. "
						+ "Equip Blood Projection, hold the required catalyst in your offhand, "
						+ "then project into the activate block until the formation is filled.";
		y += ScreenDrawUtils.wrapText(font, instructions, panelW).size() * lineH + 6;

		// Result
		ItemStack result = structure.getResult();
		if (result != null && !result.isEmpty()) {
			y += lineH;
			List<String> resultLines = ScreenDrawUtils.wrapText(font, result.getHoverName().getString(), panelW - 20);
			y += Math.max(20, resultLines.size() * lineH + 4);
		}

		y += 4;

		// Materials
		if (structure.getPattern() != null) {
			List<MultiblockPattern.MaterialCount> materials = structure.getPattern().getMaterialCounts(false);
			if (!materials.isEmpty()) {
				y += lineH;
				long cycleIndex = materialCycleIndex();
				for (MultiblockPattern.MaterialCount material : materials) {
					MultiblockPatternKey key = material.key();
					Block block = key.displayBlock(cycleIndex);
					if (block == null || block == Blocks.AIR) continue;
					ItemStack blockStack = new ItemStack(block);
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

	/** Derives a display name from the result item, falling back to the recipe ID path. */
	private String deriveStructureName() {
		ItemStack result = structure.getResult();
		if (result != null && !result.isEmpty()) {
			return result.getHoverName().getString();
		}
		String path = structureId.getPath();
		if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
		return HLTextUtils.toProperCase(path.replace("_", " "));
	}

	// ── Layer Buttons ──

	private void drawLayerButtons(GuiGraphics gfx, int mouseX, int mouseY, int bx, int cy) {
		if (maxLayer <= 0) return;

		int bs = LAYER_BTN_SIZE;

		int upY = cy - bs - 14;
		boolean upHov = mouseX >= bx && mouseX <= bx + bs && mouseY >= upY && mouseY <= upY + bs;
		drawNavButton(gfx, bx, upY, bs, bs, "\u25B2", upHov);

		int downY = cy + 14;
		boolean downHov = mouseX >= bx && mouseX <= bx + bs && mouseY >= downY && mouseY <= downY + bs;
		drawNavButton(gfx, bx, downY, bs, bs, "\u25BC", downHov);

		String label = visibleLayer < 0 ? "All" : "Y:" + (visibleLayer + 1);
		gfx.drawCenteredString(font, label, bx + bs / 2, cy - 4, 0xFFAAAAAA);
	}

	private void drawNavButton(GuiGraphics gfx, int x, int y, int w, int h, String symbol, boolean hovered) {
		gfx.fill(x, y, x + w, y + h, hovered ? 0xCC113111 : 0x99082008);
		int bc = hovered ? 0xFF448844 : 0xFF335533;
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
