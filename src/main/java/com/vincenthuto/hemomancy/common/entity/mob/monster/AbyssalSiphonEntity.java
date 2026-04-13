package com.vincenthuto.hemomancy.common.entity.mob.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

/**
 * Deep Dark mob - a sculk-adjacent blood horror that drains vitality in darkness.
 * Stronger in darkness, applies darkness and weakness effects.
 */
public class AbyssalSiphonEntity extends Monster {

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 18.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.25D)
				.add(Attributes.ATTACK_DAMAGE, 5.0D)
				.add(Attributes.ARMOR, 3.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	public AbyssalSiphonEntity(EntityType<? extends AbyssalSiphonEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean flag = super.doHurtTarget(target);
		if (flag && target instanceof net.minecraft.world.entity.LivingEntity living) {
			living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0));
			living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
		}
		return flag;
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
			MobSpawnType pReason, SpawnGroupData pSpawnData, CompoundTag pDataTag) {
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_ABYSSAL_SIPHON_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_ABYSSAL_SIPHON_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_ABYSSAL_SIPHON_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.4f;
	}

	public static boolean canSpawnHere(EntityType<? extends Monster> pType, ServerLevelAccessor pLevel,
			MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
		return pLevel.getDifficulty() != Difficulty.PEACEFUL
				&& pPos.getY() < 0
				&& (pLevel.getBlockState(pPos.below()).is(Blocks.SCULK)
						|| pLevel.getBlockState(pPos.below()).is(Blocks.DEEPSLATE)
						|| pLevel.getBlockState(pPos.below()).is(Blocks.SCULK_CATALYST))
				&& checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
	}

	@Override
	public void tick() {
		super.tick();
	}
}
