package com.huto.hemomancy.render.layer;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.model.entity.armor.ModelLivingBladeHip;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class LivingBladeHipRenderLayer<T extends PlayerEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M> {
	ModelLivingBladeHip model = new ModelLivingBladeHip();

	public LivingBladeHipRenderLayer(IEntityRenderer<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int packedLight,
			PlayerEntity player, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {

		if (Hemomancy.findLivingBlade(player) != ItemStack.EMPTY) {
			if (player.getHeldItemMainhand().getItem() != ItemInit.living_blade.get()
					&& player.getHeldItemOffhand().getItem() != ItemInit.living_blade.get()) {
				matrixStack.push();
				EntityRenderer<?> renderer = Minecraft.getInstance().getRenderManager().getRenderer(player);
				EntityModel<?> bimodel = ((IEntityRenderer<?, ?>) renderer).getEntityModel();
				if (bimodel instanceof BipedModel<?>) {
					BipedModel<?> biModel = (BipedModel<?>) bimodel;
					biModel.bipedBody.translateRotate(matrixStack);
				}
				IRenderTypeBuffer.Impl impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
				matrixStack.translate(0.3, 0.25, 0);
				matrixStack.scale(0.5f, 0.5f, 0.5f);
				IVertexBuilder ivertexbuilder = impl.getBuffer(model.getRenderType(
						(new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/model_living_blade_hip.png"))));
				model.render(matrixStack, ivertexbuilder, packedLight, OverlayTexture.NO_OVERLAY, 100, 100, 100, 100);
				impl.finish();
				matrixStack.pop();
			}
		}
	}
}