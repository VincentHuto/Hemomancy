package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
	private static final double MYCOPHANT_ATTACK_RANGE = 64.0D;

	private EndgameBossActions() {
	}

	static void tickExposedThroneAnchor(VesperTheCrownedRefusalEntity boss, int anchor, float damage) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		if (boss.tickCount % 3 == 0) {
			PartEntity<?> part = boss.getParts()[anchor];
			Vec3 wound = exposedAnchorCenter(boss, anchor);
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
		ParticleColor woundColor = damage >= VesperWoundRules.DRAIN_REQUIRED * 0.65F
				? VesperVisualEffects.BLOOD : VesperVisualEffects.DEEP_BLOOD;
		for (int level = 0; level < 3; level++) {
			Vec3 ringCenter = boss.woundCenter().add(0.0D,
					(level - 1) * part.getBbHeight() * 0.38D, 0.0D);
			VesperVisualEffects.telegraphRing(server, ringCenter, halfWidth, woundColor, 12);
		}
		if (boss.tickCount % 12 == 0) {
			Vec3 center = boss.woundCenter();
			double angle = (boss.tickCount + part.getId() * 19L) * 0.21D;
			Vec3 edge = center.add(Math.cos(angle) * halfWidth, part.getBbHeight() * 0.35D,
					Math.sin(angle) * halfWidth);
			VesperVisualEffects.lightning(server, edge, center, false, boss.tickCount * 31L + part.getId());
		}
	}

	static void hitThroneAnchor(VesperTheCrownedRefusalEntity boss, int anchor) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 center = exposedAnchorCenter(boss, anchor);
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
		Vec3 center = exposedAnchorCenter(boss, anchor);
		VesperVisualEffects.bloodCells(server, center, VesperVisualEffects.BLOOD,
				42, 0.85D, 0.85D, 0.85D, 0.11D);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				28, 1.0D, 0.9D, 1.0D, 0.07D);
		Vec3 bossCenter = boss.position().add(0.0D, boss.getBbHeight() * 0.62D, 0.0D);
		VesperVisualEffects.voidTendril(server, center, bossCenter, boss.tickCount * 37L + anchor);
		VesperVisualEffects.lightning(server, center, bossCenter, false, boss.tickCount * 53L + anchor);
		server.playSound(null, part.blockPosition(), SoundEvents.ANVIL_DESTROY, SoundSource.HOSTILE, 1.5F, 0.65F);
	}

	private static Vec3 exposedAnchorCenter(VesperTheCrownedRefusalEntity boss, int anchor) {
		return boss.woundCenter(anchor);
	}

	/** The carapace splits: scute shards and blood burst out of the socket and void tendrils tear the edges open. */
	static void openThroneWound(VesperTheCrownedRefusalEntity boss, int anchor) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 center = exposedAnchorCenter(boss, anchor);
		VesperVisualEffects.bloodCells(server, center, VesperVisualEffects.BLOOD, 34, 0.7D, 0.6D, 0.7D, 0.14D);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK, 22, 0.8D, 0.6D, 0.8D, 0.06D);
		for (int i = 0; i < 3; i++) {
			double angle = Mth.TWO_PI * i / 3.0D + anchor;
			Vec3 edge = center.add(Math.cos(angle) * 1.4D, 0.5D, Math.sin(angle) * 1.4D);
			VesperVisualEffects.voidTendril(server, center, edge, boss.tickCount * 29L + i);
		}
		VesperVisualEffects.lightning(server, center, center.add(0.0D, 3.0D, 0.0D), false, boss.tickCount * 61L);
		server.playSound(null, center.x, center.y, center.z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.0F, 0.5F);
		// Blows no longer land while the wound is open; tell the fight how to answer it.
		Component hint = Component.translatable("message.hemomancy.vesper.wound_open")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC);
		for (ServerPlayer player : server.getPlayers(nearby -> nearby.distanceToSqr(boss) < 48.0D * 48.0D)) {
			player.displayClientMessage(hint, true);
		}
	}

	static void tickVesperTransformation(VesperTheCrownedRefusalEntity boss, int tick) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		float absorption = VesperPhaseTransitionRules.absorptionProgress(tick);
		Vec3 center = vesperCocoonCenter(boss);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				tick % 4 == 0 ? 4 : 2, 1.35D, 1.65D, 1.35D, 0.025D);
		if (absorption > 0.0F && absorption < 1.0F) {
			server.sendParticles(HemoParticleData.absorbedBloodCell(ParticleColor.BLOOD),
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
		server.sendParticles(HemoParticleData.absorbedBloodCell(ParticleColor.BLOOD),
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
				mob -> mob.getTags().contains(VesperMountMoves.VESPER_PUPPET_TAG))
				.forEach(com.vincenthuto.hemomancy.common.summon.PuppetStrings::unspool);
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
				boss.level().addParticle(HemoParticleData.absorbedBloodCell(ParticleColor.BLOOD),
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
				boss.level().addParticle(HemoParticleData.absorbedBloodCell(ParticleColor.BLOOD),
						target.x, target.y, target.z, sourceOffset.x, sourceOffset.y, sourceOffset.z);
			}
			return;
		}
		EnumBloodTendency tendency = boss instanceof VesperTheEveningStarEntity evening
				? evening.getActiveTendency() : EnumBloodTendency.ANIMUS;
		VesperVisualEffects.ambient(boss.level(), boss.position(), boss.getBbWidth(), boss.getBbHeight(),
				boss instanceof VesperTheCrownedRefusalEntity, tendency);
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

	static LivingEntity acquireTarget(Mob boss, double range) {
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

	static BlockPos randomNearbyGround(ServerLevel server, BlockPos center, int radius) {
		RandomSource random = server.getRandom();
		BlockPos candidate = center.offset(random.nextInt(radius * 2 + 1) - radius, 0,
				random.nextInt(radius * 2 + 1) - radius);
		return findGround(server, candidate);
	}

	static BlockPos findGround(Level level, BlockPos origin) {
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
