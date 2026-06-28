package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

/**
 * Desert mob - a scorpion-like blood drainer adapted to arid environments.
 * Extracts moisture/blood from prey. Fast, low profile, venomous.
 */
public class DesiccantEntity extends Monster {
	private static final EntityDataAccessor<Integer> DATA_STING_TICKS = SynchedEntityData.defineId(
			DesiccantEntity.class, EntityDataSerializers.INT);
	public static final int STING_ANIMATION_TICKS = 16;
	public static final int STING_BLOOD_LOSS_TICKS = 120;
	public static final double STING_BLOOD_DRAIN = 250.0D;

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 12.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.35D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D)
				.add(Attributes.ARMOR, 4.0D);
	}

	public DesiccantEntity(EntityType<? extends DesiccantEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2D, true));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_STING_TICKS, 0);
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean flag = super.doHurtTarget(target);
		if (flag && target instanceof net.minecraft.world.entity.LivingEntity living) {
			this.startStingAttack();
			living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 1));
			living.addEffect(new MobEffectInstance(EffectInit.blood_loss, STING_BLOOD_LOSS_TICKS, 1));
			if (living instanceof Player player) {
				this.sapPlayerBlood(player);
			}
		}
		return flag;
	}

	private void startStingAttack() {
		this.setStingTicks(STING_ANIMATION_TICKS);
	}

	private void sapPlayerBlood(Player player) {
		if (this.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			if (!volume.isActive()) {
				return;
			}
			if (volume.drain(STING_BLOOD_DRAIN)) {
				BloodVolumeEvents.syncVolume(serverPlayer, volume);
			}
		});
	}

	private void setStingTicks(int stingTicks) {
		this.entityData.set(DATA_STING_TICKS, Math.max(0, stingTicks));
	}

	public int getStingTicks() {
		return this.entityData.get(DATA_STING_TICKS);
	}

	public boolean isStinging() {
		return this.getStingTicks() > 0;
	}

	public float getStingProgress() {
		return 1.0F - (float) this.getStingTicks() / STING_ANIMATION_TICKS;
	}

	@Override
	@SuppressWarnings("deprecation")
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
			MobSpawnType pReason, SpawnGroupData pSpawnData) {
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_DESICCANT_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_DESICCANT_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_DESICCANT_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.4f;
	}

	public static boolean canSpawnHere(EntityType<? extends Monster> pType, ServerLevelAccessor pLevel,
			MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
		return pLevel.getDifficulty() != Difficulty.PEACEFUL
				&& isDryHotSurface(pLevel.getBlockState(pPos.below()))
				&& checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
	}

	private static boolean isDryHotSurface(BlockState state) {
		return state.is(Blocks.SAND)
				|| state.is(Blocks.RED_SAND)
				|| state.is(Blocks.SANDSTONE)
				|| state.is(Blocks.RED_SANDSTONE)
				|| state.is(BlockTags.TERRACOTTA);
	}

	@Override
	public void tick() {
		super.tick();
		int stingTicks = this.getStingTicks();
		if (!this.level().isClientSide && stingTicks > 0) {
			this.setStingTicks(stingTicks - 1);
		}
	}
}
