
package com.huto.hemomancy.integration.jei;

import java.util.Map;

import javax.annotation.Nonnull;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.recipe.RecipeRecaller;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RecallerRecipeCategory implements IRecipeCategory<RecipeRecaller> {
	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "visceral_artificial_recaller");
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;
	private static final ResourceLocation GUI_RECALLER = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/recaller_overlay.png");
	IGuiHelper guiHelper;

	public RecallerRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(170, 100);
		localizedName = I18n.format("hemomancy.jei.recaller");
		overlay = guiHelper.createDrawable(GUI_RECALLER, 0, 0, 170, 100);
		icon = guiHelper.createDrawableIngredient(new ItemStack(BlockInit.visceral_artificial_recaller.get()));
		this.guiHelper = guiHelper;
	}

	@Nonnull
	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends RecipeRecaller> getRecipeClass() {
		return RecipeRecaller.class;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setIngredients(RecipeRecaller recipe, IIngredients ingredients) {

		ingredients.setInput(VanillaTypes.ITEM, new ItemStack(ItemInit.hematic_memory.get()));
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void draw(RecipeRecaller recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		overlay.draw(matrixStack);
		GlStateManager.pushMatrix();
		int centerX = (Minecraft.getInstance().getMainWindow().getScaledWidth() / 2);
		int centerY = (Minecraft.getInstance().getMainWindow().getScaledHeight() / 2);
		GlStateManager.translated(centerX + 4, centerY + 22, 10);
		drawCenter(recipe.getTendency());
		GlStateManager.popMatrix();
	}

	private void drawCenter(Map<EnumBloodTendency, Float> tends) {
		GlStateManager.pushMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.translated(4.5, 5, 0);
		GlStateManager.scaled(0.25, 0.25, 0.25);
		int centerOffset = 8;
		int cx = 0, cy = 0;
		float rotAngle = -90f;
		int distance = 85;
		int diameter = 35;
		float spikeBaseWidth = 23.5f;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			double cx1 = ((double) cx
					+ Math.cos(Math.toRadians((float) rotAngle + spikeBaseWidth)) * (double) diameter);
			double cx2 = ((double) cx
					+ Math.cos(Math.toRadians((float) rotAngle - spikeBaseWidth)) * (double) diameter);
			double cy1 = ((double) cy
					+ Math.sin(Math.toRadians((float) rotAngle + spikeBaseWidth)) * (double) diameter);
			double cy2 = ((double) cy
					+ Math.sin(Math.toRadians((float) rotAngle - spikeBaseWidth)) * (double) diameter);
			double depthDist = ((float) (distance - diameter) * tends.get(tend) + (float) diameter);
			int lx = (int) ((double) cx + Math.cos(Math.toRadians(rotAngle)) * (double) depthDist);
			int ly = (int) ((double) cy + Math.sin(Math.toRadians(rotAngle)) * (double) depthDist);
			int displace = (int) ((float) (Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2)
					- Math.min(cy1, cy2)) / 2f);
			GuiUtils.fracLine(lx + centerOffset, ly + centerOffset, cx1 + centerOffset, cy1 + centerOffset, 10,
					tend.getColor().getColor(), displace, 1.1);
			GuiUtils.fracLine(lx + centerOffset, ly + centerOffset, cx2 + centerOffset, cy2 + centerOffset, 10,
					tend.getColor().getColor(), displace, 1.1);
			GuiUtils.fracLine(cx1 + centerOffset, cy1 + 8, lx + centerOffset, ly + centerOffset, 10,
					tend.getColor().getColor(), displace, 0.8);
			GuiUtils.fracLine(cx2 + centerOffset, cy2 + centerOffset, lx + centerOffset, ly + centerOffset, 10,
					tend.getColor().getColor(), displace, 0.8);
			rotAngle += 45;
		}
		GlStateManager.popMatrix();
		GlStateManager.scaled(0.75, 0.75, 0.75);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int newX = (int) ((double) cx + Math.cos(Math.toRadians(rotAngle)) * (double) distance / 1.75);
			int newY = (int) ((double) cy + Math.sin(Math.toRadians(rotAngle)) * (double) distance / 1.75);
			mc.getItemRenderer().renderItemIntoGUI(new ItemStack(EnumBloodTendency.getRepEnzyme(tend)), newX, newY);
			rotAngle += 44.5f;

		}
		GlStateManager.popMatrix();
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull RecipeRecaller recipe,
			@Nonnull IIngredients ingredients) {
		int runeIn = 1;
		int outputRune = 3;
		recipeLayout.getItemStacks().init(runeIn, true, 4, 10);
		recipeLayout.getItemStacks().set(runeIn, ingredients.getInputs(VanillaTypes.ITEM).get(0));
		recipeLayout.getItemStacks().init(outputRune, false, 148, 76);
		recipeLayout.getItemStacks().set(outputRune, ingredients.getOutputs(VanillaTypes.ITEM).get(0));

	}

	Minecraft mc = Minecraft.getInstance();

}
