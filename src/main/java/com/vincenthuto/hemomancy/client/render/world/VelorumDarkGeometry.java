package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostGeometry.Field;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostGeometry.Ground;
import com.vincenthuto.hemomancy.client.render.world.VelorumTelegraphPass.Scene;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Shapes;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDevoteeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Velorum's liquid dark as layered geometry (revision 2026-11-06, design R.2 and R.11): darkness thinned to a liquid,
 * a flat glossy near-black surface with a slow violet-black sheen, its edges torn into broad tatters of smoke that lift
 * off the rim, fray upward and tear away, and slow gloom above it. It is drawn in the pass's DARK phase, before every
 * telegraph, so nothing he strikes with is ever hidden by it. His lantern's light holds a radius the dark cannot hold
 * ({@link Light}): pools are drawn torn back at its edge, and an awake ally's warm blood carves a hole. Also here: the
 * folds' and the shapes' smoke, the Eclipse shapes' reaching limbs (bold, few: tapering 0.6 to 0.25 in four straight
 * segments, never noodles), the gloam cuts and the Eclipse's red seam grid. No contour lines, no webs of thin strands.
 */
final class VelorumDarkGeometry {
	/** Liquid dark's base and its smoke, for the school flows' tint. */
	static final int INK = 0xFFFFFF;

	private VelorumDarkGeometry() {
	}

	/** The light that pushes the dark back this frame: his lantern's radius at its floor point, and the awake allies. */
	record Light(float lantern, Vec3 foot, List<VelorumDevoteeEntity> allies, float partial) {
		static final Light NONE = new Light(0.0F, Vec3.ZERO, List.of(), 0.0F);

		static Light of(VelorumEntity boss, float partial) {
			return new Light(VelorumDarkSight.lantern(boss, partial), VelorumDarkSight.lanternFoot(boss, partial),
					VelorumDarkSight.allies(boss.level(), boss.position(), 40.0D), partial);
		}

		/** How far the dark is pushed back at a floor point, 0 none to 1 wholly. */
		float push(double x, double z) {
			float lit = VelorumVisualRules.lit(Math.hypot(x - foot.x, z - foot.z), lantern);
			for (VelorumDevoteeEntity ally : allies) {
				Vec3 at = ally.getPosition(partial);
				lit = Math.max(lit, VelorumVisualRules.lit(Math.hypot(x - at.x, z - at.z), VelorumDarkSight.WARMTH - 0.6D));
			}
			return lit;
		}
	}

	// ── pools ────────────────────────────────────────────────────────

	/**
	 * One pool hazard of liquid dark: its surface over its shape (a growing disc, a strip along a lane or a road, the
	 * whole box in the Eclipse), torn at its rim and wherever the light holds it back; tatters of smoke lifting off its
	 * rim; slow gloom over a disc's middle.
	 */
	static void pool(Scene s, BossGroundHazardEntity hazard, VelorumEntity boss, Light light, int seed) {
		float age = AnimationTime.age(hazard, s.partial());
		float fade = SaintTelegraphTiming.hazardFade(age, hazard.life(), hazard.fade(), hazard.fading(), hazard.fadeStart());
		if (fade <= 0.0F) return;
		FootprintFrame frame = hazard.frame();
		BossFootprint shape = hazard.shape();
		if (shape instanceof BossFootprint.Disc disc) {
			Vec3 centre = frame.anchor(disc.at());
			double r = VelorumDarkSight.radius(hazard, boss, s.partial());
			disc(s, centre, frame.floorY(), r, fade, light, seed);
			return;
		}
		if (shape instanceof BossFootprint.Region region) {
			AABB box = region.area() == BossFootprint.Area.CHAMBER ? frame.chamber() : frame.bossFloor();
			Vec3 middle = box.getCenter();
			Ground g = new Ground(middle.x, middle.z, 1.0D, 0.0D, frame.floorY(), s.camera());
			Field field = (along, right) -> {
				double x = g.wx(along, right), z = g.wz(along, right);
				return VelorumDarkSight.cover(hazard, boss, x, z, s.partial()) * (1.0F - light.push(x, z));
			};
			VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, box.minX - middle.x, box.maxX - middle.x,
					box.minZ - middle.z, box.maxZ - middle.z, Field.of(1.0F), 0.0F, 0.0F, field, seed);
			return;
		}
		if (shape instanceof BossFootprint.Lane lane) {
			Ground base = Ground.of(frame.origin(), Shapes.forward(frame), frame.floorY(), s.camera());
			for (int n = 0; n < lane.lanes(); n++) {
				int index = lane.index(n);
				Ground g = base.lane(lane.offset(index), lane.angle(index));
				strip(s, hazard, boss, g, 0.0D, lane.length(), lane.halfWidth(), light, seed + index * 7);
			}
			return;
		}
		if (shape instanceof BossFootprint.Path path) {
			List<Vec3> points = frame.points();
			for (int i = 0; i + 1 < points.size(); i++) {
				Vec3 a = points.get(i), b = points.get(i + 1);
				double dx = b.x - a.x, dz = b.z - a.z, length = Math.hypot(dx, dz);
				if (length < 0.05D) continue;
				Ground g = new Ground(a.x, a.z, dx / length, dz / length, frame.floorY(), s.camera());
				strip(s, hazard, boss, g, -path.halfWidth(), length + path.halfWidth(), path.halfWidth(), light, seed + i * 7);
			}
		}
	}

	/** A strip of dark along a ground from {@code a0} to {@code a1}, {@code half} wide either side, read from the hazard. */
	private static void strip(Scene s, BossGroundHazardEntity hazard, VelorumEntity boss, Ground g, double a0, double a1, double half,
			Light light, int seed) {
		Field field = (along, right) -> {
			double x = g.wx(along, right), z = g.wz(along, right);
			return VelorumDarkSight.cover(hazard, boss, x, z, s.partial()) * (1.0F - light.push(x, z));
		};
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, a0, a1, -half - 0.6D, half + 0.6D, Field.of(1.0F), 0.0F,
				0.0F, field, seed);
	}

	/** A disc of liquid dark at {@code centre} of radius {@code r}: the surface, its torn rim's tatters, gloom above. */
	static void disc(Scene s, Vec3 centre, double floorY, double r, float strength, Light light, int seed) {
		if (r < 0.15D || strength <= 0.0F) return;
		Ground g = new Ground(centre.x, centre.z, 1.0D, 0.0D, floorY, s.camera());
		Field field = (along, right) -> {
			float cover = VelorumVisualRules.poolCover(Math.hypot(along, right), r);
			return cover <= 0.0F ? 0.0F : cover * strength * (1.0F - light.push(g.wx(along, right), g.wz(along, right)));
		};
		VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, 0.0D, r + 0.25D, -180.0D, 180.0D, Field.of(1.0F), 0.0F,
				0.0F, field, seed);
		int tatters = VelorumVisualRules.rimTatters(r);
		for (int i = 0; i < tatters; i++) {
			double a = i * Math.PI * 2.0D / tatters + VelorumFrostRules.unit(seed, i) * 0.4D;
			double phase = (s.time() / VelorumVisualRules.TATTER + VelorumFrostRules.unit(seed, 100 + i)) % 1.0D;
			double out = r * 0.9D + 0.35D * phase;
			double x = centre.x + Math.cos(a) * out, z = centre.z + Math.sin(a) * out;
			float held = 1.0F - light.push(x, z);
			if (held < 0.2F) continue;
			double[] t = VelorumVisualRules.tatter(phase);
			tatter(s, new Vec3(x, floorY, z), 0.32D + 0.25D * VelorumFrostRules.unit(seed, 200 + i), t[0], (float) t[1],
					0.75F * strength * held * (float) Math.sin(phase * Math.PI), seed + 300 + i);
		}
		if (r > 1.8D) {
			for (int k = 0; k < 3; k++) {
				double a = k * 2.1D + seed;
				Vec3 at = new Vec3(centre.x + Math.cos(a) * r * 0.35D, floorY, centre.z + Math.sin(a) * r * 0.35D);
				if (light.push(at.x, at.z) > 0.6F) continue;
				double drift = (s.time() * 0.006D + k / 3.0D) % 1.0D;
				tatter(s, at, r * 0.45D, 1.6D + 0.8D * drift, (float) (0.3D + 0.5D * drift), 0.22F * strength * (float) Math.sin(drift * Math.PI),
						seed + 400 + k);
			}
		}
	}

	/** A torn tatter of smoke standing at a world foot, turned to the camera: broad at its foot, fraying as it lifts. */
	static void tatter(Scene s, Vec3 foot, double width, double height, float torn, float alpha, int seed) {
		if (alpha <= 0.004F || height < 0.05D) return;
		VelorumFrostGeometry.upright(s.p(), s.frost(), s.local(foot), width, height, VelorumFrostGeometry.SMOKE,
				VelorumFrostGeometry.state(0.35F, torn, 0.0F), alpha, seed);
	}

	// ── the Eclipse ──────────────────────────────────────────────────

	/**
	 * The Eclipse's red seam grid: in the dark the floor's own block seams fill with dull glowing cruor across the whole
	 * boss floor, so the floor stays readable. Drawn after the dark, so it shows through it.
	 */
	static void grid(Scene s, AABB floor, double y, float glow, int seed) {
		if (glow <= 0.01F) return;
		Vec3 middle = floor.getCenter();
		Ground g = new Ground(middle.x, middle.z, 1.0D, 0.0D, y, s.camera());
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.SEAMS, floor.minX - middle.x, floor.maxX - middle.x,
				floor.minZ - middle.z, floor.maxZ - middle.z, Field.of(1.0F), 1.0F, glow, Field.of(0.6F + 0.4F * glow), seed);
	}

	/**
	 * A reaching limb of torn smoke from a shape to {@code to}: four straight segments, tapering from 0.6 at the shoulder
	 * to 0.25 at the wrist, down off the shape and out along the floor, torn smoke cards along it; the cruor-red hand at
	 * its end is the pass's (in the solid phase). {@code reach} 0 to 1 is how far it has thrown.
	 */
	static List<Vec3> limb(Scene s, Vec3 shoulder, Vec3 to, double floorY, float reach, float alpha, int seed) {
		Vec3 flat = new Vec3(to.x - shoulder.x, 0.0D, to.z - shoulder.z);
		double length = flat.length();
		if (length < 0.2D || reach <= 0.01F || alpha <= 0.004F) return List.of();
		Vec3 dir = flat.scale(1.0D / length);
		double out = length * reach;
		List<Vec3> joints = new ArrayList<>(5);
		joints.add(shoulder);
		joints.add(new Vec3(shoulder.x + dir.x * Math.min(0.7D, out), shoulder.y - 0.5D, shoulder.z + dir.z * Math.min(0.7D, out)));
		joints.add(new Vec3(shoulder.x + dir.x * Math.min(1.5D, out), floorY + 0.35D, shoulder.z + dir.z * Math.min(1.5D, out)));
		double sway = (VelorumFrostRules.unit(seed, 1) - 0.5D) * 0.35D;
		Vec3 across = new Vec3(-dir.z, 0.0D, dir.x);
		joints.add(new Vec3(shoulder.x + dir.x * out * 0.7D + across.x * sway, floorY + 0.22D, shoulder.z + dir.z * out * 0.7D + across.z * sway));
		joints.add(new Vec3(shoulder.x + dir.x * out, floorY + 0.16D, shoulder.z + dir.z * out));
		if (!s.floor()) return joints;
		List<Vec3> local = new ArrayList<>(5);
		double[] widths = new double[5];
		for (int i = 0; i < 5; i++) {
			local.add(s.local(joints.get(i)));
			widths[i] = HemoEasing.lerp(0.3D, 0.125D, i / 4.0D);
		}
		LuxUmbraGeometry.ribbonPath(s.p(), s.shade(), local, Vec3.ZERO, widths, LuxUmbraGeometry.WISP, seed, INK, 0.85F * alpha);
		for (int i = 1; i < 5; i++) {
			Vec3 mid = joints.get(i - 1).lerp(joints.get(i), 0.5D);
			double phase = (s.time() / 18.0D + VelorumFrostRules.unit(seed, 10 + i)) % 1.0D;
			tatter(s, new Vec3(mid.x, Math.max(floorY, mid.y - 0.3D), mid.z), widths[i] * 1.3D, 0.5D + 0.6D * phase, (float) phase,
					0.6F * alpha * (float) Math.sin(phase * Math.PI), seed + 20 + i);
		}
		return joints;
	}

	// ── gloam cuts ───────────────────────────────────────────────────

	/** A gloam cut's leading edge: frost light, the only blue on it. */
	private static final int EDGE = 0xCFE6FA;

	/**
	 * One gloam cut flying along its lane's ground, its leading edge {@code front} blocks out: a crescent of liquid dark
	 * with a torn trailing edge and a cold hairline at its front. A low cut lies close along the floor with a knee-high
	 * smoke crest (you jump it); the high cut stands at face height (you stoop under it) and lays its shadow on the floor.
	 */
	static void cut(Scene s, Ground g, double front, double half, boolean high, float alpha, float glint, int seed) {
		if (alpha <= 0.004F || front <= 0.05D) return;
		double back = Math.max(0.0D, front - 2.2D);
		int state = VelorumFrostGeometry.state(0.0F, 0.0F, glint);
		if (!high) {
			double y = g.y() + 0.32D;
			VelorumFrostGeometry.card(s.p(), s.frost(), s.local(g.wx(back, -half), y, g.wz(back, -half)), s.local(g.wx(back, half), y, g.wz(back, half)),
					s.local(g.wx(front, half), y + 0.12D, g.wz(front, half)), s.local(g.wx(front, -half), y + 0.12D, g.wz(front, -half)),
					VelorumFrostGeometry.CUT, state, alpha, seed);
			// polish round 2: from the boss camera a low cut was dark on a dark floor. It now lays its own shadow of
			// liquid dark on the floor under it, carries a bold frost-light leading edge, and a fuller smoke crest
			shadow(s, g, back, front, half, 0.6F * alpha, seed + 2);
			edge(s, g, front, half, y + 0.12D, alpha, seed + 3);
			for (int k = -3; k <= 3; k++) {
				double r = k * half * 0.3D;
				double bow = front - 0.3D * (1.0D - (r / half) * (r / half));
				tatter(s, new Vec3(g.wx(bow, r), g.y(), g.wz(bow, r)), half * 0.32D, 1.1D, 0.25F, 0.7F * alpha, seed + 10 + k);
			}
			return;
		}
		double low = g.y() + 1.55D, top = g.y() + 2.6D;
		VelorumFrostGeometry.card(s.p(), s.frost(), s.local(g.wx(back, -half), low + 0.45D, g.wz(back, -half)),
				s.local(g.wx(back, half), low + 0.45D, g.wz(back, half)), s.local(g.wx(front, half), low + 0.55D, g.wz(front, half)),
				s.local(g.wx(front, -half), low + 0.55D, g.wz(front, -half)), VelorumFrostGeometry.CUT, state, alpha, seed);
		// the cut's face: a curtain of torn dark from its low edge to its top, so it reads at a challenger's eye
		VelorumFrostGeometry.card(s.p(), s.frost(), s.local(g.wx(front - 0.1D, -half), low, g.wz(front - 0.1D, -half)),
				s.local(g.wx(front - 0.1D, half), low, g.wz(front - 0.1D, half)), s.local(g.wx(front, half), top, g.wz(front, half)),
				s.local(g.wx(front, -half), top, g.wz(front, -half)), VelorumFrostGeometry.SMOKE, VelorumFrostGeometry.state(0.5F, 0.2F, 0.0F),
				0.8F * alpha, seed + 1);
		// its shadow lying on the floor under it, and a frost-light edge along its lower lip and its top
		shadow(s, g, back, front, half, 0.55F * alpha, seed + 2);
		edge(s, g, front, half, low + 0.55D, alpha, seed + 3);
		edge(s, g, front, half, top, 0.6F * alpha, seed + 4);
	}

	/** A cut's shadow of liquid dark on the floor, the crescent's own shape, so a cut reads from above. */
	private static void shadow(Scene s, Ground g, double back, double front, double half, float alpha, int seed) {
		double y = g.y() + 0.03D;
		VelorumFrostGeometry.card(s.p(), s.frost(), s.local(g.wx(back, -half), y, g.wz(back, -half)),
				s.local(g.wx(back, half), y, g.wz(back, half)), s.local(g.wx(front, half), y, g.wz(front, half)),
				s.local(g.wx(front, -half), y, g.wz(front, -half)), VelorumFrostGeometry.CUT, VelorumFrostGeometry.state(0.0F, 0.0F, 0.0F),
				alpha, seed);
	}

	/** A cut's leading edge as a bold band of frost light bowed along its crescent, at height {@code y}. */
	private static void edge(Scene s, Ground g, double front, double half, double y, float alpha, int seed) {
		if (!s.floor() || alpha <= 0.004F) return;
		List<Vec3> points = new ArrayList<>(7);
		for (int k = -3; k <= 3; k++) {
			double r = k * half / 3.0D;
			double bow = front - 0.45D * (r / half) * (r / half);
			points.add(s.local(g.wx(bow, r), y, g.wz(bow, r)));
		}
		LuxUmbraGeometry.ribbonPath(s.p(), s.rime(), points, Vec3.ZERO, 0.16D, 0.07D, LuxUmbraGeometry.RIBBON, seed, EDGE, alpha);
	}
}
