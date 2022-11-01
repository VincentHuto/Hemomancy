//
//package com.vincenthuto.hemomancy.integration;
//
//import java.awt.TextComponent;
//import java.nio.FloatBuffer;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import javax.annotation.Nonnull;
//
//import org.lwjgl.BufferUtils;
//
//import com.mojang.blaze3d.platform.Lighting;
//import com.mojang.blaze3d.platform.Window;
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.math.Matrix4f;
//import com.mojang.math.Quaternion;
//import com.vincenthuto.hemomancy.Hemomancy;
//import com.vincenthuto.hemomancy.init.ItemInit;
//import com.vincenthuto.hemomancy.recipe.BloodStructureRecipe;
//import com.vincenthuto.hutoslib.client.HLClientUtils;
//
//import mezz.jei.api.constants.VanillaTypes;
//import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
//import mezz.jei.api.gui.drawable.IDrawable;
//import mezz.jei.api.helpers.IGuiHelper;
//import mezz.jei.api.recipe.IFocusGroup;
//import mezz.jei.api.recipe.RecipeIngredientRole;
//import mezz.jei.api.recipe.category.IRecipeCategory;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.client.gui.GuiComponent;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.texture.OverlayTexture;
//import net.minecraft.client.resources.language.I18n;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.phys.AABB;
//import net.minecraft.world.phys.Vec3;
//
//public class BloodStructureRecipeCategory implements IRecipeCategory<BloodStructureRecipe> {
//	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "blood_structure");
//	private final IDrawable background;
//	private final String localizedName;
//	private final IDrawable overlay;
//	private final IDrawable icon;
//	IGuiHelper guiHelper;
//
//	public BloodStructureRecipeCategory(IGuiHelper guiHelper) {
//		background = guiHelper.createBlankDrawable(150, 110);
//		localizedName = I18n.get("hemomancy.jei.blood_crafting");
//		overlay = guiHelper.createDrawable(new ResourceLocation("hemomancy", "textures/gui/bloodcraftingoverlay.png"),
//				0, 0, 150, 110);
//		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemInit.sanguine_formation.get()));
//		this.guiHelper = guiHelper;
//	}
//
//	@Nonnull
//	@Override
//	public ResourceLocation getUid() {
//		return UID;
//	}
//
//	@Override
//	public Class<? extends BloodStructureRecipe> getRecipeClass() {
//		return BloodStructureRecipe.class;
//	}
//
//	@Override
//	public IDrawable getIcon() {
//		return icon;
//	}
//
//	@Nonnull
//	@Override
//	public TextComponent getTitle() {
//		return Component.literal(localizedName);
//	}
//
//	@Nonnull
//	@Override
//	public IDrawable getBackground() {
//		return background;
//	}
//
//	@Override
//	public void draw(BloodStructureRecipe recipe, PoseStack ms, double mouseX, double mouseY) {
//		overlay.draw(ms);
//		TranslatableComponent experienceString = Component.literal(String.valueOf(recipe.getResult()));
//		Minecraft minecraft = Minecraft.getInstance();
//		Font fontRenderer = minecraft.font;
//		int stringWidth = fontRenderer.width(experienceString);
//		fontRenderer.draw(ms, experienceString, background.getWidth() - stringWidth, 0, 0xFF808080);
//		Window mainWindow = Minecraft.getInstance().getWindow();
//		int scissorX = 27;
//		int scissorY = 0;
//		double guiScaleFactor = mainWindow.getGuiScale();
//		ms.translate(8, 16, 0);
//		ScreenArea scissorBounds = new ScreenArea(scissorX, scissorY, 70, 70);
//		AABB dims = new AABB(0, 0, 0, 0, 0, 0);
//		Lighting.setupForFlatItems();
//		renderRecipe(recipe, ms, dims, guiScaleFactor, scissorBounds);
//	}
//
//	private void renderRecipe(BloodStructureRecipe recipe, PoseStack mx, AABB dims, double guiScaleFactor,
//			ScreenArea scissorBounds) {
//		try {
//			GuiComponent.fill(mx, scissorBounds.x, scissorBounds.y, scissorBounds.x + scissorBounds.width,
//					scissorBounds.height, 0xFF404040);
//			MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
//			final double scale = Minecraft.getInstance().getWindow().getGuiScale();
//			final Matrix4f matrix = mx.last().pose();
//			final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
//			matrix.store(buf);
//			Lighting.setupLevel(matrix);
//			
//			Vec3 translation = new Vec3(buf.get(12) * scale, buf.get(13) * scale, buf.get(14) * scale);
//			scissorBounds.x *= scale;
//			scissorBounds.y *= scale;
//			scissorBounds.width *= scale;
//			scissorBounds.height *= scale;
//			final int scissorX = Math.round(Math.round(translation.x + scissorBounds.x));
//			final int scissorY = Math.round(Math.round(Minecraft.getInstance().getWindow().getHeight() - scissorBounds.y
//					- scissorBounds.height - translation.y));
//			final int scissorW = Math.round(scissorBounds.width);
//			final int scissorH = Math.round(scissorBounds.height);
//			RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
//			mx.pushPose();
//			mx.translate(27 + (35), scissorBounds.y + (35), 100);
//			Vec3 dimsVec = new Vec3(dims.getXsize(), dims.getYsize(), dims.getZsize());
//			float recipeAvgDim = (float) dimsVec.length();
//
//			int layerCount = recipe.getPattern().getBlockPattern().getHeight() ;
//
//			double explodeMulti = 1.5;
//
//			float previewScale = (float) ((2 + Math.exp(2 - (recipeAvgDim / 5))) / explodeMulti);
//			mx.scale(previewScale, -previewScale, previewScale);
//			double test = Math.toDegrees(HLClientUtils.getWorld().getGameTime()) / 15;
//			mx.mulPose(new Quaternion(35f, (float) test, 0, true));
//			mx.mulPose(new Quaternion(0, 35f, 0, true));
//
//			recipe.getPattern().getBlockPosBlockList().forEach((box) -> {
//				if (box.getPos().getY() < layerCount) {
//					mx.pushPose();
//
//					mx.translate(
//							explodeMulti
//									* (box.getPos().getX() - (recipe.getPattern().getBlockPattern().getWidth() / 2))
//									- 0.5,
//							explodeMulti
//									* (box.getPos().getY() - (recipe.getPattern().getBlockPattern().getHeight() / 2))
//									- 0.5,
//							explodeMulti
//									* (box.getPos().getZ() - (recipe.getPattern().getBlockPattern().getDepth() / 2))
//									- 0.5);
//
//
//					Minecraft.getInstance().getBlockRenderer().renderSingleBlock(box.getBlock().defaultBlockState(), mx,
//							buffers, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
//					mx.popPose();
//				}
//			});
//			mx.popPose();
//			buffers.endBatch();
//			RenderSystem.disableScissor();
//		} catch (Exception ex) {
//			Hemomancy.LOGGER.warn(ex);
//		}
//	}
//
//	@Override
//	public void setRecipe(IRecipeLayoutBuilder builder, BloodStructureRecipe recipe, IFocusGroup focuses) {
//
//		List<List<ItemStack>> list = new ArrayList<>();
//		List<ItemStack> heldStack = new ArrayList<ItemStack>() {
//			{
//				add(recipe.getHeldItem());
//			}
//		};
//		List<ItemStack> hitStack = new ArrayList<ItemStack>() {
//			{
//				add(new ItemStack(recipe.getHitBlock().asItem()));
//			}
//		};
//		Collections.addAll(list, heldStack, hitStack);
//
//		if (list.size() > 1) {
//			builder.addSlot(RecipeIngredientRole.INPUT, 5, 31).addIngredients(VanillaTypes.ITEM_STACK, list.get(0));
//			builder.addSlot(RecipeIngredientRole.INPUT, 5, 51).addIngredients(VanillaTypes.ITEM_STACK, list.get(1));
//			builder.addSlot(RecipeIngredientRole.OUTPUT, 115, 41).addIngredient(VanillaTypes.ITEM_STACK, recipe.getResult());
//		}
//	}
//
//}
