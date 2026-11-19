package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetRules;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/**
 * The Mnemonist Puppet's moves in the Retired Understudy's ordeal (2026-11-17): memory made flesh, it gives back what
 * it has been shown. Between its ordinary blows and replays it recites three blows walked forward (the last one the
 * fighter's own hardest blow, given back), casts three threads from its spool that reel a fighter in, lets go an echo
 * (a ring wave and its echo, both jumped) and lunges down a lane the way the fighter stepped in. Each is drawn on the
 * floor by the ordeal's bold telegraph pass from the first tick of its wind-up, posed by the true puppet's model from
 * the same entity event, and landed here. The hits are {@link UnderstudyOrdealGeometry}.
 */
public final class OrdealMnemonistMoves {
	public static final int RECITAL = 0, THREAD_CAST = 1, ECHO = 2, LUNGE = 3;
	/** Wind-ups, in ticks: each move's gather before it lands. */
	public static final int RECITAL_WINDUP = 14, THREAD_WINDUP = 16, ECHO_WINDUP = 16, LUNGE_WINDUP = 12;
	/** The recital's blows land this many ticks apart; the echo's second wave follows the first this late. */
	public static final int RECITAL_BEAT = 5, ECHO_GAP = 7;
	/** How long each move takes from its start to the puppet's next choice, recovery included. */
	private static final int[] LENGTH = {RECITAL_WINDUP + 2 * RECITAL_BEAT + 16, THREAD_WINDUP + 18,
			ECHO_WINDUP + ECHO_GAP + UnderstudyOrdealGeometry.ECHO_TRAVEL + 12,
			LUNGE_WINDUP + UnderstudyOrdealGeometry.LUNGE_RUN + 16};
	private static final float RECITAL_DAMAGE = 4.0F, THREAD_DAMAGE = 4.0F, ECHO_DAMAGE = 4.0F, LUNGE_DAMAGE = 6.0F;
	/** The recital's last blow gives back the fighter's own hardest recent blow, at the replay's share, within these. */
	private static final float ENCORE_MIN = 4.0F, ENCORE_MAX = 7.0F;
	/** How hard a caught thread reels the fighter in. */
	private static final double REEL = 1.15D;
	/** Quiet ticks between moves, when its ordinary blows and replays carry the fight. */
	private static final int REST = 30;
	private static final Map<UUID, State> STATES = new HashMap<>();

	/** The puppet's moves as its ordeal's {@link OrdealPuppetMoves}. */
	public static final OrdealPuppetMoves MOVES = new OrdealPuppetMoves() {
		@Override
		public void tick(ServerLevel chamber, ServerPlayer caster, Mob puppet, Vec3 floor) {
			if (puppet instanceof MnemonistPuppetEntity mnemonist) OrdealMnemonistMoves.tick(chamber, caster, mnemonist, floor);
		}

		@Override
		public void forget(UUID puppet) {
			OrdealMnemonistMoves.forget(puppet);
		}

		@Override
		public void clearSessionState() {
			OrdealMnemonistMoves.clearSessionState();
		}
	};

	private OrdealMnemonistMoves() {
	}

	private static final class State {
		int move = -1;
		long startedAt;
		long nextAt;
		Vec3 aim = Vec3.ZERO;
		Vec3 origin = Vec3.ZERO;
		boolean landed;
		int hits;
		/** Its health last tick, and the hardest blow it has taken lately: the recital's encore gives it back. */
		float lastHealth = -1.0F, hardest;
		long hardestAt;
	}

	static void tick(ServerLevel chamber, ServerPlayer caster, MnemonistPuppetEntity puppet, Vec3 floor) {
		long now = chamber.getGameTime();
		State state = STATES.computeIfAbsent(puppet.getUUID(), ignored -> {
			State fresh = new State();
			fresh.nextAt = now + 50;
			return fresh;
		});
		remember(state, puppet, now);
		OrdealKit.keepInRing(puppet, floor, OrdealStage.RING_RADIUS - 1.0D);
		if (state.move < 0) {
			if (now < state.nextAt) return;
			start(chamber, caster, puppet, state, floor, now);
			return;
		}
		int age = (int) (now - state.startedAt);
		puppet.getNavigation().stop();
		switch (state.move) {
			case RECITAL -> recital(chamber, caster, puppet, state, age, floor);
			case THREAD_CAST -> threadCast(chamber, caster, puppet, state, age);
			case ECHO -> echo(chamber, caster, puppet, state, age, floor);
			case LUNGE -> lunge(chamber, caster, puppet, state, age, floor);
			default -> {
			}
		}
		if (age >= LENGTH[state.move]) {
			state.move = -1;
			state.nextAt = now + REST;
		}
	}

	/** Reads the blows it takes from its health, so the encore can give the hardest of the last few seconds back. */
	private static void remember(State state, MnemonistPuppetEntity puppet, long now) {
		float health = puppet.getHealth();
		if (state.lastHealth >= 0.0F && health < state.lastHealth) {
			float taken = state.lastHealth - health;
			if (taken >= state.hardest || now - state.hardestAt > MnemonistPuppetRules.MEMORY_EXPIRY_TICKS) {
				state.hardest = taken;
				state.hardestAt = now;
			}
		}
		state.lastHealth = health;
	}

	/** Picks by range: up close it recites or echoes; from afar it lunges or casts its threads. */
	private static void start(ServerLevel chamber, ServerPlayer caster, MnemonistPuppetEntity puppet, State state, Vec3 floor,
			long now) {
		double distance = puppet.distanceTo(caster);
		int roll = (int) Math.floorMod(now / 7 + caster.getUUID().getLeastSignificantBits(), 2);
		int move = distance > 6.0D ? new int[]{LUNGE, THREAD_CAST}[roll]
				: distance < 3.2D ? new int[]{RECITAL, ECHO}[roll]
				: new int[]{THREAD_CAST, RECITAL}[roll];
		state.move = move;
		state.startedAt = now;
		state.landed = false;
		state.hits = 0;
		state.aim = OrdealKit.flat(caster.position().subtract(puppet.position()));
		if (state.aim.lengthSqr() < 1.0E-4D) state.aim = Vec3.directionFromRotation(0, puppet.getYRot());
		Vec3 feet = new Vec3(puppet.getX(), floor.y, puppet.getZ());
		state.origin = feet;
		OrdealKit.face(puppet, state.aim);
		puppet.ordealMove(move);
		switch (move) {
			case RECITAL -> {
				// Three marks walked forward, each lighting as its blow comes.
				for (int blow = 0; blow < 3; blow++)
					OrdealKit.hazard(chamber, puppet, UnderstudyChoreography.KIND_RECITAL, new BossFootprint.Disc(
							BossFootprint.Anchor.ORIGIN, UnderstudyOrdealGeometry.RECITAL_RADIUS, BossFootprint.Height.STANDING),
							UnderstudyOrdealGeometry.recitalBlow(feet, state.aim, blow), feet,
							RECITAL_WINDUP + blow * RECITAL_BEAT + 6);
			}
			case THREAD_CAST -> {
				for (Vec3 thread : UnderstudyOrdealGeometry.threads(state.aim))
					OrdealKit.hazard(chamber, puppet, UnderstudyChoreography.KIND_THREAD, new BossFootprint.Lane(
							UnderstudyOrdealGeometry.THREAD_LENGTH, UnderstudyOrdealGeometry.THREAD_HALF, 0, 0.0D, 0.0D, 0.0D, 0,
							BossFootprint.Height.STANDING), feet, feet.add(thread), THREAD_WINDUP + 4);
			}
			case ECHO -> OrdealKit.hazard(chamber, puppet, UnderstudyChoreography.KIND_ECHO, new BossFootprint.Ring(
					UnderstudyOrdealGeometry.ECHO_RADIUS, UnderstudyOrdealGeometry.ECHO_RADIUS, .8D, 1, 0, ECHO_WINDUP,
					BossFootprint.Height.JUMPABLE), feet, feet, ECHO_WINDUP);
			case LUNGE -> OrdealKit.hazard(chamber, puppet, UnderstudyChoreography.KIND_LUNGE, new BossFootprint.Lane(
					UnderstudyOrdealGeometry.LUNGE_LENGTH + 1.0D, UnderstudyOrdealGeometry.LUNGE_HALF, 0, 0.0D, 0.0D, 0.0D, 0,
					BossFootprint.Height.STANDING), feet, feet.add(state.aim), LUNGE_WINDUP + UnderstudyOrdealGeometry.LUNGE_RUN);
			default -> {
			}
		}
		// A dry clatter of bisque as it gathers, and its threads drawing taut.
		chamber.playSound(null, puppet.getX(), puppet.getY() + 1.5D, puppet.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(),
				SoundSource.HOSTILE, 1.6F, 1.15F);
	}

	private static void recital(ServerLevel chamber, ServerPlayer caster, MnemonistPuppetEntity puppet, State state, int age,
			Vec3 floor) {
		if (age < RECITAL_WINDUP) {
			CircusEffects.gather(chamber, puppet.position().add(0, 1.4D, 0), CircusEffects.CRIMSON, age / (float) RECITAL_WINDUP);
			return;
		}
		int since = age - RECITAL_WINDUP;
		if (since % RECITAL_BEAT != 0 || since / RECITAL_BEAT > 2) return;
		int blow = since / RECITAL_BEAT;
		// Each blow walks it a step after the last.
		puppet.move(MoverType.SELF, state.aim.scale(blow == 0 ? 0.5D : UnderstudyOrdealGeometry.RECITAL_STEP));
		OrdealKit.face(puppet, state.aim);
		Vec3 at = UnderstudyOrdealGeometry.recitalBlow(state.origin, state.aim, blow);
		landed(chamber, at, blow == 2 ? 1.2D : .8D, blow == 2 ? .6F : .85F);
		if (UnderstudyOrdealGeometry.recitalHits(at, caster.position()) && state.hits < 2) {
			state.hits++;
			// The encore: the fighter's own hardest recent blow, given back at the replay's share.
			float damage = blow == 2 ? Math.clamp(MnemonistPuppetRules.replayDamage(state.hardest) * 2.0F, ENCORE_MIN, ENCORE_MAX)
					: RECITAL_DAMAGE;
			OrdealKit.strike(chamber, caster, puppet, damage, state.aim, blow == 2 ? 1.0D : .35D, blow == 2 ? .45D : .2D);
		}
	}

	private static void threadCast(ServerLevel chamber, ServerPlayer caster, MnemonistPuppetEntity puppet, State state, int age) {
		Vec3 spool = puppet.position().add(0, 1.2D, 0);
		List<Vec3> threads = UnderstudyOrdealGeometry.threads(state.aim);
		if (age < THREAD_WINDUP) {
			// The threads pay out along the floor, taut and shivering.
			if (age % 3 == 0)
				for (Vec3 thread : threads) {
					double reach = UnderstudyOrdealGeometry.THREAD_LENGTH * age / THREAD_WINDUP;
					chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), state.origin.x + thread.x * reach,
							state.origin.y + .3D, state.origin.z + thread.z * reach, 2, .05D, .05D, .05D, 0.0D);
				}
			return;
		}
		if (age != THREAD_WINDUP) return;
		chamber.playSound(null, spool.x, spool.y, spool.z, SoundInit.ENTITY_IMPALED_PROCESSION_THREAD_SEVER.get(),
				SoundSource.HOSTILE, 2.2F, .7F);
		for (Vec3 thread : threads) {
			Vec3 end = state.origin.add(thread.scale(UnderstudyOrdealGeometry.THREAD_LENGTH)).add(0, .4D, 0);
			CircusEffects.pullThread(chamber, end, spool, puppet.getUUID().getLeastSignificantBits() + (long) (thread.x * 100));
			CircusEffects.impact(chamber, end, .6D);
		}
		for (Vec3 thread : threads) {
			if (!UnderstudyOrdealGeometry.threadHits(state.origin, thread, caster.position())) continue;
			// Caught: hooked and reeled in toward the puppet, where its recital waits.
			OrdealKit.strike(chamber, caster, puppet, THREAD_DAMAGE, puppet.position().subtract(caster.position()), REEL, .35D);
			break;
		}
	}

	private static void echo(ServerLevel chamber, ServerPlayer caster, MnemonistPuppetEntity puppet, State state, int age,
			Vec3 floor) {
		Vec3 feet = state.origin;
		if (age < ECHO_WINDUP) {
			CircusEffects.gather(chamber, puppet.position().add(0, 1.0D, 0), CircusEffects.BONE, age / (float) ECHO_WINDUP);
			return;
		}
		for (int wave = 0; wave < 2; wave++) {
			int since = age - ECHO_WINDUP - wave * ECHO_GAP;
			if (since == 0) {
				landed(chamber, feet, 1.3D, wave == 0 ? .7F : 1.0F);
				OrdealKit.hazard(chamber, puppet, UnderstudyChoreography.KIND_ECHO, new BossFootprint.Ring(
						UnderstudyOrdealGeometry.ECHO_START, UnderstudyOrdealGeometry.ECHO_RADIUS, UnderstudyOrdealGeometry.ECHO_BAND,
						1, 0, UnderstudyOrdealGeometry.ECHO_TRAVEL, BossFootprint.Height.JUMPABLE), feet, feet,
						UnderstudyOrdealGeometry.ECHO_TRAVEL + 2);
			}
			if (since < 0 || since > UnderstudyOrdealGeometry.ECHO_TRAVEL || (state.hits & (1 << wave)) != 0) continue;
			if (UnderstudyOrdealGeometry.echoHits(feet, UnderstudyOrdealGeometry.echoRadius(since), caster.position())) {
				state.hits |= 1 << wave;
				OrdealKit.strike(chamber, caster, puppet, ECHO_DAMAGE, caster.position().subtract(feet), .6D, .45D);
			}
		}
	}

	private static void lunge(ServerLevel chamber, ServerPlayer caster, MnemonistPuppetEntity puppet, State state, int age,
			Vec3 floor) {
		if (age < LUNGE_WINDUP || age >= LUNGE_WINDUP + UnderstudyOrdealGeometry.LUNGE_RUN) return;
		// The lunge ends at the curb: it never carries the puppet out of its ring.
		Vec3 next = UnderstudyOrdealGeometry.lungeStep(puppet.position(), state.aim, floor, OrdealStage.RING_RADIUS - 1.5D);
		if (next == null) {
			if (!state.landed) landed(chamber, puppet.position(), .9D, .9F);
			state.landed = true;
			return;
		}
		puppet.move(MoverType.SELF, state.aim.scale(UnderstudyOrdealGeometry.LUNGE_SPEED));
		OrdealKit.face(puppet, state.aim);
		if ((age - LUNGE_WINDUP) % 2 == 0)
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), puppet.getX(), puppet.getY() + 1.0D, puppet.getZ(),
					3, .2D, .4D, .2D, 0.0D);
		if (!state.landed && puppet.getBoundingBox().inflate(.6D).intersects(caster.getBoundingBox())) {
			state.landed = true;
			landed(chamber, caster.position(), .9D, .8F);
			OrdealKit.strike(chamber, caster, puppet, LUNGE_DAMAGE, state.aim, 1.4D, .5D);
		}
	}

	/** A blow landing: a crack of bisque on the boards and a spray of blood and bone. */
	private static void landed(ServerLevel chamber, Vec3 at, double size, float pitch) {
		chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.4F, pitch);
		chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 1.6F, pitch + .2F);
		CircusEffects.impact(chamber, at.add(0, .4D, 0), size);
		CircusEffects.dust(chamber, at.add(0, .2D, 0), (int) (14 * size));
	}

	/** Whether the puppet is in one of its moves, when its ordinary blow and its replay wait. */
	public static boolean busy(UUID puppet) {
		State state = STATES.get(puppet);
		return state != null && state.move >= 0;
	}

	static void forget(UUID puppet) {
		STATES.remove(puppet);
	}

	public static void clearSessionState() {
		STATES.clear();
	}
}
