package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class BloodConstructEntity extends PathfinderMob implements IBloodConstruct {

	public static AttributeSupplier.Builder setAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0D)
				.add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MOVEMENT_SPEED, 0f)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1000);
	}
	public static final int DEFAULT_LIFETIME_TICKS = 120;
	private static final ParticleColor DISSOLUTION = new ParticleColor(64, 6, 10);
	public float deathTicks = 1;
	private final BloodConstructSoundGate soundGate = new BloodConstructSoundGate();

	public LivingEntity creator;

	protected BloodConstructEntity(EntityType<? extends PathfinderMob> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.IRON_GOLEM_DAMAGE;
	}

	@Override
	public LivingEntity getCreator() {
		return creator;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.IRON_GOLEM_DAMAGE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.IRON_GOLEM_DAMAGE;
	}

	@Override
	protected float getSoundVolume() {
		return 0.1f;
	}

	protected final void playConstructExpirationSound() {
		playConstructLifecycleSound(soundGate.claimExpiration(getSoundVolume()), 1.2F);
	}

	protected final void playConstructDissolutionSound() {
		playConstructLifecycleSound(soundGate.claimDissolution(getSoundVolume()), 0.2F);
	}

	private void playConstructLifecycleSound(BloodConstructSoundGate.CueRequest request, float pitch) {
		if (request.shouldPlay()) {
			level().playLocalSound(getX(), getY(), getZ(), SoundEvents.IRON_GOLEM_DAMAGE,
					SoundSource.HOSTILE, request.volume(), pitch, false);
		}
	}

	// Later implement a potion of dispelling that will remove them
	@Override
	public boolean isAffectedByPotions() {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return true;
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void registerGoals() {
	}

	public void setCreator(LivingEntity creator) {
		this.creator = creator;
	}

	@Override
	public void tick() {
		super.tick();
		int lifetime = constructLifetimeTicks();
		if (lifetime <= 0) return;
		this.setYBodyRot(0);
		if (this.tickCount == lifetime) {
			this.setHealth(0);
			playConstructExpirationSound();
		}
	}

	/** Ticks before the construct expires on its own; zero or less keeps it until something else removes it. */
	protected int constructLifetimeTicks() {
		return DEFAULT_LIFETIME_TICKS;
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
				this.level().addParticle(HemoParticleData.darkGlow(DISSOLUTION), this.getX() + g, this.getY() + 2.0D + g1,
						this.getZ() + g2, 0.0D, 0.0D, 0.0D);
			}
		}

		if (this.deathTicks <= 0.1 && !this.level().isClientSide) {
			this.remove(RemovalReason.KILLED);
		}

	}

}
