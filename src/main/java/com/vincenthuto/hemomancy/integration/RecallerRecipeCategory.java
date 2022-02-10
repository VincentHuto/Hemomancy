
package com.vincenthuto.hemomancy.integration;

import java.util.Map;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.gui.HemoGuiUtils;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.recipe.RecipeRecaller;

//GlStateManager;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
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
	int guiWidth = 170;
	int guiHeight = 100;

	public RecallerRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(170, 100);
		localizedName = I18n.get("hemomancy.jei.recaller");
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
	public TextComponent getTitle() {
		return new TextComponent(localizedName);
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void draw(RecipeRecaller recipe, PoseStack ms, double mouseX, double mouseY) {
		overlay.draw(ms);
		int centerX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2);
		int centerY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2);
		ms.translate(87, 45, 0);
		drawCenter(ms, recipe.getTendency(), 177, 140);
	}

	private int zLevel = 10;

	private void drawCenter(PoseStack ms, Map<EnumBloodTendency, Float> tends, int xOff, int yOff) {
		int centerOffset = 8;
		int cx = 0, cy = 0;
		float rotAngle = -90f;
		int iconDiameter = 70;
		int diameter = 15;
		float spikeBaseWidth = 23.5f;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int cx1 = (int) (cx + Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + xOff;
			int cx2 = (int) (cx + Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + xOff;
			int cy1 = (int) (cy + Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + yOff;
			int cy2 = (int) (cy + Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + yOff;
			double depthDist = ((iconDiameter - diameter) * tends.get(tend) * 0.2 + diameter);
			int lx = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * depthDist) + xOff;
			int ly = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * depthDist) + yOff;
			int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2))
					/ 2f);
			HemoGuiUtils.fracLine(ms, lx + centerOffset, ly + centerOffset, cx1 + centerOffset, cy1 + centerOffset,
					this.zLevel, tend.getColor(), displace, 1.1);
			HemoGuiUtils.fracLine(ms, lx + centerOffset, ly + centerOffset, cx2 + centerOffset, cy2 + centerOffset,
					this.zLevel, tend.getColor(), displace, 1.1);
			HemoGuiUtils.fracLine(ms, cx1 + centerOffset, cy1 + 8, lx + centerOffset, ly + centerOffset, this.zLevel,
					tend.getColor(), displace, 0.8);
			HemoGuiUtils.fracLine(ms, cx2 + centerOffset, cy2 + centerOffset, lx + centerOffset, ly + centerOffset,
					this.zLevel, tend.getColor(), displace, 0.8);
			int newX = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
			int newY = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
			HemoGuiUtils.renderItemStackInGui(ms, new ItemStack(EnumBloodTendency.getRepEnzyme(tend)), newX, newY);
			rotAngle += 45;
		}
		// GlStateManager._popMatrix();
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
