package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.phys.Vec3;

/**
 * The Retired Understudy's ordeal as pure geometry (2026-11-17): what each of their interventions from the wings and
 * each of the Mnemonist Puppet's moves hits, so every one can be read and dodged. Positions are feet; {@code floor} is
 * the top of the ring's plate at its centre.
 */
public final class UnderstudyOrdealGeometry {
	/** The spotlight's pool, how fast it hunts the fighter while it follows, and how high off the boards it burns. */
	public static final double SPOT_RADIUS = 1.8D, SPOT_SPEED = 0.24D, SPOT_HEIGHT = 3.0D;
	/** The curtain: how far it runs from the wings' rim to the far rim, how thick it is, and its parting. */
	public static final double CURTAIN_REACH = 9.5D, CURTAIN_HALF = 0.6D, PARTING_HALF = 1.25D, PARTING_SPAN = 6.0D;
	/** The curtain is a wall: no jump clears it. */
	public static final double CURTAIN_HEIGHT = 4.0D;
	public static final int CURTAIN_TICKS = 40;
	/** A reprise mark, burst where the fighter stood. */
	public static final double REPRISE_RADIUS = 1.4D;
	/** A trapdoor, and how far from the fighter the three others open. */
	public static final double TRAP_RADIUS = 1.5D, TRAP_SPREAD = 3.5D;
	/** The ring, and how far inside it a footprint's centre is kept. */
	public static final double RING = OrdealStage.RING_RADIUS;
	/** Feet this far above the floor clear a jumpable wave. */
	public static final double CLEARANCE = 1.1D;

	/** The puppet's recital: three blows walked forward, each a disc this far ahead of its feet. */
	public static final double RECITAL_FIRST = 1.6D, RECITAL_STEP = 1.0D, RECITAL_RADIUS = 1.4D;
	/** Its thread cast: three threads fanned this many degrees apart, this long, this thick. */
	public static final double THREAD_FAN = 28.0D, THREAD_LENGTH = 7.0D, THREAD_HALF = 0.55D;
	/** Its echo: a ring wave out to this radius over {@link #ECHO_TRAVEL} ticks, twice. */
	public static final double ECHO_RADIUS = 6.0D, ECHO_START = 0.8D, ECHO_BAND = 1.2D;
	public static final int ECHO_TRAVEL = 9;
	/** Its lunge: speed, run and the lane's half width. */
	public static final double LUNGE_SPEED = 0.95D, LUNGE_HALF = 1.0D;
	public static final int LUNGE_RUN = 8;
	public static final double LUNGE_LENGTH = LUNGE_SPEED * LUNGE_RUN;

	private UnderstudyOrdealGeometry() {
	}

	// --- the Understudy's interventions --------------------------------------------------------------------------

	/** The spotlight hunting the fighter: a step toward them of at most {@link #SPOT_SPEED}, kept inside the ring. */
	public static Vec3 spotlightStep(Vec3 light, Vec3 feet, Vec3 floor) {
		Vec3 toward = new Vec3(feet.x - light.x, 0.0D, feet.z - light.z);
		double length = toward.length();
		Vec3 next = length <= SPOT_SPEED ? new Vec3(feet.x, light.y, feet.z) : light.add(toward.scale(SPOT_SPEED / length));
		return inRing(next, floor, RING - 1.0D);
	}

	public static boolean spotlightHits(Vec3 light, Vec3 feet) {
		return horizontal(light, feet) <= SPOT_RADIUS && feet.y - light.y < SPOT_HEIGHT && feet.y >= light.y - 0.5D;
	}

	/** Where the curtain stands along z from the ring's centre, {@code ticks} into its sweep: from the wings' rim (-z) to the far rim. */
	public static double curtainOffset(int ticks) {
		return -CURTAIN_REACH + 2.0D * CURTAIN_REACH * Math.min(1.0D, Math.max(0, ticks) / (double) CURTAIN_TICKS);
	}

	/**
	 * Whether the curtain at {@code along} takes the fighter: on its line, inside the ring, and not standing in the
	 * parting at {@code parting} (an x offset from the centre). No jump clears it.
	 */
	public static boolean curtainHits(Vec3 floor, double along, double parting, Vec3 feet) {
		return Math.abs(feet.z - (floor.z + along)) <= CURTAIN_HALF && Math.abs(feet.x - (floor.x + parting)) > PARTING_HALF
				&& horizontal(floor, feet) <= RING && feet.y - floor.y < CURTAIN_HEIGHT;
	}

	/** The parting's x offset for a seed in [0, 1): somewhere in the middle of the ring, never at its rim. */
	public static double parting(double unit) {
		return (unit * 2.0D - 1.0D) * PARTING_SPAN;
	}

	/** The curtain's two halves at {@code along}, either side of the parting: each a [from, to] pair of x offsets. */
	public static double[][] curtainHalves(double parting) {
		return new double[][]{{-CURTAIN_REACH, parting - PARTING_HALF}, {parting + PARTING_HALF, CURTAIN_REACH}};
	}

	public static boolean repriseHits(Vec3 mark, Vec3 feet) {
		return horizontal(mark, feet) <= REPRISE_RADIUS && feet.y - mark.y < 2.5D && feet.y >= mark.y - 0.5D;
	}

	/**
	 * The four trapdoors: one under the fighter, three round them at {@link #TRAP_SPREAD} a third of a turn apart from
	 * {@code turn} radians, each kept inside the ring.
	 */
	public static List<Vec3> trapdoors(Vec3 feet, Vec3 floor, double turn) {
		List<Vec3> doors = new ArrayList<>(4);
		Vec3 at = new Vec3(feet.x, floor.y, feet.z);
		doors.add(inRing(at, floor, RING - TRAP_RADIUS));
		for (int i = 0; i < 3; i++) {
			double a = turn + i * Math.PI * 2.0D / 3.0D;
			doors.add(inRing(at.add(Math.cos(a) * TRAP_SPREAD, 0.0D, Math.sin(a) * TRAP_SPREAD), floor, RING - TRAP_RADIUS));
		}
		return doors;
	}

	/** A trapdoor takes whoever is standing on it or over it when it drops: a jump does not carry you off it. */
	public static boolean trapdoorHits(Vec3 door, Vec3 feet) {
		return horizontal(door, feet) <= TRAP_RADIUS && feet.y - door.y < 2.5D && feet.y >= door.y - 0.5D;
	}

	// --- the Mnemonist Puppet's moves ----------------------------------------------------------------------------

	/** Where the recital's {@code blow} (0 to 2) lands: walked a step further forward each time. */
	public static Vec3 recitalBlow(Vec3 feet, Vec3 aim, int blow) {
		return feet.add(flat(aim).scale(RECITAL_FIRST + RECITAL_STEP * blow));
	}

	public static boolean recitalHits(Vec3 blow, Vec3 feet) {
		return horizontal(blow, feet) <= RECITAL_RADIUS && feet.y - blow.y < 2.5D && feet.y >= blow.y - 0.5D;
	}

	/** The thread cast's three directions: the aim and the aim turned {@link #THREAD_FAN} degrees either way. */
	public static List<Vec3> threads(Vec3 aim) {
		Vec3 a = flat(aim);
		double fan = Math.toRadians(THREAD_FAN);
		return List.of(a.yRot((float) fan), a, a.yRot((float) -fan));
	}

	/** Whether a thread from {@code origin} along {@code direction} catches the fighter. */
	public static boolean threadHits(Vec3 origin, Vec3 direction, Vec3 feet) {
		Vec3 d = flat(direction), to = new Vec3(feet.x - origin.x, 0.0D, feet.z - origin.z);
		double along = to.dot(d);
		if (along < 0.0D || along > THREAD_LENGTH) return false;
		double across = Math.abs(to.x * d.z - to.z * d.x);
		return across <= THREAD_HALF + 0.3D && feet.y - origin.y < 2.5D && feet.y >= origin.y - 0.5D;
	}

	/** The echo's wave radius {@code ticks} after it is let go. */
	public static double echoRadius(int ticks) {
		return ECHO_START + (ECHO_RADIUS - ECHO_START) * Math.min(1.0D, Math.max(0, ticks) / (double) ECHO_TRAVEL);
	}

	public static boolean echoHits(Vec3 centre, double wave, Vec3 feet) {
		return Math.abs(horizontal(centre, feet) - wave) <= ECHO_BAND / 2.0D + 0.3D && feet.y - centre.y < CLEARANCE;
	}

	/** The lunge's next step, or null where it would carry the puppet past {@code limit} from the ring's centre. */
	public static Vec3 lungeStep(Vec3 at, Vec3 aim, Vec3 floor, double limit) {
		Vec3 next = at.add(flat(aim).scale(LUNGE_SPEED));
		return horizontal(next, floor) > limit ? null : next;
	}

	// --- shared ----------------------------------------------------------------------------------------------------

	static Vec3 inRing(Vec3 at, Vec3 floor, double limit) {
		Vec3 out = new Vec3(at.x - floor.x, 0.0D, at.z - floor.z);
		if (out.length() <= limit) return at;
		Vec3 back = out.normalize().scale(limit);
		return new Vec3(floor.x + back.x, at.y, floor.z + back.z);
	}

	static Vec3 flat(Vec3 v) {
		Vec3 f = new Vec3(v.x, 0.0D, v.z);
		return f.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : f.normalize();
	}

	static double horizontal(Vec3 a, Vec3 b) {
		return Math.hypot(a.x - b.x, a.z - b.z);
	}
}
