package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import java.util.List;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealHazardKinds;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealKit;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealMoves;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The Impaled Procession comes alive on his stage (2026-11-18): the copy carousel's three moves. It spins up and
 * flings its horses down three lanes; it spins hard and lets them sprint out on a spiral; it rides up its pole and
 * slams down in two jumpable rings. Its turn, the horses' reach, their drop toward the floor and the ride up the pole
 * are pure functions of the synced move and tick, so the server's hits and the client's horses agree. Every number is
 * the builder's starting value for the owner to tune.
 */
public final class CarouselAlive {
	public static final int KIND_HORSE = OrdealHazardKinds.RINGMASTER + 4, KIND_SPIRAL = KIND_HORSE + 1, KIND_SLAM = KIND_HORSE + 2;
	/** The idle turn between moves, degrees a tick. */
	public static final float IDLE_SPIN = 0.5F;
	/** The volley's spin-up: one full turn more than it needs to land horse 0 on the fighter. */
	public static final int VOLLEY_OUT = 4, VOLLEY_HOLD = 4, VOLLEY_BACK = 20;
	public static final float VOLLEY_DAMAGE = 6.0F;
	/** The spiral's spin: this many degrees across its wind-up, ending fastest. */
	public static final float SPIRAL_SPIN = 540.0F;
	public static final int SPIRAL_BACK = 16;
	public static final float SPIRAL_DAMAGE = 5.0F;
	/** The pole slam: it rides this high up its pole across the wind-up, drops in {@link #SLAM_DROP} ticks. */
	public static final double SLAM_RISE = 3.0D;
	public static final int SLAM_DROP = 2;
	public static final float SLAM_DAMAGE = 5.0F;
	/** How far the horses drop toward the floor at full reach, so their hooves run on it. */
	public static final double HORSE_DROP = 1.2D;

	/** The carousel's moves, with their wind-up, active and recovery spans. */
	public enum Move {
		NONE(null),
		HORSE_VOLLEY(new BossMoveTimeline(20, VOLLEY_OUT + VOLLEY_HOLD, VOLLEY_BACK)),
		HORSE_SPIRAL(new BossMoveTimeline(24, RingmasterFinaleGeometry.SPIRAL_TICKS, SPIRAL_BACK)),
		POLE_SLAM(new BossMoveTimeline(24, SLAM_DROP + RingmasterFinaleGeometry.SLAM_GAP + RingmasterFinaleGeometry.SLAM_TRAVEL, 12));

		private final BossMoveTimeline timeline;

		Move(BossMoveTimeline timeline) {
			this.timeline = timeline;
		}

		public BossMoveTimeline timeline() {
			return timeline;
		}

		public static Move byId(int id) {
			Move[] all = values();
			return id >= 0 && id < all.length ? all[id] : NONE;
		}
	}

	private CarouselAlive() {
	}

	// --- pure poses -------------------------------------------------------------------------------------------------

	/**
	 * The carousel's turn {@code t} ticks into {@code move}, from the turn {@code from} it started at, landing on
	 * {@code fire} where the move aims. Between moves {@code t} counts the idle ticks.
	 */
	public static float yaw(Move move, float t, float from, float fire) {
		return switch (move) {
			case NONE, POLE_SLAM -> from + IDLE_SPIN * t;
			case HORSE_VOLLEY -> {
				// One turn more than it needs, eased in and out, so it spins up hard and locks on the fighter.
				float windup = move.timeline().windup();
				if (t >= windup) yield fire;
				float span = 360.0F + Mth.positiveModulo(fire - from, 360.0F);
				yield fire - span * (1.0F - HemoEasing.smoothstep(t / windup));
			}
			case HORSE_SPIRAL -> {
				float windup = move.timeline().windup();
				if (t < windup) yield from + SPIRAL_SPIN * HemoEasing.easeInCubic(t / windup);
				yield from + SPIRAL_SPIN + RingmasterFinaleGeometry.SPIRAL_TURN * Math.min(t - windup, RingmasterFinaleGeometry.SPIRAL_TICKS);
			}
		};
	}

	/** Where a spiral that starts from {@code from} lets its horses go. */
	public static float spiralFire(float from) {
		return from + SPIRAL_SPIN;
	}

	/** How far out the horses stand from the hub, {@code t} ticks into {@code move}. */
	public static double reach(Move move, float t) {
		double rest = CircusCarouselRules.HORSE_RADIUS;
		switch (move) {
			case HORSE_VOLLEY -> {
				float out = t - move.timeline().windup();
				if (out < 0.0F) return rest;
				if (out < VOLLEY_OUT) return HemoEasing.lerp(rest, RingmasterFinaleGeometry.HORSE_LANE, HemoEasing.easeOutCubic(out / VOLLEY_OUT));
				float back = out - VOLLEY_OUT - VOLLEY_HOLD;
				if (back < 0.0F) return RingmasterFinaleGeometry.HORSE_LANE;
				return HemoEasing.lerp(RingmasterFinaleGeometry.HORSE_LANE, rest, HemoEasing.smoothstep(HemoEasing.clamp01(back / VOLLEY_BACK)));
			}
			case HORSE_SPIRAL -> {
				float out = t - move.timeline().windup();
				if (out < 0.0F) return rest;
				if (out <= RingmasterFinaleGeometry.SPIRAL_TICKS) return RingmasterFinaleGeometry.spiralRadius(out);
				float back = out - RingmasterFinaleGeometry.SPIRAL_TICKS;
				return HemoEasing.lerp(RingmasterFinaleGeometry.SPIRAL_TO, rest, HemoEasing.smoothstep(HemoEasing.clamp01(back / SPIRAL_BACK)));
			}
			default -> {
				return rest;
			}
		}
	}

	/** How far the horses have dropped toward the floor at {@code reach}: none on the carousel, their hooves down at full reach. */
	public static double drop(double reach) {
		double rest = CircusCarouselRules.HORSE_RADIUS;
		return HORSE_DROP * HemoEasing.clamp01((reach - rest) / (RingmasterFinaleGeometry.HORSE_LANE - rest));
	}

	/** How far the carousel has ridden up its pole: rising across the slam's wind-up, then dropping in two ticks. */
	public static double lift(Move move, float t) {
		if (move != Move.POLE_SLAM) return 0.0D;
		float windup = move.timeline().windup();
		if (t < windup) return SLAM_RISE * HemoEasing.easeOutCubic(t / windup);
		float down = t - windup;
		if (down < SLAM_DROP) return SLAM_RISE * (1.0D - HemoEasing.easeInCubic(down / SLAM_DROP));
		// A short bounce off the floor as the weight lands.
		float after = down - SLAM_DROP;
		return after < 6.0F ? 0.25D * Math.sin(after / 6.0F * Math.PI) : 0.0D;
	}

	/** The tick the slam lands on, from the move's start. */
	public static int slamAt() {
		return Move.POLE_SLAM.timeline().windup() + SLAM_DROP;
	}

	// --- the moves on the server ------------------------------------------------------------------------------------

	/**
	 * One tick of the carousel's move {@code move} at {@code t}: its telegraphs from the first tick, then the blow.
	 * {@code by} is the Ringmaster's copy, who names every hit. True when the move is over.
	 */
	static boolean run(ServerLevel chamber, ServerPlayer player, CircusRingmasterEntity by, CircusCarouselEntity wheel,
			Move move, int t, RingmasterFinaleMoves.State state) {
		Vec3 hub = new Vec3(wheel.getX(), state.floor.y, wheel.getZ());
		Vec3 feet = player.position();
		switch (move) {
			case HORSE_VOLLEY -> volley(chamber, player, by, wheel, hub, feet, t, state);
			case HORSE_SPIRAL -> spiral(chamber, player, by, wheel, hub, feet, t, state);
			case POLE_SLAM -> slam(chamber, player, by, wheel, hub, feet, t, state);
			default -> {
				return true;
			}
		}
		return t >= move.timeline().total();
	}

	private static void volley(ServerLevel chamber, ServerPlayer player, CircusRingmasterEntity by, CircusCarouselEntity wheel,
			Vec3 hub, Vec3 feet, int t, RingmasterFinaleMoves.State state) {
		int windup = Move.HORSE_VOLLEY.timeline().windup();
		float fire = wheel.aliveFireYaw();
		List<Vec3> ends = RingmasterFinaleGeometry.horseLanes(hub, fire);
		if (t == 0) {
			for (Vec3 end : ends) lane(chamber, by, KIND_HORSE, hub, end, RingmasterFinaleGeometry.HORSE_HALF, windup);
			chamber.playSound(null, hub.x, hub.y + 2.0D, hub.z, SoundEvents.CHAIN_HIT, SoundSource.HOSTILE, 2.5F, 0.55F);
		}
		if (t < windup) {
			// The spin-up: the hub groaning faster, the horses' blood shaken loose.
			if (t % 4 == 0) chamber.playSound(null, hub.x, hub.y + 2.0D, hub.z, SoundEvents.CHAIN_HIT, SoundSource.HOSTILE,
					1.6F, 0.6F + t / (float) windup);
			if (t % 2 == 0) horseGlow(chamber, wheel, Move.HORSE_VOLLEY, t);
			return;
		}
		if (t == windup) {
			for (Vec3 end : ends) lane(chamber, by, KIND_HORSE, hub, end, RingmasterFinaleGeometry.HORSE_HALF, VOLLEY_OUT);
			chamber.playSound(null, hub.x, hub.y + 2.0D, hub.z, SoundInit.ENTITY_IMPALED_PROCESSION_ANCHOR_BREAK.get(),
					SoundSource.HOSTILE, 3.0F, 0.7F);
		}
		int out = t - windup;
		if (out <= VOLLEY_OUT) {
			double reach = reach(Move.HORSE_VOLLEY, t);
			for (int horse = 0; horse < 3; horse++) {
				Vec3 at = RingmasterFinaleGeometry.horseAt(hub, fire, horse, reach);
				CircusEffects.dust(chamber, at, 4);
				if (!state.landed && RingmasterFinaleGeometry.laneHits(hub, at, RingmasterFinaleGeometry.HORSE_HALF, feet)) {
					state.landed = true;
					OrdealKit.strike(chamber, player, by, VOLLEY_DAMAGE, feet.subtract(hub), 1.1D, 0.45D);
					chamber.playSound(null, feet.x, feet.y, feet.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F, 0.8F);
				}
			}
			if (out == VOLLEY_OUT) for (Vec3 end : ends) {
				CircusEffects.impact(chamber, end.add(0.0D, 0.4D, 0.0D), 1.4D);
				chamber.playSound(null, end.x, end.y, end.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.5F, 0.75F);
			}
		}
		if (out == VOLLEY_OUT + VOLLEY_HOLD)
			chamber.playSound(null, hub.x, hub.y + 2.0D, hub.z, SoundEvents.CHAIN_PLACE, SoundSource.HOSTILE, 2.0F, 0.6F);
	}

	private static void spiral(ServerLevel chamber, ServerPlayer player, CircusRingmasterEntity by, CircusCarouselEntity wheel,
			Vec3 hub, Vec3 feet, int t, RingmasterFinaleMoves.State state) {
		int windup = Move.HORSE_SPIRAL.timeline().windup();
		float fire = spiralFire(wheel.aliveFromYaw());
		int step = 4;
		if (t == 0) {
			// Each horse's run, drawn from the first tick: the arms of the spiral it will sprint out along.
			for (int horse = 0; horse < 3; horse++)
				for (int k = 0; k < RingmasterFinaleGeometry.SPIRAL_TICKS; k += step) {
					Vec3 a = RingmasterFinaleGeometry.spiralPoint(hub, fire, horse, k);
					Vec3 b = RingmasterFinaleGeometry.spiralPoint(hub, fire, horse, k + step);
					lane(chamber, by, KIND_SPIRAL, a, b, RingmasterFinaleGeometry.SPIRAL_DISC, windup + k);
				}
			chamber.playSound(null, hub.x, hub.y + 2.0D, hub.z, SoundEvents.CHAIN_HIT, SoundSource.HOSTILE, 2.5F, 0.5F);
		}
		if (t < windup) {
			if (t % 3 == 0) chamber.playSound(null, hub.x, hub.y + 2.0D, hub.z, SoundEvents.CHAIN_HIT, SoundSource.HOSTILE,
					1.6F, 0.7F + 1.1F * t / windup);
			if (t % 2 == 0) horseGlow(chamber, wheel, Move.HORSE_SPIRAL, t);
			return;
		}
		int run = t - windup;
		if (run == 0) chamber.playSound(null, hub.x, hub.y + 2.0D, hub.z, SoundInit.ENTITY_IMPALED_PROCESSION_ANCHOR_BREAK.get(),
				SoundSource.HOSTILE, 3.0F, 0.9F);
		if (run > RingmasterFinaleGeometry.SPIRAL_TICKS) return;
		for (int horse = 0; horse < 3; horse++) {
			Vec3 at = RingmasterFinaleGeometry.spiralPoint(hub, fire, horse, run);
			if (run % step == 0 && run < RingmasterFinaleGeometry.SPIRAL_TICKS)
				lane(chamber, by, KIND_SPIRAL, at, RingmasterFinaleGeometry.spiralPoint(hub, fire, horse, run + step),
						RingmasterFinaleGeometry.SPIRAL_DISC, 4);
			if (run % 2 == 0) CircusEffects.dust(chamber, at, 3);
			if (!state.landed && RingmasterFinaleGeometry.discHits(at, RingmasterFinaleGeometry.SPIRAL_DISC, feet)) {
				state.landed = true;
				OrdealKit.strike(chamber, player, by, SPIRAL_DAMAGE, feet.subtract(at), 0.9D, 0.35D);
				chamber.playSound(null, feet.x, feet.y, feet.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.0F, 0.9F);
			}
		}
		if (run % 8 == 0) chamber.playSound(null, hub.x, hub.y, hub.z, SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 2.0F, 0.6F);
	}

	private static void slam(ServerLevel chamber, ServerPlayer player, CircusRingmasterEntity by, CircusCarouselEntity wheel,
			Vec3 hub, Vec3 feet, int t, RingmasterFinaleMoves.State state) {
		int windup = Move.POLE_SLAM.timeline().windup();
		int land = slamAt();
		if (t == 0) {
			// Where the two waves start and where they die: both rims smoulder through the rise.
			ring(chamber, by, hub, RingmasterFinaleGeometry.SLAM_FROM, RingmasterFinaleGeometry.SLAM_FROM, land);
			ring(chamber, by, hub, RingmasterFinaleGeometry.SLAM_TO, RingmasterFinaleGeometry.SLAM_TO, land);
			chamber.playSound(null, hub.x, hub.y + 2.0D, hub.z, SoundEvents.CHAIN_FALL, SoundSource.HOSTILE, 2.5F, 0.5F);
		}
		if (t < windup) {
			if (t % 3 == 0) chamber.playSound(null, hub.x, hub.y + 4.0D, hub.z, SoundEvents.CHAIN_STEP, SoundSource.HOSTILE,
					2.0F, 0.5F + 0.5F * t / windup);
			if (t % 2 == 0) chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), hub.x, hub.y + 0.2D, hub.z, 6,
					1.2D, 0.05D, 1.2D, 0.02D);
			return;
		}
		for (int wave = 0; wave < 2; wave++) {
			int start = land + wave * RingmasterFinaleGeometry.SLAM_GAP;
			if (t == start) {
				ring(chamber, by, hub, RingmasterFinaleGeometry.SLAM_FROM, RingmasterFinaleGeometry.SLAM_TO,
						RingmasterFinaleGeometry.SLAM_TRAVEL);
				chamber.playSound(null, hub.x, hub.y, hub.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 4.0F,
						wave == 0 ? 0.6F : 0.75F);
				if (wave == 0) {
					chamber.playSound(null, hub.x, hub.y, hub.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.0F, 0.6F);
					CircusEffects.collapse(chamber, hub);
				}
			}
			double radius = RingmasterFinaleGeometry.slamRadius(t - start);
			if (radius < 0.0D) continue;
			if ((t - start) % 2 == 0) BossTelegraphs.ring(chamber, hub, radius, CircusEffects.CRIMSON, 28);
			if ((state.hitMask & (1 << wave)) == 0 && RingmasterFinaleGeometry.ringHits(hub, radius,
					RingmasterFinaleGeometry.SLAM_BAND, feet, OrdealMoves.CLEARANCE)) {
				state.hitMask |= 1 << wave;
				OrdealKit.strike(chamber, player, by, SLAM_DAMAGE, feet.subtract(hub), 0.7D, 0.5D);
			}
		}
	}

	/** Blood shaken off the horses while the carousel spins up. */
	private static void horseGlow(ServerLevel chamber, CircusCarouselEntity wheel, Move move, int t) {
		float yaw = wheel.getYRot();
		for (int horse = 0; horse < 3; horse++) {
			CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(yaw, horse);
			chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), wheel.getX() + pose.x(),
					wheel.getY() + 2.4D, wheel.getZ() + pose.z(), 1 + t / 8, 0.3D, 0.3D, 0.3D, 0.08D);
		}
	}

	/** A floor lane from {@code from} to {@code to}, drawn by the ordeal pass (a smoulder, or burning once short-lived). */
	static void lane(ServerLevel chamber, CircusRingmasterEntity by, int kind, Vec3 from, Vec3 to, double halfWidth, int life) {
		double length = Math.hypot(to.x - from.x, to.z - from.z);
		if (length < 1.0E-3D) return;
		OrdealKit.hazard(chamber, by, kind, new BossFootprint.Lane(length, halfWidth, 0, 0.0D, 0.0D, 0.0D, 0,
				BossFootprint.Height.STANDING), from, to, life);
	}

	private static void ring(ServerLevel chamber, CircusRingmasterEntity by, Vec3 hub, double from, double to, int ticks) {
		OrdealKit.hazard(chamber, by, KIND_SLAM, new BossFootprint.Ring(from, to, RingmasterFinaleGeometry.SLAM_BAND, 1, 0,
				ticks, BossFootprint.Height.JUMPABLE), hub, hub, ticks + 1);
	}
}
