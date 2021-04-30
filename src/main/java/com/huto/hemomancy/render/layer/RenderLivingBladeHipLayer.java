package com.huto.hemomancy.render.layer;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.tool.living.ItemLivingBlade;
import com.huto.hemomancy.model.entity.armor.ModelLivingBladeHip;
import com.huto.hemomancy.util.RenderShapes;
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

public class RenderLivingBladeHipLayer<T extends PlayerEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M> {
	ModelLivingBladeHip model = new ModelLivingBladeHip();

	public RenderLivingBladeHipLayer(IEntityRenderer<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int packedLight,
			PlayerEntity player, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {

		if (Hemomancy.findItemInPlayerInv(player, ItemLivingBlade.class) != ItemStack.EMPTY) {
			if (player.getHeldItemMainhand().getItem() != ItemInit.living_blade.get()
					&& player.getHeldItemOffhand().getItem() != ItemInit.living_blade.get()) {
				matrixStack.push();
				matrixStack.push();
				IRenderTypeBuffer.Impl impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
				IVertexBuilder ivertexbuilder = impl.getBuffer(model.getRenderType(
						(new ResourceLocation(Hemomancy.MOD_ID, "textures/block/sanguine_glass.png"))));
				int color = 0xB6B900;
				int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
				int gridSize = 3;
				float gridSpacing = .25f;
				for (int x = 1; x < gridSize + 1; x++) {
					for (int z = 1; z <  2; z++) {
							RenderShapes.renderSizedPanel(matrixStack, iRenderTypeBuffer, packedLight, OverlayTexture.NO_OVERLAY,
									ivertexbuilder, gridSpacing * (x * 1.001f), 1.1f ,
									gridSpacing * (z * 1.001f), 0.25f, 0.25f, 0.25f);
						}
					}

				// Top

				matrixStack.pop();

				EntityRenderer<?> renderer = Minecraft.getInstance().getRenderManager().getRenderer(player);
				EntityModel<?> bimodel = ((IEntityRenderer<?, ?>) renderer).getEntityModel();
				if (bimodel instanceof BipedModel<?>) {
					BipedModel<?> biModel = (BipedModel<?>) bimodel;
					biModel.bipedBody.translateRotate(matrixStack);
				}
				ivertexbuilder = impl.getBuffer(model.getRenderType(
						(new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/model_living_blade_hip.png"))));
				matrixStack.translate(0.3, 0.25, 0);
				matrixStack.scale(0.5f, 0.5f, 0.5f);

				model.render(matrixStack, ivertexbuilder, packedLight, OverlayTexture.NO_OVERLAY, 100, 100, 100, 100);
				impl.finish();
				matrixStack.pop();
			}
		}
	}
}
