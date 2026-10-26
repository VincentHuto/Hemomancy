package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;

/** The joining's wings and pulp: the Hyphae Lances, the Lattice Net and the Gaze of the Many Eyes. */
final class MycophantWingMoves {
	static final double LANCE_LENGTH = 12.0D;
	static final double LANCE_SPACING = 1.0D;
	/** The line starts at the edge of her body, toward the quarry, so no lance erupts unseen under her. */
	static final double LANCE_START = 2.0D;
	/** Below this share of max health a second line follows the first, turned this far to one side. */
	static final float SECOND_LINE_BELOW = 0.35F;
	static final float SECOND_LINE_DEG = 30.0F;
	/** Ticks the second line lags behind the first, so it reads as following it. */
	static final int SECOND_LINE_LAG = 6;
	/** Ticks the net hangs over the player before it comes down. */
	static final int NET_LAND_DELAY = 16;
	/** Ticks after the landing in which the move looks for its catch (the net ticks apart from her). */
	static final int NET_CATCH_WINDOW = 3;
	static final int GAZE_EVERY = 5;
	static final float GAZE_DAMAGE = 3.0F;
	static final float GAZE_PRESSURE = 1.0F;
	private static final int THREAD_CORE = 0xF8FFB040;
	private static final int THREAD_GLOW = 0xB8A8161E;

	private MycophantWingMoves() {
	}

	/**
	 * One tick of the wing moves. {@code tick} counts from the start of the move (the wind-up), as the controller's attack clock
	 * does; the active stage starts at {@code MycophantControllerRules.timeline(attack, phase, variant).windup()}.
	 */
	static void tick(ServerLevel level, MycophantEntity boss, MycophantController controller, MycophantAttack attack, int tick,
			BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		int active = tick - MycophantControllerRules.timeline(attack, boss.getPhase(), controller.variant()).windup();
		switch (attack) {
			case HYPHAE_LANCES -> lances(level, boss, tick, stage, commit, quarry);
			case LATTICE_NET -> net(level, boss, controller, tick, active, stage, commit, quarry);
			case GAZE -> gaze(level, boss, controller, tick, active, stage, commit, quarry);
			default -> { }
		}
	}

	// ------------------------------------------------------------------ the Hyphae Lances

	private static void lances(ServerLevel level, MycophantEntity boss, int tick, BossMoveTimeline.Stage stage, boolean commit,
			@Nullable LivingEntity quarry) {
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			// the wings rise and the spar tips gather amber before they stab down
			if (tick % 3 == 0) {
				BossTelegraphs.glow(level, boss.bodyPoint(-3.2D, 4.6D, -0.4D), MycophantMoves.AMBER, 2, 0.2D);
				BossTelegraphs.glow(level, boss.bodyPoint(3.2D, 4.6D, -0.4D), MycophantMoves.AMBER, 2, 0.2D);
			}
			return;
		}
		if (!commit) return;
		Vec3 toward = quarry != null ? quarry.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D) : Vec3.ZERO;
		Vec3 direction = toward.lengthSqr() < 1.0E-4D ? boss.forward() : toward.normalize();
		Vec3 origin = boss.position().add(direction.scale(LANCE_START));
		line(level, boss, origin, direction, 0);
		if (secondLine(boss.getHealth(), boss.getMaxHealth())) {
			float side = level.random.nextBoolean() ? SECOND_LINE_DEG : -SECOND_LINE_DEG;
			line(level, boss, origin, direction.yRot(side * ((float) Math.PI / 180.0F)), SECOND_LINE_LAG);
		}
		for (double right : new double[] { -2.6D, 2.6D }) {
			Vec3 stab = boss.bodyPoint(right, 0.2D, 1.4D);
			BossTelegraphs.bloodCells(level, stab, MycophantMoves.AMBER, 8, 0.3D, 0.1D);
			BossTelegraphs.spores(level, stab, MycophantMoves.SPORE, 6, 0.3D, 0.04D);
		}
		level.playSound(null, boss.blockPosition(), SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.HOSTILE, 1.8F, 0.5F);
		level.playSound(null, boss.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.6F, 0.6F);
	}

	private static void line(ServerLevel level, MycophantEntity boss, Vec3 origin, Vec3 direction, int lag) {
		double[][] points = MycophantStrikeRules.lanceLine(origin.x, origin.z, origin.x + direction.x, origin.z + direction.z,
				LANCE_LENGTH, LANCE_SPACING);
		for (int i = 0; i < points.length; i++)
			MycophantLanceEntity.erupt(level, new Vec3(points[i][0], boss.getY(), points[i][1]), boss, MycophantStrikeRules.lanceDelay(i) + lag);
	}

	/** Late in the joining the lances come in two lines. */
	static boolean secondLine(float health, float maxHealth) {
		return health < maxHealth * SECOND_LINE_BELOW;
	}

	// ------------------------------------------------------------------ the Lattice Net

	private static void net(ServerLevel level, MycophantEntity boss, MycophantController controller, int tick, int active,
			BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		Vec3 left = boss.bodyPoint(-2.8D, 4.2D, 0.6D);
		Vec3 right = boss.bodyPoint(2.8D, 4.2D, 0.6D);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			// the wings spread forward and the lattice between the spars lights
			if (tick % 3 == 0) {
				BossTelegraphs.glow(level, left, MycophantMoves.YOLK, 2, 0.5D);
				BossTelegraphs.glow(level, right, MycophantMoves.YOLK, 2, 0.5D);
			}
			if (tick == 4) level.playSound(null, boss.blockPosition(), SoundEvents.COBWEB_PLACE, SoundSource.HOSTILE, 1.4F, 0.6F);
			return;
		}
		if (commit) {
			if (quarry == null) {
				controller.skipToRecovery();
				return;
			}
			Vec3 at = floorUnder(level, quarry.position());
			MycophantHazardEntity.net(level, at, boss, NET_LAND_DELAY);
			// the net goes out on threads from both wings
			long seed = boss.getId() * 41L + level.getGameTime();
			BossTelegraphs.tendril(level, left, at.add(0.0D, 2.5D, 0.0D), THREAD_CORE, THREAD_GLOW, seed);
			BossTelegraphs.tendril(level, right, at.add(0.0D, 2.5D, 0.0D), THREAD_CORE, THREAD_GLOW, seed + 1);
			level.playSound(null, boss.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.HOSTILE, 1.6F, 0.6F);
			return;
		}
		if (quarry == null || !netCatchTick(active)) return;
		for (MycophantHazardEntity net : MycophantHazardEntity.of(level, boss, MycophantHazardEntity.Kind.LATTICE_NET)) {
			if (net.landed() && net.caught(quarry)) {
				controller.markNetted();
				return;
			}
		}
	}

	/** The ticks after the net comes down in which its catch is read (the net may tick after her on its landing tick). */
	static boolean netCatchTick(int active) {
		return active >= NET_LAND_DELAY && active <= NET_LAND_DELAY + NET_CATCH_WINDOW;
	}

	// ------------------------------------------------------------------ the Gaze of the Many Eyes

	private static void gaze(ServerLevel level, MycophantEntity boss, MycophantController controller, int tick, int active,
			BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		double[] at = MycophantVisualRules.gazeEye();
		Vec3 eye = boss.bodyPoint(at[0], at[1], at[2]);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			// the eyes open one by one, each with a higher note, and lock on
			if (tick % 6 == 0) {
				BossTelegraphs.glow(level, eye, MycophantMoves.YOLK, 2 + tick / 6, 0.35D);
				level.playSound(null, boss.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.6F,
						0.5F + tick * 0.03F);
			}
			if (quarry != null) {
				boss.setGazeYaw(bearingFrom(eye, quarry.position()));
				if (tick >= 12 && tick % 6 == 0) {
					double reach = Math.min(MycophantStrikeRules.GAZE_RANGE, eye.distanceTo(quarry.position()) + 3.0D);
					BossTelegraphs.cone(level, boss.position(), quarry.position().subtract(boss.position()), reach,
							MycophantStrikeRules.GAZE_SWEEP_DEG, MycophantMoves.YOLK);
				}
			}
			return;
		}
		if (stage != BossMoveTimeline.Stage.ACTIVE) return;
		int direction = gazeDirection(controller.variant(), coin(level.getGameTime() - tick, boss.getId()));
		float start;
		if (commit || active <= 0) {
			start = quarry != null ? bearingFrom(eye, quarry.position()) : boss.yBodyRot;
			level.playSound(null, boss.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 2.0F, 0.5F);
			BossTelegraphs.glow(level, eye, MycophantMoves.AMBER, 12, 0.4D);
		} else {
			// the yaw synced last tick holds the sweep's start, so nothing else needs remembering
			start = gazeStart(boss.getGazeYaw(), direction, active - 1);
		}
		float yaw = MycophantStrikeRules.gazeYaw(start, direction, active);
		boss.setGazeYaw(yaw);
		if (active % 10 == 5) level.playSound(null, boss.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 1.6F, 0.6F);
		if (!gazeStrikes(active)) return;
		double range = MycophantStrikeRules.GAZE_RANGE;
		for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(range, 8.0D, range),
				entity -> MycophantHazardEntity.takes(boss, entity))) {
			if (!MycophantStrikeRules.gazeHits(yaw, victim.getX() - eye.x, victim.getZ() - eye.z, range)) continue;
			Vec3 body = victim.position().add(0.0D, MycophantStrikeRules.gazeTargetHeight(victim.getBbHeight()), 0.0D);
			if (!clearLine(level, eye, body)) continue;
			// the beam burns every five ticks; the hurt cooldown would swallow every other burn
			victim.invulnerableTime = 0;
			if (!victim.hurt(boss.damageSources().mobAttack(boss), GAZE_DAMAGE)) continue;
			controller.markHit();
			if (boss.getEncounterOwner() != null && boss.getEncounterOwner().equals(victim.getUUID())) boss.addPressure(GAZE_PRESSURE);
			BossTelegraphs.bloodCells(level, body, MycophantMoves.AMBER, 6, 0.25D, 0.08D);
			BossTelegraphs.glow(level, body, MycophantMoves.YOLK, 3, 0.25D);
		}
	}

	/** Variant 1 sweeps one way, variant 2 the other; variant 0 tosses for it. +1 or -1, as {@link MycophantStrikeRules#gazeYaw} takes. */
	static int gazeDirection(int variant, boolean coin) {
		if (variant == 1) return 1;
		if (variant >= 2) return -1;
		return coin ? 1 : -1;
	}

	/** A stable toss for one move: the same move start always gives the same side. */
	static boolean coin(long moveStart, int id) {
		long mixed = (moveStart * 0x9E3779B97F4A7C15L) ^ (id * 0xC2B2AE3D27D4EB4FL);
		mixed ^= mixed >>> 31;
		return (mixed & 1L) == 0L;
	}

	/** The sweep's start yaw recovered from the yaw it gave {@code previousActive} ticks into the sweep. */
	static float gazeStart(float previousYaw, int direction, int previousActive) {
		return previousYaw - MycophantStrikeRules.gazeYaw(0.0F, direction, previousActive);
	}

	/** The beam burns on the first sweep tick and every {@link #GAZE_EVERY} after it, for the whole sweep. */
	static boolean gazeStrikes(int active) {
		return active >= 0 && active < MycophantStrikeRules.GAZE_SWEEP_TICKS && active % GAZE_EVERY == 0;
	}

	private static float bearingFrom(Vec3 from, Vec3 to) {
		return MycophantStrikeRules.bearing(to.x - from.x, to.z - from.z);
	}

	/** Nothing solid between the pulp and the point: a brood tumor in the way shelters whoever stands behind it. */
	private static boolean clearLine(ServerLevel level, Vec3 from, Vec3 to) {
		return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()))
				.getType() == HitResult.Type.MISS;
	}

	/** The floor under {@code at} (within a few blocks), or the point itself over a drop. */
	static Vec3 floorUnder(ServerLevel level, Vec3 at) {
		HitResult hit = level.clip(new ClipContext(at.add(0.0D, 1.0D, 0.0D), at.subtract(0.0D, 4.0D, 0.0D), ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, CollisionContext.empty()));
		return hit.getType() == HitResult.Type.MISS ? at : hit.getLocation();
	}
}
