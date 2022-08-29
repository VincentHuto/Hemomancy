
package com.vincenthuto.hemomancy.integration;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.recipe.BloodStructureRecipe;

//GlStateManager;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class BloodStructureRecipeCategory implements IRecipeCategory<BloodStructureRecipe> {
	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "blood_structure");
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;
	private static final ResourceLocation GUI_RECALLER = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/recaller_overlay.png");
	IGuiHelper guiHelper;
	int guiWidth = 170;
	int guiHeight = 100;

	public BloodStructureRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(170, 100);
		localizedName = I18n.get("hemomancy.jei.blood_structure");
		overlay = guiHelper.createDrawable(GUI_RECALLER, 0, 0, 170, 100);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(BlockInit.hematic_iron_block.get()));
		this.guiHelper = guiHelper;
	}

	@Nonnull
	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends BloodStructureRecipe> getRecipeClass() {
		return BloodStructureRecipe.class;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Nonnull
	@Override
	public TextComponent getTitle() {
		return new TextComponent(localizedName);
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void draw(BloodStructureRecipe recipe, PoseStack ms, double mouseX, double mouseY) {
		TranslatableComponent experienceString = new TranslatableComponent(String.valueOf(recipe.getResult()));
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		int stringWidth = fontRenderer.width(experienceString);
		fontRenderer.draw(ms, experienceString, background.getWidth() - stringWidth, 0, 0xFF808080);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BloodStructureRecipe recipe, IFocusGroup focuses) {
//		List<List<ItemStack>> list = new ArrayList<>();
//		for (Ingredient ingr : recipe.getIngredients()) {
//			list.add(Arrays.asList(ingr.getItems()));
//		}
//		builder.addSlot(RecipeIngredientRole.INPUT, 5, 11).addIngredient(VanillaTypes.ITEM,
//				new ItemStack(ItemInit.hematic_memory.get()));
//
//		if (list.size() > 0) {
//			builder.addSlot(RecipeIngredientRole.INPUT, 23, 11).addIngredients(VanillaTypes.ITEM, list.get(0));
//		}
//		builder.addSlot(RecipeIngredientRole.OUTPUT, 149, 77).addIngredient(VanillaTypes.ITEM, recipe.getResultItem());

	}

}
