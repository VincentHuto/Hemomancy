
package com.vincenthuto.hemomancy.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

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
		int centerX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2);
		int centerY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2);
		PoseStack ms = graphics.pose();
		ms.translate(87, 45, 0);
		drawCenter(graphics, recipe.getTendency(), centerX, centerY);
	}

	private void drawCenter(GuiGraphics graphics, Map<EnumBloodTendency, Float> tends, int xOff, int yOff) {
		float guiHeight = 228, guiWidth = 174;
		float left = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) - guiWidth / 2;
		float top = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) - guiHeight / 2;
		int centerOffset = 8;
		int cx = 0, cy = 0;
		float rotAngle = -90f;
		int iconDiameter = 70;
		int diameter = 15;
		float spikeBaseWidth = 23.5f;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int cx1 = (int) (cx + Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + xOff + 3;
			int cx2 = (int) (cx + Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + xOff + 3;
			int cy1 = (int) (cy + Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + yOff + 23;
			int cy2 = (int) (cy + Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + yOff + 23;
			double depthDist = ((iconDiameter - diameter) * tends.get(tend) * 0.2 + diameter);
			int lx = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * depthDist) + xOff + 3;
			int ly = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * depthDist) + yOff + 23;
			int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2))
					/ 2f);
			PoseStack ms = graphics.pose();
			HLGuiUtils.fracLine(ms, lx + centerOffset, ly + centerOffset, cx1 + centerOffset, cy1 + centerOffset,
					this.zLevel, tend.getColor(), displace, 1.1);
			HLGuiUtils.fracLine(ms, lx + centerOffset, ly + centerOffset, cx2 + centerOffset, cy2 + centerOffset,
					this.zLevel, tend.getColor(), displace, 1.1);
			HLGuiUtils.fracLine(ms, cx1 + centerOffset, cy1 + 8, lx + centerOffset, ly + centerOffset, this.zLevel,
					tend.getColor(), displace, 0.8);
			HLGuiUtils.fracLine(ms, cx2 + centerOffset, cy2 + centerOffset, lx + centerOffset, ly + centerOffset,
					this.zLevel, tend.getColor(), displace, 0.8);
			int newX = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
			int newY = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
			HLGuiUtils.renderItemStackInGui(graphics, new ItemStack(EnumBloodTendency.getRepEnzyme(tend)), newX, newY);
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
		List<List<ItemStack>> list = new ArrayList<>();
		for (Ingredient ingr : recipe.getIngredients()) {
			list.add(Arrays.asList(ingr.getItems()));
		}
		builder.addSlot(RecipeIngredientRole.INPUT, 5, 11).addIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(ItemInit.hematic_memory.get()));

		if (list.size() > 0) {
			builder.addSlot(RecipeIngredientRole.INPUT, 23, 11).addIngredients(VanillaTypes.ITEM_STACK, list.get(0));
		}
		builder.addSlot(RecipeIngredientRole.OUTPUT, 149, 77).addIngredient(VanillaTypes.ITEM_STACK,
				recipe.getResultItem(null));

	}

}