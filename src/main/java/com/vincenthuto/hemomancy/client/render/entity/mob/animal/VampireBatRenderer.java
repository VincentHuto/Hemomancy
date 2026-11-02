package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.VampireBatModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VampireBatEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class VampireBatRenderer extends MobRenderer<VampireBatEntity, VampireBatModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/vampire_bat/model_vampire_bat.png");
	private static final RenderType EYES = RenderType.eyes(Hemomancy.rloc(
			"textures/entity/vampire_bat/model_vampire_bat_eyes.png"));

	public VampireBatRenderer(EntityRendererProvider.Context context) {
		super(context, new VampireBatModel(context.bakeLayer(VampireBatModel.LAYER_LOCATION)), 0.35F);
		addLayer(new EyesLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(VampireBatEntity entity) {
		return TEXTURE;
	}

	/** Red eyes that hold in a dark roost; dimmer until it has prey. */
	private static final class EyesLayer extends RenderLayer<VampireBatEntity, VampireBatModel> {
		EyesLayer(RenderLayerParent<VampireBatEntity, VampireBatModel> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack pose, MultiBufferSource buffers, int light, VampireBatEntity entity, float limbSwing,
				float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			getParentModel().renderToBuffer(pose, buffers.getBuffer(EYES), light, OverlayTexture.NO_OVERLAY,
					entity.isHunting() ? 0xFFFFFFFF : 0xFF909090);
		}
	}
}
