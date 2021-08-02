package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.RenderTypeInit;
import com.huto.hemomancy.item.tool.living.ItemLivingAxe;
import com.huto.hemomancy.model.entity.armor.ModelLivingAxe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RenderItemLivingAxe extends BlockEntityWithoutLevelRenderer {
	public RenderItemLivingAxe(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
	}

	public final ModelLivingAxe axeModel = new ModelLivingAxe();

	public static ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_living_axe_hand.png");

	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {

		if (stack.getItem() instanceof ItemLivingAxe) {
			ms.pushPose();
			ms.mulPose(new Quaternion(Vector3f.XP, 180, true));
			ms.mulPose(new Quaternion(Vector3f.YP, 180, true));

			MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
					.immediate(Tesselator.getInstance().getBuilder());
			VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(axeModel.renderType(TEXTURE));
			ms.scale(0.65f, 0.65f, 0.65f);
			ms.translate(-0.75, -1.25, 0.75);
			if (p_239207_2_ == ItemTransforms.TransformType.GUI) {
				ms.translate(-0, 0.15, 0);
			}

			boolean state = stack.getTag().getBoolean("state");
			if (state) {
				VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
				VertexConsumer buffer = VertexMultiConsumer.create(glint, ivertexbuilder);
				axeModel.renderToBuffer(ms, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			} else {
				axeModel.renderToBuffer(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}

			irendertypebuffer$impl.endBatch();
			ms.popPose();
		}
	}

	public ModelLivingAxe getModel() {
		return axeModel;
	}
}