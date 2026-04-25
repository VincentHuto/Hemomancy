package com.vincenthuto.hemomancy.compat.jei;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;

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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * JEI recipe category for the Morphling Incubator — polyp crafting.
 * Displays the center polyp input, up to 4 catalyst inputs arranged in a
 * cardinal pattern around it, and the morphling output.
 * Fully programmatic rendering (no texture atlas required).
 */
public class IncubatorRecipeCategory implements IRecipeCategory<IncubatorRecipe> {

	// Layout constants
	private static final int BG_W = 170;
	private static final int BG_H = 100;

	// Center slot position (polyp input)
	private static final int CENTER_X = 60;
	private static final int CENTER_Y = 42;

	// Catalyst slot positions (top, right, bottom, left — relative to center)
	private static final int[][] CATALYST_OFFSETS = {
			{  0, -30 }, // top
			{ 30,   0 }, // right
			{  0,  30 }, // bottom
			{-30,   0 }  // left
	};

	// Output slot position
	private static final int OUTPUT_X = 140;
	private static final int OUTPUT_Y = 42;

	// Progress ring constants — matching MorphlingIncubatorScreen.renderProgressRing
	private static final int RING_OUTER_RADIUS = 24;
	private static final int RING_INNER_RADIUS = 19;
	private static final int RING_SEGMENTS = 64;
	private static final int RING_EMPTY_COLOR = 0x30200808;
	private static final float RING_ANIM_PERIOD = 10f; // seconds per full cycle in JEI

	// Colors — dark, fleshy theme matching the mod's aesthetic
	private static final int BG_COLOR        = 0xFF0A0204;
	private static final int BORDER_OUTER    = 0xFF330808;
	private static final int SLOT_BG         = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK  = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;

	private final IDrawable background;
	private final IDrawable icon;

	public IncubatorRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(BG_W, BG_H);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(BlockInit.morphling_incubator.get()));
	}

	// ── Drawing ──────────────────────────────────────────────────────
	private float animTime = 0f;

	@Override
	public void draw(IncubatorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx,
			double mouseX, double mouseY) {

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		// Dark background fill
		gfx.fill(0, 0, BG_W, BG_H, BG_COLOR);

		// Subtle radial glow behind center
		int cx = CENTER_X + 8;
		int cy = CENTER_Y + 8;
		for (int ring = 36; ring > 0; ring -= 3) {
			float t = ring / 36f;
			int alpha = (int) (14 * (1f - t));
			int red   = (int) (25 * (1f - t));
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, (alpha << 24) | (red << 16));
		}

		// Border
		gfx.fill(0, 0, BG_W, 1, BORDER_OUTER);
		gfx.fill(0, BG_H - 1, BG_W, BG_H, BORDER_OUTER);
		gfx.fill(0, 0, 1, BG_H, BORDER_OUTER);
		gfx.fill(BG_W - 1, 0, BG_W, BG_H, BORDER_OUTER);

		// ── Circular progress ring around center (matches incubator screen) ──
		renderProgressRing(gfx, cx, cy, time);

		// ── Slot backgrounds ──
		// Center polyp slot
		drawSlot(gfx, CENTER_X, CENTER_Y);

		// Catalyst slots (only draw slots that are used by this recipe + up to 4)
		int catalystCount = recipe.getCatalysts().size();
		for (int i = 0; i < 4; i++) {
			int sx = CENTER_X + CATALYST_OFFSETS[i][0];
			int sy = CENTER_Y + CATALYST_OFFSETS[i][1];
			if (i < catalystCount) {
				drawSlot(gfx, sx, sy);
				// Connecting line from catalyst to center
				drawConnectingLine(gfx, sx + 8, sy + 8, cx, cy, time, i);
			} else {
				// Empty slot outline — dimmed
				drawEmptySlot(gfx, sx, sy);
			}
		}

		// Output slot (highlighted)
		drawSlot(gfx, OUTPUT_X, OUTPUT_Y);
		gfx.fill(OUTPUT_X, OUTPUT_Y, OUTPUT_X + 16, OUTPUT_Y + 16, 0x18FF4444);


		// ── Label ──
		Font font = Minecraft.getInstance().font;
		Component title = Component.literal("Incubation");
		int tw = font.width(title);
		gfx.drawString(font, title, (BG_W - tw) / 2, 2, 0xFF884444, false);

		// Blood cost hint
		Component bloodHint = Component.literal("\u2764 Requires Blood");
		gfx.drawString(font, bloodHint, 4, BG_H - 11, 0xFF773333, false);
	}

	/**
	 * Draws the circular progress ring exactly matching
	 * MorphlingIncubatorScreen.renderProgressRing — pixel-by-pixel arc,
	 * 64 segments, inner/outer radius, pulsing crimson fill, dim track,
	 * and leading-edge glow dot. Animated on a timer since JEI has no
	 * real crafting progress.
	 */
	private void renderProgressRing(GuiGraphics gfx, int cx, int cy, float time) {
		// Animated progress cycles over RING_ANIM_PERIOD seconds
		float progress = (time % RING_ANIM_PERIOD) / RING_ANIM_PERIOD;
		int filledSegments = (int) (RING_SEGMENTS * progress);

		for (int i = 0; i < RING_SEGMENTS; i++) {
			double angle1 = -Math.PI / 2 + (2 * Math.PI / RING_SEGMENTS) * i;
			double angle2 = -Math.PI / 2 + (2 * Math.PI / RING_SEGMENTS) * (i + 1);

			// Draw arc segment pixel by pixel along the radial
			for (int r = RING_INNER_RADIUS; r <= RING_OUTER_RADIUS; r++) {
				// Sample a few points along the arc
				for (double a = angle1; a < angle2; a += 0.04) {
					int px = cx + (int) (r * Math.cos(a));
					int py = cy + (int) (r * Math.sin(a));

					int color;
					if (i < filledSegments) {
						// Filled — bright pulsing crimson
						float pulse = 0.7f + 0.3f * Mth.sin(time * 3f + i * 0.1f);
						int red = (int) (200 * pulse);
						int green = (int) (20 * pulse);
						color = (0xDD << 24) | (red << 16) | (green << 8) | 0x15;
					} else {
						// Empty — dim dark track
						color = RING_EMPTY_COLOR;
					}
					gfx.fill(px, py, px + 1, py + 1, color);
				}
			}
		}

		// Draw a subtle glow at the leading edge of progress
		if (filledSegments > 0 && filledSegments < RING_SEGMENTS) {
			double leadAngle = -Math.PI / 2 + (2 * Math.PI / RING_SEGMENTS) * filledSegments;
			int leadX = cx + (int) ((RING_INNER_RADIUS + RING_OUTER_RADIUS) / 2.0 * Math.cos(leadAngle));
			int leadY = cy + (int) ((RING_INNER_RADIUS + RING_OUTER_RADIUS) / 2.0 * Math.sin(leadAngle));

			float pulse = 0.5f + 0.5f * Mth.sin(time * 5f);
			int glowAlpha = (int) (120 * pulse);
			int glowColor = (glowAlpha << 24) | (0xFF << 16) | (0x30 << 8) | 0x20;
			// Small 3x3 glow dot
			gfx.fill(leadX - 1, leadY - 1, leadX + 2, leadY + 2, glowColor);
		}
	}

	/** Draws a pulsing connecting line between a catalyst slot and the center. */
	private void drawConnectingLine(GuiGraphics gfx, int x1, int y1, int x2, int y2, float time, int index) {
		int steps = 8;
		for (int s = 0; s < steps; s++) {
			float t = (float) s / steps;
			int px = (int) Mth.lerp(t, x1, x2);
			int py = (int) Mth.lerp(t, y1, y2);
			float pulse = 0.4f + 0.6f * Mth.sin(time * 4f + s * 0.8f + index * 1.5f);
			int alpha = (int) (80 * pulse);
			int r = (int) (70 * pulse);
			gfx.fill(px, py, px + 1, py + 1, (alpha << 24) | (r << 16) | 0x0808);
		}
	}


	private void drawSlot(GuiGraphics gfx, int sx, int sy) {
		gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
		gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
		gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
	}

	private void drawEmptySlot(GuiGraphics gfx, int sx, int sy) {
		gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0x20202020);
		gfx.fill(sx, sy, sx + 16, sy + 16, 0x10101010);
	}

	// ── IRecipeCategory impl ─────────────────────────────────────────

	@SuppressWarnings("removal")
	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Nonnull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public RecipeType<IncubatorRecipe> getRecipeType() {
		return JEIPlugin.incubator_recipe_type;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.translatable("hemomancy.jei.morphling_incubator");
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IncubatorRecipe recipe, IFocusGroup focuses) {
		// Center input — morphling polyp (always required)
		builder.addSlot(RecipeIngredientRole.INPUT, CENTER_X, CENTER_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemInit.morphling_polyp.get()));

		// Catalyst inputs — positioned around the center
		var catalysts = recipe.getCatalysts();
		for (int i = 0; i < catalysts.size() && i < 4; i++) {
			int sx = CENTER_X + CATALYST_OFFSETS[i][0];
			int sy = CENTER_Y + CATALYST_OFFSETS[i][1];
			builder.addSlot(RecipeIngredientRole.INPUT, sx, sy)
					.addIngredients(catalysts.get(i));
		}

		// Output slot
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItemStack().copy());
	}
}
