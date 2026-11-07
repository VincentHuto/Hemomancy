package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Shapes;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphFamily;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The default telegraph painter: every {@link BossFootprint} drawn from the same motion methods its hit test reads
 * ({@code Lane.front}, {@code Sweep.angle}, {@code Ring.radius}, {@code Annulus.arcAngle}, {@code Path.walker},
 * {@code Union.live}), so a drawn edge is a hit edge. Through the wind-up the whole footprint shows as a faint wash
 * that firms toward the commit; once committed, the live part is lit and the rest stays faint. Fills are a flat tint at
 * or under {@link TelegraphCanvas#MAX_FILL_ALPHA}; edges are hairline material ribbons; nothing stands up off the floor.
 */
public final class SaintTelegraphPainter {
	/** Edge half-width; the canvas caps it at {@link TelegraphCanvas#MAX_EDGE_WIDTH}. */
	public static final double EDGE = 0.012D;
	private static final float PREVIEW_FILL = 0.07F;
	private static final float LIVE_FILL = 0.2F;
	private static final float PREVIEW_EDGE = 0.32F;
	private static final float LIVE_EDGE = 0.62F;
	/** A region larger than this many cells a side is drawn at its edges only. */
	private static final int REGION_LIMIT = 48;

	/** How one footprint is drawn: families, strengths and a seed. */
	public record Look(TelegraphFamily fill, TelegraphFamily edge, float fillAlpha, float edgeAlpha, float previewFill,
			float previewEdge, int seed) {
		public Look scaled(float factor) {
			return new Look(fill, edge, fillAlpha * factor, edgeAlpha * factor, previewFill * factor,
					previewEdge * factor, seed);
		}
	}

	private SaintTelegraphPainter() {
	}

	/** A move in the default look; the families are the spec's, else the Saint's. */
	public static boolean paint(TelegraphDraw draw, TelegraphCanvas canvas, TelegraphFamily fill, TelegraphFamily edge) {
		if (canvas.solids()) return true;
		float firm = 0.45F + 0.55F * HemoEasing.smoothstep(HemoEasing.clamp01(draw.reveal()));
		float flash = draw.commitFlash();
		Look look = new Look(fill, edge, LIVE_FILL, Math.min(1.0F, LIVE_EDGE + 0.35F * flash), PREVIEW_FILL * firm,
				Math.min(1.0F, PREVIEW_EDGE * firm + 0.4F * flash), draw.saint().getId() * 31 + draw.move().id());
		footprint(canvas, draw.footprint(), draw.frame(), draw.age(), look);
		return true;
	}

	/** A hazard in the default look: its shape lit as standing, eased in and faded out. */
	public static boolean paintHazard(HazardDraw draw, TelegraphCanvas canvas, TelegraphFamily fill, TelegraphFamily edge) {
		if (canvas.solids()) return true;
		float strength = draw.fade() * HemoEasing.smoothstep(HemoEasing.clamp01(draw.age() / 6.0F));
		Look look = new Look(fill, edge, 0.14F, 0.42F, 0.05F, 0.22F, draw.hazard().getId() * 17).scaled(strength);
		footprint(canvas, draw.shape(), draw.frame(), draw.age(), look);
		return true;
	}

	/** Draws {@code shape} at {@code age} (ticks since the commit; negative shows the whole footprint as a preview). */
	public static void footprint(TelegraphCanvas canvas, BossFootprint shape, FootprintFrame frame, double age, Look look) {
		double y = canvas.ground(frame.floorY());
		double[] forward = Shapes.forward(frame);
		boolean live = age >= 0.0D;
		switch (shape) {
			case BossFootprint.Lane lane -> {
				for (int n = 0; n < lane.lanes(); n++) {
					int index = lane.index(n);
					double[] dir = Shapes.rotate(forward, lane.angle(index));
					Vec3 root = offset(frame.origin(), dir, lane.offset(index));
					double front = live ? lane.front(age, index) : -1.0D;
					if (front < 0.0D) strip(canvas, root, dir, 0.0D, lane.length(), lane.halfWidth(), y, look, false, n);
					else {
						strip(canvas, root, dir, 0.0D, front, lane.halfWidth(), y, look, true, n);
						if (front < lane.length()) strip(canvas, root, dir, front, lane.length(), lane.halfWidth(), y, look, false, n);
					}
				}
			}
			case BossFootprint.Spokes spokes -> {
				double reach = live ? spokes.reach(age) : spokes.length();
				for (int i = 0; i < spokes.count(); i++) {
					double[] dir = Shapes.rotate(forward, spokes.angle(i));
					strip(canvas, frame.origin(), dir, spokes.inner(), reach, spokes.halfWidth(), y, look, live, i);
					if (live && reach < spokes.length()) {
						strip(canvas, frame.origin(), dir, reach, spokes.length(), spokes.halfWidth(), y, look, false, i);
					}
				}
			}
			case BossFootprint.Cone cone -> sector(canvas, frame.origin(), y, forward, cone.inner(), cone.radius(),
					cone.offsetDegrees() - cone.degrees() / 2.0D, cone.offsetDegrees() + cone.degrees() / 2.0D, look, live, true);
			case BossFootprint.Sweep sweep -> {
				double half = sweep.degrees() / 2.0D;
				sector(canvas, frame.origin(), y, forward, sweep.inner(), sweep.radius(), -half, half, look, false, true);
				if (live) {
					double at = sweep.angle(age), band = sweep.bandDegrees() / 2.0D;
					sector(canvas, frame.origin(), y, forward, sweep.inner(), sweep.radius(), Math.max(-half, at - band),
							Math.min(half, at + band), look, true, true);
				}
			}
			case BossFootprint.Ring ring -> {
				if (!live) {
					sector(canvas, frame.origin(), y, forward, Math.min(ring.from(), ring.to()), Math.max(ring.from(), ring.to()),
							-180.0D, 180.0D, look.scaled(0.6F), false, false);
					canvas.edgeArc(look.edge(), frame.origin(), y, forward, ring.to(), -180.0D, 180.0D, EDGE,
							look.previewEdge(), look.seed());
				}
				for (int wave = 0; live && wave < Math.max(1, ring.waves()); wave++) {
					double radius = ring.radius(age, wave);
					if (radius < 0.0D) continue;
					sector(canvas, frame.origin(), y, forward, Math.max(0.0D, radius - ring.band() / 2.0D),
							radius + ring.band() / 2.0D, -180.0D, 180.0D, look, true, false);
				}
			}
			case BossFootprint.Disc disc -> sector(canvas, frame.anchor(disc.at()), y, forward, 0.0D, disc.radius(),
					-180.0D, 180.0D, look, live, false);
			case BossFootprint.Annulus annulus -> {
				if (annulus.arcs() <= 0) {
					sector(canvas, frame.origin(), y, forward, annulus.inner(), annulus.outer(), -180.0D, 180.0D, look, live, false);
				} else {
					double clock = Math.max(0.0D, age);
					for (int arc = 0; arc < annulus.arcs(); arc++) {
						double at = annulus.arcAngle(clock, arc), half = annulus.arcDegrees() / 2.0D;
						sector(canvas, frame.origin(), y, forward, annulus.inner(), annulus.outer(), at - half, at + half,
								look, live, true);
					}
				}
			}
			case BossFootprint.Cross cross -> {
				Vec3 centre = frame.anchor(cross.at());
				double[] across = Shapes.rotate(forward, 90.0D);
				strip(canvas, offsetAlong(centre, forward, -cross.arm()), forward, 0.0D, cross.arm() * 2.0D,
						cross.halfWidth(), y, look, live, 0);
				strip(canvas, offsetAlong(centre, across, -cross.arm()), across, 0.0D, cross.arm() * 2.0D,
						cross.halfWidth(), y, look, live, 1);
			}
			case BossFootprint.Discs discs -> {
				List<Vec3> points = frame.points();
				for (int i = 0; i < points.size(); i++) {
					boolean landed = live && discs.landed(age, i);
					sector(canvas, points.get(i), y, forward, 0.0D, discs.radius(), -180.0D, 180.0D, look, landed, false);
				}
			}
			case BossFootprint.Path path -> {
				List<Vec3> points = frame.points();
				if (points.size() < 2) return;
				boolean whole = path.walkers() <= 0;
				polyline(canvas, points, path.halfWidth(), y, look, live && whole);
				if (live && !whole) {
					double length = Shapes.pathLength(points);
					for (int index = 0; index < path.walkers(); index++) {
						double head = Math.min(length, path.walker(age, index));
						double tail = Math.max(0.0D, head - path.frontLength());
						if (head <= 0.0D || tail >= length) continue;
						polyline(canvas, slice(points, tail, head), path.halfWidth(), y, look, true);
					}
				}
			}
			case BossFootprint.Region region -> region(canvas, region, frame, y, age, look, live);
			case BossFootprint.Tether tether -> {
				if (tether.broken(frame)) return;
				Vec3 a = tether.end(frame, tether.from()), b = tether.end(frame, tether.to());
				if (tether.halfWidth() <= 0.0D) {
					canvas.edge(look.edge(), TelegraphCanvas.flat(a, y), TelegraphCanvas.flat(b, y), EDGE,
							live ? look.edgeAlpha() * 0.6F : look.previewEdge() * 0.6F, look.seed());
					sector(canvas, b, y, forward, 0.0D, BossFootprint.Tether.END_REACH, -180.0D, 180.0D, look, live, false);
				} else {
					double[] dir = direction(a, b);
					if (dir != null) strip(canvas, a, dir, 0.0D, Shapes.planar(a, b), tether.halfWidth(), y, look, live, 0);
				}
			}
			case BossFootprint.Union union -> {
				for (BossFootprint.Union.Part part : union.parts()) {
					footprint(canvas, part.shape(), frame, age - part.delay(), look);
				}
			}
			case BossFootprint.Occluded occluded -> footprint(canvas, occluded.shape(), frame, age, look);
			case BossFootprint.None none -> {
			}
		}
	}

	// ── pieces ───────────────────────────────────────────────────────

	/** A strip from {@code root} along {@code dir}, from {@code from} to {@code to}: tint plus its two long edges. */
	private static void strip(TelegraphCanvas canvas, Vec3 root, double[] dir, double from, double to, double halfWidth,
			double y, Look look, boolean lit, int seed) {
		if (to <= from) return;
		Vec3 a = offsetAlong(root, dir, from), b = offsetAlong(root, dir, to);
		canvas.tintStrip(a, b, y, halfWidth, TelegraphCanvas.rgb(look.fill()), lit ? look.fillAlpha() : look.previewFill());
		double[] side = {-dir[1], dir[0]};
		float edge = lit ? look.edgeAlpha() : look.previewEdge();
		for (int sign = -1; sign <= 1; sign += 2) {
			Vec3 s = new Vec3(side[0] * halfWidth * sign, 0.0D, side[1] * halfWidth * sign);
			canvas.edge(look.edge(), TelegraphCanvas.flat(a.add(s), y), TelegraphCanvas.flat(b.add(s), y), EDGE, edge,
					look.seed() + seed * 7 + sign);
		}
	}

	/** An annular sector: tint, its arcs and (when {@code radial}) its two straight sides. */
	private static void sector(TelegraphCanvas canvas, Vec3 centre, double y, double[] forward, double r0, double r1,
			double fromDeg, double toDeg, Look look, boolean lit, boolean radial) {
		if (r1 <= r0 || toDeg <= fromDeg) return;
		canvas.tintSector(centre, y, forward, r0, r1, fromDeg, toDeg, TelegraphCanvas.rgb(look.fill()),
				lit ? look.fillAlpha() : look.previewFill());
		float edge = lit ? look.edgeAlpha() : look.previewEdge();
		canvas.edgeArc(look.edge(), centre, y, forward, r1, fromDeg, toDeg, EDGE, edge, look.seed());
		if (r0 > 0.05D) canvas.edgeArc(look.edge(), centre, y, forward, r0, fromDeg, toDeg, EDGE, edge * 0.7F, look.seed() + 1);
		if (radial && toDeg - fromDeg < 359.0D) {
			canvas.edge(look.edge(), TelegraphCanvas.polar(centre, y, forward, fromDeg, r0),
					TelegraphCanvas.polar(centre, y, forward, fromDeg, r1), EDGE, edge, look.seed() + 2);
			canvas.edge(look.edge(), TelegraphCanvas.polar(centre, y, forward, toDeg, r0),
					TelegraphCanvas.polar(centre, y, forward, toDeg, r1), EDGE, edge, look.seed() + 3);
		}
	}

	private static void polyline(TelegraphCanvas canvas, List<Vec3> points, double halfWidth, double y, Look look,
			boolean lit) {
		for (int i = 0; i + 1 < points.size(); i++) {
			double[] dir = direction(points.get(i), points.get(i + 1));
			if (dir == null) continue;
			strip(canvas, points.get(i), dir, 0.0D, Shapes.planar(points.get(i), points.get(i + 1)), halfWidth, y, look,
					lit, i);
		}
	}

	/** The arena area as block cells, each tested against the region's own hit rule. */
	private static void region(TelegraphCanvas canvas, BossFootprint.Region region, FootprintFrame frame, double y,
			double age, Look look, boolean live) {
		AABB box = switch (region.area()) {
			case BOSS_FLOOR -> frame.bossFloor();
			case CHAMBER -> frame.chamber();
			case AROUND_ORIGIN -> new AABB(frame.origin(), frame.origin()).inflate(region.radius(), 0.0D, region.radius());
		};
		int x0 = Mth.floor(box.minX), x1 = Mth.floor(box.maxX), z0 = Mth.floor(box.minZ), z1 = Mth.floor(box.maxZ);
		if (x1 - x0 > REGION_LIMIT || z1 - z0 > REGION_LIMIT) return;
		int rgb = TelegraphCanvas.rgb(look.fill());
		float alpha = live ? look.fillAlpha() : look.previewFill();
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				Vec3 centre = new Vec3(x + 0.5D, y, z + 0.5D);
				if (!region.hits(frame, centre, age)) continue;
				canvas.tintQuad(new Vec3(x, y, z), new Vec3(x + 1, y, z), new Vec3(x + 1, y, z + 1), new Vec3(x, y, z + 1),
						rgb, alpha);
			}
		}
	}

	private static List<Vec3> slice(List<Vec3> points, double from, double to) {
		List<Vec3> out = new ArrayList<>();
		out.add(Shapes.pointAlong(points, from));
		double travelled = 0.0D;
		for (int i = 0; i + 1 < points.size(); i++) {
			travelled += Shapes.planar(points.get(i), points.get(i + 1));
			if (travelled > from && travelled < to) out.add(points.get(i + 1));
		}
		out.add(Shapes.pointAlong(points, to));
		return out;
	}

	private static Vec3 offset(Vec3 origin, double[] dir, double side) {
		return side == 0.0D ? origin : new Vec3(origin.x - dir[1] * side, origin.y, origin.z + dir[0] * side);
	}

	private static Vec3 offsetAlong(Vec3 origin, double[] dir, double distance) {
		return new Vec3(origin.x + dir[0] * distance, origin.y, origin.z + dir[1] * distance);
	}

	private static double[] direction(Vec3 a, Vec3 b) {
		double dx = b.x - a.x, dz = b.z - a.z, length = Math.sqrt(dx * dx + dz * dz);
		return length < 1.0E-6D ? null : new double[]{dx / length, dz / length};
	}
}
