package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/**
 * The Gorebound Hulk's moves in the Strongman's ordeal (2026-11-17): the true self is not a summon that only punches.
 * Between its ordinary blows it hammers the floor in front of it, charges down a lane, grabs a fighter who stands too
 * close and throws them, and stomps out a ring you jump. Each is drawn on the floor by the ordeal's bold telegraph pass
 * from the first tick of its wind-up, posed by the true Hulk's model from the same entity event, and landed here.
 */
public final class OrdealHulkMoves {
	public static final int HAMMER = 0, CHARGE = 1, GRAB = 2, STOMP = 3;
	/** Wind-ups, in ticks: each move's gather before it lands. */
	public static final int HAMMER_WINDUP = 14, CHARGE_WINDUP = 16, GRAB_WINDUP = 10, STOMP_WINDUP = 18;
	/** The charge's run, the grab's hold before the throw, the stomp wave's travel. */
	public static final int CHARGE_RUN = 14, GRAB_HOLD = 14, STOMP_WAVE = 9;
	/** How long each move takes from its start to the Hulk's next choice, recovery included. */
	private static final int[] LENGTH = {HAMMER_WINDUP + 16, CHARGE_WINDUP + CHARGE_RUN + 18, GRAB_WINDUP + GRAB_HOLD + 14,
			STOMP_WINDUP + STOMP_WAVE + 14};
	/** The hammer lands this far in front of the Hulk, in a disc this wide. */
	public static final double HAMMER_REACH = 2.5D, HAMMER_RADIUS = 2.5D;
	public static final double CHARGE_SPEED = .8D, CHARGE_LENGTH = CHARGE_SPEED * CHARGE_RUN;
	public static final double GRAB_REACH = 3.0D, STOMP_RADIUS = 6.0D, STOMP_BAND = 1.2D;
	private static final float HAMMER_DAMAGE = 7.0F, CHARGE_DAMAGE = 6.0F, THROW_DAMAGE = 5.0F, STOMP_DAMAGE = 4.0F;
	/** Quiet ticks between moves, when its ordinary blows carry the fight. */
	private static final int REST = 34;
	private static final Map<UUID, State> STATES = new HashMap<>();

	/** The Hulk's moves as its ordeal's {@link OrdealPuppetMoves}. */
	public static final OrdealPuppetMoves MOVES = new OrdealPuppetMoves() {
		@Override
		public void tick(ServerLevel chamber, ServerPlayer caster, net.minecraft.world.entity.Mob puppet, Vec3 floor) {
			if (puppet instanceof GoreboundHulkEntity hulk) OrdealHulkMoves.tick(chamber, caster, hulk, floor);
		}

		@Override
		public void forget(UUID puppet) {
			OrdealHulkMoves.forget(puppet);
		}

		@Override
		public void clearSessionState() {
			OrdealHulkMoves.clearSessionState();
		}

		@Override
		public boolean holding(UUID puppet, long now) {
			return OrdealHulkMoves.holding(puppet, now);
		}
	};

	private OrdealHulkMoves() {
	}

	private static final class State {
		int move = -1;
		long startedAt;
		long nextAt;
		Vec3 aim = Vec3.ZERO;
		boolean landed;
	}

	static void tick(ServerLevel chamber, ServerPlayer caster, GoreboundHulkEntity hulk, Vec3 floor) {
		long now = chamber.getGameTime();
		State state = STATES.computeIfAbsent(hulk.getUUID(), ignored -> {
			State fresh = new State();
			fresh.nextAt = now + 50;
			return fresh;
		});
		keepInRing(hulk, floor);
		if (state.move < 0) {
			if (now < state.nextAt || hulk.getStrikeWindup() > 0) return;
			start(chamber, caster, hulk, state, floor, now);
			return;
		}
		int age = (int) (now - state.startedAt);
		hulk.getNavigation().stop();
		switch (state.move) {
			case HAMMER -> hammer(chamber, caster, hulk, state, age, floor);
			case CHARGE -> charge(chamber, caster, hulk, state, age, floor);
			case GRAB -> grab(chamber, caster, hulk, state, age);
			case STOMP -> stomp(chamber, caster, hulk, state, age, floor);
			default -> {
			}
		}
		if (age >= LENGTH[state.move]) {
			state.move = -1;
			state.nextAt = now + REST;
		}
	}

	/** Picks by range: up close it hammers, grabs or stomps; from afar it charges. */
	private static void start(ServerLevel chamber, ServerPlayer caster, GoreboundHulkEntity hulk, State state, Vec3 floor,
			long now) {
		double distance = hulk.distanceTo(caster);
		int roll = (int) Math.floorMod(now / 7 + caster.getUUID().getLeastSignificantBits(), 3);
		int move = distance > 6.5D ? CHARGE : distance < GRAB_REACH ? new int[]{HAMMER, GRAB, STOMP}[roll]
				: new int[]{HAMMER, STOMP, CHARGE}[roll];
		state.move = move;
		state.startedAt = now;
		state.landed = false;
		state.aim = flat(caster.position().subtract(hulk.position()));
		if (state.aim.lengthSqr() < 1.0E-4D) state.aim = Vec3.directionFromRotation(0, hulk.getYRot());
		face(hulk, state.aim);
		hulk.ordealMove(move);
		Vec3 feet = new Vec3(hulk.getX(), floor.y, hulk.getZ());
		switch (move) {
			case HAMMER -> hazard(chamber, hulk, OrdealInterventionsDriver.KIND_HAMMER,
					new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, HAMMER_RADIUS, BossFootprint.Height.STANDING),
					feet.add(state.aim.scale(HAMMER_REACH)), HAMMER_WINDUP + 6);
			case CHARGE -> hazard(chamber, hulk, OrdealInterventionsDriver.KIND_CHARGE, feet, feet.add(state.aim),
					new BossFootprint.Lane(CHARGE_LENGTH + 1.5D, 1.2D, 0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING),
					CHARGE_WINDUP + CHARGE_RUN);
			case GRAB -> hazard(chamber, hulk, OrdealInterventionsDriver.KIND_GRAB,
					new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, GRAB_REACH, BossFootprint.Height.STANDING),
					feet.add(state.aim.scale(1.2D)), GRAB_WINDUP + 6);
			case STOMP -> hazard(chamber, hulk, OrdealInterventionsDriver.KIND_STOMP,
					new BossFootprint.Ring(STOMP_RADIUS, STOMP_RADIUS, .8D, 1, 0, STOMP_WINDUP, BossFootprint.Height.JUMPABLE),
					feet, STOMP_WINDUP);
			default -> {
			}
		}
		chamber.playSound(null, hulk.getX(), hulk.getY() + 1.5D, hulk.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(),
				SoundSource.HOSTILE, 2.0F, .7F);
	}

	private static void hammer(ServerLevel chamber, ServerPlayer caster, GoreboundHulkEntity hulk, State state, int age, Vec3 floor) {
		if (age != HAMMER_WINDUP) return;
		Vec3 at = new Vec3(hulk.getX(), floor.y, hulk.getZ()).add(state.aim.scale(HAMMER_REACH));
		landed(chamber, at, 1.8D);
		if (horizontal(at, caster.position()) <= HAMMER_RADIUS && caster.getY() - floor.y < 2.5D)
			strike(chamber, caster, hulk, HAMMER_DAMAGE, state.aim, .9D, .45D);
	}

	private static void charge(ServerLevel chamber, ServerPlayer caster, GoreboundHulkEntity hulk, State state, int age, Vec3 floor) {
		if (age < CHARGE_WINDUP || age >= CHARGE_WINDUP + CHARGE_RUN) return;
		// The rush ends at the curb: it never carries the Hulk out of its ring.
		Vec3 next = hulk.position().add(state.aim.scale(CHARGE_SPEED));
		if (horizontal(next, floor) > OrdealStage.RING_RADIUS - 1.5D) {
			if (!state.landed) landed(chamber, hulk.position(), 1.2D);
			state.landed = true;
			return;
		}
		hulk.move(MoverType.SELF, state.aim.scale(CHARGE_SPEED));
		face(hulk, state.aim);
		if ((age - CHARGE_WINDUP) % 3 == 0) CircusEffects.dust(chamber, hulk.position().add(0, .2, 0), 6);
		if (!state.landed && hulk.getBoundingBox().inflate(.6D).intersects(caster.getBoundingBox())) {
			state.landed = true;
			landed(chamber, caster.position(), 1.0D);
			strike(chamber, caster, hulk, CHARGE_DAMAGE, state.aim, 1.6D, .6D);
		}
	}

	private static void grab(ServerLevel chamber, ServerPlayer caster, GoreboundHulkEntity hulk, State state, int age) {
		if (age == GRAB_WINDUP) {
			Vec3 hands = hulk.position().add(state.aim.scale(1.2D));
			state.landed = horizontal(hands, caster.position()) <= GRAB_REACH && Math.abs(caster.getY() - hulk.getY()) < 2.0D;
			if (!state.landed) {
				hulk.ordealMove(-1);
				return;
			}
			chamber.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundInit.RITE_SUBJECT_THUD.get(),
					SoundSource.HOSTILE, 2.0F, 1.1F);
		}
		if (!state.landed || age < GRAB_WINDUP) return;
		if (age < GRAB_WINDUP + GRAB_HOLD) {
			// Held up over its head, kicking.
			Vec3 held = hulk.position().add(state.aim.scale(.6D)).add(0.0D, 3.2D, 0.0D);
			caster.teleportTo(held.x, held.y, held.z);
			caster.setDeltaMovement(Vec3.ZERO);
			caster.hurtMarked = true;
		} else if (age == GRAB_WINDUP + GRAB_HOLD) {
			landed(chamber, caster.position(), .8D);
			strike(chamber, caster, hulk, THROW_DAMAGE, state.aim, 1.8D, .7D);
		}
	}

	private static void stomp(ServerLevel chamber, ServerPlayer caster, GoreboundHulkEntity hulk, State state, int age, Vec3 floor) {
		Vec3 feet = new Vec3(hulk.getX(), floor.y, hulk.getZ());
		if (age == STOMP_WINDUP) {
			landed(chamber, feet, 1.5D);
			hazard(chamber, hulk, OrdealInterventionsDriver.KIND_STOMP, new BossFootprint.Ring(.5D, STOMP_RADIUS, STOMP_BAND, 1, 0,
					STOMP_WAVE, BossFootprint.Height.JUMPABLE), feet, STOMP_WAVE + 2);
		}
		if (age < STOMP_WINDUP || age > STOMP_WINDUP + STOMP_WAVE || state.landed) return;
		double wave = .5D + (STOMP_RADIUS - .5D) * (age - STOMP_WINDUP) / (double) STOMP_WAVE;
		if (Math.abs(horizontal(feet, caster.position()) - wave) <= STOMP_BAND / 2.0D + .3D
				&& caster.getY() - floor.y < OrdealMoves.CLEARANCE) {
			state.landed = true;
			strike(chamber, caster, hulk, STOMP_DAMAGE, flat(caster.position().subtract(feet)), .7D, .5D);
		}
	}

	/** A heavy landing: a thud through the floor, blood and bone off the plate. */
	private static void landed(ServerLevel chamber, Vec3 at, double size) {
		chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F, .6F);
		CircusEffects.impact(chamber, at.add(0, .4, 0), size);
		CircusEffects.dust(chamber, at.add(0, .2, 0), (int) (20 * size));
	}

	private static void strike(ServerLevel chamber, ServerPlayer caster, GoreboundHulkEntity hulk, float damage, Vec3 away,
			double push, double lift) {
		caster.hurt(chamber.damageSources().mobAttack(hulk), damage);
		Vec3 out = away.lengthSqr() < 1.0E-4D ? Vec3.ZERO : away.normalize().scale(push);
		caster.setDeltaMovement(out.x, lift, out.z);
		caster.hurtMarked = true;
	}

	private static void hazard(ServerLevel chamber, GoreboundHulkEntity hulk, int kind, BossFootprint shape, Vec3 origin, int life) {
		hazard(chamber, hulk, kind, origin, origin.add(0, 0, 1), shape, life);
	}

	private static void hazard(ServerLevel chamber, GoreboundHulkEntity hulk, int kind, Vec3 origin, Vec3 aim,
			BossFootprint shape, int life) {
		BossGroundHazardEntity.spawn(chamber, hulk, kind, shape, FootprintFrame.simple(origin, aim, aim, origin.y), life,
				Integer.MAX_VALUE);
	}

	/** The Hulk stays in its ring: knocked or walked past the curb, it is set back inside it. */
	private static void keepInRing(GoreboundHulkEntity hulk, Vec3 floor) {
		double limit = OrdealStage.RING_RADIUS - 1.0D;
		Vec3 out = new Vec3(hulk.getX() - floor.x, 0.0D, hulk.getZ() - floor.z);
		if (out.length() <= limit) return;
		Vec3 back = floor.add(out.normalize().scale(limit));
		hulk.teleportTo(back.x, Math.max(hulk.getY(), floor.y), back.z);
	}

	private static void face(GoreboundHulkEntity hulk, Vec3 aim) {
		float yaw = (float) (Mth.atan2(aim.z, aim.x) * Mth.RAD_TO_DEG) - 90.0F;
		hulk.setYRot(yaw);
		hulk.setYHeadRot(yaw);
		hulk.setYBodyRot(yaw);
	}

	private static Vec3 flat(Vec3 v) {
		Vec3 f = new Vec3(v.x, 0.0D, v.z);
		return f.lengthSqr() < 1.0E-4D ? Vec3.ZERO : f.normalize();
	}

	private static double horizontal(Vec3 a, Vec3 b) {
		return Math.hypot(a.x - b.x, a.z - b.z);
	}

	/** Whether the Hulk is in one of its moves, when its ordinary blow waits. */
	public static boolean busy(UUID hulk) {
		State state = STATES.get(hulk);
		return state != null && state.move >= 0;
	}

	/** Whether the Hulk has a fighter held over its head right now. */
	public static boolean holding(UUID hulk, long now) {
		State state = STATES.get(hulk);
		if (state == null || state.move != GRAB || !state.landed) return false;
		long age = now - state.startedAt;
		return age >= GRAB_WINDUP && age < GRAB_WINDUP + GRAB_HOLD;
	}

	static void forget(UUID hulk) {
		STATES.remove(hulk);
	}

	public static void clearSessionState() {
		STATES.clear();
	}
}
