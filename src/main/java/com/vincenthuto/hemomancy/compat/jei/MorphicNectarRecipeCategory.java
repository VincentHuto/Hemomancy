package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.MorphicNectarRecipe;
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

import javax.annotation.Nonnull;

/**
 * JEI recipe category for Morphic Nectar item transformations.
 * Shows an item dropped into a Morphic Nectar pool and emerging as the recipe result.
 */
public class MorphicNectarRecipeCategory implements IRecipeCategory<MorphicNectarRecipe> {

	public static final RecipeType<MorphicNectarRecipe> JEI_TYPE =
			RecipeType.create(Hemomancy.MOD_ID, "morphic_nectar", MorphicNectarRecipe.class);

	private static final int BG_W = 170;
	private static final int BG_H = 82;

	private static final int INPUT_X = 19;
	private static final int INPUT_Y = 32;
	private static final int OUTPUT_X = 134;
	private static final int OUTPUT_Y = 32;

	private static final int POOL_X = 58;
	private static final int POOL_Y = 24;
	private static final int POOL_W = 54;
	private static final int POOL_H = 28;

	private static final int BG_COLOR = 0xFF071006;
	private static final int BORDER_OUTER = 0xFF243D0A;
	private static final int BORDER_INNER = 0xFF101F05;
	private static final int SLOT_BG = 0xFF101807;
	private static final int SLOT_BORDER_DARK = 0xFF071003;
	private static final int SLOT_BORDER_LIGHT = 0xFF2F4D12;
	private static final int NECTAR_VEIN = 0xBFC7F03A;
	private static final int NECTAR_SHADOW = 0xEE111A04;
	private static final int LABEL_COLOR = 0xFFB6D85A;
	private static final int MUTED_LABEL_COLOR = 0xFF6F8740;

	private final IDrawable background;
	private final IDrawable icon;
	private float animTime = 0f;

	public MorphicNectarRecipeCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createBlankDrawable(BG_W, BG_H);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(ItemInit.hemolytic_vial.get()));
	}

	@Override
	@Nonnull
	public RecipeType<MorphicNectarRecipe> getRecipeType() {
		return JEI_TYPE;
	}

	@Override
	@Nonnull
	public Component getTitle() {
		return Component.translatable("hemomancy.jei.morphic_nectar");
	}

	@SuppressWarnings("removal")
	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Override
	@Nonnull
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull MorphicNectarRecipe recipe,
			@Nonnull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, INPUT_Y + 1)
				.addIngredients(recipe.getInput());

		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, OUTPUT_Y + 1)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItemRaw().copy());
	}

	@Override
	public void draw(@Nonnull MorphicNectarRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, @Nonnull GuiGraphics gfx,
			double mouseX, double mouseY) {
		animTime += 0.016f;
		float time = animTime;

		Font font = Minecraft.getInstance().font;

		drawBackground(gfx, time);
		drawSlot(gfx, INPUT_X, INPUT_Y);
		drawSlot(gfx, OUTPUT_X, OUTPUT_Y);
		drawArrow(gfx, INPUT_X + 23, INPUT_Y + 7, POOL_X - 5, time);
		drawArrow(gfx, POOL_X + POOL_W + 5, OUTPUT_Y + 7, OUTPUT_X - 5, time);
		drawNectarPool(gfx, time);

		Component fluidLabel = Component.translatable("fluid.hemomancy.morphic_nectar");
		int fluidLabelWidth = font.width(fluidLabel);
		gfx.drawString(font, fluidLabel, (BG_W - fluidLabelWidth) / 2, 6, LABEL_COLOR, false);

		drawTransformTime(recipe, gfx, font);
	}

	private void drawBackground(GuiGraphics gfx, float time) {
		gfx.fill(0, 0, BG_W, BG_H, BG_COLOR);

		int glowX = POOL_X + POOL_W / 2;
		int glowY = POOL_Y + POOL_H / 2;
		float pulse = 0.65f + 0.35f * Mth.sin(time * 2.0f);
		for (int ring = 44; ring > 0; ring -= 4) {
			float falloff = 1f - ring / 44f;
			int alpha = (int) (22 * falloff * pulse);
			int color = (alpha << 24) | (0x8A << 16) | (0xB8 << 8) | 0x18;
			gfx.fill(glowX - ring, glowY - ring / 2, glowX + ring, glowY + ring / 2, color);
		}

		gfx.fill(0, 0, BG_W, 1, BORDER_OUTER);
		gfx.fill(0, BG_H - 1, BG_W, BG_H, BORDER_INNER);
		gfx.fill(0, 0, 1, BG_H, BORDER_OUTER);
		gfx.fill(BG_W - 1, 0, BG_W, BG_H, BORDER_INNER);
	}

	private void drawNectarPool(GuiGraphics gfx, float time) {
		// Organic double frame with the same slightly swollen silhouette as the blood HUD.
		gfx.fill(POOL_X - 3, POOL_Y - 3, POOL_X + POOL_W + 3, POOL_Y + POOL_H + 3, 0xFF121D05);
		gfx.fill(POOL_X - 4, POOL_Y + 5, POOL_X - 3, POOL_Y + POOL_H - 5, 0xFF121D05);
		gfx.fill(POOL_X + POOL_W + 3, POOL_Y + 5, POOL_X + POOL_W + 4, POOL_Y + POOL_H - 5, 0xFF121D05);
		gfx.fill(POOL_X - 2, POOL_Y - 2, POOL_X + POOL_W + 2, POOL_Y + POOL_H + 2, 0xFF31480F);
		gfx.fill(POOL_X - 3, POOL_Y + 6, POOL_X - 2, POOL_Y + POOL_H - 6, 0xFF31480F);
		gfx.fill(POOL_X + POOL_W + 2, POOL_Y + 6, POOL_X + POOL_W + 3, POOL_Y + POOL_H - 6, 0xFF31480F);
		gfx.fill(POOL_X - 1, POOL_Y - 1, POOL_X + POOL_W + 1, POOL_Y + POOL_H + 1, NECTAR_SHADOW);

		// Layered animated body: dark at edges, warm amber-green in the middle.
		for (int y = 0; y < POOL_H; y++) {
			float rowT = (float) y / Math.max(POOL_H - 1, 1);
			for (int x = 0; x < POOL_W; x++) {
				float colT = (float) x / Math.max(POOL_W - 1, 1);
				float edgeShade = Math.min(Math.min(colT, 1f - colT) * 2.4f, 1f);
				float pulse = 0.78f + 0.22f * Mth.sin(time * 1.2f + x * 0.11f + y * 0.17f);
				int r = (int) Mth.clamp((54 + 72 * edgeShade + 22 * (1f - rowT)) * pulse, 0, 255);
				int g = (int) Mth.clamp((76 + 122 * edgeShade + 28 * (1f - rowT)) * pulse, 0, 255);
				int b = (int) Mth.clamp((8 + 20 * edgeShade + 8 * (1f - rowT)) * pulse, 0, 255);
				gfx.fill(POOL_X + x, POOL_Y + y, POOL_X + x + 1, POOL_Y + y + 1,
						(0xEE << 24) | (r << 16) | (g << 8) | b);
			}
		}

		// Ropy vertical veins, warped rather than straight, to echo the overlay fill texture.
		for (int vein = 0; vein < 5; vein++) {
			float seed = vein * 1.73f;
			int baseX = POOL_X + 6 + vein * 10;
			for (int y = 2; y < POOL_H - 2; y++) {
				int x = baseX + (int) (Mth.sin(time * 1.4f + y * 0.35f + seed) * 2f);
				float shimmer = 0.45f + 0.55f * Mth.sin(time * 2.8f + y * 0.27f + seed);
				int alpha = (int) (58 * shimmer);
				gfx.fill(x, POOL_Y + y, x + 1, POOL_Y + y + 1,
						(alpha << 24) | (0xD4 << 16) | (0xF0 << 8) | 0x42);
				if (shimmer > 0.74f) {
					gfx.fill(x + 1, POOL_Y + y, x + 2, POOL_Y + y + 1, NECTAR_VEIN);
				}
			}
		}

		// Wobbling meniscus and gloss strips.
		for (int x = 2; x < POOL_W - 2; x++) {
			int y = POOL_Y + 4 + (int) (Mth.sin(time * 3.0f + x * 0.32f) * 1.5f);
			float gleam = 0.5f + 0.5f * Mth.sin(time * 4.1f + x * 0.19f);
			int alpha = (int) (100 + 80 * gleam);
			gfx.fill(POOL_X + x, y, POOL_X + x + 1, y + 1,
					(alpha << 24) | (0xE4 << 16) | (0xFF << 8) | 0x62);
			if (x % 7 < 4) {
				gfx.fill(POOL_X + x, y + 6, POOL_X + x + 1, y + 7, 0x66C2E73A);
			}
		}


		// Rising bubbles and dark drifting specks.
		for (int i = 0; i < 6; i++) {
			float speed = 0.16f + i * 0.035f;
			float progress = (time * speed + i * 0.23f) % 1.0f;
			int bubbleX = POOL_X + 5 + (i * 8 + (int) (Mth.sin(time + i) * 3f) + POOL_W) % (POOL_W - 10);
			int bubbleY = POOL_Y + POOL_H - 4 - (int) (progress * (POOL_H - 8));
			int alpha = (int) (72 * (1f - Math.abs(progress - 0.5f) * 1.4f));
			if (alpha > 8) {
				gfx.fill(bubbleX, bubbleY, bubbleX + 2, bubbleY + 2,
						(alpha << 24) | (0xE4 << 16) | (0xFF << 8) | 0x62);
			}
		}
		for (int i = 0; i < 5; i++) {
			int speckX = POOL_X + 4 + (i * 11 + (int) (time * (2 + i))) % (POOL_W - 8);
			int speckY = POOL_Y + 6 + (i * 5 + (int) (Mth.sin(time * 0.8f + i) * 3f) + POOL_H) % (POOL_H - 12);
			gfx.fill(speckX, speckY, speckX + 2, speckY + 1, 0x45111A04);
		}
	}

	private void drawSlot(GuiGraphics gfx, int x, int y) {
		gfx.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BORDER_DARK);
		gfx.fill(x, y, x + 16, y + 16, SLOT_BG);
		gfx.fill(x + 16, y, x + 17, y + 17, SLOT_BORDER_LIGHT);
		gfx.fill(x, y + 16, x + 17, y + 17, SLOT_BORDER_LIGHT);
	}

	private void drawArrow(GuiGraphics gfx, int startX, int y, int tipX, float time) {
		int shaftEnd = tipX - 5;
		int trackColor = 0x553A5010;
		int dimHeadColor = 0x66526E18;

		gfx.fill(startX, y, shaftEnd, y + 2, trackColor);
		drawArrowHead(gfx, tipX, y, dimHeadColor);

		float progress = (time * 0.85f) % 1.0f;
		float trail = 0.34f;
		int totalW = Math.max(tipX - startX + 1, 1);
		for (int x = startX; x <= tipX; x++) {
			float xProgress = (float) (x - startX) / totalW;
			float dist = progress - xProgress;
			if (dist < 0f || dist > trail) {
				continue;
			}

			float intensity = 1f - dist / trail;
			int alpha = (int) (85 + 170 * intensity);
			int red = (int) Mth.clamp(160 + 75 * intensity, 0, 255);
			int green = (int) Mth.clamp(205 + 50 * intensity, 0, 255);
			int blue = (int) Mth.clamp(32 + 66 * intensity, 0, 255);
			int color = (alpha << 24) | (red << 16) | (green << 8) | blue;

			if (x < shaftEnd) {
				gfx.fill(x, y, x + 1, y + 2, color);
			} else {
				drawArrowHeadColumn(gfx, x, tipX, y, color);
			}
		}
	}

	private void drawArrowHead(GuiGraphics gfx, int tipX, int y, int color) {
		for (int x = tipX - 5; x <= tipX; x++) {
			drawArrowHeadColumn(gfx, x, tipX, y, color);
		}
	}

	private void drawArrowHeadColumn(GuiGraphics gfx, int x, int tipX, int y, int color) {
		int fromTip = tipX - x;
		if (fromTip < 0 || fromTip > 5) {
			return;
		}

		int halfHeight = Math.max(0, fromTip / 2);
		gfx.fill(x, y - halfHeight, x + 1, y + 2 + halfHeight, color);
	}

	private void drawTransformTime(MorphicNectarRecipe recipe, GuiGraphics gfx, Font font) {
		int ticks = Math.max(0, recipe.getTransformTime());
		Component timeText = Component.translatable("gui.jei.category.smelting.time.seconds", ticks / 20.0f);
		int width = font.width(timeText);
		gfx.drawString(font, timeText, BG_W - width - 4, BG_H - 11, MUTED_LABEL_COLOR, false);

		Component hint = Component.translatable("hemomancy.jei.morphic_nectar.hint");
		gfx.drawString(font, hint, 4, BG_H - 11, MUTED_LABEL_COLOR, false);
	}
}



