package com.vincenthuto.hemomancy.common.encounter.ordeal;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

/**
 * The Stilt Walker's ordeal as pure geometry (2026-11-17): what the Stilt Walker's interventions and the Scarlet
 * Mummer's moves hit, and when, so a drawn footprint is a hit footprint and the true selves' models can pose from the
 * same numbers. Positions are feet; {@code floor} is the top of the ring's plate at its centre.
 */
public final class StiltWalkerMoves {
	/** The Stilt Walker's block of ground-hazard kinds ({@link OrdealHazardKinds#STILT_WALKER}). */
	public static final int KIND_PLANT = OrdealHazardKinds.STILT_WALKER, KIND_STRIDE = KIND_PLANT + 1,
			KIND_SPIN = KIND_PLANT + 2, KIND_SHADOW = KIND_PLANT + 3;
	/** The Scarlet Mummer's moves, in the same block. */
	public static final int KIND_DAZZLE = KIND_PLANT + 5, KIND_FEINT = KIND_PLANT + 6, KIND_WHIRL = KIND_PLANT + 7,
			KIND_ENTRANCE = KIND_PLANT + 8;
	/** Feet this far above the floor clear the spin: a jump. */
	public static final double CLEARANCE = OrdealMoves.CLEARANCE;
	/** The rim the professor's manifestations enter by. */
	public static final double RIM = OrdealStage.RING_RADIUS;

	// --- the Stilt Walker ------------------------------------------------------------------------------------------

	/** The planted stilt: a spectral stilt driven into the floor like a spear, and left standing there a moment. */
	public static final int PLANT_WINDUP = 22, PLANT_HOLD = 16;
	public static final double PLANT_RADIUS = 1.75D;
	public static final float PLANT_DAMAGE = 7.0F;
	/** The stride: four footfalls of a spectral leg walking across the ring on the chord through the fighter. */
	public static final int STRIDE_WINDUP = 20, STRIDE_GAP = 8, STRIDE_STEPS = 4;
	public static final double STRIDE_RADIUS = 1.6D, STRIDE_SWAY = 0.6D;
	public static final float STRIDE_DAMAGE = 5.0F;
	/** The spin: two spectral stilts across the ring's whole width, turned half a circle at knee height. */
	public static final int SPIN_WINDUP = 24, SPIN_TICKS = 32;
	public static final double SPIN_HALF_WIDTH = 0.6D;
	public static final float SPIN_DAMAGE = 5.0F;
	/** The long shadow: their shadow stands up at the rim and falls across the ring like a felled pole. */
	public static final int SHADOW_WINDUP = 30, SHADOW_REST = 14;
	public static final double SHADOW_LENGTH = 14.0D, SHADOW_HALF_WIDTH = 1.6D;
	public static final float SHADOW_DAMAGE = 8.0F;

	// --- the Scarlet Mummer ----------------------------------------------------------------------------------------

	/** The dazzle: every eye on its train opens at once; a fighter within reach who is looking at it is struck. */
	public static final int DAZZLE_WINDUP = 18;
	public static final double DAZZLE_REACH = 6.5D, DAZZLE_LOOK = 0.64D;
	public static final float DAZZLE_DAMAGE = 4.0F;
	/** The feint: a step aside, then a lunge back down a short lane with its long fingers. */
	public static final int FEINT_WINDUP = 12, FEINT_DASH = 3;
	public static final double FEINT_SIDESTEP = 2.5D, FEINT_LENGTH = 4.5D, FEINT_HALF_WIDTH = 0.9D;
	public static final float FEINT_DAMAGE = 6.0F;
	/** The whirl: a spin with its ribbons and fans out like blades. */
	public static final int WHIRL_WINDUP = 14;
	public static final double WHIRL_RADIUS = 3.0D;
	public static final float WHIRL_DAMAGE = 5.0F;
	/** The grand entrance: a leap across the ring onto the fighter's mark. */
	public static final int ENTRANCE_WINDUP = 14, ENTRANCE_FLIGHT = 12;
	public static final double ENTRANCE_RADIUS = 2.4D, ENTRANCE_HEIGHT = 4.0D;
	public static final float ENTRANCE_DAMAGE = 7.0F;

	private StiltWalkerMoves() {
	}

	public static boolean plantHits(Vec3 mark, Vec3 feet) {
		return horizontal(mark, feet) <= PLANT_RADIUS && feet.y - mark.y < 3.0D && feet.y >= mark.y - 0.5D;
	}

	/** Where the professor's manifestations enter the ring: the rim on the professor's side. */
	public static Vec3 rimToward(Vec3 floor, Vec3 professor) {
		Vec3 out = OrdealKit.flat(professor.subtract(floor));
		if (out.lengthSqr() < 1.0E-6D) out = new Vec3(0.0D, 0.0D, 1.0D);
		return floor.add(out.scale(RIM));
	}

	/**
	 * The stride's chord: from the rim on the professor's side through the fighter to the far rim, as its start and the
	 * vector to its end. A fighter on that rim gets the chord through the centre.
	 */
	public static Vec3[] strideChord(Vec3 floor, Vec3 rim, Vec3 fighter) {
		Vec3 aim = OrdealKit.flat(new Vec3(fighter.x - rim.x, 0.0D, fighter.z - rim.z));
		if (horizontal(rim, fighter) < 1.0D) aim = OrdealKit.flat(floor.subtract(rim));
		// The chord's far end: where the line from the rim along aim leaves the ring again.
		Vec3 rel = new Vec3(rim.x - floor.x, 0.0D, rim.z - floor.z);
		double length = Math.max(2.0D, -2.0D * rel.dot(aim));
		Vec3 start = new Vec3(rim.x, floor.y, rim.z);
		return new Vec3[] { start, aim.scale(length) };
	}

	/** Footfall {@code k} of the stride along its chord: spread evenly, swaying left and right like a walker's feet. */
	public static Vec3 strideStep(Vec3 start, Vec3 chord, int k) {
		Vec3 side = OrdealKit.flat(new Vec3(-chord.z, 0.0D, chord.x)).scale(k % 2 == 0 ? -STRIDE_SWAY : STRIDE_SWAY);
		return start.add(chord.scale((2.0D * k + 1.0D) / (2.0D * STRIDE_STEPS))).add(side);
	}

	/** The tick footfall {@code k} lands, from the wind-up's start. */
	public static int strideLands(int k) {
		return STRIDE_WINDUP + k * STRIDE_GAP;
	}

	public static boolean strideHits(Vec3 step, Vec3 feet) {
		return horizontal(step, feet) <= STRIDE_RADIUS && feet.y - step.y < 3.0D && feet.y >= step.y - 0.5D;
	}

	/** The spin's angle {@code ticks} after it starts turning: half a circle, slow out of the wind, whipping through. */
	public static double spinAngle(double start, double ticks) {
		return start + Math.PI * HemoEasing.smoothstep(HemoEasing.clamp01(ticks / SPIN_TICKS));
	}

	/** The spin's start: a quarter turn short of the fighter, so the stilts sweep through them halfway round. */
	public static double spinStart(Vec3 floor, Vec3 fighter) {
		return Math.atan2(fighter.z - floor.z, fighter.x - floor.x) - Math.PI / 2.0D;
	}

	/** Whether the stilts across the ring at {@code angle} catch these feet: on the line, inside the rim, not jumped. */
	public static boolean spinHits(Vec3 floor, double angle, Vec3 feet) {
		double dx = feet.x - floor.x, dz = feet.z - floor.z;
		double across = Math.abs(-Math.sin(angle) * dx + Math.cos(angle) * dz);
		return across <= SPIN_HALF_WIDTH && Math.hypot(dx, dz) <= RIM && feet.y - floor.y < CLEARANCE;
	}

	/** The shadow's fall: from the rim along its aim toward the fighter, or toward the centre if they stand on it. */
	public static Vec3 shadowAim(Vec3 foot, Vec3 fighter, Vec3 floor) {
		Vec3 aim = OrdealKit.flat(new Vec3(fighter.x - foot.x, 0.0D, fighter.z - foot.z));
		return horizontal(foot, fighter) < 1.0D || aim.lengthSqr() < 1.0E-6D ? OrdealKit.flat(floor.subtract(foot)) : aim;
	}

	public static boolean shadowHits(Vec3 foot, Vec3 aim, Vec3 feet) {
		double dx = feet.x - foot.x, dz = feet.z - foot.z;
		double along = dx * aim.x + dz * aim.z, across = Math.abs(dx * aim.z - dz * aim.x);
		return along >= 0.0D && along <= SHADOW_LENGTH && across <= SHADOW_HALF_WIDTH && feet.y - foot.y < 2.5D;
	}

	/** How far the shadow has tipped, in radians from upright, {@code since} ticks into its wind-up: a felled pole. */
	public static float shadowFall(float since) {
		float t = Math.min(1.0F, Math.max(0.0F, since / SHADOW_WINDUP));
		return (float) (Math.PI / 2.0D) * t * t * t;
	}

	// --- the Scarlet Mummer ----------------------------------------------------------------------------------------

	/** Whether the dazzle catches a fighter: within its reach and looking at it, the look within about 50 degrees. */
	public static boolean dazzleHits(Vec3 mummer, Vec3 eyes, Vec3 look) {
		if (horizontal(mummer, eyes) > DAZZLE_REACH) return false;
		Vec3 toward = mummer.subtract(eyes);
		if (toward.lengthSqr() < 1.0E-6D) return true;
		return look.normalize().dot(toward.normalize()) >= DAZZLE_LOOK;
	}

	public static boolean feintHits(Vec3 start, Vec3 aim, Vec3 feet) {
		double dx = feet.x - start.x, dz = feet.z - start.z;
		double along = dx * aim.x + dz * aim.z, across = Math.abs(dx * aim.z - dz * aim.x);
		return along >= -0.5D && along <= FEINT_LENGTH && across <= FEINT_HALF_WIDTH && Math.abs(feet.y - start.y) < 2.5D;
	}

	public static boolean whirlHits(Vec3 centre, Vec3 feet) {
		return horizontal(centre, feet) <= WHIRL_RADIUS && Math.abs(feet.y - centre.y) < 2.5D;
	}

	/** The leap's arc from {@code from} to {@code to}, {@code t} from 0 to 1. */
	public static Vec3 entranceArc(Vec3 from, Vec3 to, double t) {
		double u = HemoEasing.clamp01(t);
		return from.lerp(to, u).add(0.0D, ENTRANCE_HEIGHT * 4.0D * u * (1.0D - u), 0.0D);
	}

	public static boolean entranceHits(Vec3 mark, Vec3 feet) {
		return horizontal(mark, feet) <= ENTRANCE_RADIUS && feet.y - mark.y < 2.5D && feet.y >= mark.y - 0.5D;
	}

	/** A mark kept inside the ring, {@code margin} short of the rim. */
	public static Vec3 inside(Vec3 floor, Vec3 mark, double margin) {
		Vec3 out = new Vec3(mark.x - floor.x, 0.0D, mark.z - floor.z);
		double limit = RIM - margin;
		if (out.length() <= limit) return new Vec3(mark.x, floor.y, mark.z);
		Vec3 back = floor.add(out.normalize().scale(limit));
		return new Vec3(back.x, floor.y, back.z);
	}

	private static double horizontal(Vec3 a, Vec3 b) {
		return Math.hypot(a.x - b.x, a.z - b.z);
	}
}
