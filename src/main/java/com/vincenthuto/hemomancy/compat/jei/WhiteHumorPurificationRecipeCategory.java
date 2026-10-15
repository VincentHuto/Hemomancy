package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.WhiteHumorPurificationRecipe;
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
 * JEI category for white humor pool purification.
 */
public class WhiteHumorPurificationRecipeCategory implements IRecipeCategory<WhiteHumorPurificationRecipe> {

	public static final RecipeType<WhiteHumorPurificationRecipe> JEI_TYPE =
			RecipeType.create(Hemomancy.MOD_ID, "white_humor_purification", WhiteHumorPurificationRecipe.class);

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


	private final IDrawable icon;

	public WhiteHumorPurificationRecipeCategory(IGuiHelper guiHelper) {
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

	@Override public int getWidth() { return BG_W; }
	@Override public int getHeight() { return BG_H; }

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
		Font font = Minecraft.getInstance().font;
		JeiPanel.Theme t = JeiPanel.STILL;
		float time = JeiPanel.time();

		JeiPanel.panel(gfx, t, BG_W, BG_H, FOOTER_Y);
		JeiPanel.glow(gfx, POOL_X + POOL_W / 2, POOL_Y + POOL_H / 2, 46, 24, 0xFFD8F1FF, 0.65f + 0.35f * JeiPanel.pulse(time, 1.8f));
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.pool.offer"), INPUT_X + 9, INPUT_Y - 10, t.muted());
		JeiPanel.slot(gfx, t, INPUT_X + 1, INPUT_Y + 1);
		JeiProgressArrow.draw(gfx, INPUT_X + 23, INPUT_Y + 8, POOL_X - 6, time, t);
		JeiProgressArrow.draw(gfx, POOL_X + POOL_W + 6, OUTPUT_Y + 8, OUTPUT_X - 6, time + 0.5f, t);
		drawWhiteHumorPool(gfx, time);
		JeiPanel.label(gfx, font, Component.translatable("fluid.hemomancy.white_humor"), POOL_X + POOL_W / 2, POOL_Y - 13, t.title());
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.pool.emerges"), OUTPUT_X + 9, OUTPUT_Y - 12, t.muted());
		JeiPanel.resultSlot(gfx, t, OUTPUT_X + 1, OUTPUT_Y + 1, t.accent(), time);
		JeiPanel.footer(gfx, font, footer(recipe), BG_W, FOOTER_Y);
	}

	private static List<JeiPanel.Stat> footer(WhiteHumorPurificationRecipe recipe) {
		JeiPanel.Theme t = JeiPanel.STILL;
		return List.of(
				JeiPanel.Stat.of(JeiPanel.Icon.NONE, Component.translatable("hemomancy.jei.white_humor_purification.hint"), t.text(),
						Component.translatable("hemomancy.jei.white_humor_purification.hint.tip")),
				JeiPanel.Stat.of(JeiPanel.Icon.TIME, JeiPanel.secondsText(Math.max(0, recipe.getTransformTime())), t.text()));
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, WhiteHumorPurificationRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
		JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), BG_W, FOOTER_Y, BG_H, mouseX, mouseY);
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

}
