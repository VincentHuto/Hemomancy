package com.vincenthuto.hemomancy.entity.mob;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class EntityBloodDrunkPuppeteer extends Monster {

	public EntityBloodDrunkPuppeteer(EntityType<? extends EntityBloodDrunkPuppeteer> type, Level worldIn) {
		super(type, worldIn);

	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
			MobSpawnType pReason, SpawnGroupData pSpawnData, CompoundTag pDataTag) {
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
	}

	@Override
	public void tick() {
		super.tick();
		if (level.dayTime() % 10 == 0) {

//			EntityEnthralledDoll[] needles = new EntityEnthralledDoll[1];
//			for (int i = 0; i < needles.length; i++) {
//				needles[i] = new EntityEnthralledDoll(level, this);
//				needles[i].setPos(position());
//				needles[i].setOwner(this);
//				level.addFreshEntity(needles[i]);
//			}
		}
		/*
		 * // Particle MobEffects float f = (this.rand.nextFloat() - 0.5F) * 2.0F; float
		 * f1 = -1; float f2 = (this.rand.nextFloat() - 0.5F) * 2.0F; if
		 * (this.ticksExisted < 2) { this.world.addParticle(ParticleTypes.POOF,
		 * this.getPosX() + (double) f, this.getPosY() + 2.0D + (double) f1,
		 * this.getPosZ() + (double) f2, 0.0D, 0.0D, 0.0D); }
		 * 
		 * if (this.ticksExisted > 2 && this.ticksExisted < 20) {
		 * 
		 * this.world.addParticle(ParticleTypes.ITEM_SNOWBALL, this.getPosX() + (double)
		 * f, this.getPosY() + 2.0D + (double) f1, this.getPosZ() + (double) f2, 0.0D,
		 * 0.0D, 0.0D); }
		 * 
		 * if (this.ticksExisted > 180 && this.ticksExisted < 220) {
		 * this.world.addParticle(ParticleTypes.ITEM_SNOWBALL, this.getPosX() + (double)
		 * f, this.getPosY() + 2.0D + (double) f1, this.getPosZ() + (double) f2, 0.0D,
		 * 0.0D, 0.0D);
		 * 
		 * } if (this.ticksExisted == 220) { this.world.addParticle(ParticleTypes.POOF,
		 * this.getPosX() + (double) f, this.getPosY() + 2.0D + (double) f1,
		 * this.getPosZ() + (double) f2, 0.0D, 0.0D, 0.0D); if (!this.world.isRemote) {
		 * this.setHealth(0); } else { if (!world.isRemote) {
		 * world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(),
		 * SoundEvents.BLOCK_SNOW_BREAK, SoundSource.HOSTILE, 3f, 1.2f, false); } } }
		 */
	}

	@Override
	public void playerTouch(Player entityIn) {
		super.playerTouch(entityIn);
		// entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 1.5f);

	}

	@Override
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);
		/*
		 * if (!(entityIn instanceof EntityDerangedBeast || entityIn instanceof
		 * EntityBeastFromBeyond)) {
		 * entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 1.5f); }
		 */
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));

	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WOLF_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WOLF_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.WOLF_HURT;
	}
}
