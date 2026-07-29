package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.IchorianSigilLandmarkRenderer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilPose;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilOrganicGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRenderPalette;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilLandmarkGeometry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

final class AwakenedIchorianSigilGeometryRenderer {
	private static final int VESSEL_SEGMENTS = 5;

	private AwakenedIchorianSigilGeometryRenderer() {
	}

	static void render(AwakenedIchorianSigilPose pose, PoseStack stack,
			MultiBufferSource buffers, float time, int color, long seed) {
		renderGlowPass(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW),
				pose, stack, time, seed,
				IchorianSigilRenderPalette.vessel(true), color);
		renderCorePass(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE),
				pose, stack, time, seed,
				IchorianSigilRenderPalette.vessel(false), color);
	}

	private static void renderGlowPass(VertexConsumer glow, AwakenedIchorianSigilPose pose,
			PoseStack stack, float time, long seed,
			IchorianSigilRenderPalette.Color vesselColor,
			int authoredColor) {
		renderVessels(glow, stack.last().pose(), pose, pose.primaryVessels(),
				vesselColor.red(), vesselColor.green(), vesselColor.blue(), 0.20F, time, seed);
		renderVessels(glow, stack.last().pose(), pose, pose.secondaryVessels(),
				vesselColor.red(), vesselColor.green(), vesselColor.blue(), 0.18F, time, seed + 71);
		renderLandmarks(pose, stack, glow, time, seed, authoredColor, true);
	}

	private static void renderCorePass(VertexConsumer core, AwakenedIchorianSigilPose pose,
			PoseStack stack, float time, long seed,
			IchorianSigilRenderPalette.Color vesselColor,
			int authoredColor) {
		IchorianSigilRenderPalette.Color membrane = IchorianSigilRenderPalette.membrane();
		renderMembranes(core, stack.last().pose(), pose,
				membrane.red(), membrane.green(), membrane.blue(), 0.14F);
		renderVessels(core, stack.last().pose(), pose, pose.primaryVessels(),
				vesselColor.red(), vesselColor.green(), vesselColor.blue(), 0.88F, time, seed);
		renderVessels(core, stack.last().pose(), pose, pose.secondaryVessels(),
				vesselColor.red(), vesselColor.green(), vesselColor.blue(), 0.78F, time, seed + 71);
		renderLandmarks(pose, stack, core, time, seed, authoredColor, false);
	}

	private static void renderLandmarks(AwakenedIchorianSigilPose pose, PoseStack stack,
			VertexConsumer consumer, float time, long seed, int authoredColor, boolean glow) {
		for (AwakenedIchorianSigilPose.Landmark landmark : pose.landmarks()) {
			stack.pushPose();
			Vec3 point = landmark.position();
			stack.translate(point.x, point.y, point.z);
			IchorianSigilLandmarkRenderer.render(consumer, stack,
					IchorianSigilLandmarkGeometry.forRole(landmark.role(),
							seed + landmark.source()),
					landmark.radius(), authoredColor, time,
					seed + landmark.source(), 1.0F, glow, false);
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
			float radius = vessel.thickness() * vessel.growth();
			List<IchorianSigilOrganicGeometry.Sample> samples =
					new ArrayList<>(VESSEL_SEGMENTS + 1);
			for (int step = 0; step <= VESSEL_SEGMENTS; step++) {
				double progress = step / (double) VESSEL_SEGMENTS;
				Vec3 next = start.lerp(end, progress);
				double envelope = Math.sin(Math.PI * progress);
				double writhe = Math.sin(time * 0.08D + progress * Math.PI * 3
						+ vesselIndex + seed * 0.001D) * envelope * 0.018D;
				next = next.add(writhe, writhe * 0.55D, -writhe * 0.4D);
				float bolus = IchorianSigilOrganicGeometry.bolusIntensity(
						(float) progress,
						IchorianSigilOrganicGeometry.bolusPosition(time, seed + vesselIndex));
				samples.add(new IchorianSigilOrganicGeometry.Sample(
						next.x, next.y, next.z,
						IchorianSigilOrganicGeometry.vesselWidth(radius, (float) progress,
								IchorianSigilOrganicGeometry.heartbeat(time), bolus),
						0.72F + bolus * 0.28F));
			}
			List<IchorianSigilOrganicGeometry.TubeFrame> frames =
					IchorianSigilOrganicGeometry.tubeFrames(samples);
			for (int step = 1; step < frames.size(); step++) {
				float intensity = (samples.get(step - 1).redIntensity()
						+ samples.get(step).redIntensity()) * 0.5F;
				renderTubeSection(consumer, matrix, frames.get(step - 1), frames.get(step),
						Math.min(1.0F, red * intensity), green, blue,
						alpha * vessel.growth());
			}
		}
	}

	private static void renderTubeSection(VertexConsumer consumer, Matrix4f matrix,
			IchorianSigilOrganicGeometry.TubeFrame start,
			IchorianSigilOrganicGeometry.TubeFrame end,
			float red, float green, float blue, float alpha) {
		Vec3 startCenter = new Vec3(start.centerX(), start.centerY(), start.centerZ());
		Vec3 endCenter = new Vec3(end.centerX(), end.centerY(), end.centerZ());
		Vec3 startSide = new Vec3(start.sideX(), start.sideY(), start.sideZ());
		Vec3 endSide = new Vec3(end.sideX(), end.sideY(), end.sideZ());
		Vec3 startVertical = new Vec3(start.verticalX(), start.verticalY(), start.verticalZ());
		Vec3 endVertical = new Vec3(end.verticalX(), end.verticalY(), end.verticalZ());
		quad(consumer, matrix, startCenter.subtract(startSide), startCenter.add(startSide),
				endCenter.add(endSide), endCenter.subtract(endSide), red, green, blue, alpha);
		quad(consumer, matrix, startCenter.subtract(startVertical), startCenter.add(startVertical),
				endCenter.add(endVertical), endCenter.subtract(endVertical),
				red, green, blue, alpha);
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
