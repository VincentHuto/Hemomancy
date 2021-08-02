package com.huto.hemomancy.render.layer;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.tool.living.ItemLivingBlade;
import com.huto.hemomancy.model.entity.armor.ModelLivingBladeHip;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RenderLivingBladeHipLayer<T extends Player, M extends PlayerModel<T>> extends RenderLayer<T, M> {
	ModelLivingBladeHip model = new ModelLivingBladeHip();

	public RenderLivingBladeHipLayer(RenderLayerParent<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int packedLight,
			Player player, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {

		if (Hemomancy.findItemInPlayerInv(player, ItemLivingBlade.class) != ItemStack.EMPTY) {
			if (player.getHeldItemMainhand().getItem() != ItemInit.living_blade.get()
					&& player.getHeldItemOffhand().getItem() != ItemInit.living_blade.get()) {
				matrixStack.push();
				matrixStack.push();
				IRenderTypeBuffer.Impl impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
				EntityRenderer<?> renderer = Minecraft.getInstance().getRenderManager().getRenderer(player);
				EntityModel<?> bimodel = ((IEntityRenderer<?, ?>) renderer).getEntityModel();
				if (bimodel instanceof BipedModel<?>) {
					BipedModel<?> biModel = (BipedModel<?>) bimodel;
					biModel.bipedBody.translateRotate(matrixStack);
				}
				IVertexBuilder ivertexbuilder = impl.getBuffer(model.getRenderType(
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
