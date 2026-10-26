package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.encounter.MycophantEncounterManager;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantLastBroodRules.Outcome;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/** The desperation set piece below a quarter health in phase 2: the tell, the arena-wide pull, the binding tendrils, and its endings. */
final class MycophantLastBrood {
	/** The same source the filled sac meter pays through (a record, so equal to MycophantEntity's own). */
	/** Share of the velocity a pulled body keeps each tick, so the pull settles at a steady speed instead of growing. */
	static final double PULL_DAMPING = 2.0D / 3.0D;
	/** How close to the hold point (horizontally) a pulled player is taken. */
	static final double REACH = 1.2D;
	/** How far round her the pull reaches: the whole Nursery. */
	static final double PULL_RADIUS = 32.0D;
	/** Ticks of the tell she spends dragging back toward the pool's middle. */
	private static final int RETREAT_TICKS = 20;
	private static final double RETREAT_SPEED = 0.12D;
	private static final int ROAR_TICK = 6;
	private static final int TENDRIL_CORE = 0xF8FFB040;
	private static final int TENDRIL_GLOW = 0xB8A8161E;

	private MycophantLastBrood() {
	}

	/**
	 * One tick of the Last Brood. {@code tick} counts from the start of the move (the wind-up), as the controller's attack clock
	 * does; the active stage starts at {@code MycophantControllerRules.timeline(attack, phase, variant).windup()}.
	 */
	static void tick(ServerLevel level, MycophantEntity boss, MycophantController controller, MycophantAttack attack, int tick,
			BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		int active = tick - MycophantControllerRules.timeline(attack, boss.getPhase(), controller.variant()).windup();
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			tell(level, boss, controller, tick, quarry);
			return;
		}
		Vec3 sac = boss.bodyPoint(0.0D, 1.6D, 2.1D);
		if (commit) bind(level, boss);
		if (boss.getLastBroodOutcome() == Outcome.COCOONED.ordinal()) {
			// the cocoon holds the clock still; once it lets go she is done
			if (!boss.isHolding()) controller.skipToRecovery();
			else if (boss.tickCount % 4 == 0) BossTelegraphs.glow(level, sac, MycophantMoves.AMBER, 4, 0.6D);
			return;
		}
		if (quarry != null) controller.face(quarry.position(), 2.0F);
		Vec3 hold = MycophantEntity.holdPoint(boss.position(), boss.yBodyRot, MycophantHoldKind.SAC);
		LivingEntity reached = null;
		for (LivingEntity pulled : pulled(level, boss)) {
			Vec3 to = hold.subtract(pulled.position());
			if (reached == null && reached(to.x, to.y, to.z)) {
				reached = pulled;
				continue;
			}
			double[] velocity = pullStep(pulled.getDeltaMovement().x, pulled.getDeltaMovement().z, to.x, to.z);
			// a player's server-side velocity is only ever what was last sent; never send back a stale lift
			pulled.setDeltaMovement(velocity[0], Math.min(0.0D, pulled.getDeltaMovement().y), velocity[1]);
			pulled.hurtMarked = true;
			if ((boss.tickCount + pulled.getId()) % 10 == 0)
				BossTelegraphs.tendril(level, sac, pulled.position().add(0.0D, 1.0D, 0.0D), TENDRIL_CORE, TENDRIL_GLOW, boss.getId() * 13L + boss.tickCount);
			if (boss.tickCount % 3 == 0) BossTelegraphs.spores(level, pulled.position().add(0.0D, 0.8D, 0.0D), MycophantMoves.SPORE, 2, 0.3D, 0.02D);
		}
		int left = MycophantHazardEntity.of(level, boss, MycophantHazardEntity.Kind.BINDING_TENDRIL).size();
		// judged after this tick's pull, so the pull runs exactly PULL ticks
		Outcome outcome = MycophantLastBroodRules.outcome(active + 1, left, reached != null);
		switch (outcome) {
			case PULLING -> {
				boss.setGlow(1.6F + 0.2F * (float) Math.sin(boss.tickCount * 0.4F));
				if (boss.tickCount % 4 == 0) BossTelegraphs.glow(level, sac, MycophantMoves.YOLK, 3, 0.7D);
			}
			case CUT_FREE -> cutFree(level, boss, controller, sac);
			case SPENT -> spent(level, boss, controller, sac);
			case COCOONED -> cocoon(level, boss, controller, reached, sac);
		}
	}

	// ------------------------------------------------------------------ the tell

	private static void tell(ServerLevel level, MycophantEntity boss, MycophantController controller, int tick, @Nullable LivingEntity quarry) {
		if (tick == 0) {
			boss.finishLastBrood();
			boss.setLastBroodOutcome(-1);
		}
		// she drags back toward the middle of her pool (or away from what she fights, outside the Nursery)
		if (tick < RETREAT_TICKS) {
			Vec3 middle = MycophantEncounterManager.arenaInteriorAt(level, boss.position()).map(AABB::getCenter)
					.orElseGet(() -> quarry != null ? boss.position().add(boss.position().subtract(quarry.position())) : boss.position());
			Vec3 to = middle.subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			if (to.lengthSqr() > 1.0D) controller.drag(to, RETREAT_SPEED);
		}
		boss.setGlow(0.8F + tick / (float) MycophantLastBroodRules.TELL);
		Vec3 sac = boss.bodyPoint(0.0D, 1.6D, 2.1D);
		if (tick % 3 == 0) BossTelegraphs.glow(level, sac, MycophantMoves.AMBER, 2 + tick / 5, 0.6D);
		if (tick == ROAR_TICK) {
			level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_SUMMON.get(), SoundSource.HOSTILE, 2.6F, 0.4F);
			BossTelegraphs.ring(level, boss.position(), 6.0D, MycophantMoves.AMBER, 40);
			BossTelegraphs.spores(level, boss.bodyPoint(0.0D, 4.6D, 1.0D), MycophantMoves.SPORE, 40, 2.0D, 0.1D);
		}
		// where the binding tendrils will root, read from across the Nursery before they burst up
		if (tick >= 18 && tick % 4 == 2) {
			double r = MycophantLastBroodRules.ANCHOR_MIN + (MycophantLastBroodRules.ANCHOR_MAX - MycophantLastBroodRules.ANCHOR_MIN) * 0.5D;
			BossTelegraphs.ring(level, boss.position(), r, MycophantMoves.CRIMSON, 48);
		}
	}

	/** The sac opens and four binding tendrils burst out to their anchors. */
	private static void bind(ServerLevel level, MycophantEntity boss) {
		double[][] anchors = MycophantLastBroodRules.anchors(boss.getX(), boss.getZ(), boss.yBodyRot, boss.getId() * 31L + level.getGameTime());
		for (double[] anchor : anchors) {
			Vec3 at = MycophantWingMoves.floorUnder(level, new Vec3(anchor[0], boss.getY(), anchor[1]));
			MycophantHazardEntity.tendril(level, boss, at);
			BossTelegraphs.bloodCells(level, at.add(0.0D, 0.3D, 0.0D), MycophantMoves.CRIMSON, 12, 0.4D, 0.12D);
			BossTelegraphs.spores(level, at.add(0.0D, 0.3D, 0.0D), MycophantMoves.SPORE, 8, 0.4D, 0.05D);
		}
		BossTelegraphs.glow(level, boss.bodyPoint(0.0D, 1.6D, 2.1D), MycophantMoves.YOLK, 20, 0.8D);
		level.playSound(null, boss.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 2.0F, 0.4F);
		level.playSound(null, boss.blockPosition(), SoundEvents.SLIME_BLOCK_BREAK, SoundSource.HOSTILE, 1.6F, 0.5F);
	}

	/** Every body the sac pulls: survival players and whatever she fights, never her brood. */
	private static List<LivingEntity> pulled(ServerLevel level, MycophantEntity boss) {
		return level.getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(PULL_RADIUS, 8.0D, PULL_RADIUS),
				entity -> MycophantHazardEntity.takes(boss, entity));
	}

	// ------------------------------------------------------------------ the endings

	private static void cutFree(ServerLevel level, MycophantEntity boss, MycophantController controller, Vec3 sac) {
		boss.setLastBroodOutcome(Outcome.CUT_FREE.ordinal());
		BossTelegraphs.bloodCells(level, sac, MycophantMoves.CRIMSON, 70, 1.4D, 0.28D);
		BossTelegraphs.bloodCells(level, sac, MycophantMoves.AMBER, 30, 1.0D, 0.2D);
		BossTelegraphs.spores(level, sac, MycophantMoves.SPORE, 40, 1.6D, 0.1D);
		BossTelegraphs.ring(level, boss.position(), 5.0D, MycophantMoves.CRIMSON, 40);
		level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_HURT.get(), SoundSource.HOSTILE, 2.2F, 0.45F);
		level.playSound(null, boss.blockPosition(), SoundEvents.SLIME_BLOCK_BREAK, SoundSource.HOSTILE, 2.0F, 0.4F);
		boss.setGlow(0.4F);
		clear(boss);
		controller.forceStagger(MycophantLastBroodRules.COLLAPSE, null);
	}

	private static void spent(ServerLevel level, MycophantEntity boss, MycophantController controller, Vec3 sac) {
		boss.setLastBroodOutcome(Outcome.SPENT.ordinal());
		BossTelegraphs.spores(level, sac, MycophantMoves.BONE, 24, 1.2D, 0.04D);
		level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_HURT.get(), SoundSource.HOSTILE, 1.6F, 0.6F);
		boss.setGlow(0.6F);
		clear(boss);
		controller.forceStagger(MycophantLastBroodRules.SPENT, null);
	}

	private static void cocoon(ServerLevel level, MycophantEntity boss, MycophantController controller, LivingEntity taken, Vec3 sac) {
		boss.setLastBroodOutcome(Outcome.COCOONED.ordinal());
		clear(boss);
		for (int i = 0; i < 4; i++)
			BossTelegraphs.tendril(level, sac, taken.position().add(0.0D, 0.4D + i * 0.4D, 0.0D), TENDRIL_CORE, TENDRIL_GLOW, boss.getId() * 7L + i);
		BossTelegraphs.glow(level, sac, MycophantMoves.AMBER, 24, 0.7D);
		if (taken instanceof ServerPlayer player) {
			boss.beginHold(player, MycophantHoldKind.COCOON);
			return;
		}
		// whatever a summoned Mycophant fights is not a player she can seal: it takes the blow and she lets it go
		taken.hurt(boss.damageSources().mobAttack(boss), MycophantLastBroodRules.COCOON_DAMAGE);
		controller.skipToRecovery();
	}

	/** A cocoon ran out (the hold code calls this): claim damage, a blood drain and pressure, then she lets the player go. */
	static void onCocoonReleased(MycophantEntity boss, ServerPlayer held) {
		if (!(boss.level() instanceof ServerLevel level)) return;
		Vec3 at = held.position().add(0.0D, 1.0D, 0.0D);
		BossTelegraphs.bloodCells(level, at, MycophantMoves.CRIMSON, 30, 0.6D, 0.16D);
		BossTelegraphs.spores(level, at, MycophantMoves.SPORE, 20, 0.6D, 0.05D);
		level.playSound(null, boss.blockPosition(), SoundEvents.SLIME_BLOCK_BREAK, SoundSource.HOSTILE, 1.8F, 0.5F);
		float damage = MycophantClaimRules.clampNonlethal(held.getHealth(), MycophantLastBroodRules.COCOON_DAMAGE);
		if (damage > 0.0F) held.hurt(HemoDamageTypes.mycophantClaim(level, boss), damage);
		if (!held.isAlive() || held.level() != level) return;
		var volume = HemoCapabilityAccess.getBloodVolume(held);
		if (volume.isActive()) BloodAccounting.pay(held, volume.getMaxBloodVolume() * MycophantLastBroodRules.COCOON_DRAIN, MycophantEntity.CLAIM);
		boss.addPressure(MycophantLastBroodRules.COCOON_PRESSURE);
		Vec3 away = held.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		away = away.lengthSqr() < 1.0E-4D ? boss.forward() : away.normalize();
		held.setDeltaMovement(away.x * 1.1D, 0.4D, away.z * 1.1D);
		held.hurtMarked = true;
	}

	/** Removes whatever the set piece left in the world (its binding tendrils). */
	static void clear(MycophantEntity boss) {
		if (!(boss.level() instanceof ServerLevel level)) return;
		for (MycophantHazardEntity tendril : MycophantHazardEntity.of(level, boss, MycophantHazardEntity.Kind.BINDING_TENDRIL)) tendril.wither();
	}

	// ------------------------------------------------------------------ the pull (pure)

	/**
	 * A pulled body's next horizontal velocity: the pull added toward the hold point, then damped, so it settles at
	 * {@code PULL_PER_TICK * damping / (1 - damping)} (about sprint pace) and any older push dies away.
	 */
	static double[] pullStep(double vx, double vz, double dx, double dz) {
		double distance = Math.sqrt(dx * dx + dz * dz);
		double px = distance < 1.0E-6D ? 0.0D : dx / distance * MycophantLastBroodRules.PULL_PER_TICK;
		double pz = distance < 1.0E-6D ? 0.0D : dz / distance * MycophantLastBroodRules.PULL_PER_TICK;
		return new double[] { (vx + px) * PULL_DAMPING, (vz + pz) * PULL_DAMPING };
	}

	/** The pull has reeled a body in: within {@link #REACH} of the hold point across the floor, and level with the sac. */
	static boolean reached(double dx, double dy, double dz) {
		return dx * dx + dz * dz <= REACH * REACH && dy >= -1.5D && dy <= 2.5D;
	}
}
