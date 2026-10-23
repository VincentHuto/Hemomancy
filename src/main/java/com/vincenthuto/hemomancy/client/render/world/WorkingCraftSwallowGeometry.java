package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The Working floor's tentacles climbing a craft's structure, drawn to the Hematic
 * Riposte standard on the blood school's material: a translucent sheath, three
 * opaque sinew strands braided round a writhing spine and tapering in three
 * segments, a hot pale core, a clotted hooked tip gripping the structure's edge
 * at the fill line, and a dark clot where each one leaves the floor. They beat
 * with the floor, and tighten and darken once the daemon possesses the structure.
 */
final class WorkingCraftSwallowGeometry {
	private static final int TINT = 0xFFFFFF;
	private static final int CORE = 0xFFD2C4;
	private static final int CLOT = 0x7A1020;
	private static final int STRANDS = 3;

	private WorkingCraftSwallowGeometry() {
	}

	static void collect(PoseStack poses, LuxUmbraBatch batch, Vec3 camera, float partialTick) {
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		List<WorkingCraftSwallowRenderer.View> views = WorkingCraftSwallowRenderer.views(partialTick);
		if (views.isEmpty()) return;
		double time = AnimationTime.gameTime(level, partialTick);
		float heartbeat = CardinalRiteFloorVisualRules.heartbeat((float) time);
		VertexConsumer vertices = batch.vertices(LuxUmbraBatch.Material.ANIMUS);
		for (WorkingCraftSwallowRenderer.View view : views) {
			if (view.fill() <= 0.0F || view.alpha() <= 0.0F) continue;
			Vec3 origin = new Vec3(view.minX(), view.bottomY() - view.sink(), view.minZ());
			if (origin.distanceToSqr(camera) > 96.0D * 96.0D) continue;
			poses.pushPose();
			poses.translate(origin.x - camera.x, origin.y - camera.y, origin.z - camera.z);
			Vec3 localCamera = camera.subtract(origin);
			double width = view.maxX() - view.minX() + 1;
			double depth = view.maxZ() - view.minZ() + 1;
			double top = view.height() * Math.max(0.0F, Math.min(1.0F, view.fill()));
			float possession = view.possession();
			float swell = 1.0F + 0.2F * heartbeat + 0.45F * view.sealPulse() + 0.25F * possession;
			int seed = (int) Math.floorMod(view.center().asLong(), 7919L);
			int sinewColor = WorkingCraftSwallowRules.possessionTint(TINT, possession * 0.75F);
			double[][] corners = {{0, 0}, {width, 0}, {width, depth}, {0, depth}};
			for (int c = 0; c < corners.length; c++) {
				double cx = corners[c][0];
				double cz = corners[c][1];
				List<Vec3> spine = WorkingCraftSwallowRules.tentacleSpine(cx, cz, cx == 0 ? -1 : 1, cz == 0 ? -1 : 1,
						top, time, seed + c * 13, possession);
				float flicker = 0.9F + 0.1F * (float) Math.sin(time * 0.9D + c * 1.7D + seed);
				float alpha = view.alpha() * flicker;
				tentacle(poses, vertices, localCamera, spine, swell, time, seed + c * 101, sinewColor, alpha);
				LuxUmbraGeometry.ground(poses, vertices, spine.getFirst().multiply(1, 0, 1).add(0, 0.03D, 0),
						0.34D * swell, LuxUmbraGeometry.POOL, seed + c + 61, CLOT, view.alpha() * 0.75F);
			}
			// The membrane swells into a lip at the structure's foot as it starts to climb.
			LuxUmbraGeometry.ground(poses, vertices, new Vec3(width / 2.0D, 0.03D, depth / 2.0D),
					Math.max(width, depth) / 2.0D + 0.45D, LuxUmbraGeometry.POOL, seed + 97, CLOT,
					view.alpha() * (0.35F + 0.3F * possession) * Math.min(1.0F, view.fill() * 4.0F));
			poses.popPose();
		}
	}

	private static void tentacle(PoseStack poses, VertexConsumer vertices, Vec3 camera, List<Vec3> spine, float swell,
			double time, int seed, int sinewColor, float alpha) {
		// 1. Sheath: a broad translucent ribbon so the tentacle reads as wet flesh, not wire.
		LuxUmbraGeometry.ribbonPath(poses, vertices, spine, camera, 0.15D * swell, 0.06D * swell,
				LuxUmbraGeometry.RIBBON, seed, TINT, alpha * 0.3F);
		// 2. Sinew: three opaque strands braided round the spine, tapering root to tip in three segments.
		int third = Math.max(1, (spine.size() - 1) / 3);
		for (int strand = 0; strand < STRANDS; strand++) {
			List<Vec3> braid = braid(spine, strand, time, swell);
			LuxUmbraGeometry.ribbonPath(poses, vertices, braid.subList(0, third + 1), camera,
					0.07D * swell, 0.055D * swell, LuxUmbraGeometry.RIBBON, seed + strand, sinewColor, alpha);
			LuxUmbraGeometry.ribbonPath(poses, vertices, braid.subList(third, 2 * third + 1), camera,
					0.055D * swell, 0.04D * swell, LuxUmbraGeometry.RIBBON, seed + strand + 3, sinewColor, alpha);
			LuxUmbraGeometry.ribbonPath(poses, vertices, braid.subList(2 * third, braid.size()), camera,
					0.04D * swell, 0.02D * swell, LuxUmbraGeometry.RIBBON, seed + strand + 6, sinewColor, alpha);
		}
		// 3. Core: a thin hot line down the middle.
		LuxUmbraGeometry.ribbonPath(poses, vertices, spine, camera, 0.022D * swell, 0.01D * swell,
				LuxUmbraGeometry.RIBBON, seed + 31, CORE, alpha * 0.6F);
		// 4. Hooked tip: a dark clot gripping the structure's edge.
		LuxUmbraGeometry.ribbonPath(poses, vertices, spine.subList(spine.size() - 3, spine.size()), camera,
				0.06D * swell, 0.012D * swell, LuxUmbraGeometry.SLASH, seed + 41, CLOT, alpha);
	}

	/** One sinew strand twisting round the spine, its radius thinning toward the tip. */
	private static List<Vec3> braid(List<Vec3> spine, int strand, double time, float swell) {
		List<Vec3> points = new ArrayList<>(spine.size());
		for (int i = 0; i < spine.size(); i++) {
			Vec3 at = spine.get(i);
			Vec3 tangent = spine.get(Math.min(i + 1, spine.size() - 1)).subtract(spine.get(Math.max(0, i - 1)));
			if (tangent.lengthSqr() < 1.0E-8D) tangent = new Vec3(0, 1, 0);
			tangent = tangent.normalize();
			Vec3 normal = Math.abs(tangent.y) > 0.9D ? new Vec3(1, 0, 0).cross(tangent) : new Vec3(0, 1, 0).cross(tangent);
			normal = normal.normalize();
			Vec3 binormal = tangent.cross(normal);
			double t = i / (spine.size() - 1.0D);
			double angle = t * 9.0D + time * 0.25D + strand * (Math.PI * 2.0D / STRANDS);
			double radius = 0.045D * swell * (1.0D - 0.55D * t);
			points.add(at.add(normal.scale(Math.cos(angle) * radius)).add(binormal.scale(Math.sin(angle) * radius)));
		}
		return points;
	}
}
