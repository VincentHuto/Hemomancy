package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationStatusRules;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityIronPillar extends BloodConstructEntity {
	private static final double MAGNETIC_RADIUS = 8.0D;
	private static final double MAGNETIC_PIN_RADIUS = 1.3D;
	public float deathTicks = 1;
	private boolean magnetic;
	private int lifeTicks = 120;

	public EntityIronPillar(EntityType<? extends EntityIronPillar> type, Level worldIn) {
		super(type, worldIn);

	}

	public EntityIronPillar(EntityType<? extends EntityIronPillar> type, Level worldIn, LivingEntity creator) {
		super(type, worldIn);
		this.creator = creator;
	}

	@Override
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);
		if (magnetic && entityIn instanceof LivingEntity target && isMagneticTarget(target)) {
			pullTowardPillar(target, this.position().add(0.0D, this.getBbHeight() * 0.45D, 0.0D));
			return;
		}
		if (!(entityIn instanceof EntityIronPillar)) {
			if (getCreator() != null) {
				if (entityIn != creator) {
					entityIn.setDeltaMovement(0, 0.5f, 0);
				}
			} else {
				entityIn.setDeltaMovement(0, 0.5f, 0);
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.setYBodyRot(0);
		if (magnetic && !this.level().isClientSide) {
			tickMagnetism();
		}

		// Particle MobEffects
		float f = (this.random.nextFloat() - 0.5F) * 2.0F;
		float f1 = -1;
		float f2 = (this.random.nextFloat() - 0.5F) * 2.0F;
		float f3 = (this.random.nextFloat() - 0.5F) * 1.5F;
		if (this.tickCount < 2) {
			this.level().addParticle(ParticleTypes.SQUID_INK, this.getX() + f, this.getY() + 2.0D + f1,
					this.getZ() + f2, 0.0D, 0.0D, 0.0D);
		}
		if (this.tickCount > 2 && this.tickCount < lifeTicks) {
			for (int i = 0; i < 2; i++) {
				this.level().addParticle(DustParticleOptions.REDSTONE, this.getX() + f * 0.5, this.getY(),
						this.getZ() + f2 * 0.5, 0.0D, 0.0D, 0.0D);
				this.level().addParticle(ParticleTypes.ASH, this.getX() + f,
						this.getY() + (0.0D + i) + f3, this.getZ() + f2, 0.0D, 0.0D, 0.0D);
			}
		}
		if (this.tickCount == lifeTicks) {
			this.level().addParticle(ParticleTypes.SQUID_INK, this.getX() + f, this.getY() + 2.0D + f1,
					this.getZ() + f2, 0.0D, 0.0D, 0.0D);
			this.setHealth(0);

			playConstructExpirationSound();

		}
	}

	public void setMagnetic(int durationTicks) {
		this.magnetic = true;
		this.lifeTicks = Math.max(20, durationTicks);
	}

	public boolean isMagnetic() {
		return magnetic;
	}

	private void tickMagnetism() {
		Vec3 anchor = this.position().add(0.0D, this.getBbHeight() * 0.45D, 0.0D);
		this.level().getEntitiesOfClass(LivingEntity.class, new AABB(this.blockPosition()).inflate(MAGNETIC_RADIUS),
						target -> isMagneticTarget(target)
								&& target.position().distanceTo(anchor) <= MAGNETIC_RADIUS)
				.forEach(target -> pullTowardPillar(target, anchor));
	}

	private boolean isMagneticTarget(LivingEntity target) {
		return ManipulationStatusRules.canMagnetize(target instanceof Monster,
				getCreator() != null && (target.isAlliedTo(getCreator()) || getCreator().isAlliedTo(target)),
				target == getCreator());
	}

	private void pullTowardPillar(LivingEntity target, Vec3 anchor) {
		Vec3 toAnchor = anchor.subtract(target.position());
		double distance = toAnchor.length();
		if (distance <= 0.001D) {
			return;
		}
		Vec3 pull = toAnchor.normalize().scale(distance <= MAGNETIC_PIN_RADIUS ? 0.08D : 0.22D);
		target.setDeltaMovement(target.getDeltaMovement().add(pull).multiply(0.72D, 0.92D, 0.72D));
		target.hurtMarked = true;
		if (distance <= MAGNETIC_PIN_RADIUS) {
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 12, 4, false, true, true));
		}
	}

	@Override
	protected void tickDeath() {
		// Particle MobEffects
		float g = (this.random.nextFloat() - 0.5F) * 2.0F;
		float g1 = -1;
		float g2 = (this.random.nextFloat() - 0.5F) * 2.0F;
		deathTicks -= 0.05;
		if (this.deathTicks <= 0.1) {
			if (level().isClientSide) {
				playConstructDissolutionSound();
				this.level().addParticle(ParticleTypes.SQUID_INK, this.getX() + g,
						this.getY() + 2.0D + g1, this.getZ() + g2, 0.0D, 0.0D, 0.0D);
			}
		}
		if (this.deathTicks <= 0.1 && !this.level().isClientSide) {
			this.remove(RemovalReason.KILLED);
		}

	}
}
