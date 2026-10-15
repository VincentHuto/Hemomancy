package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.recipe.BloodInfusionRecipe;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/** JEI category for Blood Infusion: a placed block struck by Blood Projection becomes its infused form. */
public final class BloodInfusionRecipeCategory implements IRecipeCategory<BloodInfusionRecipe> {
	public static final RecipeType<BloodInfusionRecipe> JEI_TYPE =
			RecipeType.create(Hemomancy.MOD_ID, "blood_infusion", BloodInfusionRecipe.class);

	private static final int W = 176;
	private static final int H = 68;
	private static final int FOOTER_Y = 52;
	private static final int SLOT_Y = 22;
	private static final int INPUT_X = 80;
	private static final int CAST_X = 16;
	private static final int OUTPUT_X = 144;

	private final IDrawable icon;

	public BloodInfusionRecipeCategory(IGuiHelper guiHelper) {
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(ItemInit.blood_projection.get()));
	}

	@Override public RecipeType<BloodInfusionRecipe> getRecipeType() { return JEI_TYPE; }
	@Override public Component getTitle() { return Component.translatable("hemomancy.jei.blood_infusion"); }
	@Override public int getWidth() { return W; }
	@Override public int getHeight() { return H; }
	@Override public IDrawable getIcon() { return icon; }

	@Override
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull BloodInfusionRecipe recipe,
			@Nonnull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(recipe.input()))
				.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
						"hemomancy.jei.blood_infusion.block.tip").withStyle(ChatFormatting.GRAY)));
		builder.addSlot(RecipeIngredientRole.CATALYST, CAST_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemInit.blood_projection.get()))
				.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
						"hemomancy.jei.blood_infusion.cast.tip").withStyle(ChatFormatting.GRAY)));
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.resultStack());
	}

	private static List<JeiPanel.Stat> footer(BloodInfusionRecipe recipe) {
		return List.of(
				JeiPanel.Stat.of(JeiPanel.Icon.BLOOD, Component.translatable("hemomancy.jei.blood_infusion.cost",
						BloodGourdItem.formatBloodAmount(recipe.bloodCost())), JeiPanel.BLOOD_RED,
						Component.translatable("hemomancy.jei.blood_infusion.cost.tip")),
				JeiPanel.Stat.of(JeiPanel.Icon.NONE, Component.translatable("hemomancy.jei.blood_infusion.in_world"),
						JeiPanel.BLOOD.muted(), Component.translatable("hemomancy.jei.blood_infusion.block.tip")));
	}

	@Override
	public void draw(@Nonnull BloodInfusionRecipe recipe, @Nonnull IRecipeSlotsView slots,
			@Nonnull GuiGraphics gfx, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		JeiPanel.Theme t = JeiPanel.BLOOD;
		float time = JeiPanel.time();

		JeiPanel.panel(gfx, t, W, H, FOOTER_Y);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.blood_infusion.block"), INPUT_X + 8, SLOT_Y - 11, t.muted());
		JeiPanel.slot(gfx, t, INPUT_X, SLOT_Y);

		// The projection: a beating aura with blood motes drawn out toward the block
		int cx = CAST_X + 8, cy = SLOT_Y + 8;
		JeiPanel.glow(gfx, cx, cy, 18, 14, 0xFFB01020, 0.6f + 0.4f * JeiPanel.pulse(time, 3.4f));
		for (int i = 0; i < 4; i++) {
			float p = (time * 0.6f + i * 0.25f) % 1f;
			int mx = Math.round(Mth.lerp(p, cx + 12, INPUT_X - 6));
			int my = cy + Math.round(Mth.sin(p * 6.28f + i) * 3f);
			gfx.fill(mx, my, mx + 2, my + 1, JeiPanel.withAlpha(0xFFE0303A, (int) (220 * (1f - p))));
		}
		JeiPanel.tintedSlot(gfx, t, CAST_X, SLOT_Y, 0xFFB01A22);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.blood_infusion.cast"), cx, SLOT_Y + 21, t.muted());

		JeiProgressArrow.draw(gfx, INPUT_X + 24, SLOT_Y + 7, OUTPUT_X - 8, time, t);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.blood_infusion.infused"), OUTPUT_X + 8, SLOT_Y - 13, t.muted());
		JeiPanel.resultSlot(gfx, t, OUTPUT_X, SLOT_Y, t.accent(), time);

		JeiPanel.footer(gfx, font, footer(recipe), W, FOOTER_Y);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, BloodInfusionRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
		JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), W, FOOTER_Y, H, mouseX, mouseY);
	}
}
