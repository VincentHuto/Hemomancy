
package com.vincenthuto.hemomancy.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.RecallerRecipe;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;

//GlStateManager;

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
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class RecallerRecipeCategory implements IRecipeCategory<RecallerRecipe> {
	public static final ResourceLocation UID = Hemomancy.rloc("visceral_artificial_recaller");
	private static final ResourceLocation GUI_RECALLER = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/recaller_overlay.png");
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;
	IGuiHelper guiHelper;
	int guiWidth = 170;
	int guiHeight = 100;

	private int zLevel = 10;

	Minecraft mc = Minecraft.getInstance();

	public RecallerRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(170, 100);
		localizedName = I18n.get("hemomancy.jei.recaller");
		overlay = guiHelper.createDrawable(GUI_RECALLER, 0, 0, 170, 100);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(BlockInit.visceral_artificial_recaller.get()));
		this.guiHelper = guiHelper;
	}

	@Override
	public void draw(RecallerRecipe recipe, IRecipeSlotsView recipeSlotsView,GuiGraphics graphics, double mouseX,
			double mouseY) {
		overlay.draw(graphics);

		// JEI's draw() has the pose stack pre-translated so (0,0) = recipe area top-left.
		// HLGuiUtils.fracLine / renderItemStackInGui apply the pose stack transform internally,
		// so we must extract the current translation to compute absolute screen coords,
		// then reset to identity for drawing — exactly matching TendencyViewScreen's approach.
		PoseStack ms = graphics.pose();
		Matrix4f mat = ms.last().pose();

		// Extract the JEI translation (recipe area top-left in screen coords)
		float recipeAreaX = mat.m30();
		float recipeAreaY = mat.m31();

		// Compute the absolute screen position of the center of the recipe area
		int absCenterX = (int) recipeAreaX + 94;
		int absCenterY = (int) recipeAreaY + 52;

		// Undo JEI's translation so we draw with raw screen coords
		ms.pushPose();
		ms.translate(-recipeAreaX, -recipeAreaY, 0);

		drawCenter(graphics, recipe.getTendency(), absCenterX, absCenterY);

		ms.popPose();
	}

	private void drawCenter(GuiGraphics graphics, Map<EnumBloodTendency, Float> tends, int centerX, int centerY) {
		float rotAngle = -90f;
		int iconDiameter = 70;
		int diameter = 15;
		float spikeBaseWidth = 23.5f;
		int itemSize = 16;
		int halfItem = itemSize / 2;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int cx1 = centerX + (int) (Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter);
			int cx2 = centerX + (int) (Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter);
			int cy1 = centerY + (int) (Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter);
			int cy2 = centerY + (int) (Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter);
			double depthDist = ((iconDiameter - diameter) * tends.get(tend) * 0.2 + diameter);
			int lx = centerX + (int) (Math.cos(Math.toRadians(rotAngle)) * depthDist);
			int ly = centerY + (int) (Math.sin(Math.toRadians(rotAngle)) * depthDist);
			int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2))
					/ 2f);
			PoseStack ms = graphics.pose();
			HLGuiUtils.fracLine(ms, lx, ly, cx1, cy1,
					this.zLevel, tend.getColor(), displace, 1.1);
			HLGuiUtils.fracLine(ms, lx, ly, cx2, cy2,
					this.zLevel, tend.getColor(), displace, 1.1);
			HLGuiUtils.fracLine(ms, cx1, cy1, lx, ly, this.zLevel,
					tend.getColor(), displace, 0.8);
			HLGuiUtils.fracLine(ms, cx2, cy2, lx, ly,
					this.zLevel, tend.getColor(), displace, 0.8);
			double iconDist = iconDiameter / 1.75;
			int iconCenterX = centerX + (int) (Math.cos(Math.toRadians(rotAngle)) * iconDist);
			int iconCenterY = centerY + (int) (Math.sin(Math.toRadians(rotAngle)) * iconDist);
			HLGuiUtils.renderItemStackInGui(graphics, new ItemStack(EnumBloodTendency.getRepEnzyme(tend)),
					iconCenterX - halfItem, iconCenterY - halfItem);
			rotAngle += 45;
		}
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public RecipeType<RecallerRecipe> getRecipeType() {
		return JEIPlugin.recaller_recipe_type;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.literal(localizedName);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecallerRecipe recipe, IFocusGroup focuses) {
		// Hematic Memory input - always required
		builder.addSlot(RecipeIngredientRole.INPUT, 5, 11).addIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(ItemInit.hematic_memory.get()));

		// Enzyme/catalyst ingredient slot - show if recipe has a non-empty ingredient
		List<List<ItemStack>> list = new ArrayList<>();
		for (Ingredient ingr : recipe.getIngredients()) {
			if (ingr != null && ingr != Ingredient.EMPTY && ingr.getItems().length > 0) {
				list.add(Arrays.asList(ingr.getItems()));
			}
		}
		if (!list.isEmpty()) {
			builder.addSlot(RecipeIngredientRole.INPUT, 23, 11).addIngredients(VanillaTypes.ITEM_STACK, list.get(0));
		}

		// Output slot
		builder.addSlot(RecipeIngredientRole.OUTPUT, 149, 77).addIngredient(VanillaTypes.ITEM_STACK,
				recipe.getResultItem(null));

	}

}
