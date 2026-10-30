package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.TendrilTubeBrush;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * The possession scenes' floor tendrils: a ring of thin hyphae rising out of the ground around a body, each a cubic
 * curve from a buried root over to a target on or above the body, forking into side branches that fork again into
 * twigs. A branch grows out once its parent has grown past the fork. Growth draws the curve from the root out, sinking
 * lowers the whole tendril into the floor (the ground hides what goes under), and a travelling pulse in the amber
 * shell shows what flows along it. Centre lines are in pixels from the body's feet, as {@link TendrilTubeBrush} takes
 * them.
 */
final class PossessionTendrils {
	static final int STATIONS = 14;
	private static final int BRANCH_STATIONS = 8;
	private static final int TWIG_STATIONS = 5;
	/** Side branches per main strand, and twigs per branch. */
	private static final int BRANCHES = 3;
	private static final int TWIGS = 1;
	private static final float PIXELS = 16.0F;

	/** One tendril's centre line (pixels), its root and tip radii (pixels), shell alpha and pulse (0 to 1, or -1). */
	record Strand(float[] line, float root, float tip, float glow, float pulse, long seed) {
		int stations() {
			return line.length / 3;
		}

		/** A ring strand rather than a branch or twig. */
		boolean main() {
			return stations() == STATIONS;
		}

		/** A station of the line, in blocks from the feet. */
		float x(int station) {
			return line[station * 3] / PIXELS;
		}

		float y(int station) {
			return line[station * 3 + 1] / PIXELS;
		}

		float z(int station) {
			return line[station * 3 + 2] / PIXELS;
		}
	}

	/**
	 * The shape of a ring: {@code count} tendrils rooted about {@code radius} out, their first control point
	 * {@code rise} up, reaching a target {@code reach} high and {@code closeIn} from the axis, coming down onto it from
	 * {@code arch} above. Sizes in blocks, thickness in pixels.
	 */
	record Ring(int count, float radius, float rise, float reach, float closeIn, float arch, float writhe, float root,
			float tip) {
	}

	private PossessionTendrils() {
	}

	/** Adds a ring's tendrils, drawn {@code growth} of the way out and lowered {@code sink} blocks. */
	static void ring(List<Strand> out, Ring ring, long seed, float growth, float sink, float glow, float pulse,
			float time) {
		if (growth < 0.02F) return;
		for (int i = 0; i < ring.count(); i++) {
			long hash = seed * 0x9E3779B97F4A7C15L + (i + 1) * 0xBF58476D1CE4E5B9L;
			float jitter = ((hash >>> 8) & 255) / 255.0F;
			float jitter2 = ((hash >>> 20) & 255) / 255.0F;
			float azimuth = (i + 0.35F * (jitter - 0.5F)) * Mth.TWO_PI / ring.count() + (seed & 63) * 0.1F;
			float cos = Mth.cos(azimuth);
			float sin = Mth.sin(azimuth);
			float r = ring.radius() * (0.85F + 0.3F * jitter2);
			float reach = ring.reach() * (0.85F + 0.3F * jitter);
			float rise = Math.max(ring.rise(), 0.05F);
			float mid = ring.closeIn() + (r - ring.closeIn()) * 0.45F;
			float[] line = new float[STATIONS * 3];
			for (int k = 0; k < STATIONS; k++) {
				float u = growth * k / (STATIONS - 1.0F);
				float a = 1.0F - u;
				float b0 = a * a * a, b1 = 3.0F * a * a * u, b2 = 3.0F * a * u * u, b3 = u * u * u;
				float along = b0 * r + b1 * r * 1.15F + b2 * mid + b3 * ring.closeIn();
				float y = b0 * -0.15F + b1 * rise + b2 * (reach + ring.arch()) + b3 * reach;
				// The writhe swings round the ring, quiet at the root and the grip.
				float writhe = ring.writhe() * Mth.sin(time * 0.35F + u * 7.0F + jitter * 6.0F) * Mth.sin(u * Mth.PI);
				line[k * 3] = (cos * along - sin * writhe) * PIXELS;
				line[k * 3 + 1] = (y - sink) * PIXELS;
				line[k * 3 + 2] = (sin * along + cos * writhe) * PIXELS;
			}
			out.add(new Strand(line, ring.root(), ring.tip(), glow, pulse, hash));
			float reachLength = (ring.radius() + reach) * 0.42F;
			branches(out, line, STATIONS, growth, ring.root(), ring.tip(), reachLength, cos, sin, glow, pulse, hash,
					time, BRANCHES, BRANCH_STATIONS, true);
		}
	}

	/**
	 * Forks {@code count} branches off a strand's centre line: each leaves at a seeded station, heads outward, up and to
	 * one side, and curls; it grows once {@code growth} has passed its fork. With {@code twigs}, each branch forks once
	 * more.
	 */
	private static void branches(List<Strand> out, float[] parent, int parentStations, float growth, float root,
			float tip, float length, float outX, float outZ, float glow, float pulse, long seed, float time, int count,
			int stations, boolean twigs) {
		for (int b = 0; b < count; b++) {
			long hash = seed * 0x94D049BB133111EBL + (b + 1) * 0x2545F4914F6CDD1DL;
			float j1 = ((hash >>> 12) & 255) / 255.0F;
			float j2 = ((hash >>> 24) & 255) / 255.0F;
			float j3 = ((hash >>> 36) & 255) / 255.0F;
			// Forks spread along the middle of the parent, the first ones lower.
			float at = 0.22F + (b + 0.3F + 0.4F * j1) / count * 0.6F;
			float own = (growth - at) / (1.0F - at);
			if (own <= 0.03F) continue;
			own = Math.min(1.0F, own * 1.6F);
			int k = Math.min(parentStations - 2, Math.max(1, Math.round(at * (parentStations - 1))));
			float px = parent[k * 3] / PIXELS, py = parent[k * 3 + 1] / PIXELS, pz = parent[k * 3 + 2] / PIXELS;
			float tx = parent[(k + 1) * 3] / PIXELS - parent[(k - 1) * 3] / PIXELS;
			float ty = parent[(k + 1) * 3 + 1] / PIXELS - parent[(k - 1) * 3 + 1] / PIXELS;
			float tz = parent[(k + 1) * 3 + 2] / PIXELS - parent[(k - 1) * 3 + 2] / PIXELS;
			float tl = Mth.sqrt(tx * tx + ty * ty + tz * tz) + 1.0E-4F;
			float side = (b & 1) == 0 ? 1.0F : -1.0F;
			// Along the parent a little, out and to its side, lifting.
			float dx = tx / tl * 0.45F + outX * 0.55F - outZ * side * (0.5F + 0.4F * j2);
			float dy = ty / tl * 0.3F + 0.35F + 0.3F * j3;
			float dz = tz / tl * 0.45F + outZ * 0.55F + outX * side * (0.5F + 0.4F * j2);
			float dl = Mth.sqrt(dx * dx + dy * dy + dz * dz) + 1.0E-4F;
			dx /= dl;
			dy /= dl;
			dz /= dl;
			float len = length * (0.6F + 0.5F * j1);
			float[] line = new float[stations * 3];
			for (int s = 0; s < stations; s++) {
				float u = own * s / (stations - 1.0F);
				// A curl that bends the branch back toward the side it left from, with a slow writhe.
				float curl = u * u * 0.35F * len;
				float writhe = 0.05F * Mth.sin(time * 0.4F + u * 6.0F + j2 * 6.0F) * u;
				line[s * 3] = (px + dx * len * u + outZ * side * curl + writhe) * PIXELS;
				line[s * 3 + 1] = (py + dy * len * u - curl * 0.6F) * PIXELS;
				line[s * 3 + 2] = (pz + dz * len * u - outX * side * curl + writhe) * PIXELS;
			}
			float fork = Mth.lerp(at, root, tip);
			float branchRoot = Math.max(0.35F, fork * 0.7F);
			float branchTip = Math.max(0.12F, tip * 0.4F);
			out.add(new Strand(line, branchRoot, branchTip, glow * 0.8F, pulse, hash));
			if (twigs) {
				branches(out, line, stations, own, branchRoot, branchTip, len * 0.5F, dx, dz, glow * 0.7F, pulse, hash,
						time, TWIGS, TWIG_STATIONS, false);
			}
		}
	}

	/** The lit hyphae core; dark tissue, the texture running its length. */
	static void core(PoseStack.Pose pose, VertexConsumer out, Strand strand, int light) {
		float[] radii = TendrilTubeBrush.taper(strand.stations(), strand.root(), strand.tip(), 2.5F, strand.seed());
		TendrilTubeBrush.core(pose, out, strand.line(), radii, (strand.seed() & 7) * 0.2F, light,
				OverlayTexture.NO_OVERLAY, 0.82F);
	}

	/** The amber shell, brighter where the pulse is. */
	static void glow(PoseStack.Pose pose, VertexConsumer out, Strand strand, float red, float green, float blue) {
		if (strand.glow() <= 0.003F) return;
		int stations = strand.stations();
		float[] radii = TendrilTubeBrush.taper(stations, strand.root(), strand.tip(), 2.5F, strand.seed());
		float[] alpha = new float[stations];
		for (int k = 0; k < stations; k++) {
			float s = k / (stations - 1.0F);
			float pulse = strand.pulse() < 0.0F ? 0.0F : Math.max(0.0F, 1.0F - Math.abs(s - strand.pulse()) * 5.0F);
			alpha[k] = Math.min(1.0F, strand.glow() * (1.0F - 0.4F * s + 0.9F * pulse));
		}
		TendrilTubeBrush.glow(pose, out, strand.line(), radii, 1.5F, red, green, blue, alpha);
	}

	/** A soft additive disc lying flat {@code y} above the feet, {@code size} blocks across. */
	static void floorHalo(PoseStack poseStack, VertexConsumer out, float y, float size, float red, float green,
			float blue, float alpha) {
		if (alpha <= 0.002F || size <= 0.01F) return;
		poseStack.pushPose();
		poseStack.translate(0.0F, y, 0.0F);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		TendrilTubeBrush.halo(poseStack.last(), out, 0.0F, 0.0F, 0.0F, size * PIXELS, size * PIXELS, red, green, blue,
				Math.min(1.0F, alpha));
		poseStack.popPose();
	}
}
