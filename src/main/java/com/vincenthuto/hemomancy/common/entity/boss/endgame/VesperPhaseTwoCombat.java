package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.block.harbinger.CrimsonFireHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodBoltEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.LivingSickleHookEntity;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronPillar;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchBreathRules;
import com.vincenthuto.hemomancy.common.manipulation.EntityManipulationEffects;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCastContext;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.TemporaryIceManager;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Server-authoritative, telegraphed weapon combat for the Evening Star. */
public final class VesperPhaseTwoCombat {
	private static final double TARGET_RANGE = 48.0D;
	private static final String VESPER_IRON_OWNER = "HemomancyVesperIronOwner";

	private VesperPhaseTwoCombat() { }

	public static void cancel(VesperTheEveningStarEntity boss) {
		cancelInternal(boss, true);
	}

	public static void cancelForDefeat(VesperTheEveningStarEntity boss) {
		cancelInternal(boss, false);
	}

	public static void cancelForHoodRemoval(VesperTheEveningStarEntity boss) {
		cancelInternal(boss, true);
	}

	private static void cancelInternal(VesperTheEveningStarEntity boss, boolean clearWeaponAction) {
		if (clearWeaponAction) boss.clearWeaponAction();
		boss.releaseHook();
		boss.setActionCooldown(0);
		boss.removeEffect(EffectInit.iron_retort);
		boss.removeEffect(EffectInit.blood_rush);
		boss.removeEffect(MobEffects.SLOW_FALLING);
		boss.getNavigation().stop();
		cleanupArenaEffects(boss);
	}

	private static void cleanupArenaEffects(VesperTheEveningStarEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		AABB arena = boss.getBoundingBox().inflate(32.0D, 12.0D, 32.0D);
		TemporaryIceManager.clearEncounterOwned(server, arena, boss.getUUID(),
				boss.getOrdealOwner() != null && boss.getActiveTendency() == EnumBloodTendency.CONGEATIO);
		server.getEntitiesOfClass(EntityIronPillar.class, arena,
				pillar -> isOwnedIron(pillar.getCreator(), pillar.getPersistentData(), boss))
				.forEach(EntityIronPillar::discard);
		server.getEntitiesOfClass(EntityIronSpike.class, arena,
				spike -> isOwnedIron(spike.getCreator(), spike.getPersistentData(), boss))
				.forEach(EntityIronSpike::discard);
	}

	static void tickAction(VesperTheEveningStarEntity boss, LivingEntity target) {
        var hit = com.vincenthuto.hemomancy.common.damage.SchoolHitContext.load(
                boss.getPersistentData().getCompound("hemomancy:weapon_school_hit"));
        try (var schoolAction = com.vincenthuto.hemomancy.common.damage.SchoolDamage.scope(hit, boss)) {
            tickSchoolAction(boss, target);
        }
    }

    private static void tickSchoolAction(VesperTheEveningStarEntity boss, LivingEntity target) {
		VesperWeaponAction action = boss.getWeaponAction();
		int tick = boss.getActionTick() + 1;
		boss.setActionTick(tick);
		if (tick <= action.impactTick()) {
			faceLockedOrTarget(boss, target, tick >= Math.max(1, action.impactTick() - 8));
		}
		boss.getNavigation().stop();
		telegraph(boss, action, tick);
		emitWeaponEffect(boss, target, action, tick);
		switch (action) {
			case ICHIMONJI -> ichimonji(boss, target, tick);
			case CROSSCUT -> crosscut(boss, target, tick);
			case LEAPING_CLEAVE -> leapingCleave(boss, target, tick);
			case REAPER_SWEEP -> reaperSweep(boss, target, tick);
			case SKY_LANCE -> skyLance(boss, target, tick);
			case LANCE_FLURRY -> lanceFlurry(boss, target, tick);
			case TWIN_REND -> twinRend(boss, target, tick);
			case PREDATOR_POUNCE -> predatorPounce(boss, target, tick);
			case CONDUCTIVE_VOLLEY -> conductiveVolley(boss, target, tick);
			case STORM_LOCK -> stormLock(boss, target, tick);
			case BRANDING_THRUSTS -> brandingThrusts(boss, target, tick);
			case UPDRAFT_IMPALEMENT -> updraftImpalement(boss, target, tick);
			case FLAMMEUS_CONCENTRATION -> flammeusConcentration(boss, target, tick);
			case BLADE_RISING_CUT -> bladeRisingCut(boss, target, tick);
			case BLADE_DRAWING_STEP -> bladeDrawingStep(boss, target, tick);
			case AXE_HAFT_SHOVE -> axeHaftShove(boss, target, tick);
			case AXE_GRAVE_SPLITTER -> axeGraveSplitter(boss, target, tick);
			case SPEAR_VAULT_KICK -> spearVaultKick(boss, target, tick);
			case SPEAR_PINNING_THRUST -> spearPinningThrust(boss, target, tick);
			case CLAW_SHADOW_FEINT -> clawShadowFeint(boss, target, tick);
			case CLAW_EVISCERATE -> clawEviscerate(boss, target, tick);
			case CROSSBOW_SNAP_SHOT -> crossbowSnapShot(boss, target, tick);
			case CROSSBOW_BOLT_RAIN -> crossbowBoltRain(boss, target, tick);
			case TORCH_CINDER_SWEEP -> torchCinderSweep(boss, target, tick);
			case FLAIL_ORBIT -> flailOrbit(boss, target, tick);
			case FLAIL_SHATTER_SLAM -> flailShatterSlam(boss, target, tick);
			case STAFF_LODESTONE_SWEEP -> staffLodestoneSweep(boss, target, tick);
			case STAFF_PILLAR_VAULT -> staffPillarVault(boss, target, tick);
			case SICKLE_REAP_L -> sickleReap(boss, target, tick, true);
			case SICKLE_REAP_R -> sickleReap(boss, target, tick, false);
			case SICKLE_HOOK_REEL -> sickleHookReel(boss, target, tick);
			case SICKLE_REEL_CYCLONE -> sickleReelCyclone(boss, target, tick);
			case SICKLE_REAPERS_RISE -> sickleReapersRise(boss, target, tick);
			case SICKLE_PASS_THROUGH -> sicklePassThrough(boss, target, tick);
			case SICKLE_GALLOWS_WHEEL -> sickleGallowsWheel(boss, target, tick);
			case SICKLE_TRAVELLING_CYCLONE -> sickleTravellingCyclone(boss, target, tick);
			case SICKLE_REVERSE_CYCLONE -> sickleReverseCyclone(boss, target, tick);
			case SICKLE_FEINT_CYCLONE -> sickleFeintCyclone(boss, target, tick);
			case SICKLE_POUNCE_FEINT -> sicklePounceFeint(boss, target, tick);
			case SICKLE_CRESCENT_FAN -> sickleCrescentFan(boss, target, tick);
			case SICKLE_CRESCENT_STAGGER -> sickleCrescentStagger(boss, target, tick);
			case SICKLE_CRESCENT_SKIM -> sickleCrescentSkim(boss, target, tick);
			case SICKLE_CRESCENT_RETURN -> sickleCrescentReturn(boss, target, tick);
			case SICKLE_ANCHOR_HOOK -> sickleAnchorHook(boss, target, tick);
			case SICKLE_HARVEST -> sickleHarvest(boss, target, tick);
			case BORROWED_HULK_YANK, BORROWED_MARROW_VOLLEY, BORROWED_VULTURE_DIVE, BORROWED_MUMMER_DECOYS,
					BORROWED_HOUND_CURS, BORROWED_BELLOWS_BREATH, BORROWED_MNEMONIC_LASH ->
					VesperBorrowedAttacks.tick(boss, target, action, tick);
			case CHAIN_SWEEP -> chainSweep(boss, target, tick);
			case HOOK_AND_CRUSH -> hookAndCrush(boss, target, tick);
			case MAGNETIC_AXIS -> magneticAxis(boss, target, tick);
			case IRON_RETORT -> ironRetort(boss, target, tick);
			case SICKLE_CYCLONE -> sickleCyclone(boss, target, tick);
			case SICKLE_POUNCE -> sicklePounce(boss, target, tick);
			case SICKLE_CROSS_REND -> sickleCrossRend(boss, target, tick);
			case SICKLE_HOOK -> sickleHook(boss, target, tick);
			case SANGUINE_CRESCENTS -> sanguineCrescents(boss, target, tick);
			default -> { }
		}
		if (tick >= action.durationTicks()) finish(boss);
	}

	private static void ichimonji(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 1) boss.addEffect(new MobEffectInstance(EffectInit.blood_rush, 30, 1, false, true, true));
		if (tick == 18) hitLine(boss, target, 6.0D, 1.1D, 16.0F, 0);
	}

	private static void crosscut(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 12 || tick == 23) dashThrough(boss, tick == 12 ? 1.35D : 1.15D, tick == 12);
		if (tick == 15) hitArc(boss, target, 4.5D, 130.0D, 8.0F, 0);
		if (tick == 26 && hitArc(boss, target, 4.5D, 130.0D, 8.0F, 1)) {
			cast(boss, target, "blood_aneurysm", 0.75D);
		}
	}

	private static void leapingCleave(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 10) moveToward(boss, boss.getLockedActionAim(), 0.9D, 0.72D);
		if (tick == 22) hitArea(boss, target, boss.getLockedActionAim(), 5.0D, 15.0F, 0);
	}

	private static void reaperSweep(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 18 && hitArc(boss, target, 6.0D, 150.0D, 17.0F, 0)) {
			cast(boss, target, target.getHealth() <= target.getMaxHealth() * 0.35F ? "exsanguinate" : "grave_debt", 0.7D);
		}
	}

	private static void skyLance(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick < 12) boss.setDeltaMovement(0.0D, 0.13D, 0.0D);
		if (tick == 16) moveToward(boss, boss.getLockedActionAim(), 1.85D, 0.0D);
		if (tick >= 17 && tick <= 25) hitLine(boss, target, 3.2D, 1.2D, 14.0F, 0);
	}

	private static void lanceFlurry(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		int index = tick == 12 ? 0 : tick == 20 ? 1 : tick == 28 ? 2 : -1;
		if (index >= 0) {
			moveCommitted(boss, 0.62D, 0.05D);
			if (hitLine(boss, target, 4.8D, 1.15D, 6.0F, index) && index == 2) {
				cast(boss, target, (boss.getActionVariant() & 1) == 0
						? "prismatic_reproof" : "hematic_flare", 0.65D);
			}
		}
	}

	private static void twinRend(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 8 && !cast(boss, target, "umbral_step", 0.65D)) safeFlank(boss, target);
		if (tick == 12 || tick == 20) {
			int bit = tick == 12 ? 0 : 1;
			hitArc(boss, target, 4.2D, 135.0D, 7.0F, bit);
		}
	}

	private static void predatorPounce(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 12) moveToward(boss, boss.getLockedActionAim(), 1.25D, 0.48D);
		if (tick == 20 && hitArc(boss, target, 4.8D, 145.0D, 10.0F, 0)) {
			cast(boss, target, "gloam_laceration", 0.75D);
		}
	}

	private static void conductiveVolley(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 20) for (int i = -1; i <= 1; i++) fireBolt(boss, target, i * 4.5F);
	}

	private static void stormLock(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 1 && boss.distanceTo(target) <= 4.0F) {
			pushAway(boss, target, 0.9D);
			pulse(boss, EnumBloodTendency.DUCTILIS, 18);
		}
		if (tick == 18) cast(boss, target, "conductive_mark", 0.7D);
		if (tick == 28 || tick == 36 || tick == 44) {
			Vec3 center = stormPoint(boss.getLockedActionAim(), tick);
			lightningCircle(boss, target, center, (tick - 28) / 8);
		}
	}

	private static void brandingThrusts(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		int index = tick == 12 ? 0 : tick == 19 ? 1 : tick == 26 ? 2 : -1;
		if (index >= 0) {
			moveCommitted(boss, 0.55D, 0.03D);
			hitLine(boss, target, 4.0D, 0.95D, 5.0F, index);
		}
	}

	private static void updraftImpalement(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 16 && hitLine(boss, target, 4.5D, 1.0D, 11.0F, 0)) {
			target.push(0.0D, 0.72D, 0.0D);
			cast(boss, target, "scalding_updraft", 0.7D);
		}
		if (tick == 22) boss.setDeltaMovement(0.0D, -0.8D, 0.0D);
		if (tick == 24) hitLine(boss, target, 4.0D, 1.1D, 5.0F, 1);
	}

	private static void flammeusConcentration(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 1) boss.playSound(SoundEvents.FIRECHARGE_USE, 1.25F, 0.62F);
		if (tick == VesperFlammeusBreathRules.WINDUP_TICKS) {
			boss.playSound(SoundInit.ITEM_LIVING_TORCH_BREATH_LOOP.get(), 1.05F, 0.72F);
		}
		flammeusBreathEffect(boss, tick);
		if (!VesperFlammeusBreathRules.isDamagePulse(tick)) return;
		Vec3 origin = boss.getEyePosition();
		Vec3 look = boss.getLookAngle().normalize();
		AABB bounds = new AABB(origin, origin).inflate(LivingTorchBreathRules.RANGE);
		for (LivingEntity candidate : boss.level().getEntitiesOfClass(LivingEntity.class, bounds,
				entity -> entity instanceof net.minecraft.world.entity.player.Player player
						&& !player.isCreative() && !player.isSpectator()
						&& entity.isAlive() && entity != boss && boss.canAttack(entity)
						&& !entity.isAlliedTo(boss) && !boss.isAlliedTo(entity))) {
			Vec3 aim = candidate.getBoundingBox().getCenter();
			if (!LivingTorchBreathRules.isInsideCone(origin.x, origin.y, origin.z,
					look.x, look.y, look.z, aim.x, aim.y, aim.z)
					|| !LivingTorchBreathRules.canHitCandidate(boss.hasLineOfSight(candidate), true, false)) continue;
			candidate.hurt(HemoDamageTypes.livingTorchBreath(boss.level(), boss), LivingTorchBreathRules.DAMAGE_PER_PULSE);
		}
	}

	private static void flammeusBreathEffect(VesperTheEveningStarEntity boss, int tick) {
		if (!(boss.level() instanceof ServerLevel server) || tick < VesperFlammeusBreathRules.WINDUP_TICKS
				|| tick > VesperFlammeusBreathRules.LAST_DAMAGE_TICK) return;
		Vec3 direction = boss.getLookAngle().normalize();
		Vec3 origin = boss.getEyePosition().add(direction.scale(0.55D));
		for (int i = 0; i < 6; i++) {
			double distance = 0.65D + i * 1.05D;
			Vec3 point = origin.add(direction.scale(distance));
			VesperVisualEffects.embers(server, point,
					i % 2 == 0 ? VesperVisualEffects.BLOOD : VesperVisualEffects.BLACK,
					2, distance * 0.04D, distance * 0.03D, distance * 0.04D, 0.02D, 0.13F, 8);
			VesperVisualEffects.darkGlow(server, point, VesperVisualEffects.BLACK,
					1, distance * 0.025D, distance * 0.02D, distance * 0.025D, 0.0D);
			if ((i & 1) == 0) VesperVisualEffects.bloodCells(server, point, VesperVisualEffects.BLOOD,
					1, 0.03D, 0.03D, 0.03D, 0.01D);
		}
	}

	private static void chainSweep(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 18 && hitNear(boss, target, 6.5D, 13.0F, 0)) {
			pushAway(boss, target, 0.75D);
		}
	}

	private static void hookAndCrush(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 14 && hitLine(boss, target, 7.5D, 1.35D, 7.0F, 0)) pull(boss, target, 0.9D);
		if (tick == 28 && hitNear(boss, target, 5.0D, 12.0F, 1)) {
			cast(boss, target, (boss.getActionVariant() & 1) == 0
					? "glacial_grasp" : "glacial_rampart", 0.65D);
		}
	}

	private static void magneticAxis(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 18) {
			spawnIronLane(boss, 3);
			if (boss.distanceTo(target) < 8.0F) pushAway(boss, target, 1.1D); else pull(boss, target, 0.85D);
			cast(boss, target, "sanguine_magnetism", 0.65D);
			markOwnedIronPillars(boss);
		}
	}

	private static void ironRetort(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 16) cast(boss, target, "iron_retort", 0.7D);
		Vec3 approach = boss.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
		boolean approaching = approach.lengthSqr() > 0.01D && target.getDeltaMovement().dot(approach.normalize()) > 0.05D;
		if (tick >= 16 && tick <= 32 && boss.distanceTo(target) <= 5.5F && (boss.hurtTime > 0 || approaching)
				&& hitNear(boss, target, 5.5D, 12.0F, 0)) {
			spawnIronRetort(boss, 8);
			pushAway(boss, target, 1.2D);
			cast(boss, target, "sanguine_magnetism", 0.55D);
			markOwnedIronPillars(boss);
		}
	}

	// ---- chained-moveset additions

	/** Blade follow-up: a rising cut out of the low line that pops the target into the air. */
	private static void bladeRisingCut(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 10) {
			moveCommitted(boss, 0.5D, 0.2D);
			if (hitArc(boss, target, 4.0D, 120.0D, 9.0F, 0)) target.push(0.0D, 0.55D, 0.0D);
		}
	}

	/** Blade finisher: a drawing step straight through the target, then a turning cut on the far side. */
	private static void bladeDrawingStep(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick < 16) stopHorizontal(boss);
		if (tick == 16) {
			dashThrough(boss, 1.6D, true);
			hitLine(boss, target, 6.5D, 1.1D, 12.0F, 0);
		}
		if (tick == 24 && hitArc(boss, target, 4.5D, 140.0D, 10.0F, 1)) cast(boss, target, "blood_aneurysm", 0.6D);
	}

	/** Axe opener: a short haft shove that knocks a crowding target off him. */
	private static void axeHaftShove(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 10 && hitArc(boss, target, 3.5D, 100.0D, 6.0F, 0)) pushAway(boss, target, 1.1D);
	}

	/** Axe finisher: an overhead splitting the floor along a lane, then the split bursts where it was aimed. */
	private static void axeGraveSplitter(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 24 && hitLine(boss, target, 7.0D, 1.4D, 18.0F, 0)) cast(boss, target, "grave_debt", 0.6D);
		if (tick == 30) hitArea(boss, target, boss.getLockedActionAim(), 4.0D, 8.0F, 1);
	}

	/** Spear follow-up: vaults off the planted spear into a kick, then a knee that drives the target away. */
	private static void spearVaultKick(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 12) {
			moveToward(boss, target.position(), 0.9D, 0.55D);
			hitArc(boss, target, 3.5D, 140.0D, 7.0F, 0);
		}
		if (tick == 18 && hitNear(boss, target, 3.2D, 6.0F, 1)) pushAway(boss, target, 1.0D);
	}

	/** Spear finisher: a long lunging thrust that pins the target in place and flares. */
	private static void spearPinningThrust(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick < 18) stopHorizontal(boss);
		if (tick == 18) {
			moveCommitted(boss, 0.9D, 0.0D);
			if (hitLine(boss, target, 6.5D, 1.0D, 15.0F, 0)) {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3));
				cast(boss, target, "hematic_flare", 0.6D);
			}
		}
	}

	/** Claw opener: a feint that slips to the flank before the real rake lands. */
	private static void clawShadowFeint(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 6) safeFlank(boss, target);
		if (tick == 14) hitArc(boss, target, 3.8D, 160.0D, 8.0F, 0);
	}

	/** Claw finisher: three quick rakes and a disembowelling hook that lacerates. */
	private static void clawEviscerate(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		int bit = tick == 12 ? 0 : tick == 16 ? 1 : tick == 20 ? 2 : -1;
		if (bit >= 0) {
			moveCommitted(boss, 0.25D, 0.0D);
			hitArc(boss, target, 3.6D, 140.0D, 4.0F, bit);
		}
		if (tick == 24 && hitArc(boss, target, 4.0D, 160.0D, 9.0F, 3)) cast(boss, target, "gloam_laceration", 0.7D);
	}

	/** Crossbow opener: a snap shot fired while hopping back out of reach. */
	private static void crossbowSnapShot(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 10) {
			fireBolt(boss, target, 0.0F);
			Vec3 back = committedDirection(boss).scale(-0.6D);
			boss.setDeltaMovement(back.x, 0.2D, back.z);
		}
	}

	/** Crossbow follow-up: a staggered rain of bolts walking across the target's lane. */
	private static void crossbowBoltRain(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 20) fireBolt(boss, target, -7.0F);
		if (tick == 26) fireBolt(boss, target, 7.0F);
		if (tick == 32) fireBolt(boss, target, 0.0F);
	}

	/** Torch follow-up: a wide burning sweep that sets the target alight. */
	private static void torchCinderSweep(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 14 && hitArc(boss, target, 4.8D, 170.0D, 10.0F, 0)) {
			target.setRemainingFireTicks(60);
			cast(boss, target, "sanguine_ignition", 0.6D);
		}
	}

	/** Flail follow-up: the head orbits him three times, catching anything inside its reach. */
	private static void flailOrbit(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		int bit = tick == 12 ? 0 : tick == 20 ? 1 : tick == 28 ? 2 : -1;
		if (bit >= 0 && hitNear(boss, target, 5.5D, 6.0F, bit)) pushAway(boss, target, 0.4D);
	}

	/** Flail finisher: the head comes down where he aimed it and the ground freezes and shatters. */
	private static void flailShatterSlam(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 22 && hitArea(boss, target, boss.getLockedActionAim(), 4.5D, 16.0F, 0)) {
			cast(boss, target, "cryogenic_pulse", 0.65D);
		}
	}

	/** Staff follow-up: a broad sweep that drags the target in on lodestone pull. */
	private static void staffLodestoneSweep(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 14 && hitArc(boss, target, 5.5D, 180.0D, 9.0F, 0)) pull(boss, target, 0.7D);
	}

	/** Staff follow-up: raises an iron lane and vaults off it to land on the target. */
	private static void staffPillarVault(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 16) {
			spawnIronLane(boss, 2);
			markOwnedIronPillars(boss);
			moveToward(boss, boss.getLockedActionAim(), 1.0D, 0.6D);
		}
		if (tick == 22 && hitArea(boss, target, boss.position(), 4.0D, 11.0F, 1)) {
			cast(boss, target, "sanguine_magnetism", 0.55D);
		}
	}

	// ---- the expanded sickle act

	private static void sickleReap(VesperTheEveningStarEntity boss, LivingEntity target, int tick, boolean left) {
		if (tick == 10) {
			dashThrough(boss, 0.55D, !left);
			if (hitArc(boss, target, 4.0D, 150.0D, 5.5F, 0)) sickleSlashVisual(boss, target, left, 1.1F);
		}
	}

	/** Throws the hook (with a glint first); a catch reels the target in until the reel breaks or the hook lands. */
	private static void sickleHookReel(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 12) fireSickleHook(boss, target);
		if (tick > 12 && tick <= 24) reel(boss, 0.38D);
		if (tick == 24) hitNear(boss, target, 3.0D, 4.0F, 1);
	}

	/** Spins while reeling: the hooked target is dragged into the turning blades. */
	private static void sickleReelCyclone(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		reel(boss, 0.22D);
		int bit = contactIndex(VesperWeaponAction.SICKLE_REEL_CYCLONE, tick);
		if (bit >= 0 && hitNear(boss, target, 4.5D, 5.0F, bit)) sickleSlashVisual(boss, target, (bit & 1) == 0, 1.2F);
	}

	/** A rising cut off the reel that launches the target into the air. */
	private static void sickleReapersRise(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 12 && hitArc(boss, target, 3.5D, 140.0D, 9.0F, 0)) {
			target.push(0.0D, 0.95D, 0.0D);
			target.hurtMarked = true;
			sickleSlashVisual(boss, target, true, 1.4F);
			sickleSlashVisual(boss, target, false, 1.4F);
		}
	}

	/** Reels himself past the target instead, and cuts back from behind. */
	private static void sicklePassThrough(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 10) {
			Vec3 through = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			if (through.lengthSqr() > 1.0E-4D) moveToward(boss, target.position().add(through.normalize().scale(2.5D)),
					1.5D, 0.1D);
			hitNear(boss, target, 2.5D, 5.0F, 0);
		}
		if (tick == 18) {
			boss.getLookControl().setLookAt(target, 180.0F, 30.0F);
			if (hitNear(boss, target, 4.0D, 9.0F, 1)) sickleSlashVisual(boss, target, false, 1.3F);
		}
	}

	/**
	 * Both hooks thrown wide on tethers that cross the target's position; he spins and the lines sweep around him.
	 * The lines run at waist height: jump them or get out of the ring.
	 */
	private static void sickleGallowsWheel(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick < 16 || tick > 40) return;
		double base = Math.toRadians(boss.getYRot() + 90.0D);
		double sweep = (tick - 16) * 0.26D;
		if (boss.level() instanceof ServerLevel server && tick % 2 == 0) {
			for (int line = 0; line < 2; line++) {
				double angle = base + sweep + line * Math.PI;
				Vec3 end = boss.position().add(Math.cos(angle) * 6.0D, 1.0D, Math.sin(angle) * 6.0D);
				VesperVisualEffects.tendril(server, boss.position().add(0.0D, 1.0D, 0.0D), end, false,
						boss.getId() * 7L + tick + line);
			}
		}
		int bit = contactIndex(VesperWeaponAction.SICKLE_GALLOWS_WHEEL, tick);
		if (bit < 0) return;
		Vec3 to = target.position().subtract(boss.position());
		double distance = to.horizontalDistance();
		boolean jumped = target.getY() > boss.getY() + 1.8D;
		if (distance < 2.0D || distance > 6.5D || jumped) return;
		double targetAngle = Math.atan2(to.z, to.x);
		for (int line = 0; line < 2; line++) {
			double lineAngle = base + sweep + line * Math.PI;
			if (Math.abs(Mth.wrapDegrees(Math.toDegrees(targetAngle - lineAngle))) < 22.0D
					&& hitNear(boss, target, 6.5D, 5.0F, bit)) {
				pull(boss, target, 0.25D);
				break;
			}
		}
	}

	/** A spin that walks after the target at a capped turn rate; the speed-up into it is the tell. */
	private static void sickleTravellingCyclone(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		float progress = tick / (float) VesperWeaponAction.SICKLE_TRAVELLING_CYCLONE.durationTicks();
		float rate = VesperSickleRules.spinRate(progress);
		if (tick >= 8 && tick <= 40) {
			Vec3 toward = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			if (toward.lengthSqr() > 0.01D) setSafeMovement(boss, toward.normalize().scale(0.3D * rate + 0.06D));
		}
		int bit = contactIndex(VesperWeaponAction.SICKLE_TRAVELLING_CYCLONE, tick);
		if (bit >= 0 && hitNear(boss, target, 4.2D, 4.5F, bit)) sickleSlashVisual(boss, target, (bit & 1) == 0, 1.2F);
	}

	/** A slow vortex draws the target in, then the spin reverses into an outward burst. */
	private static void sickleReverseCyclone(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick >= 4 && tick < 20 && boss.distanceTo(target) < 9.0F) {
			pull(boss, target, 0.12D);
			if (boss.level() instanceof ServerLevel server && tick % 3 == 0) {
				VesperVisualEffects.telegraphRing(server, boss.position(), 5.0D - tick * 0.15D,
						VesperVisualEffects.BLACK, 24);
			}
		}
		if ((tick == 20 || tick == 32) && hitNear(boss, target, 5.0D, 7.0F, tick == 20 ? 0 : 1)) {
			pushAway(boss, target, 1.2D);
		}
	}

	/** A short fake spin that cuts straight into a pounce. */
	private static void sickleFeintCyclone(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 10) hitNear(boss, target, 3.8D, 4.0F, 0);
	}

	/** Leaps and lands short on purpose; the hook comes out of the landing. */
	private static void sicklePounceFeint(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 8) {
			Vec3 toward = boss.getLockedActionAim().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			Vec3 shortLanding = toward.lengthSqr() > 9.0D
					? boss.getLockedActionAim().subtract(toward.normalize().scale(3.0D)) : boss.position();
			moveToward(boss, shortLanding, 1.2D, 0.5D);
		}
	}

	private static void sickleCrescentFan(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 14) {
			for (int i = -2; i <= 2; i++) fireSickleArc(boss, target, i * 12.0F);
			sickleArcVisual(boss, true, 1.6F);
		}
	}

	private static void sickleCrescentStagger(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		int wave = contactIndex(VesperWeaponAction.SICKLE_CRESCENT_STAGGER, tick);
		if (wave >= 0) {
			fireSickleArc(boss, target, (wave & 1) == 0 ? -6.0F : 6.0F);
			sickleArcVisual(boss, (wave & 1) == 0, 1.3F);
		}
	}

	/** Low crescents skimming the floor: jump them. */
	private static void sickleCrescentSkim(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 12 || tick == 20) {
			fireSickleArcLow(boss, target, tick == 12 ? -4.0F : 4.0F);
			sickleArcVisual(boss, tick == 12, 1.4F);
		}
	}

	/** A crescent thrown out past the target that comes back through it from behind. */
	private static void sickleCrescentReturn(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 12) {
			fireSickleArc(boss, target, 0.0F);
			sickleArcVisual(boss, true, 1.4F);
		}
		if (tick == 30) fireReturningArc(boss, target);
	}

	/** Hooks the floor off to one side and slingshots himself out of the corner. */
	private static void sickleAnchorHook(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		Vec3 toward = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		if (toward.lengthSqr() < 1.0E-4D) toward = new Vec3(1.0D, 0.0D, 0.0D);
		Vec3 side = new Vec3(-toward.z, 0.0D, toward.x).normalize().scale(((boss.getActionVariant() & 1) == 0) ? 7.0D : -7.0D);
		Vec3 anchor = boss.position().add(side).add(toward.normalize().scale(2.0D));
		if (tick == 12 && boss.level() instanceof ServerLevel server) {
			VesperVisualEffects.tendril(server, boss.position().add(0.0D, 1.0D, 0.0D), anchor, false, boss.getId() * 11L);
			VesperVisualEffects.floorChips(server, anchor, 10, 0.6D);
		}
		if (tick > 12 && tick < 20) moveToward(boss, anchor, 1.3D, 0.15D);
		if (tick == 20) hitNear(boss, target, 3.0D, 5.0F, 1);
	}

	/** His last stand, once, below a tenth of his health: hook, reel, reel-spin, cross rend, returning volley. */
	private static void sickleHarvest(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick == 1) boss.playSound(SoundEvents.WITHER_AMBIENT, 1.4F, 0.5F);
		if (tick < 14) stopHorizontal(boss);
		if (tick == 14) fireSickleHook(boss, target);
		if (tick > 14 && tick <= 30) reel(boss, 0.4D);
		if (tick == 30 || tick == 38) {
			if (hitNear(boss, target, 4.5D, 6.0F, tick == 30 ? 1 : 2)) sickleSlashVisual(boss, target, tick == 30, 1.4F);
		}
		if (tick == 46 && hitArc(boss, target, 4.8D, 170.0D, 10.0F, 3)) {
			sickleSlashVisual(boss, target, true, 1.6F);
			sickleSlashVisual(boss, target, false, 1.6F);
		}
		if (tick == 54) for (int i = -1; i <= 1; i++) fireSickleArc(boss, target, i * 14.0F);
		if (tick == 62) fireReturningArc(boss, target);
		if (tick == 70 && hitArea(boss, target, boss.position(), 5.0D, 14.0F, 4)) pushAway(boss, target, 1.4D);
	}

	/** Pulls the hooked target in along the tether, drawing the line taut; the reel snaps if it is fought. */
	private static void reel(VesperTheEveningStarEntity boss, double strength) {
		LivingEntity hooked = boss.hookedTarget();
		if (hooked == null) return;
		if (VesperSickleRules.reelBreaks(boss.reelDamage(), boss.reelSprintTicks())) {
			boss.breakReel();
			return;
		}
		Vec3 toward = boss.position().subtract(hooked.position()).multiply(1.0D, 0.0D, 1.0D);
		if (toward.lengthSqr() > 2.25D) {
			Vec3 away = toward.normalize().scale(-1.0D);
			boolean fighting = hooked instanceof net.minecraft.world.entity.player.Player player && player.isSprinting()
					&& hooked.getDeltaMovement().dot(away) > 0.05D;
			boss.addReelSprint(fighting ? 1 : 0);
			hooked.setDeltaMovement(hooked.getDeltaMovement().scale(0.4D).add(toward.normalize().scale(strength)));
			hooked.hurtMarked = true;
		}
		if (boss.level() instanceof ServerLevel server && boss.tickCount % 3 == 0) {
			VesperVisualEffects.tendril(server, boss.position().add(0.0D, 1.1D, 0.0D),
					hooked.position().add(0.0D, hooked.getBbHeight() * 0.5D, 0.0D), false, boss.getId() * 3L + boss.tickCount);
		}
	}

	private static int contactIndex(VesperWeaponAction action, int tick) {
		for (int i = 0; i < action.contactCount(); i++) if (action.contactTick(i) == tick) return i;
		return -1;
	}

	private static void fireSickleArcLow(VesperTheEveningStarEntity boss, LivingEntity target, float yawOffset) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		BloodBoltEntity bolt = new BloodBoltEntity(server, boss, new ItemStack(ItemInit.living_sickle.get()));
		Vec3 start = boss.position().add(0.0D, 0.35D, 0.0D);
		bolt.setPos(start.x, start.y, start.z);
		Vec3 aim = boss.getLockedActionAim().add(0.0D, 0.3D, 0.0D).subtract(start);
		double yaw = Math.atan2(aim.z, aim.x) + Math.toRadians(yawOffset);
		double horizontal = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
		bolt.shoot(Math.cos(yaw) * horizontal, 0.0D, Math.sin(yaw) * horizontal, 1.5F, 0.5F);
		bolt.setNoGravity(true);
		bolt.setBaseDamage(5.0D);
		server.addFreshEntity(bolt);
		if (boss.level() instanceof ServerLevel s) {
			VesperVisualEffects.telegraphLine(s, start, start.add(aim.normalize().scale(10.0D)), VesperVisualEffects.BLOOD);
		}
		boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 0.7F);
	}

	private static void fireReturningArc(VesperTheEveningStarEntity boss, LivingEntity target) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		BloodBoltEntity bolt = new BloodBoltEntity(server, boss, new ItemStack(ItemInit.living_sickle.get()));
		Vec3 behind = target.position().add(target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D)
				.normalize().scale(5.0D)).add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
		bolt.setPos(behind.x, behind.y, behind.z);
		Vec3 back = boss.position().add(0.0D, 1.0D, 0.0D).subtract(behind);
		bolt.shoot(back.x, back.y, back.z, 1.6F, 0.5F);
		bolt.setBaseDamage(6.0D);
		server.addFreshEntity(bolt);
		VesperVisualEffects.darkGlow(server, behind, VesperVisualEffects.BLACK, 10, 0.4D, 0.4D, 0.4D, 0.04D);
		boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.25F, 0.55F);
	}

	private static void sickleCyclone(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick < 10) stopHorizontal(boss);
		if (tick >= 10 && tick <= 24) {
			boss.setYRot(boss.getYRot() + 42.0F);
			boss.setYBodyRot(boss.getYRot());
			Vec3 toward = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			if (toward.lengthSqr() > 0.01D) {
				Vec3 side = new Vec3(-toward.z, 0.0D, toward.x).normalize()
						.scale((boss.getActionVariant() & 1) == 0 ? 0.42D : -0.42D);
				setSafeMovement(boss, toward.normalize().scale(0.24D).add(side));
			}
		}
		int bit = tick == 12 ? 0 : tick == 18 ? 1 : tick == 24 ? 2 : -1;
		if (bit >= 0 && hitNear(boss, target, 5.2D, 6.0F, bit)) {
			sickleSlashVisual(boss, target, bit != 1, 1.35F);
		}
	}

	private static void sicklePounce(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick < 9) stopHorizontal(boss);
		if (tick == 10) {
			double distance = boss.position().distanceTo(boss.getLockedActionAim());
			moveToward(boss, boss.getLockedActionAim(), Mth.clamp(distance / 3.2D, 1.45D, 3.0D), 0.68D);
		}
		if (tick == 14 && boss.position().distanceToSqr(boss.getLockedActionAim()) <= 25.0D
				&& hitArea(boss, target, boss.position(), 4.4D, 11.0F, 0)) {
			sickleSlashVisual(boss, target, true, 1.5F);
			sickleSlashVisual(boss, target, false, 1.5F);
		}
	}

	private static void sickleCrossRend(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		int bit = tick == 10 ? 0 : tick == 14 ? 1 : tick == 18 ? 2 : -1;
		if (bit < 0) return;
		dashThrough(boss, bit == 1 ? 0.82D : 1.05D, (bit & 1) == 0);
		if (hitArc(boss, target, 4.8D, 155.0D, 6.0F, bit)) {
			sickleSlashVisual(boss, target, (bit & 1) == 0, 1.2F);
		}
	}

	private static void sickleHook(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		stopHorizontal(boss);
		if (tick == 14) fireSickleHook(boss, target);
	}

	private static void sanguineCrescents(VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		if (tick < 10) stopHorizontal(boss);
		int wave = tick == 12 ? 0 : tick == 17 ? 1 : tick == 22 ? 2 : -1;
		if (wave >= 0) {
			fireSickleArc(boss, target, wave == 0 ? -8.0F : wave == 1 ? 8.0F : 0.0F);
			sickleArcVisual(boss, (wave & 1) == 0, 1.45F);
		}
	}

	private static void emitWeaponEffect(VesperTheEveningStarEntity boss, LivingEntity target,
			VesperWeaponAction action, int tick) {
		if (!VesperWeaponEffectRules.shouldEmit(action, tick) || !(boss.level() instanceof ServerLevel server)) return;
		switch (VesperWeaponEffectRules.styleName(action)) {
			case "blood_blade" -> bloodBladeEffect(server, boss, action);
			case "living_axe" -> axeDebrisEffect(server, boss, action, tick);
			case "living_spear" -> spearSpiralEffect(server, boss, tick);
			case "gloam_claw" -> clawAttackEffect(server, boss, action, tick);
			case "crimson_torch" -> torchFireEffect(server, boss, target);
			case "glacial_flail" -> flailArcEffect(server, boss, action, tick);
			default -> { }
		}
	}

	private static void bloodBladeEffect(ServerLevel server, VesperTheEveningStarEntity boss,
			VesperWeaponAction action) {
		Vec3 forward = committedDirection(boss);
		Vec3 center = boss.position().add(0.0D, 1.35D, 0.0D).add(forward.scale(2.8D));
		VesperVisualEffects.bloodCells(server, center, VesperVisualEffects.BLOOD,
				action == VesperWeaponAction.ICHIMONJI ? 26 : 20, 1.45D, 1.0D, 1.45D, 0.085D);
		VesperVisualEffects.glow(server, center, VesperVisualEffects.BLOOD,
				14, 1.2D, 0.75D, 1.2D, 0.035D);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				10, 1.3D, 0.8D, 1.3D, 0.025D);
	}

	private static void axeDebrisEffect(ServerLevel server, VesperTheEveningStarEntity boss,
			VesperWeaponAction action, int tick) {
		Vec3 center = action == VesperWeaponAction.LEAPING_CLEAVE && tick == action.lastImpactTick()
				? boss.getLockedActionAim() : boss.position().add(committedDirection(boss).scale(3.0D));
		BlockPos floor = BlockPos.containing(center).below();
		var state = server.getBlockState(floor);
		if (state.isAir()) state = server.getBlockState(boss.blockPosition().below());
		if (!state.isAir()) {
			server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
					center.x, center.y + 0.35D, center.z, 28, 1.8D, 0.55D, 1.8D, 0.18D);
		}
		Vec3 wake = center.add(0.0D, 0.55D, 0.0D);
		VesperVisualEffects.darkGlow(server, wake, VesperVisualEffects.BLACK,
				20, 1.7D, 0.55D, 1.7D, 0.075D);
		VesperVisualEffects.embers(server, wake, VesperVisualEffects.DEEP_BLOOD,
				15, 1.5D, 0.65D, 1.5D, 0.055D, 0.24F, 24);
	}

	private static void spearSpiralEffect(ServerLevel server, VesperTheEveningStarEntity boss, int tick) {
		Vec3 forward = committedDirection(boss);
		Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
		Vec3 origin = boss.position().add(0.0D, 1.25D, 0.0D).subtract(forward.scale(1.8D));
		for (int i = 0; i < 14; i++) {
			double phase = tick * 0.82D + i * Mth.TWO_PI / 7.0D;
			double radius = 1.15D - i * 0.035D;
			Vec3 point = origin.add(forward.scale(i * 0.28D))
					.add(right.scale(Math.cos(phase) * radius))
					.add(0.0D, Math.sin(phase) * radius, 0.0D);
			if (i % 3 == 0) VesperVisualEffects.bloodCells(server, point, VesperVisualEffects.BLOOD,
					1, 0.0D, 0.0D, 0.0D, 0.0D);
			else VesperVisualEffects.glow(server, point, VesperVisualEffects.WHITE,
					1, 0.0D, 0.0D, 0.0D, 0.0D);
		}
		if (tick % 6 == 0) {
			VesperVisualEffects.tendril(server, origin, origin.add(forward.scale(4.4D)), false,
					boss.getActionVariant() * 257L + tick);
		}
		if (tick % 5 == 0) boss.playSound(SoundEvents.TRIDENT_RIPTIDE_1.value(), 0.9F, 0.8F);
	}

	private static void clawAttackEffect(ServerLevel server, VesperTheEveningStarEntity boss,
			VesperWeaponAction action, int tick) {
		Vec3 forward = committedDirection(boss);
		Vec3 center = boss.position().add(0.0D, 1.3D, 0.0D).add(forward.scale(2.8D));
		boolean mirrored = action == VesperWeaponAction.TWIN_REND ? tick == 12 : false;
		float scale = action == VesperWeaponAction.PREDATOR_POUNCE ? 1.18F : 0.9F;
		PacketHandler.sendClawSlash(center, forward, new ParticleColor(70, 0, 125), mirrored,
				scale, 64.0D, server);
		PacketHandler.sendClawParticles(center, new ParticleColor(88, 0, 138), 64.0D, server);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				12, 1.25D, 0.7D, 1.25D, 0.045D);
		if (action == VesperWeaponAction.PREDATOR_POUNCE) {
			PacketHandler.sendClawSlash(center, forward, new ParticleColor(45, 0, 135), true,
					1.18F, 64.0D, server);
		}
	}

	private static void torchFireEffect(ServerLevel server, VesperTheEveningStarEntity boss, LivingEntity target) {
		Vec3 forward = committedDirection(boss);
		Vec3 center = boss.position().add(0.0D, 1.25D, 0.0D).add(forward.scale(
				Math.min(3.2D, Math.max(1.5D, boss.distanceTo(target) * 0.55D))));
		VesperVisualEffects.embers(server, center, VesperVisualEffects.EMBER,
				34, 1.2D, 0.95D, 1.2D, 0.085D, 0.34F, 34);
		VesperVisualEffects.glow(server, center, new ParticleColor(255, 82, 12),
				20, 1.35D, 1.05D, 1.35D, 0.06D);
		VesperVisualEffects.spores(server, center, VesperVisualEffects.DEEP_BLOOD,
				14, 1.4D, 0.95D, 1.4D, 0.04D);
	}

	private static void flailArcEffect(ServerLevel server, VesperTheEveningStarEntity boss,
			VesperWeaponAction action, int tick) {
		double arcDegrees = VesperWeaponEffectRules.arcDegrees(action);
		double baseYaw = Math.atan2(committedDirection(boss).z, committedDirection(boss).x);
		double start = baseYaw - Math.toRadians(arcDegrees) * 0.5D;
		int points = action == VesperWeaponAction.CHAIN_SWEEP ? 32 : 24;
		for (int i = 0; i < points; i++) {
			double progress = points == 1 ? 0.0D : (double) i / (points - 1);
			double angle = start + Math.toRadians(arcDegrees) * progress;
			double radius = 2.2D + progress * 4.1D;
			Vec3 point = boss.position().add(Math.cos(angle) * radius,
					0.75D + Math.sin(progress * Math.PI) * 1.1D, Math.sin(angle) * radius);
			VesperVisualEffects.glow(server, point, (i & 1) == 0 ? VesperVisualEffects.ICE : VesperVisualEffects.WHITE,
					1, 0.04D, 0.04D, 0.04D, 0.0D);
			if (i % 4 == 0) VesperVisualEffects.bloodCells(server, point, VesperVisualEffects.ICE,
					1, 0.03D, 0.03D, 0.03D, 0.0D);
		}
		if (tick == action.lastImpactTick()) {
			Vec3 center = boss.position().add(0.0D, 0.8D, 0.0D);
			VesperVisualEffects.tendril(server, center.add(-3.0D, 0.0D, 0.0D),
					center.add(3.0D, 0.0D, 0.0D), true, boss.getActionVariant() * 311L + tick);
			VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.ICE,
					16, 3.2D, 0.45D, 3.2D, 0.035D);
		}
	}

	private static void finish(VesperTheEveningStarEntity boss) {
		float fraction = boss.getMaxHealth() <= 0.0F ? 1.0F : boss.getHealth() / boss.getMaxHealth();
		VesperWeaponAction action = boss.getWeaponAction();
		boss.setLastActionLanded(VesperSickleRules.landed(action, boss.getActionHitMask() != 0,
				boss.hookedTarget() != null));
		// A Hook and Reel catch stays on the line for the reel-spin, rise or pass-through that follows it.
		boolean holdTether = VesperSickleRules.holdsTether(action) && boss.hookedTarget() != null;
		boss.clearWeaponAction();
		// Links inside a string flow straight on; only a finisher (or an unchained move) earns the full recovery.
		boolean link = VesperChainGraph.inGraph(action) && action.role() != VesperChainGraph.Role.FINISHER;
		boss.setActionCooldown(boss.isRaging() ? VesperRageCombatRules.recoveryTicks()
				: link ? 3 : VesperWeaponCombatRules.recoveryTicks(10, fraction));
		double carry = VesperSickleRules.carryVelocity(action);
		if (carry > 0.0D) {
			// A spin's leftover momentum slingshots into whatever comes next instead of stopping dead.
			Vec3 forward = committedDirection(boss).scale(carry);
			boss.setDeltaMovement(forward.x, boss.getDeltaMovement().y, forward.z);
			if (!holdTether) boss.releaseHook();
			return;
		}
		if (!holdTether) boss.releaseHook();
		stopHorizontal(boss);
	}

	static void positionForRage(VesperTheEveningStarEntity boss, LivingEntity target, int strafeSide) {
		double distance = boss.distanceTo(target);
		boss.getLookControl().setLookAt(target, 55.0F, 45.0F);
		if (distance > 13.0D) {
			boss.getNavigation().moveTo(target, 1.45D);
			return;
		}
		boss.getNavigation().stop();
		Vec3 toward = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		if (toward.lengthSqr() < 0.01D) return;
		toward = toward.normalize();
		double direction = strafeSide;
		Vec3 side = new Vec3(-toward.z, 0.0D, toward.x).scale(0.48D * direction);
		double forward = distance < 3.0D ? -0.22D : distance > 8.0D ? 0.24D : 0.04D * direction;
		steer(boss, side.add(toward.scale(forward)));
	}

	static void positionForWeapon(VesperTheEveningStarEntity boss, LivingEntity target,
			EnumBloodTendency tendency, int side) {
		VesperWeaponCombatRules.RangeBand band = VesperWeaponCombatRules.rangeBand(tendency);
		double distance = boss.distanceTo(target);
		double haste = boss.getHealth() <= boss.getMaxHealth() * 0.25F ? 1.2D : 1.0D;
		boss.getLookControl().setLookAt(target, 35.0F, 35.0F);
		if (distance > band.maximum()) {
			boss.getNavigation().moveTo(target, (tendency == EnumBloodTendency.MORTEM ? 0.9D : 1.18D) * haste);
			return;
		}
		boss.getNavigation().stop();
		Vec3 away = boss.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
		if (away.lengthSqr() < 0.01D) away = new Vec3(1.0D, 0.0D, 0.0D);
		away = away.normalize();
		if (distance < band.minimum()) {
			double retreat = tendency == EnumBloodTendency.DUCTILIS ? 0.38D
					: tendency == EnumBloodTendency.FERRIC ? 0.28D : 0.22D;
			steer(boss, away.scale(retreat * haste));
		} else {
			double direction = side;
			double orbitSpeed = switch (tendency) {
				case TENEBRIS -> 0.32D;
				case LUX, DUCTILIS -> 0.22D;
				case ANIMUS, FLAMMEUS -> 0.19D;
				case CONGEATIO -> 0.17D;
				case FERRIC -> 0.14D;
				case MORTEM -> 0.08D;
			};
			Vec3 orbit = new Vec3(-away.z, 0.0D, away.x).scale(direction * orbitSpeed * haste);
			if (tendency == EnumBloodTendency.FLAMMEUS) orbit = orbit.add(away.scale(-0.08D * direction));
			steer(boss, orbit);
		}
	}

	static boolean readyToCommit(EnumBloodTendency tendency, double distance, VesperWeaponAction action) {
		VesperWeaponCombatRules.RangeBand band = VesperWeaponCombatRules.rangeBand(tendency);
		if (distance >= band.minimum() && distance <= band.maximum()) return true;
		return (action == VesperWeaponAction.STORM_LOCK && distance <= 4.0D)
				|| (action == VesperWeaponAction.IRON_RETORT && distance <= 5.0D);
	}

	static LivingEntity target(VesperTheEveningStarEntity boss) {
		LivingEntity target = boss.getTarget();
		if (target instanceof net.minecraft.world.entity.player.Player player && target.isAlive()
				&& !player.isCreative() && !player.isSpectator()
				&& boss.distanceToSqr(target) <= TARGET_RANGE * TARGET_RANGE) return target;
		List<LivingEntity> targets = boss.level().getEntitiesOfClass(LivingEntity.class,
				boss.getBoundingBox().inflate(TARGET_RANGE), entity -> entity.isAlive() && entity != boss
						&& entity instanceof net.minecraft.world.entity.player.Player player
						&& !player.isCreative() && !player.isSpectator());
		if (targets.isEmpty()) {
			// With no player in reach he still answers whatever he is fighting (a pet that bit him, a summon).
			LivingEntity current = boss.getTarget();
			return current != null && current.isAlive() && current != boss
					&& boss.distanceToSqr(current) <= TARGET_RANGE * TARGET_RANGE ? current : null;
		}
		target = targets.stream().min(java.util.Comparator.comparingDouble(boss::distanceToSqr)).orElse(null);
		boss.setTarget(target);
		return target;
	}

	static Vec3 predictedAim(LivingEntity target, VesperWeaponAction action) {
		double lead = switch (action) {
			case SKY_LANCE, LEAPING_CLEAVE, PREDATOR_POUNCE, SICKLE_POUNCE -> 7.0D;
			case CONDUCTIVE_VOLLEY, STORM_LOCK -> 5.0D;
			case SANGUINE_CRESCENTS, SICKLE_HOOK -> 4.0D;
			default -> 2.0D;
		};
		return target.position().add(target.getDeltaMovement().multiply(lead, 0.0D, lead));
	}

	static double attackAngle(VesperTheEveningStarEntity boss, LivingEntity target) {
		Vec3 facing = boss.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
		Vec3 toward = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		if (facing.lengthSqr() < 0.01D || toward.lengthSqr() < 0.01D) return 0.0D;
		return Math.toDegrees(Math.acos(Mth.clamp(facing.normalize().dot(toward.normalize()), -1.0D, 1.0D)));
	}

	private static void telegraph(VesperTheEveningStarEntity boss, VesperWeaponAction action, int tick) {
		boolean hookThrow = action == VesperWeaponAction.SICKLE_HOOK_REEL || action == VesperWeaponAction.SICKLE_HARVEST
				|| action == VesperWeaponAction.SICKLE_HOOK;
		int release = action == VesperWeaponAction.SICKLE_HARVEST ? 14 : action.impactTick();
		if (hookThrow && tick == release - 6 && boss.level() instanceof ServerLevel glintLevel) {
			// The blade glints before every hook throw.
			Vec3 blade = boss.getEyePosition().add(boss.getLookAngle().scale(0.8D)).subtract(0.0D, 0.3D, 0.0D);
			glintLevel.sendParticles(com.vincenthuto.hemomancy.common.particle.HemoParticleData.hitGlow(VesperVisualEffects.WHITE), blade.x, blade.y, blade.z,
					2, 0.05D, 0.05D, 0.05D, 0.0D);
			boss.playSound(SoundEvents.TRIDENT_RETURN, 0.9F, 1.6F);
		}
		int telegraphEnd = action == VesperWeaponAction.STORM_LOCK ? 44 : action.impactTick();
		if (!(boss.level() instanceof ServerLevel server) || tick > telegraphEnd || tick % 2 != 0) return;
		if (action == VesperWeaponAction.LEAPING_CLEAVE || action == VesperWeaponAction.SICKLE_POUNCE
				|| action == VesperWeaponAction.CHAIN_SWEEP
				|| action == VesperWeaponAction.IRON_RETORT) {
			boolean centeredOnBoss = action == VesperWeaponAction.CHAIN_SWEEP || action == VesperWeaponAction.IRON_RETORT;
			ring(server, centeredOnBoss ? boss.position() : boss.getLockedActionAim(),
					action == VesperWeaponAction.CHAIN_SWEEP ? 6.5D : action == VesperWeaponAction.IRON_RETORT ? 5.5D : 4.5D,
					new ParticleColor(230, 20, 13));
		} else if (action == VesperWeaponAction.SICKLE_CYCLONE) {
			ring(server, boss.position(), 5.2D, VesperVisualEffects.BLOOD);
		} else if (action == VesperWeaponAction.HOOK_AND_CRUSH && tick > 14) {
			ring(server, boss.position(), 5.0D, VesperVisualEffects.ICE);
		} else if (action == VesperWeaponAction.STORM_LOCK) {
			for (int strike = 28; strike <= 44; strike += 8) ring(server, stormPoint(boss.getLockedActionAim(), strike),
					2.2D, new ParticleColor(242, 232, 92));
		} else {
			Vec3 from = boss.position().add(0.0D, 1.2D, 0.0D);
			double length = Math.max(8.0D, boss.getLockedActionOrigin().distanceTo(boss.getLockedActionAim()));
			line(server, from, from.add(committedDirection(boss).scale(length)),
					VesperVisualEffects.BLOOD);
		}
	}

	private static boolean hitNear(VesperTheEveningStarEntity boss, LivingEntity target,
			double range, float damage, int bit) {
		if (!VesperWeaponCombatRules.canApplyHit(boss.getActionHitMask(), bit) || boss.distanceTo(target) > range) {
			return false;
		}
		boss.setActionHitMask(VesperWeaponCombatRules.recordHit(boss.getActionHitMask(), bit));
		boolean hit = target.hurt(boss.damageSources().mobAttack(boss), damage);
		if (hit) EndgameBossActions.disableShieldOnHit(boss, target, 100);
		return hit;
	}

	private static boolean hitArea(VesperTheEveningStarEntity boss, LivingEntity target, Vec3 center,
			double range, float damage, int bit) {
		if (!VesperWeaponCombatRules.canApplyHit(boss.getActionHitMask(), bit)
				|| target.position().distanceToSqr(center) > range * range) return false;
		boss.setActionHitMask(VesperWeaponCombatRules.recordHit(boss.getActionHitMask(), bit));
		boolean hit = target.hurt(boss.damageSources().mobAttack(boss), damage);
		if (hit) EndgameBossActions.disableShieldOnHit(boss, target, 100);
		return hit;
	}

	private static boolean hitArc(VesperTheEveningStarEntity boss, LivingEntity target, double range,
			double degrees, float damage, int bit) {
		Vec3 forward = committedDirection(boss);
		Vec3 toTarget = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		if (toTarget.lengthSqr() > range * range) return false;
		double angle = Math.toDegrees(Math.acos(Mth.clamp(forward.dot(toTarget.normalize()), -1.0D, 1.0D)));
		return VesperWeaponCombatRules.withinArc(toTarget.length(), angle, range, degrees)
				&& hitNear(boss, target, range, damage, bit);
	}

	private static boolean hitLine(VesperTheEveningStarEntity boss, LivingEntity target, double length,
			double width, float damage, int bit) {
		Vec3 forward = committedDirection(boss);
		Vec3 relative = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		double along = relative.dot(forward);
		return VesperWeaponCombatRules.withinLane(along,
				relative.subtract(forward.scale(along)).horizontalDistance(), length, width)
				&& hitNear(boss, target, length, damage, bit);
	}

	static boolean cast(VesperTheEveningStarEntity boss, LivingEntity target, String name, double scale) {
		var manipulation = ManipulationInit.MANIPS_TYPE_REGISTRY.get(com.vincenthuto.hemomancy.Hemomancy.rloc(name));
		if (manipulation == null) return false;
		ManipulationCastContext context = new ManipulationCastContext(boss, boss.level(), boss.getLivingWeaponStack(),
				boss.blockPosition(), target, target.getEyePosition().subtract(boss.getEyePosition()).normalize(),
				false, false, 1.15D, scale);
		return EntityManipulationEffects.cast(manipulation, context);
	}

	private static void fireBolt(VesperTheEveningStarEntity boss, LivingEntity target, float yawOffset) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		BloodBoltEntity bolt = new BloodBoltEntity(server, boss, new ItemStack(ItemInit.living_crossbow.get()));
		Vec3 aim = boss.getLockedActionAim().add(0.0D, target.getBbHeight() * 0.6D, 0.0D)
				.subtract(boss.getEyePosition());
		double yaw = Math.atan2(aim.z, aim.x) + Math.toRadians(yawOffset);
		double horizontal = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
		bolt.shoot(Math.cos(yaw) * horizontal, aim.y, Math.sin(yaw) * horizontal, 2.15F, 2.0F);
		bolt.setBaseDamage(6.0D);
		server.addFreshEntity(bolt);
		boss.playSound(SoundEvents.CROSSBOW_SHOOT, 1.2F, 0.8F + yawOffset * 0.01F);
	}

	private static void fireSickleArc(VesperTheEveningStarEntity boss, LivingEntity target, float yawOffset) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		BloodBoltEntity bolt = new BloodBoltEntity(server, boss, new ItemStack(ItemInit.living_sickle.get()));
		Vec3 aim = boss.getLockedActionAim().add(0.0D, target.getBbHeight() * 0.55D, 0.0D)
				.subtract(boss.getEyePosition());
		double yaw = Math.atan2(aim.z, aim.x) + Math.toRadians(yawOffset);
		double horizontal = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
		bolt.shoot(Math.cos(yaw) * horizontal, aim.y, Math.sin(yaw) * horizontal, 1.8F, 1.5F);
		bolt.setBaseDamage(5.0D);
		server.addFreshEntity(bolt);
		boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.25F, 0.8F + boss.getRandom().nextFloat() * 0.35F);
	}

	private static void fireSickleHook(VesperTheEveningStarEntity boss, LivingEntity target) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		LivingSickleHookEntity hook = new LivingSickleHookEntity(server, boss);
		hook.setAttackDamage(7.0F);
		Vec3 aim = boss.getLockedActionAim().add(0.0D, target.getBbHeight() * 0.55D, 0.0D)
				.subtract(boss.getEyePosition());
		hook.shoot(aim.x, aim.y, aim.z, 2.0F, 0.6F);
		server.addFreshEntity(hook);
		hook.spawnBloodTendril();
		boss.playSound(SoundEvents.TRIDENT_THROW.value(), 1.15F, 0.68F);
	}

	private static void lightningCircle(VesperTheEveningStarEntity boss, LivingEntity target, Vec3 center, int bit) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		ring(server, center, 2.2D, new ParticleColor(242, 232, 92));
		long seed = boss.getActionVariant() * 401L + bit;
		VesperVisualEffects.lightning(server, center.add(-2.0D, 0.15D, 0.0D),
				center.add(2.0D, 0.35D, 0.0D), false, seed);
		VesperVisualEffects.lightning(server, center.add(0.0D, 0.25D, -2.0D),
				center.add(0.0D, 0.1D, 2.0D), false, seed + 1L);
		if (target.position().distanceToSqr(center) <= 4.84D
				&& VesperWeaponCombatRules.canApplyHit(boss.getActionHitMask(), bit)) {
			boss.setActionHitMask(VesperWeaponCombatRules.recordHit(boss.getActionHitMask(), bit));
			DuctilisLightningEffects.synapticJolt(boss, target);
			target.hurt(boss.damageSources().magic(), 8.0F);
			pushAway(boss, target, 0.7D);
		}
	}

	private static Vec3 stormPoint(Vec3 origin, int strikeTick) {
		int index = (strikeTick - 28) / 8;
		double angle = index * Mth.TWO_PI / 3.0D;
		return origin.add(Math.cos(angle) * 2.8D, 0.0D, Math.sin(angle) * 2.8D);
	}

	static void safeFlank(VesperTheEveningStarEntity boss, LivingEntity target) {
		Vec3 look = target.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
		Vec3 horizontal = target.position().subtract(look.scale(2.8D));
		for (int dy = 1; dy >= -4; dy--) {
			BlockPos feet = BlockPos.containing(horizontal.x, target.getY() + dy, horizontal.z);
			BlockPos floor = feet.below();
			if (boss.level().getBlockState(floor).getCollisionShape(boss.level(), floor).isEmpty()) continue;
			Vec3 destination = new Vec3(horizontal.x, feet.getY(), horizontal.z);
			AABB moved = boss.getBoundingBox().move(destination.subtract(boss.position()));
			if (boss.level().noCollision(boss, moved)) {
				boss.teleportTo(destination.x, destination.y, destination.z);
				return;
			}
		}
	}

	private static void spawnIronLane(VesperTheEveningStarEntity boss, int count) {
		for (int i = 1; i <= count; i++) {
			spawnIronSpike(boss, boss.position().add(committedDirection(boss).scale(i * 2.4D)), 60);
		}
	}

	private static void spawnIronRetort(VesperTheEveningStarEntity boss, int count) {
		for (int i = 0; i < count; i++) {
			double angle = Mth.TWO_PI * i / count;
			spawnIronSpike(boss, boss.position().add(Math.cos(angle) * 3.4D, 0.0D, Math.sin(angle) * 3.4D), 45);
		}
	}

	private static void spawnIronSpike(VesperTheEveningStarEntity boss, Vec3 near, int lifeTicks) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		for (int dy = 2; dy >= -4; dy--) {
			BlockPos feet = BlockPos.containing(near.x, boss.getY() + dy, near.z);
			BlockPos floor = feet.below();
			if (server.getBlockState(floor).getCollisionShape(server, floor).isEmpty()
					|| !server.getBlockState(feet).canBeReplaced()) continue;
			EntityIronSpike spike = new EntityIronSpike(EntityInit.iron_spike.get(), server, boss);
			spike.moveTo(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D, boss.getYRot(), 0.0F);
			spike.setTemporaryResponse(lifeTicks, false);
			spike.getPersistentData().putUUID(VESPER_IRON_OWNER, boss.getUUID());
			server.addFreshEntity(spike);
			return;
		}
	}

	static void markOwnedIronPillars(VesperTheEveningStarEntity boss) {
		for (EntityIronPillar pillar : boss.level().getEntitiesOfClass(EntityIronPillar.class,
				boss.getBoundingBox().inflate(24.0D), candidate -> candidate.getCreator() == boss)) {
			pillar.getPersistentData().putUUID(VESPER_IRON_OWNER, boss.getUUID());
		}
	}

	private static boolean isOwnedIron(LivingEntity creator, net.minecraft.nbt.CompoundTag data,
			VesperTheEveningStarEntity boss) {
		return creator == boss || (data.hasUUID(VESPER_IRON_OWNER)
				&& boss.getUUID().equals(data.getUUID(VESPER_IRON_OWNER)));
	}

	private static void sickleSlashVisual(VesperTheEveningStarEntity boss, LivingEntity target,
			boolean mirrored, float scale) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		PacketHandler.sendClawSlash(target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D),
				committedDirection(boss), new ParticleColor(220, 0, 12), mirrored, scale, 64.0D, server);
	}

	private static void sickleArcVisual(VesperTheEveningStarEntity boss, boolean mirrored, float scale) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 direction = committedDirection(boss);
		PacketHandler.sendClawSlash(boss.getEyePosition().add(direction.scale(1.5D)), direction,
				new ParticleColor(220, 0, 12), mirrored, scale, 64.0D, server);
	}

	private static void faceLockedOrTarget(VesperTheEveningStarEntity boss, LivingEntity target, boolean locked) {
		Vec3 point = locked ? boss.position().add(committedDirection(boss)) : target.getEyePosition();
		double dx = point.x - boss.getX();
		double dz = point.z - boss.getZ();
		boss.setYRot((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F);
		boss.setYBodyRot(boss.getYRot());
		boss.setYHeadRot(boss.getYRot());
	}

	private static void dashThrough(VesperTheEveningStarEntity boss, double speed, boolean rightward) {
		Vec3 forward = committedDirection(boss);
		Vec3 lateral = new Vec3(-forward.z, 0.0D, forward.x).scale(rightward ? 0.32D : -0.32D);
		setSafeMovement(boss, forward.scale(speed).add(lateral).add(0.0D, 0.08D, 0.0D));
	}

	private static void moveToward(VesperTheEveningStarEntity boss, Vec3 point, double speed, double lift) {
		Vec3 direction = point.subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		if (direction.lengthSqr() <= 0.01D) return;
		Vec3 movement = direction.normalize().scale(speed).add(0.0D, lift, 0.0D);
		setSafeMovement(boss, movement);
	}

	private static void moveCommitted(VesperTheEveningStarEntity boss, double speed, double lift) {
		setSafeMovement(boss, committedDirection(boss).scale(speed).add(0.0D, lift, 0.0D));
	}

	private static Vec3 committedDirection(VesperTheEveningStarEntity boss) {
		Vec3 direction = boss.getLockedActionAim().subtract(boss.getLockedActionOrigin())
				.multiply(1.0D, 0.0D, 1.0D);
		if (direction.lengthSqr() < 0.01D) return Vec3.directionFromRotation(0.0F, boss.getYRot())
				.multiply(1.0D, 0.0D, 1.0D).normalize();
		return direction.normalize();
	}

	private static void setSafeMovement(VesperTheEveningStarEntity boss, Vec3 movement) {
		AABB box = boss.getBoundingBox();
		if (boss.level().noCollision(boss, box.move(movement))) {
			boss.setDeltaMovement(movement);
			return;
		}
		// A lip in the floor or a block-high step: vanilla movement climbs it, so only a real wall stops him. Refusing
		// these moves outright left him stalling and lurching on every bump of uneven ground.
		if (boss.onGround() && boss.level().noCollision(boss,
				box.move(movement.x, Math.max(0.0D, movement.y) + boss.maxUpStep(), movement.z))) {
			boss.setDeltaMovement(movement);
			return;
		}
		// Glancing off a wall: keep the part of the move that runs along it.
		Vec3 alongX = new Vec3(movement.x, movement.y, 0.0D);
		Vec3 alongZ = new Vec3(0.0D, movement.y, movement.z);
		Vec3 first = Math.abs(movement.x) >= Math.abs(movement.z) ? alongX : alongZ;
		Vec3 second = first == alongX ? alongZ : alongX;
		for (Vec3 slide : new Vec3[] { first, second }) {
			if (slide.horizontalDistanceSqr() > 1.0E-4D && boss.level().noCollision(boss, box.move(slide))) {
				boss.setDeltaMovement(slide);
				return;
			}
		}
		stopHorizontal(boss);
	}

	/**
	 * Footwork between strikes eases into a new heading over a few ticks instead of reversing in one, so a strafe
	 * that flips sides plants and pushes off rather than teleporting its velocity.
	 */
	private static void steer(VesperTheEveningStarEntity boss, Vec3 desired) {
		Vec3 realized = new Vec3(boss.getX() - boss.xo, 0.0D, boss.getZ() - boss.zo);
		if (realized.lengthSqr() > 1.0D) realized = Vec3.ZERO;
		Vec3 eased = realized.lerp(new Vec3(desired.x, 0.0D, desired.z), 0.4D);
		setSafeMovement(boss, new Vec3(eased.x, desired.y, eased.z));
	}

	private static void pull(VesperTheEveningStarEntity boss, LivingEntity target, double strength) {
		Vec3 direction = boss.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
		if (direction.lengthSqr() > 0.01D) target.push(direction.normalize().x * strength, 0.16D,
				direction.normalize().z * strength);
	}

	private static void pushAway(VesperTheEveningStarEntity boss, LivingEntity target, double strength) {
		Vec3 direction = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		if (direction.lengthSqr() > 0.01D) target.push(direction.normalize().x * strength, 0.16D,
				direction.normalize().z * strength);
	}

	private static void stopHorizontal(VesperTheEveningStarEntity boss) {
		boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
	}

	private static void pulse(VesperTheEveningStarEntity boss, EnumBloodTendency tendency, int count) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		ParticleColor color = VesperVisualEffects.tendencyColor(tendency);
		Vec3 center = boss.position().add(0.0D, 2.0D, 0.0D);
		VesperVisualEffects.glow(server, center, color, count, 1.15D, 1.6D, 1.15D, 0.025D);
		VesperVisualEffects.darkGlow(server, center, tendency == EnumBloodTendency.TENEBRIS
				? VesperVisualEffects.BLACK : color, Math.max(4, count / 2), 1.25D, 1.7D, 1.25D, 0.018D);
		VesperVisualEffects.bloodCells(server, center, color, Math.max(3, count / 3),
				0.95D, 1.35D, 0.95D, 0.035D);
	}

	private static void line(ServerLevel server, Vec3 from, Vec3 to, ParticleColor color) {
		VesperVisualEffects.telegraphLine(server, from, to, color);
	}

	private static void ring(ServerLevel server, Vec3 center, double radius, ParticleColor color) {
		VesperVisualEffects.telegraphRing(server, center, radius, color, 24);
	}
}
