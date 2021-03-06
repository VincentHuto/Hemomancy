package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.RenderTypeInit;
import com.huto.hemomancy.item.tool.living.ItemLivingBlade;
import com.huto.hemomancy.model.entity.armor.ModelLivingBladeHand;
import com.huto.hemomancy.model.entity.armor.ModelLivingBladeHandTame;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class RenderItemLivingBlade extends ItemStackTileEntityRenderer {
	public final ModelLivingBladeHand unleashed = new ModelLivingBladeHand();
	public final ModelLivingBladeHandTame tame = new ModelLivingBladeHandTame();

	public static ResourceLocation living_blade = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_living_blade_hand.png");

	public RenderItemLivingBlade() {
	}

	@Override
	public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack ms,
			IRenderTypeBuffer buffers, int light, int overlay) {

		if (stack.getItem() instanceof ItemLivingBlade) {
			Model model;
			ms.push();
			ms.rotate(new Quaternion(Vector3f.XP, 180, true));
			ms.rotate(new Quaternion(Vector3f.YP, 180, true));

			IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer
					.getImpl(Tessellator.getInstance().getBuffer());
			IVertexBuilder ivertexbuilder = irendertypebuffer$impl.getBuffer(unleashed.getRenderType(living_blade));
			ms.scale(0.5f, 0.5f, 0.5f);
			ms.translate(-1, -1.8, 1);
			model = stack.getTag().getBoolean("state") ? unleashed : tame;
			if (model == unleashed) {
				IVertexBuilder glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
				IVertexBuilder buffer = VertexBuilderUtils.newDelegate(glint, ivertexbuilder);
				model.render(ms, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			} else {
				model.render(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}

			irendertypebuffer$impl.finish();
			ms.pop();
		}
	}

	public ModelLivingBladeHand getModel() {
		return unleashed;
	}
}