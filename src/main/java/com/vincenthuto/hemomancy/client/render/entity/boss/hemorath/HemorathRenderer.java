package com.vincenthuto.hemomancy.client.render.entity.boss.hemorath;

import com.vincenthuto.hemomancy.client.render.entity.boss.SaintRiseRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathAnimState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;
import com.vincenthuto.hemomancy.client.render.layer.mob.HemorathOverloadLayer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Renderer for the Hollow Vessel (Saint Hemorath).
 * Uses the dedicated {@link HemorathModel} — an emaciated, elongated
 * humanoid distinct from the standard Harbinger Vicar silhouette.
 */
public class HemorathRenderer extends MobRenderer<HemorathEntity, HemorathModel<HemorathEntity>> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/hemorath/hemorath.png");

	public HemorathRenderer(Context context) {
		super(context, new HemorathModel<>(context.bakeLayer(HemorathModel.LAYER_LOCATION)), 1.5F);
		this.addLayer(new HemorathOverloadLayer<>(this));
	}

	@Override
	public ResourceLocation getTextureLocation(HemorathEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(HemorathEntity entity, PoseStack poseStack, float partialTick) {
		SaintRiseRender.sink(poseStack, entity, partialTick);
		float base = HemorathVisualRules.scale(entity.isInPhase2());
		poseStack.scale(base, base, base);
	}

	@Override
	public void render(HemorathEntity entity, float yaw, float partial, PoseStack poses, MultiBufferSource buffers, int light) {
		super.render(entity, yaw, partial, poses, buffers, light);
		float bodyYaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);
		float scale = HemorathVisualRules.scale(entity.isInPhase2());
		HemorathAnimState.of(entity).attachments(offset(model.core(), bodyYaw, scale),
				offset(model.claw(), bodyYaw, scale), offset(model.staffHead(), bodyYaw, scale));
	}

	private static Vec3 offset(Vector3f point, float yaw, float scale) {
		point.set(-point.x * scale, (1.501F - point.y) * scale, point.z * scale);
		return new Vec3(point.rotateY((180 - yaw) * Mth.DEG_TO_RAD));
	}
}
