package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.RenderTypeInit;
import com.huto.hemomancy.item.tool.living.ItemLivingAxe;
import com.huto.hemomancy.model.entity.armor.ModelLivingAxe;
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

public class RenderItemLivingAxe extends ItemStackTileEntityRenderer {
	public final ModelLivingAxe axeModel = new ModelLivingAxe();

	public static ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_living_axe_hand.png");

	public RenderItemLivingAxe() {
	}

	@Override
	public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack ms,
			IRenderTypeBuffer buffers, int light, int overlay) {

		if (stack.getItem() instanceof ItemLivingAxe) {
			ms.push();
			ms.rotate(new Quaternion(Vector3f.XP, 180, true));
			ms.rotate(new Quaternion(Vector3f.YP, 180, true));

			IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer
					.getImpl(Tessellator.getInstance().getBuffer());
			IVertexBuilder ivertexbuilder = irendertypebuffer$impl.getBuffer(axeModel.getRenderType(TEXTURE));
			ms.scale(0.65f, 0.65f, 0.65f);
			ms.translate(-0.75, -1.25, 0.75);
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