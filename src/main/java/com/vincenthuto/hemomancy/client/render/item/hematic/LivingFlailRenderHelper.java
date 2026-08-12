package com.vincenthuto.hemomancy.client.render.item.hematic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.LivingFlailModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailDeployment;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LivingFlailRenderHelper {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_living_flail.png");
	private static final Vec3 CHAIN_ANCHOR = new Vec3(0.0, 0.58, 0.0);
	private static final Vec3 CHAIN_DROOP_OFFSET = new Vec3(-0.2, -0.1, 0.1);
	private static final float CHAIN_DROOP_ROLL_DEGREES = 180.0F;
	private static final float CHAIN_DROOP_PITCH_DEGREES = 80.0F;
	private static final float CHAIN_FACE_AWAY_YAW_DEGREES = 180.0F;
	private static final Map<Key, PhysicsState> STATES = new ConcurrentHashMap<>();

	private LivingFlailRenderHelper() {
	}

	public static void renderStatic(LivingFlailModel<?> model, ItemStack stack, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		if (LivingFlailDeployment.isDeployed(stack)) {
			VertexConsumer base = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
			model.renderHandle(poseStack, base, packedLight, packedOverlay, -1);
			return;
		}
		renderWithBob(model, poseStack, buffer, packedLight, packedOverlay, new Vec3(0.0, 0.62, 0.0), 0.0f);
	}

	public static void renderHeld(LivingFlailModel<?> model, LivingEntity holder, HumanoidArm arm, ItemStack stack,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, boolean firstPerson) {
		Key key = new Key(holder.getUUID(), firstPerson ? 1 : 0, arm == HumanoidArm.LEFT ? 0 : 1);
		poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
		poseStack.mulPose(Axis.XN.rotationDegrees(80f));
		poseStack.translate( .15D, -0.1D, -.2);
		if (LivingFlailDeployment.isDeployed(stack)) {
			VertexConsumer base = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
			model.renderHandle(poseStack, base, packedLight, packedOverlay, -1);
			return;
		}
		if (holder.isUsingItem() && holder.getUseItem() == stack) {
			renderChargingOrbit(model, holder, stack, poseStack, buffer, packedLight, packedOverlay, firstPerson);
			return;
		}
		PhysicsState state = STATES.computeIfAbsent(key, ignored -> new PhysicsState(holder));
		double chainLength = firstPerson ? 0.72 : 0.64;
		Vec3 bob = state.update(holder, firstPerson, chainLength);
		float tilt = (float) Mth.clamp(bob.x * 48.0 + bob.z * 28.0, -30.0, 30.0);
		renderWithBob(model, poseStack, buffer, packedLight, packedOverlay, bob, tilt);
	}

	private static void renderChargingOrbit(LivingFlailModel<?> model, LivingEntity holder, ItemStack stack,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, boolean firstPerson) {
		VertexConsumer base = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		model.renderHandle(poseStack, base, packedLight, packedOverlay, -1);
		int usedTicks = holder.getTicksUsingItem();
		float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
		double phase = LivingFlailRules.orbitAngle(usedTicks, partialTick);
		double radius = firstPerson ? 0.72D : 0.66D;
		Vec3 orbit = new Vec3(Math.cos(phase) * radius,
				0.18D + Math.sin(phase * 2.0D) * 0.08D, Math.sin(phase) * radius);
		poseStack.pushPose();
		poseStack.translate(CHAIN_ANCHOR.x, CHAIN_ANCHOR.y, CHAIN_ANCHOR.z);
		renderChainFromAnchor(model, poseStack, base, packedLight, packedOverlay, orbit);
		poseStack.pushPose();
		poseStack.translate(orbit.x, orbit.y, orbit.z);
		float headScale = LivingFlailRules.visualScale(LivingFlailRules.charge(usedTicks));
		poseStack.scale(headScale, headScale, headScale);
		poseStack.mulPose(Axis.XP.rotationDegrees((holder.tickCount + partialTick) * 34.0F));
		model.renderHead(poseStack, base, packedLight, packedOverlay, -1);
		VertexConsumer glint = buffer.getBuffer(RenderTypeInit.getCrimsonGlint());
		model.renderHead(poseStack, glint, packedLight, OverlayTexture.NO_OVERLAY, -1);
		float maximumChargeFlash = LivingFlailRules.maximumChargeFlash(usedTicks, partialTick);
		if (maximumChargeFlash > 0.0F) {
			VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
			int alpha = Mth.clamp(Math.round(maximumChargeFlash * 180.0F), 0, 255);
			model.renderHead(poseStack, glow, 0xF000F0, OverlayTexture.NO_OVERLAY,
					(alpha << 24) | 0x00BFEFFF);
		}
		poseStack.popPose();
		poseStack.popPose();
	}

	public static void renderHeldDissolving(LivingFlailModel<?> model, LivingEntity holder, HumanoidArm arm,
			ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
			float progress, float seed) {
		Key key = new Key(holder.getUUID(), 0, arm == HumanoidArm.LEFT ? 0 : 1);
		poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		poseStack.mulPose(Axis.XN.rotationDegrees(80.0F));
		poseStack.translate(0.15D, -0.1D, -0.2D);
		PhysicsState state = STATES.computeIfAbsent(key, ignored -> new PhysicsState(holder));
		Vec3 bob = state.update(holder, false, 0.64D);
		float tilt = (float) Mth.clamp(bob.x * 48.0D + bob.z * 28.0D, -30.0D, 30.0D);
		VertexConsumer dissolve = buffer.getBuffer(HemoRenderTypes.hermitFarewellDissolve(
				TEXTURE, holder.tickCount, progress, seed));
		renderWithBob(model, poseStack, buffer, dissolve, packedLight, packedOverlay, bob, tilt, false);
	}

	private static void renderWithBob(LivingFlailModel<?> model, PoseStack poseStack, MultiBufferSource buffer,
			int packedLight, int packedOverlay, Vec3 bob, float tiltDegrees) {
		VertexConsumer base = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		renderWithBob(model, poseStack, buffer, base, packedLight, packedOverlay, bob, tiltDegrees, true);
	}

	private static void renderWithBob(LivingFlailModel<?> model, PoseStack poseStack, MultiBufferSource buffer,
			VertexConsumer base, int packedLight, int packedOverlay, Vec3 bob, float tiltDegrees,
			boolean renderGlint) {
		model.renderHandle(poseStack, base, packedLight, packedOverlay, -1);
		renderDroopingChainAndHead(model, poseStack, buffer, base, packedLight, packedOverlay, bob, tiltDegrees,
				renderGlint);
	}

	private static void renderDroopingChainAndHead(LivingFlailModel<?> model, PoseStack poseStack,
			MultiBufferSource buffer, VertexConsumer base, int packedLight, int packedOverlay, Vec3 bob,
			float tiltDegrees, boolean renderGlint) {
		poseStack.translate(0.2,0.1,0.2);
		poseStack.mulPose(Axis.XP.rotationDegrees(-20));
		poseStack.mulPose(Axis.YP.rotationDegrees(0));
		poseStack.mulPose(Axis.ZP.rotationDegrees(0));



		poseStack.pushPose();
		poseStack.translate(CHAIN_ANCHOR.x, CHAIN_ANCHOR.y, CHAIN_ANCHOR.z);
		poseStack.mulPose(Axis.YP.rotationDegrees(CHAIN_FACE_AWAY_YAW_DEGREES));
		poseStack.mulPose(Axis.ZP.rotationDegrees(CHAIN_DROOP_ROLL_DEGREES));
		poseStack.mulPose(Axis.XP.rotationDegrees(CHAIN_DROOP_PITCH_DEGREES));
		poseStack.translate(CHAIN_DROOP_OFFSET.x, CHAIN_DROOP_OFFSET.y, CHAIN_DROOP_OFFSET.z);
		renderChainFromAnchor(model, poseStack, base, packedLight, packedOverlay, bob);

		poseStack.pushPose();
		poseStack.translate(bob.x, bob.y, bob.z);
		poseStack.mulPose(Axis.ZP.rotationDegrees(tiltDegrees));
		poseStack.mulPose(Axis.XP.rotationDegrees((float) Mth.clamp(-bob.z * 36.0, -20.0, 20.0)));
		poseStack.scale(0.82f, 0.82f, 0.82f);
		model.renderHead(poseStack, base, packedLight, packedOverlay, -1);
		if (renderGlint) {
			VertexConsumer glint = buffer.getBuffer(RenderTypeInit.getCrimsonGlint());
			model.renderHead(poseStack, glint, packedLight, OverlayTexture.NO_OVERLAY, -1);
		}
		poseStack.popPose();
		poseStack.popPose();
	}

	private static void renderChainFromAnchor(LivingFlailModel<?> model, PoseStack poseStack,
			VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, Vec3 bob) {
		int links = 7;
		for (int i = 1; i <= links; i++) {
			double t = i / (double) (links + 1);
			Vec3 point = chainPoint(bob, t);
			Vec3 before = chainPoint(bob, Math.max(0.0, t - 0.06));
			Vec3 after = chainPoint(bob, Math.min(1.0, t + 0.06));
			Vec3 tangent = after.subtract(before);
			if (tangent.lengthSqr() < 1.0E-6) {
				tangent = new Vec3(0.0, -1.0, 0.0);
			} else {
				tangent = tangent.normalize();
			}

			poseStack.pushPose();
			poseStack.translate(point.x, point.y, point.z);
			poseStack.mulPose(new Quaternionf().rotateTo(0.0f, 1.0f, 0.0f,
					(float) tangent.x, (float) tangent.y, (float) tangent.z));
			poseStack.mulPose(Axis.YP.rotationDegrees(i % 2 == 0 ? 90.0f : 0.0f));
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) (Math.sin(t * Math.PI * 2.0) * 5.0)));
			poseStack.scale(0.5f, 0.5f, 0.5f);
			model.renderChainLink(poseStack, vertexConsumer, packedLight, packedOverlay, -1);
			poseStack.popPose();
		}
	}

	private static Vec3 chainPoint(Vec3 bob, double t) {
		Vec3 line = bob.scale(t);
		double horizontal = Math.sqrt(bob.x * bob.x + bob.z * bob.z);
		if (horizontal < 1.0E-4) {
			return line;
		}
		double curve = Math.sin(Math.PI * t);
		double sway = Mth.clamp(horizontal * 0.32, 0.0, 0.16) * curve;
		double sag = Mth.clamp(0.03 + horizontal * 0.1, 0.03, 0.1) * curve;
		Vec3 backward = new Vec3(-bob.x / horizontal, 0.0, -bob.z / horizontal).scale(sway);
		Vec3 cross = new Vec3(-bob.z / horizontal, 0.0, bob.x / horizontal)
				.scale(Math.sin(t * Math.PI * 2.0) * sway * 0.18);
		return line.add(backward).add(cross).add(0.0, sag, 0.0);
	}

	private record Key(UUID holderId, int view, int arm) {
	}

	private static final class PhysicsState {
		private double x;
		private double z;
		private double vx;
		private double vz;
		private float lastYaw;
		private int lastTick;

		private PhysicsState(LivingEntity holder) {
			this.lastYaw = holder.getYRot();
			this.lastTick = holder.tickCount;
		}

		private Vec3 update(LivingEntity holder, boolean firstPerson, double chainLength) {
			if (holder.tickCount == lastTick) {
				return currentBob(chainLength);
			}
			lastTick = holder.tickCount;

			Vec3 movement = holder.getDeltaMovement();
			float yawDelta = Mth.wrapDegrees(holder.getYRot() - lastYaw);
			lastYaw = holder.getYRot();

			double yawRad = Math.toRadians(holder.getYRot());
			double side = movement.x * Math.cos(yawRad) - movement.z * Math.sin(yawRad);
			double forward = movement.x * Math.sin(yawRad) + movement.z * Math.cos(yawRad);
			double attack = holder.swingTime > 0 ? 0.24 : 0.0;
			double limit = firstPerson ? 0.36 : 0.48;
			double targetX = Mth.clamp(-side * 10.5 - yawDelta * 0.006, -limit, limit);
			double targetZ = Mth.clamp(-forward * 9.5 - attack, -limit, limit);
			if (firstPerson) {
				targetX *= 1.2;
				targetZ *= 1.2;
			}

			vx += (targetX - x) * 0.14;
			vz += (targetZ - z) * 0.14;
			vx *= 0.84;
			vz *= 0.84;
			x = Mth.clamp(x + vx, -limit, limit);
			z = Mth.clamp(z + vz, -limit, limit);
			return currentBob(chainLength);
		}

		private Vec3 currentBob(double chainLength) {
			double slack = Math.max(0.0, chainLength * chainLength - x * x - z * z);
			return new Vec3(x, Math.sqrt(slack), z);
		}
	}
}
