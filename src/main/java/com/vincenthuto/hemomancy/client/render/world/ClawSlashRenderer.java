package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ClawSlashRenderer {
	private static final List<Slash> ACTIVE_SLASHES = new ArrayList<>();
	private static final int LIFETIME_TICKS = 11;
	private static final int STROKE_COUNT = 3;
	private static final int SEGMENTS = 10;
	private static final double ZERO_LENGTH_THRESHOLD = 1.0E-5D;
	private static final float BASE_LENGTH = 2.25F;
	private static final float BASE_ARC_HEIGHT = 0.56F;
	private static final float BASE_CORE_WIDTH = 0.075F;
	private static final float BASE_GLOW_WIDTH = 0.28F;
	private static final float STROKE_SPACING = 0.34F;
	private static final float MAX_ROLL_RADIANS = 0.55F;
	private static final float MAX_YAW_RADIANS = 0.18F;
	private static final float MAX_PITCH_RADIANS = 0.14F;
	private static final float MAX_LENGTH_JITTER = 0.18F;
	private static final float MAX_ARC_JITTER = 0.22F;
	private static final float MAX_STROKE_OFFSET_JITTER = 0.12F;
	private static final float MAX_CURVE_JITTER = 0.16F;

	private ClawSlashRenderer() {
	}

	public static void spawn(Vec3 center, Vec3 forward, ParticleColor color, boolean ambush, float scale, int seed) {
		Vec3 normal = forward.lengthSqr() < ZERO_LENGTH_THRESHOLD ? new Vec3(0.0D, 0.0D, 1.0D) : forward.normalize();
		ACTIVE_SLASHES.add(new Slash(center, randomizedForward(normal, seed), color, ambush, Math.max(0.25F, scale), seed));
	}

	public static void tick() {
		Iterator<Slash> iterator = ACTIVE_SLASHES.iterator();
		while (iterator.hasNext()) {
			Slash slash = iterator.next();
			slash.age++;
			if (slash.age >= slash.lifetime) {
				iterator.remove();
			}
		}
	}

	public static void render(PoseStack poseStack, float partialTick) {
		if (ACTIVE_SLASHES.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		Matrix4f matrix = poseStack.last().pose();

		VertexConsumer glow = buffer.getBuffer(RenderTypeInit.CLAW_SLASH_GLOW);
		for (Slash slash : ACTIVE_SLASHES) {
			drawSlash(glow, matrix, slash, camera, partialTick, true);
		}
		buffer.endBatch(RenderTypeInit.CLAW_SLASH_GLOW);

		VertexConsumer core = buffer.getBuffer(RenderTypeInit.CLAW_SLASH_CORE);
		for (Slash slash : ACTIVE_SLASHES) {
			drawSlash(core, matrix, slash, camera, partialTick, false);
		}
		buffer.endBatch(RenderTypeInit.CLAW_SLASH_CORE);
	}

	private static void drawSlash(VertexConsumer consumer, Matrix4f matrix, Slash slash, Vec3 camera, float partialTick,
			boolean glow) {
		float age = slash.age + partialTick;
		float life = Mth.clamp(age / slash.lifetime, 0.0F, 1.0F);
		float reveal = easeOutCubic(Mth.clamp(life * 2.25F, 0.0F, 1.0F));
		float fade = (1.0F - life) * (1.0F - life * 0.38F);
		if (fade <= 0.01F) return;

		Vec3 forward = slash.forward;
		Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
		if (right.lengthSqr() < ZERO_LENGTH_THRESHOLD) {
			right = forward.cross(new Vec3(1.0D, 0.0D, 0.0D));
		}
		right = right.normalize();
		Vec3 up = right.cross(forward).normalize();
		if ((slash.seed & 1) != 0) {
			up = up.reverse();
		}

		float roll = seededSignedUnit(slash.seed, 3) * MAX_ROLL_RADIANS;
		Vec3 tiltUp = rotateAroundAxis(up, forward, roll);
		Vec3 tiltRight = forward.cross(tiltUp).normalize();
		float baseLength = BASE_LENGTH * slash.scale * (0.82F + reveal * 0.18F);
		float baseArcHeight = BASE_ARC_HEIGHT * slash.scale;
		Vec3 center = slash.center.add(forward.scale(0.03D * age)).subtract(camera);

		float r = normalizeColor(slash.color.getRed());
		float g = normalizeColor(slash.color.getGreen());
		float b = normalizeColor(slash.color.getBlue());
		float alphaBase = glow ? (slash.ambush ? 0.42F : 0.31F) : (slash.ambush ? 0.94F : 0.78F);
		float widthBase = (glow ? BASE_GLOW_WIDTH : BASE_CORE_WIDTH) * slash.scale;

		for (int stroke = 0; stroke < STROKE_COUNT; stroke++) {
			float lengthJitter = 1.0F + seededSignedUnit(slash.seed, 17 + stroke * 11) * MAX_LENGTH_JITTER;
			float arcJitter = 1.0F + seededSignedUnit(slash.seed, 23 + stroke * 11) * MAX_ARC_JITTER;
			float length = baseLength * lengthJitter;
			float arcHeight = baseArcHeight * arcJitter;
			float strokeOffset = (stroke - 1) * STROKE_SPACING * slash.scale
					+ strokeShapeOffset(slash.seed, stroke) * slash.scale;
			float stagger = stroke * 0.035F;
			float visible = Mth.clamp((reveal - stagger) / (1.0F - stagger), 0.0F, 1.0F);
			if (visible <= 0.0F) continue;
			int visibleSegments = Math.max(1, Mth.ceil(SEGMENTS * visible));
			for (int segment = 0; segment < visibleSegments; segment++) {
				float t0 = (float) segment / SEGMENTS;
				float t1 = (float) (segment + 1) / SEGMENTS;
				Vec3 p0 = pointOnStroke(center, tiltRight, tiltUp, length, arcHeight, strokeOffset, t0, slash.seed, stroke);
				Vec3 p1 = pointOnStroke(center, tiltRight, tiltUp, length, arcHeight, strokeOffset, t1, slash.seed, stroke);
				float w0 = taperedWidth(t0, widthBase);
				float w1 = taperedWidth(t1, widthBase);
				float a0 = alphaAt(t0, alphaBase, fade, glow);
				float a1 = alphaAt(t1, alphaBase, fade, glow);
				emitSegment(consumer, matrix, forward, p0, p1, w0, w1, r, g, b, a0, a1);
			}
		}
	}

	private static Vec3 pointOnStroke(Vec3 center, Vec3 right, Vec3 up, float length, float arcHeight,
			float strokeOffset, float t, int seed, int stroke) {
		float x = (t - 0.5F) * length;
		float curve = Mth.sin(t * Mth.PI) * arcHeight;
		float hook = (t - 0.5F) * (t - 0.5F) * 0.22F * (((seed >>> 4) & 1) == 0 ? 1.0F : -1.0F);
		float wobble = curveJitter(t, seed, stroke);
		return center.add(right.scale(x + wobble * 0.22F)).add(up.scale(curve + strokeOffset + hook + wobble));
	}

	private static float taperedWidth(float t, float maxWidth) {
		float taper = Mth.sin(Mth.clamp(t, 0.0F, 1.0F) * Mth.PI);
		return maxWidth * (0.08F + 0.92F * (float) Math.pow(taper, 0.85D));
	}

	private static float alphaAt(float t, float alphaBase, float fade, boolean glow) {
		float taper = Mth.sin(Mth.clamp(t, 0.0F, 1.0F) * Mth.PI);
		float edgeBias = glow ? 0.58F : 0.88F;
		return alphaBase * fade * (0.18F + edgeBias * taper);
	}

	private static void emitSegment(VertexConsumer consumer, Matrix4f matrix, Vec3 forward, Vec3 p0, Vec3 p1,
			float w0, float w1, float r, float g, float b, float a0, float a1) {
		Vec3 tangent = p1.subtract(p0);
		if (tangent.lengthSqr() < ZERO_LENGTH_THRESHOLD) return;
		Vec3 widthDir = forward.cross(tangent).normalize();
		Vec3 p0a = p0.add(widthDir.scale(w0));
		Vec3 p0b = p0.subtract(widthDir.scale(w0));
		Vec3 p1a = p1.add(widthDir.scale(w1));
		Vec3 p1b = p1.subtract(widthDir.scale(w1));
		vertex(consumer, matrix, p0a, r, g, b, a0);
		vertex(consumer, matrix, p0b, r, g, b, a0);
		vertex(consumer, matrix, p1b, r, g, b, a1);
		vertex(consumer, matrix, p1a, r, g, b, a1);
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos, float r, float g, float b, float a) {
		consumer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z).setColor(r, g, b, a);
	}

	private static float normalizeColor(float value) {
		return value > 1.0F ? value / 255.0F : value;
	}

	private static float easeOutCubic(float value) {
		float inverse = 1.0F - value;
		return 1.0F - inverse * inverse * inverse;
	}

	private static Vec3 randomizedForward(Vec3 forward, int seed) {
		Vec3 right = perpendicularRight(forward);
		Vec3 up = right.cross(forward).normalize();
		float yaw = seededSignedUnit(seed, 41) * MAX_YAW_RADIANS;
		float pitch = seededSignedUnit(seed, 53) * MAX_PITCH_RADIANS;
		Vec3 yawed = rotateAroundAxis(forward, up, yaw);
		return rotateAroundAxis(yawed, right, pitch).normalize();
	}

	private static Vec3 perpendicularRight(Vec3 forward) {
		Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
		if (right.lengthSqr() < ZERO_LENGTH_THRESHOLD) {
			right = forward.cross(new Vec3(1.0D, 0.0D, 0.0D));
		}
		return right.normalize();
	}

	private static float strokeShapeOffset(int seed, int stroke) {
		return seededSignedUnit(seed, 71 + stroke * 13) * MAX_STROKE_OFFSET_JITTER;
	}

	private static float curveJitter(float t, int seed, int stroke) {
		float phase = seededSignedUnit(seed, 89 + stroke * 17) * Mth.PI;
		float frequency = 1.35F + Math.abs(seededSignedUnit(seed, 101 + stroke * 17)) * 1.45F;
		float envelope = Mth.sin(Mth.clamp(t, 0.0F, 1.0F) * Mth.PI);
		return Mth.sin(t * Mth.PI * frequency + phase) * envelope * MAX_CURVE_JITTER;
	}

	private static Vec3 rotateAroundAxis(Vec3 value, Vec3 axis, float angle) {
		Vec3 normal = axis.normalize();
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		Vec3 cross = normal.cross(value);
		double dot = normal.dot(value);
		return value.scale(cos).add(cross.scale(sin)).add(normal.scale(dot * (1.0D - cos)));
	}

	private static float seededSignedUnit(int seed, int salt) {
		int mixed = seed ^ (salt * 0x9E3779B9);
		mixed ^= mixed >>> 16;
		mixed *= 0x7FEB352D;
		mixed ^= mixed >>> 15;
		mixed *= 0x846CA68B;
		mixed ^= mixed >>> 16;
		return ((mixed & 0xFFFF) / 32767.5F) - 1.0F;
	}

	private static final class Slash {
		private final Vec3 center;
		private final Vec3 forward;
		private final ParticleColor color;
		private final boolean ambush;
		private final float scale;
		private final int seed;
		private final int lifetime;
		private int age;

		private Slash(Vec3 center, Vec3 forward, ParticleColor color, boolean ambush, float scale, int seed) {
			this.center = center;
			this.forward = forward;
			this.color = color;
			this.ambush = ambush;
			this.scale = scale;
			this.seed = seed;
			this.lifetime = LIFETIME_TICKS;
		}
	}
}
