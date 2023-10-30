
package com.vincenthuto.hemomancy.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.JuiceinatorRecipe;
import com.vincenthuto.hemomancy.common.registry.BlockInit;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class JuiceinatorRecipeCategory implements IRecipeCategory<JuiceinatorRecipe> {

	public static final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/juiceinator_gui_overlay.png");

	private final IDrawable background;
	private final IDrawable overlay;
	private final IDrawable icon;
	private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;
	protected IDrawableStatic staticFlame;
	protected IDrawableAnimated animatedFlame;

	public JuiceinatorRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 110);
		overlay = guiHelper.createDrawable(texture, 0, 0, 150, 110);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockInit.juiceinator.get()));
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

	@Override
	public void draw(JuiceinatorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
			double mouseY) {
		overlay.draw(graphics);
		animatedFlame.draw(graphics, 57, 37);
		IDrawableAnimated arrow = getArrow(recipe);
		arrow.draw(graphics, 79, 34);
		drawExperience(recipe, graphics, 0);
		drawCookTime(recipe, graphics, 80);
	}

	protected void drawCookTime(JuiceinatorRecipe recipe, GuiGraphics graphics, int y) {
		int cookTime = recipe.getCookingTime();
		if (cookTime > 0) {
			int cookTimeSeconds = cookTime / 20;
			MutableComponent timeString = Component.translatable("gui.jei.category.smelting.time.seconds",
					cookTimeSeconds);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(timeString);
			graphics.drawString(fontRenderer, timeString, background.getWidth() - stringWidth, y, 0xFF808080, false);
		}
	}

	protected void drawExperience(JuiceinatorRecipe recipe, GuiGraphics graphics, int y) {
		float experience = recipe.getExperience();
		if (experience > 0) {
			MutableComponent experienceString = Component.translatable("gui.jei.category.smelting.experience",
					experience);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(experienceString);
			graphics.drawString(fontRenderer, experienceString, background.getWidth() - stringWidth, y, 0xFF808080,
					false);

		}
	}

	protected IDrawableAnimated getArrow(JuiceinatorRecipe recipe) {
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) {
			cookTime = 200;
		}
		return this.cachedArrows.getUnchecked(cookTime);
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

	@Override
	public RecipeType<JuiceinatorRecipe> getRecipeType() {
		return JEIPlugin.juiceinator_recipe_type;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.literal("Juiceinator");
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, JuiceinatorRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 36).addIngredient(VanillaTypes.ITEM_STACK,
				recipe.getResultItem(null));
		List<List<ItemStack>> list = new ArrayList<>();
		for (Ingredient ingr : recipe.getIngredients()) {
			list.add(Arrays.asList(ingr.getItems()));
		}
		builder.addSlot(RecipeIngredientRole.INPUT, 56, 17).addIngredients(VanillaTypes.ITEM_STACK, list.get(0));

	}

}