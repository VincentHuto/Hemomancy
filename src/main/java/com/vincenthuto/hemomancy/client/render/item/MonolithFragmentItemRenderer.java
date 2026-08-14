package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.MonolithFragmentBurdenRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix4f;

public class MonolithFragmentItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final int BLACK = 0xFF000000;
	private static final int TRIANGLE_COUNT = 1;
	private static final int SUBDIVISIONS = 8;
	private static final float TIME_CYCLE_MS = 240000.0f;
	private static final float MORPH_PHASE_RATE = 0.0f;
	private static final float GUI_ROTATION_DEGREES_PER_SECOND = 0.0f;
	private static final float BREATHING_TILT_RATE = 0.72f;
	private static final float DEFAULT_ORBIT_DRIFT_RATE = 0.18f;
	private static final float GUI_SCALE = 1.55f;
	private static final float GUI_BOUND_RADIUS = 0.22f;
	private static final float GUI_ORBIT_DRIFT_RATE = 0.0f;
	private static final float GUI_SHARD_SIZE_SCALE = 0.68f;
	private static final float GUI_SHARD_SPREAD_SCALE = 0.25f;
	private static final float GUI_TILT_DEGREES = 0.0f;
	private static final float DEFAULT_TILT_DEGREES = 18.0f;
	public MonolithFragmentItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		RenderTuning tuning = resolveRenderTuning(displayContext);
		applyContextScale(poseStack, tuning);

		float timeSeconds = (System.currentTimeMillis() % (long) TIME_CYCLE_MS) / 1000.0f;
		float morphPhase = timeSeconds * MORPH_PHASE_RATE;
		poseStack.mulPose(Axis.YP.rotationDegrees(timeSeconds * GUI_ROTATION_DEGREES_PER_SECOND));
		poseStack.mulPose(Axis.XP.rotationDegrees(
				(float) Math.sin(timeSeconds * BREATHING_TILT_RATE) * tuning.tiltDegrees()));

		CarrierVisualState state = resolveCarrierVisualState();
		float shardSeed = Math.floorMod(System.identityHashCode(stack), 1009) / 1009.0f + stack.getCount() * 0.017f;
		float guiClamp = displayContext == ItemDisplayContext.GUI ? 1.0f : 0.0f;
		VertexConsumer vc = buffer.getBuffer(HemoRenderTypes.monolithFragment(timeSeconds, shardSeed,
				state.burden(), state.attuned(), guiClamp));
		Matrix4f mat = poseStack.last().pose();
		if (displayContext == ItemDisplayContext.GUI) {
			drawGuiBoundTriangle(vc, mat, timeSeconds, combinedLight);
		} else {
			for (int i = 0; i < TRIANGLE_COUNT; i++) {
				drawMorphingTriangle(vc, mat, i, timeSeconds, morphPhase, combinedLight, tuning);
			}
		}

		poseStack.popPose();
	}

	private static void applyContextScale(PoseStack poseStack, RenderTuning tuning) {
		poseStack.scale(tuning.contextScale(), tuning.contextScale(), tuning.contextScale());
	}

	private static RenderTuning resolveRenderTuning(ItemDisplayContext displayContext) {
		if (displayContext == ItemDisplayContext.GUI) {
			return new RenderTuning(GUI_SCALE, GUI_ORBIT_DRIFT_RATE, GUI_SHARD_SIZE_SCALE, GUI_SHARD_SPREAD_SCALE,
					GUI_TILT_DEGREES);
		} else if (displayContext == ItemDisplayContext.GROUND) {
			return new RenderTuning(1.15f, DEFAULT_ORBIT_DRIFT_RATE, 1.0f, 1.0f, DEFAULT_TILT_DEGREES);
		} else if (displayContext == ItemDisplayContext.FIXED) {
			return new RenderTuning(1.65f, DEFAULT_ORBIT_DRIFT_RATE, 1.0f, 1.0f, DEFAULT_TILT_DEGREES);
		} else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			return new RenderTuning(1.05f, DEFAULT_ORBIT_DRIFT_RATE, 1.0f, 1.0f, DEFAULT_TILT_DEGREES);
		} else {
			return new RenderTuning(0.9f, DEFAULT_ORBIT_DRIFT_RATE, 1.0f, 1.0f, DEFAULT_TILT_DEGREES);
		}
	}

	private static CarrierVisualState resolveCarrierVisualState() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return new CarrierVisualState(0.0f, 0.0f);
		}
		return HemoCapabilityAccess.getInitiatoryDegree(mc.player)
				.map(degree -> {
					boolean burden = MonolithFragmentBurdenRules.shouldBurdenCarrier(degree.getDegreeNumber());
					return new CarrierVisualState(burden ? 1.0f : 0.0f, 0.0f);
				})
				.orElse(new CarrierVisualState(0.0f, 0.0f));
	}

	private static void drawGuiBoundTriangle(VertexConsumer vc, Matrix4f mat, float time, int combinedLight) {
		float radius = GUI_BOUND_RADIUS;
		drawSubdividedTriangle(vc, mat,
				new Vec3f(-radius * 0.92f, -radius * 0.46f, 0.0f),
				new Vec3f(radius * 0.98f, -radius * 0.30f, 0.0f),
				new Vec3f(radius * 0.12f, radius * 0.86f, 0.0f), combinedLight);
	}

	private static void drawMorphingTriangle(VertexConsumer vc, Matrix4f mat, int index, float time, float morphPhase,
			int combinedLight, RenderTuning tuning) {
		float baseAngle = (float) (index * Math.PI * 2.0 / TRIANGLE_COUNT);
		float angle = baseAngle + morphNoise(index * 17 + 1, morphPhase) * 0.9f + time * tuning.orbitDriftRate();
		float spread = (0.035f + Math.abs(morphNoise(index * 23 + 3, morphPhase)) * 0.065f)
				* tuning.shardSpreadScale();
		float centerX = (float) Math.cos(angle) * spread;
		float centerZ = (float) Math.sin(angle) * spread;
		float centerY = morphNoise(index * 31 + 5, morphPhase) * 0.07f * tuning.shardSizeScale();

		float size = (0.105f + Math.abs(morphNoise(index * 37 + 7, morphPhase)) * 0.115f)
				* tuning.shardSizeScale();
		float shear = morphNoise(index * 41 + 11, morphPhase) * 0.08f * tuning.shardSizeScale();
		float tilt = morphNoise(index * 43 + 13, morphPhase) * 0.075f * tuning.shardSizeScale();
		float spin = angle + morphNoise(index * 47 + 17, morphPhase) * 1.7f;

		float x1 = centerX + (float) Math.cos(spin) * size;
		float z1 = centerZ + (float) Math.sin(spin) * size;
		float y1 = centerY + tilt;
		float x2 = centerX + (float) Math.cos(spin + 2.2f) * (size * 0.72f) + shear;
		float z2 = centerZ + (float) Math.sin(spin + 2.2f) * (size * 0.72f) - shear;
		float y2 = centerY - size * 0.44f;
		float x3 = centerX + (float) Math.cos(spin + 4.4f) * (size * 0.96f) - shear;
		float z3 = centerZ + (float) Math.sin(spin + 4.4f) * (size * 0.96f) + shear;
		float y3 = centerY + size * 0.36f + tilt;

		drawSubdividedTriangle(vc, mat,
				new Vec3f(x1, y1, z1), new Vec3f(x2, y2, z2), new Vec3f(x3, y3, z3), combinedLight);
	}

	private static void drawSubdividedTriangle(VertexConsumer vc, Matrix4f mat, Vec3f a, Vec3f b, Vec3f c,
			int combinedLight) {
		for (int i = 0; i < SUBDIVISIONS; i++) {
			for (int j = 0; j < SUBDIVISIONS - i; j++) {
				GridPoint p0 = point(a, b, c, i, j);
				GridPoint p1 = point(a, b, c, i + 1, j);
				GridPoint p2 = point(a, b, c, i, j + 1);
				drawTriangle(vc, mat, p0, p1, p2, combinedLight);
				if (j < SUBDIVISIONS - i - 1) {
					GridPoint p3 = point(a, b, c, i + 1, j + 1);
					drawTriangle(vc, mat, p1, p3, p2, combinedLight);
				}
			}
		}
	}

	private static GridPoint point(Vec3f a, Vec3f b, Vec3f c, int i, int j) {
		float ba = i / (float) SUBDIVISIONS;
		float bb = j / (float) SUBDIVISIONS;
		float bc = 1.0f - ba - bb;
		float x = a.x() * ba + b.x() * bb + c.x() * bc;
		float y = a.y() * ba + b.y() * bb + c.y() * bc;
		float z = a.z() * ba + b.z() * bb + c.z() * bc;
		float u = 0.5f * ba + bb;
		float v = ba;
		return new GridPoint(x, y, z, u, v);
	}

	private static void drawTriangle(VertexConsumer vc, Matrix4f mat, GridPoint p0, GridPoint p1, GridPoint p2,
			int combinedLight) {
		int a = (BLACK >>> 24) & 0xFF;
		int r = (BLACK >>> 16) & 0xFF;
		int g = (BLACK >>> 8) & 0xFF;
		int b = BLACK & 0xFF;
		vc.addVertex(mat, p0.x(), p0.y(), p0.z()).setColor(r, g, b, a).setUv(p0.u(), p0.v()).setLight(combinedLight);
		vc.addVertex(mat, p1.x(), p1.y(), p1.z()).setColor(r, g, b, a).setUv(p1.u(), p1.v()).setLight(combinedLight);
		vc.addVertex(mat, p2.x(), p2.y(), p2.z()).setColor(r, g, b, a).setUv(p2.u(), p2.v()).setLight(combinedLight);
	}

	private static float morphNoise(int seed, float morphPhase) {
		float basePhase = (float) Math.floor(morphPhase);
		float blend = smoothStep(morphPhase - basePhase);
		return lerp(noise(seed, basePhase), noise(seed, basePhase + 1.0f), blend);
	}

	private static float smoothStep(float value) {
		return value * value * (3.0f - 2.0f * value);
	}

	private static float lerp(float from, float to, float blend) {
		return from + (to - from) * blend;
	}

	private static float noise(int seed, float morphPhase) {
		int x = seed * 73428767 + (int) morphPhase * 912271;
		x ^= x >>> 13;
		x *= 1274126177;
		x ^= x >>> 16;
		return ((x & 0xFFFF) / 32767.5f) - 1.0f;
	}

	private record CarrierVisualState(float burden, float attuned) {
	}

	private record RenderTuning(float contextScale, float orbitDriftRate, float shardSizeScale,
			float shardSpreadScale, float tiltDegrees) {
	}

	private record GridPoint(float x, float y, float z, float u, float v) {
	}

	private record Vec3f(float x, float y, float z) {
	}
}
