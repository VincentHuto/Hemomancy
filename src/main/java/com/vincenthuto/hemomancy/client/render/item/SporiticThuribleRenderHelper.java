package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.SporiticThuribleModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SporiticThuribleRenderHelper {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/sporitic_thurible.png");
	private static final Map<Key, PhysicsState> STATES = new ConcurrentHashMap<>();
	private static final double STATIC_CHAIN_LENGTH = 0.56;
	private static final double FIRST_PERSON_CHAIN_LENGTH = 0.68;
	private static final double THIRD_PERSON_CHAIN_LENGTH = 0.64;

	private SporiticThuribleRenderHelper() {
	}

	public static void renderStatic(SporiticThuribleModel<?> model, ItemStack stack, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		renderWithBob(model, stack, poseStack, buffer, packedLight, packedOverlay,
				new Vec3(0.0, STATIC_CHAIN_LENGTH, 0.0), 0.0f);
	}

	public static void renderHeld(SporiticThuribleModel<?> model, LivingEntity holder, HumanoidArm arm, ItemStack stack,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, boolean firstPerson) {
		Key key = new Key(holder.getUUID(), firstPerson ? 1 : 0, arm == HumanoidArm.LEFT ? 0 : 1);
		PhysicsState state = STATES.computeIfAbsent(key, ignored -> new PhysicsState(holder));
		double chainLength = firstPerson ? FIRST_PERSON_CHAIN_LENGTH : THIRD_PERSON_CHAIN_LENGTH;
		Vec3 bob = state.update(holder, firstPerson, chainLength);
		float tilt = (float) Mth.clamp(bob.x * 44.0 + bob.z * 26.0, -26.0, 26.0);
		renderWithBob(model, stack, poseStack, buffer, packedLight, packedOverlay, bob, tilt);
	}

	private static void renderWithBob(SporiticThuribleModel<?> model, ItemStack stack, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay, Vec3 bob, float tiltDegrees) {
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		int color = getPackedColor(stack);
		renderChain(model, poseStack, vertexConsumer, packedLight, packedOverlay, color, bob);

		poseStack.pushPose();
		poseStack.translate(bob.x, bob.y, bob.z);
		poseStack.mulPose(Axis.ZP.rotationDegrees(tiltDegrees));
		poseStack.mulPose(Axis.XP.rotationDegrees((float) Mth.clamp(-bob.z * 34.0, -18.0, 18.0)));
		poseStack.scale(0.82f, 0.82f, 0.82f);
		model.renderBody(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		poseStack.popPose();
	}

	private static void renderChain(SporiticThuribleModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int color, Vec3 bob) {
		int links = 8;
		double yaw = Math.toDegrees(Math.atan2(bob.x, bob.z));
		double horizontal = Math.sqrt(bob.x * bob.x + bob.z * bob.z);
		double pitch = Math.toDegrees(Math.atan2(horizontal, Math.max(0.001, bob.y)));
		for (int i = 1; i <= links; i++) {
			double t = i / (double) (links + 1);
			poseStack.pushPose();
			poseStack.translate(bob.x * t, bob.y * t, bob.z * t);
			poseStack.mulPose(Axis.YP.rotationDegrees((float) yaw));
			poseStack.mulPose(Axis.XP.rotationDegrees((float) pitch));
			poseStack.mulPose(Axis.ZP.rotationDegrees(i % 2 == 0 ? 90.0f : 0.0f));
			poseStack.scale(0.55f, 0.55f, 0.55f);
			model.renderChainLink(poseStack, vertexConsumer, packedLight, packedOverlay, color);
			poseStack.popPose();
		}
	}

	private static int getPackedColor(ItemStack stack) {
		return 0xFFFFFFFF;
	}

	private record Key(UUID holderId, int view, int arm) {
	}

	private static final class PhysicsState {
		private double x;
		private double z;
		private double vx;
		private double vz;
		private double lastX;
		private double lastZ;
		private float lastYaw;
		private int lastTick;

		private PhysicsState(LivingEntity holder) {
			this.lastX = holder.getX();
			this.lastZ = holder.getZ();
			this.lastYaw = holder.getYRot();
			this.lastTick = holder.tickCount;
		}

		private Vec3 update(LivingEntity holder, boolean firstPerson, double chainLength) {
			if (holder.tickCount == lastTick) {
				return currentBob(chainLength);
			}
			lastTick = holder.tickCount;

			Vec3 movement = holder.getDeltaMovement();
			double dx = movement.x;
			double dz = movement.z;
			float yawDelta = Mth.wrapDegrees(holder.getYRot() - lastYaw);
			lastX = holder.getX();
			lastZ = holder.getZ();
			lastYaw = holder.getYRot();

			double yawRad = Math.toRadians(holder.getYRot());
			double side = dx * Math.cos(yawRad) - dz * Math.sin(yawRad);
			double forward = dx * Math.sin(yawRad) + dz * Math.cos(yawRad);
			double attack = holder.swingTime > 0 ? 0.16 : 0.0;
			double limit = firstPerson ? 0.34 : 0.46;
			double targetX = Mth.clamp(-side * 10.0 - yawDelta * 0.006, -limit, limit);
			double targetZ = Mth.clamp(-forward * 9.0 - attack, -limit, limit);
			if (firstPerson) {
				targetX *= 1.25;
				targetZ *= 1.25;
			}

			vx += (targetX - x) * 0.13;
			vz += (targetZ - z) * 0.13;
			vx *= 0.86;
			vz *= 0.86;
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
