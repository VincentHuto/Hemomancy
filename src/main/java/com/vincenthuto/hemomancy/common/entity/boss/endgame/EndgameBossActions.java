package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.TrackingBloodOrbEntity;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypEntity;
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

import java.util.List;

final class EndgameBossActions {
	private static final double VESPER_ATTACK_RANGE = 64.0D;
	private static final double MYCOPHANT_ATTACK_RANGE = 64.0D;

	private EndgameBossActions() {
	}

	static void tickVesperPattern(Monster boss, int addBonus) {
		LivingEntity target = acquireTarget(boss, VESPER_ATTACK_RANGE);
		if (target == null) {
			return;
		}

		chaseTarget(boss, target, 1.15D, 36.0D);
		float regen = EndgameBossCombatRules.regenerationPerTick(boss.getHealth(), boss.getMaxHealth());
		if (regen > 0.0F) {
			boss.heal(regen);
		}

		if (EndgameBossCombatRules.shouldTrigger(boss.tickCount, boss.getId() % 5, 50,
				boss.getHealth(), boss.getMaxHealth())) {
			spawnTrackingOrb(boss, target);
		}
		if (EndgameBossCombatRules.shouldTrigger(boss.tickCount, boss.getId() % 9, 120,
				boss.getHealth(), boss.getMaxHealth())) {
			if ((boss.tickCount / 120 + boss.getId()) % 2 == 0) {
				summonGripSpikes(boss, target, 4 + addBonus);
			} else {
				summonPolypAid(boss, target, 2 + addBonus);
			}
		}
		if (boss.onGround() && EndgameBossCombatRules.shouldTrigger(boss.tickCount, boss.getId() % 7, 100,
				boss.getHealth(), boss.getMaxHealth())) {
			summonGripSpikes(boss, target, 3 + addBonus);
		}
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
		for (int i = 0; i < 2; i++) {
			double x = boss.getX() + (random.nextDouble() - 0.5D) * boss.getBbWidth();
			double y = boss.getY() + random.nextDouble() * boss.getBbHeight();
			double z = boss.getZ() + (random.nextDouble() - 0.5D) * boss.getBbWidth();
			boss.level().addParticle(i == 0 ? ParticleTypes.CRIMSON_SPORE : ParticleTypes.ASH,
					x, y, z, 0.0D, 0.01D, 0.0D);
		}
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
		sendParticles(server, ParticleTypes.ASH, boss, count, 1.4D, 1.0D, 1.4D, 0.08D);
		sendParticles(server, ParticleTypes.CRIMSON_SPORE, boss, count / 2, 1.2D, 0.8D, 1.2D, 0.02D);
		if (deathTicks % 40 == 0) {
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

	private static void spawnTrackingOrb(Monster boss, LivingEntity target) {
		if (!(boss.level() instanceof ServerLevel server)) {
			return;
		}
		TrackingBloodOrbEntity orb = new TrackingBloodOrbEntity(boss, true);
		Vec3 look = boss.getLookAngle();
		orb.setPos(boss.getX() + look.x * 1.4D, boss.getY() + boss.getBbHeight() * 0.72D, boss.getZ() + look.z * 1.4D);
		orb.setTarget(target);
		if (orb.findTarget()) {
			server.addFreshEntity(orb);
			boss.playSound(SoundInit.ENTITY_VESPER_HIT.get(), 1.2F, 0.85F + server.getRandom().nextFloat() * 0.25F);
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

	private static void summonPolypAid(Monster boss, LivingEntity target, int count) {
		if (!(boss.level() instanceof ServerLevel server)) {
			return;
		}
		RandomSource random = server.getRandom();
		for (int i = 0; i < count; i++) {
			MorphlingPolypEntity polyp = EntityInit.morphling_polyp.get().create(server);
			if (polyp == null) {
				continue;
			}
			BlockPos pos = randomNearbyGround(server, boss.blockPosition(), 5);
			polyp.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
					random.nextFloat() * 360.0F, 0.0F);
			polyp.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null);
			polyp.setTarget(target);
			server.addFreshEntity(polyp);
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
