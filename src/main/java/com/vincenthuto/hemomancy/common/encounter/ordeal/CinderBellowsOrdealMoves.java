package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/**
 * The Cinder Bellows' moves in the Fire Breather's ordeal (2026-11-17): the true self is a bellows-beast of ember and
 * breath, not a summon that only breathes on a count. Between its ordinary breaths it inhales and blasts a cone of fire
 * (the inhale drawing a fighter in front of it toward its mouth), vents the furnace in its ribs all round it, lashes
 * its coil round in a ring the fighter must jump, and lunges down a lane jaws first. Each is drawn on the floor by the
 * ordeal's bold telegraph pass from the first tick of its wind-up, posed by the true Bellows' model from the same entity
 * event, and landed here. The hits are {@link FireEaterOrdealGeometry}.
 */
public final class CinderBellowsOrdealMoves {
	public static final int BLAST = 0, VENT = 1, LASH = 2, LUNGE = 3;
	/** The Bellows' footprints, in the Fire Breather's block of {@link OrdealHazardKinds}. */
	public static final int KIND_BLAST = OrdealHazardKinds.FIRE_EATER + 4, KIND_VENT = KIND_BLAST + 1, KIND_LASH = KIND_BLAST + 2,
			KIND_LUNGE = KIND_BLAST + 3;
	/** Wind-ups, in ticks: the inhale before the blast, the furnace stoked before the vent, the coil drawn, the crouch. */
	public static final int BLAST_WINDUP = 22, VENT_WINDUP = 14, LASH_WINDUP = 16, LUNGE_WINDUP = 14;
	/** The blast's burn, and the lunge's run. */
	public static final int BLAST_BURN = 10, LUNGE_RUN = 8;
	/** How long each move takes from its start to the Bellows' next choice, recovery included. */
	private static final int[] LENGTH = {BLAST_WINDUP + BLAST_BURN + 18, VENT_WINDUP + 18,
			LASH_WINDUP + FireEaterOrdealGeometry.LASH_TICKS + 14, LUNGE_WINDUP + LUNGE_RUN + 18};
	public static final double LUNGE_SPEED = .9D, LUNGE_LENGTH = LUNGE_SPEED * LUNGE_RUN;
	private static final float BLAST_DAMAGE = 6.0F, VENT_DAMAGE = 5.0F, LASH_DAMAGE = 5.0F, LUNGE_DAMAGE = 6.0F;
	/** Quiet ticks between moves, when its ordinary breath carries the fight. */
	private static final int REST = 30;
	private static final Map<UUID, State> STATES = new HashMap<>();

	/** The Bellows' moves as its ordeal's {@link OrdealPuppetMoves}. */
	public static final OrdealPuppetMoves MOVES = new OrdealPuppetMoves() {
		@Override
		public void tick(ServerLevel chamber, ServerPlayer caster, Mob puppet, Vec3 floor) {
			if (puppet instanceof CinderBellowsEntity bellows) CinderBellowsOrdealMoves.tick(chamber, caster, bellows, floor);
		}

		@Override
		public void forget(UUID puppet) {
			CinderBellowsOrdealMoves.forget(puppet);
		}

		@Override
		public void clearSessionState() {
			CinderBellowsOrdealMoves.clearSessionState();
		}
	};

	private CinderBellowsOrdealMoves() {
	}

	private static final class State {
		int move = -1;
		long startedAt;
		long nextAt;
		Vec3 aim = Vec3.ZERO;
		boolean landed;
	}

	static void tick(ServerLevel chamber, ServerPlayer caster, CinderBellowsEntity bellows, Vec3 floor) {
		long now = chamber.getGameTime();
		State state = STATES.computeIfAbsent(bellows.getUUID(), ignored -> {
			State fresh = new State();
			fresh.nextAt = now + 50;
			return fresh;
		});
		OrdealKit.keepInRing(bellows, floor, OrdealStage.RING_RADIUS - 1.0D);
		if (state.move < 0) {
			// Its ordinary breath is let finish its burn before a move takes over.
			if (now < state.nextAt || bellows.breathBurning()) return;
			start(chamber, caster, bellows, state, floor, now);
			return;
		}
		int age = (int) (now - state.startedAt);
		bellows.getNavigation().stop();
		Vec3 feet = new Vec3(bellows.getX(), floor.y, bellows.getZ());
		switch (state.move) {
			case BLAST -> blast(chamber, caster, bellows, state, age, feet);
			case VENT -> vent(chamber, caster, bellows, state, age, feet);
			case LASH -> lash(chamber, caster, bellows, state, age, feet);
			case LUNGE -> lunge(chamber, caster, bellows, state, age, floor);
			default -> {
			}
		}
		if (age >= LENGTH[state.move]) {
			state.move = -1;
			state.nextAt = now + REST;
		}
	}

	/** Picks by range: close in it vents or lashes, at middle range it blasts or lashes, from afar it lunges or blasts. */
	private static void start(ServerLevel chamber, ServerPlayer caster, CinderBellowsEntity bellows, State state, Vec3 floor,
			long now) {
		double distance = OrdealKit.horizontal(bellows.position(), caster.position());
		int roll = (int) Math.floorMod(now / 7 + caster.getUUID().getLeastSignificantBits(), 2);
		int move = distance < 3.0D ? (roll == 0 ? VENT : LASH)
				: distance <= FireEaterOrdealGeometry.BLAST_RADIUS ? (roll == 0 ? BLAST : LASH)
				: (roll == 0 ? LUNGE : BLAST);
		state.move = move;
		state.startedAt = now;
		state.landed = false;
		state.aim = OrdealKit.flat(caster.position().subtract(bellows.position()));
		if (state.aim.lengthSqr() < 1.0E-4D) state.aim = Vec3.directionFromRotation(0, bellows.getYRot());
		OrdealKit.face(bellows, state.aim);
		bellows.ordealMove(move);
		bellows.setDemonstrationCycle(0);
		Vec3 feet = new Vec3(bellows.getX(), floor.y, bellows.getZ());
		switch (move) {
			case BLAST -> OrdealKit.hazard(chamber, bellows, KIND_BLAST, new BossFootprint.Cone(0.0D,
					FireEaterOrdealGeometry.BLAST_RADIUS, FireEaterOrdealGeometry.BLAST_DEGREES, 0.0D, BossFootprint.Height.STANDING),
					feet, feet.add(state.aim), BLAST_WINDUP + BLAST_BURN);
			case VENT -> OrdealKit.hazard(chamber, bellows, KIND_VENT, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
					FireEaterOrdealGeometry.VENT_RADIUS, BossFootprint.Height.STANDING), feet, feet, VENT_WINDUP + 6);
			case LASH -> OrdealKit.hazard(chamber, bellows, KIND_LASH, new BossFootprint.Ring(FireEaterOrdealGeometry.LASH_TO,
					FireEaterOrdealGeometry.LASH_TO, .8D, 1, 0, LASH_WINDUP, BossFootprint.Height.JUMPABLE), feet, feet, LASH_WINDUP);
			case LUNGE -> OrdealKit.hazard(chamber, bellows, KIND_LUNGE, new BossFootprint.Lane(LUNGE_LENGTH + 1.5D, 1.1D, 0, 0.0D,
					0.0D, 0.0D, 0, BossFootprint.Height.STANDING), feet, feet.add(state.aim), LUNGE_WINDUP + LUNGE_RUN);
			default -> {
			}
		}
		chamber.playSound(null, bellows.getX(), bellows.getY() + 1.5D, bellows.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(),
				SoundSource.HOSTILE, 2.0F, move == BLAST ? .6F : .85F);
	}

	private static void blast(ServerLevel chamber, ServerPlayer caster, CinderBellowsEntity bellows, State state, int age, Vec3 feet) {
		Vec3 mouth = bellows.position().add(0, 1.9D, 0).add(state.aim.scale(.6D));
		OrdealKit.face(bellows, state.aim);
		if (age < BLAST_WINDUP) {
			// The inhale: air and embers dragged into the mouth from the cone, a fighter in front pulled toward it.
			Vec3 from = mouth.add(state.aim.scale(4.0D + (age % 4)));
			chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, .35F, .7F, 8), from.x, from.y - .8D, from.z, 3,
					1.2D, .4D, 1.2D, 0.0D);
			CircusEffects.gather(chamber, mouth, CircusEffects.FLAME, age / (float) BLAST_WINDUP);
			Vec3 pull = FireEaterOrdealGeometry.inhalePull(feet, state.aim, caster.position());
			if (pull.lengthSqr() > 0.0D) {
				caster.push(pull.x, 0.0D, pull.z);
				caster.hurtMarked = true;
			}
			return;
		}
		int since = age - BLAST_WINDUP;
		if (since >= BLAST_BURN) return;
		if (since == 0) {
			chamber.playSound(null, mouth.x, mouth.y, mouth.z, SoundInit.ITEM_LIVING_TORCH_BREATH_LOOP.get(), SoundSource.HOSTILE,
					3.5F, .7F);
			chamber.playSound(null, mouth.x, mouth.y, mouth.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 2.0F, 1.2F);
		}
		for (double yaw : new double[]{-24.0D, -12.0D, 0.0D, 12.0D, 24.0D}) {
			Vec3 tongue = state.aim.yRot((float) Math.toRadians(yaw)).add(0.0D, -.22D, 0.0D);
			CircusEffects.flameStream(chamber, mouth, tongue, 8, .7D, 2, .22D);
		}
		if (!state.landed && FireEaterOrdealGeometry.blastHits(feet, state.aim, caster.position())) {
			state.landed = true;
			strike(chamber, caster, bellows, BLAST_DAMAGE, state.aim, 1.2D, .4D);
		}
	}

	private static void vent(ServerLevel chamber, ServerPlayer caster, CinderBellowsEntity bellows, State state, int age, Vec3 feet) {
		Vec3 chest = bellows.position().add(0, 1.2D, 0);
		if (age < VENT_WINDUP) {
			// Stoking: the ribs glowing through, sparks spitting from between them.
			chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME_CORE, .4F, .9F, 6), chest.x, chest.y, chest.z,
					1 + age / 4, .5D, .5D, .5D, .05D);
			return;
		}
		if (age != VENT_WINDUP) return;
		chamber.playSound(null, chest.x, chest.y, chest.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F, .7F);
		chamber.playSound(null, chest.x, chest.y, chest.z, SoundInit.ITEM_LIVING_TORCH_BREATH_LOOP.get(), SoundSource.HOSTILE, 3.0F,
				.9F);
		for (int i = 0; i < 16; i++) {
			Vec3 out = Vec3.directionFromRotation(0, i * 22.5F);
			CircusEffects.flameStream(chamber, chest, out.add(0, .15D, 0), 5, .7D, 1, .2D);
		}
		CircusEffects.impact(chamber, feet.add(0, .4, 0), FireEaterOrdealGeometry.VENT_RADIUS);
		if (FireEaterOrdealGeometry.ventHits(feet, caster.position()))
			strike(chamber, caster, bellows, VENT_DAMAGE, caster.position().subtract(feet), 1.1D, .45D);
	}

	private static void lash(ServerLevel chamber, ServerPlayer caster, CinderBellowsEntity bellows, State state, int age, Vec3 feet) {
		if (age < LASH_WINDUP) return;
		int since = age - LASH_WINDUP;
		if (since == 0) {
			OrdealKit.hazard(chamber, bellows, KIND_LASH, new BossFootprint.Ring(FireEaterOrdealGeometry.LASH_FROM,
					FireEaterOrdealGeometry.LASH_TO, FireEaterOrdealGeometry.LASH_BAND, 1, 0, FireEaterOrdealGeometry.LASH_TICKS,
					BossFootprint.Height.JUMPABLE), feet, feet, FireEaterOrdealGeometry.LASH_TICKS + 2);
			chamber.playSound(null, feet.x, feet.y, feet.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.5F, .7F);
		}
		if (since > FireEaterOrdealGeometry.LASH_TICKS) return;
		double radius = FireEaterOrdealGeometry.lashRadius(since);
		for (int i = 0; i < 12; i++) {
			double a = Math.PI * 2.0D * i / 12 + since * .4D;
			chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, .5F, .9F, 10), feet.x + Math.cos(a) * radius,
					feet.y + .2D, feet.z + Math.sin(a) * radius, 1, .1D, .05D, .1D, .02D);
		}
		if (!state.landed && FireEaterOrdealGeometry.lashHits(feet, radius, caster.position())) {
			state.landed = true;
			strike(chamber, caster, bellows, LASH_DAMAGE, caster.position().subtract(feet), .8D, .4D);
		}
	}

	private static void lunge(ServerLevel chamber, ServerPlayer caster, CinderBellowsEntity bellows, State state, int age, Vec3 floor) {
		if (age < LUNGE_WINDUP || age >= LUNGE_WINDUP + LUNGE_RUN) return;
		// The lunge ends at the curb: it never carries the Bellows out of its ring.
		Vec3 next = bellows.position().add(state.aim.scale(LUNGE_SPEED));
		if (OrdealKit.horizontal(next, floor) > OrdealStage.RING_RADIUS - 1.5D) {
			if (!state.landed) CircusEffects.impact(chamber, bellows.position().add(0, .4, 0), 1.2D);
			state.landed = true;
			return;
		}
		bellows.move(MoverType.SELF, state.aim.scale(LUNGE_SPEED));
		OrdealKit.face(bellows, state.aim);
		Vec3 mouth = bellows.position().add(0, 1.2D, 0).add(state.aim.scale(1.2D));
		chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, .6F, .9F, 12), mouth.x, mouth.y, mouth.z, 4, .3D, .3D,
				.3D, .02D);
		if (!state.landed && bellows.getBoundingBox().inflate(.7D).intersects(caster.getBoundingBox())) {
			state.landed = true;
			chamber.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE,
					3.0F, .8F);
			strike(chamber, caster, bellows, LUNGE_DAMAGE, state.aim, 1.5D, .55D);
		}
	}

	private static void strike(ServerLevel chamber, ServerPlayer caster, CinderBellowsEntity bellows, float damage, Vec3 away,
			double push, double lift) {
		OrdealKit.strike(chamber, caster, bellows, damage, away, push, lift);
	}

	/** Whether the Bellows is in one of its moves, when its ordinary breath waits. */
	public static boolean busy(UUID bellows) {
		State state = STATES.get(bellows);
		return state != null && state.move >= 0;
	}

	static void forget(UUID bellows) {
		STATES.remove(bellows);
	}

	public static void clearSessionState() {
		STATES.clear();
	}
}
