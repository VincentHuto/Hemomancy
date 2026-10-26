package com.vincenthuto.hemomancy.client.render.entity.boss.seraphae;

import com.vincenthuto.hemomancy.client.render.entity.boss.SaintRiseRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeAnimState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeVisualRules;
import com.vincenthuto.hemomancy.client.render.layer.mob.SeraphaeWingGlowLayer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeState;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Renderer for Saint Seraphae — The Bound Radiance.
 * Applies visual scaling and translucency based on the boss's state.
 */
public class SeraphaeRenderer extends MobRenderer<SeraphaeEntity, SeraphaeModel<SeraphaeEntity>> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/seraphae/seraphae.png");

	public SeraphaeRenderer(Context context) {
		super(context, new SeraphaeModel<>(context.bakeLayer(SeraphaeModel.LAYER_LOCATION)), 0.9F);
		this.addLayer(new SeraphaeWingGlowLayer<>(this));
	}

	@Override
	public ResourceLocation getTextureLocation(SeraphaeEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(SeraphaeEntity entity, PoseStack poseStack, float partialTick) {
		SaintRiseRender.sink(poseStack, entity, partialTick);
		float base = SeraphaeVisualRules.MODEL_SCALE;
		poseStack.scale(base, base, base);
	}

	@Override
	public void render(SeraphaeEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		// Skip rendering when fully dispersed (invisible is set server-side,
		// but we reinforce it client-side for visual clarity)
		if (entity.getSeraphaeState() == SeraphaeState.DISPERSED) {
			return;
		}
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		var motion = SeraphaeAnimState.of(entity);
		float yaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
		motion.wrists(worldOffset(model.wrist(true), yaw), worldOffset(model.wrist(false), yaw));
		for (int i = 0; i < 6; i++)
			motion.feather(i, worldOffset(model.featherTip(i < 3, (i % 3) * 3), yaw));
	}

	private static Vec3 worldOffset(Vector3f point, float yaw) {
		float scale = SeraphaeVisualRules.MODEL_SCALE;
		point.set(-point.x * scale, (1.501F - point.y) * scale, point.z * scale);
		point.rotateY((180 - yaw) * Mth.DEG_TO_RAD);
		return new Vec3(point);
	}
}
