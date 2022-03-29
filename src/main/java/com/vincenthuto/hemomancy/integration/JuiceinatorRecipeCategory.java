
package com.vincenthuto.hemomancy.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.recipe.JuiceinatorRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class JuiceinatorRecipeCategory implements IRecipeCategory<JuiceinatorRecipe> {

	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "juiceinator");
	public static final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/juiceinator_gui_overlay.png");

	private final IDrawable background;
	@SuppressWarnings("unused")
	private final String localizedName = "Juiceinator";
	private final IDrawable overlay;
	private final IDrawable icon;
	private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;
	protected IDrawableStatic staticFlame;
	protected IDrawableAnimated animatedFlame;

	@SuppressWarnings("deprecation")
	public JuiceinatorRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 110);
		overlay = guiHelper.createDrawable(texture, 0, 0, 150, 110);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(BlockInit.juiceinator.get()));
		this.staticFlame = guiHelper.createDrawable(texture, 143 + 16, 0, 14, 14);
		this.animatedFlame = guiHelper.createAnimatedDrawable(staticFlame, 300, IDrawableAnimated.StartDirection.TOP,
				true);
		this.cachedArrows = CacheBuilder.newBuilder().maximumSize(25).build(new CacheLoader<>() {
			@Override
			public IDrawableAnimated load(Integer cookTime) {
				return guiHelper.drawableBuilder(texture, 143 + 16, 14, 24, 17).buildAnimated(cookTime,
						IDrawableAnimated.StartDirection.LEFT, false);
			}
		});
	}

	@Nonnull
	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Nonnull
	@Override
	public Class<? extends JuiceinatorRecipe> getRecipeClass() {
		return JuiceinatorRecipe.class;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return new TextComponent("Juiceinator");
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Nonnull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	protected void drawExperience(JuiceinatorRecipe recipe, PoseStack poseStack, int y) {
		float experience = recipe.getExperience();
		if (experience > 0) {
			TranslatableComponent experienceString = new TranslatableComponent("gui.jei.category.smelting.experience",
					experience);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(experienceString);
			fontRenderer.draw(poseStack, experienceString, background.getWidth() - stringWidth, y, 0xFF808080);
		}
	}

	protected void drawCookTime(JuiceinatorRecipe recipe, PoseStack poseStack, int y) {
		int cookTime = recipe.getCookingTime();
		if (cookTime > 0) {
			int cookTimeSeconds = cookTime / 20;
			TranslatableComponent timeString = new TranslatableComponent("gui.jei.category.smelting.time.seconds",
					cookTimeSeconds);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(timeString);
			fontRenderer.draw(poseStack, timeString, background.getWidth() - stringWidth, y, 0xFF808080);
		}
	}

	protected IDrawableAnimated getArrow(JuiceinatorRecipe recipe) {
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) {
			cookTime = 200;
		}
		return this.cachedArrows.getUnchecked(cookTime);
	}

	@Override
	public void draw(JuiceinatorRecipe recipe, PoseStack PoseStack, double mouseX, double mouseY) {
		overlay.draw(PoseStack);
		animatedFlame.draw(PoseStack, 57, 37);
		IDrawableAnimated arrow = getArrow(recipe);
		arrow.draw(PoseStack, 79, 34);
		drawExperience(recipe, PoseStack, 0);
		drawCookTime(recipe, PoseStack, 60);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, JuiceinatorRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 36).addIngredient(VanillaTypes.ITEM, recipe.getResultItem());
		List<List<ItemStack>> list = new ArrayList<>();
		for (Ingredient ingr : recipe.getIngredients()) {
			list.add(Arrays.asList(ingr.getItems()));
		}
		builder.addSlot(RecipeIngredientRole.INPUT, 56, 17).addIngredients(VanillaTypes.ITEM, list.get(0));

	}

}