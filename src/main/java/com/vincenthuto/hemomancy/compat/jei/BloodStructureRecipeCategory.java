package com.vincenthuto.hemomancy.compat.jei;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;
import com.vincenthuto.hutoslib.math.MultiblockPattern.MaterialCount;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import com.vincenthuto.hutoslib.math.Quaternion;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * JEI recipe category for Blood Structure crafting — fully programmatic rendering.
 * Shows a rotating 3D preview of the multiblock, input slots for the held item,
 * hit block, and required structure blocks, plus the output and blood cost.
 */
public class BloodStructureRecipeCategory implements IRecipeCategory<BloodStructureRecipe> {

	// ── Layout constants ──
	private static final int BG_W = 150;
	private static final int BG_H = 110;

	// Preview area (3D multiblock render)
	private static final int PREVIEW_X = 27;
	private static final int PREVIEW_Y = 14;
	private static final int PREVIEW_W = 70;
	private static final int PREVIEW_H = 70;

	// Slot positions
	private static final int INPUT_SLOT_X = 5;
	private static final int HELD_ITEM_SLOT_Y = 21;
	private static final int HIT_BLOCK_SLOT_Y = 41;
	private static final int PATTERN_SLOT_START_Y = 61;
	private static final int PATTERN_SLOT_SPACING = 20;
	private static final int MAX_PATTERN_SLOTS = 2;
	private static final int OUTPUT_SLOT_X = 115;
	private static final int OUTPUT_SLOT_Y = 41;

	// Colors
	private static final int BG_COLOR = 0xFF0A0204;
	private static final int PREVIEW_BG = 0xFF1A0808;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
	private static final int COST_COLOR = 0xFFAA0000;
	private static final int LABEL_COLOR = 0xFF888888;

	private final IDrawable background;
	private final IDrawable icon;

	public BloodStructureRecipeCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createBlankDrawable(BG_W, BG_H);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(ItemInit.sanguine_formation.get()));
	}

	// ═══════════════════════════════════════════════════════════════
	//  IRecipeCategory overrides
	// ═══════════════════════════════════════════════════════════════

	@SuppressWarnings("removal")
	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public RecipeType<BloodStructureRecipe> getRecipeType() {
		return JEIPlugin.blood_structure_recipe_type;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.translatable("hemomancy.jei.blood_crafting");
	}
	private float animTime = 0f;

	// ═══════════════════════════════════════════════════════════════
	//  Drawing
	// ═══════════════════════════════════════════════════════════════

	@Override
	public void draw(BloodStructureRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx,
			double mouseX, double mouseY) {

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		// ── Dark background with subtle glow ──
		gfx.fill(0, 0, BG_W, BG_H, BG_COLOR);
		drawSubtleGlow(gfx, BG_W / 2, BG_H / 2, time);

		// ── Outer border ──
		drawBorder(gfx, 0, 0, BG_W, BG_H, BORDER_OUTER, BORDER_INNER);

		// ── Preview area border ──
		drawBorder(gfx, PREVIEW_X - 1, PREVIEW_Y - 1, PREVIEW_W + 2, PREVIEW_H + 2,
				BORDER_INNER, BORDER_OUTER);

		// ── Slot frames ──
		drawSlotFrame(gfx, INPUT_SLOT_X - 1, HELD_ITEM_SLOT_Y - 1);
		drawSlotFrame(gfx, INPUT_SLOT_X - 1, HIT_BLOCK_SLOT_Y - 1);
		drawSlotFrame(gfx, OUTPUT_SLOT_X - 1, OUTPUT_SLOT_Y - 1);

		// Pattern block slot frames
		if (recipe.getPattern() != null && recipe.getPattern().getSymbolList() != null) {
			int slotCount = 0;
			for (MaterialCount material : recipe.getPattern().getMaterialCounts(false)) {
				if (slotCount >= MAX_PATTERN_SLOTS) {
					break;
				}
				if (!itemStacksFor(material.key(), material.count()).isEmpty()) {
					drawSlotFrame(gfx, INPUT_SLOT_X - 1,
							PATTERN_SLOT_START_Y + (slotCount * PATTERN_SLOT_SPACING) - 1);
					slotCount++;
				}
			}
		}

		// ── Arrow indicator ──
		drawArrow(gfx, 100, OUTPUT_SLOT_Y + 4, time);

		// ── Labels ──
		Font font = Minecraft.getInstance().font;

		MutableComponent costText = Component.literal("Blood: " + (int) recipe.getBloodCost());
		int costWidth = font.width(costText);
		gfx.drawString(font, costText, BG_W - costWidth - 3, 3, COST_COLOR, false);

		gfx.drawString(font, Component.literal("In"), INPUT_SLOT_X, HELD_ITEM_SLOT_Y - 10, LABEL_COLOR, false);
		gfx.drawString(font, Component.literal("Out"), OUTPUT_SLOT_X, OUTPUT_SLOT_Y - 10, LABEL_COLOR, false);

		// ── 3D multiblock preview ──
		render3DPreview(recipe, gfx);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BloodStructureRecipe recipe, IFocusGroup focuses) {
		// Held item
		builder.addSlot(RecipeIngredientRole.INPUT, INPUT_SLOT_X, HELD_ITEM_SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getHeldItem());

		// Hit block
		builder.addSlot(RecipeIngredientRole.INPUT, INPUT_SLOT_X, HIT_BLOCK_SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(recipe.getHitBlock().asItem()));

		// Pattern block inputs (deduplicated with counts)
		if (recipe.getPattern() != null && recipe.getPattern().getSymbolList() != null) {
			int slotIndex = 0;
			for (MaterialCount material : recipe.getPattern().getMaterialCounts(false)) {
				if (slotIndex >= MAX_PATTERN_SLOTS) {
					break;
				}
				List<ItemStack> stacks = itemStacksFor(material.key(), material.count());
				if (!stacks.isEmpty()) {
					int slotY = PATTERN_SLOT_START_Y + (slotIndex * PATTERN_SLOT_SPACING);
					builder.addSlot(RecipeIngredientRole.INPUT, INPUT_SLOT_X, slotY)
							.addIngredients(VanillaTypes.ITEM_STACK, stacks);
					slotIndex++;
				}
			}
		}

		// Result
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResult());
	}

	// ═══════════════════════════════════════════════════════════════
	//  3D multiblock preview
	// ═══════════════════════════════════════════════════════════════

	private void render3DPreview(BloodStructureRecipe recipe, GuiGraphics gfx) {
		if (recipe.getPattern() == null) return;

		try {
			gfx.fill(PREVIEW_X, PREVIEW_Y, PREVIEW_X + PREVIEW_W, PREVIEW_Y + PREVIEW_H, PREVIEW_BG);

			MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
			double scale = Minecraft.getInstance().getWindow().getGuiScale();
			Matrix4f matrix = gfx.pose().last().pose();
			FloatBuffer buf = BufferUtils.createFloatBuffer(16);
			matrix.get(buf);
			Lighting.setupLevel();

			Vec3 translation = new Vec3(buf.get(12) * scale, buf.get(13) * scale, buf.get(14) * scale);

			// Scissor rect in screen-space pixels
			int scaledX = (int) (PREVIEW_X * scale);
			int scaledY = (int) (PREVIEW_Y * scale);
			int scaledW = (int) (PREVIEW_W * scale);
			int scaledH = (int) (PREVIEW_H * scale);
			int scissorX = Math.round(Math.round(translation.x + scaledX));
			int scissorY = Math.round(Math.round(
					Minecraft.getInstance().getWindow().getHeight() - scaledY - scaledH - translation.y));

			RenderSystem.enableScissor(scissorX, scissorY, Math.round(scaledW), Math.round(scaledH));

			gfx.pose().pushPose();
			gfx.pose().translate(
					PREVIEW_X + PREVIEW_W / 2.0,
					PREVIEW_Y + PREVIEW_H / 2.0,
					100);

			// Scale based on structure dimensions
			int patW = recipe.getPattern().getBlockPattern().getWidth();
			int patH = recipe.getPattern().getBlockPattern().getHeight();
			int patD = recipe.getPattern().getBlockPattern().getDepth();
			float avgDim = (float) Math.sqrt(patW * patW + patH * patH + patD * patD);
			float previewScale = (float) ((2 + Math.exp(2 - (avgDim / 5))) / 1.5);
			gfx.pose().scale(previewScale, -previewScale, previewScale);

			drawRotatingMultiblock(recipe, gfx, buffers);

			gfx.pose().popPose();
			buffers.endBatch();
			RenderSystem.disableScissor();
		} catch (Exception ex) {
			Hemomancy.LOGGER.warn("Error rendering blood structure preview", ex);
		}
	}

	private void drawRotatingMultiblock(BloodStructureRecipe recipe, GuiGraphics gfx,
			MultiBufferSource.BufferSource buffers) {

		double rotation = Math.toDegrees(HLClientUtils.getWorld().getGameTime()) / 15.0;
		gfx.pose().mulPose(new Quaternion(35f, (float) rotation, 0, true).toMoj());
		gfx.pose().mulPose(new Quaternion(0, 35f, 0, true).toMoj());

		int patW = recipe.getPattern().getBlockPattern().getWidth();
		int patH = recipe.getPattern().getBlockPattern().getHeight();
		int patD = recipe.getPattern().getBlockPattern().getDepth();
		double explodeSpacing = 1.5;

		for (BlockPosBlockPair pair : recipe.getPattern().getDisplayBlockPosBlockList(materialCycleIndex())) {
			if (pair.getPos().getY() >= patH) continue;
			if (pair.getBlock() == null || pair.getBlock() == Blocks.AIR) continue;

			gfx.pose().pushPose();
			gfx.pose().translate(
					explodeSpacing * (pair.getPos().getX() - patW / 2) - 0.5,
					explodeSpacing * (pair.getPos().getY() - patH / 2) - 0.5,
					explodeSpacing * (pair.getPos().getZ() - patD / 2) - 0.5);

			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
					pair.getBlock().defaultBlockState(), gfx.pose(), buffers,
					15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
					RenderType.cutoutMipped());
			gfx.pose().popPose();
		}
	}

	// ═══════════════════════════════════════════════════════════════
	//  Drawing helpers
	// ═══════════════════════════════════════════════════════════════

	private long materialCycleIndex() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level != null) {
			return minecraft.level.getGameTime() / 40L;
		}
		return System.currentTimeMillis() / 2000L;
	}

	private List<ItemStack> itemStacksFor(MultiblockPatternKey key, int count) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Block block : key.displayBlocks()) {
			if (block == null || block == Blocks.AIR) {
				continue;
			}
			ItemStack stack = new ItemStack(block.asItem(), count);
			if (!stack.isEmpty()) {
				stacks.add(stack);
			}
		}
		return stacks;
	}

	/** Draws a subtle pulsing red glow in the background. */
	private void drawSubtleGlow(GuiGraphics gfx, int cx, int cy, float time) {
		float pulse = 0.5f + 0.5f * Mth.sin(time * 1.8f);
		for (int ring = 35; ring > 0; ring -= 5) {
			int alpha = (int) (6 * pulse * ((float) ring / 35));
			if (alpha <= 0) continue;
			int color = (alpha << 24) | (0x60 << 16) | (0x08 << 8) | 0x04;
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}
	}

	/** Draws a beveled border rectangle. */
	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h, int light, int dark) {
		gfx.fill(x, y, x + w, y + 1, light);
		gfx.fill(x, y + h - 1, x + w, y + h, dark);
		gfx.fill(x, y, x + 1, y + h, light);
		gfx.fill(x + w - 1, y, x + w, y + h, dark);
	}

	/** Draws an 18x18 item slot frame. */
	private void drawSlotFrame(GuiGraphics gfx, int x, int y) {
		gfx.fill(x, y, x + 18, y + 18, SLOT_BG);
		gfx.fill(x, y, x + 18, y + 1, SLOT_BORDER_DARK);
		gfx.fill(x, y, x + 1, y + 18, SLOT_BORDER_DARK);
		gfx.fill(x, y + 17, x + 18, y + 18, SLOT_BORDER_LIGHT);
		gfx.fill(x + 17, y, x + 18, y + 18, SLOT_BORDER_LIGHT);
	}

	/** Draws a pulsing arrow between preview and output. */
	private void drawArrow(GuiGraphics gfx, int x, int y, float time) {
		float pulse = 0.4f + 0.6f * Mth.sin(time * 3f);
		int alpha = (int) (180 * pulse);
		int color = (alpha << 24) | (0xAA << 16) | (0x10 << 8) | 0x08;
		gfx.fill(x, y + 3, x + 8, y + 4, color);
		gfx.fill(x + 6, y + 1, x + 7, y + 6, color);
		gfx.fill(x + 7, y + 2, x + 8, y + 5, color);
		gfx.fill(x + 8, y + 3, x + 9, y + 4, color);
	}
}
