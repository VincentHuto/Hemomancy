
package com.huto.hemomancy.integration.jei;

import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

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
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RecallerRecipeCategory implements IRecipeCategory<RecipeRecaller> {
	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "visceral_artificial_recaller");
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;
	private static final ResourceLocation GUI_RECALLER = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/recaller_gui.png");

	public RecallerRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 110);
		localizedName = I18n.format("hemomancy.jei.recaller");
		overlay = guiHelper.createDrawable(GUI_RECALLER, 0, 0, 150, 110);
		icon = guiHelper.createDrawableIngredient(new ItemStack(BlockInit.visceral_artificial_recaller.get()));

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

	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	int centerX = (Minecraft.getInstance().getMainWindow().getScaledWidth() / 2) - guiWidth + 200 / 2;
	int centerY = (Minecraft.getInstance().getMainWindow().getScaledHeight() / 2) - guiHeight + 300 / 2;
	private int zLevel = 10;

	@Override
	public void draw(RecipeRecaller recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		overlay.draw(matrixStack);
		GlStateManager.pushMatrix();
		GlStateManager.translatef(centerX, centerY, 10);
		drawCenter(recipe.getTendency());
		GlStateManager.popMatrix();

	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull RecipeRecaller recipe,
			@Nonnull IIngredients ingredients) {
		recipeLayout.getItemStacks().init(0, true, 22, 0);
		recipeLayout.getItemStacks().set(0, new ItemStack(BlockInit.visceral_artificial_recaller.get()));

		int runeIn = 1;
		int outputRune = 3;
		recipeLayout.getItemStacks().init(runeIn, true, 3, 32);
		recipeLayout.getItemStacks().set(runeIn, ingredients.getInputs(VanillaTypes.ITEM).get(0));
		recipeLayout.getItemStacks().init(outputRune, false, 114, 40);
		recipeLayout.getItemStacks().set(outputRune, ingredients.getOutputs(VanillaTypes.ITEM).get(0));

	}

	public static void drawLine(double src_x, double src_y, double dst_x, double dst_y, int zLevel, int color,
			int displace) {
		GL11.glDisable((int) 3553);
		GL11.glLineWidth((float) 1.0f);
		GL11.glColor3f((float) ((float) ((color & 0xFF0000) >> 16) / 255.0f),
				(float) ((float) ((color & 0xFF00) >> 8) / 255.0f), (float) ((float) (color & 0xFF) / 255.0f));
		GL11.glBegin((int) 1);
		GL11.glVertex3f((float) src_x, (float) src_y, (float) zLevel);
		GL11.glVertex3f((float) dst_x, (float) dst_y, (float) zLevel);
		GL11.glEnd();
		GL11.glColor3f((float) 1.0f, (float) 1.0f, (float) 1.0f);
		GL11.glEnable((int) 3553);
	}

	public static void fracLine(double src_x, double src_y, double dst_x, double dst_y, int zLevel, int color,
			int displace, double detail) {
		if (displace < detail) {
			drawLine(src_x, src_y, dst_x, dst_y, zLevel, color, displace);
		} else {
			Random rand = new Random();
			double mid_x = (dst_x + src_x) / 2;
			double mid_y = (dst_y + src_y) / 2;
			mid_x = ((double) mid_x + ((double) rand.nextFloat() - 0.5) * (double) displace * 0.5);
			mid_y = ((double) mid_y + ((double) rand.nextFloat() - 0.5) * (double) displace * 0.5);
			fracLine(src_x, src_y, mid_x, mid_y, zLevel, color, (displace / 2), detail);
			fracLine(dst_x, dst_y, mid_x, mid_y, zLevel, color, (displace / 2), detail);

		}
	}

	Minecraft mc = Minecraft.getInstance();

	private void drawCenter(Map<EnumBloodTendency, Float> tends) {
		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 2, 0);
		GlStateManager.pushMatrix();
		GlStateManager.scaled(0.25, 0.25, 0.25);
		GlStateManager.translated(385, 210, 0);
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
			fracLine(lx + centerOffset, ly + centerOffset, cx1 + centerOffset, cy1 + centerOffset, this.zLevel,
					tend.getColor().getColor(), displace, 1.1);
			fracLine(lx + centerOffset, ly + centerOffset, cx2 + centerOffset, cy2 + centerOffset, this.zLevel,
					tend.getColor().getColor(), displace, 1.1);
			fracLine(cx1 + centerOffset, cy1 + 8, lx + centerOffset, ly + centerOffset, this.zLevel,
					tend.getColor().getColor(), displace, 0.8);
			fracLine(cx2 + centerOffset, cy2 + centerOffset, lx + centerOffset, ly + centerOffset, this.zLevel,
					tend.getColor().getColor(), displace, 0.8);
			rotAngle += 45;
		}
		GlStateManager.popMatrix();
		GlStateManager.scaled(0.75, 0.75, 0.75);
		GlStateManager.translated(122, 65, 0);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int newX = (int) ((double) cx + Math.cos(Math.toRadians(rotAngle)) * (double) distance / 1.75);
			int newY = (int) ((double) cy + Math.sin(Math.toRadians(rotAngle)) * (double) distance / 1.75);
			mc.getItemRenderer().renderItemIntoGUI(new ItemStack(EnumBloodTendency.getRepEnzyme(tend)), newX, newY);
			rotAngle += 44.5f;

		}
		GlStateManager.popMatrix();
	}

}
