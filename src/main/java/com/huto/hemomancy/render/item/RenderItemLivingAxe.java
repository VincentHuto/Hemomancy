package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.RenderTypeInit;
import com.huto.hemomancy.item.tool.living.ItemLivingAxe;
import com.huto.hemomancy.model.entity.armor.ModelLivingAxe;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RenderItemLivingAxe extends BlockEntityWithoutLevelRenderer {
	public final ModelLivingAxe axeModel = new ModelLivingAxe();

	public static ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_living_axe_hand.png");

	public RenderItemLivingAxe() {
	}

	@Override
	public void func_239207_a_(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {

		if (stack.getItem() instanceof ItemLivingAxe) {
			ms.push();
			ms.rotate(new Quaternion(Vector3f.XP, 180, true));
			ms.rotate(new Quaternion(Vector3f.YP, 180, true));

			IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer
					.getImpl(Tessellator.getInstance().getBuffer());
			IVertexBuilder ivertexbuilder = irendertypebuffer$impl.getBuffer(axeModel.getRenderType(TEXTURE));
			ms.scale(0.65f, 0.65f, 0.65f);
			ms.translate(-0.75, -1.25, 0.75);
			if (p_239207_2_ == ItemCameraTransforms.TransformType.GUI) {
				ms.translate(-0, 0.15, 0);
			}
			
			boolean state = stack.getTag().getBoolean("state");
			if (state) {
				IVertexBuilder glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
				IVertexBuilder buffer = VertexBuilderUtils.newDelegate(glint, ivertexbuilder);
				axeModel.render(ms, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			} else {
				axeModel.render(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}

			irendertypebuffer$impl.finish();
			ms.pop();
		}
	}

	public ModelLivingAxe getModel() {
		return axeModel;
	}
}