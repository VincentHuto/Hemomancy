package com.vincenthuto.hemomancy.client.render.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class SanguineMonolithShatterRenderer {

	private static final int BURST_COUNT = 54;
	private static final float MIN_SIZE = 0.05f;
	private static final float MAX_SIZE = 0.16f;
	private static final int MIN_LIFETIME = 14;
	private static final int MAX_LIFETIME = 28;
	private static final double BASE_SPEED = 0.22;
	private static final double SPEED_VARIANCE = 0.24;
	private static final double DRAG = 0.93;
	private static final double GRAVITY = 0.015;
	private static final double ZERO_LENGTH_THRESHOLD = 1.0e-5;

	private static final List<Shard> ACTIVE_SHARDS = new ArrayList<>();

	public static void spawnBurst(Vec3 center, RandomSource random) {
		for (int i = 0; i < BURST_COUNT; i++) {
			Vec3 dir = randomDirection(random);
			double speed = BASE_SPEED + random.nextDouble() * SPEED_VARIANCE;
			Vec3 vel = dir.scale(speed);
			float size = MIN_SIZE + random.nextFloat() * (MAX_SIZE - MIN_SIZE);
			int lifetime = MIN_LIFETIME + random.nextInt(MAX_LIFETIME - MIN_LIFETIME + 1);
			Vec3 spinAxis = randomDirection(random);
			float spinSpeed = (random.nextFloat() - 0.5f) * 0.55f;
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
	}

	public static void render(PoseStack poseStack, float partialTick) {
		if (ACTIVE_SHARDS.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		VertexConsumer vc = buffer.getBuffer(RenderTypeInit.MONOLITH_SHATTER_TRIANGLES);
		Matrix4f mat = poseStack.last().pose();

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
}
