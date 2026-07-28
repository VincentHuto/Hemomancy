package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.SanguineFormationProjectionRenderer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilPose;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilAnatomy;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

final class AwakenedIchorianSigilGeometryRenderer {
	private static final int VESSEL_SEGMENTS = 5;

	private AwakenedIchorianSigilGeometryRenderer() {
	}

	static void render(AwakenedIchorianSigilPose pose, PoseStack stack,
			MultiBufferSource buffers, float time, int color, long seed) {
		float red = Math.min(0.72F, ((color >> 16) & 255) / 255.0F * 0.42F + 0.22F);
		float green = ((color >> 8) & 255) / 255.0F * 0.12F;
		float blue = (color & 255) / 255.0F * 0.15F;
		VertexConsumer glow = buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
		VertexConsumer core = buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);

		renderMembranes(core, stack.last().pose(), pose, red * 0.34F, green, blue, 0.20F);
		renderVessels(glow, stack.last().pose(), pose, pose.primaryVessels(),
				red, green, blue, 0.20F, time, seed);
		renderVessels(glow, stack.last().pose(), pose, pose.secondaryVessels(),
				red, green, blue, 0.18F, time, seed + 71);
		renderVessels(core, stack.last().pose(), pose, pose.primaryVessels(),
				red * 0.58F, green, blue, 0.88F, time, seed);
		renderVessels(core, stack.last().pose(), pose, pose.secondaryVessels(),
				red * 0.65F, green, blue, 0.78F, time, seed + 71);
		renderLandmarks(pose, stack, glow, core, time, seed, red, green, blue);
	}

	private static void renderLandmarks(AwakenedIchorianSigilPose pose, PoseStack stack,
			VertexConsumer glow, VertexConsumer core, float time, long seed,
			float red, float green, float blue) {
		for (AwakenedIchorianSigilPose.Landmark landmark : pose.landmarks()) {
			if (landmark.activation() <= 0.001F && pose.migration() > 0.001F) continue;
			stack.pushPose();
			Vec3 point = landmark.position();
			stack.translate(point.x, point.y, point.z);
			float radius = landmark.radius();
			if (landmark.role() == IchorianSigilAnatomy.Role.EYE) {
				stack.scale(0.82F, 0.82F, 1.15F);
				SanguineFormationProjectionRenderer.renderSphere(glow, stack.last().pose(),
						radius * 1.20F, time, seed, 0.32F, 0.0F, 0.01F, 0.24F);
				SanguineFormationProjectionRenderer.renderSphere(core, stack.last().pose(),
						radius, time, seed, 0.45F, 0.01F, 0.02F, 0.96F);
				stack.translate(0, 0, -radius * 0.72F);
				SanguineFormationProjectionRenderer.renderSphere(core, stack.last().pose(),
						radius * 0.38F, time, seed + 3, 0.015F, 0.0F, 0.0F, 1.0F);
			} else {
				float organ = landmark.role() == IchorianSigilAnatomy.Role.ORGAN ? 1.12F : 1.0F;
				SanguineFormationProjectionRenderer.renderSphere(glow, stack.last().pose(),
						radius * organ * 1.25F, time, seed + landmark.source(),
						red, green, blue, 0.16F);
				SanguineFormationProjectionRenderer.renderSphere(core, stack.last().pose(),
						radius * organ, time, seed + landmark.source(),
						landmark.role() == IchorianSigilAnatomy.Role.ORGAN ? red * 0.42F : red,
						green, blue, 0.88F);
			}
			stack.popPose();
		}
	}

	private static void renderVessels(VertexConsumer consumer, Matrix4f matrix,
			AwakenedIchorianSigilPose pose, List<AwakenedIchorianSigilPose.Vessel> vessels,
			float red, float green, float blue, float alpha, float time, long seed) {
		for (int vesselIndex = 0; vesselIndex < vessels.size(); vesselIndex++) {
			var vessel = vessels.get(vesselIndex);
			if (vessel.growth() <= 0.001F) continue;
			Vec3 start = pose.landmarks().get(vessel.from()).position();
			Vec3 end = pose.landmarks().get(vessel.to()).position();
			Vec3 previous = start;
			for (int step = 1; step <= VESSEL_SEGMENTS; step++) {
				double progress = step / (double) VESSEL_SEGMENTS;
				Vec3 next = start.lerp(end, progress);
				double envelope = Math.sin(Math.PI * progress);
				double writhe = Math.sin(time * 0.08D + progress * Math.PI * 3
						+ vesselIndex + seed * 0.001D) * envelope * 0.018D;
				next = next.add(writhe, writhe * 0.55D, -writhe * 0.4D);
				renderTubeSection(consumer, matrix, previous, next,
						vessel.thickness() * vessel.growth(), red, green, blue,
						alpha * vessel.growth());
				previous = next;
			}
		}
	}

	private static void renderTubeSection(VertexConsumer consumer, Matrix4f matrix,
			Vec3 start, Vec3 end, float radius,
			float red, float green, float blue, float alpha) {
		Vec3 direction = end.subtract(start);
		if (direction.lengthSqr() < 1.0E-8D) return;
		Vec3 side = direction.cross(new Vec3(0, 1, 0));
		if (side.lengthSqr() < 1.0E-8D) side = direction.cross(new Vec3(1, 0, 0));
		side = side.normalize().scale(radius);
		Vec3 vertical = direction.normalize().cross(side).normalize().scale(radius);
		quad(consumer, matrix, start.subtract(side), start.add(side),
				end.add(side), end.subtract(side), red, green, blue, alpha);
		quad(consumer, matrix, start.subtract(vertical), start.add(vertical),
				end.add(vertical), end.subtract(vertical), red, green, blue, alpha);
	}

	private static void renderMembranes(VertexConsumer consumer, Matrix4f matrix,
			AwakenedIchorianSigilPose pose, float red, float green, float blue, float alpha) {
		for (var membrane : pose.membranes()) {
			if (membrane.inflation() <= 0.001F) continue;
			Vec3 a = pose.landmarks().get(membrane.a()).position();
			Vec3 b = pose.landmarks().get(membrane.b()).position();
			Vec3 c = pose.landmarks().get(membrane.c()).position();
			quad(consumer, matrix, a, b, c, c,
					red, green, blue, alpha * membrane.inflation());
		}
	}

	private static void quad(VertexConsumer consumer, Matrix4f matrix,
			Vec3 a, Vec3 b, Vec3 c, Vec3 d,
			float red, float green, float blue, float alpha) {
		vertex(consumer, matrix, a, red, green, blue, alpha);
		vertex(consumer, matrix, b, red, green, blue, alpha);
		vertex(consumer, matrix, c, red, green, blue, alpha);
		vertex(consumer, matrix, d, red, green, blue, alpha);
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vec3 point,
			float red, float green, float blue, float alpha) {
		consumer.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
				.setColor(red, green, blue, alpha);
	}
}
