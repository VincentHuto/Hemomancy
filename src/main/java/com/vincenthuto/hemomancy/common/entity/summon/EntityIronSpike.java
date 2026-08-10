package com.vincenthuto.hemomancy.common.entity.summon;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityIronSpike extends BloodConstructEntity {
	public float deathTicks = 1;
	private int lifeTicks = 120;
	private static final String NO_CONTACT_DAMAGE = "HemomancyNoContactDamage";
	private static final String TEMPORARY_LIFE = "HemomancyTemporaryLife";

	public EntityIronSpike(EntityType<? extends EntityIronSpike> type, Level worldIn) {
		super(type, worldIn);

	}

	public EntityIronSpike(EntityType<? extends EntityIronSpike> type, Level worldIn, LivingEntity creator) {
		super(type, worldIn);
		this.creator = creator;
	}

	@Override
	protected void doPush(Entity entityIn) {
		if (!getPersistentData().getBoolean(NO_CONTACT_DAMAGE) && !(entityIn instanceof EntityIronSpike)) {
			if (getCreator() != null) {
				if (entityIn != creator) {
					entityIn.hurt(this.damageSources().generic(), 3.5f);
				}
			} else {
				entityIn.hurt(this.damageSources().generic(), 3.5f);
			}
		}
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (getPersistentData().contains(TEMPORARY_LIFE)) lifeTicks = getPersistentData().getInt(TEMPORARY_LIFE);
		this.setYBodyRot(0);

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

	public void setTemporaryResponse(int durationTicks, boolean contactDamage) {
		this.lifeTicks = Math.max(20, durationTicks);
		getPersistentData().putInt(TEMPORARY_LIFE, this.lifeTicks);
		getPersistentData().putBoolean(NO_CONTACT_DAMAGE, !contactDamage);
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
