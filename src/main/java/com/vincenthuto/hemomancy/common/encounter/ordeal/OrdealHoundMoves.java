package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static com.vincenthuto.hemomancy.common.encounter.ordeal.BeastTamerMoves.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * The Sanguine Hound's moves in the Beast Tamer's ordeal (2026-11-17): its true self is the hound before the bridle,
 * and it hunts. Between its ordinary bites it lunges down a lane, pounces onto where the fighter is going, seizes a
 * fighter who stands too close and shakes them in its jaws, and howls a wave you jump. The Tamer's goad sends it into a
 * frenzy: little rest between moves and a faster chase. Each move is drawn on the floor by the ordeal's bold pass from
 * the first tick of its wind-up, posed by the true Hound's model from the same entity event, and landed here; the hits
 * are {@link BeastTamerMoves}.
 */
public final class OrdealHoundMoves {
	private static final Map<UUID, State> STATES = new HashMap<>();
	/** Where the Hound is kept: a little inside the curb. */
	private static final double LIMIT = OrdealStage.RING_RADIUS - 1.0D;
	private static final ResourceLocation FRENZY = Hemomancy.rloc("ordeal_frenzy");

	/** The Hound's moves as its ordeal's {@link OrdealPuppetMoves}. */
	public static final OrdealPuppetMoves MOVES = new OrdealPuppetMoves() {
		@Override
		public void tick(ServerLevel chamber, ServerPlayer caster, Mob puppet, Vec3 floor) {
			if (puppet instanceof SanguineHoundEntity hound) OrdealHoundMoves.tick(chamber, caster, hound, floor);
		}

		@Override
		public void forget(UUID puppet) {
			OrdealHoundMoves.forget(puppet);
		}

		@Override
		public void clearSessionState() {
			OrdealHoundMoves.clearSessionState();
		}

		@Override
		public boolean holding(UUID puppet, long now) {
			return OrdealHoundMoves.holding(puppet, now);
		}
	};

	private OrdealHoundMoves() {
	}

	private static final class State {
		int move = -1;
		long startedAt;
		long nextAt;
		long frenzyUntil;
		boolean lungeNext;
		Vec3 aim = Vec3.ZERO;
		Vec3 from = Vec3.ZERO;
		Vec3 to = Vec3.ZERO;
		boolean landed;
	}

	static void tick(ServerLevel chamber, ServerPlayer caster, SanguineHoundEntity hound, Vec3 floor) {
		long now = chamber.getGameTime();
		State state = STATES.computeIfAbsent(hound.getUUID(), ignored -> {
			State fresh = new State();
			fresh.nextAt = now + 40;
			return fresh;
		});
		OrdealKit.keepInRing(hound, floor, LIMIT);
		boolean frenzied = now < state.frenzyUntil;
		AttributeModifiers.sync(hound.getAttribute(Attributes.MOVEMENT_SPEED), FRENZY, FRENZY_PACE,
				AttributeModifier.Operation.ADD_MULTIPLIED_BASE, frenzied);
		if (frenzied && now % 3 == 0)
			// Blood steaming off it while it is goaded.
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), hound.getX(), hound.getY() + hound.getBbHeight() * .8D,
					hound.getZ(), 3, .35D, .2D, .35D, .02D);
		if (state.move < 0) {
			if (now < state.nextAt) return;
			start(chamber, caster, hound, state, floor, now);
			return;
		}
		int age = (int) (now - state.startedAt);
		hound.getNavigation().stop();
		switch (state.move) {
			case LUNGE -> lunge(chamber, caster, hound, state, age, floor);
			case POUNCE -> pounce(chamber, caster, hound, state, age, floor);
			case SHAKE -> shake(chamber, caster, hound, state, age);
			case HOWL -> howl(chamber, caster, hound, state, age, floor);
			default -> {
			}
		}
		if (age >= LENGTH[state.move]) {
			if (state.move == POUNCE) hound.setNoGravity(false);
			state.move = -1;
			state.nextAt = now + (now < state.frenzyUntil ? FRENZY_REST : REST);
		}
	}

	/** Picks by range: in its face it seizes or howls; at middle reach it lunges or pounces; from afar it pounces. */
	private static void start(ServerLevel chamber, ServerPlayer caster, SanguineHoundEntity hound, State state, Vec3 floor,
			long now) {
		double distance = hound.distanceTo(caster);
		int roll = (int) Math.floorMod(now / 7 + caster.getUUID().getLeastSignificantBits(), 3);
		int move;
		if (state.lungeNext) move = LUNGE;
		else if (distance < SHAKE_AHEAD + SHAKE_REACH - 0.6D) move = new int[]{SHAKE, SHAKE, HOWL}[roll];
		else if (distance < 4.0D) move = new int[]{LUNGE, HOWL, SHAKE}[roll];
		else move = new int[]{POUNCE, LUNGE, POUNCE}[roll];
		state.lungeNext = false;
		state.move = move;
		state.startedAt = now;
		state.landed = false;
		state.aim = flat(caster.position().subtract(hound.position()));
		OrdealKit.face(hound, state.aim);
		hound.ordealMove(move);
		Vec3 feet = new Vec3(hound.getX(), floor.y, hound.getZ());
		switch (move) {
			case LUNGE -> OrdealKit.hazard(chamber, hound, KIND_LUNGE, new BossFootprint.Lane(LUNGE_LENGTH, LUNGE_HALF_WIDTH, 0,
					0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING), feet, feet.add(state.aim), LUNGE_WINDUP + LUNGE_RUN);
			case POUNCE -> {
				state.from = hound.position();
				state.to = pounceMark(feet, caster.position(), caster.getDeltaMovement(), floor, LIMIT);
				OrdealKit.hazard(chamber, hound, KIND_POUNCE, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, POUNCE_RADIUS,
						BossFootprint.Height.STANDING), state.to, state.to, POUNCE_WINDUP + POUNCE_FLIGHT + 4);
				state.aim = flat(state.to.subtract(feet));
				OrdealKit.face(hound, state.aim);
			}
			case SHAKE -> OrdealKit.hazard(chamber, hound, KIND_SHAKE, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
					SHAKE_REACH, BossFootprint.Height.STANDING), feet.add(state.aim.scale(SHAKE_AHEAD)),
					feet.add(state.aim.scale(SHAKE_AHEAD)), SHAKE_WINDUP + 4);
			case HOWL -> OrdealKit.hazard(chamber, hound, KIND_HOWL, new BossFootprint.Ring(HOWL_RADIUS, HOWL_RADIUS, .8D, 1, 0,
					HOWL_WINDUP, BossFootprint.Height.JUMPABLE), feet, feet, HOWL_WINDUP);
			default -> {
			}
		}
		chamber.playSound(null, hound.getX(), hound.getY() + 1.0D, hound.getZ(), move == HOWL ? SoundEvents.WOLF_PANT
				: SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 2.5F, .6F);
	}

	private static void lunge(ServerLevel chamber, ServerPlayer caster, SanguineHoundEntity hound, State state, int age,
			Vec3 floor) {
		if (age < LUNGE_WINDUP) {
			OrdealKit.face(hound, state.aim);
			if (age % 3 == 0) CircusEffects.dust(chamber, hound.position().add(state.aim.scale(-.6D)), 3);
			return;
		}
		if (age >= LUNGE_WINDUP + LUNGE_RUN) return;
		Vec3 step = state.aim.scale(LUNGE_SPEED);
		// The rush ends at the curb: it never carries the Hound out of its ring.
		if (lungeStops(hound.position().add(step), floor, LIMIT)) {
			if (!state.landed) landed(chamber, hound.position(), .9D);
			state.landed = true;
			return;
		}
		hound.move(MoverType.SELF, step);
		OrdealKit.face(hound, state.aim);
		if (age == LUNGE_WINDUP)
			chamber.playSound(null, hound.getX(), hound.getY(), hound.getZ(), SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 3.0F, .8F);
		if ((age - LUNGE_WINDUP) % 2 == 0) CircusEffects.dust(chamber, hound.position().add(0, .2, 0), 5);
		if (!state.landed && hound.getBoundingBox().inflate(.5D).intersects(caster.getBoundingBox())) {
			state.landed = true;
			landed(chamber, caster.position(), 1.0D);
			chamber.sendParticles(HemoParticleData.bloodClaw(CircusEffects.CRIMSON), caster.getX(), caster.getY() + 1.0D,
					caster.getZ(), 3, .3D, .3D, .3D, 0.0D);
			OrdealKit.strike(chamber, caster, hound, LUNGE_DAMAGE, state.aim, 1.2D, .45D);
		}
	}

	private static void pounce(ServerLevel chamber, ServerPlayer caster, SanguineHoundEntity hound, State state, int age,
			Vec3 floor) {
		if (age < POUNCE_WINDUP) {
			OrdealKit.face(hound, state.aim);
			return;
		}
		int flight = age - POUNCE_WINDUP;
		if (flight == 0) {
			hound.setNoGravity(true);
			chamber.playSound(null, hound.getX(), hound.getY(), hound.getZ(), SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 3.0F, .7F);
			CircusEffects.dust(chamber, hound.position(), 14);
		}
		if (flight < POUNCE_FLIGHT) {
			Vec3 next = pounceArc(state.from, state.to, (flight + 1) / (double) POUNCE_FLIGHT);
			Vec3 motion = next.subtract(hound.position());
			hound.setDeltaMovement(motion);
			hound.move(MoverType.SELF, motion);
			hound.hasImpulse = true;
			OrdealKit.face(hound, state.aim);
			return;
		}
		if (flight == POUNCE_FLIGHT) {
			hound.setNoGravity(false);
			hound.setDeltaMovement(Vec3.ZERO);
			landed(chamber, state.to, 1.4D);
			if (discHits(state.to, POUNCE_RADIUS, caster.position())) {
				chamber.sendParticles(HemoParticleData.bloodClaw(CircusEffects.CRIMSON), caster.getX(), caster.getY() + 1.0D,
						caster.getZ(), 4, .35D, .3D, .35D, 0.0D);
				OrdealKit.strike(chamber, caster, hound, POUNCE_DAMAGE, caster.position().subtract(state.to), .6D, .25D);
			}
		}
	}

	private static void shake(ServerLevel chamber, ServerPlayer caster, SanguineHoundEntity hound, State state, int age) {
		if (age < SHAKE_WINDUP) {
			OrdealKit.face(hound, state.aim);
			return;
		}
		if (age == SHAKE_WINDUP) {
			Vec3 jaws = hound.position().add(state.aim.scale(SHAKE_AHEAD));
			state.landed = discHits(new Vec3(jaws.x, hound.getY(), jaws.z), SHAKE_REACH, caster.position());
			chamber.playSound(null, jaws.x, jaws.y, jaws.z, SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 3.0F, .5F);
			if (!state.landed) {
				// The jaws close on air: it snaps, and the pose ends.
				hound.ordealMove(-1);
				return;
			}
			chamber.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundInit.RITE_SUBJECT_THUD.get(),
					SoundSource.HOSTILE, 2.0F, 1.2F);
		}
		if (!state.landed) return;
		int held = age - SHAKE_WINDUP;
		if (held < SHAKE_HOLD) {
			// Held in the jaws and shaken, side to side, harder at each bite.
			OrdealKit.face(hound, state.aim);
			double swing = Math.sin(held * .8D) * .7D;
			Vec3 side = new Vec3(-state.aim.z, 0.0D, state.aim.x);
			Vec3 at = hound.position().add(state.aim.scale(SHAKE_AHEAD)).add(side.scale(swing)).add(0.0D, .45D, 0.0D);
			caster.teleportTo(at.x, at.y, at.z);
			caster.setDeltaMovement(Vec3.ZERO);
			caster.hurtMarked = true;
			for (int bite : SHAKES)
				if (held == bite) {
					chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), at.x, at.y + 1.0D, at.z, 14, .3D, .3D, .3D,
							.08D);
					OrdealKit.strike(chamber, caster, hound, SHAKE_DAMAGE, Vec3.ZERO, 0.0D, 0.0D);
				}
		} else if (held == SHAKE_HOLD) {
			landed(chamber, caster.position(), .8D);
			Vec3 side = new Vec3(-state.aim.z, 0.0D, state.aim.x);
			OrdealKit.strike(chamber, caster, hound, FLING_DAMAGE, state.aim.add(side.scale(.6D)), 1.6D, .6D);
		}
	}

	private static void howl(ServerLevel chamber, ServerPlayer caster, SanguineHoundEntity hound, State state, int age,
			Vec3 floor) {
		Vec3 feet = new Vec3(hound.getX(), floor.y, hound.getZ());
		if (age < HOWL_WINDUP) {
			CircusEffects.gather(chamber, hound.position().add(0, hound.getBbHeight() * 1.1D, 0), CircusEffects.CRIMSON,
					age / (float) HOWL_WINDUP);
			return;
		}
		if (age == HOWL_WINDUP) {
			chamber.playSound(null, hound.getX(), hound.getY() + 1.0D, hound.getZ(), SoundEvents.WOLF_HOWL, SoundSource.HOSTILE,
					4.0F, .55F);
			chamber.playSound(null, feet.x, feet.y, feet.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 2.5F, .7F);
			CircusEffects.impact(chamber, hound.position().add(0, 1.0, 0), 1.5D);
			OrdealKit.hazard(chamber, hound, KIND_HOWL, new BossFootprint.Ring(.5D, HOWL_RADIUS, HOWL_BAND, 1, 0, HOWL_WAVE,
					BossFootprint.Height.JUMPABLE), feet, feet, HOWL_WAVE + 2);
		}
		int wave = age - HOWL_WINDUP;
		if (wave > HOWL_WAVE || state.landed) return;
		if (howlHits(feet, howlRadius(wave), caster.position())) {
			state.landed = true;
			OrdealKit.strike(chamber, caster, hound, HOWL_DAMAGE, caster.position().subtract(feet), .7D, .4D);
		}
	}

	/** A heavy landing: a thud through the floor, blood off the sawdust. */
	private static void landed(ServerLevel chamber, Vec3 at, double size) {
		chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 2.5F, .8F);
		CircusEffects.impact(chamber, at.add(0, .4, 0), size);
		CircusEffects.dust(chamber, at.add(0, .2, 0), (int) (16 * size));
	}

	/** The Tamer's goad: the Hound goes into a frenzy until {@code until}, and opens it with a lunge. */
	static void goad(UUID hound, long until, long now) {
		State state = STATES.computeIfAbsent(hound, ignored -> new State());
		state.frenzyUntil = until;
		state.lungeNext = true;
		if (state.move < 0) state.nextAt = Math.min(state.nextAt, now + 2);
	}

	/** Whether the Hound is in one of its moves, when its ordinary bite waits. */
	public static boolean busy(UUID hound) {
		State state = STATES.get(hound);
		return state != null && state.move >= 0;
	}

	/** Whether the Hound has a fighter in its jaws right now. */
	public static boolean holding(UUID hound, long now) {
		State state = STATES.get(hound);
		if (state == null || state.move != SHAKE || !state.landed) return false;
		long age = now - state.startedAt;
		return age >= SHAKE_WINDUP && age < SHAKE_WINDUP + SHAKE_HOLD;
	}

	static void forget(UUID hound) {
		STATES.remove(hound);
	}

	public static void clearSessionState() {
		STATES.clear();
	}
}
