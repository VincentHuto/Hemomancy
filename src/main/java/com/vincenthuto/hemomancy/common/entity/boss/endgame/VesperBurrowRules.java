package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The Burrow's timeline. The mount dives, hunts under the floor surfacing to strike, goes quiet while the ground
 * rumbles, erupts beneath the locked point and then needs time to gather itself. Every tick of the cycle is a pure
 * function of (plan, tick) so saves, reloads and lost targets resolve deterministically.
 */
public final class VesperBurrowRules {
	public static final int DIVE_TICKS = 24;
	public static final int STRIKE_TELL_TICKS = 12;
	public static final int STRIKE_TICKS = 8;
	public static final int STRIKE_INTERVAL = 50;
	public static final int RUMBLE_TICKS = 36;
	public static final int RUMBLE_LOCK_TICK = 22;
	public static final int ERUPTION_TICKS = 10;
	public static final int RECOVERY_TICKS = 70;
	public static final double TRAIL_SPEED = 0.42D;
	/** How deep the hidden mount sits while it tunnels; during a strike only its claws or tail clear the floor. */
	public static final double SUBMERGED_DEPTH = 6.0D;
	public static final double STRIKE_DEPTH = 4.5D;
	public static final float ERUPTION_DAMAGE = 22.0F;
	public static final double ERUPTION_LAUNCH = 1.6D;
	public static final double STRIKE_RADIUS = 1.6D;
	public static final double ERUPTION_RADIUS = 2.5D;

	/** Claws rake through armor; the tail's sting poisons and opens the blood. */
	public record StrikeEffect(float damage, boolean armorPiercing, int poisonTicks, int bloodLossTicks) {
	}

	public static StrikeEffect effects(Strike strike) {
		return switch (strike) {
			case CLAW -> new StrikeEffect(9.0F, true, 0, 0);
			case TAIL -> new StrikeEffect(6.0F, false, VesperMountAttackRules.POISON_TICKS,
					VesperMountAttackRules.BLOOD_LOSS_TICKS);
		};
	}

	public enum Stage { DIVE, HUNT, STRIKE_TELL, STRIKE, RUMBLE, ERUPTION, RECOVERY, COMPLETE }

	public enum Strike { CLAW, TAIL }

	public record Plan(int huntTicks, boolean fromDive) {
		/** Ten to fifteen seconds of hunting from the ground. */
		public static Plan ground(RandomSource random) {
			return new Plan(200 + random.nextInt(101), false);
		}

		/** Five to seven seconds when a dive drives straight into the floor. */
		public static Plan dive(RandomSource random) {
			return new Plan(100 + random.nextInt(41), true);
		}
	}

	/** Where the burrow is: tick since the dive, the trail's position and the eruption lock. */
	public record State(int tick, Plan plan, double x, double z, double lockX, double lockZ) {
		public void save(CompoundTag tag) {
			CompoundTag burrow = new CompoundTag();
			burrow.putInt("Tick", tick);
			burrow.putInt("Hunt", plan.huntTicks());
			burrow.putBoolean("FromDive", plan.fromDive());
			burrow.putDouble("X", x);
			burrow.putDouble("Z", z);
			burrow.putDouble("LockX", lockX);
			burrow.putDouble("LockZ", lockZ);
			tag.put("VesperBurrow", burrow);
		}

		@Nullable
		public static State load(CompoundTag tag) {
			if (!tag.contains("VesperBurrow")) return null;
			CompoundTag burrow = tag.getCompound("VesperBurrow");
			return new State(Math.max(0, burrow.getInt("Tick")),
					new Plan(Mth.clamp(burrow.getInt("Hunt"), 0, 300), burrow.getBoolean("FromDive")),
					burrow.getDouble("X"), burrow.getDouble("Z"), burrow.getDouble("LockX"), burrow.getDouble("LockZ"));
		}

		public State withTick(int nextTick) {
			return new State(nextTick, plan, x, z, lockX, lockZ);
		}

		public State withTrail(double nextX, double nextZ) {
			return new State(tick, plan, nextX, nextZ, lockX, lockZ);
		}

		public State withLock(double nextX, double nextZ) {
			return new State(tick, plan, x, z, nextX, nextZ);
		}
	}

	private VesperBurrowRules() {
	}

	public static Strike strikeAt(int index) {
		return (index & 1) == 0 ? Strike.CLAW : Strike.TAIL;
	}

	public static int rumbleStart(Plan plan) {
		return DIVE_TICKS + plan.huntTicks();
	}

	public static int lockTick(Plan plan) {
		return rumbleStart(plan) + RUMBLE_LOCK_TICK;
	}

	public static int eruptionStart(Plan plan) {
		return rumbleStart(plan) + RUMBLE_TICKS;
	}

	public static int recoveryStart(Plan plan) {
		return eruptionStart(plan) + ERUPTION_TICKS;
	}

	public static int total(Plan plan) {
		return recoveryStart(plan) + RECOVERY_TICKS;
	}

	public static Stage stageAt(Plan plan, int tick) {
		if (tick < DIVE_TICKS) return Stage.DIVE;
		if (tick < rumbleStart(plan)) {
			int local = tick - DIVE_TICKS;
			// Only whole intervals carry a strike, so a tell is never left hanging without its strike.
			if (local >= (plan.huntTicks() / STRIKE_INTERVAL) * STRIKE_INTERVAL) return Stage.HUNT;
			int cycle = local % STRIKE_INTERVAL;
			if (cycle >= STRIKE_INTERVAL - STRIKE_TICKS) return Stage.STRIKE;
			if (cycle >= STRIKE_INTERVAL - STRIKE_TICKS - STRIKE_TELL_TICKS) return Stage.STRIKE_TELL;
			return Stage.HUNT;
		}
		if (tick < eruptionStart(plan)) return Stage.RUMBLE;
		if (tick < recoveryStart(plan)) return Stage.ERUPTION;
		if (tick < total(plan)) return Stage.RECOVERY;
		return Stage.COMPLETE;
	}

	/** The strike in progress (or being told) at this tick; only meaningful inside the hunt. */
	public static int strikeIndexAt(int tick) {
		return Math.max(0, tick - DIVE_TICKS) / STRIKE_INTERVAL;
	}

	/** The first tick of the strike itself, after its tell, for the strike being told at this tick. */
	public static boolean isStrikeImpact(Plan plan, int tick) {
		return stageAt(plan, tick) == Stage.STRIKE && stageAt(plan, tick - 1) != Stage.STRIKE;
	}

	/** A reload mid-burrow never resumes an unseen hunt: the mount surfaces where it was and gathers itself. */
	public static State afterReload(State state) {
		Stage stage = stageAt(state.plan(), state.tick());
		if (stage == Stage.RECOVERY || stage == Stage.COMPLETE) return state;
		return state.withTick(recoveryStart(state.plan()));
	}

	/** With nothing left to hunt, the trail goes quiet at once and erupts where it stands. */
	public static State onTargetLost(State state) {
		if (state.tick() >= rumbleStart(state.plan())) return state;
		return state.withTick(rumbleStart(state.plan())).withLock(state.x(), state.z());
	}

	public static Vec3 clampTrail(Vec3 point, double centerX, double floorY, double centerZ) {
		VesperWingedFlightRules.Point clamped = VesperWingedFlightRules.clampGroundPoint(
				point.x, point.z, centerX, floorY, centerZ);
		return new Vec3(clamped.x(), clamped.y(), clamped.z());
	}
}
