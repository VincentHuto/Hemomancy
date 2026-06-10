package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class BlackVeilRenderer {
	private static final int SEGMENTS = 64;
	private static final int RINGS = 10;
	private static final int VEINS = 14;
	private static final List<Entry> VEILS = new ArrayList<>();

	private BlackVeilRenderer() {
	}

	public static void addVeil(Vec3 center, float radius, int durationTicks, int seed) {
		Minecraft mc = Minecraft.getInstance();
		long now = mc.level == null ? 0 : mc.level.getGameTime();
		VEILS.add(new Entry(center, radius, now + durationTicks, seed));
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || VEILS.isEmpty()) return;

		long now = mc.level.getGameTime();
		Iterator<Entry> iterator = VEILS.iterator();
		while (iterator.hasNext()) {
			if (now > iterator.next().expiryTick()) iterator.remove();
		}
		if (VEILS.isEmpty()) return;

		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
		float time = now + partialTick;
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

		VertexConsumer glow = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
		for (Entry veil : VEILS) drawDome(poseStack, glow, cam, veil, time, true);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);

		VertexConsumer core = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		for (Entry veil : VEILS) drawDome(poseStack, core, cam, veil, time, false);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
	}

	private static void drawDome(PoseStack poseStack, VertexConsumer vc, Vec3 cam, Entry veil, float time, boolean glow) {
		poseStack.pushPose();
		poseStack.translate(veil.center.x - cam.x, veil.center.y - cam.y, veil.center.z - cam.z);
		Matrix4f mat = poseStack.last().pose();
		float alpha = glow ? 0.18F : 0.62F;
		float width = glow ? 0.34F : 0.11F;

		for (int ring = 1; ring <= RINGS; ring++) {
			double theta = (Math.PI * ring) / (RINGS + 1);
			float y = (float) (Math.cos(theta) * veil.radius);
			float radius = (float) (Math.sin(theta) * veil.radius);
			drawRing(vc, mat, radius, y, width, time, veil.seed + ring, alpha, glow);
		}
		for (int i = 0; i < VEINS; i++) {
			double angle = (Math.PI * 2.0 * i / VEINS) + Math.sin(time * 0.025 + veil.seed) * 0.12;
			drawVein(vc, mat, veil.radius, angle, width * 0.7F, time, veil.seed + i * 17, glow ? 0.22F : 0.78F);
		}
		poseStack.popPose();
	}

	private static void drawRing(VertexConsumer vc, Matrix4f mat, float radius, float y, float width,
			float time, int seed, float alpha, boolean glow) {
		for (int i = 0; i < SEGMENTS; i++) {
			double a1 = Math.PI * 2.0 * i / SEGMENTS;
			double a2 = Math.PI * 2.0 * (i + 1) / SEGMENTS;
			float wobble1 = (float) (Math.sin(a1 * 5.0 + time * 0.06 + seed) * 0.08);
			float wobble2 = (float) (Math.sin(a2 * 5.0 + time * 0.06 + seed) * 0.08);
			float r1 = radius + wobble1;
			float r2 = radius + wobble2;
			emitQuad(vc, mat,
					(float) Math.cos(a1) * (r1 - width), y, (float) Math.sin(a1) * (r1 - width),
					(float) Math.cos(a1) * (r1 + width), y, (float) Math.sin(a1) * (r1 + width),
					(float) Math.cos(a2) * (r2 + width), y, (float) Math.sin(a2) * (r2 + width),
					(float) Math.cos(a2) * (r2 - width), y, (float) Math.sin(a2) * (r2 - width),
					glow ? 0.20F : 0.01F, 0.0F, glow ? 0.33F : 0.03F, alpha);
		}
	}

	private static void drawVein(VertexConsumer vc, Matrix4f mat, float radius, double angle, float width,
			float time, int seed, float alpha) {
		float lastX = 0;
		float lastY = radius;
		float lastZ = 0;
		for (int step = 1; step <= RINGS; step++) {
			float t = (float) step / RINGS;
			float y = radius * (1.0F - t * 2.0F);
			float lateral = (float) Math.sin(Math.PI * t) * radius * (0.55F + 0.08F * (float) Math.sin(seed));
			double curl = angle + Math.sin(time * 0.04 + seed + t * 4.0F) * 0.18;
			float x = (float) Math.cos(curl) * lateral;
			float z = (float) Math.sin(curl) * lateral;
			emitSegment(vc, mat, lastX, lastY, lastZ, x, y, z, width * (1.2F - t), 0.34F, 0.0F, 0.42F, alpha);
			lastX = x;
			lastY = y;
			lastZ = z;
		}
	}

	private static void emitSegment(VertexConsumer vc, Matrix4f mat, float x1, float y1, float z1,
			float x2, float y2, float z2, float width, float r, float g, float b, float a) {
		float dx = x2 - x1;
		float dz = z2 - z1;
		float len = Math.max(0.001F, (float) Math.sqrt(dx * dx + dz * dz));
		float nx = -dz / len * width;
		float nz = dx / len * width;
		emitQuad(vc, mat, x1 - nx, y1, z1 - nz, x1 + nx, y1, z1 + nz, x2 + nx, y2, z2 + nz,
				x2 - nx, y2, z2 - nz, r, g, b, a);
	}

	private static void emitQuad(VertexConsumer vc, Matrix4f mat, float x1, float y1, float z1,
			float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4,
			float r, float g, float b, float a) {
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
		vc.addVertex(mat, x3, y3, z3).setColor(r, g, b, a);
		vc.addVertex(mat, x4, y4, z4).setColor(r, g, b, a);
	}

	private record Entry(Vec3 center, float radius, long expiryTick, int seed) {
	}
}
