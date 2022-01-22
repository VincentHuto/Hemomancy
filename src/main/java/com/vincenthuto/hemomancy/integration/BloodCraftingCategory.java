
package com.vincenthuto.hemomancy.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.recipe.RecipeBaseBloodCrafting;
import com.vincenthuto.hutoslib.client.render.block.BlockPosBlockPair;
import com.vincenthuto.hutoslib.client.render.block.LabeledBlockPattern;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
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

	@SuppressWarnings("deprecation")
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
		ingredients.setInputLists(VanillaTypes.ITEM, list);
		ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(recipe.getCreation()));
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
	int guiWidth = 176;
	int guiHeight = 186;
	int centerX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) - guiWidth / 2;
	int centerY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2) - guiHeight / 2;

	@Override
	public void draw(RecipeBaseBloodCrafting recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		overlay.draw(matrixStack);
		renderPatternInGUI(matrixStack, Minecraft.getInstance(), recipe.getBundledPattern(), centerX + 50,
				centerY + 50);
		Minecraft.getInstance().font.drawWordWrap(new TextComponent("Held Item"), -50,
				(int) (Minecraft.getInstance().font.lineHeight) - 22, 150, 0);
		Minecraft.getInstance().font.drawWordWrap(new TextComponent("Hit Block"), -50,
				(int) (Minecraft.getInstance().font.lineHeight) + 25, 150, 0);

	}

	public static void renderPatternInGUI(PoseStack ms, Minecraft mc, LabeledBlockPattern pattern, double xOff,
			double yOff) {
		PoseStack viewModelPose = RenderSystem.getModelViewStack();

		viewModelPose.pushPose();
		Lighting.setupFor3DItems();
		List<BlockPosBlockPair> patternList = pattern.getBlockPosBlockList();
		viewModelPose.mulPose(new Quaternion(Vector3f.YP, 45, true));
		viewModelPose.scale(1f, 1f, 1f);
		for (BlockPosBlockPair pair : patternList) {
			viewModelPose.translate(0, 2, 1);

			mc.getItemRenderer().renderAndDecorateItem(new ItemStack(pair.getBlock()),
					(int) xOff + pair.getPos().getX() * -16, (int) yOff + pair.getPos().getZ() * -16,
					(int) pair.getPos().getY() * -16);

		}
		viewModelPose.popPose();
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull RecipeBaseBloodCrafting recipe,
			@Nonnull IIngredients ingredients) {
		if (ingredients.getInputs(VanillaTypes.ITEM).size() > 1) {
			recipeLayout.getItemStacks().init(1, true, 4, 30);
			recipeLayout.getItemStacks().set(1, ingredients.getInputs(VanillaTypes.ITEM).get(0));
			recipeLayout.getItemStacks().init(2, true, 4, 50);
			recipeLayout.getItemStacks().set(2, ingredients.getInputs(VanillaTypes.ITEM).get(1));
			recipeLayout.getItemStacks().init(3, false, 114, 40);
			recipeLayout.getItemStacks().set(3, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
		}
	}

}
