package com.huto.hemomancy.entity.blood;

import javax.annotation.Nullable;

import com.huto.hemomancy.entity.blood.iron.IBloodConstruct;
import com.sun.jna.Structure;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.server.ServerWorld;

public class EntityBloodConstruct extends PathfinderMob implements IBloodConstruct {

	public float deathTicks = 1;
	public LivingEntity creator;

	protected EntityBloodConstruct(EntityType<? extends PathfinderMob> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean canBeAttackedWithItem() {
		return false;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	// Later implement a potion of dispelling that will remove them
	@Override
	public boolean canBeHitWithPotion() {
		return false;
	}

	@Override
	public boolean canBeLeashedTo(Player player) {
		return false;
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	protected void registerData() {
		super.registerData();

	}

	@Nullable
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason,
			@Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.setEquipmentBasedOnDifficulty(difficultyIn);
		World world = worldIn.getWorld();
		if (world instanceof ServerWorld && ((ServerWorld) world).getStructureManager()
				.getStructureStart(this.getPosition(), true, Structure.SWAMP_HUT).isValid()) {
			this.enablePersistence();
		}

		return spawnDataIn;

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
	public boolean isInvulnerableTo(DamageSource source) {
		return true;
	}

	@Override
	protected void registerGoals() {
	}

	public static AttributeModifierMap.MutableAttribute setAttributes() {
		return LivingEntity.registerAttributes().createMutableAttribute(Attributes.FOLLOW_RANGE, 16.0D)
				.createMutableAttribute(Attributes.ATTACK_KNOCKBACK)
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0f)
				.createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 1000);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_IRON_GOLEM_DAMAGE;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_IRON_GOLEM_DAMAGE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_IRON_GOLEM_DAMAGE;
	}

	@Override
	public LivingEntity getCreator() {
		return creator;
	}

}
