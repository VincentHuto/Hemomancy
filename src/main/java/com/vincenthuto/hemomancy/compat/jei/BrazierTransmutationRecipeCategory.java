package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.recipe.BrazierTransmutationRecipe;
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
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * JEI category for the Iron Brazier's transmutation rite: an offering in a lit brazier, drawn out by held Absorption
 * with a memory selected (and a catalyst in the off hand where the recipe asks), becomes its result.
 */
public final class BrazierTransmutationRecipeCategory implements IRecipeCategory<BrazierTransmutationRecipe> {
	public static final RecipeType<BrazierTransmutationRecipe> JEI_TYPE =
			RecipeType.create(Hemomancy.MOD_ID, "brazier_transmutation", BrazierTransmutationRecipe.class);

	private static final int W = 176;
	private static final int H = 68;
	private static final int FOOTER_Y = 52;
	private static final int SLOT_Y = 22;
	private static final int SCULPTING_X = 11;
	private static final int CATALYST_X = 33;
	private static final int INPUT_X = 64;
	private static final int OUTPUT_X = 128;
	private static final int BYPRODUCT_X = 150;

	private final IDrawable icon;

	public BrazierTransmutationRecipeCategory(IGuiHelper guiHelper) {
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockInit.iron_brazier.get()));
	}

	@Override public RecipeType<BrazierTransmutationRecipe> getRecipeType() { return JEI_TYPE; }
	@Override public Component getTitle() { return Component.translatable("hemomancy.jei.brazier_transmutation"); }
	@Override public int getWidth() { return W; }
	@Override public int getHeight() { return H; }
	@Override public IDrawable getIcon() { return icon; }

	@Override
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull BrazierTransmutationRecipe recipe,
			@Nonnull IFocusGroup focuses) {
		ItemStack sculpting = recipe.sculpting().map(JeiSculptingIcons::memoryFor)
				.orElseGet(() -> new ItemStack(ItemInit.blood_absorption.get()));
		builder.addSlot(RecipeIngredientRole.CATALYST, SCULPTING_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, sculpting)
				.addRichTooltipCallback((view, tip) -> tip.add((recipe.sculpting().isPresent()
						? Component.translatable("hemomancy.jei.brazier_transmutation.sculpting.tip",
								JeiSculptingIcons.nameOf(recipe.sculpting().get()))
						: Component.translatable("hemomancy.jei.brazier_transmutation.any.tip")).withStyle(ChatFormatting.GRAY)));
		recipe.catalyst().ifPresent(catalyst -> builder.addSlot(RecipeIngredientRole.INPUT, CATALYST_X, SLOT_Y)
				.addItemStacks(java.util.Arrays.stream(catalyst.getItems())
						.map(stack -> stack.copyWithCount(recipe.catalystCount())).toList())
				.addRichTooltipCallback((view, tip) -> tip.add((recipe.catalystCount() > 1
						? Component.translatable("hemomancy.jei.brazier_transmutation.catalysts.tip", recipe.catalystCount())
						: Component.translatable("hemomancy.jei.brazier_transmutation.catalyst.tip"))
						.withStyle(ChatFormatting.GRAY))));
		builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOT_Y)
				.addIngredients(recipe.input())
				.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
						"hemomancy.jei.brazier_transmutation.offering.tip").withStyle(ChatFormatting.GRAY)));
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.result());
		recipe.byproduct().ifPresent(byproduct -> builder.addSlot(RecipeIngredientRole.OUTPUT, BYPRODUCT_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, byproduct));
	}

	private static List<JeiPanel.Stat> footer(BrazierTransmutationRecipe recipe) {
		return List.of(
				JeiPanel.Stat.of(JeiPanel.Icon.BLOOD, Component.translatable("hemomancy.jei.brazier_transmutation.cost",
						BloodGourdItem.formatBloodAmount(recipe.bloodCost())), JeiPanel.BLOOD_RED,
						Component.translatable("hemomancy.jei.brazier_transmutation.cost.tip")),
				JeiPanel.Stat.of(JeiPanel.Icon.NONE, Component.translatable("hemomancy.jei.brazier_transmutation.how"),
						JeiPanel.BLOOD.muted(), Component.translatable("hemomancy.jei.brazier_transmutation.how.tip")));
	}

	@Override
	public void draw(@Nonnull BrazierTransmutationRecipe recipe, @Nonnull IRecipeSlotsView slots,
			@Nonnull GuiGraphics gfx, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		JeiPanel.Theme t = JeiPanel.BLOOD;
		float time = JeiPanel.time();

		JeiPanel.panel(gfx, t, W, H, FOOTER_Y);
		JeiPanel.tintedSlot(gfx, t, SCULPTING_X, SLOT_Y, 0xFFB01A22);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.brazier_transmutation.selected"),
				SCULPTING_X + 8, SLOT_Y - 11, t.muted());
		if (recipe.catalyst().isPresent()) {
			JeiPanel.slot(gfx, t, CATALYST_X, SLOT_Y);
			JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.brazier_transmutation.off_hand"),
					CATALYST_X + 8, SLOT_Y + 21, t.muted());
		}
		// The offering sits in the bowl over a crimson flame.
		JeiPanel.flame(gfx, INPUT_X + 8, SLOT_Y + 20, time, 0xFF5A0A0E, 0xFFB01A22, 0xFFF0603A);
		JeiPanel.slot(gfx, t, INPUT_X, SLOT_Y);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.brazier_transmutation.offering"),
				INPUT_X + 8, SLOT_Y - 11, t.muted());
		JeiProgressArrow.draw(gfx, INPUT_X + 24, SLOT_Y + 7, OUTPUT_X - 8, time, t);
		JeiPanel.resultSlot(gfx, t, OUTPUT_X, SLOT_Y, t.accent(), time);
		if (recipe.byproduct().isPresent()) JeiPanel.slot(gfx, t, BYPRODUCT_X, SLOT_Y);
		JeiPanel.footer(gfx, font, footer(recipe), W, FOOTER_Y);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, BrazierTransmutationRecipe recipe, IRecipeSlotsView slots,
			double mouseX, double mouseY) {
		JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), W, FOOTER_Y, H, mouseX, mouseY);
	}
}
