package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowJugglerRules;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLock;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * The Marrow Juggler's moves in the Dagger Thrower's ordeal (2026-11-17). Between its ordinary three-dagger volleys it
 * tosses its marrow bones up to rain on a line across the fighter, sends a pin volley down a lane (the third dagger
 * pins), and climbs over the fighter to dive and whirl its six blades on them. Each is drawn on the floor by the
 * ordeal's bold pass from the first tick of its wind-up, posed by the true Juggler's model from the same entity event,
 * and landed here.
 */
public final class MarrowJugglerOrdealMoves {
	public static final int HAIL = 0, VOLLEY = 1, WHIRL = 2;
	/** Wind-ups, in ticks. The hail's bones leave its hands at {@link #HAIL_TOSS}. */
	public static final int HAIL_WINDUP = 18, HAIL_TOSS = 8, VOLLEY_WINDUP = 14, WHIRL_WINDUP = 18;
	/** The volley's three throws apart; the whirl's dive and spin. */
	public static final int VOLLEY_GAP = 4, WHIRL_DIVE = 4, WHIRL_SPIN = 10, WHIRL_RECOVER = 18;
	private static final int[] LENGTH = {HAIL_WINDUP + 16, VOLLEY_WINDUP + 2 * VOLLEY_GAP + 16,
			WHIRL_WINDUP + WHIRL_DIVE + WHIRL_SPIN + WHIRL_RECOVER};
	public static final double HAIL_RADIUS = 1.6D, HAIL_SPREAD = 2.8D;
	public static final double VOLLEY_LENGTH = 14.0D, VOLLEY_HALF_WIDTH = 0.75D, VOLLEY_HEIGHT = 2.4D;
	public static final double WHIRL_RADIUS = 2.6D, WHIRL_HEIGHT = 4.5D, WHIRL_LOW = 0.6D;
	private static final float HAIL_DAMAGE = 4.0F, VOLLEY_DAMAGE = 3.0F, WHIRL_DAMAGE = 6.0F;
	/** Quiet ticks between moves, when its ordinary volley carries the fight. */
	private static final int REST = 30;
	private static final Map<UUID, State> STATES = new HashMap<>();

	/** The Juggler's moves as its ordeal's {@link OrdealPuppetMoves}. */
	public static final OrdealPuppetMoves MOVES = new OrdealPuppetMoves() {
		@Override
		public void tick(ServerLevel chamber, ServerPlayer caster, Mob puppet, Vec3 floor) {
			if (puppet instanceof MarrowSpitterEntity juggler) MarrowJugglerOrdealMoves.tick(chamber, caster, juggler, floor);
		}

		@Override
		public void forget(UUID puppet) {
			MarrowJugglerOrdealMoves.forget(puppet);
		}

		@Override
		public void clearSessionState() {
			MarrowJugglerOrdealMoves.clearSessionState();
		}

		@Override
		public boolean holding(UUID puppet, long now) {
			return MarrowJugglerOrdealMoves.holding(puppet, now);
		}
	};

	private MarrowJugglerOrdealMoves() {
	}

	private static final class State {
		int move = -1;
		long startedAt;
		long nextAt;
		/** The fighter is pinned (by its volley or by the Dagger Thrower's blade) until this tick. */
		long pinnedUntil;
		Vec3 aim = Vec3.ZERO;
		Vec3 mark = Vec3.ZERO;
		Vec3 from = Vec3.ZERO;
		boolean landed;
	}

	static void tick(ServerLevel chamber, ServerPlayer caster, MarrowSpitterEntity juggler, Vec3 floor) {
		long now = chamber.getGameTime();
		State state = STATES.computeIfAbsent(juggler.getUUID(), ignored -> fresh(now + 50));
		juggler.holdToRing(floor);
		OrdealKit.keepInRing(juggler, floor, OrdealStage.RING_RADIUS - 1.0D);
		if (state.move < 0) {
			if (now < state.nextAt) return;
			start(chamber, caster, juggler, state, floor, now);
			return;
		}
		int age = (int) (now - state.startedAt);
		switch (state.move) {
			case HAIL -> hail(chamber, caster, juggler, state, age, floor);
			case VOLLEY -> volley(chamber, caster, juggler, state, age, floor, now);
			case WHIRL -> whirl(chamber, caster, juggler, state, age, floor);
			default -> {
			}
		}
		if (age >= LENGTH[state.move]) {
			state.move = -1;
			state.nextAt = now + REST;
			juggler.ordealMove(-1);
		}
	}

	private static State fresh(long nextAt) {
		State state = new State();
		state.nextAt = nextAt;
		return state;
	}

	/** Picks by range: from afar a volley or the hail, close in the whirl or the hail. */
	private static void start(ServerLevel chamber, ServerPlayer caster, MarrowSpitterEntity juggler, State state, Vec3 floor,
			long now) {
		double distance = OrdealKit.horizontal(juggler.position(), caster.position());
		int roll = (int) Math.floorMod(now / 7 + caster.getUUID().getLeastSignificantBits(), 2);
		int move = distance > 7.0D ? new int[]{VOLLEY, HAIL}[roll] : distance < 4.0D ? new int[]{WHIRL, HAIL}[roll]
				: new int[]{VOLLEY, WHIRL}[roll];
		state.move = move;
		state.startedAt = now;
		state.landed = false;
		state.from = new Vec3(juggler.getX(), floor.y, juggler.getZ());
		state.mark = new Vec3(caster.getX(), floor.y, caster.getZ());
		state.aim = OrdealKit.flat(state.mark.subtract(state.from));
		if (state.aim.lengthSqr() < 1.0E-4D) state.aim = Vec3.directionFromRotation(0, juggler.getYRot());
		OrdealKit.face(juggler, state.aim);
		juggler.getNavigation().stop();
		juggler.ordealMove(move);
		switch (move) {
			case HAIL -> {
				for (int k = 0; k < 3; k++)
					OrdealKit.hazard(chamber, juggler, KnifeOrdealMoves.KIND_HAIL, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
							HAIL_RADIUS, BossFootprint.Height.STANDING), hailSpot(state, k), hailSpot(state, k), HAIL_WINDUP + 6);
			}
			case VOLLEY -> OrdealKit.hazard(chamber, juggler, KnifeOrdealMoves.KIND_VOLLEY, new BossFootprint.Lane(VOLLEY_LENGTH,
					VOLLEY_HALF_WIDTH, 0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING), state.from, state.from.add(state.aim),
					VOLLEY_WINDUP + 2 * VOLLEY_GAP + 2);
			case WHIRL -> OrdealKit.hazard(chamber, juggler, KnifeOrdealMoves.KIND_WHIRL, new BossFootprint.Disc(
					BossFootprint.Anchor.ORIGIN, WHIRL_RADIUS, BossFootprint.Height.STANDING), state.mark, state.mark,
					WHIRL_WINDUP + WHIRL_DIVE + 6);
			default -> {
			}
		}
		chamber.playSound(null, juggler.getX(), juggler.getY() + 1.0D, juggler.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(),
				SoundSource.HOSTILE, 1.6F, 1.25F);
	}

	/** The hail's three marks: a line across the fighter, square to the Juggler's line to them. */
	private static Vec3 hailSpot(State state, int k) {
		Vec3 across = new Vec3(-state.aim.z, 0.0D, state.aim.x);
		return state.mark.add(across.scale((k - 1) * HAIL_SPREAD));
	}

	private static void hail(ServerLevel chamber, ServerPlayer caster, MarrowSpitterEntity juggler, State state, int age, Vec3 floor) {
		Vec3 hands = juggler.position().add(0.0D, 1.2D, 0.0D);
		if (age < HAIL_TOSS) {
			CircusEffects.gather(chamber, hands, CircusEffects.BONE, age / (float) HAIL_TOSS);
			return;
		}
		if (age == HAIL_TOSS) {
			// The bones thrown high out of the cascade, whirling up out of sight.
			chamber.playSound(null, hands.x, hands.y, hands.z, SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE, 1.0F, 0.8F);
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), hands.x, hands.y + 1.0D, hands.z, 14, 0.25D, 0.8D,
					0.25D, 0.15D);
		}
		if (age < HAIL_WINDUP) {
			// The bones coming down out of the dark onto each mark, quicker as they fall.
			float t = (age - HAIL_TOSS) / (float) (HAIL_WINDUP - HAIL_TOSS);
			double drop = 12.0D * (1.0D - t * t);
			for (int k = 0; k < 3; k++) {
				Vec3 at = hailSpot(state, k);
				chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), at.x, at.y + 0.6D + drop, at.z, 3, 0.15D, 0.3D,
						0.15D, 0.0D);
			}
			return;
		}
		if (age != HAIL_WINDUP) return;
		Vec3 feet = caster.position();
		for (int k = 0; k < 3; k++) {
			Vec3 at = hailSpot(state, k);
			chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 2.0F, 1.1F + .1F * k);
			CircusEffects.impact(chamber, at.add(0, .4, 0), HAIL_RADIUS);
			CircusEffects.dust(chamber, at.add(0, .2, 0), 16);
			if (!state.landed && KnifeOrdealMoves.spotHits(at, HAIL_RADIUS, feet)) {
				state.landed = true;
				OrdealKit.strike(chamber, caster, juggler, HAIL_DAMAGE, feet.subtract(at), .4D, .3D);
			}
		}
	}

	private static void volley(ServerLevel chamber, ServerPlayer caster, MarrowSpitterEntity juggler, State state, int age, Vec3 floor,
			long now) {
		OrdealKit.face(juggler, state.aim);
		Vec3 hands = juggler.position().add(0.0D, 1.1D, 0.0D);
		if (age < VOLLEY_WINDUP) {
			CircusEffects.gather(chamber, hands, CircusEffects.CRIMSON, age / (float) VOLLEY_WINDUP);
			return;
		}
		int throwing = age - VOLLEY_WINDUP;
		if (throwing % VOLLEY_GAP != 0 || throwing / VOLLEY_GAP > 2) return;
		boolean pin = throwing / VOLLEY_GAP == 2;
		// The troupe's own knives fly down the lane for the look; the lane itself does the hit.
		CircusKnifeProjectileEntity knife = new CircusKnifeProjectileEntity(chamber, juggler, 0.0F);
		knife.setHarmless();
		knife.setPos(hands.x, hands.y, hands.z);
		Vec3 target = state.from.add(state.aim.scale(VOLLEY_LENGTH)).add(0.0D, 1.0D, 0.0D);
		Vec3 path = target.subtract(hands);
		knife.shoot(path.x, path.y, path.z, 2.4F, 0.0F);
		chamber.addFreshEntity(knife);
		chamber.playSound(null, hands.x, hands.y, hands.z, SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE, 1.0F,
				pin ? 1.1F : 1.5F);
		CircusEffects.lineFootprint(chamber, state.from, state.from.add(state.aim.scale(VOLLEY_LENGTH)),
				pin ? CircusEffects.CRIMSON : CircusEffects.BONE);
		// The lane burns where the dagger runs.
		OrdealKit.hazard(chamber, juggler, KnifeOrdealMoves.KIND_VOLLEY, new BossFootprint.Lane(VOLLEY_LENGTH, VOLLEY_HALF_WIDTH * .6D,
				0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING), state.from, state.from.add(state.aim), 3);
		Vec3 feet = caster.position();
		if (!KnifeOrdealMoves.laneHits(state.from, state.aim, VOLLEY_LENGTH, VOLLEY_HALF_WIDTH, feet, floor.y, VOLLEY_HEIGHT))
			return;
		OrdealKit.strike(chamber, caster, juggler, VOLLEY_DAMAGE, state.aim, .25D, .1D);
		if (pin) {
			MorphlingLock.pin(caster, MarrowJugglerRules.PIN_TICKS);
			state.pinnedUntil = now + MarrowJugglerRules.PIN_TICKS;
			chamber.playSound(null, feet.x, feet.y, feet.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 1.6F, 1.3F);
		}
	}

	private static void whirl(ServerLevel chamber, ServerPlayer caster, MarrowSpitterEntity juggler, State state, int age, Vec3 floor) {
		Vec3 over = state.mark.add(0.0D, WHIRL_HEIGHT, 0.0D);
		Vec3 low = state.mark.add(0.0D, WHIRL_LOW, 0.0D);
		Vec3 want;
		if (age < WHIRL_WINDUP) {
			// Climbing over the fighter, the blades fanned out and turning.
			want = juggler.position().lerp(over, .2D);
			CircusEffects.gather(chamber, juggler.position().add(0, 1, 0), CircusEffects.BONE, age / (float) WHIRL_WINDUP);
		} else if (age < WHIRL_WINDUP + WHIRL_DIVE) {
			// The dive: accelerating like a dropped weight.
			float t = (age - WHIRL_WINDUP + 1) / (float) WHIRL_DIVE;
			want = over.lerp(low, t * t);
		} else if (age < WHIRL_WINDUP + WHIRL_DIVE + WHIRL_SPIN) {
			want = low;
			if (age == WHIRL_WINDUP + WHIRL_DIVE) {
				chamber.playSound(null, low.x, low.y, low.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.5F, 1.2F);
				CircusEffects.impact(chamber, low, WHIRL_RADIUS);
				CircusEffects.dust(chamber, state.mark.add(0, .2, 0), 30);
			}
			if ((age - WHIRL_WINDUP - WHIRL_DIVE) % 2 == 0) whirlSparks(chamber, low, age);
			Vec3 feet = caster.position();
			if (!state.landed && KnifeOrdealMoves.spotHits(state.mark, WHIRL_RADIUS, feet)) {
				state.landed = true;
				OrdealKit.strike(chamber, caster, juggler, WHIRL_DAMAGE, feet.subtract(state.mark), 1.3D, .5D);
			}
		} else {
			// The climb back to its hover, heavy after the spin.
			want = juggler.position().lerp(state.mark.add(0.0D, 2.8D, 0.0D), .12D);
		}
		Vec3 step = want.subtract(juggler.position());
		juggler.setDeltaMovement(step);
		juggler.hurtMarked = true;
	}

	/** The six blades whirling at the floor: bone sparks thrown round the rim of the spin. */
	private static void whirlSparks(ServerLevel chamber, Vec3 centre, int age) {
		for (int i = 0; i < 6; i++) {
			double a = age * .9D + i * Math.PI / 3.0D;
			Vec3 at = centre.add(Math.cos(a) * WHIRL_RADIUS * .8D, .3D, Math.sin(a) * WHIRL_RADIUS * .8D);
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), at.x, at.y, at.z, 2, .05D, .05D, .05D, .02D);
		}
		chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), centre.x, centre.y + .3D, centre.z, 4,
				WHIRL_RADIUS * .4D, .1D, WHIRL_RADIUS * .4D, .05D);
	}

	/** Whether the Juggler is in one of its moves, when its hover and ordinary volley wait. */
	public static boolean busy(UUID juggler) {
		State state = STATES.get(juggler);
		return state != null && state.move >= 0;
	}

	/** Whether the fighter is pinned in this ordeal right now, by the Juggler's volley or the Dagger Thrower's blade. */
	public static boolean holding(UUID juggler, long now) {
		State state = STATES.get(juggler);
		return state != null && now < state.pinnedUntil;
	}

	/** Records a pin the Dagger Thrower's blade has put on the fighter of this Juggler's ordeal. */
	static void notePinned(UUID juggler, long until) {
		STATES.computeIfAbsent(juggler, ignored -> fresh(until)).pinnedUntil = until;
	}

	static void forget(UUID juggler) {
		STATES.remove(juggler);
	}

	public static void clearSessionState() {
		STATES.clear();
	}
}
