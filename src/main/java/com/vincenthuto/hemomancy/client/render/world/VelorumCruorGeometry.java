package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.world.VelorumFrostGeometry.Field;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostGeometry.Ground;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Shard;
import com.vincenthuto.hemomancy.client.render.world.VelorumTelegraphPass.Scene;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Velorum's Frozen Cruor where it leaves its source (revision 2026-11-06, design R.2 and R.11): nothing of his freezes
 * out of empty air. Here are the floor's seams welling with old blood and setting into cruor, the stains of blood the
 * challengers spilled, the Cruor Javelins (borrowed from {@code CruorJavelinGeometry}: frost strands freezing together
 * round a spine of clots, flying point-first, frost fingers where they strike, a rime collar where they lodge), the
 * frozen veins running from the congregation, the Arrested Pulse's spines bursting out of a body, the Draw of Silence's
 * stream of a challenger's own blood, and a shape's cruor-red hand. The solids are his ICE material (the clots live in
 * its shader); the strands, fingers and powder are the school's CRUOR flow; the floor work is his frost material.
 */
final class VelorumCruorGeometry {
	/** A javelin's radius (polish round 2: 0.075 was a hairline at range) and a vein's ridge half-width. */
	static final double SHAFT_GIRTH = 0.15D, VEIN_RIDGE = 0.3D;
	private static final int WHITE = 0xFFFFFF;

	private VelorumCruorGeometry() {
	}

	// ── the source: the floor's seams and the spilled blood ──────────

	/**
	 * The floor's block seams over an annular sector of a ground, old blood welling into them as {@code well} runs (a
	 * field, so a front can run across it), setting into cruor under a frost lip as {@code set} rises.
	 */
	static void seams(Scene s, Ground g, double r0, double r1, double from, double to, Field well, float set, float alpha, int seed) {
		if (!s.floor() || alpha <= 0.004F) return;
		VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.SEAMS, Math.max(0.0D, r0), r1, from, to, well, set, 0.0F,
				Field.of(alpha), seed);
	}

	/** The same over a rectangle of a ground. */
	static void seamsRect(Scene s, Ground g, double a0, double a1, double b0, double b1, Field well, float set, float glow, float alpha,
			int seed) {
		if (!s.floor() || alpha <= 0.004F) return;
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.SEAMS, a0, a1, b0, b1, well, set, glow, Field.of(alpha), seed);
	}

	/** How far blood has welled into a move's seams through its wind-up: run by 65% of the reveal, as the frost does. */
	static float welled(float build) {
		return HemoEasing.smoothstep(HemoEasing.clamp01(build / 0.65F));
	}

	/** A stain of a challenger's spilled blood, soaked {@code depth} deep, beading up by {@code bead}, ringed by {@code ring}. */
	static void stain(Scene s, Vec3 at, float depth, float bead, float ring, float alpha, int seed) {
		if (!s.floor() || alpha <= 0.004F) return;
		double radius = VelorumVisualRules.stainRadius(depth);
		VelorumFrostGeometry.flat(s.p(), s.frost(), s.local(at), radius, VelorumFrostRules.unit(seed, 1) * Math.PI, VelorumFrostGeometry.STAIN,
				VelorumFrostGeometry.state(depth, bead, ring), alpha, seed);
	}

	// ── javelins ─────────────────────────────────────────────────────

	/**
	 * A javelin of frozen blood from {@code tail} to {@code head} (world points), {@code formed} 0 to 1 frozen together
	 * and {@code shatter} 0 to 1 broken: a faceted cruor shaft round its spine of clots (the solid phase), three frost
	 * strands twisting about it and a frost point (the flows), frost powder shed behind it while {@code flying}.
	 */
	static void javelin(Scene s, Vec3 tail, Vec3 head, float formed, float shatter, boolean flying, int seed) {
		Vec3 line = head.subtract(tail);
		double length = line.length();
		if (length < 0.05D || formed <= 0.02F || shatter >= 1.0F) return;
		Vec3 dir = line.scale(1.0D / length);
		float whole = 1.0F - shatter;
		if (s.solid()) {
			double shown = length * formed;
			// polish round 2: a shaft read as a hairline at the boss camera's range; it is now a javelin's girth
			VelorumIceGeometry.shard(s.p(), s.ice(), s.local(head.subtract(dir.scale(shown))), dir, shown, SHAFT_GIRTH, 0.9D, seed * 0.7D, 5, whole,
					VelorumIceGeometry.withGlint(VelorumIceGeometry.CRUOR_SHAFT, flying ? 0.35F : 0.0F), VelorumIceGeometry.SHARD, seed);
			return;
		}
		if (!s.floor()) return;
		Vec3[] basis = basis(dir);
		Vec3 start = head.subtract(dir.scale(length * formed));
		for (int strand = 0; strand < 3; strand++) {
			List<Vec3> points = new ArrayList<>(9);
			for (int i = 0; i <= 8; i++) {
				double t = i / 8.0D;
				double angle = t * 4.0D + strand * Math.PI * 2.0D / 3.0D + s.time() * 0.05D;
				double radius = SHAFT_GIRTH * 1.25D * (1.0D - t * 0.6D) * (1.0D + shatter * 2.0D);
				points.add(s.local(start.lerp(head, t).add(basis[0].scale(Math.cos(angle) * radius)).add(basis[1].scale(Math.sin(angle) * radius))));
			}
			LuxUmbraGeometry.ribbonPath(s.p(), s.cruor(), points, Vec3.ZERO, 0.07D, 0.015D, ThermalGeometry.FROST_STRAND, seed + strand, WHITE,
					0.9F * whole);
		}
		LuxUmbraGeometry.ribbonPath(s.p(), s.cruor(), List.of(s.local(head), s.local(head.add(dir.scale(0.36D)))), Vec3.ZERO, 0.07D, 0.004D,
				ThermalGeometry.FROST_STRAND, seed + 5, WHITE, whole);
		if (flying) {
			for (int k = 1; k <= 6; k++) {
				ThermalGeometry.powder(s.p(), s.cruor(), s.local(start.subtract(dir.scale(k * 0.3D))), s.right(), s.up(), 0.2D + k * 0.04D,
						seed + 30 + k, 0.65F * (1.0F - k / 7.0F));
			}
		}
	}

	/** Frost fingers racing across a body or the floor round a wound at {@code at}, out of {@code dir}: seven, {@code reach} long. */
	static void fingers(Scene s, Vec3 at, Vec3 dir, double reach, float alpha, int seed) {
		if (!s.floor() || alpha <= 0.004F || reach <= 0.02D) return;
		Vec3 axis = dir.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 1.0D, 0.0D) : dir.normalize();
		Vec3[] basis = basis(axis);
		for (int finger = 0; finger < 7; finger++) {
			double angle = finger * Math.PI * 2.0D / 7.0D + seed;
			Vec3 out = basis[0].scale(Math.cos(angle)).add(basis[1].scale(Math.sin(angle)));
			List<Vec3> path = new ArrayList<>(4);
			for (int i = 0; i <= 3; i++) {
				double t = i / 3.0D;
				path.add(s.local(at.add(out.scale(reach * t)).add(out.cross(axis).scale(Math.sin(t * 3.0D + finger) * 0.06D * t))));
			}
			LuxUmbraGeometry.ribbonPath(s.p(), s.cruor(), path, Vec3.ZERO, 0.06D, 0.008D, ThermalGeometry.FROST_STRAND, seed + 40 + finger, WHITE,
					0.85F * alpha);
		}
		ThermalGeometry.powder(s.p(), s.cruor(), s.local(at), s.right(), s.up(), 0.3D * reach + 0.1D, seed + 50, 0.6F * alpha);
	}

	/** A lodged javelin's rime collar on the floor: frost crusting where its point went in. */
	static void collar(Scene s, Vec3 foot, float formed, float whole, int seed) {
		if (!s.solid() || formed <= 0.02F) return;
		VelorumIceGeometry.collar(s.p(), s.ice(), s.local(foot), 0.2D, 0.13D * formed, whole, VelorumIceGeometry.HOARFROST,
				VelorumIceGeometry.SHARD, seed);
	}

	/** Cruor chips flung where a shaft shatters on rime or a cover's face. */
	static void shards(Scene s, Vec3 at, float age, int seed) {
		if (!s.solid()) return;
		VelorumIceGeometry.chips(s.p(), s.ice(), s.local(at), age, 1.1D, 7, VelorumIceGeometry.CRUOR_SPLINTER, seed);
	}

	// ── the body's own blood ─────────────────────────────────────────

	/**
	 * Cruor spines bursting out of a body's own blood: five to seven, out of its chest and limbs, 0.9 long at full,
	 * leaning out and a little up, with frost fingers across the body where they broke out.
	 */
	static void spines(Scene s, Vec3 feet, double height, double width, float grown, float frost, int seed) {
		if (grown <= 0.01F) return;
		int count = 5 + (int) (VelorumFrostRules.unit(seed, 1) * 3.0D);
		for (int i = 0; i < count; i++) {
			double a = i * Math.PI * 2.0D / count + VelorumFrostRules.unit(seed, 10 + i) * 0.6D;
			double y = height * (0.25D + 0.55D * VelorumFrostRules.unit(seed, 20 + i));
			Vec3 out = new Vec3(Math.cos(a), 0.4D + 0.6D * VelorumFrostRules.unit(seed, 30 + i), Math.sin(a)).normalize();
			// polish round 3: the spines read as needles; they now break out from inside the body with a blade's girth
			Vec3 root = feet.add(Math.cos(a) * width * 0.15D, y, Math.sin(a) * width * 0.15D);
			if (s.solid()) {
				VelorumIceGeometry.shard(s.p(), s.ice(), s.local(root), out, 1.05D * grown * (0.75D + 0.4D * VelorumFrostRules.unit(seed, 40 + i)),
						0.14D, 0.7D, a, 5, 1.0F, VelorumIceGeometry.withGlint(VelorumIceGeometry.CRUOR_SPINE, (1.0F - grown) * 0.6F),
						VelorumIceGeometry.SHARD, seed + i * 7);
			} else if (s.floor() && i % 2 == 0) {
				fingers(s, root, out, 0.35D * grown, frost * grown, seed + 60 + i);
			}
		}
	}

	/** The points of cruor pushing out of a marked body's silhouette before a beat: short, growing with {@code tell}. */
	static void points(Scene s, Vec3 feet, double height, double width, float tell, int seed) {
		if (!s.solid() || tell <= 0.02F) return;
		for (int i = 0; i < 6; i++) {
			double a = i * Math.PI / 3.0D + VelorumFrostRules.unit(seed, i);
			double y = height * (0.3D + 0.5D * VelorumFrostRules.unit(seed, 10 + i));
			Vec3 out = new Vec3(Math.cos(a), 0.2D, Math.sin(a)).normalize();
			VelorumIceGeometry.shard(s.p(), s.ice(), s.local(feet.add(Math.cos(a) * width * 0.4D, y, Math.sin(a) * width * 0.4D)), out,
					0.36D * tell, 0.075D, 0.8D, a, 4, 1.0F, VelorumIceGeometry.withGlint(VelorumIceGeometry.CRUOR_SPINE, tell * 0.5F),
					VelorumIceGeometry.SHARD, seed + i * 5);
		}
	}

	/**
	 * The Draw of Silence's stream: a challenger's own blood drawn out of them, a dark-red ribbon body with a frost skin
	 * (the flows), beads of cruor setting as they travel in (the solid phase), gathering into a knot at his fist sized by
	 * how much has been drawn. The inactive's stream is drawn pale: frost alone.
	 */
	static void stream(Scene s, Vec3 from, Vec3 to, float strength, float drawn, boolean blood, int seed) {
		int n = 12;
		List<Vec3> world = new ArrayList<>(n + 1);
		for (int i = 0; i <= n; i++) {
			double t = i / (double) n;
			double sway = Math.sin(t * Math.PI) * Math.sin(s.time() * 0.03D + t * 4.0D + seed) * 0.08D;
			world.add(from.lerp(to, t).add(0.0D, -Math.sin(t * Math.PI) * 0.35D + sway, 0.0D));
		}
		if (s.floor()) {
			List<Vec3> path = new ArrayList<>(n + 1);
			double[] body = new double[n + 1], skin = new double[n + 1];
			for (int i = 0; i <= n; i++) {
				double t = i / (double) n;
				path.add(s.local(world.get(i)));
				body[i] = 0.06D + 0.12D * (1.0D - t) * (1.0D - t);
				skin[i] = body[i] * 1.35D;
			}
			if (blood) {
				LuxUmbraGeometry.ribbonPath(s.p(), s.cruor(), path, Vec3.ZERO, body, ThermalGeometry.FROST_STRAND, seed, WHITE, 0.95F * strength);
			}
			LuxUmbraGeometry.ribbonPath(s.p(), s.rime(), path, Vec3.ZERO, skin, LuxUmbraGeometry.RIBBON, seed + 1, WHITE,
					(blood ? 0.3F : 0.6F) * strength);
			return;
		}
		if (!s.solid() || !blood) return;
		for (int k = 0; k < 5; k++) {
			double run = (s.time() * 0.02D + k / 5.0D) % 1.0D;
			int i = (int) Math.min(n - 1, Math.floor(run * n));
			Vec3 bead = world.get(i).lerp(world.get(i + 1), run * n - i);
			VelorumIceGeometry.chip(s.p(), s.ice(), s.local(bead), (0.05D + 0.04D * run) * strength, s.time() * 0.1D + k,
					VelorumIceGeometry.CRUOR_SHAFT, seed + 10 + k);
		}
		double knot = (0.08D + 0.14D * HemoEasing.clamp01(drawn)) * strength;
		VelorumIceGeometry.chip(s.p(), s.ice(), s.local(to), knot, s.time() * 0.03D, VelorumIceGeometry.CRUOR_SHAFT, seed + 20);
	}

	/** A frozen vein along the floor's seams, run {@code shown} blocks: the ridge and its bright running bead. */
	static void vein(Scene s, double[] xz, double y, double shown, float frost, float alpha, int seed) {
		if (s.floor()) {
			VelorumFrostGeometry.strip(s.p(), s.frost(), y, xz, shown, VEIN_RIDGE, VelorumFrostGeometry.RIDGE, frost, 0.0F, 0.0F, 0.6D, 1.0F, alpha,
					s.camera(), seed);
			return;
		}
		if (!s.solid() || shown <= 0.05D) return;
		// polish round 2: a vein read as a line on the floor; it now has body, a crest of cruor knuckles heaved up along
		// the seam behind its running front, low enough to read as the floor's own blood and not a wall
		double length = VelorumFrostGeometry.length(xz);
		int k = 0;
		for (double d = 0.3D; d < Math.min(shown, length) - 0.15D; d += 0.45D, k++) {
			double[] at = VelorumFrostGeometry.along(xz, d), next = VelorumFrostGeometry.along(xz, Math.min(length, d + 0.3D));
			double ax = next[0] - at[0], az = next[1] - at[1], lean = Math.hypot(ax, az) < 1.0E-6D ? 0.0D : 0.35D / Math.hypot(ax, az);
			double high = (0.18D + 0.12D * VelorumFrostRules.unit(seed, 40 + k)) * Math.min(1.0D, (shown - d) / 0.9D) * alpha;
			VelorumIceGeometry.shard(s.p(), s.ice(), s.local(at[0], y - 0.04D, at[1]), new Vec3(ax * lean, 1.0D, az * lean), high, 0.15D,
					0.6D, Math.atan2(az, ax), 4, 1.0F, VelorumIceGeometry.CRUOR_BLADE, VelorumIceGeometry.SHARD, seed + 60 + k * 5);
		}
		if (shown >= length - 0.05D) return;
		double[] head = VelorumFrostGeometry.along(xz, shown);
		VelorumIceGeometry.chip(s.p(), s.ice(), s.local(head[0], y + 0.14D, head[1]), 0.19D, s.time() * 0.05D,
				VelorumIceGeometry.withGlint(VelorumIceGeometry.CRUOR_SPINE, 0.8F), seed + 3);
	}

	/** Spines erupting along a vein: a short cluster every 1.3 blocks, 1.2 to 1.6 tall, crumbling from {@code crumbleAt}. */
	static void erupt(Scene s, double[] xz, double y, float age, float crumbleAt, int seed) {
		if (!s.solid() || age <= 0.0F) return;
		double length = VelorumFrostGeometry.length(xz);
		int k = 0;
		for (double d = 0.8D; d < length - 0.2D; d += 1.3D, k++) {
			double[] at = VelorumFrostGeometry.along(xz, d);
			int site = seed + k * 13;
			List<Shard> cluster = VelorumFrostRules.cluster(site, 1.2D + 0.4D * VelorumFrostRules.unit(site, 1), 3, 0.6D, 5);
			VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(at[0], y, at[1]), cluster, age - k * 0.15F, crumbleAt, VelorumIceGeometry.CRUOR_SPINE,
					VelorumIceGeometry.SHARD, k % 2 == 0, site);
		}
	}

	/**
	 * A cruor-red hand forming at the end of a shape's limb: a palm and four splayed fingers of cruor, pointing on along
	 * {@code dir}, {@code formed} 0 to 1. Solid phase only.
	 */
	static void hand(Scene s, Vec3 at, Vec3 dir, float formed, int seed) {
		if (!s.solid() || formed <= 0.02F) return;
		Vec3 flat = new Vec3(dir.x, 0.0D, dir.z);
		Vec3 forward = flat.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : flat.normalize();
		Vec3 across = new Vec3(-forward.z, 0.0D, forward.x);
		VelorumIceGeometry.chip(s.p(), s.ice(), s.local(at), 0.16D * formed, seed * 0.3D, VelorumIceGeometry.CRUOR_BLADE, seed);
		for (int i = 0; i < 4; i++) {
			double spread = (i - 1.5D) * 0.32D;
			Vec3 finger = forward.add(across.scale(spread)).add(0.0D, -0.15D, 0.0D).normalize();
			VelorumIceGeometry.shard(s.p(), s.ice(), s.local(at.add(across.scale(spread * 0.18D))), finger, 0.36D * formed, 0.035D, 0.8D,
					spread, 4, 1.0F, VelorumIceGeometry.CRUOR_SPINE, VelorumIceGeometry.SHARD, seed + i * 3);
		}
	}

	private static Vec3[] basis(Vec3 axis) {
		Vec3 reference = Math.abs(axis.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
		Vec3 first = axis.cross(reference).normalize();
		return new Vec3[]{first, first.cross(axis).normalize()};
	}
}
