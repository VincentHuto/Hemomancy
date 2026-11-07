package com.vincenthuto.hemomancy.client.render.entity.boss.hemorath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.entity.boss.SaintRiseRender;
import com.vincenthuto.hemomancy.client.render.layer.mob.HemorathVesselLayer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Saint Hemorath: the hunched skeleton from {@code tools/model_export/hemorath.py} at one render scale (2.0, so its two
 * texels a unit land at 16 px a block), with his vessels lit by the meter ({@link HemorathVesselLayer}). Each frame it
 * records the core, claw and staff head in world space for the reliquary geometry.
 */
public class HemorathRenderer extends MobRenderer<HemorathEntity, HemorathModel<HemorathEntity>> {
	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/hemorath/hemorath.png");
	private static final float SHADOW = 1.5F;
	/**
	 * The block light his bone never falls below: his chamber's boss floor is lit only from the walls and the vault, and
	 * unlit he reads as a black outline there instead of the frail pale frame the fight teaches from.
	 */
	private static final int LIGHT_FLOOR = 8;

	public HemorathRenderer(Context context) {
		super(context, new HemorathModel<>(context.bakeLayer(HemorathModel.LAYER_LOCATION)), SHADOW);
		this.addLayer(new HemorathVesselLayer<>(this));
	}

	@Override
	public ResourceLocation getTextureLocation(HemorathEntity entity) {
		return TEXTURE;
	}

	@Override
	protected int getBlockLightLevel(HemorathEntity entity, BlockPos pos) {
		return Math.max(LIGHT_FLOOR, super.getBlockLightLevel(entity, pos));
	}

	@Override
	protected void scale(HemorathEntity entity, PoseStack poseStack, float partialTick) {
		SaintRiseRender.sink(poseStack, entity, partialTick);
		poseStack.scale(HemorathVisualRules.RENDER_SCALE, HemorathVisualRules.RENDER_SCALE, HemorathVisualRules.RENDER_SCALE);
	}

	@Override
	public void render(HemorathEntity entity, float yaw, float partial, PoseStack poses, MultiBufferSource buffers, int light) {
		super.render(entity, yaw, partial, poses, buffers, light);
		float bodyYaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);
		float scale = HemorathVisualRules.RENDER_SCALE;
		HemorathAnimState.of(entity).attachments(offset(model.core(), bodyYaw, scale),
				offset(model.claw(), bodyYaw, scale), offset(model.staffHead(), bodyYaw, scale), entity.getPosition(partial),
				AnimationTime.age(entity, partial));
	}

	private static Vec3 offset(Vector3f point, float yaw, float scale) {
		point.set(-point.x * scale, (1.501F - point.y) * scale, point.z * scale);
		return new Vec3(point.rotateY((180 - yaw) * Mth.DEG_TO_RAD));
	}
}
