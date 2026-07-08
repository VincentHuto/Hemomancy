package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.ItemInit;
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

public class LivingWeaponGraftRecipeCategory implements IRecipeCategory<LivingWeaponGraftJeiRecipe> {
	private static final int BG_W = 170;
	private static final int BG_H = 82;

	private static final int STAFF_X = 15;
	private static final int BRAZIER_X = 61;
	private static final int GRAFT_X = BRAZIER_X;
	private static final int OUTPUT_X = 134;
	private static final int GRAFT_Y = 18;
	private static final int SLOT_Y = 46;

	private static final int BG_COLOR = 0xFF080304;
	private static final int BORDER_OUTER = 0xFF431010;
	private static final int BORDER_INNER = 0xFF1B0607;
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF5A1816;
	private static final int TEXT_RED = 0xFFB83A35;
	private static final int TEXT_MUTED = 0xFF9A7C74;

	private final IDrawable background;
	private final IDrawable icon;
	private float animTime = 0f;

	public LivingWeaponGraftRecipeCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createBlankDrawable(BG_W, BG_H);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(ItemInit.living_weapon_graft.get()));
	}

	@Override
	@Nonnull
	public RecipeType<LivingWeaponGraftJeiRecipe> getRecipeType() {
		return JEIPlugin.living_weapon_graft_type;
	}

	@Override
	@Nonnull
	public Component getTitle() {
		return Component.translatable("hemomancy.jei.living_weapon_graft");
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
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull LivingWeaponGraftJeiRecipe recipe,
			@Nonnull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, STAFF_X + 1, SLOT_Y + 1)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.livingStaff());
		builder.addSlot(RecipeIngredientRole.INPUT, BRAZIER_X + 1, SLOT_Y + 1)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.ironBrazier());
		builder.addSlot(RecipeIngredientRole.INPUT, GRAFT_X + 1, GRAFT_Y + 1)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.graft());
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, SLOT_Y + 1)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.output());
	}

	@Override
	public void draw(@Nonnull LivingWeaponGraftJeiRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView,
			@Nonnull GuiGraphics gfx, double mouseX, double mouseY) {
		animTime += 0.016f;
		float time = animTime;
		Font font = Minecraft.getInstance().font;

		drawBackground(gfx, time);
		drawSlot(gfx, STAFF_X, SLOT_Y);
		drawSlot(gfx, BRAZIER_X, SLOT_Y);
		drawSlot(gfx, GRAFT_X, GRAFT_Y);
		drawSlot(gfx, OUTPUT_X, SLOT_Y);
		drawArrow(gfx, BRAZIER_X + 24, SLOT_Y + 7, OUTPUT_X - 8, time);
		drawBrazierFlame(gfx, BRAZIER_X + 8, SLOT_Y - 7, time);

		Component formName = Component.literal(recipe.form().graftName());
		gfx.drawString(font, formName, 5, 6, TEXT_RED, false);

		Component hint = Component.translatable("hemomancy.jei.living_weapon_graft.hint");
		gfx.drawString(font, hint, 5, BG_H - 12, TEXT_MUTED, false);
	}

	private void drawBackground(GuiGraphics gfx, float time) {
		gfx.fill(0, 0, BG_W, BG_H, BG_COLOR);
		float pulse = 0.55f + 0.45f * Mth.sin(time * 1.8f);
		int centerX = BRAZIER_X + 8;
		int centerY = SLOT_Y + 8;
		for (int ring = 46; ring > 0; ring -= 5) {
			float falloff = 1f - ring / 46f;
			int alpha = (int) (18 * falloff * pulse);
			gfx.fill(centerX - ring, centerY - ring / 2, centerX + ring, centerY + ring / 2,
					(alpha << 24) | (0x8D << 16) | (0x12 << 8) | 0x10);
		}
		gfx.fill(0, 0, BG_W, 1, BORDER_OUTER);
		gfx.fill(0, BG_H - 1, BG_W, BG_H, BORDER_INNER);
		gfx.fill(0, 0, 1, BG_H, BORDER_OUTER);
		gfx.fill(BG_W - 1, 0, BG_W, BG_H, BORDER_INNER);
	}

	private void drawSlot(GuiGraphics gfx, int x, int y) {
		gfx.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BORDER_DARK);
		gfx.fill(x, y, x + 16, y + 16, SLOT_BG);
		gfx.fill(x + 16, y, x + 17, y + 17, SLOT_BORDER_LIGHT);
		gfx.fill(x, y + 16, x + 17, y + 17, SLOT_BORDER_LIGHT);
	}

	private void drawArrow(GuiGraphics gfx, int startX, int y, int tipX, float time) {
		int shaftEnd = tipX - 5;
		int trackColor = 0x553A1212;
		int dimHeadColor = 0x665A1816;

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
			int red = (int) Mth.clamp(170 + 65 * intensity, 0, 255);
			int green = (int) Mth.clamp(42 + 36 * intensity, 0, 255);
			int blue = (int) Mth.clamp(35 + 28 * intensity, 0, 255);
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

	private void drawBrazierFlame(GuiGraphics gfx, int x, int y, float time) {
		int flameHeight = 6 + (int) (2 * (0.5f + 0.5f * Mth.sin(time * 5.0f)));
		gfx.fill(x - 2, y + 8 - flameHeight, x + 3, y + 8, 0xAA8A1612);
		gfx.fill(x - 1, y + 9 - flameHeight, x + 2, y + 8, 0xCCCA4C28);
		gfx.fill(x, y + 10 - flameHeight, x + 1, y + 8, 0xFFE8A447);
	}
}
