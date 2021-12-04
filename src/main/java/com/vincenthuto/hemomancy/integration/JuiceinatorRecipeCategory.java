//
//package com.vincenthuto.hemomancy.integration;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import javax.annotation.Nonnull;
//
//import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.CacheLoader;
//import com.google.common.cache.LoadingCache;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.vincenthuto.hemomancy.Hemomancy;
//import com.vincenthuto.hemomancy.init.BlockInit;
//import com.vincenthuto.hemomancy.recipe.JuiceinatorDataRecipe;
//
//import mezz.jei.api.constants.VanillaTypes;
//import mezz.jei.api.gui.IRecipeLayout;
//import mezz.jei.api.gui.drawable.IDrawable;
//import mezz.jei.api.gui.drawable.IDrawableAnimated;
//import mezz.jei.api.gui.drawable.IDrawableStatic;
//import mezz.jei.api.helpers.IGuiHelper;
//import mezz.jei.api.ingredients.IIngredients;
//import mezz.jei.api.recipe.category.IRecipeCategory;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.TextComponent;
//import net.minecraft.network.chat.TranslatableComponent;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.Ingredient;
//
//public class JuiceinatorRecipeCategory implements IRecipeCategory<JuiceinatorDataRecipe> {
//
//	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "juiceinator");
//	public static final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
//			"textures/gui/juiceinator_gui_overlay.png");
//
//	private final IDrawable background;
//	@SuppressWarnings("unused")
//	private final String localizedName = "Juiceinator";
//	private final IDrawable overlay;
//	private final IDrawable icon;
//	private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;
//	protected IDrawableStatic staticFlame;
//	protected IDrawableAnimated animatedFlame;
//
//	public JuiceinatorRecipeCategory(IGuiHelper guiHelper) {
//		background = guiHelper.createBlankDrawable(150, 110);
//		overlay = guiHelper.createDrawable(texture, 0, 0, 150, 110);
//		icon = guiHelper.createDrawableIngredient(new ItemStack(BlockInit.juiceinator.get()));
//		this.staticFlame = guiHelper.createDrawable(texture, 143+16, 0, 14, 14);
//		this.animatedFlame = guiHelper.createAnimatedDrawable(staticFlame, 300, IDrawableAnimated.StartDirection.TOP,
//				true);
//		this.cachedArrows = CacheBuilder.newBuilder().maximumSize(25).build(new CacheLoader<>() {
//			@Override
//			public IDrawableAnimated load(Integer cookTime) {
//				return guiHelper.drawableBuilder(texture, 143+16, 14, 24, 17).buildAnimated(cookTime,
//						IDrawableAnimated.StartDirection.LEFT, false);
//			}
//		});
//	}
//
//	@Nonnull
//	@Override
//	public ResourceLocation getUid() {
//		return UID;
//	}
//
//	@Nonnull
//	@Override
//	public Class<? extends JuiceinatorDataRecipe> getRecipeClass() {
//		return JuiceinatorDataRecipe.class;
//	}
//
//	@Nonnull
//	@Override
//	public Component getTitle() {
//		return new TextComponent("Juiceinator");
//	}
//
//	@Nonnull
//	@Override
//	public IDrawable getBackground() {
//		return background;
//	}
//
//	@Nonnull
//	@Override
//	public IDrawable getIcon() {
//		return icon;
//	}
//
//	@Override
//	public void setIngredients(JuiceinatorDataRecipe recipe, IIngredients iIngredients) {
//		List<List<ItemStack>> list = new ArrayList<>();
//		for (Ingredient ingr : recipe.getIngredients()) {
//			list.add(Arrays.asList(ingr.getItems()));
//		}
//		iIngredients.setInputLists(VanillaTypes.ITEM, list);
//		iIngredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
//	}
//
//	protected void drawExperience(JuiceinatorDataRecipe recipe, PoseStack poseStack, int y) {
//		float experience = recipe.getExperience();
//		if (experience > 0) {
//			TranslatableComponent experienceString = new TranslatableComponent("gui.jei.category.smelting.experience",
//					experience);
//			Minecraft minecraft = Minecraft.getInstance();
//			Font fontRenderer = minecraft.font;
//			int stringWidth = fontRenderer.width(experienceString);
//			fontRenderer.draw(poseStack, experienceString, background.getWidth() - stringWidth, y, 0xFF808080);
//		}
//	}
//
//	protected void drawCookTime(JuiceinatorDataRecipe recipe, PoseStack poseStack, int y) {
//		int cookTime = recipe.getCookingTime();
//		if (cookTime > 0) {
//			int cookTimeSeconds = cookTime / 20;
//			TranslatableComponent timeString = new TranslatableComponent("gui.jei.category.smelting.time.seconds",
//					cookTimeSeconds);
//			Minecraft minecraft = Minecraft.getInstance();
//			Font fontRenderer = minecraft.font;
//			int stringWidth = fontRenderer.width(timeString);
//			fontRenderer.draw(poseStack, timeString, background.getWidth() - stringWidth, y, 0xFF808080);
//		}
//	}
//
//	protected IDrawableAnimated getArrow(JuiceinatorDataRecipe recipe) {
//		int cookTime = recipe.getCookingTime();
//		if (cookTime <= 0) {
//			cookTime = 200;
//		}
//		return this.cachedArrows.getUnchecked(cookTime);
//	}
//
//	@Override
//	public void draw(JuiceinatorDataRecipe recipe, PoseStack PoseStack, double mouseX, double mouseY) {
//		overlay.draw(PoseStack);
//		animatedFlame.draw(PoseStack, 57, 37);
//		IDrawableAnimated arrow = getArrow(recipe);
//		arrow.draw(PoseStack, 79, 34);
//		drawExperience(recipe, PoseStack, 0);
//		drawCookTime(recipe, PoseStack, 60);
//	}
//
//	@Override
//	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull JuiceinatorDataRecipe recipe,
//			@Nonnull IIngredients ingredients) {
//		recipeLayout.getItemStacks().init(0, true, 116, 35);
//		recipeLayout.getItemStacks().set(0, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
//		recipeLayout.getItemStacks().init(1, true, 55, 16);
//		recipeLayout.getItemStacks().set(1, ingredients.getInputs(VanillaTypes.ITEM).get(0));
//	}
//
//}