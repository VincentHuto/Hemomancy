package com.vincenthuto.hemomancy.common.entity.npc.circus;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public final class CircusStiltWalkerEntity extends CircusPerformerEntity {
	private int attackCooldown;

	public CircusStiltWalkerEntity(EntityType<? extends CircusStiltWalkerEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.18D).add(Attributes.ARMOR, 4.0D)
				.add(Attributes.STEP_HEIGHT, 1.5D).add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	@Override protected String roleId() { return "circus_stilt_walker"; }
	@Override protected String texturePath() { return "textures/entity/circus/stilt_walker_0.png"; }
	@Override protected int performanceDuration() { return 160; }

	@Override
	protected void tickPerformance(int actTick) {
		if (actTick % 40 == 1) {
			double angle = actTick / 160.0D * Math.PI * 2.0D;
			getNavigation().moveTo(getHome().getX() + 0.5D + Math.cos(angle) * 4.0D, getHome().getY(),
					getHome().getZ() + 0.5D + Math.sin(angle) * 4.0D, 0.65D);
		}
	}

	@Override
	protected void tickDefense(LivingEntity target) {
		if (distanceToSqr(target) > 16.0D) getNavigation().moveTo(target, 0.85D);
		if (attackCooldown-- > 0 || distanceToSqr(target) > 16.0D) return;
		attackCooldown = 70;
		level().playSound(null, blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 0.9F, 0.65F);
		if (level() instanceof ServerLevel server) {
			server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,
					level().getBlockState(getBlockPosBelowThatAffectsMyMovement())),
					getX(), getY() + 0.1D, getZ(), 24, 1.4D, 0.05D, 1.4D, 0.0D);
		}
		for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.5D))) {
			if (victim == this || isTroupeMember(victim)) continue;
			double dx = victim.getX() - getX();
			double dz = victim.getZ() - getZ();
			double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
			if (victim.hurt(damageSources().mobAttack(this), 3.0F))
				victim.push(dx / length * 1.1D, 0.35D, dz / length * 1.1D);
		}
	}
}
