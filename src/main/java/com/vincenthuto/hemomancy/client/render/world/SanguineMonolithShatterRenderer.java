package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SanguineMonolithShatterRenderer {

	private static final int BURST_COUNT = 54;
	private static final int SHARD_PULSE_COUNT = 18;
	private static final float MIN_SIZE = 0.05f;
	private static final float MAX_SIZE = 0.16f;
	private static final int MIN_LIFETIME = 14;
	private static final int MAX_LIFETIME = 28;
	private static final double BASE_SPEED = 0.22;
	private static final double SPEED_VARIANCE = 0.24;
	private static final double DRAG = 0.93;
	private static final double GRAVITY = 0.015;
	private static final double ZERO_LENGTH_THRESHOLD = 1.0e-5;
	private static final int PULSE_LIFETIME = 30;
	private static final float PULSE_CORE_MAX_RADIUS = 1.15f;
	private static final float PULSE_SHELL_MAX_RADIUS = 3.8f;
	private static final int POME_PULSE_LIFETIME = 24;
	private static final float POME_PULSE_CORE_MAX_RADIUS = 0.85f;
	private static final float POME_PULSE_SHELL_MAX_RADIUS = 2.65f;
	private static final int OMEN_BURST_COUNT = 36;
	private static final int OMEN_PULSE_LIFETIME = 38;
	private static final float OMEN_PULSE_CORE_MAX_RADIUS = 1.35f;
	private static final float OMEN_PULSE_SHELL_MAX_RADIUS = 5.25f;
	private static final int PULSE_RINGS = 10;
	private static final int PULSE_SEGMENTS = 18;

	private static final List<Shard> ACTIVE_SHARDS = new ArrayList<>();
	private static final List<Pulse> ACTIVE_PULSES = new ArrayList<>();

	public static void spawnBurst(Vec3 center, RandomSource random) {
		ACTIVE_PULSES.add(new Pulse(center, PULSE_CORE_MAX_RADIUS, PULSE_SHELL_MAX_RADIUS, PULSE_LIFETIME));
		spawnShards(center, random, BURST_COUNT, 1.0D, 1.0F, 0);
	}

	public static void spawnShardPulse(Vec3 center, RandomSource random) {
		spawnShards(center, random, SHARD_PULSE_COUNT, 0.62D, 0.85F, -4);
	}

	private static void spawnShards(Vec3 center, RandomSource random, int count, double speedScale,
			float sizeScale, int lifetimeOffset) {
		for (int i = 0; i < count; i++) {
			Vec3 dir = randomDirection(random);
			double speed = (BASE_SPEED + random.nextDouble() * SPEED_VARIANCE) * speedScale;
			Vec3 vel = dir.scale(speed);
			float size = (MIN_SIZE + random.nextFloat() * (MAX_SIZE - MIN_SIZE)) * sizeScale;
			int lifetime = Math.max(6, MIN_LIFETIME + random.nextInt(MAX_LIFETIME - MIN_LIFETIME + 1)
					+ lifetimeOffset);
			Vec3 spinAxis = randomDirection(random);
			float spinSpeed = (random.nextFloat() - 0.5f) * 0.55f;
			Vec3 v1 = randomDirection(random).scale(size);
			Vec3 v2 = randomDirection(random).scale(size);
			Vec3 v3 = randomDirection(random).scale(size);
			ACTIVE_SHARDS.add(new Shard(center, vel, v1, v2, v3, spinAxis, spinSpeed, lifetime));
		}
	}

	public static void spawnOmenBurst(Vec3 center) {
		RandomSource random = Minecraft.getInstance().level != null
				? Minecraft.getInstance().level.random
				: RandomSource.create();
		ACTIVE_PULSES.add(new Pulse(center, OMEN_PULSE_CORE_MAX_RADIUS, OMEN_PULSE_SHELL_MAX_RADIUS,
				OMEN_PULSE_LIFETIME));
		for (int i = 0; i < OMEN_BURST_COUNT; i++) {
			Vec3 dir = randomDirection(random);
			double speed = (BASE_SPEED + random.nextDouble() * SPEED_VARIANCE) * 0.82D;
			Vec3 vel = dir.scale(speed);
			float size = (MIN_SIZE + random.nextFloat() * (MAX_SIZE - MIN_SIZE)) * 1.25F;
			int lifetime = MIN_LIFETIME + random.nextInt(MAX_LIFETIME - MIN_LIFETIME + 1) + 8;
			Vec3 spinAxis = randomDirection(random);
			float spinSpeed = (random.nextFloat() - 0.5f) * 0.45f;
			Vec3 v1 = randomDirection(random).scale(size);
			Vec3 v2 = randomDirection(random).scale(size);
			Vec3 v3 = randomDirection(random).scale(size);
			ACTIVE_SHARDS.add(new Shard(center, vel, v1, v2, v3, spinAxis, spinSpeed, lifetime));
		}
	}

	public static void tick() {
		Iterator<Shard> it = ACTIVE_SHARDS.iterator();
		while (it.hasNext()) {
			Shard shard = it.next();
			shard.tick();
			if (shard.isDead()) {
				it.remove();
			}
		}

		Iterator<Pulse> pulseIt = ACTIVE_PULSES.iterator();
		while (pulseIt.hasNext()) {
			Pulse pulse = pulseIt.next();
			pulse.tick();
			if (pulse.isDead()) {
				pulseIt.remove();
			}
		}
	}

	public static void spawnPomePulse(Vec3 center) {
		ACTIVE_PULSES.add(new Pulse(center, POME_PULSE_CORE_MAX_RADIUS, POME_PULSE_SHELL_MAX_RADIUS, POME_PULSE_LIFETIME));
	}

	public static void render(PoseStack poseStack, float partialTick) {
		if (ACTIVE_SHARDS.isEmpty() && ACTIVE_PULSES.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		VertexConsumer vc = buffer.getBuffer(RenderTypeInit.MONOLITH_SHATTER_TRIANGLES);
		Matrix4f mat = poseStack.last().pose();

		for (Pulse pulse : ACTIVE_PULSES) {
			float age = pulse.age + partialTick;
			float lifeCoeff = Math.min(1f, age / pulse.lifetime);
			float shellEase = easeOutCubic(lifeCoeff);
			float coreEase = easeOutCubic(Math.min(1f, lifeCoeff * 1.8f));
			float shellAlpha = 0.62f * (1f - lifeCoeff) * (1f - lifeCoeff);
			float coreAlpha = 0.82f * Math.max(0f, 1f - lifeCoeff * 1.35f);
			Vec3 center = pulse.center.subtract(cam);

			if (coreAlpha > 0.01f) {
				drawSphere(vc, mat, center, 0.12f + pulse.coreMaxRadius * coreEase, coreAlpha);
			}
			if (shellAlpha > 0.01f) {
				drawSphere(vc, mat, center, 0.25f + pulse.shellMaxRadius * shellEase, shellAlpha);
			}
		}

		for (Shard shard : ACTIVE_SHARDS) {
			float age = shard.age + partialTick;
			float lifeCoeff = Math.min(1f, age / shard.lifetime);
			float alpha = 0.95f * (1f - lifeCoeff);
			if (alpha <= 0.01f) continue;

			Vec3 center = shard.prevPos.lerp(shard.pos, partialTick).subtract(cam);
			float angle = (shard.age + partialTick) * shard.spinSpeed;
			Vec3 rv1 = rotate(shard.v1, shard.spinAxis, angle).add(center);
			Vec3 rv2 = rotate(shard.v2, shard.spinAxis, angle).add(center);
			Vec3 rv3 = rotate(shard.v3, shard.spinAxis, angle).add(center);

			vc.addVertex(mat, (float) rv1.x, (float) rv1.y, (float) rv1.z).setColor(0f, 0f, 0f, alpha);
			vc.addVertex(mat, (float) rv2.x, (float) rv2.y, (float) rv2.z).setColor(0f, 0f, 0f, alpha);
			vc.addVertex(mat, (float) rv3.x, (float) rv3.y, (float) rv3.z).setColor(0f, 0f, 0f, alpha);
		}

		buffer.endBatch(RenderTypeInit.MONOLITH_SHATTER_TRIANGLES);
	}

	private static Vec3 randomDirection(RandomSource random) {
		for (int i = 0; i < 8; i++) {
			double x = random.nextDouble() * 2.0 - 1.0;
			double y = random.nextDouble() * 2.0 - 1.0;
			double z = random.nextDouble() * 2.0 - 1.0;
			Vec3 v = new Vec3(x, y, z);
			double lenSq = v.lengthSqr();
			if (lenSq >= ZERO_LENGTH_THRESHOLD && lenSq <= 1.0) {
				return v.normalize();
			}
		}
		return new Vec3(0, 1, 0);
	}

	private static void drawSphere(VertexConsumer vc, Matrix4f mat, Vec3 center, float radius, float alpha) {
		for (int ring = 0; ring < PULSE_RINGS; ring++) {
			double phi1 = -Math.PI / 2.0 + Math.PI * ring / PULSE_RINGS;
			double phi2 = -Math.PI / 2.0 + Math.PI * (ring + 1) / PULSE_RINGS;
			for (int segment = 0; segment < PULSE_SEGMENTS; segment++) {
				double theta1 = Math.PI * 2.0 * segment / PULSE_SEGMENTS;
				double theta2 = Math.PI * 2.0 * (segment + 1) / PULSE_SEGMENTS;

				Vec3 p1 = spherePoint(center, radius, phi1, theta1);
				Vec3 p2 = spherePoint(center, radius, phi2, theta1);
				Vec3 p3 = spherePoint(center, radius, phi2, theta2);
				Vec3 p4 = spherePoint(center, radius, phi1, theta2);

				vertex(vc, mat, p1, alpha);
				vertex(vc, mat, p2, alpha);
				vertex(vc, mat, p3, alpha);
				vertex(vc, mat, p1, alpha);
				vertex(vc, mat, p3, alpha);
				vertex(vc, mat, p4, alpha);
			}
		}
	}

	private static Vec3 spherePoint(Vec3 center, float radius, double phi, double theta) {
		double cosPhi = Math.cos(phi);
		return center.add(
				radius * cosPhi * Math.cos(theta),
				radius * Math.sin(phi),
				radius * cosPhi * Math.sin(theta));
	}

	private static void vertex(VertexConsumer vc, Matrix4f mat, Vec3 pos, float alpha) {
		vc.addVertex(mat, (float) pos.x, (float) pos.y, (float) pos.z).setColor(0f, 0f, 0f, alpha);
	}

	private static float easeOutCubic(float value) {
		float inverse = 1f - value;
		return 1f - inverse * inverse * inverse;
	}

	private static Vec3 rotate(Vec3 v, Vec3 axis, float angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		Vec3 cross = axis.cross(v);
		double dot = axis.dot(v);
		return v.scale(cos).add(cross.scale(sin)).add(axis.scale(dot * (1.0 - cos)));
	}

	private static class Shard {
		private Vec3 pos;
		private Vec3 prevPos;
		private Vec3 vel;
		private final Vec3 v1;
		private final Vec3 v2;
		private final Vec3 v3;
		private final Vec3 spinAxis;
		private final float spinSpeed;
		private final int lifetime;
		private int age;

		private Shard(Vec3 pos, Vec3 vel, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 spinAxis, float spinSpeed, int lifetime) {
			this.pos = pos;
			this.prevPos = pos;
			this.vel = vel;
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.spinAxis = spinAxis;
			this.spinSpeed = spinSpeed;
			this.lifetime = lifetime;
			this.age = 0;
		}

		private void tick() {
			prevPos = pos;
			pos = pos.add(vel);
			vel = vel.scale(DRAG).add(0, -GRAVITY, 0);
			age++;
		}

		private boolean isDead() {
			return age >= lifetime;
		}
	}

	private static class Pulse {
		private final Vec3 center;
		private final float coreMaxRadius;
		private final float shellMaxRadius;
		private final int lifetime;
		private int age;

		private Pulse(Vec3 center, float coreMaxRadius, float shellMaxRadius, int lifetime) {
			this.center = center;
			this.coreMaxRadius = coreMaxRadius;
			this.shellMaxRadius = shellMaxRadius;
			this.lifetime = lifetime;
			this.age = 0;
		}

		private void tick() {
			age++;
		}

		private boolean isDead() {
			return age >= lifetime;
		}
	}
}
