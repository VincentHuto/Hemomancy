package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.WhiteHumorPurificationRecipe;
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
 * JEI category for white humor pool purification.
 */
public class WhiteHumorPurificationRecipeCategory implements IRecipeCategory<WhiteHumorPurificationRecipe> {

	public static final RecipeType<WhiteHumorPurificationRecipe> JEI_TYPE =
			RecipeType.create(Hemomancy.MOD_ID, "white_humor_purification", WhiteHumorPurificationRecipe.class);

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

	private static final int BG_COLOR = 0xFF071018;
	private static final int BORDER_OUTER = 0xFFBFD8E8;
	private static final int BORDER_INNER = 0xFF476273;
	private static final int SLOT_BG = 0xFF0C1820;
	private static final int SLOT_BORDER_DARK = 0xFF031018;
	private static final int SLOT_BORDER_LIGHT = 0xFF8FB6C9;
	private static final int LABEL_COLOR = 0xFFE9F8FF;
	private static final int MUTED_LABEL_COLOR = 0xFF91AFC0;

	private final IDrawable background;
	private final IDrawable icon;
	private float animTime = 0f;

	public WhiteHumorPurificationRecipeCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createBlankDrawable(BG_W, BG_H);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(ItemInit.pale_humor_flask.get()));
	}

	@Override
	@Nonnull
	public RecipeType<WhiteHumorPurificationRecipe> getRecipeType() {
		return JEI_TYPE;
	}

	@Override
	@Nonnull
	public Component getTitle() {
		return Component.translatable("hemomancy.jei.white_humor_purification");
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
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull WhiteHumorPurificationRecipe recipe,
			@Nonnull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, INPUT_Y + 1)
				.addIngredients(recipe.getInput());
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, OUTPUT_Y + 1)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItemRaw().copy());
	}

	@Override
	public void draw(@Nonnull WhiteHumorPurificationRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView,
			@Nonnull GuiGraphics gfx, double mouseX, double mouseY) {
		animTime += 0.016f;
		float time = animTime;
		Font font = Minecraft.getInstance().font;

		drawBackground(gfx, time);
		drawSlot(gfx, INPUT_X, INPUT_Y);
		drawSlot(gfx, OUTPUT_X, OUTPUT_Y);
		drawArrow(gfx, INPUT_X + 23, INPUT_Y + 7, POOL_X - 5, time);
		drawArrow(gfx, POOL_X + POOL_W + 5, OUTPUT_Y + 7, OUTPUT_X - 5, time);
		drawWhiteHumorPool(gfx, time);

		Component fluidLabel = Component.translatable("fluid.hemomancy.white_humor");
		gfx.drawString(font, fluidLabel, (BG_W - font.width(fluidLabel)) / 2, 6, LABEL_COLOR, false);
		drawTransformTime(recipe, gfx, font);
	}

	private void drawBackground(GuiGraphics gfx, float time) {
		gfx.fill(0, 0, BG_W, BG_H, BG_COLOR);
		int glowX = POOL_X + POOL_W / 2;
		int glowY = POOL_Y + POOL_H / 2;
		float pulse = 0.65f + 0.35f * Mth.sin(time * 1.6f);
		for (int ring = 44; ring > 0; ring -= 4) {
			float falloff = 1f - ring / 44f;
			int alpha = (int) (18 * falloff * pulse);
			gfx.fill(glowX - ring, glowY - ring / 2, glowX + ring, glowY + ring / 2,
					(alpha << 24) | (0xD8 << 16) | (0xF1 << 8) | 0xFF);
		}
		gfx.fill(0, 0, BG_W, 1, BORDER_OUTER);
		gfx.fill(0, BG_H - 1, BG_W, BG_H, BORDER_INNER);
		gfx.fill(0, 0, 1, BG_H, BORDER_OUTER);
		gfx.fill(BG_W - 1, 0, BG_W, BG_H, BORDER_INNER);
	}

	private void drawWhiteHumorPool(GuiGraphics gfx, float time) {
		gfx.fill(POOL_X - 3, POOL_Y - 3, POOL_X + POOL_W + 3, POOL_Y + POOL_H + 3, 0xFF0B1620);
		gfx.fill(POOL_X - 2, POOL_Y - 2, POOL_X + POOL_W + 2, POOL_Y + POOL_H + 2, 0xFF7FA8BC);
		gfx.fill(POOL_X - 1, POOL_Y - 1, POOL_X + POOL_W + 1, POOL_Y + POOL_H + 1, 0xDD132736);

		for (int y = 0; y < POOL_H; y++) {
			float rowT = (float) y / Math.max(POOL_H - 1, 1);
			for (int x = 0; x < POOL_W; x++) {
				float ripple = 0.78f + 0.22f * Mth.sin(time * 1.1f + x * 0.09f + y * 0.16f);
				int r = (int) Mth.clamp((180 + 46 * (1f - rowT)) * ripple, 0, 255);
				int g = (int) Mth.clamp((206 + 38 * (1f - rowT)) * ripple, 0, 255);
				int b = (int) Mth.clamp((220 + 30 * (1f - rowT)) * ripple, 0, 255);
				gfx.fill(POOL_X + x, POOL_Y + y, POOL_X + x + 1, POOL_Y + y + 1,
						(0xE8 << 24) | (r << 16) | (g << 8) | b);
			}
		}

		for (int x = 2; x < POOL_W - 2; x++) {
			int y = POOL_Y + 5 + (int) (Mth.sin(time * 2.4f + x * 0.3f) * 1.5f);
			gfx.fill(POOL_X + x, y, POOL_X + x + 1, y + 1, 0xDFFFFFFF);
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
		gfx.fill(startX, y, shaftEnd, y + 2, 0x55476273);
		drawArrowHead(gfx, tipX, y, 0x665F8297);
		float progress = (time * 0.65f) % 1.0f;
		float trail = 0.34f;
		int totalW = Math.max(tipX - startX + 1, 1);
		for (int x = startX; x <= tipX; x++) {
			float xProgress = (float) (x - startX) / totalW;
			float dist = progress - xProgress;
			if (dist < 0f || dist > trail) continue;
			float intensity = 1f - dist / trail;
			int alpha = (int) (80 + 160 * intensity);
			int color = (alpha << 24) | (0xE8 << 16) | (0xF6 << 8) | 0xFF;
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
		if (fromTip < 0 || fromTip > 5) return;
		int halfHeight = Math.max(0, fromTip / 2);
		gfx.fill(x, y - halfHeight, x + 1, y + 2 + halfHeight, color);
	}

	private void drawTransformTime(WhiteHumorPurificationRecipe recipe, GuiGraphics gfx, Font font) {
		int ticks = Math.max(0, recipe.getTransformTime());
		Component timeText = Component.translatable("gui.jei.category.smelting.time.seconds", ticks / 20.0f);
		gfx.drawString(font, timeText, BG_W - font.width(timeText) - 4, BG_H - 11, MUTED_LABEL_COLOR, false);
		Component hint = Component.translatable("hemomancy.jei.white_humor_purification.hint");
		gfx.drawString(font, hint, 4, BG_H - 11, MUTED_LABEL_COLOR, false);
	}
}
