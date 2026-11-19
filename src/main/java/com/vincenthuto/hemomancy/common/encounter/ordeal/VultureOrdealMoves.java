package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static com.vincenthuto.hemomancy.common.encounter.ordeal.AcrobatOrdealHits.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * The Veinwing Vulture's moves in the Acrobat's ordeal (2026-11-17): its true self is a bird of prey, not a summon
 * that pecks. Between its ordinary talon blows and feather volleys it stoops from high over a mark and stays down a
 * moment where it hits, rakes low down a lane with its talons, beats a downdraft onto the floor under it, and snatches
 * a fighter who stands too close, carries them up and drops them. Each is drawn on the floor by the ordeal's bold pass
 * from the first tick of its wind-up, posed by the true Vulture's model from the same entity event, and landed here.
 */
public final class VultureOrdealMoves {
	public static final int STOOP = 0, RAKE = 1, DOWNDRAFT = 2, CARRY = 3;
	/** Wind-ups, in ticks: each move's gather before it lands. */
	public static final int STOOP_WINDUP = 20, RAKE_WINDUP = 14, DOWNDRAFT_WINDUP = 22, CARRY_WINDUP = 12;
	/** The stoop's dive and its time on the ground, the rake's pass, the carry's climb. */
	public static final int STOOP_DIVE = 5, STOOP_DOWN = 18, RAKE_RUN = 11, CARRY_HOLD = 18;
	/** How long each move takes from its start to the Vulture's next choice, recovery included. */
	private static final int[] LENGTH = {STOOP_WINDUP + STOOP_DIVE + STOOP_DOWN + 4, RAKE_WINDUP + RAKE_RUN + 12,
			DOWNDRAFT_WINDUP + 16, CARRY_WINDUP + CARRY_HOLD + 10};
	public static final double RAKE_LENGTH = RAKE_SPEED * RAKE_RUN + 1.0D;
	private static final float STOOP_DAMAGE = 6.0F, RAKE_DAMAGE = 5.0F, DOWNDRAFT_DAMAGE = 3.0F, CARRY_DAMAGE = 3.0F;
	/** How high a carried fighter is lifted over the plate before the drop. */
	private static final double CARRY_TOP = 5.5D;
	/** Quiet ticks between moves, when its ordinary blows carry the fight. */
	private static final int REST = 30;
	private static final Map<UUID, State> STATES = new HashMap<>();

	/** The Vulture's moves as its ordeal's {@link OrdealPuppetMoves}. */
	public static final OrdealPuppetMoves MOVES = new OrdealPuppetMoves() {
		@Override
		public void tick(ServerLevel chamber, ServerPlayer caster, Mob puppet, Vec3 floor) {
			if (puppet instanceof VeinwingVultureEntity vulture) VultureOrdealMoves.tick(chamber, caster, vulture, floor);
		}

		@Override
		public void forget(UUID puppet) {
			VultureOrdealMoves.forget(puppet);
		}

		@Override
		public void clearSessionState() {
			VultureOrdealMoves.clearSessionState();
		}

		@Override
		public boolean holding(UUID puppet, long now) {
			return VultureOrdealMoves.holding(puppet, now);
		}
	};

	private VultureOrdealMoves() {
	}

	private static final class State {
		int move = -1;
		long startedAt;
		long nextAt;
		Vec3 aim = Vec3.ZERO;
		Vec3 mark = Vec3.ZERO;
		Vec3 from = Vec3.ZERO;
		boolean landed;
		boolean stopped;
	}

	static void tick(ServerLevel chamber, ServerPlayer caster, VeinwingVultureEntity vulture, Vec3 floor) {
		long now = chamber.getGameTime();
		State state = STATES.computeIfAbsent(vulture.getUUID(), ignored -> {
			State fresh = new State();
			fresh.nextAt = now + 50;
			return fresh;
		});
		OrdealKit.keepInRing(vulture, floor, OrdealStage.RING_RADIUS - 1.0D);
		if (vulture.getY() > floor.y + 9.0D) vulture.setPos(vulture.getX(), floor.y + 9.0D, vulture.getZ());
		if (state.move < 0) {
			if (now < state.nextAt) return;
			start(chamber, caster, vulture, state, floor, now);
			return;
		}
		int age = (int) (now - state.startedAt);
		vulture.getNavigation().stop();
		switch (state.move) {
			case STOOP -> stoop(chamber, caster, vulture, state, age, floor);
			case RAKE -> rake(chamber, caster, vulture, state, age, floor);
			case DOWNDRAFT -> downdraft(chamber, caster, vulture, state, age, floor);
			case CARRY -> carry(chamber, caster, vulture, state, age, floor);
			default -> {
			}
		}
		if (age >= LENGTH[state.move] || state.move == CARRY && age >= CARRY_WINDUP + 8 && !state.landed) {
			state.move = -1;
			state.nextAt = now + REST;
		}
	}

	/** Picks by range: from afar it stoops or rakes; up close it carries or beats the downdraft. */
	private static void start(ServerLevel chamber, ServerPlayer caster, VeinwingVultureEntity vulture, State state, Vec3 floor,
			long now) {
		double distance = horizontal(vulture.position(), caster.position());
		int roll = (int) Math.floorMod(now / 7 + caster.getUUID().getLeastSignificantBits(), 3);
		int move = distance > 6.0D ? new int[]{STOOP, RAKE, STOOP}[roll]
				: distance < CARRY_REACH + 0.6D ? new int[]{CARRY, DOWNDRAFT, STOOP}[roll]
				: new int[]{STOOP, DOWNDRAFT, RAKE}[roll];
		Vec3 feet = new Vec3(caster.getX(), floor.y, caster.getZ());
		state.move = move;
		state.startedAt = now;
		state.landed = false;
		state.stopped = false;
		state.mark = feet;
		state.from = vulture.position();
		state.aim = flat(caster.position().subtract(vulture.position()));
		if (state.aim.lengthSqr() < 1.0E-4D) state.aim = Vec3.directionFromRotation(0, vulture.getYRot());
		OrdealKit.face(vulture, state.aim);
		vulture.ordealMove(move);
		switch (move) {
			case STOOP -> OrdealKit.hazard(chamber, vulture, KIND_STOOP, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
					STOOP_RADIUS, BossFootprint.Height.STANDING), feet, feet, STOOP_WINDUP + STOOP_DIVE + 2);
			case RAKE -> {
				// The pass starts a step back from where it hovers, low, and runs on through the fighter.
				Vec3 back = vulture.position().subtract(state.aim.scale(1.5D));
				if (!insideRing(floor, back, 1.2D)) back = vulture.position();
				state.from = new Vec3(back.x, floor.y + 1.0D, back.z);
				Vec3 lane = new Vec3(back.x, floor.y, back.z);
				OrdealKit.hazard(chamber, vulture, KIND_RAKE, new BossFootprint.Lane(RAKE_LENGTH, RAKE_HALF_WIDTH, 0, 0.0D, 0.0D,
						0.0D, 0, BossFootprint.Height.STANDING), lane, lane.add(state.aim), RAKE_WINDUP + RAKE_RUN);
			}
			case DOWNDRAFT -> OrdealKit.hazard(chamber, vulture, KIND_DOWNDRAFT, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
					DOWNDRAFT_RADIUS, BossFootprint.Height.STANDING), feet, feet, DOWNDRAFT_WINDUP + 3);
			case CARRY -> OrdealKit.hazard(chamber, vulture, KIND_CARRY, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
					CARRY_REACH, BossFootprint.Height.STANDING), feet, feet, CARRY_WINDUP + 4);
			default -> {
			}
		}
		chamber.playSound(null, vulture.getX(), vulture.getY(), vulture.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.HOSTILE, 2.5F, .6F);
		chamber.playSound(null, vulture.getX(), vulture.getY(), vulture.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(),
				SoundSource.HOSTILE, 1.5F, 1.4F);
	}

	private static void stoop(ServerLevel chamber, ServerPlayer caster, VeinwingVultureEntity vulture, State state, int age, Vec3 floor) {
		Vec3 over = state.mark.add(0.0D, STOOP_HEIGHT, 0.0D);
		if (age < STOOP_WINDUP) {
			// Climbing hard over the mark, beating, then hanging there as the wings fold.
			glide(vulture, over, age < STOOP_WINDUP - 6 ? .2D : .45D);
			Vec3 toward = flat(state.mark.subtract(vulture.position()));
			OrdealKit.face(vulture, toward.lengthSqr() > 0.0D ? toward : state.aim);
			if (age == STOOP_WINDUP - 1) state.from = vulture.position();
			if (age % 5 == 0) chamber.playSound(null, vulture.getX(), vulture.getY(), vulture.getZ(), SoundEvents.PHANTOM_FLAP,
					SoundSource.HOSTILE, 2.0F, .7F);
			return;
		}
		int dive = age - STOOP_WINDUP;
		if (dive < STOOP_DIVE) {
			if (dive == 0) chamber.playSound(null, vulture.getX(), vulture.getY(), vulture.getZ(), SoundEvents.PHANTOM_SWOOP,
					SoundSource.HOSTILE, 3.0F, .6F);
			double t = (dive + 1) / (double) STOOP_DIVE;
			double x = HemoEasing.lerp(state.from.x, state.mark.x, t * t), z = HemoEasing.lerp(state.from.z, state.mark.z, t * t);
			double y = state.mark.y + stoopHeight(dive + 1);
			place(vulture, new Vec3(x, y, z));
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), x, y + 1.0D, z, 5, .2D, .6D, .2D, 0.0D);
			if (dive == STOOP_DIVE - 1) {
				landed(chamber, state.mark.add(0, .4D, 0), STOOP_RADIUS + .6D);
				CircusEffects.dust(chamber, state.mark.add(0, .2D, 0), 30);
				if (discHits(state.mark, STOOP_RADIUS, caster.position()))
					OrdealKit.strike(chamber, caster, vulture, STOOP_DAMAGE, caster.position().subtract(state.mark), .9D, .5D);
			}
			return;
		}
		// Down on the floor where it hit, sprawled, wings beating weakly: the moment to punish it.
		if (age < STOOP_WINDUP + STOOP_DIVE + STOOP_DOWN) {
			place(vulture, new Vec3(vulture.getX(), state.mark.y + .05D, vulture.getZ()));
			if (age % 6 == 0) CircusEffects.dust(chamber, vulture.position().add(0, .2D, 0), 5);
		}
	}

	private static void rake(ServerLevel chamber, ServerPlayer caster, VeinwingVultureEntity vulture, State state, int age, Vec3 floor) {
		if (age < RAKE_WINDUP) {
			// Pulled back and low, talons drawn up, facing down the lane.
			glide(vulture, state.from, .3D);
			OrdealKit.face(vulture, state.aim);
			return;
		}
		int run = age - RAKE_WINDUP;
		if (run < RAKE_RUN) {
			if (run == 0) chamber.playSound(null, vulture.getX(), vulture.getY(), vulture.getZ(), SoundEvents.PHANTOM_SWOOP,
					SoundSource.HOSTILE, 3.0F, .8F);
			if (state.stopped) return;
			Vec3 next = rakeAt(state.from, state.aim, run + 1);
			// The pass ends at the curb: it never carries the Vulture out of the ring.
			if (!insideRing(floor, next, 1.2D)) {
				state.stopped = true;
				return;
			}
			place(vulture, next);
			OrdealKit.face(vulture, state.aim);
			if (run % 2 == 0) chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), next.x, floor.y + .3D, next.z, 3, .3D,
					.05D, .3D, 0.0D);
			if (!state.landed && vulture.getBoundingBox().inflate(.5D).intersects(caster.getBoundingBox())) {
				state.landed = true;
				landed(chamber, caster.position().add(0, 1.0D, 0), .9D);
				Vec3 aside = new Vec3(-state.aim.z, 0.0D, state.aim.x);
				if (aside.dot(caster.position().subtract(vulture.position())) < 0.0D) aside = aside.scale(-1.0D);
				OrdealKit.strike(chamber, caster, vulture, RAKE_DAMAGE, aside, .9D, .35D);
			}
			return;
		}
		// The flare after the pass: up out of it.
		glide(vulture, new Vec3(vulture.getX(), floor.y + 2.2D, vulture.getZ()).add(state.aim.scale(.6D)), .15D);
	}

	private static void downdraft(ServerLevel chamber, ServerPlayer caster, VeinwingVultureEntity vulture, State state, int age,
			Vec3 floor) {
		Vec3 over = state.mark.add(0.0D, DOWNDRAFT_HEIGHT, 0.0D);
		if (age < DOWNDRAFT_WINDUP) {
			// Rising over the fighter, the wings raised high and trembling.
			glide(vulture, over, .25D);
			if (age > DOWNDRAFT_WINDUP - 8)
				chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), vulture.getX(), vulture.getY() + 1.5D, vulture.getZ(),
						3, 1.2D, .3D, 1.2D, 0.0D);
			return;
		}
		if (age == DOWNDRAFT_WINDUP) {
			chamber.playSound(null, over.x, over.y, over.z, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 3.0F, .7F);
			chamber.playSound(null, state.mark.x, state.mark.y, state.mark.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE,
					2.5F, .9F);
			// The air hitting the plate: a ring of dust and blood light rolling out from under it.
			for (int angle = 0; angle < 360; angle += 30) {
				double a = Math.toRadians(angle);
				Vec3 at = state.mark.add(Math.cos(a) * DOWNDRAFT_RADIUS * .8D, .3D, Math.sin(a) * DOWNDRAFT_RADIUS * .8D);
				CircusEffects.dust(chamber, at, 6);
			}
			BossTelegraphs.ring(chamber, state.mark.add(0, .15D, 0), DOWNDRAFT_RADIUS, CircusEffects.CRIMSON, 32);
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), state.mark.x, state.mark.y + 2.0D, state.mark.z, 30, .8D,
					1.2D, .8D, 0.0D);
			if (discHits(state.mark, DOWNDRAFT_RADIUS, caster.position()))
				OrdealKit.strike(chamber, caster, vulture, DOWNDRAFT_DAMAGE, downdraftShove(state.mark, caster.position()), 1.0D, -.8D);
		}
		place(vulture, vulture.position());
	}

	private static void carry(ServerLevel chamber, ServerPlayer caster, VeinwingVultureEntity vulture, State state, int age, Vec3 floor) {
		if (age < CARRY_WINDUP) {
			// Closing on the fighter with its talons thrown forward.
			glide(vulture, caster.position().add(0.0D, 1.6D, 0.0D), .3D);
			OrdealKit.face(vulture, flat(caster.position().subtract(vulture.position())));
			return;
		}
		if (age == CARRY_WINDUP) {
			state.landed = horizontal(vulture.position(), caster.position()) <= CARRY_REACH
					&& Math.abs(vulture.getY() - (caster.getY() + 1.0D)) < 2.5D;
			if (!state.landed) {
				vulture.ordealMove(-1);
				return;
			}
			state.from = vulture.position();
			chamber.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundInit.ENTITY_VESPER_GRAB_BITE.get(),
					SoundSource.HOSTILE, 2.0F, 1.2F);
			landed(chamber, caster.position().add(0, 1.0D, 0), .6D);
		}
		if (!state.landed) return;
		int held = age - CARRY_WINDUP;
		if (held < CARRY_HOLD) {
			// Labouring up with the fighter under its talons, drifting toward the ring's middle.
			double t = (held + 1) / (double) CARRY_HOLD;
			Vec3 toward = floor.subtract(state.from).multiply(1, 0, 1).scale(.35D * t);
			Vec3 at = new Vec3(state.from.x + toward.x, floor.y + 2.0D + CARRY_TOP * HemoEasing.smoothstep(t),
					state.from.z + toward.z);
			place(vulture, at);
			caster.teleportTo(at.x, at.y - 2.0D, at.z);
			caster.setDeltaMovement(Vec3.ZERO);
			caster.hurtMarked = true;
			if (held % 4 == 0) chamber.playSound(null, at.x, at.y, at.z, SoundEvents.PHANTOM_FLAP, SoundSource.HOSTILE, 2.0F, .55F);
		} else if (held == CARRY_HOLD) {
			// Let go over the plate: the fall does the rest.
			chamber.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundInit.ENTITY_VESPER_GRAB_RELEASE.get(),
					SoundSource.HOSTILE, 2.0F, 1.0F);
			OrdealKit.strike(chamber, caster, vulture, CARRY_DAMAGE, Vec3.ZERO, 0.0D, 0.0D);
		}
	}

	/** Eases the body toward {@code to}: a bird's pull, quick when far, settling as it arrives. */
	private static void glide(VeinwingVultureEntity vulture, Vec3 to, double pull) {
		place(vulture, vulture.position().add(to.subtract(vulture.position()).scale(pull)));
	}

	private static void place(VeinwingVultureEntity vulture, Vec3 at) {
		vulture.setPos(at.x, at.y, at.z);
		vulture.setDeltaMovement(Vec3.ZERO);
	}

	/** A hit landing: a thud and a burst of blood. */
	private static void landed(ServerLevel chamber, Vec3 at, double size) {
		chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F, .8F);
		CircusEffects.impact(chamber, at, size);
	}

	/** Whether the Vulture is in one of its moves, when its ordinary blows wait. */
	public static boolean busy(UUID vulture) {
		State state = STATES.get(vulture);
		return state != null && state.move >= 0;
	}

	/** Whether the Vulture has a fighter in its talons right now. */
	public static boolean holding(UUID vulture, long now) {
		State state = STATES.get(vulture);
		if (state == null || state.move != CARRY || !state.landed) return false;
		long age = now - state.startedAt;
		return age >= CARRY_WINDUP && age < CARRY_WINDUP + CARRY_HOLD;
	}

	static void forget(UUID vulture) {
		STATES.remove(vulture);
	}

	public static void clearSessionState() {
		STATES.clear();
	}
}
