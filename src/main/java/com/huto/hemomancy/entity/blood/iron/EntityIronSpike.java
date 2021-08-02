package com.huto.hemomancy.entity.blood.iron;

import com.huto.hemomancy.entity.blood.EntityBloodConstruct;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.damagesource.DamageSource;
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
	public boolean canBePushed() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.setRenderYawOffset(0);

		// Particle Effects
		float f = (this.rand.nextFloat() - 0.5F) * 2.0F;
		float f1 = -1;
		float f2 = (this.rand.nextFloat() - 0.5F) * 2.0F;
		float f3 = (this.rand.nextFloat() - 0.5F) * 1.5F;
		if (this.ticksExisted < 2) {
			this.world.addParticle(ParticleTypes.SQUID_INK, this.getPosX() + (double) f,
					this.getPosY() + 2.0D + (double) f1, this.getPosZ() + (double) f2, 0.0D, 0.0D, 0.0D);
		}

		if (this.ticksExisted > 2 && this.ticksExisted < 120) {
			for (int i = 0; i < 2; i++) {
				this.world.addParticle(RedstoneParticleData.REDSTONE_DUST, this.getPosX() + (double) f * 0.5,
						this.getPosY(), this.getPosZ() + (double) f2 * 0.5, 0.0D, 0.0D, 0.0D);
				this.world.addParticle(ParticleTypes.ASH, this.getPosX() + (double) f,
						this.getPosY() + (0.0D + i) + (double) f3, this.getPosZ() + (double) f2, 0.0D, 0.0D, 0.0D);
			}
		}
		if (this.ticksExisted == 120) {
			this.world.addParticle(ParticleTypes.SQUID_INK, this.getPosX() + (double) f,
					this.getPosY() + 2.0D + (double) f1, this.getPosZ() + (double) f2, 0.0D, 0.0D, 0.0D);
			this.setHealth(0);

			world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_IRON_GOLEM_DAMAGE,
					SoundCategory.HOSTILE, 3f, 1.2f, false);

		}
	}

	@Override
	protected void onDeathUpdate() {
		// Particle Effects
		float g = (this.rand.nextFloat() - 0.5F) * 2.0F;
		float g1 = -1;
		float g2 = (this.rand.nextFloat() - 0.5F) * 2.0F;
		deathTicks -= 0.05;
		if (this.deathTicks <= 0.1) {
			if (level.isClientSide) {
				world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_IRON_GOLEM_DAMAGE,
						SoundCategory.HOSTILE, 3f, 0.2f, false);
				this.world.addParticle(ParticleTypes.SQUID_INK, this.getPosX() + (double) g,
						this.getPosY() + 2.0D + (double) g1, this.getPosZ() + (double) g2, 0.0D, 0.0D, 0.0D);
			}
		}

		if (this.deathTicks <= 0.1 && !this.level.isClientSide) {
			this.remove();
		}

	}

	@Override
	protected void collideWithEntity(Entity entityIn) {
		if (!(entityIn instanceof EntityIronSpike)) {
			if (getCreator() != null) {
				if (entityIn != creator) {
					entityIn.attackEntityFrom(DamageSource.GENERIC, 3.5f);
				}
			} else {
				entityIn.attackEntityFrom(DamageSource.GENERIC, 3.5f);
			}
		}
	}

}
