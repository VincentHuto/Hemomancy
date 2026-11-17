package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusRingmasterModel;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CircusRingmasterRenderer extends MobRenderer<CircusRingmasterEntity, CircusRingmasterModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/npc/harbinger/circus/ringmaster.png");
	private static final RenderType GLOW = RenderType.eyes(
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/ringmaster_glow.png"));

	public CircusRingmasterRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusRingmasterModel(context.bakeLayer(CircusRingmasterModel.LAYER_LOCATION)), 0.45F);
		addLayer(new EyesLayer<>(this) {
			@Override public RenderType renderType() { return GLOW; }
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusRingmasterEntity entity) {
		return TEXTURE;
	}

	/**
	 * Sleight's doubles (chunk 15 T15) are not entities: they are drawn here, in his own pose, where the server says
	 * they stand. Nothing can strike them; only he bleeds.
	 */
	@Override
	public void render(CircusRingmasterEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
		double x = Mth.lerp(partialTick, entity.xo, entity.getX());
		double y = Mth.lerp(partialTick, entity.yo, entity.getY());
		double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
		for (Vec3 decoy : entity.doubles()) {
			poseStack.pushPose();
			poseStack.translate(decoy.x - x, decoy.y - y, decoy.z - z);
			super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
			poseStack.popPose();
		}
	}

	@Override
	public boolean shouldRender(CircusRingmasterEntity entity, Frustum frustum,
			double cameraX, double cameraY, double cameraZ) {
		return frustum.isVisible(entity.getBoundingBoxForCulling().inflate(1.0D));
	}
}
