package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * The Scarlet Mummer's moves in the Stilt Walker's ordeal (2026-11-17). Its true self is a pageant that wants every eye:
 * between its ordinary blows it opens the whole train of eyes on its fans (a fighter in reach who is looking at it is
 * struck and dazzled; look away), feints a step aside and lunges back with its long fingers, whirls with its ribbons out
 * like blades, and makes a grand entrance, leaping across the ring onto the fighter's mark. Each is drawn on the floor by
 * the ordeal's bold telegraph pass from its first wind-up tick and posed by the true Mummer's model from the same entity
 * event ({@link ScarletMummerEntity#ordealMove}). The hits are {@link StiltWalkerMoves}.
 */
public final class ScarletMummerOrdealMoves {
	public static final int DAZZLE = 0, FEINT = 1, WHIRL = 2, ENTRANCE = 3;
	/** Each move from its start to the Mummer's next choice, recovery included. */
	private static final int[] LENGTH = {StiltWalkerMoves.DAZZLE_WINDUP + 16,
			StiltWalkerMoves.FEINT_WINDUP + StiltWalkerMoves.FEINT_DASH + 14, StiltWalkerMoves.WHIRL_WINDUP + 16,
			StiltWalkerMoves.ENTRANCE_WINDUP + StiltWalkerMoves.ENTRANCE_FLIGHT + 14};
	/** Quiet ticks between moves, when its ordinary blows carry the fight. */
	private static final int REST = 30;
	/** The eyes on its fans, opening: a hot gold and the scarlet round them. */
	private static final ParticleColor EYE = new ParticleColor(242, 190, 40);
	private static final Map<UUID, State> STATES = new HashMap<>();

	/** The Mummer's moves as its ordeal's {@link OrdealPuppetMoves}. */
	public static final OrdealPuppetMoves MOVES = new OrdealPuppetMoves() {
		@Override
		public void tick(ServerLevel chamber, ServerPlayer caster, Mob puppet, Vec3 floor) {
			if (puppet instanceof ScarletMummerEntity mummer) ScarletMummerOrdealMoves.tick(chamber, caster, mummer, floor);
		}

		@Override
		public void forget(UUID puppet) {
			STATES.remove(puppet);
		}

		@Override
		public void clearSessionState() {
			ScarletMummerOrdealMoves.clearSessionState();
		}
	};

	private ScarletMummerOrdealMoves() {
	}

	private static final class State {
		int move = -1;
		long startedAt;
		long nextAt;
		int turn;
		Vec3 aim = Vec3.ZERO;
		Vec3 from = Vec3.ZERO;
		Vec3 mark = Vec3.ZERO;
		boolean landed;
	}

	static void tick(ServerLevel chamber, ServerPlayer caster, ScarletMummerEntity mummer, Vec3 floor) {
		long now = chamber.getGameTime();
		State state = STATES.computeIfAbsent(mummer.getUUID(), ignored -> {
			State fresh = new State();
			fresh.nextAt = now + 40;
			return fresh;
		});
		OrdealKit.keepInRing(mummer, floor, OrdealStage.RING_RADIUS - 1.0D);
		if (state.move < 0) {
			if (now < state.nextAt) return;
			start(chamber, caster, mummer, state, floor, now);
			return;
		}
		int age = (int) (now - state.startedAt);
		mummer.getNavigation().stop();
		switch (state.move) {
			case DAZZLE -> dazzle(chamber, caster, mummer, state, age);
			case FEINT -> feint(chamber, caster, mummer, state, age, floor);
			case WHIRL -> whirl(chamber, caster, mummer, state, age, floor);
			case ENTRANCE -> entrance(chamber, caster, mummer, state, age);
			default -> {
			}
		}
		if (age >= LENGTH[state.move]) {
			state.move = -1;
			state.nextAt = now + REST;
			mummer.ordealMove(-1);
		}
	}

	/** Picks by range, in turn: from afar it leaps or dazzles; up close it whirls or feints; between, it dazzles or feints. */
	private static void start(ServerLevel chamber, ServerPlayer caster, ScarletMummerEntity mummer, State state, Vec3 floor,
			long now) {
		double distance = OrdealKit.horizontal(mummer.position(), caster.position());
		int turn = state.turn++;
		int move = distance > 6.0D ? (turn % 2 == 0 ? ENTRANCE : DAZZLE)
				: distance < 3.0D ? (turn % 2 == 0 ? WHIRL : FEINT) : (turn % 2 == 0 ? DAZZLE : FEINT);
		state.move = move;
		state.startedAt = now;
		state.landed = false;
		state.aim = OrdealKit.flat(caster.position().subtract(mummer.position()));
		if (state.aim.lengthSqr() < 1.0E-4D) state.aim = Vec3.directionFromRotation(0, mummer.getYRot());
		state.from = new Vec3(mummer.getX(), floor.y, mummer.getZ());
		OrdealKit.face(mummer, state.aim);
		mummer.ordealMove(move);
		switch (move) {
			case DAZZLE -> OrdealKit.hazard(chamber, mummer, StiltWalkerMoves.KIND_DAZZLE, new BossFootprint.Ring(
					StiltWalkerMoves.DAZZLE_REACH, StiltWalkerMoves.DAZZLE_REACH, .9D, 1, 0, StiltWalkerMoves.DAZZLE_WINDUP,
					BossFootprint.Height.STANDING), state.from, state.from, StiltWalkerMoves.DAZZLE_WINDUP);
			case FEINT -> {
				// The step aside comes first, to whichever side keeps it in the ring; the lunge is drawn from there.
				Vec3 side = new Vec3(-state.aim.z, 0.0D, state.aim.x).scale((turn / 2) % 2 == 0 ? 1.0D : -1.0D);
				Vec3 to = state.from.add(side.scale(StiltWalkerMoves.FEINT_SIDESTEP));
				if (OrdealKit.horizontal(to, floor) > OrdealStage.RING_RADIUS - 1.5D)
					to = state.from.subtract(side.scale(StiltWalkerMoves.FEINT_SIDESTEP));
				state.mark = StiltWalkerMoves.inside(floor, to, 1.5D);
				state.aim = OrdealKit.flat(caster.position().subtract(state.mark));
				if (state.aim.lengthSqr() < 1.0E-4D) state.aim = OrdealKit.flat(floor.subtract(state.mark));
				OrdealKit.hazard(chamber, mummer, StiltWalkerMoves.KIND_FEINT, new BossFootprint.Lane(StiltWalkerMoves.FEINT_LENGTH,
						StiltWalkerMoves.FEINT_HALF_WIDTH, 0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING), state.mark,
						state.mark.add(state.aim), StiltWalkerMoves.FEINT_WINDUP + StiltWalkerMoves.FEINT_DASH);
				chamber.sendParticles(HemoParticleData.serpent(EYE), mummer.getX(), mummer.getY() + 1.0D, mummer.getZ(), 10, .2D, .5D,
						.2D, .02D);
			}
			case WHIRL -> OrdealKit.hazard(chamber, mummer, StiltWalkerMoves.KIND_WHIRL, new BossFootprint.Disc(
					BossFootprint.Anchor.ORIGIN, StiltWalkerMoves.WHIRL_RADIUS, BossFootprint.Height.STANDING), state.from,
					state.from, StiltWalkerMoves.WHIRL_WINDUP + 6);
			case ENTRANCE -> {
				state.mark = StiltWalkerMoves.inside(floor, caster.position(), 1.2D);
				OrdealKit.hazard(chamber, mummer, StiltWalkerMoves.KIND_ENTRANCE, new BossFootprint.Disc(
						BossFootprint.Anchor.ORIGIN, StiltWalkerMoves.ENTRANCE_RADIUS, BossFootprint.Height.STANDING), state.mark,
						state.mark, StiltWalkerMoves.ENTRANCE_WINDUP + StiltWalkerMoves.ENTRANCE_FLIGHT + 6);
			}
			default -> {
			}
		}
		chamber.playSound(null, mummer.getX(), mummer.getY() + 1.5D, mummer.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(),
				SoundSource.HOSTILE, 2.0F, 1.25F);
	}

	private static void dazzle(ServerLevel chamber, ServerPlayer caster, ScarletMummerEntity mummer, State state, int age) {
		int windup = StiltWalkerMoves.DAZZLE_WINDUP;
		Vec3 eyes = mummer.position().add(0.0D, 2.4D, 0.0D);
		if (age < windup) {
			// The eyes on its train opening one by one as the fans rise.
			if (age % 3 == 0) chamber.sendParticles(HemoParticleData.glow(EYE), eyes.x, eyes.y, eyes.z, 2 + age / 3, .9D, .6D, .9D, 0.0D);
			return;
		}
		if (age != windup) return;
		chamber.playSound(null, eyes.x, eyes.y, eyes.z, SoundInit.ENTITY_CIRCUS_RINGMASTER_PHASE_CHANGE.get(), SoundSource.HOSTILE,
				2.5F, 1.4F);
		chamber.sendParticles(HemoParticleData.hitGlow(EYE), eyes.x, eyes.y, eyes.z, 6, .8D, .5D, .8D, 0.0D);
		chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), eyes.x, eyes.y, eyes.z, 30, 1.6D, 1.0D, 1.6D, .08D);
		if (StiltWalkerMoves.dazzleHits(eyes, caster.getEyePosition(), caster.getLookAngle())) {
			OrdealKit.strike(chamber, caster, mummer, StiltWalkerMoves.DAZZLE_DAMAGE, Vec3.ZERO, 0.0D, 0.0D);
			caster.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false));
			caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false));
		}
	}

	private static void feint(ServerLevel chamber, ServerPlayer caster, ScarletMummerEntity mummer, State state, int age, Vec3 floor) {
		int windup = StiltWalkerMoves.FEINT_WINDUP;
		if (age < 4) {
			// The step aside: quick, light, gone before the eye follows.
			Vec3 at = state.from.lerp(state.mark, (age + 1) / 4.0D);
			mummer.teleportTo(at.x, Math.max(mummer.getY(), floor.y), at.z);
			OrdealKit.face(mummer, state.aim);
			return;
		}
		if (age < windup) {
			OrdealKit.face(mummer, state.aim);
			return;
		}
		if (age < windup + StiltWalkerMoves.FEINT_DASH) {
			// The lunge back: down the lane in three ticks, fingers first.
			Vec3 at = state.mark.add(state.aim.scale(StiltWalkerMoves.FEINT_LENGTH * .7D * (age - windup + 1) / StiltWalkerMoves.FEINT_DASH));
			if (OrdealKit.horizontal(at, floor) < OrdealStage.RING_RADIUS - 1.0D) mummer.teleportTo(at.x, Math.max(mummer.getY(), floor.y), at.z);
			if (!state.landed && StiltWalkerMoves.feintHits(state.mark, state.aim, caster.position())) {
				state.landed = true;
				chamber.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE,
						2.0F, 1.3F);
				OrdealKit.strike(chamber, caster, mummer, StiltWalkerMoves.FEINT_DAMAGE, state.aim, 1.0D, .35D);
			}
		}
	}

	private static void whirl(ServerLevel chamber, ServerPlayer caster, ScarletMummerEntity mummer, State state, int age, Vec3 floor) {
		int windup = StiltWalkerMoves.WHIRL_WINDUP;
		Vec3 centre = new Vec3(mummer.getX(), floor.y, mummer.getZ());
		if (age < windup) {
			if (age % 2 == 0) CircusEffects.gather(chamber, centre.add(0, 1.4, 0), CircusEffects.CRIMSON, age / (float) windup);
			return;
		}
		if (age > windup + 8) return;
		// The ribbons out like blades, flung round for eight ticks; the blow lands on the first.
		double a = age * 1.1D;
		for (int i = 0; i < 3; i++) {
			double b = a + i * Math.PI * 2.0D / 3.0D;
			chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), centre.x + Math.cos(b) * 2.4D, centre.y + 1.0D,
					centre.z + Math.sin(b) * 2.4D, 2, .1D, .1D, .1D, .05D);
		}
		if (age != windup) return;
		chamber.playSound(null, centre.x, centre.y, centre.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.5F, 1.1F);
		CircusEffects.impact(chamber, centre.add(0, .8, 0), StiltWalkerMoves.WHIRL_RADIUS);
		if (StiltWalkerMoves.whirlHits(centre, caster.position()))
			OrdealKit.strike(chamber, caster, mummer, StiltWalkerMoves.WHIRL_DAMAGE, caster.position().subtract(centre), 1.2D, .4D);
	}

	private static void entrance(ServerLevel chamber, ServerPlayer caster, ScarletMummerEntity mummer, State state, int age) {
		int windup = StiltWalkerMoves.ENTRANCE_WINDUP, flight = StiltWalkerMoves.ENTRANCE_FLIGHT;
		if (age < windup) {
			OrdealKit.face(mummer, state.mark.subtract(state.from));
			return;
		}
		if (age <= windup + flight) {
			// The leap: a high arc across the ring, set tick by tick so it lands where it was drawn.
			Vec3 at = StiltWalkerMoves.entranceArc(state.from, state.mark, (age - windup) / (double) flight);
			mummer.teleportTo(at.x, at.y, at.z);
			mummer.setDeltaMovement(Vec3.ZERO);
			if ((age - windup) % 2 == 0)
				chamber.sendParticles(HemoParticleData.serpent(new ParticleColor(176, 8, 28)), at.x, at.y + 1.0D, at.z, 4, .2D, .3D, .2D, .01D);
		}
		if (age != windup + flight) return;
		chamber.playSound(null, state.mark.x, state.mark.y, state.mark.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F, .8F);
		CircusEffects.impact(chamber, state.mark.add(0, .4, 0), StiltWalkerMoves.ENTRANCE_RADIUS);
		CircusEffects.dust(chamber, state.mark.add(0, .2, 0), 30);
		if (StiltWalkerMoves.entranceHits(state.mark, caster.position()))
			OrdealKit.strike(chamber, caster, mummer, StiltWalkerMoves.ENTRANCE_DAMAGE, caster.position().subtract(state.mark), .9D, .5D);
	}

	/** Whether the Mummer is in one of its moves, when its ordinary blow waits. */
	public static boolean busy(UUID mummer) {
		State state = STATES.get(mummer);
		return state != null && state.move >= 0;
	}

	public static void clearSessionState() {
		STATES.clear();
	}
}
