package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.SporiticThuribleModel;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleSpore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SporiticThuribleRenderHelper {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/sporitic_thurible.png");
	private static final Map<Key, PhysicsState> STATES = new ConcurrentHashMap<>();

    private SporiticThuribleRenderHelper() {
	}

	public static void renderStatic(SporiticThuribleModel<?> model, ItemStack stack, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double STATIC_CHAIN_LENGTH = 0.56;
        renderWithBob(model, stack, poseStack, buffer, packedLight, packedOverlay,
				new Vec3(0.0, STATIC_CHAIN_LENGTH, 0.0), 0.0f, null, false);
	}

	public static void renderHeld(SporiticThuribleModel<?> model, LivingEntity holder, HumanoidArm arm, ItemStack stack,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, boolean firstPerson) {
		Key key = new Key(holder.getUUID(), firstPerson ? 1 : 0, arm == HumanoidArm.LEFT ? 0 : 1);
		PhysicsState state = STATES.computeIfAbsent(key, ignored -> new PhysicsState(holder));
        double THIRD_PERSON_CHAIN_LENGTH = 0.54;
        double FIRST_PERSON_CHAIN_LENGTH = 0.68;
        double chainLength = firstPerson ? FIRST_PERSON_CHAIN_LENGTH : THIRD_PERSON_CHAIN_LENGTH;
		Vec3 bob = state.update(holder, firstPerson, chainLength);
		float tilt = (float) Mth.clamp(bob.x * 44.0 + bob.z * 26.0, -26.0, 26.0);
		renderWithBob(model, stack, poseStack, buffer, packedLight, packedOverlay, bob, tilt, state, firstPerson);
	}

	private static void renderWithBob(SporiticThuribleModel<?> model, ItemStack stack, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay, Vec3 bob, float tiltDegrees,
			PhysicsState physicsState, boolean firstPerson) {
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		int color = getPackedColor(stack);
		renderChain(model, poseStack, vertexConsumer, packedLight, packedOverlay, color, bob);

		poseStack.pushPose();
		poseStack.translate(bob.x, bob.y, bob.z);
		poseStack.mulPose(Axis.ZP.rotationDegrees(tiltDegrees));
		poseStack.mulPose(Axis.XP.rotationDegrees((float) Mth.clamp(-bob.z * 34.0, -18.0, 18.0)));
		poseStack.scale(0.82f, 0.82f, 0.82f);
		model.renderBody(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		renderBurningSpore(stack, poseStack, buffer, packedOverlay, physicsState, firstPerson);
		poseStack.popPose();
	}

	private static void renderBurningSpore(ItemStack thuribleStack, PoseStack poseStack, MultiBufferSource buffer,
			int packedOverlay, PhysicsState physicsState, boolean firstPerson) {
		if (!SporiticThuribleItem.isLit(thuribleStack)) {
			return;
		}
		SporiticThuribleSpore spore = SporiticThuribleItem.getStoredSpore(thuribleStack).orElse(null);
		if (spore == null) {
			return;
		}

		ItemStack sporeStack = spore.displayStack();
		if (sporeStack.isEmpty()) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		float age = minecraft.level != null ? minecraft.level.getGameTime() + minecraft.getTimer().getGameTimeDeltaPartialTick(false) : 0.0f;
		float pulse = 0.24f + Mth.sin(age * 0.18f) * 0.014f;
		poseStack.pushPose();
		poseStack.translate(0.0f, 0.1f, -0f);
		emitBurningSporeParticle(minecraft, poseStack, physicsState, age, firstPerson);
		poseStack.mulPose(Axis.YP.rotationDegrees(age * 3.0f));
		poseStack.mulPose(Axis.XP.rotationDegrees(16.0f));
		renderBurningSporePlane(minecraft, sporeStack, poseStack, buffer, packedOverlay, pulse, spore.ordinal());
		poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
		renderBurningSporePlane(minecraft, sporeStack, poseStack, buffer, packedOverlay, pulse, spore.ordinal() + 31);
		poseStack.popPose();
	}

	private static void emitBurningSporeParticle(Minecraft minecraft, PoseStack poseStack, PhysicsState physicsState,
			float age, boolean firstPerson) {
		if (physicsState == null || minecraft.level == null || minecraft.cameraEntity == null) {
			return;
		}
		long gameTime = minecraft.level.getGameTime();
		if (!physicsState.shouldEmitFlame(gameTime)) {
			return;
		}

		float localX = firstPerson ? -0.45f : 0.0f;
		float localZ = firstPerson ? 0.15f : 0.0f;
		Vector3f renderPosition = poseStack.last().pose().transformPosition(localX, -0.14f, localZ, new Vector3f());
		Vec3 cameraPosition = minecraft.gameRenderer.getMainCamera().getPosition();
		double wobbleX = Mth.sin(age * 0.73f) * 0.006;
		double wobbleZ = Mth.cos(age * 0.61f) * 0.006;
		minecraft.level.addParticle(ParticleTypes.FLAME,
				cameraPosition.x + renderPosition.x() + wobbleX,
				cameraPosition.y + renderPosition.y(),
				cameraPosition.z + renderPosition.z() + wobbleZ,
				0.0D, 0.004D, 0.0D);
	}

	private static void renderBurningSporePlane(Minecraft minecraft, ItemStack sporeStack, PoseStack poseStack,
			MultiBufferSource buffer, int packedOverlay, float scale, int seed) {
		poseStack.pushPose();
		poseStack.scale(scale, scale, scale);
		minecraft.getItemRenderer().renderStatic(sporeStack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
				packedOverlay, poseStack, buffer, minecraft.level, seed);
		poseStack.popPose();
	}

	private static void renderChain(SporiticThuribleModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int color, Vec3 bob) {
		int links = 9;
		for (int i = 1; i <= links; i++) {
			double t = i / (double) (links + 1);
			double sampleDistance = 0.055;
			Vec3 point = chainPoint(bob, t);
			Vec3 before = chainPoint(bob, Math.max(0.0, t - sampleDistance));
			Vec3 after = chainPoint(bob, Math.min(1.0, t + sampleDistance));
			Vec3 tangent = after.subtract(before);
			if (tangent.lengthSqr() < 1.0E-6) {
				tangent = new Vec3(0.0, 1.0, 0.0);
			} else {
				tangent = tangent.normalize();
			}

			poseStack.pushPose();
			poseStack.translate(point.x, point.y, point.z);
			poseStack.mulPose(new Quaternionf().rotateTo(0.0f, 1.0f, 0.0f,
					(float) tangent.x, (float) tangent.y, (float) tangent.z));
			poseStack.mulPose(Axis.YP.rotationDegrees(i % 2 == 0 ? 90.0f : 0.0f));
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) (Math.sin(t * Math.PI * 2.0) * 5.0)));
			poseStack.scale(0.48f, 0.48f, 0.48f);
			model.renderChainLink(poseStack, vertexConsumer, packedLight, packedOverlay, color);
			poseStack.popPose();
		}
	}

	private static Vec3 chainPoint(Vec3 bob, double t) {
		double horizontal = Math.sqrt(bob.x * bob.x + bob.z * bob.z);
		Vec3 line = bob.scale(t);
		if (horizontal < 1.0E-4) {
			return line;
		}

		double curve = Math.sin(Math.PI * t);
		double sway = Mth.clamp(horizontal * 0.35, 0.0, 0.14) * curve;
		double sag = Mth.clamp(0.025 + horizontal * 0.12, 0.025, 0.095) * curve;
		Vec3 backward = new Vec3(-bob.x / horizontal, 0.0, -bob.z / horizontal).scale(sway);
		Vec3 cross = new Vec3(-bob.z / horizontal, 0.0, bob.x / horizontal)
				.scale(Math.sin(t * Math.PI * 2.0) * sway * 0.22);
		return line.add(backward).add(cross).add(0.0, sag, 0.0);
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
		private long lastFlameParticleTick = Long.MIN_VALUE;

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

		private boolean shouldEmitFlame(long gameTime) {
			if (lastFlameParticleTick == gameTime) {
				return false;
			}
			lastFlameParticleTick = gameTime;
			return true;
		}
	}
}
