package com.vincenthuto.hemomancy.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;

/**
 * JEI recipe category for the Somatic Loom.
 * Displays tendency requirements as fractal lines radiating from a center,
 * with enzyme icons at each spoke and input/output slots.
 */
public class MemoryWeavingRecipeCategory implements IRecipeCategory<MemoryWeavingRecipe> {

	// ── Layout constants ──
	private static final int BG_W = 170;
	private static final int BG_H = 100;

	// Tendency wheel center (relative to recipe area)
	private static final int WHEEL_CENTER_X = 94;
	private static final int WHEEL_CENTER_Y = 52;

	// Slot positions
	private static final int MEMORY_SLOT_X = 5;
	private static final int MEMORY_SLOT_Y = 11;
	private static final int CATALYST_SLOT_X = 23;
	private static final int CATALYST_SLOT_Y = 11;
	private static final int OUTPUT_SLOT_X = 149;
	private static final int OUTPUT_SLOT_Y = 77;

	// Tendency wheel geometry
	private static final float START_ANGLE = -90f;
	private static final int ICON_DIAMETER = 70;
	private static final int SPOKE_BASE_RADIUS = 15;
	private static final float SPOKE_BASE_HALF_ANGLE = 23.5f;
	private static final int ITEM_SIZE = 16;
	private static final int Z_LEVEL = 10;

	// Colors
	private static final int BG_COLOR = 0xFF0A0204;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
	private static final int COST_COLOR = 0xFFAA0000;
	private static final int LABEL_COLOR = 0xFF888888;

	/** Greater-than-or-equal-to symbol shown on minimum tendency/cost labels. */
	private static final String GTE = "\u2265";

	/** Fixed spoke length for required tendencies in the JEI wheel. */
	private static final double REQUIRED_SPOKE_LENGTH = 1.0;

	/** Scale factor for tendency labels next to enzyme icons. */
	private static final float LABEL_SCALE = 0.7f;
	/** Vertical offset for tendency labels relative to icon center. */
	private static final int LABEL_Y_OFFSET = -3;

	private final IDrawable background;
	private final IDrawable icon;

	public MemoryWeavingRecipeCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createBlankDrawable(BG_W, BG_H);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(BlockInit.somatic_loom.get()));
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
	public RecipeType<MemoryWeavingRecipe> getRecipeType() {
		return JEIPlugin.memory_weaving_type;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.translatable("hemomancy.jei.memory_weaving");
	}

	// ═══════════════════════════════════════════════════════════════
	//  Drawing
	// ═══════════════════════════════════════════════════════════════
	private float animTime = 0f;

	@Override
	public void draw(MemoryWeavingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx,
			double mouseX, double mouseY) {

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		// ── Dark background with subtle glow ──
		gfx.fill(0, 0, BG_W, BG_H, BG_COLOR);
		drawSubtleGlow(gfx, WHEEL_CENTER_X, WHEEL_CENTER_Y, time);

		// ── Outer border ──
		drawBorder(gfx, 0, 0, BG_W, BG_H, BORDER_OUTER, BORDER_INNER);

		// ── Slot frames ──
		drawSlotFrame(gfx, MEMORY_SLOT_X - 1, MEMORY_SLOT_Y - 1);
		drawSlotFrame(gfx, CATALYST_SLOT_X - 1, CATALYST_SLOT_Y - 1);
		drawSlotFrame(gfx, OUTPUT_SLOT_X - 1, OUTPUT_SLOT_Y - 1);

		// ── Labels ──
		Font font = Minecraft.getInstance().font;

		// Blood cost — top right
		MutableComponent costText = Component.literal("Blood: " + (int) recipe.getBloodCost());
		int costWidth = font.width(costText);
		gfx.drawString(font, costText, BG_W - costWidth - 3, 3, COST_COLOR, false);

		// Slot labels
		gfx.drawString(font, Component.literal("In"), MEMORY_SLOT_X, MEMORY_SLOT_Y - 10, LABEL_COLOR, false);
		gfx.drawString(font, Component.literal("Out"), OUTPUT_SLOT_X, OUTPUT_SLOT_Y - 10, LABEL_COLOR, false);

		// ── Tendency fractal wheel ──
		drawTendencyWheel(gfx, recipe.getTendency());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, MemoryWeavingRecipe recipe, IFocusGroup focuses) {
		// Hematic Memory — always required
		builder.addSlot(RecipeIngredientRole.INPUT, MEMORY_SLOT_X, MEMORY_SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemInit.hematic_memory.get()));

		// Catalyst ingredient — show if recipe has a non-empty ingredient
		for (Ingredient ingr : recipe.getIngredients()) {
			if (ingr != null && ingr != Ingredient.EMPTY && ingr.getItems().length > 0) {
				builder.addSlot(RecipeIngredientRole.INPUT, CATALYST_SLOT_X, CATALYST_SLOT_Y)
						.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(ingr.getItems()));
				break;
			}
		}

		// Result output
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItem(null));
	}

	// ═══════════════════════════════════════════════════════════════
	//  Tendency fractal wheel
	// ═══════════════════════════════════════════════════════════════

	/**
	 * Draws the tendency wheel: fractal lines radiating from center,
	 * with enzyme item icons at each spoke tip.
	 * Must undo JEI's pose translation to draw in absolute screen coords
	 * since HLGuiUtils.fracLine works in screen space.
	 */
	private void drawTendencyWheel(GuiGraphics gfx, Map<EnumBloodTendency, Float> tendencies) {
		PoseStack ms = gfx.pose();
		Matrix4f mat = ms.last().pose();

		// Extract JEI's translation (recipe area top-left in screen coords)
		float recipeAreaX = mat.m30();
		float recipeAreaY = mat.m31();

		// Absolute screen position of the wheel center
		int absCenterX = (int) recipeAreaX + WHEEL_CENTER_X;
		int absCenterY = (int) recipeAreaY + WHEEL_CENTER_Y;

		// Undo JEI's translation so we draw with raw screen coords
		ms.pushPose();
		ms.translate(-recipeAreaX, -recipeAreaY, 0);

		float angle = START_ANGLE;
		int halfItem = ITEM_SIZE / 2;

		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			boolean required = tendencies.getOrDefault(tend, 0f) > 0f;

			// Base spoke endpoints
			double baseAngleRad1 = Math.toRadians(angle + SPOKE_BASE_HALF_ANGLE);
			double baseAngleRad2 = Math.toRadians(angle - SPOKE_BASE_HALF_ANGLE);
			int cx1 = absCenterX + (int) (Math.cos(baseAngleRad1) * SPOKE_BASE_RADIUS);
			int cy1 = absCenterY + (int) (Math.sin(baseAngleRad1) * SPOKE_BASE_RADIUS);
			int cx2 = absCenterX + (int) (Math.cos(baseAngleRad2) * SPOKE_BASE_RADIUS);
			int cy2 = absCenterY + (int) (Math.sin(baseAngleRad2) * SPOKE_BASE_RADIUS);

			// Spoke tip — fixed length for required tendencies, base only for non-required
			double spokeAngleRad = Math.toRadians(angle);
			double spokeValue = required ? REQUIRED_SPOKE_LENGTH : 0;
			double tipDist = (ICON_DIAMETER - SPOKE_BASE_RADIUS) * spokeValue * 0.2 + SPOKE_BASE_RADIUS;
			int tipX = absCenterX + (int) (Math.cos(spokeAngleRad) * tipDist);
			int tipY = absCenterY + (int) (Math.sin(spokeAngleRad) * tipDist);

			// Fractal displacement magnitude
			int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2)
					+ Math.max(cy1, cy2) - Math.min(cy1, cy2)) / 2f);

			// Draw fractal lines (two per edge for visual depth)
			HLGuiUtils.fracLine(ms, tipX, tipY, cx1, cy1, Z_LEVEL, tend.getColor(), displace, 1.1);
			HLGuiUtils.fracLine(ms, tipX, tipY, cx2, cy2, Z_LEVEL, tend.getColor(), displace, 1.1);
			HLGuiUtils.fracLine(ms, cx1, cy1, tipX, tipY, Z_LEVEL, tend.getColor(), displace, 0.8);
			HLGuiUtils.fracLine(ms, cx2, cy2, tipX, tipY, Z_LEVEL, tend.getColor(), displace, 0.8);

			// Enzyme icon at the spoke's outer position
			double iconDist = ICON_DIAMETER / 1.75;
			int iconX = absCenterX + (int) (Math.cos(spokeAngleRad) * iconDist) - halfItem;
			int iconY = absCenterY + (int) (Math.sin(spokeAngleRad) * iconDist) - halfItem;
			HLGuiUtils.renderItemStackInGui(gfx, new ItemStack(EnumBloodTendency.getRepEnzyme(tend)), iconX, iconY);

			// Show "Required" label next to the enzyme icon for required tendencies
			if (required) {
				Font font = Minecraft.getInstance().font;
				String label = GTE + "3";
				ms.pushPose();
				ms.scale(LABEL_SCALE, LABEL_SCALE, 1f);
				int labelX = (int) ((iconX + ITEM_SIZE) / LABEL_SCALE);
				int labelY = (int) ((iconY + ITEM_SIZE / 2 + LABEL_Y_OFFSET) / LABEL_SCALE);
				gfx.drawString(font, label, labelX, labelY, tend.getColor().getColor(), false);

				ms.popPose();
			}

			angle += 45f;
		}

		ms.popPose();
	}

	// ═══════════════════════════════════════════════════════════════
	//  Drawing helpers
	// ═══════════════════════════════════════════════════════════════

	/** Draws a subtle pulsing glow in the background. */
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
}
