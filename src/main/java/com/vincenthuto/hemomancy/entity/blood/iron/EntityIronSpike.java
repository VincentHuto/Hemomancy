package com.vincenthuto.hemomancy.entity.blood.iron;

import com.vincenthuto.hemomancy.entity.blood.EntityBloodConstruct;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityIronSpike extends EntityBloodConstruct {
	public float deathTicks = 1;

	public EntityIronSpike(EntityType<? extends EntityIronSpike> type, Level worldIn) {
		super(type, worldIn);

	}

	public EntityIronSpike(EntityType<? extends EntityIronSpike> type, Level worldIn, LivingEntity creator) {
		super(type, worldIn);
		this.creator = creator;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.setYBodyRot(0);

		// Particle MobEffects
		float f = (this.random.nextFloat() - 0.5F) * 2.0F;
		float f1 = -1;
		float f2 = (this.random.nextFloat() - 0.5F) * 2.0F;
		float f3 = (this.random.nextFloat() - 0.5F) * 1.5F;
		if (this.tickCount < 2) {
			this.level.addParticle(ParticleTypes.SQUID_INK, this.getX() + (double) f, this.getY() + 2.0D + (double) f1,
					this.getZ() + (double) f2, 0.0D, 0.0D, 0.0D);
		}

		if (this.tickCount > 2 && this.tickCount < 120) {
			for (int i = 0; i < 2; i++) {
				this.level.addParticle(DustParticleOptions.REDSTONE, this.getX() + (double) f * 0.5, this.getY(),
						this.getZ() + (double) f2 * 0.5, 0.0D, 0.0D, 0.0D);
				this.level.addParticle(ParticleTypes.ASH, this.getX() + (double) f,
						this.getY() + (0.0D + i) + (double) f3, this.getZ() + (double) f2, 0.0D, 0.0D, 0.0D);
			}
		}
		if (this.tickCount == 120) {
			this.level.addParticle(ParticleTypes.SQUID_INK, this.getX() + (double) f, this.getY() + 2.0D + (double) f1,
					this.getZ() + (double) f2, 0.0D, 0.0D, 0.0D);
			this.setHealth(0);

			level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.IRON_GOLEM_DAMAGE,
					SoundSource.HOSTILE, 3f, 1.2f, false);

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
			if (level.isClientSide) {
				level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.IRON_GOLEM_DAMAGE,
						SoundSource.HOSTILE, 3f, 0.2f, false);
				this.level.addParticle(ParticleTypes.SQUID_INK, this.getX() + (double) g,
						this.getY() + 2.0D + (double) g1, this.getZ() + (double) g2, 0.0D, 0.0D, 0.0D);
			}
		}

		if (this.deathTicks <= 0.1 && !this.level.isClientSide) {
			this.remove(RemovalReason.KILLED);
		}

	}

	@Override
	protected void doPush(Entity entityIn) {
		if (!(entityIn instanceof EntityIronSpike)) {
			if (getCreator() != null) {
				if (entityIn != creator) {
					entityIn.hurt(DamageSource.GENERIC, 3.5f);
				}
			} else {
				entityIn.hurt(DamageSource.GENERIC, 3.5f);
			}
		}
	}

}
