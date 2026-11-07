package com.vincenthuto.hemomancy.client.render.entity.boss.seraphae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.entity.boss.SaintRiseRender;
import com.vincenthuto.hemomancy.client.render.layer.mob.SeraphaeNerveLayer;
import com.vincenthuto.hemomancy.client.render.layer.mob.SeraphaeVesselLayer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeVisualRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Saint Seraphae at render scale 2.0 (16 texels per block), with her white vessel layer and her gold nerve layer kept
 * apart. After drawing it records the world offsets of her irons, hands, heart and nerve tips, which her chains and
 * move geometry read, and keeps her hands and tips in the world frame by frame ({@link SeraphaeAnimState#handAt},
 * {@link SeraphaeAnimState#tipAt}) so a reach or a lash drawn along them follows the animation. In her ending she
 * sinks into the tomb over ticks 90 to 100 and is gone after it.
 */
public class SeraphaeRenderer extends MobRenderer<SeraphaeEntity, SeraphaeModel<SeraphaeEntity>> {
	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/seraphae/seraphae.png");
	/** How far the sink takes her, in blocks: her whole height. */
	private static final float SINK_DEPTH = SeraphaeEntity.HEIGHT + 0.4F;
	/**
	 * She is the chamber's white light: her ivory body never falls under this block light, so on the dim floor of the
	 * Reaching she reads pale instead of as a dark outline (the radiant stone of the later phases lights her anyway).
	 */
	static final int LIGHT_FLOOR = 12;

	public SeraphaeRenderer(Context context) {
		super(context, new SeraphaeModel<>(context.bakeLayer(SeraphaeModel.LAYER_LOCATION)), 1.0F);
		this.addLayer(new SeraphaeVesselLayer<>(this));
		this.addLayer(new SeraphaeNerveLayer<>(this));
	}

	@Override
	public ResourceLocation getTextureLocation(SeraphaeEntity entity) {
		return TEXTURE;
	}

	@Override
	protected int getBlockLightLevel(SeraphaeEntity entity, BlockPos pos) {
		return Math.max(LIGHT_FLOOR, super.getBlockLightLevel(entity, pos));
	}

	@Override
	protected void scale(SeraphaeEntity entity, PoseStack poseStack, float partialTick) {
		SaintRiseRender.sink(poseStack, entity, partialTick);
		float ending = entity.view().endingTime(partialTick);
		if (ending >= 0.0F) poseStack.translate(0.0F, -SeraphaeEndingRules.sink(ending) * SINK_DEPTH, 0.0F);
		float base = SeraphaeVisualRules.MODEL_SCALE;
		poseStack.scale(base, base, base);
	}

	@Override
	public void render(SeraphaeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		float ending = entity.view().endingTime(partialTicks);
		if (ending >= SeraphaeEndingRules.END_TRIAL) return;
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		SeraphaeAnimState motion = SeraphaeAnimState.of(entity);
		float yaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
		motion.shackle(SeraphaeRestraint.LEFT_WRIST, worldOffset(model.wristShackle(true), yaw));
		motion.shackle(SeraphaeRestraint.RIGHT_WRIST, worldOffset(model.wristShackle(false), yaw));
		motion.shackle(SeraphaeRestraint.LEFT_WING, worldOffset(model.wingShackle(true), yaw));
		motion.shackle(SeraphaeRestraint.RIGHT_WING, worldOffset(model.wingShackle(false), yaw));
		motion.shackle(SeraphaeRestraint.WAIST, worldOffset(model.waistPoint(), yaw));
		motion.heart(worldOffset(model.heartPoint(), yaw));
		motion.hands(worldOffset(model.handPoint(true), yaw), worldOffset(model.handPoint(false), yaw));
		for (int i = 0; i < 6; i++) motion.tip(i, worldOffset(model.tipPoint(i), yaw));
		motion.trail(entity.getPosition(partialTicks), AnimationTime.age(entity, partialTicks));
	}

	/** A posed model-space point as an offset from her feet in world axes. */
	private static Vec3 worldOffset(Vector3f point, float yaw) {
		float scale = SeraphaeVisualRules.MODEL_SCALE;
		point.set(-point.x * scale, (1.501F - point.y) * scale, point.z * scale);
		point.rotateY((180 - yaw) * Mth.DEG_TO_RAD);
		return new Vec3(point);
	}
}
