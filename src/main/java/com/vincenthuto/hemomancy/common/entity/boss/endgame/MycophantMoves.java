package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.encounter.MycophantEncounterManager;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** What each of the Spawn Mother's moves does on the server, stage by stage, and the focal beats around them. */
final class MycophantMoves {
	static final ParticleColor AMBER = new ParticleColor(255, 168, 40);
	static final ParticleColor YOLK = new ParticleColor(255, 206, 80);
	static final ParticleColor SPORE = new ParticleColor(200, 176, 64);
	static final ParticleColor CRIMSON = new ParticleColor(168, 22, 30);
	static final ParticleColor BONE = new ParticleColor(232, 216, 176);
	private static final int TENDRIL_CORE = 0xF8FFB040;
	private static final int TENDRIL_GLOW = 0xB8A8161E;

	private MycophantMoves() {
	}

	static void tick(MycophantEntity boss, MycophantController controller, MycophantAttack attack, int tick,
			BossMoveTimeline.Stage stage, boolean commit, @Nullable Vec3 lockedAim, @Nullable LivingEntity quarry) {
		if (!(boss.level() instanceof ServerLevel level)) return;
		switch (attack) {
			case RUSHING_EMBRACE, FUSION_EMBRACE -> embrace(level, boss, controller, attack, tick, stage, commit, quarry);
			case SIPHON_FEED -> siphon(level, boss, controller, tick, stage, commit, quarry);
			case PLUCK -> pluck(level, boss, stage, commit, quarry);
			case BROOD_CALL -> brood(level, boss, stage, tick, commit, quarry);
			case NECTAR_SURGE -> surge(level, boss, stage, tick, commit);
			case WING_GUARD -> guard(level, boss, controller, stage, tick, commit);
			case WING_SWEEP -> sweep(level, boss, stage, tick, commit, quarry);
			case PULP_BLOOM -> bloom(level, boss, stage, tick, commit, quarry);
			case RAKE_STRING, BROOD_SETTLE, CLAW_PIN, TAIL_LASH, HAND_SWEEP -> MycophantClawMoves.tick(level, boss, controller, attack, tick, stage, commit, quarry);
			case VENT_BLOOM, SIPHON_SPIT, SPORE_MORTAR, NURSING -> MycophantRangedMoves.tick(level, boss, controller, attack, tick, stage, commit, quarry);
			case HYPHAE_LANCES, LATTICE_NET, GAZE -> MycophantWingMoves.tick(level, boss, controller, attack, tick, stage, commit, quarry);
			case LAST_BROOD -> MycophantLastBrood.tick(level, boss, controller, attack, tick, stage, commit, quarry);
			case NONE -> { }
		}
	}

	// ------------------------------------------------------------------ the embrace (claim and fusion)

	private static void embrace(ServerLevel level, MycophantEntity boss, MycophantController controller, MycophantAttack attack,
			int tick, BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		boolean fusion = attack == MycophantAttack.FUSION_EMBRACE;
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			boss.setGlow(0.7F + 0.5F * tick / 20.0F);
			if (quarry != null && tick % 4 == 0) {
				double reach = Math.min(14.0D, Math.max(4.0D, boss.position().distanceTo(quarry.position())));
				BossTelegraphs.cone(level, boss.position(), boss.forward(), reach, fusion ? 70.0D : 50.0D, AMBER);
			}
			if (tick == 2) level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_SUMMON.get(),
					SoundSource.HOSTILE, 1.6F, fusion ? 0.7F : 0.55F);
			return;
		}
		if (stage != BossMoveTimeline.Stage.ACTIVE) return;
		if (commit) level.playSound(null, boss.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.8F, 0.5F);
		if (!MycophantControllerRules.mayLunge(boss.isHolding(), controller.caughtThisMove())) return;
		controller.lunge();
		if (tick % 2 == 0) BossTelegraphs.bloodCells(level, boss.bodyPoint(0.0D, 0.2D, 1.5D), CRIMSON, 4, 1.2D, 0.05D);
		if (quarry == null || !MycophantControllerRules.mayCatch(boss.isHolding(), controller.caughtThisMove())) return;
		double catchRadius = fusion ? 3.4D : 3.0D;
		if (quarry.position().distanceTo(boss.holdPoint()) > catchRadius) return;
		if (quarry instanceof ServerPlayer player && !player.isSpectator() && !player.isCreative()) {
			controller.markCaught();
			boss.beginHold(player);
		} else if (!(quarry instanceof ServerPlayer)) {
			controller.markCaught();
			strike(boss, quarry, 10.0F);
			controller.skipToRecovery();
		}
	}

	static void holdBegins(MycophantEntity boss, ServerPlayer held) {
		if (!(boss.level() instanceof ServerLevel level)) return;
		Vec3 sac = boss.bodyPoint(0.0D, 1.6D, 2.1D);
		for (int i = 0; i < 3; i++) BossTelegraphs.tendril(level, sac, held.position().add(0.0D, 1.0D, 0.0D),
				TENDRIL_CORE, TENDRIL_GLOW, boss.getId() * 31L + i);
		BossTelegraphs.glow(level, sac, AMBER, 18, 0.6D);
		held.displayClientMessage(Component.translatable(boss.getPhase() >= 2
				? "message.hemomancy.mycophant.fusion" : "message.hemomancy.mycophant.claimed")
				.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC), true);
	}

	static void holdTick(MycophantEntity boss, ServerPlayer held, float meter) {
		if (!(boss.level() instanceof ServerLevel level)) return;
		Vec3 sac = boss.bodyPoint(0.0D, 1.6D, 2.1D);
		if (boss.tickCount % 10 == 0) BossTelegraphs.tendril(level, sac.add(0.0D, (meter - 0.5D), 0.0D),
				held.position().add(0.0D, 0.6D + meter, 0.0D), TENDRIL_CORE, TENDRIL_GLOW, boss.getId() * 17L + boss.tickCount);
		if (boss.tickCount % 4 == 0) BossTelegraphs.glow(level, held.position().add(0.0D, 1.0D, 0.0D), AMBER,
				2 + Math.round(meter * 6.0F), 0.4D);
	}

	static void holdBroken(MycophantEntity boss, ServerPlayer held, boolean empowered) {
		if (!(boss.level() instanceof ServerLevel level)) return;
		Vec3 point = boss.holdPoint();
		BossTelegraphs.bloodCells(level, point, CRIMSON, empowered ? 40 : 22, 0.9D, 0.18D);
		BossTelegraphs.spores(level, point, SPORE, 16, 0.8D, 0.05D);
		BossTelegraphs.ring(level, boss.position(), 3.0D, AMBER, 18);
		held.displayClientMessage(Component.translatable("message.hemomancy.mycophant.broken")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
	}

	// ------------------------------------------------------------------ the siphon

	private static void siphon(ServerLevel level, MycophantEntity boss, MycophantController controller, int tick,
			BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		double[] at = MycophantVisualRules.siphonMouth();
		Vec3 mouth = boss.bodyPoint(at[0], at[1], at[2]);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			if (tick % 3 == 0 && quarry != null) BossTelegraphs.line(level, mouth, quarry.position(), AMBER);
			BossTelegraphs.glow(level, mouth, YOLK, 2, 0.15D);
			return;
		}
		if (stage != BossMoveTimeline.Stage.ACTIVE) return;
		if (commit) {
			if (quarry instanceof ServerPlayer player && player.distanceTo(boss) <= MycophantClaimRules.siphonRange()
					&& boss.hasLineOfSight(player) && !player.isSpectator()) {
				boss.latchSiphon(player);
			} else {
				controller.skipToRecovery();
			}
			return;
		}
		if (boss.getSiphonTargetId() < 0) controller.skipToRecovery();
	}

	static void siphonTick(MycophantEntity boss, ServerPlayer target) {
		if (!(boss.level() instanceof ServerLevel level) || boss.tickCount % 5 != 0) return;
		BossTelegraphs.glow(level, target.position().add(0.0D, 1.1D, 0.0D), CRIMSON, 3, 0.25D);
	}

	// ------------------------------------------------------------------ hand, brood, vents, wings, pulp (the claws are MycophantClawMoves)

	private static void pluck(ServerLevel level, MycophantEntity boss, BossMoveTimeline.Stage stage, boolean commit,
			@Nullable LivingEntity quarry) {
		Vec3 hand = boss.bodyPoint(-1.8D, 2.4D, 2.2D);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			BossTelegraphs.glow(level, hand, AMBER, 1, 0.2D);
			return;
		}
		if (!commit || quarry == null) return;
		if (quarry.position().distanceTo(hand) > 5.5D || boss.angleTo(quarry.position()) > 70.0D) return;
		if (!strike(boss, quarry, 4.0F)) return;
		Vec3 toward = boss.holdPoint().subtract(quarry.position()).multiply(1.0D, 0.0D, 1.0D).scale(0.22D);
		quarry.setDeltaMovement(toward.x, 0.5D, toward.z);
		quarry.hurtMarked = true;
		BossTelegraphs.bloodCells(level, quarry.position().add(0.0D, 1.0D, 0.0D), CRIMSON, 10, 0.4D, 0.1D);
	}

	private static void brood(ServerLevel level, MycophantEntity boss, BossMoveTimeline.Stage stage, int tick,
			boolean commit, @Nullable LivingEntity quarry) {
		Vec3 left = boss.bodyPoint(-2.3D, 0.8D, -3.0D);
		Vec3 right = boss.bodyPoint(2.3D, 0.8D, -3.0D);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			boss.setGlow(0.7F + tick / 30.0F);
			if (tick % 3 == 0) {
				BossTelegraphs.spores(level, left, SPORE, 4, 0.3D, 0.03D);
				BossTelegraphs.spores(level, right, SPORE, 4, 0.3D, 0.03D);
				BossTelegraphs.glow(level, left, AMBER, 2, 0.25D);
				BossTelegraphs.glow(level, right, AMBER, 2, 0.25D);
			}
			return;
		}
		if (!commit) return;
		int wanted = boss.getPhase() >= 2 ? 3 : 2;
		int room = Math.max(0, 4 - boss.livingBrood());
		for (int i = 0; i < Math.min(wanted, room); i++) {
			FunglingEntity fungling = EntityInit.fungling.get().create(level);
			if (fungling == null) continue;
			Vec3 at = i % 2 == 0 ? left : right;
			fungling.moveTo(at.x, boss.getY() + 0.2D, at.z, boss.getYRot(), 0.0F);
			fungling.setBrood(boss.getUUID());
			fungling.finalizeSpawn(level, level.getCurrentDifficultyAt(fungling.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
			if (quarry != null) fungling.setTarget(quarry);
			level.addFreshEntity(fungling);
			dissolve(level, at);
		}
		level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_AMBIENT.get(), SoundSource.HOSTILE, 1.4F, 1.3F);
	}

	private static void surge(ServerLevel level, MycophantEntity boss, BossMoveTimeline.Stage stage, int tick, boolean commit) {
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			if (tick % 5 == 0) BossTelegraphs.ring(level, boss.position(), 2.0D + tick * 0.2D, AMBER, 24);
			return;
		}
		if (!commit) return;
		boss.setPressure(MycophantCombatRules.pressureAfterSurge(boss.getNectarPressure()));
		BossTelegraphs.spores(level, boss.bodyPoint(0.0D, 4.0D, -1.0D), SPORE, 40, 2.4D, 0.08D);
		BossTelegraphs.ring(level, boss.position(), 8.0D, YOLK, 40);
		if (boss.getEncounterOwner() == null) {
			for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(6.0D),
					e -> e != boss && !(e instanceof FunglingEntity))) near.hurt(boss.damageSources().mobAttack(boss), 6.0F);
		}
	}

	private static void guard(ServerLevel level, MycophantEntity boss, MycophantController controller,
			BossMoveTimeline.Stage stage, int tick, boolean commit) {
		if (commit) {
			controller.setGuarding(true);
			level.playSound(null, boss.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.2F, 1.4F);
		}
		if (stage == BossMoveTimeline.Stage.RECOVERY) controller.setGuarding(false);
		if (stage == BossMoveTimeline.Stage.ACTIVE && tick % 6 == 0)
			BossTelegraphs.glow(level, boss.bodyPoint(0.0D, 2.4D, 2.4D), CRIMSON, 4, 1.2D);
	}

	private static void sweep(ServerLevel level, MycophantEntity boss, BossMoveTimeline.Stage stage, int tick,
			boolean commit, @Nullable LivingEntity quarry) {
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			if (tick % 4 == 0) BossTelegraphs.cone(level, boss.position(), boss.forward(), 7.0D, 140.0D, CRIMSON);
			return;
		}
		if (!commit) return;
		BossTelegraphs.bloodCells(level, boss.bodyPoint(0.0D, 3.0D, 2.0D), CRIMSON, 24, 3.0D, 0.15D);
		level.playSound(null, boss.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.HOSTILE, 2.0F, 0.5F);
		if (quarry != null && quarry.distanceTo(boss) <= 7.5D && boss.angleTo(quarry.position()) <= 70.0D) {
			if (!strike(boss, quarry, 12.0F)) return;
			Vec3 push = quarry.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(1.4D);
			quarry.push(push.x, 0.45D, push.z);
			quarry.hurtMarked = true;
		}
	}

	private static void bloom(ServerLevel level, MycophantEntity boss, BossMoveTimeline.Stage stage, int tick,
			boolean commit, @Nullable LivingEntity quarry) {
		Vec3 pulp = boss.bodyPoint(0.0D, 5.2D, 2.6D);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			boss.setGlow(0.8F + tick / 26.0F);
			if (tick % 2 == 0) BossTelegraphs.glow(level, pulp, YOLK, 1 + tick / 6, 0.4D);
			return;
		}
		if (!commit) return;
		BossTelegraphs.spores(level, pulp, SPORE, 60, 2.6D, 0.12D);
		BossTelegraphs.ring(level, boss.position(), 7.0D, SPORE, 36);
		if (quarry instanceof ServerPlayer player && player.distanceTo(boss) <= 7.0D && boss.hasLineOfSight(player)) {
			boss.addPressure(6.0F);
			MycophantEncounterManager.sendBloomVeil(player);
		}
	}

	/** Lands a blow; true only if the quarry is still here to be pushed (a lethal blow expels an owner at once). */
	static boolean strike(MycophantEntity boss, LivingEntity quarry, float damage) {
		quarry.hurt(boss.damageSources().mobAttack(boss), damage);
		return quarry.isAlive() && quarry.level() == boss.level();
	}

	static void dissolve(ServerLevel level, Vec3 at) {
		BossTelegraphs.spores(level, at.add(0.0D, 0.3D, 0.0D), SPORE, 12, 0.35D, 0.04D);
		BossTelegraphs.glow(level, at.add(0.0D, 0.2D, 0.0D), AMBER, 6, 0.3D);
	}

	// ------------------------------------------------------------------ the waking, the split and the yield

	static void introBeat(MycophantEntity boss, int t, boolean rematch, @Nullable ServerPlayer owner) {
		if (!(boss.level() instanceof ServerLevel level)) return;
		Vec3 cradle = boss.bodyPoint(-0.6D, 2.6D, 2.6D);
		if (t % 8 == 0 && t < MycophantIntroRules.tickOf(MycophantIntroRules.Beat.TURN, rematch))
			BossTelegraphs.glow(level, cradle, AMBER, 2, 0.3D);
		// the fibers snap on the player's own screen, one strand at a time (MycophantIntroClientEvents)
		if (t == MycophantIntroRules.tickOf(MycophantIntroRules.Beat.NOTICE, rematch))
			level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_AMBIENT.get(), SoundSource.HOSTILE, 1.4F, 0.5F);
		if (!rematch && t == MycophantIntroRules.tickOf(MycophantIntroRules.Beat.DROP, false) + MycophantIntroRules.dropFallTicks()) {
			Vec3 pool = boss.bodyPoint(-0.6D, 0.2D, 3.0D);
			dissolve(level, pool);
			BossTelegraphs.ring(level, pool, 1.2D, AMBER, 14);
		}
		if (t == MycophantIntroRules.tickOf(MycophantIntroRules.Beat.ROAR, rematch)) {
			level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_SUMMON.get(), SoundSource.HOSTILE, 2.4F, 0.45F);
			BossTelegraphs.ring(level, boss.position(), 5.0D, AMBER, 36);
			BossTelegraphs.spores(level, boss.bodyPoint(0.0D, 5.0D, 1.5D), SPORE, 50, 2.0D, 0.1D);
			boss.setGlow(1.4F);
		}
	}

	static void splitTick(MycophantEntity boss, int t) {
		if (!(boss.level() instanceof ServerLevel level)) return;
		Vec3 head = boss.bodyPoint(0.0D, 5.4D, 2.4D);
		if (t == 1) level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_TELEPORT.get(), SoundSource.HOSTILE, 1.6F, 0.55F);
		int snap = MycophantSplitRules.snapTick();
		if (t <= snap && t % 2 == 0) BossTelegraphs.glow(level, head, BONE, 3, 0.25D);
		if (t == snap) {
			level.playSound(null, boss.blockPosition(), SoundEvents.SKELETON_HURT, SoundSource.HOSTILE, 2.0F, 0.4F);
			BossTelegraphs.bloodCells(level, head, CRIMSON, 40, 0.6D, 0.2D);
		}
		if (t > snap && t < 44 && t % 3 == 0) BossTelegraphs.glow(level, head, YOLK, 4, 0.5D);
		float wings = MycophantSplitRules.wings(t);
		if (wings > 0.0F && wings < 1.0F && t % 4 == 0) {
			// spores shed along the growing wings, from the shoulder buds out toward the opening tips
			double reach = 1.2D + wings * 2.0D;
			BossTelegraphs.spores(level, boss.bodyPoint(-reach, 3.9D + wings * 0.8D, -0.2D), SPORE, 4, 0.6D, 0.04D);
			BossTelegraphs.spores(level, boss.bodyPoint(reach, 3.9D + wings * 0.8D, -0.2D), SPORE, 4, 0.6D, 0.04D);
		}
		if (t == MycophantSplitRules.pushTick()) {
			BossTelegraphs.ring(level, boss.position(), 6.0D, YOLK, 40);
			BossTelegraphs.spores(level, head, SPORE, 50, 2.0D, 0.15D);
			boss.setPressure(Math.max(50.0F, boss.getNectarPressure()));
			ServerPlayer owner = boss.owner();
			if (owner != null) {
				Vec3 away = owner.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
				away = away.lengthSqr() < 1.0E-4D ? boss.forward() : away.normalize();
				owner.setDeltaMovement(away.x * 1.6D, 0.5D, away.z * 1.6D);
				owner.hurtMarked = true;
				owner.displayClientMessage(Component.translatable("message.hemomancy.mycophant.tender_phase")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC), true);
			}
		}
	}

	static void yieldTick(MycophantEntity boss, int t) {
		if (!(boss.level() instanceof ServerLevel level)) return;
		float sink = MycophantYieldRules.sink(t);
		boss.setGlow(1.0F - sink * 0.9F);
		if (t % 5 == 0) BossTelegraphs.spores(level, boss.bodyPoint(0.0D, 2.0D - sink * 2.0D, 0.0D), SPORE, 6, 1.8D, 0.04D);
		if (t == 40 && boss.owner() != null) MycophantEncounterManager.proclaimHeir(boss.owner());
		if (t >= 40 && t <= 75 && t % 4 == 0) BossTelegraphs.glow(level, boss.bodyPoint(2.2D, 1.8D, 3.6D), YOLK, 3, 0.15D);
		if (t == 70) BossTelegraphs.ring(level, boss.position(), 4.0D, AMBER, 30);
		if (t > 70 && t % 10 == 0) BossTelegraphs.ring(level, boss.position(), 3.0D + (t - 70) * 0.08D, SPORE, 24);
	}
}
