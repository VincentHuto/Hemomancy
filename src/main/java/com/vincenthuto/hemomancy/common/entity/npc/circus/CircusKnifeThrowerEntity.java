package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class CircusKnifeThrowerEntity extends CircusPerformerEntity {
	private int attackCooldown;
	private int volley;

	public CircusKnifeThrowerEntity(EntityType<? extends CircusKnifeThrowerEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 28.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.24D).add(Attributes.ARMOR, 2.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	@Override protected String roleId() { return "circus_knife_thrower"; }
	@Override protected String texturePath() { return "textures/entity/circus/knife_thrower_0.png"; }
	@Override protected int performanceDuration() { return 100; }

	@Override
	protected void tickPerformance(int actTick) {
		if ((actTick == 30 || actTick == 55 || actTick == 80) && level() instanceof ServerLevel server) {
			Vec3 target = new Vec3(getHome().getX() + 3.0D, getHome().getY() + 1.5D, getHome().getZ() + 3.0D);
			Vec3 direction = target.subtract(getEyePosition()).normalize();
			server.sendParticles(ParticleTypes.CRIT, getEyePosition().x + direction.x,
					getEyePosition().y + direction.y, getEyePosition().z + direction.z,
					7, 0.02D, 0.02D, 0.02D, 0.12D);
			level().playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.NEUTRAL, 0.35F, 1.7F);
		}
	}

	@Override
	protected void tickDefense(LivingEntity target) {
		getLookControl().setLookAt(target, 30.0F, 30.0F);
		if (distanceToSqr(target) > 225.0D) getNavigation().moveTo(target, 0.9D);
		if (attackCooldown-- > 0 || distanceToSqr(target) > 225.0D) return;
		attackCooldown = 70;
		boolean fan = ++volley % 4 == 0;
		if (fan) {
			throwKnife(target, -10.0D, 3.0F);
			throwKnife(target, 0.0D, 3.0F);
			throwKnife(target, 10.0D, 3.0F);
		} else {
			throwKnife(target, 0.0D, 4.0F);
		}
		level().playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE, 0.8F, 1.45F);
	}

	private void throwKnife(LivingEntity target, double yawOffsetDegrees, float damage) {
		CircusKnifeProjectileEntity knife = new CircusKnifeProjectileEntity(level(), this, damage);
		Vec3 direction = target.getEyePosition().subtract(getEyePosition()).normalize()
				.yRot((float) Math.toRadians(yawOffsetDegrees));
		knife.setPos(getX(), getEyeY() - 0.15D, getZ());
		knife.shoot(direction.x, direction.y, direction.z, 1.25F, 0.5F);
		level().addFreshEntity(knife);
	}
}
