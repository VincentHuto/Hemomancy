package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.client.particle.factory.AbsorbedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.VesperScuteProjectileEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.particle.CardinalRiteImpactPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

final class EndgameBossActions {
	private static final String VESPER_PUPPET_TAG = "HemomancyVesperPuppet";
	private static final int VESPER_PUPPET_MUSTER_SIZE = 3;
	private static final int VESPER_PUPPET_CAP = 6;
	private static final double VESPER_ATTACK_RANGE = 64.0D;
	private static final double MYCOPHANT_ATTACK_RANGE = 64.0D;

	private EndgameBossActions() {
	}

	static boolean tickVesperPhaseOneAttack(VesperTheCrownedRefusalEntity boss,
			VesperPhaseOneAttack attack, int attackTick) {
		LivingEntity target = acquireTarget(boss, VESPER_ATTACK_RANGE);
		if (attack == VesperPhaseOneAttack.CARAPACE_ANEURYSM) return tickCarapaceAneurysm(boss, attackTick);
		if (attack == VesperPhaseOneAttack.GRAB_IMPALEMENT) return tickGrabImpalement(boss, target, attackTick);
		if (target == null) return attackTick > 40;
		boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
		switch (attack) {
			case ROYAL_SCUTTLE -> {
				boss.getNavigation().stop();
				if (attackTick < 25) telegraphLine(boss, target, 0.85F, 0.02F, 0.02F);
				if (attackTick == 25) {
					Vec3 rush = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
					if (rush.lengthSqr() > 0.01D) boss.setDeltaMovement(rush.normalize().scale(1.65D).add(0.0D, 0.08D, 0.0D));
					boss.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.65F);
				}
				if (attackTick >= 25 && attackTick <= 42) hurtNearby(boss, 3.7D, 10.0F, 0.8D);
				return attackTick >= 62;
			}
			case PINCER_VICE -> {
				boss.getNavigation().stop();
				if (attackTick < 24) telegraphRing(boss, 4.2D, 0.8F, 0.02F, 0.02F);
				if (attackTick == 24 || attackTick == 38) hurtNearbyArc(boss, target, 5.2D, 110.0D, 12.0F);
				return attackTick >= 58;
			}
			case STINGER_SCRIPT -> {
				boss.getNavigation().stop();
				if (attackTick >= 16 && attackTick <= 52 && attackTick % 12 == 4) summonGripSpikes(boss, target, 3);
				return attackTick >= 68;
			}
			case BROOD_TRAMPLE -> {
				if (attackTick < 22) telegraphRing(boss, 5.5D, 0.65F, 0.0F, 0.0F);
				if (attackTick == 22) boss.setDeltaMovement(boss.getDeltaMovement().add(0.0D, 0.75D, 0.0D));
				if (attackTick == 38) {
					hurtNearby(boss, 6.0D, 13.0F, 1.15D);
					if (boss.level() instanceof ServerLevel server) {
						Vec3 impact = boss.position().add(0.0D, 0.2D, 0.0D);
						VesperVisualEffects.telegraphRing(server, impact, 5.5D, VesperVisualEffects.BLOOD, 48);
						VesperVisualEffects.bloodCells(server, impact, VesperVisualEffects.BLOOD,
								48, 4.2D, 0.25D, 4.2D, 0.11D);
						VesperVisualEffects.darkGlow(server, impact, VesperVisualEffects.BLACK,
								30, 4.5D, 0.35D, 4.5D, 0.07D);
					}
				}
				return attackTick >= 62;
			}
			case PUPPET_MUSTER -> {
				boss.getNavigation().stop();
				if (attackTick < 32) telegraphRing(boss, 2.5D, 0.5F, 0.0F, 0.0F);
				if (attackTick == 32) summonVesperPuppet(boss, target);
				return attackTick >= 68;
			}
			default -> { return true; }
		}
	}

	private static boolean tickCarapaceAneurysm(VesperTheCrownedRefusalEntity boss, int tick) {
		VesperMountAttackRules.AneurysmStage stage = VesperMountAttackRules.aneurysmStage(tick);
		boss.setCarapaceExposed(VesperMountAttackRules.isCarapaceExposed(tick));
		if (stage != VesperMountAttackRules.AneurysmStage.RECOVERY
				&& stage != VesperMountAttackRules.AneurysmStage.COMPLETE) {
			boss.getNavigation().stop();
			boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
		}
		if (stage == VesperMountAttackRules.AneurysmStage.BRACE) {
			telegraphRing(boss, 5.5D, 0.88F, 0.0F, 0.03F);
		}
		if (stage == VesperMountAttackRules.AneurysmStage.ERUPTION) eruptScutes(boss);
		if ((stage == VesperMountAttackRules.AneurysmStage.EXPOSED
				|| stage == VesperMountAttackRules.AneurysmStage.REFORM) && tick % 4 == 1
				&& boss.level() instanceof ServerLevel server) {
			Vec3 wound = boss.position().add(0.0D, boss.getBbHeight() * 0.42D, 0.0D);
			VesperVisualEffects.bloodCells(server, wound, VesperVisualEffects.BLOOD, 8, 1.7D, 1.0D, 2.2D, 0.025D);
			VesperVisualEffects.darkGlow(server, wound, VesperVisualEffects.BLACK, 6, 1.9D, 1.1D, 2.4D, 0.018D);
			if (tick % 12 == 1) {
				double angle = tick * 0.37D;
				Vec3 socket = wound.add(Math.cos(angle) * 1.8D, 0.25D * Math.sin(angle * 0.7D), Math.sin(angle) * 2.1D);
				VesperVisualEffects.voidTendril(server, socket, wound, boss.tickCount * 97L + tick);
			}
		}
		if (tick == 85) boss.playSound(SoundInit.ENTITY_VESPER_CARAPACE_REFORM.get(), 1.4F, 1.0F);
		return stage == VesperMountAttackRules.AneurysmStage.COMPLETE;
	}

	private static void eruptScutes(VesperTheCrownedRefusalEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 origin = boss.position().add(0.0D, boss.getBbHeight() * 0.46D, 0.0D);
		for (int ring = 0; ring < 2; ring++) {
			for (int i = 0; i < 12; i++) {
				double angle = Mth.TWO_PI * i / 12.0D + (ring == 1 ? Mth.TWO_PI / 24.0D : 0.0D);
				Vec3 direction = new Vec3(Math.cos(angle), ring == 1 ? 0.18D : -0.035D, Math.sin(angle)).normalize();
				VesperScuteProjectileEntity scute = new VesperScuteProjectileEntity(server, boss);
				scute.setPos(origin.x, origin.y + ring * 0.75D, origin.z);
				scute.shoot(direction.x, direction.y, direction.z, 1.25F, 0.0F);
				server.addFreshEntity(scute);
			}
		}
		VesperVisualEffects.bloodCells(server, origin, VesperVisualEffects.BLOOD, 44, 2.2D, 1.2D, 2.2D, 0.14D);
		VesperVisualEffects.darkGlow(server, origin, VesperVisualEffects.BLACK, 28, 2.5D, 1.4D, 2.5D, 0.09D);
		boss.playSound(SoundInit.ENTITY_VESPER_SCUTE_LAUNCH.get(), 1.8F, 1.0F);
	}

	private static boolean tickGrabImpalement(VesperTheCrownedRefusalEntity boss, LivingEntity target, int tick) {
		LivingEntity victim = boss.getRestrainedVictim();
		VesperMountAttackRules.GrabStage stage = VesperMountAttackRules.grabStage(tick, victim != null);
		if (stage != VesperMountAttackRules.GrabStage.RECOVERY
				&& stage != VesperMountAttackRules.GrabStage.COMPLETE) boss.getNavigation().stop();
		if (target != null && stage != VesperMountAttackRules.GrabStage.RECOVERY) {
			boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
		}
		if (stage == VesperMountAttackRules.GrabStage.TELEGRAPH) {
			if (target != null) telegraphLine(boss, target, 0.92F, 0.0F, 0.04F);
			if (tick == 1) boss.playSound(SoundInit.ENTITY_VESPER_GRAB_TELEGRAPH.get(), 1.2F, 1.0F);
		}
		if (tick == 15 && target != null) {
			Vec3 rush = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			if (rush.lengthSqr() > 0.01D) boss.setDeltaMovement(rush.normalize().scale(1.15D));
		}
		if (stage == VesperMountAttackRules.GrabStage.LUNGE && victim == null && target != null) boss.tryRestrain(target);
		victim = boss.getRestrainedVictim();
		if (victim != null && tick >= 21 && tick <= 42) boss.tickRestrainedVictim(tick);
		if (stage == VesperMountAttackRules.GrabStage.BITE) {
			boss.applyGrabBite();
			if (boss.level() instanceof ServerLevel server) {
				Vec3 feet = victim.position().add(0.0D, 0.15D, 0.0D);
				VesperVisualEffects.bloodCells(server, feet, VesperVisualEffects.BLOOD, 18, 0.5D, 0.25D, 0.5D, 0.08D);
				PacketHandler.sendClawSlash(feet, boss.getLookAngle(), VesperVisualEffects.BLOOD, false, 0.8F, 48.0D, server);
			}
			boss.playSound(SoundInit.ENTITY_VESPER_GRAB_BITE.get(), 1.3F, 1.0F);
		}
		if (stage == VesperMountAttackRules.GrabStage.TAIL_WINDUP && victim != null && tick % 3 == 1
				&& boss.level() instanceof ServerLevel server) {
			Vec3 tip = boss.position().add(Vec3.directionFromRotation(0.0F, boss.getYRot()).scale(-2.0D)).add(0.0D, 3.7D, 0.0D);
			VesperVisualEffects.darkGlow(server, tip, VesperVisualEffects.BLACK, 5, 0.24D, 0.24D, 0.24D, 0.02D);
		}
		if (stage == VesperMountAttackRules.GrabStage.IMPALE) {
			boss.applyGrabImpale();
			if (boss.level() instanceof ServerLevel server) {
				Vec3 tip = boss.position().add(Vec3.directionFromRotation(0.0F, boss.getYRot()).scale(-1.8D)).add(0.0D, 3.8D, 0.0D);
				Vec3 impact = victim.position().add(0.0D, victim.getBbHeight() * 0.48D, 0.0D);
				VesperVisualEffects.voidTendril(server, tip, impact, boss.tickCount * 131L);
				VesperVisualEffects.lightning(server, tip, impact, false, boss.tickCount * 173L);
				VesperVisualEffects.bloodCells(server, impact, VesperVisualEffects.BLOOD, 24, 0.45D, 0.7D, 0.45D, 0.1D);
			}
			boss.playSound(SoundInit.ENTITY_VESPER_GRAB_PIERCE.get(), 1.5F, 1.0F);
		}
		if (tick == 43 && victim != null) {
			boss.releaseRestrainedVictim(true);
			boss.playSound(SoundInit.ENTITY_VESPER_GRAB_RELEASE.get(), 1.2F, 1.0F);
		}
		return tick >= 70;
	}

	static void tickExposedThroneAnchor(VesperTheCrownedRefusalEntity boss, int anchor, float damage) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		if (boss.tickCount % 3 == 0) {
			PartEntity<?> part = boss.getParts()[anchor];
			Vec3 wound = exposedAnchorCenter(boss);
			VesperVisualEffects.bloodCells(server, wound, VesperVisualEffects.BLOOD,
					5, 0.32D, 0.42D, 0.32D, 0.025D);
			VesperVisualEffects.darkGlow(server, wound, VesperVisualEffects.BLACK,
					4, 0.38D, 0.5D, 0.38D, 0.018D);
			outlineExposedThroneAnchor(server, boss, part, damage);
		}
	}

	private static void outlineExposedThroneAnchor(ServerLevel server, VesperTheCrownedRefusalEntity boss,
			PartEntity<?> part, float damage) {
		double halfWidth = part.getBbWidth() * 0.5D;
		ParticleColor woundColor = damage >= VesperCombatRules.ANCHOR_MAX_DAMAGE * 0.65F
				? VesperVisualEffects.BLOOD : VesperVisualEffects.DEEP_BLOOD;
		for (int level = 0; level < 3; level++) {
			Vec3 ringCenter = exposedAnchorCenter(boss).add(0.0D,
					(level - 1) * part.getBbHeight() * 0.38D, 0.0D);
			VesperVisualEffects.telegraphRing(server, ringCenter, halfWidth, woundColor, 12);
		}
		if (boss.tickCount % 12 == 0) {
			Vec3 center = exposedAnchorCenter(boss);
			double angle = (boss.tickCount + part.getId() * 19L) * 0.21D;
			Vec3 edge = center.add(Math.cos(angle) * halfWidth, part.getBbHeight() * 0.35D,
					Math.sin(angle) * halfWidth);
			VesperVisualEffects.lightning(server, edge, center, false, boss.tickCount * 31L + part.getId());
		}
	}

	static void hitThroneAnchor(VesperTheCrownedRefusalEntity boss, int anchor) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 center = exposedAnchorCenter(boss);
		VesperVisualEffects.bloodCells(server, center, VesperVisualEffects.BLOOD,
				12, 0.42D, 0.46D, 0.42D, 0.065D);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				9, 0.48D, 0.5D, 0.48D, 0.035D);
		Vec3 throne = boss.position().add(0.0D, 2.15D, 0.0D);
		VesperVisualEffects.lightning(server, center, throne, false, boss.tickCount * 71L + anchor * 13L);
		server.playSound(null, center.x, center.y, center.z, SoundEvents.IRON_GOLEM_DAMAGE,
				SoundSource.HOSTILE, 0.8F, 1.25F);
		server.playSound(null, center.x, center.y, center.z, SoundInit.ENTITY_VESPER_HIT.get(),
				SoundSource.HOSTILE, 0.7F, 1.15F);
	}

	static void breakThroneAnchor(VesperTheCrownedRefusalEntity boss, int anchor) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		PartEntity<?> part = boss.getParts()[anchor];
		Vec3 center = exposedAnchorCenter(boss);
		VesperVisualEffects.bloodCells(server, center, VesperVisualEffects.BLOOD,
				42, 0.85D, 0.85D, 0.85D, 0.11D);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				28, 1.0D, 0.9D, 1.0D, 0.07D);
		Vec3 bossCenter = boss.position().add(0.0D, boss.getBbHeight() * 0.62D, 0.0D);
		VesperVisualEffects.voidTendril(server, center, bossCenter, boss.tickCount * 37L + anchor);
		VesperVisualEffects.lightning(server, center, bossCenter, false, boss.tickCount * 53L + anchor);
		server.playSound(null, part.blockPosition(), SoundEvents.ANVIL_DESTROY, SoundSource.HOSTILE, 1.5F, 0.65F);
	}

	private static Vec3 exposedAnchorCenter(VesperTheCrownedRefusalEntity boss) {
		VesperCombatRules.AnchorCenter center = VesperCombatRules.anchorCenter(
				boss.getX(), boss.getY(), boss.getZ(), boss.getYRot());
		return new Vec3(center.x(), center.y(), center.z());
	}

	static void tickVesperTransformation(VesperTheCrownedRefusalEntity boss, int tick) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		float absorption = VesperPhaseTransitionRules.absorptionProgress(tick);
		Vec3 center = vesperCocoonCenter(boss);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				tick % 4 == 0 ? 4 : 2, 1.35D, 1.65D, 1.35D, 0.025D);
		if (absorption > 0.0F && absorption < 1.0F) {
			server.sendParticles(AbsorbedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
					center.x, center.y, center.z, 5,
					1.5D * (1.0D - absorption * 0.55D), 1.35D,
					1.5D * (1.0D - absorption * 0.55D), 0.055D);
		}
		if (tick >= VesperPhaseTransitionRules.COCOON_START_TICK
				&& tick < VesperPhaseTransitionRules.COCOON_BEAM_START_TICK
				&& (tick - VesperPhaseTransitionRules.COCOON_START_TICK) % 16 == 0) {
			int cocoonTick = tick - VesperPhaseTransitionRules.COCOON_START_TICK;
			for (int index = 0; index < 2; index++) {
				double angle = cocoonTick * 0.071D + index * Math.PI;
				Vec3 root = center.add(Math.cos(angle) * 1.45D, -1.55D,
						Math.sin(angle) * 1.45D);
				Vec3 crown = center.add(Math.cos(angle + 1.3D) * 0.25D, 1.75D,
						Math.sin(angle + 1.3D) * 0.25D);
				VesperVisualEffects.voidTendril(server, root, crown, cocoonTick * 101L + index);
			}
		}
		if (tick >= VesperPhaseTransitionRules.COCOON_BEAM_START_TICK
				&& tick < VesperPhaseTransitionRules.COCOON_BURST_START_TICK) {
			float beam = VesperPhaseTransitionRules.cocoonBeamProgress(tick);
			if (tick % 2 == 0) {
				VesperVisualEffects.glow(server, center, VesperVisualEffects.DEEP_BLOOD,
						Math.max(2, Math.round(5.0F * beam)), 0.35D, 0.55D, 0.35D, 0.018D);
			}
			if (tick % 14 == 0) {
				double angle = tick * 0.19D;
				Vec3 first = center.add(Math.cos(angle) * 1.35D, 1.1D,
						Math.sin(angle) * 1.35D);
				Vec3 second = center.add(-Math.cos(angle) * 1.35D, -1.1D,
						-Math.sin(angle) * 1.35D);
				VesperVisualEffects.lightning(server, first, second, false, tick * 131L);
			}
			if (tick % 20 == 0) {
				server.playSound(null, BlockPos.containing(center), SoundEvents.WITHER_AMBIENT,
						SoundSource.HOSTILE, 1.0F, 0.45F + beam * 0.18F);
			}
		}
		if (tick == VesperPhaseTransitionRules.DISMOUNT_TICKS
				+ VesperPhaseTransitionRules.MOUNT_ABSORPTION_TICKS) finishVesperMountAbsorption(boss);
		if (tick == VesperPhaseTransitionRules.COCOON_BURST_START_TICK) blastVesperCocoon(boss, center);
	}

	static void finishVesperMountAbsorption(VesperTheCrownedRefusalEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 center = vesperCocoonCenter(boss);
		VesperVisualEffects.bloodCells(server, center, VesperVisualEffects.BLOOD,
				28, 0.85D, 1.25D, 0.85D, 0.065D);
		server.playSound(null, BlockPos.containing(center), SoundEvents.RESPAWN_ANCHOR_CHARGE,
				SoundSource.HOSTILE, 1.1F, 0.52F);
	}

	private static void blastVesperCocoon(VesperTheCrownedRefusalEntity boss, Vec3 center) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		VesperVisualEffects.bloodCells(server, center, VesperVisualEffects.BLOOD,
				96, 1.2D, 1.65D, 1.2D, 0.22D);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				72, 1.4D, 1.8D, 1.4D, 0.16D);
		for (int index = 0; index < 8; index++) {
			double angle = Mth.TWO_PI * index / 8.0D;
			Vec3 destination = center.add(Math.cos(angle) * 5.2D,
					-0.7D + (index % 3) * 0.8D, Math.sin(angle) * 5.2D);
			VesperVisualEffects.voidTendril(server, center, destination,
					boss.getId() * 401L + index);
			if ((index & 1) == 0) {
				VesperVisualEffects.lightning(server, center, destination, false,
						boss.getId() * 433L + index);
			}
		}
		PacketDistributor.sendToPlayersNear(server, null, center.x, center.y, center.z, 40.0D,
				new CardinalRiteImpactPacket(10, 0.10F, boss.getId() * 31 + boss.tickCount));
		server.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(),
				SoundSource.HOSTILE, 1.7F, 0.48F);
		server.playSound(null, BlockPos.containing(center), SoundEvents.WITHER_SPAWN,
				SoundSource.HOSTILE, 1.2F, 0.72F);
	}

	static void finishVesperCocoonReveal(VesperTheCrownedRefusalEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 center = vesperCocoonCenter(boss);
		VesperVisualEffects.glow(server, center, VesperVisualEffects.BLOOD,
				48, 1.0D, 1.45D, 1.0D, 0.11D);
		server.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE,
				SoundSource.HOSTILE, 1.35F, 0.68F);
	}

	private static Vec3 vesperCocoonCenter(VesperTheCrownedRefusalEntity boss) {
		Vec3 forward = Vec3.directionFromRotation(0.0F, boss.getYRot()).multiply(1.5D, 0.0D, 1.5D);
		return boss.position().add(forward).add(0.0D, 1.58D, 0.0D);
	}

	static void tickVesperAwakening(VesperTheEveningStarEntity boss, int tick) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 center = boss.position().add(0.0D, boss.getBbHeight() * 0.55D, 0.0D);
		float glow = VesperPhaseTransitionRules.awakeningGlow(tick);
		if (glow > 0.0F) {
			VesperVisualEffects.glow(server, center, VesperVisualEffects.BLOOD,
					Math.max(3, Math.round(10.0F * glow)), 0.7D, 1.1D, 0.7D, 0.035D);
			VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.DEEP_BLOOD,
					Math.max(2, Math.round(6.0F * glow)), 0.9D, 1.25D, 0.9D, 0.025D);
		}
		if (tick >= VesperPhaseTransitionRules.AWAKENING_SIGIL_START_TICK
				&& (tick - VesperPhaseTransitionRules.AWAKENING_SIGIL_START_TICK)
						% VesperPhaseTransitionRules.AWAKENING_SIGIL_INTERVAL_TICKS == 0) {
			server.playSound(null, boss.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
					SoundSource.HOSTILE, 0.9F, 0.68F + tick * 0.006F);
			VesperVisualEffects.bloodCells(server, center, VesperVisualEffects.BLOOD,
					12, 0.75D, 1.0D, 0.75D, 0.045D);
		}
	}

	static void tickVesperDefeat(VesperTheEveningStarEntity boss, int tick) {
		if (!(boss.level() instanceof ServerLevel server)
				|| tick > VesperCombatRules.WEAPON_DISSOLVE_TICKS || tick % 2 != 0) return;
		double yaw = Math.toRadians(boss.getYRot());
		Vec3 side = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw)).scale(0.72D);
		Vec3 hands = boss.position().add(0.0D, 1.25D, 0.0D);
		for (Vec3 hand : new Vec3[] { hands.add(side), hands.subtract(side) }) {
			VesperVisualEffects.bloodCells(server, hand, VesperVisualEffects.BLOOD,
					3, 0.2D, 0.22D, 0.2D, 0.025D);
			VesperVisualEffects.darkGlow(server, hand, VesperVisualEffects.BLACK,
					2, 0.16D, 0.18D, 0.16D, 0.018D);
		}
	}

	static void tickVesperHoodRemoval(VesperTheEveningStarEntity boss, int tick) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 head = boss.position().add(0.0D, boss.getBbHeight() * 0.82D, 0.0D);
		if (tick % 3 == 0 && tick <= VesperEveningStarPresentationRules.HOOD_REMOVAL_TICKS) {
			VesperVisualEffects.bloodCells(server, head, VesperVisualEffects.DEEP_BLOOD,
					4, 0.46D, 0.34D, 0.46D, 0.035D);
			VesperVisualEffects.darkGlow(server, head, VesperVisualEffects.BLACK,
					2, 0.38D, 0.28D, 0.38D, 0.018D);
		}
	}

	static void tickVesperBloodAbsorption(VesperTheEveningStarEntity boss, float progress, int collapseTick) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		boolean finalCollapse = VesperEveningStarPresentationRules.isFinalCollapseComplete(collapseTick);
		if (boss.tickCount % 2 != 0 && !finalCollapse) return;
		float normalized = VesperEveningStarPresentationRules.absorptionProgress(progress);
		Vec3 center = boss.position().add(0.0D, 0.72D * (1.0D - normalized * 0.55D), 0.0D);
		server.sendParticles(AbsorbedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
				center.x, center.y, center.z, 5,
				0.9D * (1.0D - normalized * 0.72D), 0.65D, 0.9D * (1.0D - normalized * 0.72D), 0.045D);
		if (boss.tickCount % 10 == 0) {
			double angle = boss.tickCount * 0.19D;
			Vec3 source = center.add(Math.cos(angle) * (1.35D - normalized),
					0.55D - normalized * 0.4D, Math.sin(angle) * (1.35D - normalized));
			VesperVisualEffects.voidTendril(server, source, center, boss.tickCount * 157L);
		}
		if (finalCollapse) {
			VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
					18, 0.18D, 0.18D, 0.18D, 0.065D);
		}
	}

	static void finishVesperAwakening(VesperTheEveningStarEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 center = boss.position().add(0.0D, boss.getBbHeight() * 0.55D, 0.0D);
		VesperVisualEffects.glow(server, center, VesperVisualEffects.BLOOD,
				48, 1.1D, 1.7D, 1.1D, 0.11D);
		VesperVisualEffects.lightning(server, center.add(-1.8D, 1.0D, 0.0D),
				center.add(1.8D, -0.7D, 0.0D), false, boss.getId() * 433L);
		server.playSound(null, boss.blockPosition(), SoundEvents.WITHER_SPAWN,
				SoundSource.HOSTILE, 1.2F, 1.35F);
	}

	static void clearVesperPuppets(Mob boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		server.getEntitiesOfClass(Mob.class, boss.getBoundingBox().inflate(72.0D),
				mob -> mob.getTags().contains(VESPER_PUPPET_TAG)).forEach(Entity::discard);
	}

	static void tickMycophantPattern(MycophantEntity boss) {
		LivingEntity target = acquireTarget(boss, MYCOPHANT_ATTACK_RANGE);
		if (target == null) {
			return;
		}

		chaseTarget(boss, target, 1.0D, 42.0D);
		float regen = EndgameBossCombatRules.regenerationPerTick(boss.getHealth(), boss.getMaxHealth());
		if (regen > 0.0F) {
			boss.heal(regen);
		}

		if (EndgameBossCombatRules.shouldTrigger(boss.tickCount, boss.getId() % 11, 100,
				boss.getHealth(), boss.getMaxHealth())) {
			placeCrimsonFlames(boss, target);
		}
		if (EndgameBossCombatRules.shouldTrigger(boss.tickCount, boss.getId() % 13, 120,
				boss.getHealth(), boss.getMaxHealth())) {
			massBlind(boss, target);
		}
		if (EndgameBossCombatRules.shouldTrigger(boss.tickCount, boss.getId() % 17, 170,
				boss.getHealth(), boss.getMaxHealth())) {
			summonFungalAid(boss, target, 3);
		}
		if (EndgameBossCombatRules.shouldTrigger(boss.tickCount, boss.getId() % 5, 70,
				boss.getHealth(), boss.getMaxHealth())) {
			repelNearbyTargets(boss);
		}
	}

	static void tickVesperClientParticles(Monster boss) {
		if (!boss.level().isClientSide) {
			return;
		}
		RandomSource random = boss.level().getRandom();
		if (boss instanceof VesperTheCrownedRefusalEntity crowned
				&& VesperPhaseTransitionRules.isAbsorbing(crowned.getTransitionTick())) {
			Vec3 target = crowned.position().add(0.0D, crowned.getBbHeight() * 0.68D, 0.0D);
			float progress = VesperPhaseTransitionRules.absorptionProgress(crowned.getTransitionTick());
			for (int i = 0; i < 5; i++) {
				double radius = crowned.getBbWidth() * (0.48D - progress * 0.22D);
				double angle = random.nextDouble() * Mth.TWO_PI;
				Vec3 source = crowned.position().add(Mth.cos((float) angle) * radius,
						0.3D + random.nextDouble() * crowned.getBbHeight() * 0.48D,
						Mth.sin((float) angle) * radius);
				Vec3 sourceOffset = source.subtract(target);
				boss.level().addParticle(AbsorbedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
						target.x, target.y, target.z, sourceOffset.x, sourceOffset.y, sourceOffset.z);
			}
			return;
		}
		if (boss instanceof VesperTheEveningStarEntity evening && evening.isAwaitingAbsorption()) {
			float progress = VesperEveningStarPresentationRules.absorptionProgress(
					evening.getDefeatAbsorptionProgress());
			if (progress <= 0.0F) return;
			Vec3 target = evening.position().add(0.0D, 0.75D * (1.0D - progress * 0.55D), 0.0D);
			for (int i = 0; i < 4; i++) {
				double radius = 1.15D * (1.0D - progress * 0.7D);
				double angle = random.nextDouble() * Mth.TWO_PI;
				Vec3 source = target.add(Mth.cos((float) angle) * radius,
						(random.nextDouble() - 0.5D) * 1.2D, Mth.sin((float) angle) * radius);
				Vec3 sourceOffset = source.subtract(target);
				boss.level().addParticle(AbsorbedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
						target.x, target.y, target.z, sourceOffset.x, sourceOffset.y, sourceOffset.z);
			}
			return;
		}
		EnumBloodTendency tendency = boss instanceof VesperTheEveningStarEntity evening
				? evening.getActiveTendency() : EnumBloodTendency.ANIMUS;
		VesperVisualEffects.ambient(boss.level(), boss.position(), boss.getBbWidth(), boss.getBbHeight(),
				boss instanceof VesperTheCrownedRefusalEntity, tendency);
	}

	static void tickMycophantClientParticles(MycophantEntity boss) {
		if (!boss.level().isClientSide) {
			return;
		}
		RandomSource random = boss.level().getRandom();
		for (int i = 0; i < 3; i++) {
			double x = boss.getX() + (random.nextDouble() - 0.5D) * boss.getBbWidth();
			double y = boss.getY() + random.nextDouble() * boss.getBbHeight();
			double z = boss.getZ() + (random.nextDouble() - 0.5D) * boss.getBbWidth();
			boss.level().addParticle(i == 0 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.ASH,
					x, y, z, 0.0D, 0.01D, 0.0D);
		}
	}

	static void tickVesperDeathParticles(Monster boss, int deathTicks) {
		if (!(boss.level() instanceof ServerLevel server)) {
			return;
		}
		int count = deathTicks % 20 == 0 ? 80 : 12;
		Vec3 center = boss.position().add(0.0D, boss.getBbHeight() * 0.55D, 0.0D);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				count, 1.5D, 1.15D, 1.5D, 0.075D);
		VesperVisualEffects.bloodCells(server, center, VesperVisualEffects.BLOOD,
				Math.max(4, count / 2), 1.25D, 0.9D, 1.25D, 0.055D);
		VesperVisualEffects.embers(server, center, VesperVisualEffects.EMBER,
				Math.max(3, count / 3), 1.35D, 1.0D, 1.35D, 0.045D, 0.3F, 28);
		if (deathTicks % 40 == 0) {
			VesperVisualEffects.lightning(server, center.add(-1.4D, 1.2D, 0.0D),
					center.add(1.4D, -0.8D, 0.0D), false, deathTicks * 191L + boss.getId());
			server.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
					SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 0.8F, 1.45F);
		}
	}

	static void tickMycophantDeathParticles(MycophantEntity boss, int deathTicks) {
		if (!(boss.level() instanceof ServerLevel server)) {
			return;
		}
		int count = deathTicks % 20 == 0 ? 90 : 14;
		sendParticles(server, ParticleTypes.SOUL_FIRE_FLAME, boss, count / 2, 1.6D, 1.0D, 1.6D, 0.06D);
		sendParticles(server, ParticleTypes.SMOKE, boss, count, 1.8D, 1.2D, 1.8D, 0.05D);
		sendParticles(server, ParticleTypes.DRIPPING_OBSIDIAN_TEAR, boss, 8, 1.3D, 1.1D, 1.3D, 0.0D);
		if (deathTicks % 35 == 0) {
			server.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
					SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.2F, 0.65F);
		}
	}

	static void finishWithExplosion(Mob boss, float radius) {
		if (!boss.level().isClientSide) {
			boss.level().explode(boss, boss.getX(), boss.getY() + boss.getBbHeight() * 0.5D, boss.getZ(),
					radius, ExplosionInteraction.NONE);
		}
	}

	static boolean disableShieldOnHit(Mob boss, Entity target, int ticks) {
		if (!(target instanceof Player player) || !player.isUsingItem()) {
			return false;
		}
		if (!player.getUseItem().is(Items.SHIELD)) {
			return false;
		}
		player.getCooldowns().addCooldown(Items.SHIELD, ticks);
		player.stopUsingItem();
		boss.level().broadcastEntityEvent(player, (byte) 9);
		return true;
	}

	private static LivingEntity acquireTarget(Mob boss, double range) {
		LivingEntity target = boss.getTarget();
		if (target != null && target.isAlive() && boss.distanceToSqr(target) <= range * range) {
			return target;
		}
		Player nearest = boss.level().getNearestPlayer(boss, range);
		if (nearest != null && nearest.isAlive()) {
			boss.setTarget(nearest);
			return nearest;
		}
		return null;
	}

	private static void chaseTarget(Mob boss, LivingEntity target, double speed, double closeRangeSqr) {
		boss.getLookControl().setLookAt(target, boss.getMaxHeadYRot(), boss.getMaxHeadXRot());
		if (boss.getNavigation().isDone()) {
			boss.getNavigation().moveTo(target, speed);
		}
		if (boss.distanceToSqr(target) < closeRangeSqr) {
			double dx = target.getX() - boss.getX();
			double dz = target.getZ() - boss.getZ();
			boss.setYRot((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F);
			boss.setYBodyRot(boss.getYRot());
		}
	}

	private static void summonGripSpikes(Monster boss, LivingEntity target, int count) {
		if (!(boss.level() instanceof ServerLevel server)) {
			return;
		}
		RandomSource random = server.getRandom();
		for (int i = 0; i < count; i++) {
			double angle = random.nextDouble() * Mth.TWO_PI;
			double radius = 1.5D + random.nextDouble() * 5.0D;
			BlockPos base = BlockPos.containing(target.getX() + Mth.cos((float) angle) * radius,
					target.getY(), target.getZ() + Mth.sin((float) angle) * radius);
			BlockPos pos = findGround(server, base);
			EntityIronSpike spike = new EntityIronSpike(EntityInit.iron_spike.get(), server, boss);
			spike.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
					random.nextFloat() * 360.0F, 0.0F);
			server.addFreshEntity(spike);
		}
		server.playSound(null, target.getX(), target.getY(), target.getZ(),
				SoundEvents.IRON_GOLEM_REPAIR, SoundSource.HOSTILE, 1.0F, 0.5F);
	}

	private static void summonVesperPuppet(VesperTheCrownedRefusalEntity boss, LivingEntity target) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		long active = server.getEntitiesOfClass(Mob.class, boss.getBoundingBox().inflate(64.0D),
				mob -> mob.getTags().contains(VESPER_PUPPET_TAG)
						&& mob.getPersistentData().hasUUID(VesperEncounterPuppetEvents.BOSS_KEY)
						&& boss.getUUID().equals(mob.getPersistentData().getUUID(VesperEncounterPuppetEvents.BOSS_KEY)))
				.stream().count();
		int count = Math.min(VESPER_PUPPET_MUSTER_SIZE, Math.max(0, VESPER_PUPPET_CAP - (int) active));
		for (int i = 0; i < count; i++) {
			int kind = Math.floorMod(boss.tickCount / 100 + i, 4);
			Mob puppet = switch (kind) {
				case 0 -> EntityInit.gorebound_hulk.get().create(server);
				case 1 -> EntityInit.marrow_spitter.get().create(server);
				case 2 -> EntityInit.veinwing_vulture.get().create(server);
				default -> EntityInit.mnemonist_puppet.get().create(server);
			};
			if (!(puppet instanceof BoundPuppeteerSummon bound)) continue;
			BlockPos pos = randomNearbyGround(server, boss.blockPosition(), 6);
			puppet.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, boss.getYRot(), 0.0F);
			bound.hemomancy$setTrialSummon(true);
			bound.hemomancy$setTrialCasterUUID(boss.getOrdealOwner());
			bound.hemomancy$setOwnerUUID(boss.getUUID());
			puppet.addTag(VESPER_PUPPET_TAG);
			puppet.getPersistentData().putBoolean(VesperEncounterPuppetEvents.PUPPET_KEY, true);
			puppet.getPersistentData().putUUID(VesperEncounterPuppetEvents.BOSS_KEY, boss.getUUID());
			puppet.setTarget(target);
			puppet.setPersistenceRequired();
			server.addFreshEntity(puppet);
			Vec3 puppetCenter = puppet.position().add(0.0D, puppet.getBbHeight() * 0.5D, 0.0D);
			Vec3 throneCenter = boss.position().add(0.0D, boss.getBbHeight() * 0.62D, 0.0D);
			VesperVisualEffects.darkGlow(server, puppetCenter, VesperVisualEffects.BLACK,
					18, 0.8D, 0.8D, 0.8D, 0.045D);
			VesperVisualEffects.bloodCells(server, puppetCenter, VesperVisualEffects.BLOOD,
					12, 0.65D, 0.7D, 0.65D, 0.06D);
			VesperVisualEffects.voidTendril(server, throneCenter, puppetCenter,
					boss.tickCount * 211L + puppet.getId());
			server.playSound(null, pos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.9F,
					1.25F + i * 0.08F);
		}
	}

	private static void telegraphLine(Mob boss, LivingEntity target, float red, float green, float blue) {
		if (!(boss.level() instanceof ServerLevel server) || boss.tickCount % 2 != 0) return;
		Vec3 start = boss.position().add(0.0D, 0.1D, 0.0D);
		VesperVisualEffects.telegraphLine(server, start, target.position(),
				new ParticleColor((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F)));
	}

	private static void telegraphRing(Mob boss, double radius, float red, float green, float blue) {
		if (!(boss.level() instanceof ServerLevel server) || boss.tickCount % 2 != 0) return;
		VesperVisualEffects.telegraphRing(server, boss.position(), radius,
				new ParticleColor((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F)), 28);
	}

	private static void hurtNearby(Mob boss, double radius, float damage, double push) {
		AABB area = boss.getBoundingBox().inflate(radius, 2.0D, radius);
		for (LivingEntity living : boss.level().getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity.isAlive() && entity != boss && !(entity instanceof BoundPuppeteerSummon))) {
			if (!living.hurt(boss.damageSources().mobAttack(boss), damage)) continue;
			Vec3 away = living.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			if (away.lengthSqr() > 0.01D) {
				away = away.normalize().scale(push);
				living.push(away.x, 0.28D, away.z);
			}
		}
	}

	private static void hurtNearbyArc(Mob boss, LivingEntity target, double radius, double degrees, float damage) {
		Vec3 facing = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		if (facing.lengthSqr() < 0.01D) facing = boss.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
		facing = facing.normalize();
		double minimumDot = Math.cos(Math.toRadians(degrees * 0.5D));
		for (LivingEntity living : boss.level().getEntitiesOfClass(LivingEntity.class,
				boss.getBoundingBox().inflate(radius), entity -> entity.isAlive() && entity != boss
						&& !(entity instanceof BoundPuppeteerSummon))) {
			Vec3 toward = living.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			if (toward.lengthSqr() > 0.01D && facing.dot(toward.normalize()) >= minimumDot) {
				living.hurt(boss.damageSources().mobAttack(boss), damage);
			}
		}
	}

	private static void placeCrimsonFlames(MycophantEntity boss, LivingEntity target) {
		if (!(boss.level() instanceof ServerLevel server)) {
			return;
		}
		BlockState flame = BlockInit.crimson_flames.get().defaultBlockState();
		BlockPos center = target.blockPosition();
		BlockPos[] positions = new BlockPos[] {
				center,
				center.north(),
				center.south(),
				center.east(),
				center.west(),
				center.north(2),
				center.south(2),
				center.east(2),
				center.west(2)
		};
		for (BlockPos candidate : positions) {
			BlockPos pos = findGround(server, candidate);
			if (server.isEmptyBlock(pos) && server.getBlockState(pos.below()).isFaceSturdy(server, pos.below(), Direction.UP)) {
				server.setBlockAndUpdate(pos, flame);
			}
		}
		server.playSound(null, target.getX(), target.getY(), target.getZ(),
				SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.3F, 0.75F);
	}

	private static void massBlind(MycophantEntity boss, LivingEntity target) {
		if (!(boss.level() instanceof ServerLevel server)) {
			return;
		}
		AABB area = target.getBoundingBox().inflate(7.0D);
		List<LivingEntity> targets = server.getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity.isAlive() && entity != boss && !(entity instanceof FunglingEntity));
		for (LivingEntity living : targets) {
			living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 90, 0, false, true));
			living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, false, true));
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, true));
		}
		server.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
				SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), SoundSource.HOSTILE, 1.0F, 0.8F);
		sendParticles(server, ParticleTypes.CRIMSON_SPORE, boss, 70, 2.0D, 1.0D, 2.0D, 0.04D);
	}

	private static void summonFungalAid(MycophantEntity boss, LivingEntity target, int count) {
		if (!(boss.level() instanceof ServerLevel server)) {
			return;
		}
		RandomSource random = server.getRandom();
		for (int i = 0; i < count; i++) {
			FunglingEntity fungling = EntityInit.fungling.get().create(server);
			if (fungling == null) {
				continue;
			}
			BlockPos pos = randomNearbyGround(server, boss.blockPosition(), 7);
			fungling.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
					random.nextFloat() * 360.0F, 0.0F);
			fungling.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null);
			fungling.setTarget(target);
			server.addFreshEntity(fungling);
		}
		server.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
				SoundInit.ENTITY_MYCOPHANT_SUMMON.get(), SoundSource.HOSTILE, 1.3F, 0.85F);
	}

	private static void repelNearbyTargets(MycophantEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) {
			return;
		}
		AABB area = boss.getBoundingBox().inflate(4.5D);
		List<LivingEntity> targets = server.getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity.isAlive() && entity != boss && !(entity instanceof FunglingEntity));
		for (LivingEntity living : targets) {
			Vec3 push = living.position().subtract(boss.position());
			if (push.horizontalDistanceSqr() < 0.01D) {
				push = new Vec3(server.getRandom().nextDouble() - 0.5D, 0.0D, server.getRandom().nextDouble() - 0.5D);
			}
			Vec3 impulse = new Vec3(push.x, 0.0D, push.z).normalize().scale(0.9D);
			living.push(impulse.x, 0.32D, impulse.z);
			living.hurt(boss.damageSources().mobAttack(boss), 5.0F);
		}
		if (!targets.isEmpty()) {
			server.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
					SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
		}
	}

	private static BlockPos randomNearbyGround(ServerLevel server, BlockPos center, int radius) {
		RandomSource random = server.getRandom();
		BlockPos candidate = center.offset(random.nextInt(radius * 2 + 1) - radius, 0,
				random.nextInt(radius * 2 + 1) - radius);
		return findGround(server, candidate);
	}

	private static BlockPos findGround(Level level, BlockPos origin) {
		BlockPos.MutableBlockPos mutable = origin.mutable();
		for (int dy = 4; dy >= -5; dy--) {
			mutable.set(origin.getX(), origin.getY() + dy, origin.getZ());
			if (level.isEmptyBlock(mutable) && level.isEmptyBlock(mutable.above())
					&& !level.getBlockState(mutable.below()).getCollisionShape(level, mutable.below()).isEmpty()) {
				return mutable.immutable();
			}
		}
		return origin;
	}

	private static void sendParticles(ServerLevel server, ParticleOptions particles, Mob boss, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		server.sendParticles(particles,
				boss.getX(), boss.getY() + boss.getBbHeight() * 0.55D, boss.getZ(),
				count, xSpread, ySpread, zSpread, speed);
	}
}
