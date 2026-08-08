package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.client.particle.factory.AbsorbedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
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

import java.util.List;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

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

	static void tickExposedThroneAnchor(VesperTheCrownedRefusalEntity boss, int anchor, float damage) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		if (boss.tickCount % 3 == 0) {
			PartEntity<?> part = boss.getParts()[anchor];
			Vec3 wound = part.position().add(0.0D, part.getBbHeight() * 0.55D, 0.0D);
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
			Vec3 ringCenter = part.position().add(0.0D,
					0.08D + level * part.getBbHeight() * 0.45D, 0.0D);
			VesperVisualEffects.telegraphRing(server, ringCenter, halfWidth, woundColor, 12);
		}
		if (boss.tickCount % 12 == 0) {
			Vec3 center = part.position().add(0.0D, part.getBbHeight() * 0.5D, 0.0D);
			double angle = (boss.tickCount + part.getId() * 19L) * 0.21D;
			Vec3 edge = center.add(Math.cos(angle) * halfWidth, part.getBbHeight() * 0.35D,
					Math.sin(angle) * halfWidth);
			VesperVisualEffects.lightning(server, edge, center, false, boss.tickCount * 31L + part.getId());
		}
	}

	static void breakThroneAnchor(VesperTheCrownedRefusalEntity boss, int anchor) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		PartEntity<?> part = boss.getParts()[anchor];
		Vec3 center = part.position().add(0.0D, part.getBbHeight() * 0.5D, 0.0D);
		VesperVisualEffects.bloodCells(server, center, VesperVisualEffects.BLOOD,
				42, 0.85D, 0.85D, 0.85D, 0.11D);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				28, 1.0D, 0.9D, 1.0D, 0.07D);
		Vec3 bossCenter = boss.position().add(0.0D, boss.getBbHeight() * 0.62D, 0.0D);
		VesperVisualEffects.voidTendril(server, center, bossCenter, boss.tickCount * 37L + anchor);
		VesperVisualEffects.lightning(server, center, bossCenter, false, boss.tickCount * 53L + anchor);
		server.playSound(null, part.blockPosition(), SoundEvents.ANVIL_DESTROY, SoundSource.HOSTILE, 1.5F, 0.65F);
	}

	static void tickVesperTransformation(VesperTheCrownedRefusalEntity boss, int tick) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		float absorption = VesperPhaseTransitionRules.absorptionProgress(tick);
		int count = tick % 20 == 0 ? 70 : 8;
		Vec3 center = boss.position().add(0.0D, boss.getBbHeight() * 0.62D, 0.0D);
		VesperVisualEffects.darkGlow(server, center, VesperVisualEffects.BLACK,
				Math.max(5, count / 2), 2.8D, 2.2D, 2.8D, 0.07D);
		if (absorption <= 0.0F) {
			VesperVisualEffects.spores(server, center, VesperVisualEffects.DEEP_BLOOD,
					Math.max(4, count / 3), 3.2D, 2.5D, 3.2D, 0.035D);
		} else {
			server.sendParticles(AbsorbedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
					boss.getX(), boss.getY() + boss.getBbHeight() * 0.68D, boss.getZ(),
					12, 2.8D * (1.0D - absorption * 0.65D), 1.5D, 2.8D * (1.0D - absorption * 0.65D), 0.12D);
		}
		if (tick % 20 == 0) {
			for (int i = 0; i < 4; i++) {
				double angle = Mth.TWO_PI * i / 4.0D + tick * 0.035D;
				Vec3 source = center.add(Math.cos(angle) * 3.2D, -0.6D + (i & 1) * 1.6D,
						Math.sin(angle) * 3.2D);
				VesperVisualEffects.voidTendril(server, source, center, tick * 101L + i);
			}
		}
		if (tick % 30 == 0) {
			Vec3 arcStart = center.add(-2.2D, 1.4D, 0.0D);
			Vec3 arcEnd = center.add(2.2D, -0.6D, 0.0D);
			VesperVisualEffects.lightning(server, arcStart, arcEnd, false, tick * 131L);
		}
		if (tick % 30 == 0) server.playSound(null, boss.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK,
				SoundSource.HOSTILE, 1.2F, 0.55F + tick / 300.0F);
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
