
package com.vincenthuto.hemomancy.compat.jei;

import java.nio.FloatBuffer;

import javax.annotation.Nonnull;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.Quaternion;

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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

public class BloodStructureRecipeCategory implements IRecipeCategory<BloodStructureRecipe> {
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;
	IGuiHelper guiHelper;

	public BloodStructureRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(150, 110);
		localizedName = I18n.get("hemomancy.jei.blood_crafting");
		overlay = guiHelper.createDrawable(new ResourceLocation("hemomancy", "textures/gui/bloodcraftingoverlay.png"),
				0, 0, 150, 110);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(ItemInit.sanguine_formation.get()));
		this.guiHelper = guiHelper;
	}

	@Override
	public void draw(BloodStructureRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
			double mouseY) {
		overlay.draw(graphics);
		// Show blood cost, not result.toString()
		MutableComponent costString = Component.literal("Blood Cost: " + (int) recipe.getBloodCost());
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		int stringWidth = fontRenderer.width(costString);
		graphics.drawString(fontRenderer, costString, background.getWidth() - stringWidth, 0, 0xFFAA0000);
		Window mainWindow = Minecraft.getInstance().getWindow();
		double guiScaleFactor = mainWindow.getGuiScale();
		// Offset the 3D render into the right half of the background
		graphics.pose().translate(8, 16, 0);
		int scissorX = 27;
		int scissorY = 0;
		int scissorW = 70;
		int scissorH = 70;
		ScreenArea scissorBounds = new ScreenArea(scissorX, scissorY, scissorW, scissorH);
		AABB dims = new AABB(0, 0, 0, 0, 0, 0);
		Lighting.setupForFlatItems();
		renderRecipe(recipe, graphics, dims, guiScaleFactor, scissorBounds);
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
	public RecipeType<BloodStructureRecipe> getRecipeType() {
		return JEIPlugin.blood_structure_recipe_type;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.literal(localizedName);
	}

	private void renderRecipe(BloodStructureRecipe recipe, GuiGraphics graphics, AABB dims, double guiScaleFactor,
			ScreenArea scissorBounds) {
		try {
			graphics.fill(scissorBounds.x, scissorBounds.y, scissorBounds.x + scissorBounds.width, scissorBounds.y + scissorBounds.height,
					0xFF404040);
			MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
			final double scale = Minecraft.getInstance().getWindow().getGuiScale();
			final Matrix4f matrix = graphics.pose().last().pose();
			final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
			matrix.get(buf);
			Lighting.setupLevel(matrix);

			Vec3 translation = new Vec3(buf.get(12) * scale, buf.get(13) * scale, buf.get(14) * scale);
			// Use separate scaled values instead of mutating scissorBounds
			final int scaledX = (int) (scissorBounds.x * scale);
			final int scaledY = (int) (scissorBounds.y * scale);
			final int scaledW = (int) (scissorBounds.width * scale);
			final int scaledH = (int) (scissorBounds.height * scale);
			final int scissorXFinal = Math.round(Math.round(translation.x + scaledX));
			final int scissorYFinal = Math.round(Math.round(Minecraft.getInstance().getWindow().getHeight() - scaledY
					- scaledH - translation.y));
			final int scissorWFinal = Math.round(scaledW);
			final int scissorHFinal = Math.round(scaledH);
			RenderSystem.enableScissor(scissorXFinal, scissorYFinal, scissorWFinal, scissorHFinal);
			graphics.pose().pushPose();
			graphics.pose().translate(scissorBounds.x + (scissorBounds.width / 2), scissorBounds.y + (scissorBounds.height / 2), 100);
			Vec3 dimsVec = new Vec3(dims.getXsize(), dims.getYsize(), dims.getZsize());
			float recipeAvgDim = (float) dimsVec.length();
			double explodeMulti = 1.5;

			float previewScale = (float) ((2 + Math.exp(2 - (recipeAvgDim / 5))) / explodeMulti);
			graphics.pose().scale(previewScale, -previewScale, previewScale);
			drawActualRecipe(recipe, graphics, dims, buffers);
			graphics.pose().popPose();
			buffers.endBatch();
			RenderSystem.disableScissor();
		} catch (Exception ex) {
			Hemomancy.LOGGER.warn(ex);
		}
	}

	private void drawActualRecipe(BloodStructureRecipe recipe, GuiGraphics graphics, AABB dims,
			MultiBufferSource.BufferSource buffers) {
		double test = Math.toDegrees(HLClientUtils.getWorld().getGameTime()) / 15;
		graphics.pose().mulPose(new Quaternion(35f, (float) test, 0, true).toMoj());
		graphics.pose().mulPose(new Quaternion(0, 35f, 0, true).toMoj());
		int layerCount = recipe.getPattern().getBlockPattern().getHeight();

		double explodeMulti = 1.5;
		recipe.getPattern().getBlockPosBlockList().forEach((box) -> {
			if (box.getPos().getY() < layerCount) {
				graphics.pose().pushPose();

				graphics.pose().translate(
						explodeMulti * (box.getPos().getX() - (recipe.getPattern().getBlockPattern().getWidth() / 2))
								- 0.5,
						explodeMulti * (box.getPos().getY() - (recipe.getPattern().getBlockPattern().getHeight() / 2))
								- 0.5,
						explodeMulti * (box.getPos().getZ() - (recipe.getPattern().getBlockPattern().getDepth() / 2))
								- 0.5);

				Minecraft.getInstance().getBlockRenderer().renderSingleBlock(box.getBlock().defaultBlockState(),
						graphics.pose(), buffers, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
						RenderType.cutoutMipped());
				graphics.pose().popPose();
			}
		});
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BloodStructureRecipe recipe, IFocusGroup focuses) {

		// Input slots: held item and hit block
		builder.addSlot(RecipeIngredientRole.INPUT, 5, 21)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getHeldItem());

		builder.addSlot(RecipeIngredientRole.INPUT, 5, 41)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(recipe.getHitBlock().asItem()));

		// Show the pattern blocks as additional required ingredients
		if (recipe.getPattern() != null && recipe.getPattern().getSymbolList() != null) {
			int slotIndex = 0;
			for (java.util.Map.Entry<String, Block> entry : recipe.getPattern().getSymbolList().entrySet()) {
				Block block = entry.getValue();
				if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) {
					ItemStack blockStack = new ItemStack(block.asItem());
					if (!blockStack.isEmpty()) {
						builder.addSlot(RecipeIngredientRole.INPUT, 5, 61 + (slotIndex * 20))
								.addIngredient(VanillaTypes.ITEM_STACK, blockStack);
						slotIndex++;
					}
				}
			}
		}

		// Output slot
		builder.addSlot(RecipeIngredientRole.OUTPUT, 115, 41)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResult());
	}

}
