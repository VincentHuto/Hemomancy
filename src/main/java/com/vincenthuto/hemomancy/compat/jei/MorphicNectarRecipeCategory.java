package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.MorphicNectarRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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
import java.util.List;

/**
 * JEI recipe category for Morphic Nectar item transformations.
 * Shows an item dropped into a Morphic Nectar pool and emerging as the recipe result.
 */
public class MorphicNectarRecipeCategory implements IRecipeCategory<MorphicNectarRecipe> {

	public static final RecipeType<MorphicNectarRecipe> JEI_TYPE =
			RecipeType.create(Hemomancy.MOD_ID, "morphic_nectar", MorphicNectarRecipe.class);

	private static final int BG_W = 176;
	private static final int BG_H = 92;
	private static final int FOOTER_Y = 76;

	private static final int INPUT_X = 16;
	private static final int INPUT_Y = 34;
	private static final int OUTPUT_X = 140;
	private static final int OUTPUT_Y = 34;

	private static final int POOL_X = 61;
	private static final int POOL_Y = 26;
	private static final int POOL_W = 54;
	private static final int POOL_H = 28;

	private static final int NECTAR_VEIN = 0xBFC7F03A;
	private static final int NECTAR_SHADOW = 0xEE111A04;

	private final IDrawable icon;

	public MorphicNectarRecipeCategory(IGuiHelper guiHelper) {
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

	@Override public int getWidth() { return BG_W; }
	@Override public int getHeight() { return BG_H; }

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
		Font font = Minecraft.getInstance().font;
		JeiPanel.Theme t = JeiPanel.FUNGAL;
		float time = JeiPanel.time();

		JeiPanel.panel(gfx, t, BG_W, BG_H, FOOTER_Y);
		JeiPanel.glow(gfx, POOL_X + POOL_W / 2, POOL_Y + POOL_H / 2, 46, 24, 0xFF8AB818, 0.65f + 0.35f * JeiPanel.pulse(time, 1.8f));
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.pool.offer"), INPUT_X + 9, INPUT_Y - 10, t.muted());
		JeiPanel.slot(gfx, t, INPUT_X + 1, INPUT_Y + 1);
		JeiProgressArrow.draw(gfx, INPUT_X + 23, INPUT_Y + 8, POOL_X - 6, time, t);
		JeiProgressArrow.draw(gfx, POOL_X + POOL_W + 6, OUTPUT_Y + 8, OUTPUT_X - 6, time + 0.5f, t);
		drawNectarPool(gfx, time);
		JeiPanel.label(gfx, font, Component.translatable("fluid.hemomancy.morphic_nectar"), POOL_X + POOL_W / 2, POOL_Y - 13, t.title());
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.pool.emerges"), OUTPUT_X + 9, OUTPUT_Y - 12, t.muted());
		JeiPanel.resultSlot(gfx, t, OUTPUT_X + 1, OUTPUT_Y + 1, t.accent(), time);
		JeiPanel.footer(gfx, font, footer(recipe), BG_W, FOOTER_Y);
	}

	private static List<JeiPanel.Stat> footer(MorphicNectarRecipe recipe) {
		JeiPanel.Theme t = JeiPanel.FUNGAL;
		return List.of(
				JeiPanel.Stat.of(JeiPanel.Icon.NONE, Component.translatable("hemomancy.jei.morphic_nectar.hint"), t.text(),
						Component.translatable("hemomancy.jei.morphic_nectar.hint.tip")),
				JeiPanel.Stat.of(JeiPanel.Icon.TIME, JeiPanel.secondsText(Math.max(0, recipe.getTransformTime())), t.text()));
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, MorphicNectarRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
		JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), BG_W, FOOTER_Y, BG_H, mouseX, mouseY);
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

}
