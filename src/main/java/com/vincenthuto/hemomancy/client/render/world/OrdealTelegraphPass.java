package com.vincenthuto.hemomancy.client.render.world;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealHazardKinds;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealHulkMoves;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealInterventionsDriver;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphFamily;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

import net.minecraft.world.phys.Vec3;

/**
 * A professor's interventions in a Crimson Troupe ordeal, painted bold (2026-11-17). The Saints' default painter keeps
 * the subtlety budget (hairline edges, faint fills) because a Saint's own pass carries the weight; an ordeal has no such
 * pass, and the fighter reads these from across the ring with a Hulk in their face. So each footprint is laid in thick
 * ribbons of the school materials: crimson fill, a bone-white rim, and the live edge in flame. Claimed by kind from
 * {@link SaintTelegraphRenderer}; the hits are {@code OrdealMoves}, and every shape here is drawn from the same synced
 * footprint, so a drawn edge is a hit edge.
 */
final class OrdealTelegraphPass {
	private static final double LIFT = 0.04D;
	/** Arc segments per this many degrees. */
	private static final double ARC_STEP = 5.0D;

	private OrdealTelegraphPass() {
	}

	/** True when the hazard is an ordeal footprint (drawn here, or nothing in the solid pass). */
	static boolean paint(HazardDraw draw, TelegraphCanvas canvas) {
		int kind = draw.hazard().kind();
		// Every professor's block of kinds (OrdealHazardKinds) is drawn here: by shape (a disc as a closing mark, a ring as
		// a smoulder then a wave, a lane as a bar), unless a kind below has a look of its own.
		if (!OrdealHazardKinds.isOrdeal(kind)) return false;
		if (canvas.solids()) return true;
		float fade = draw.fade() * HemoEasing.smoothstep(HemoEasing.clamp01(draw.age() / 4.0F));
		if (fade <= 0.0F) return true;
		Pen pen = new Pen(canvas, draw.frame().floorY() + LIFT + (draw.hazard().getId() % 8) * 0.002D, draw.hazard().getId() * 29);
		FootprintFrame frame = draw.frame();
		float age = draw.age();
		// The Retired Understudy's hunting spotlight, and the Ringmaster's burning one while it hunts: a pool of bone
		// light with a crimson rim, redrawn as it follows.
		boolean hunting = kind == com.vincenthuto.hemomancy.common.encounter.ordeal.UnderstudyChoreography.KIND_SPOT
				&& draw.life() <= com.vincenthuto.hemomancy.common.encounter.ordeal.UnderstudyChoreography.REDRAW + 1
				|| kind == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.KIND_SPOTLIGHT
				&& draw.life() <= com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.SPOTLIGHT_REDRAW + 1;
		if (hunting && draw.shape() instanceof BossFootprint.Disc spot) {
			pen.disc(TelegraphFamily.LUX, frame.origin(), spot.radius(), 0.55F * draw.fade());
			pen.ring(TelegraphFamily.ANIMUS, frame.origin(), spot.radius(), 0.16D, 0.9F * draw.fade());
			pen.ring(TelegraphFamily.LUX, frame.origin(), spot.radius() + 0.25D, 0.05D, 0.7F * draw.fade());
			return true;
		}
		// The Ringmaster's string of lights: a crimson run strung with bone bulbs while it snakes in, alight once it snaps;
		// and his spotlight's pool left scorched where it burned.
		if (kind == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.KIND_LIGHTS
				&& draw.shape() instanceof BossFootprint.Lane lane) {
			lights(pen, frame, lane, draw.life() > OrdealInterventionsDriver.BAR_REDRAW + 1, age, draw.life(), fade);
			return true;
		}
		if (kind == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.KIND_SPOTLIGHT
				&& draw.life() == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.SPOTLIGHT_SCORCH
				&& draw.shape() instanceof BossFootprint.Disc scorch) {
			burning(pen, frame.origin(), scorch.radius(), fade, canvas.time());
			return true;
		}
		// The Fire Breather's looks: the Bellows' breath as a burning wedge, and the cinder rain's pools left burning.
		if (kind == com.vincenthuto.hemomancy.common.encounter.ordeal.CinderBellowsOrdealMoves.KIND_BLAST
				&& draw.shape() instanceof BossFootprint.Cone cone) {
			breathCone(pen, frame, cone, age, fade);
			return true;
		}
		if (kind == com.vincenthuto.hemomancy.common.encounter.ordeal.FireEaterChoreography.KIND_POOL
				&& draw.shape() instanceof BossFootprint.Disc pool) {
			burning(pen, frame.origin(), pool.radius(), fade, canvas.time());
			return true;
		}
		// The Beast Tamer's hoop of fire, once lit: its inside burning.
		if (kind == com.vincenthuto.hemomancy.common.encounter.ordeal.BeastTamerMoves.KIND_HOOP
				&& draw.shape() instanceof BossFootprint.Disc hoop) {
			burning(pen, frame.origin(), hoop.radius(), fade, canvas.time());
			return true;
		}
		switch (draw.shape()) {
			case BossFootprint.Disc disc -> weight(pen, frame.origin(), disc.radius(), age, draw.life(), fade);
			case BossFootprint.Ring ring -> heave(pen, frame.origin(), ring, age, fade, canvas.time());
			case BossFootprint.Cone cone -> wedge(pen, frame, cone, draw.life() > OrdealInterventionsDriver.BAR_REDRAW + 1, age, draw.life(), fade);
			case BossFootprint.Lane lane -> {
				if (kind == OrdealInterventionsDriver.KIND_HOLD) hold(pen, frame, lane, fade, canvas.time());
				else if (kind == OrdealInterventionsDriver.KIND_CHARGE)
					bar(pen, frame, lane, age < OrdealHulkMoves.CHARGE_WINDUP, fade, age);
				else bar(pen, frame, lane, draw.life() > OrdealInterventionsDriver.BAR_REDRAW + 1, fade, age);
			}
			default -> {
			}
		}
		return true;
	}

	/**
	 * The Cinder Bellows' blast: through the inhale a crimson wedge smouldering faster toward the blast with bone edges;
	 * once loosed the wedge burns in flame with a white-hot spine.
	 */
	private static void breathCone(Pen pen, FootprintFrame frame, BossFootprint.Cone cone, float age, float fade) {
		double[] f = BossFootprint.Shapes.forward(frame);
		double base = Math.toDegrees(Math.atan2(f[1], f[0])), half = cone.degrees() / 2.0D;
		Vec3 centre = frame.origin();
		boolean loosed = age >= com.vincenthuto.hemomancy.common.encounter.ordeal.CinderBellowsOrdealMoves.BLAST_WINDUP;
		float beat = quickening(age, com.vincenthuto.hemomancy.common.encounter.ordeal.CinderBellowsOrdealMoves.BLAST_WINDUP);
		TelegraphFamily fill = loosed ? TelegraphFamily.FLAME : TelegraphFamily.ANIMUS;
		for (double r = 0.5D; r < cone.radius(); r += 0.5D)
			arc(pen, fill, centre, base, half, r, 0.26D, (loosed ? 0.9F : 0.35F + 0.35F * beat) * fade);
		arc(pen, TelegraphFamily.LUX, centre, base, half, cone.radius(), 0.08D, 0.9F * fade);
		for (double edge : new double[]{base - half, base + half}) {
			double a = Math.toRadians(edge);
			pen.band(TelegraphFamily.LUX, centre, centre.add(Math.cos(a) * cone.radius(), 0, Math.sin(a) * cone.radius()), 0.07D,
					0.9F * fade);
		}
		if (loosed) pen.band(TelegraphFamily.LUX, centre, centre.add(f[0] * cone.radius(), 0, f[1] * cone.radius()), 0.12D, fade);
	}

	/**
	 * A wedge struck in front of its owner (the Ringmaster's staff): through the wind-up a crimson fan filling toward the
	 * blow with bone edges; once struck, flame with a white-hot spine.
	 */
	private static void wedge(Pen pen, FootprintFrame frame, BossFootprint.Cone cone, boolean preview, float age, float life, float fade) {
		double[] f = BossFootprint.Shapes.forward(frame);
		double base = Math.toDegrees(Math.atan2(f[1], f[0])), half = cone.degrees() / 2.0D;
		Vec3 centre = frame.origin();
		float beat = preview ? quickening(age, life) : 1.0F;
		double filled = preview ? cone.radius() * (0.25D + 0.75D * HemoEasing.clamp01(age / Math.max(1.0F, life - 1.0F))) : cone.radius();
		TelegraphFamily fill = preview ? TelegraphFamily.ANIMUS : TelegraphFamily.FLAME;
		for (double r = 0.5D; r < cone.radius(); r += 0.5D)
			arc(pen, fill, centre, base, half, r, 0.26D, (r <= filled ? (preview ? 0.45F + 0.4F * beat : 0.9F) : 0.2F) * fade);
		arc(pen, TelegraphFamily.LUX, centre, base, half, cone.radius(), 0.09D, 0.95F * fade);
		for (double edge : new double[]{base - half, base + half}) {
			double a = Math.toRadians(edge);
			pen.band(TelegraphFamily.LUX, centre, centre.add(Math.cos(a) * cone.radius(), 0, Math.sin(a) * cone.radius()), 0.08D,
					0.9F * fade);
		}
		if (!preview) pen.band(TelegraphFamily.LUX, centre, centre.add(f[0] * cone.radius(), 0, f[1] * cone.radius()), 0.14D, fade);
	}

	/** A floor left burning where a cinder fell: flame flickering over the mark, a crimson halo round it. */
	private static void burning(Pen pen, Vec3 centre, double radius, float fade, double time) {
		float flicker = 0.75F + 0.25F * (float) Math.sin(time * 0.6D + centre.x * 3.1D + centre.z * 1.7D);
		pen.disc(TelegraphFamily.FLAME, centre, radius, 0.8F * flicker * fade);
		pen.ring(TelegraphFamily.ANIMUS, centre, radius + 0.2D, 0.14D, 0.7F * fade);
	}

	/** An arc of the floor ribbon at {@code radius}, {@code half} degrees either side of {@code base} (degrees from +x). */
	private static void arc(Pen pen, TelegraphFamily family, Vec3 centre, double base, double half, double radius, double halfWidth,
			float alpha) {
		int steps = Math.max(2, (int) Math.ceil(2.0D * half / ARC_STEP));
		for (int i = 0; i < steps; i++) {
			double a0 = Math.toRadians(base - half + 2.0D * half * i / steps), a1 = Math.toRadians(base - half + 2.0D * half * (i + 1) / steps);
			pen.band(family, centre.add(Math.cos(a0) * radius, 0, Math.sin(a0) * radius),
					centre.add(Math.cos(a1) * radius, 0, Math.sin(a1) * radius), halfWidth, alpha);
		}
	}

	/** The dropped weight: the mark filled in crimson, its rim in bone, and a flame ring closing on the rim as it falls. */
	private static void weight(Pen pen, Vec3 centre, double radius, float age, float life, float fade) {
		float t = HemoEasing.clamp01(age / Math.max(1.0F, life - 6.0F));
		pen.disc(TelegraphFamily.ANIMUS, centre, radius, (0.35F + 0.4F * t) * fade);
		pen.ring(TelegraphFamily.LUX, centre, radius, 0.12D, 0.95F * fade);
		pen.ring(TelegraphFamily.FLAME, centre, radius * (1.0D + 1.6D * (1.0D - t)), 0.16D + 0.1D * t, (0.45F + 0.55F * t) * fade);
		double arm = radius * 0.7D;
		pen.band(TelegraphFamily.FLAME, centre.add(-arm, 0, 0), centre.add(arm, 0, 0), 0.07D, 0.8F * fade);
		pen.band(TelegraphFamily.FLAME, centre.add(0, 0, -arm), centre.add(0, 0, arm), 0.07D, 0.8F * fade);
	}

	/** The heave: through the wind-up the rim smoulders; then a flame wave runs in across the ring. */
	private static void heave(Pen pen, Vec3 centre, BossFootprint.Ring ring, float age, float fade, double time) {
		if (ring.from() == ring.to()) {
			float beat = quickening(age, ring.travelTicks());
			pen.ring(TelegraphFamily.ANIMUS, centre, ring.from(), ring.band() * 0.5D, (0.55F + 0.3F * beat) * fade);
			pen.ring(TelegraphFamily.LUX, centre, ring.from() - ring.band() * 0.5D, 0.06D, 0.9F * fade);
			pen.ring(TelegraphFamily.LUX, centre, ring.from() + ring.band() * 0.5D, 0.06D, 0.9F * fade);
			return;
		}
		double radius = ring.radius(age, 0);
		if (radius < 0.0D) return;
		pen.ring(TelegraphFamily.ANIMUS, centre, radius + ring.band() * 1.4D, ring.band() * 0.6D, 0.4F * fade);
		pen.ring(TelegraphFamily.FLAME, centre, radius, ring.band() * 0.5D, 0.95F * fade);
		pen.ring(TelegraphFamily.LUX, centre, radius, 0.1D, fade);
	}

	/** The bar: across the wind-up it smoulders where it will start; once swung it burns where it stands. */
	private static void bar(Pen pen, FootprintFrame frame, BossFootprint.Lane lane, boolean preview, float fade, float age) {
		Vec3 from = frame.origin(), to = along(frame, lane.length());
		if (preview) {
			float beat = quickening(age, OrdealInterventionsDriver.WINDUP);
			pen.band(TelegraphFamily.ANIMUS, from, to, lane.halfWidth(), (0.5F + 0.3F * beat) * fade);
			pen.edges(TelegraphFamily.LUX, from, to, lane.halfWidth(), 0.06D, 0.85F * fade);
			return;
		}
		pen.band(TelegraphFamily.FLAME, from, to, lane.halfWidth(), 0.95F * fade);
		pen.band(TelegraphFamily.LUX, from, to, 0.12D, fade);
	}

	/** A run of the string of lights: bulbs in bone along a crimson run, brightening toward the snap; then all flame. */
	private static void lights(Pen pen, FootprintFrame frame, BossFootprint.Lane lane, boolean preview, float age, float life, float fade) {
		Vec3 from = frame.origin(), to = along(frame, lane.length());
		if (!preview) {
			pen.band(TelegraphFamily.FLAME, from, to, lane.halfWidth(), 0.95F * fade);
			pen.band(TelegraphFamily.LUX, from, to, 0.14D, fade);
			return;
		}
		float beat = quickening(age, life);
		pen.band(TelegraphFamily.ANIMUS, from, to, lane.halfWidth(), (0.6F + 0.3F * beat) * fade);
		pen.edges(TelegraphFamily.LUX, from, to, lane.halfWidth(), 0.05D, 0.8F * fade);
		pen.band(TelegraphFamily.ANIMUS, from, to, 0.08D, fade);
		for (double d = 0.35D; d < lane.length(); d += 0.7D) {
			Vec3 bulb = along(frame, d);
			pen.ring(TelegraphFamily.LUX, bulb, 0.12D, 0.12D, (0.5F + 0.5F * beat) * fade);
		}
	}

	/** Holding the line: the floor tilting toward the puppet, a lane of chevrons crawling to him. */
	private static void hold(Pen pen, FootprintFrame frame, BossFootprint.Lane lane, float fade, double time) {
		Vec3 from = frame.origin(), to = along(frame, lane.length());
		pen.band(TelegraphFamily.ANIMUS, from, to, lane.halfWidth(), 0.4F * fade);
		pen.edges(TelegraphFamily.LUX, from, to, lane.halfWidth(), 0.07D, 0.85F * fade);
		double[] f = BossFootprint.Shapes.forward(frame);
		double spacing = 1.6D, drift = (time * 0.09D) % spacing;
		for (double d = lane.length() - drift; d > 1.0D; d -= spacing) {
			Vec3 tip = along(frame, d - 0.9D);
			Vec3 left = along(frame, d).add(-f[1] * lane.halfWidth() * 0.8D, 0, f[0] * lane.halfWidth() * 0.8D);
			Vec3 right = along(frame, d).add(f[1] * lane.halfWidth() * 0.8D, 0, -f[0] * lane.halfWidth() * 0.8D);
			float a = (float) Math.min(1.0D, d / 2.0D) * 0.9F * fade;
			pen.band(TelegraphFamily.FLAME, left, tip, 0.11D, a);
			pen.band(TelegraphFamily.FLAME, right, tip, 0.11D, a);
		}
	}

	/**
	 * A wind-up's pulse, quickening and brightening toward the blow like a held breath: slow throbs at first, a flutter
	 * at the end, its floor rising so the last ticks burn.
	 */
	private static float quickening(float age, float windup) {
		float t = HemoEasing.clamp01(age / Math.max(1.0F, windup));
		float beat = 0.5F + 0.5F * (float) Math.sin(age * (0.25D + 1.4D * t * t));
		return Math.min(1.0F, beat * (1.0F - t) + t);
	}

	private static Vec3 along(FootprintFrame frame, double distance) {
		double[] f = BossFootprint.Shapes.forward(frame);
		return frame.origin().add(f[0] * distance, 0.0D, f[1] * distance);
	}

	/** Ribbons on the floor plane in the canvas's school materials, with no subtlety cap. */
	private record Pen(TelegraphCanvas canvas, double y, int seed) {
		void band(TelegraphFamily family, Vec3 from, Vec3 to, double halfWidth, float alpha) {
			VertexConsumer v = canvas.material(family);
			double[] side = TelegraphCanvas.side(from, to);
			if (v == null || side == null || alpha <= 0.0F) return;
			Vec3 c = canvas.camera();
			double sx = side[0] * halfWidth, sz = side[1] * halfWidth, ry = y - c.y;
			double ax = from.x - c.x, az = from.z - c.z, bx = to.x - c.x, bz = to.z - c.z;
			Matrix4f pose = canvas.poses().last().pose();
			MaterialQuads.quad(v, pose, ax - sx, ry, az - sz, bx - sx, ry, bz - sz, bx + sx, ry, bz + sz, ax + sx, ry, az + sz,
					LuxUmbraGeometry.RIBBON, MaterialQuads.seedTile(seed), 0xFFFFFF, Math.min(1.0F, alpha), 0.0D, 1.0D, false);
		}

		/** The two long sides of a band, as rims. */
		void edges(TelegraphFamily family, Vec3 from, Vec3 to, double halfWidth, double rim, float alpha) {
			double[] side = TelegraphCanvas.side(from, to);
			if (side == null) return;
			Vec3 s = new Vec3(side[0] * halfWidth, 0.0D, side[1] * halfWidth);
			band(family, from.add(s), to.add(s), rim, alpha);
			band(family, from.subtract(s), to.subtract(s), rim, alpha);
		}

		void ring(TelegraphFamily family, Vec3 centre, double radius, double halfWidth, float alpha) {
			if (radius <= 1.0E-3D) return;
			int steps = (int) Math.ceil(360.0D / ARC_STEP);
			for (int i = 0; i < steps; i++) {
				double a0 = Math.toRadians(i * ARC_STEP), a1 = Math.toRadians((i + 1) * ARC_STEP);
				band(family, centre.add(Math.cos(a0) * radius, 0, Math.sin(a0) * radius),
						centre.add(Math.cos(a1) * radius, 0, Math.sin(a1) * radius), halfWidth, alpha);
			}
		}

		/** A filled disc as close-laid rings. */
		void disc(TelegraphFamily family, Vec3 centre, double radius, float alpha) {
			for (double r = 0.25D; r < radius; r += 0.5D) ring(family, centre, r, 0.26D, alpha);
		}
	}
}
