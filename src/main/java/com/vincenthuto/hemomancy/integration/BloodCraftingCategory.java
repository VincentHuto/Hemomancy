
package com.vincenthuto.hemomancy.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.recipe.RecipeBaseBloodCrafting;
import com.vincenthuto.hutoslib.client.render.block.MultiblockPattern;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;
import com.vincenthuto.hutoslib.client.screen.guide.ScreenBlockTintGetter;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class BloodCraftingCategory implements IRecipeCategory<RecipeBaseBloodCrafting> {
	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "blood_crafting");
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;

	public BloodCraftingCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 110);
		localizedName = I18n.get("hemomancy.jei.blood_crafting");
		overlay = guiHelper.createDrawable(new ResourceLocation("hemomancy", "textures/gui/bloodcraftingoverlay.png"),
				0, 0, 150, 110);
		icon = guiHelper.createDrawableIngredient(new ItemStack(ItemInit.sanguine_formation.get()));

	}

	@Nonnull
	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends RecipeBaseBloodCrafting> getRecipeClass() {
		return RecipeBaseBloodCrafting.class;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@SuppressWarnings("serial")
	@Override
	public void setIngredients(RecipeBaseBloodCrafting recipe, IIngredients ingredients) {
//		List<List<ItemStack>> list = new ArrayList<>();
//		List<ItemStack> heldStack = new ArrayList<ItemStack>() {
//			{
//				add(new ItemStack(recipe.getHeldItem()));
//			}
//		};
//		List<ItemStack> hitStack = new ArrayList<ItemStack>() {
//			{
//				add(new ItemStack(recipe.getHitBlock().asItem()));
//			}
//		};
//		Collections.addAll(list, heldStack, hitStack);
//		ingredients.setInputLists(VanillaTypes.ITEM, list);
//		ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(recipe.getCreation()));
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return new TextComponent(localizedName);
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	int left, top;
	static int guiWidth = 176;
	static int guiHeight = 186;
	int centerX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) - guiWidth / 2;
	int centerY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2) - guiHeight / 2;

	@Override
	public void draw(RecipeBaseBloodCrafting recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		overlay.draw(matrixStack);

		renderPatternInGUI(matrixStack, Minecraft.getInstance(), recipe.getMultiblockPattern(), mouseX, mouseY);
//		Minecraft.getInstance().font.drawWordWrap(new TextComponent("Held Item"), -50,
//				(int) (Minecraft.getInstance().font.lineHeight) - 22, 150, 0);
//		Minecraft.getInstance().font.drawWordWrap(new TextComponent("Hit Block"), -50,
//				(int) (Minecraft.getInstance().font.lineHeight) + 25, 150, 0);

	}

	public static void renderPatternInGUI(PoseStack matrices, Minecraft mc, MultiblockPattern pattern, double xOff,
			double yOff) {
		HLGuiUtils.renderMultiBlock(matrices, pattern, 1, new ScreenBlockTintGetter(), 1, 1 * 5);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeBaseBloodCrafting recipe, IFocusGroup focuses) {

		List<List<ItemStack>> list = new ArrayList<>();
		List<ItemStack> heldStack = new ArrayList<ItemStack>() {
			{
				add(new ItemStack(recipe.getHeldItem()));
			}
		};
		List<ItemStack> hitStack = new ArrayList<ItemStack>() {
			{
				add(new ItemStack(recipe.getHitBlock().asItem()));
			}
		};
		Collections.addAll(list, heldStack, hitStack);
		// builder.addSlot(RecipeIngredientRole.OUTPUT, 117,
		// 36).addIngredient(VanillaTypes.ITEM, recipe.getResultItem());

//		ingredients.setInputLists(VanillaTypes.ITEM, list);
//		ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(recipe.getCreation()));

		if (list.size() > 1) {
			builder.addSlot(RecipeIngredientRole.INPUT, 5, 31).addIngredients(VanillaTypes.ITEM, list.get(0));
			builder.addSlot(RecipeIngredientRole.INPUT, 5, 51).addIngredients(VanillaTypes.ITEM, list.get(1));
			builder.addSlot(RecipeIngredientRole.OUTPUT, 115, 41).addIngredient(VanillaTypes.ITEM,
					new ItemStack(recipe.getCreation()));
		}
	}

}
