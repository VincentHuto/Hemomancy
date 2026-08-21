package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.recipe.BloodInfusionRecipe;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public final class BloodInfusionRecipeCategory implements IRecipeCategory<BloodInfusionRecipe> {
	public static final RecipeType<BloodInfusionRecipe> JEI_TYPE =
			RecipeType.create(Hemomancy.MOD_ID, "blood_infusion", BloodInfusionRecipe.class);
	private final IDrawable background;
	private final IDrawable icon;

	public BloodInfusionRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 54);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(ItemInit.blood_projection.get()));
	}

	@Override
	public RecipeType<BloodInfusionRecipe> getRecipeType() {
		return JEI_TYPE;
	}

	@Override
	public Component getTitle() {
		return Component.translatable("hemomancy.jei.blood_infusion");
	}

	@SuppressWarnings("removal")
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull BloodInfusionRecipe recipe,
			@Nonnull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 12, 18)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(recipe.input()));
		builder.addSlot(RecipeIngredientRole.CATALYST, 66, 18)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemInit.blood_projection.get()));
		builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 18)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.resultStack());
	}

	@Override
	public void draw(@Nonnull BloodInfusionRecipe recipe, @Nonnull IRecipeSlotsView slots,
			@Nonnull GuiGraphics graphics, double mouseX, double mouseY) {
		var font = Minecraft.getInstance().font;
		Component cost = Component.translatable("hemomancy.jei.blood_infusion.cost",
				BloodGourdItem.formatBloodAmount(recipe.bloodCost()));
		graphics.drawString(font, cost, (150 - font.width(cost)) / 2, 40, 0xFFB03030, false);
	}
}
