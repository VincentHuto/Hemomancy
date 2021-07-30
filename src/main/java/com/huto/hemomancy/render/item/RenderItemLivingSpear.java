package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.RenderTypeInit;
import com.huto.hemomancy.item.tool.living.ItemLivingSpear;
import com.huto.hemomancy.model.entity.armor.ModelLivingSpear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class RenderItemLivingSpear extends BlockEntityWithoutLevelRenderer {
	public final ModelLivingSpear spearModel = new ModelLivingSpear();

	public static ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_living_spear_hand.png");

	public RenderItemLivingSpear() {
	}

	@Override
	public void func_239207_a_(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {

		if (stack.getItem() instanceof ItemLivingSpear) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			ms.push();
			ms.rotate(new Quaternion(Vector3f.XP, 180, true));
			ms.rotate(new Quaternion(Vector3f.YP, 180, true));

			IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer
					.getImpl(Tessellator.getInstance().getBuffer());
			IVertexBuilder ivertexbuilder = irendertypebuffer$impl.getBuffer(spearModel.getRenderType(TEXTURE));

			boolean itemIsInUse = player.getItemInUseCount() > 0;
			Hand activeHand = player.getActiveHand();
			ms.scale(0.65f, 0.65f, 0.65f);
			ms.translate(-0.75, -0.95, 0.75);
			if (p_239207_2_ == ItemCameraTransforms.TransformType.GUI) {
				ms.translate(-0, -0.25, 0);
				ms.scale(0.75f, 0.75f, 0.75f);
			}
			if (p_239207_2_ == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
					|| p_239207_2_ == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
				ms.rotate(new Quaternion(Vector3f.XP, 65, true));
				ms.translate(0, 0, -0.25);

			}

			if (itemIsInUse) {
				if (activeHand == Hand.MAIN_HAND) {
					if (p_239207_2_ == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
						ms.rotate(new Quaternion(Vector3f.XP, 180, true));
						ms.translate(0, 0 - .55, 0.);

					}
					if (p_239207_2_ == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
						ms.rotate(new Quaternion(Vector3f.XP, -20, true));
						ms.translate(-.55, 0, 0.);

					}
				} else {
					if (p_239207_2_ == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND) {
						ms.rotate(new Quaternion(Vector3f.XP, 180, true));
						ms.translate(0, 0 - .55, 0.);

					}
					if (p_239207_2_ == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
						ms.rotate(new Quaternion(Vector3f.XP, -20, true));
						ms.translate(.55, 0, 0.);

					}
				}
				if (player.getActiveItemStack() == stack) {

					IVertexBuilder glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
					IVertexBuilder buffer = VertexBuilderUtils.newDelegate(glint, ivertexbuilder);
					spearModel.render(ms, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
				} else {
					spearModel.render(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
				}
			} else {
				spearModel.render(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}

			irendertypebuffer$impl.finish();
			ms.pop();
		}
	}

	public ModelLivingSpear getModel() {
		return spearModel;
	}
}