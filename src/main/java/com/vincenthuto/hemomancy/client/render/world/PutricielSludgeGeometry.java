package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.PutricielBloomRules.Sludge;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Shapes;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielHazard;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

/**
 * Saint Putriciel's rot sludge (revision 2026-11-06, R2.4 and R2.5): thick, wet, glossy pools of rot standing on the
 * chamber floor, real meshes with body in the growth material's {@link #SLUDGE} shape (solid, depth-writing,
 * facet-lit). A pool is a polar heightfield, 12 rings by 28 segments, 0.18 thick at its centre falling to a raised
 * meniscus lip just inside a lobed rim and a rounded edge to the floor; a runnel is the same section swept along its
 * lane and pinched at both ends. It spreads from its splash as it forms, bubbles in the rot (glossy domes that swell,
 * burst into a ring ripple and puff pale spores), settles into the moss when the cap takes it, and when the fire comes
 * for it it burns from the catch point outward: a burning front, then the boil (tall tongues, heaving and bursting
 * bubbles, the char spreading, the gloss gone, the lobes curling), the cracked crust and the crumble onto the ash.
 * The colours are the rot's until the catch; the burning crust uses the growth shader's char (black, glowing cracks,
 * cold ash). The vertex colour is state, never a tint: red the char, green the gloss, blue the scum; alpha is the
 * point's place out from the centre (0) to the rim (1), which the shader reads for the meniscus band.
 * Also the fire runner: a burning fuse crawling across the floor from a pyre or a burning pool to the pool it lights.
 */
final class PutricielSludgeGeometry {
	/** The growth shader's sludge shape. */
	static final int SLUDGE = 5;
	static final int RINGS = 12, SEGMENTS = 28;
	/** Where the rings stand out from the centre: closer toward the rim, so the meniscus lip and the rounded edge read. */
	private static final double[] RING_PLACES = {0.0D, 0.15D, 0.3D, 0.45D, 0.6D, 0.72D, 0.8D, 0.84D, 0.88D, 0.92D, 0.955D, 0.985D, 1.0D};
	/** A runnel's columns either side of its centre line, the same way. */
	private static final double[] SIDE_PLACES = {0.0D, 0.3D, 0.6D, 0.82D, 0.88D, 0.95D, 1.0D};
	/** A runnel's mesh: a station every quarter block along it, six columns either side of its centre line. */
	private static final double RUNNEL_STEP = 0.25D;
	private static final int RUNNEL_SIDE = SIDE_PLACES.length - 1;
	/** The mesh sits a hair over the floor so its rim never fights it; the stain reaches this far past the rim. */
	private static final double FLOAT = 0.004D, STAIN_BEYOND = 0.35D;
	/** How far the lumps of thicker rot stand out of (and sink into) the surface, blocks. */
	private static final double LUMP = 0.022D;
	/** Spatial coordinates in the shader: blocks per unit of U and of V about the pool's centre. */
	static final double U_SPAN = 4.0D, V_SPAN = 5.6D;

	private PutricielSludgeGeometry() {
	}

	/**
	 * One pool's footprint in the world: a lobed disc round {@code centre}, or a runnel from {@code start} along
	 * ({@code fx}, {@code fz}) for {@code length} with half-width {@code half}. Its seed is the hash of its frame's
	 * origin, so every client draws the same lobes and bubbles, and its burning pool (laid on the same footprint)
	 * keeps them.
	 */
	record Pool(Vec3 centre, double radius, boolean lane, Vec3 start, double fx, double fz, double length, double half, int seed) {
		static Pool of(BossGroundHazardEntity hazard) {
			BossFootprint shape = hazard.shape();
			FootprintFrame frame = hazard.frame();
			int seed = PutricielSludgeGeometry.seed(frame.origin());
			double floor = frame.floorY();
			// a runnel: a whole-length path through its two ends, carried as the frame's points (its burning pool keeps
			// them while its aim moves to the catch point)
			double runnelHalf = shape instanceof BossFootprint.Path path ? path.halfWidth()
					: shape instanceof BossFootprint.Lane lane ? lane.halfWidth() : -1.0D;
			if (runnelHalf > 0.0D) {
				Vec3 a, b;
				if (frame.points().size() >= 2) {
					a = frame.points().get(0);
					b = frame.points().get(frame.points().size() - 1);
				} else {
					double[] f = Shapes.forward(frame);
					double length = shape instanceof BossFootprint.Lane lane ? lane.length() : 4.0D;
					a = frame.origin();
					b = a.add(f[0] * length, 0.0D, f[1] * length);
				}
				double dx = b.x - a.x, dz = b.z - a.z, l = Math.max(1.0E-3D, Math.hypot(dx, dz));
				Vec3 start = new Vec3(a.x, floor, a.z);
				return new Pool(new Vec3((a.x + b.x) / 2.0D, floor, (a.z + b.z) / 2.0D), l / 2.0D, true, start, dx / l, dz / l, l,
						runnelHalf, seed);
			}
			double radius = shape instanceof BossFootprint.Disc disc ? disc.radius() : 1.5D;
			Vec3 at = shape instanceof BossFootprint.Disc disc ? frame.anchor(disc.at()) : frame.origin();
			Vec3 centre = new Vec3(at.x, floor, at.z);
			return new Pool(centre, radius, false, centre, 1.0D, 0.0D, 0.0D, 0.0D, seed);
		}

		/** A disc pool, for tests and stills. */
		static Pool disc(Vec3 centre, double radius, int seed) {
			return new Pool(centre, radius, false, centre, 1.0D, 0.0D, 0.0D, 0.0D, seed);
		}

		/** A runnel, for tests and stills. */
		static Pool runnel(Vec3 start, double fx, double fz, double length, double half, int seed) {
			double l = Math.max(1.0E-3D, Math.hypot(fx, fz));
			return new Pool(start.add(fx / l * length / 2.0D, 0.0D, fz / l * length / 2.0D), length / 2.0D, true, start, fx / l, fz / l, length,
					half, seed);
		}

		/** The rim's distance from the centre at world bearing {@code angle} (radians), {@code spread} of the way formed. */
		double rim(double angle, double spread) {
			if (!lane) return radius * PutricielBloomRules.lobe(seed, angle) * spread;
			// a runnel's stain: its lane as an ellipse round its middle
			double rel = angle - Math.atan2(fz, fx);
			double a = length / 2.0D + half * 0.5D, b = half * 1.05D;
			return spread / Math.sqrt(Math.pow(Math.cos(rel) / a, 2.0D) + Math.pow(Math.sin(rel) / b, 2.0D));
		}

		/** A point's place out from the pool's centre line or centre: 0 there, 1 at the rim, more outside. */
		double rho(double wx, double wz, double spread) {
			if (spread <= 1.0E-3D) return 2.0D;
			if (!lane) {
				double dx = wx - centre.x, dz = wz - centre.z;
				return Math.hypot(dx, dz) / Math.max(1.0E-3D, radius * PutricielBloomRules.lobe(seed, Math.atan2(dz, dx)) * spread);
			}
			double dx = wx - start.x, dz = wz - start.z;
			double s = dx * fx + dz * fz, c = -dx * fz + dz * fx;
			if (s < 0.0D || s > length) return 2.0D;
			double w = runnelHalf(s) * spread;
			return w <= 1.0E-3D ? 2.0D : Math.abs(c) / w;
		}

		/** A runnel's half-width {@code s} along it: lobed, pinched at both ends. */
		double runnelHalf(double s) {
			// a rounded end (polish: the longer square-root pinch drew each end as a blade point)
			double end = Math.min(half * 1.15D, length / 2.0D);
			double t = HemoEasing.clamp01(Math.min(s, length - s) / end);
			double pinch = Math.sqrt(1.0D - (1.0D - t) * (1.0D - t));
			return half * (0.92D + 0.16D * (PutricielBloomRules.lobe(seed, s * 1.3D) - 0.975D) / 0.125D) * pinch;
		}

		/** The point of the rim nearest {@code from}: where a fire reaching from there first touches the pool. */
		Vec3 nearest(Vec3 from) {
			if (!lane) {
				double a = Math.atan2(from.z - centre.z, from.x - centre.x);
				double r = radius * PutricielBloomRules.lobe(seed, a);
				return new Vec3(centre.x + Math.cos(a) * r, centre.y, centre.z + Math.sin(a) * r);
			}
			double s = Mth.clamp((from.x - start.x) * fx + (from.z - start.z) * fz, 0.0D, length);
			double c = -(from.x - start.x) * fz + (from.z - start.z) * fx;
			double w = runnelHalf(s) * Math.signum(c);
			return new Vec3(start.x + fx * s - fz * w, start.y, start.z + fz * s + fx * w);
		}
	}

	/** A stable seed from a frame's origin. */
	static int seed(Vec3 origin) {
		int x = (int) Math.floor(origin.x * 16.0D), z = (int) Math.floor(origin.z * 16.0D);
		return (x * 73856093) ^ (z * 19349663);
	}

	/**
	 * A pool as drawn now: its look, and when it burns the tick into the burn, where the fire caught it and where the
	 * fire came from (the draught leans the tongues toward the body).
	 */
	record State(Sludge look, float burn, @Nullable Vec3 caught, @Nullable Vec3 source, float age) {
		boolean burning() {
			return burn >= 0.0F;
		}
	}

	/**
	 * How a sludge hazard draws now, or null when it draws nothing: a pool in the rot (forming and bubbling), a pool
	 * settling under the cap, or a pool burning (its {@code SLUDGE_FIRE} on the same footprint, aim the catch point and
	 * target the source; the ending's lives ten ticks and runs the whole burn in them). A pool the fire has caught, or
	 * any pool once the ending runs, draws nothing itself: its burning pool does.
	 */
	@Nullable
	static State state(BossGroundHazardEntity hazard, PutricielHazard kind, float age, Pool pool, @Nullable PutricielEntity boss) {
		if (kind == PutricielHazard.ROT_SLUDGE) {
			if (boss != null && boss.syncedEndingTick() >= 0) return null;
			if (!hazard.fading()) return new State(PutricielBloomRules.sludge(age), -1.0F, null, null, age);
			// a caught pool fades over the burn while its burning pool draws it; any other fade is a settle
			if (hazard.fade() == PutricielSludgeRules.BURN) return null;
			Sludge settling = PutricielBloomRules.settling(age - hazard.fadeStart(), Math.max(1, hazard.fade()));
			return settling.visible() ? new State(settling, -1.0F, null, null, age) : null;
		}
		if (kind == PutricielHazard.SLUDGE_FIRE) {
			FootprintFrame frame = hazard.frame();
			float b = hazard.life() <= PutricielSludgeRules.ENDING_BURN ? age * PutricielBloomRules.SLUDGE_BURN / PutricielBloomRules.SLUDGE_ENDING_BURN
					: age;
			return new State(PutricielBloomRules.sludgeBurn(b), b, frame.aim(), frame.target(), age);
		}
		return null;
	}

	// ── the mesh ─────────────────────────────────────────────────────

	/**
	 * The pool's body: {@code heave} swells its surface (the Bloom's pulse), {@code time} runs its slow undulation.
	 * Every vertex stands between the floor and 0.25 over it.
	 */
	static void mesh(PoseStack p, VertexConsumer v, Pool pool, State state, Vec3 camera, double time, float heave) {
		Sludge look = state.look();
		if (!look.visible()) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(pool.seed());
		if (pool.lane()) {
			runnel(v, pose, pool, state, camera, time, heave, tile);
			return;
		}
		Vec3[][] grid = new Vec3[RINGS + 1][SEGMENTS];
		int[][] colour = new int[RINGS + 1][SEGMENTS];
		for (int k = 0; k <= RINGS; k++) {
			double rho = RING_PLACES[k];
			for (int j = 0; j < SEGMENTS; j++) {
				double a = j * Math.PI * 2.0D / SEGMENTS;
				double r = rho * pool.rim(a, look.spread()) * (1.0D - 0.08D * look.curl() * HemoEasing.smoothstep(0.8D, 1.0D, rho));
				double wx = pool.centre().x + Math.cos(a) * r, wz = pool.centre().z + Math.sin(a) * r;
				grid[k][j] = new Vec3(wx, pool.centre().y + surface(pool, look, rho, wx, wz, time, heave), wz);
				colour[k][j] = colour(state, pool, wx, wz);
			}
		}
		for (int k = 0; k < RINGS; k++) {
			for (int j = 0; j < SEGMENTS; j++) {
				int m = (j + 1) % SEGMENTS;
				quad(v, pose, pool, camera, tile, grid[k][j], grid[k + 1][j], grid[k + 1][m], grid[k][m], colour[k][j], colour[k + 1][j],
						colour[k + 1][m], colour[k][m], RING_PLACES[k], RING_PLACES[k + 1]);
			}
		}
	}

	/** A runnel: stations along its lane, columns across it, the same section as a pool's swept along it. */
	private static void runnel(VertexConsumer v, Matrix4f pose, Pool pool, State state, Vec3 camera, double time, float heave, int tile) {
		Sludge look = state.look();
		int stations = Math.max(2, (int) Math.ceil(pool.length() / RUNNEL_STEP));
		int columns = RUNNEL_SIDE * 2;
		Vec3[][] grid = new Vec3[stations + 1][columns + 1];
		int[][] colour = new int[stations + 1][columns + 1];
		double[] places = new double[columns + 1];
		for (int i = 0; i <= stations; i++) {
			double s = pool.length() * i / stations;
			double w = pool.runnelHalf(s) * look.spread();
			double pinch = pool.half() <= 0.0D ? 0.0D : Math.min(1.0D, w / Math.max(1.0E-3D, pool.half() * look.spread()));
			for (int c = 0; c <= columns; c++) {
				double rho = SIDE_PLACES[Math.abs(c - RUNNEL_SIDE)];
				double across = Math.signum(c - RUNNEL_SIDE) * rho;
				places[c] = rho;
				double off = across * w * (1.0D - 0.08D * look.curl() * HemoEasing.smoothstep(0.8D, 1.0D, rho));
				double wx = pool.start().x + pool.fx() * s - pool.fz() * off, wz = pool.start().z + pool.fz() * s + pool.fx() * off;
				double y = pool.centre().y + FLOAT + (surface(pool, look, rho, wx, wz, time, heave) - FLOAT) * Math.sqrt(pinch);
				grid[i][c] = new Vec3(wx, y, wz);
				colour[i][c] = colour(state, pool, wx, wz);
			}
		}
		for (int i = 0; i < stations; i++) {
			for (int c = 0; c < columns; c++) {
				// the radial place runs across a runnel, so each quad carries its own pair of places
				quadAcross(v, pose, pool, camera, tile, grid[i][c], grid[i][c + 1], grid[i + 1][c + 1], grid[i + 1][c], colour[i][c],
						colour[i][c + 1], colour[i + 1][c + 1], colour[i + 1][c], places[c], places[c + 1]);
			}
		}
	}

	/** The surface's height over the floor at a place {@code rho} out, with its lip, its curl, its undulation and heave. */
	private static double surface(Pool pool, Sludge look, double rho, double wx, double wz, double time, float heave) {
		double h = PutricielBloomRules.sludgeProfile(Math.min(0.999D, rho)) * look.thick();
		double inner = 1.0D - HemoEasing.smoothstep(0.6D, 0.9D, rho);
		// a faint slow undulation while it is wet, and the heave of a Bloom's pulse or the boil
		h += 0.008D * Math.sin(time * 0.07D + wx * 2.1D + wz * 1.7D + pool.seed()) * inner * look.gloss();
		h += 0.03D * heave * Math.sin(time * 0.45D + rho * 9.0D + wx * 3.0D - wz * 2.0D) * inner;
		// lumps of thicker rot standing in it (polish: a flat surface read as water in a dish), so the gloss breaks over them
		double phase = (pool.seed() & 1023) * 0.37D;
		h += LUMP * inner * look.thick() * (Math.sin(wx * 3.3D + phase) * Math.sin(wz * 2.9D - phase * 0.7D)
				+ 0.5D * Math.sin((wx + wz) * 5.1D + phase * 1.3D)) / 1.5D;
		// the burnt lobes curl up off the floor at the rim
		h += 0.07D * look.curl() * HemoEasing.smoothstep(0.75D, 1.0D, rho);
		return FLOAT + Math.max(0.0D, h - look.sink());
	}

	/** A vertex's state: the char behind the burning front, the gloss it keeps, the scum on it. */
	private static int colour(State state, Pool pool, double wx, double wz) {
		Sludge look = state.look();
		float charred = look.charred(), gloss = look.gloss();
		if (state.burning() && state.caught() != null) {
			double d = Math.hypot(wx - state.caught().x, wz - state.caught().z);
			float since = state.burn() - (float) (d / Math.max(1.0E-3D, pool.radius() * PutricielBloomRules.FRONT_PER_RADIUS));
			charred = Math.max(charred, PutricielBloomRules.sludgeChar(since, state.burn()));
			gloss = since < 0.0F ? 1.0F : gloss * (1.0F - HemoEasing.ramp(since, 0.0F, 10.0F));
		}
		return PutricielGroundGeometry.color(charred, gloss, look.scum());
	}

	private static void quad(VertexConsumer v, Matrix4f pose, Pool pool, Vec3 camera, int tile, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int ca, int cb,
			int cc, int cd, double rhoInner, double rhoOuter) {
		vertex(v, pose, pool, camera, tile, a, ca, rhoInner);
		vertex(v, pose, pool, camera, tile, b, cb, rhoOuter);
		vertex(v, pose, pool, camera, tile, c, cc, rhoOuter);
		vertex(v, pose, pool, camera, tile, d, cd, rhoInner);
	}

	private static void quadAcross(VertexConsumer v, Matrix4f pose, Pool pool, Vec3 camera, int tile, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int ca,
			int cb, int cc, int cd, double rhoA, double rhoB) {
		vertex(v, pose, pool, camera, tile, a, ca, rhoA);
		vertex(v, pose, pool, camera, tile, b, cb, rhoB);
		vertex(v, pose, pool, camera, tile, c, cc, rhoB);
		vertex(v, pose, pool, camera, tile, d, cd, rhoA);
	}

	/** U and V carry the point's place about the pool's centre in blocks (sixteen texels a block in the shader). */
	private static void vertex(VertexConsumer v, Matrix4f pose, Pool pool, Vec3 camera, int tile, Vec3 at, int colour, double rho) {
		float u = (float) (SLUDGE * 2 + Mth.clamp((at.x - pool.centre().x) / U_SPAN + 0.95D, 0.0D, 1.9D));
		float w = (float) (tile * 2 + Mth.clamp((at.z - pool.centre().z) / V_SPAN + 0.5D, 0.0D, 0.999D));
		MaterialQuads.vertex(v, pose, at.x - camera.x, at.y - camera.y, at.z - camera.z, colour, (float) Mth.clamp(rho, 0.0D, 1.0D), u, w);
	}

	/** A glossy dome rising out of the surface at {@code at} (world): a bubble, {@code radius} across and {@code height} tall. */
	static void dome(PoseStack p, VertexConsumer v, Pool pool, Vec3 at, double radius, double height, Vec3 camera, int colour) {
		if (radius < 0.01D || height < 0.005D) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(pool.seed());
		int n = 8, rings = 3;
		Vec3[][] grid = new Vec3[rings + 1][n];
		for (int k = 0; k <= rings; k++) {
			double lat = Math.PI / 2.0D * k / rings;
			for (int j = 0; j < n; j++) {
				double a = j * Math.PI * 2.0D / n;
				double r = Math.cos(lat) * radius;
				grid[k][j] = at.add(Math.cos(a) * r, Math.sin(lat) * height - 0.01D, Math.sin(a) * r);
			}
		}
		for (int k = 0; k < rings; k++) {
			for (int j = 0; j < n; j++) {
				int m = (j + 1) % n;
				quad(v, pose, pool, camera, tile, grid[k][j], grid[k][m], grid[k + 1][m], grid[k + 1][j], colour, colour, colour, colour, 0.2D,
						0.2D);
			}
		}
	}

	/** A ring ripple spreading over the surface from a burst bubble: a thin raised annulus, glossy. */
	static void ripple(PoseStack p, VertexConsumer v, Pool pool, Vec3 at, double radius, double width, Vec3 camera, int colour) {
		if (radius < 0.02D || width < 0.005D) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(pool.seed());
		int n = 14;
		for (int j = 0; j < n; j++) {
			double a0 = j * Math.PI * 2.0D / n, a1 = (j + 1) * Math.PI * 2.0D / n;
			Vec3 i0 = at.add(Math.cos(a0) * radius, 0.004D, Math.sin(a0) * radius), i1 = at.add(Math.cos(a1) * radius, 0.004D, Math.sin(a1) * radius);
			Vec3 o0 = at.add(Math.cos(a0) * (radius + width), 0.012D, Math.sin(a0) * (radius + width));
			Vec3 o1 = at.add(Math.cos(a1) * (radius + width), 0.012D, Math.sin(a1) * (radius + width));
			quad(v, pose, pool, camera, tile, i0, o0, o1, i1, colour, colour, colour, colour, 0.3D, 0.3D);
		}
	}

	/** The surface's height over the floor at a world point in this pool, 0 outside it. */
	static double height(Pool pool, State state, double wx, double wz) {
		Sludge look = state.look();
		if (!look.visible()) return 0.0D;
		double rho = pool.rho(wx, wz, look.spread());
		if (rho >= 1.0D) return 0.0D;
		return surface(pool, look, rho, wx, wz, 0.0D, 0.0F);
	}

	// ── what each layer draws ────────────────────────────────────────

	/**
	 * The floor under and round a pool: its dark wet stain with the mould-green fringe (fading as it settles or burns),
	 * and once it burns the scorch eating out from the catch point, cooling to charcoal plates on the new ash.
	 */
	static void ground(PoseStack p, VertexConsumer floor, Pool pool, State state, Vec3 camera, int seed) {
		Sludge look = state.look();
		float stain = state.burning() ? 1.0F - HemoEasing.ramp(state.burn(), PutricielBloomRules.SLUDGE_BOIL_END, PutricielBloomRules.SLUDGE_ASH_AT)
				: look.stain();
		PutricielGroundGeometry.stain(p, floor, pool.centre(), a -> pool.rim(a, Math.max(0.05D, look.spread())), STAIN_BEYOND, stain, 1.0F,
				Math.min(1.0F, stain * 1.2F), camera, seed);
		if (!state.burning() || state.caught() == null) return;
		float b = state.burn();
		double reach = PutricielBloomRules.catchFront(b, pool.radius());
		PutricielGroundGeometry.Ground g = new PutricielGroundGeometry.Ground(pool.centre().x, pool.centre().z, pool.lane() ? pool.fx() : 1.0D,
				pool.lane() ? pool.fz() : 0.0D, pool.centre().y, camera);
		PutricielGroundGeometry.Front front = PutricielGroundGeometry.Front.from(state.caught(), reach, 0.5D);
		float heat = PutricielBloomRules.coolingHeat(b);
		float ash = HemoEasing.ramp(b, PutricielBloomRules.SLUDGE_ASH_AT - 4.0F, PutricielBloomRules.SLUDGE_BURN + 10.0F);
		PutricielGroundGeometry.Paint scorch = new PutricielGroundGeometry.Paint(PutricielGroundGeometry.SCORCH, heat, ash,
				PutricielBloomRules.coolingAlpha(b));
		if (pool.lane()) {
			PutricielGroundGeometry.rect(p, floor, g, -pool.length() / 2.0D, pool.length() / 2.0D, -pool.half() * 1.15D, pool.half() * 1.15D, front,
					scorch, seed + 1);
		} else {
			PutricielGroundGeometry.sector(p, floor, g, 0.0D, pool.radius() * PutricielBloomRules.LOBE_MAX, -180.0D, 180.0D, front, scorch, seed + 1);
		}
	}

	/** The body and its bubbles: domes swelling out of the surface in the rot and in the boil, ripples where they burst. */
	static void solid(PoseStack p, VertexConsumer growth, Pool pool, State state, Vec3 camera, double time, float heave) {
		float boil = state.burning() ? PutricielBloomRules.boil(state.burn()) : 0.0F;
		mesh(p, growth, pool, state, camera, time, Math.max(heave, boil));
		Sludge look = state.look();
		if (!look.visible() || look.spread() < 0.95F) return;
		int wet = PutricielGroundGeometry.color(look.charred(), Math.max(0.6F, look.gloss()), look.scum() * 0.5F);
		if (!state.burning()) {
			if (look.gloss() < 0.5F) return;
			int slots = 2 + (heave > 0.2F ? 3 : 0);
			for (int slot = 0; slot < slots; slot++) {
				double[] bubble = PutricielBloomRules.bubble(pool.seed() + (slot >= 2 ? 77 : 0), slot, state.age() * (slot >= 2 ? 3.0F : 1.0F));
				if (bubble == null) continue;
				Vec3 at = bubbleAt(pool, state, bubble);
				if (bubble[3] < 0.0D) {
					double r = (0.05D + 0.07D * bubble[2]) * (0.8D + 0.4D * PutricielBloomRules.unit(pool.seed(), 930 + slot));
					dome(p, growth, pool, at, r, (0.06D + 0.08D * bubble[2]) * 0.6D, camera, wet);
				} else if (bubble[3] < PutricielBloomRules.RIPPLE) {
					double t = bubble[3] / PutricielBloomRules.RIPPLE;
					ripple(p, growth, pool, at, 0.05D + 0.35D * HemoEasing.easeOutCubic(t), 0.03D * (1.0D - t) + 0.008D, camera, wet);
				}
			}
			return;
		}
		if (boil <= 0.0F) return;
		int hot = PutricielGroundGeometry.color(Math.min(0.62F, Math.max(0.5F, look.charred())), 0.3F, 0.0F);
		for (int slot = 0; slot < PutricielBloomRules.BOIL_SLOTS; slot++) {
			double[] bubble = PutricielBloomRules.boilBubble(pool.seed(), slot, state.burn());
			if (bubble == null || bubble[3] >= 0.0D) continue;
			Vec3 at = bubbleAt(pool, state, bubble);
			dome(p, growth, pool, at, 0.06D + 0.08D * bubble[2], (0.05D + 0.09D * bubble[2]) * boil, camera, hot);
		}
	}

	/**
	 * What rises off a pool: in the rot a pale spore puff where a bubble bursts; burning, the fire itself. The low
	 * tongues run along the front through the catch; through the boil tongues stand over the whole pool (one per 0.35
	 * square blocks), leaning with the draught toward the body, and every bursting bubble flashes with heat, three
	 * sparks and a puff of soot; through the char they die back to embers and two soot columns rise; at the crumble the
	 * crust breaks into ash flakes that settle onto the new ash.
	 */
	static void air(PoseStack p, VertexConsumer spores, VertexConsumer flame, Pool pool, State state, Vec3 camera, Vec3 right, Vec3 up,
			double time, int budget) {
		Sludge look = state.look();
		if (!state.burning()) {
			if (!look.visible() || look.gloss() < 0.5F) return;
			for (int slot = 0; slot < 2; slot++) {
				double[] bubble = PutricielBloomRules.bubble(pool.seed(), slot, state.age());
				if (bubble == null || bubble[3] < 0.0D) continue;
				Vec3 at = bubbleAt(pool, state, bubble).subtract(camera);
				PutricielSporeGeometry.burst(p, spores, at.add(0.0D, 0.06D, 0.0D), right, up, (float) bubble[3] * 1.4F, 0.35D, 3, 0.15F, 0.8F,
						pool.seed() + slot * 11 + (int) (state.age() / 40.0F));
			}
			return;
		}
		float b = state.burn();
		Vec3 caught = state.caught() == null ? pool.centre() : state.caught();
		Vec3 source = state.source() == null ? pool.centre() : state.source();
		double reach = PutricielBloomRules.catchFront(b, pool.radius());
		float fire = PutricielBloomRules.sludgeFire(b);
		int seed = pool.seed();
		// the low tongues on the burning front through the catch
		if (b < PutricielBloomRules.SLUDGE_BOIL_END && fire > 0.0F) {
			int count = Math.min(14, 4 + (int) (reach * 3.0D));
			for (int i = 0; i < count; i++) {
				double a = Math.atan2(pool.centre().z - caught.z, pool.centre().x - caught.x) + (i / (double) Math.max(1, count - 1) - 0.5D) * 2.4D;
				Vec3 at = new Vec3(caught.x + Math.cos(a) * reach, pool.centre().y, caught.z + Math.sin(a) * reach);
				if (pool.rho(at.x, at.z, 1.0D) > 1.0D) continue;
				Vec3 root = at.add(0.0D, height(pool, state, at.x, at.z), 0.0D).subtract(camera);
				double h = 0.3D + 0.2D * PutricielBloomRules.unit(seed, 1000 + i);
				PutricielFlameGeometry.tongue(p, flame, root, Vec3.ZERO, h * PutricielBloomRules.flicker(time, seed, i), 0.07D, time, seed + i * 3,
						0.9F * HemoEasing.ramp(b, 0.0F, 1.5F));
			}
		}
		// the boil and the char: tongues over everything the front has passed, leaning toward the body
		float area = (float) (pool.lane() ? pool.length() * pool.half() * 2.0D : Math.PI * pool.radius() * pool.radius());
		int count = Math.min(budget, Math.max(6, Math.round(area / 0.35F)));
		Vec3 draught = new Vec3(source.x - pool.centre().x, 0.0D, source.z - pool.centre().z);
		draught = draught.lengthSqr() < 1.0E-6D ? Vec3.ZERO : draught.normalize().scale(0.22D);
		float tall = fire * HemoEasing.ramp(b, PutricielBloomRules.SLUDGE_CATCH - 1.0F, PutricielBloomRules.SLUDGE_CATCH + 3.0F);
		for (int i = 0; i < count && tall > 0.01F; i++) {
			Vec3 at = scatter(pool, seed, i);
			double d = Math.hypot(at.x - caught.x, at.z - caught.z);
			if (d > reach) continue;
			double h = 1.1D * (0.55D + 0.45D * PutricielBloomRules.unit(seed, 1100 + i)) * tall;
			Vec3 root = at.add(0.0D, height(pool, state, at.x, at.z), 0.0D).subtract(camera);
			PutricielFlameGeometry.tongue(p, flame, root, draught, h * PutricielBloomRules.flicker(time, seed, i), Math.max(0.09D, h * 0.24D), time,
					seed + 40 + i * 5, 0.95F);
			if (i % 4 == 0) PutricielFlameGeometry.bed(p, flame, root, 0.35D + 0.3D * h, seed + i, 0.45F * tall);
		}
		// bursting bubbles: a heat flash, three sparks and a soot puff each, two a tick at most
		for (int slot = 0; slot < PutricielBloomRules.BOIL_SLOTS; slot++) {
			double[] bubble = PutricielBloomRules.boilBubble(seed, slot, b);
			if (bubble == null || bubble[3] < 0.0D) continue;
			Vec3 at = bubbleAt(pool, state, bubble).subtract(camera);
			float since = (float) bubble[3];
			int key = (int) bubble[4];
			PutricielFlameGeometry.flash(p, flame, at.add(0.0D, 0.12D, 0.0D), right, up, since * 2.0F, 0.32D, key, 0.8F);
			PutricielFlameGeometry.sparks(p, flame, at.add(0.0D, 0.1D, 0.0D), Vec3.ZERO, right, up, since * 2.0F, 1.2D, 3, key, 0.9F);
			PutricielFlameGeometry.smoke(p, flame, at.add(0.0D, 0.4D + since * 0.2D, 0.0D), right, up, 0.18D, time, key, 0.5F);
		}
		// the char: two soot columns over the dying fire
		float soot = HemoEasing.window(b, PutricielBloomRules.SLUDGE_BOIL_END - 4.0F, PutricielBloomRules.SLUDGE_BOIL_END,
				PutricielBloomRules.SLUDGE_ASH_AT, PutricielBloomRules.SLUDGE_BURN + 6.0F);
		for (int c = 0; c < 2 && soot > 0.0F; c++) {
			Vec3 base = scatter(pool, seed + 500, c).subtract(camera);
			for (int k = 0; k < 3; k++) {
				PutricielFlameGeometry.smoke(p, flame, base.add(0.0D, 0.5D + k * 0.55D, 0.0D), right, up, 0.3D + 0.12D * k, time + k * 7.0D,
						seed + 600 + c * 5 + k, 0.55F * soot);
			}
		}
		// the crumble: the crust breaks to ash flakes, a few lifting, the rest settling onto the new ash
		float crumble = HemoEasing.window(b, PutricielBloomRules.SLUDGE_ASH_AT, PutricielBloomRules.SLUDGE_ASH_AT + 1.0F,
				PutricielBloomRules.SLUDGE_BURN - 1.0F, PutricielBloomRules.SLUDGE_BURN + 8.0F);
		for (int i = 0; i < 14 && crumble > 0.0F; i++) {
			Vec3 at = scatter(pool, seed + 700, i);
			double t = HemoEasing.clamp01((b - PutricielBloomRules.SLUDGE_ASH_AT) / 14.0D);
			boolean lifts = i % 4 == 0;
			double y = lifts ? 0.1D + 0.9D * t : 0.12D * (1.0D - t);
			Vec3 flake = at.add(Math.sin(i + b * 0.2D) * 0.1D * t, y, Math.cos(i * 1.7D + b * 0.2D) * 0.1D * t).subtract(camera);
			PutricielSporeGeometry.ash(p, spores, flake, right, up, 0.04D + 0.03D * PutricielBloomRules.unit(seed, 1200 + i), i % 3 == 0 ? 0.7F : 0.25F,
					(float) t * 0.5F, crumble, seed + 1200 + i);
		}
	}

	/** A pool's bubble's place in the world, on its surface. */
	private static Vec3 bubbleAt(Pool pool, State state, double[] bubble) {
		double wx, wz;
		if (pool.lane()) {
			double s = pool.length() * (0.5D + 0.4D * bubble[0]);
			double c = pool.runnelHalf(s) * 0.5D * bubble[1];
			wx = pool.start().x + pool.fx() * s - pool.fz() * c;
			wz = pool.start().z + pool.fz() * s + pool.fx() * c;
		} else {
			wx = pool.centre().x + bubble[0] * pool.radius();
			wz = pool.centre().z + bubble[1] * pool.radius();
		}
		return new Vec3(wx, pool.centre().y + height(pool, state, wx, wz) - 0.01D, wz);
	}

	/** Point {@code i} of an even scatter over the pool (world, on the floor). */
	static Vec3 scatter(Pool pool, int seed, int i) {
		if (pool.lane()) {
			double s = pool.length() * (0.1D + 0.8D * PutricielBloomRules.unit(seed, 1300 + i));
			double c = pool.runnelHalf(s) * 0.8D * (PutricielBloomRules.unit(seed, 1320 + i) * 2.0D - 1.0D);
			return new Vec3(pool.start().x + pool.fx() * s - pool.fz() * c, pool.centre().y, pool.start().z + pool.fz() * s + pool.fx() * c);
		}
		// a sunflower spiral, so the tongues stand evenly over the pool rather than clumping
		double golden = 2.39996323D;
		double a = i * golden + seed * 0.001D;
		double r = pool.radius() * 0.88D * Math.sqrt((i + 0.5D) / 36.0D);
		r = Math.min(r, pool.radius() * 0.9D * PutricielBloomRules.lobe(pool.seed(), a));
		return new Vec3(pool.centre().x + Math.cos(a) * r, pool.centre().y, pool.centre().z + Math.sin(a) * r);
	}

	// ── the fire runner ──────────────────────────────────────────────

	/**
	 * A fire runner: a burning fuse crawling across the floor from its source ({@code from}) to the pool it lights
	 * ({@code to}) at the runner's speed: a scorched strip 0.6 wide behind it, its head a knot of low tongues (0.3 to
	 * 0.5) over a glowing bed with sparks thrown off it. When it arrives its fire dies and the strip cools with the
	 * hazard.
	 */
	static void runner(PoseStack p, PutricielTelegraphPass.Scene s, Vec3 from, Vec3 to, float age, float fade, int seed) {
		double length = Math.hypot(to.x - from.x, to.z - from.z);
		if (length < 0.05D) return;
		double fx = (to.x - from.x) / length, fz = (to.z - from.z) / length;
		double head = PutricielBloomRules.runnerHead(age, length);
		float fire = PutricielBloomRules.runnerFire(age, length) * fade;
		if (s.ground()) {
			PutricielGroundGeometry.Ground g = new PutricielGroundGeometry.Ground(from.x, from.z, fx, fz, from.y, s.camera());
			PutricielGroundGeometry.rect(p, s.floor(), g, 0.0D, Math.max(0.05D, head), -0.3D, 0.3D,
					PutricielGroundGeometry.Front.from(from, head, 0.35D),
					new PutricielGroundGeometry.Paint(PutricielGroundGeometry.SCORCH, 0.35F + 0.5F * fire, 0.25F, fade), seed);
			return;
		}
		if (!s.spore() || fire <= 0.0F) return;
		Vec3 lean = new Vec3(fx * 0.25D, 0.0D, fz * 0.25D);
		for (int i = 0; i < 5; i++) {
			double back = i * 0.2D;
			if (head - back < 0.0D) break;
			Vec3 root = s.local(from.x + fx * (head - back), from.y, from.z + fz * (head - back));
			double h = (0.5D - 0.04D * i) * (0.85D + 0.3D * PutricielBloomRules.unit(seed, 1400 + i)) * PutricielBloomRules.flicker(s.time(), seed, i);
			PutricielFlameGeometry.tongue(s.p(), s.flame(), root.add(-fz * (i % 2 == 0 ? 0.08D : -0.08D), 0.0D, fx * (i % 2 == 0 ? 0.08D : -0.08D)),
					lean, h * fire, 0.075D, s.time(), seed + i * 7, fire);
		}
		Vec3 at = s.local(from.x + fx * head, from.y, from.z + fz * head);
		PutricielFlameGeometry.bed(s.p(), s.flame(), at, 0.45D, seed + 30, 0.55F * fire);
		PutricielFlameGeometry.sparks(s.p(), s.flame(), at.add(0.0D, 0.3D, 0.0D), lean, s.right(), s.up(), (float) ((age * 1.1D) % 10.0D), 1.4D, 3,
				seed + 40 + (int) (age / 9.0F), fire);
		PutricielFlameGeometry.smoke(s.p(), s.flame(), at.add(-fx * 0.4D, 0.7D, -fz * 0.4D), s.right(), s.up(), 0.25D, s.time(), seed + 50, 0.4F * fire);
	}
}
