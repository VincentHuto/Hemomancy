package com.huto.hemomancy.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

public class EntityIronSpike extends CreatureEntity {
	public float deathTicks = 1;

	public EntityIronSpike(EntityType<? extends EntityIronSpike> type, World worldIn) {
		super(type, worldIn);

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
		if (world instanceof ServerWorld && ((ServerWorld) world).func_241112_a_()
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
		if (this.ticksExisted < 2) {
			this.world.addParticle(ParticleTypes.SQUID_INK, this.getPosX() + (double) f,
					this.getPosY() + 2.0D + (double) f1, this.getPosZ() + (double) f2, 0.0D, 0.0D, 0.0D);
		}

		if (this.ticksExisted > 2 && this.ticksExisted < 20) {

			this.world.addParticle(RedstoneParticleData.REDSTONE_DUST, this.getPosX() + (double) f,
					this.getPosY() + 2.0D + (double) f1, this.getPosZ() + (double) f2, 0.0D, 0.0D, 0.0D);
		}

		if (this.ticksExisted > 80 && this.ticksExisted < 120) {
			this.world.addParticle(RedstoneParticleData.REDSTONE_DUST, this.getPosX() + (double) f,
					this.getPosY() + 2.0D + (double) f1, this.getPosZ() + (double) f2, 0.0D, 0.0D, 0.0D);

		}
		if (this.ticksExisted == 120) {
			this.world.addParticle(ParticleTypes.SQUID_INK, this.getPosX() + (double) f,
					this.getPosY() + 2.0D + (double) f1, this.getPosZ() + (double) f2, 0.0D, 0.0D, 0.0D);
			if (!this.world.isRemote) {
				this.setHealth(0);
			} else {
				if (!world.isRemote) {
					world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(),
							SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, SoundCategory.HOSTILE, 3f, 1.2f, false);
				}
			}
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
			if (world.isRemote) {
				world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_IRON_GOLEM_DAMAGE,
						SoundCategory.HOSTILE, 3f, 0.2f, false);
				this.world.addParticle(ParticleTypes.SQUID_INK, this.getPosX() + (double) g,
						this.getPosY() + 2.0D + (double) g1, this.getPosZ() + (double) g2, 0.0D, 0.0D, 0.0D);
			}
		}

		if (this.deathTicks <= 0.1 && !this.world.isRemote) {
			this.remove();
		}

	}

	@Override
	public void onCollideWithPlayer(PlayerEntity entityIn) {
		entityIn.attackEntityFrom(DamageSource.GENERIC, 3.5f);

	}

	@Override
	protected void collideWithEntity(Entity entityIn) {
		entityIn.attackEntityFrom(DamageSource.GENERIC, 3.5f);

	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
	}

	public static AttributeModifierMap.MutableAttribute setAttributes() {
		return LivingEntity.registerAttributes().createMutableAttribute(Attributes.FOLLOW_RANGE, 16.0D)
				.createMutableAttribute(Attributes.ATTACK_KNOCKBACK)
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0f)
				.createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 0);
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
}
