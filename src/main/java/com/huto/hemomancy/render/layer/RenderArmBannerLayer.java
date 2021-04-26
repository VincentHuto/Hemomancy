package com.huto.hemomancy.render.layer;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.rune.ItemArmBanner;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class RenderArmBannerLayer<T extends PlayerEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M> {

	public RenderArmBannerLayer(IEntityRenderer<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int packedLight,
			PlayerEntity player, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {

		matrixStack.push();
		ItemStack slotItemStack = Hemomancy.findItemInPlayerInv(player, ItemArmBanner.class);
		if (slotItemStack.getItem() == ItemInit.arm_banner.get()) {
			EntityRenderer<?> renderer = Minecraft.getInstance().getRenderManager().getRenderer(player);
			EntityModel<?> model = ((IEntityRenderer<?, ?>) renderer).getEntityModel();
			if (model instanceof BipedModel<?>) {
				BipedModel<?> biModel = (BipedModel<?>) model;
				biModel.bipedLeftArm.translateRotate(matrixStack);
			}
			dispatchRenders(matrixStack, packedLight, iRenderTypeBuffer, player, slotItemStack, partialTicks);
		}

		matrixStack.pop();
	}

	private void dispatchRenders(MatrixStack matrix, int packedLightIn, IRenderTypeBuffer iRenderTypeBuffer,
			PlayerEntity player, ItemStack stack, float partialTicks) {

		matrix.push();
		if (stack.getItem() instanceof ItemArmBanner) {
			ItemArmBanner armBanner = (ItemArmBanner) stack.getItem();
			RenderHelper.enableStandardItemLighting();
			matrix.rotate(Vector3f.XN.rotationDegrees(180f));
			matrix.rotate(Vector3f.YN.rotationDegrees(90f));
			matrix.rotate(Vector3f.ZN.rotationDegrees(-72.5f));
			matrix.scale(0.5f, 0.5f, 0.5f);
			matrix.translate(-0.35, 0, -0.35);
			Minecraft.getInstance().getItemRenderer().renderItem(stack, TransformType.FIXED, packedLightIn,
					OverlayTexture.NO_OVERLAY, matrix, iRenderTypeBuffer);

		}
		matrix.pop();

	}

}