package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.entity.mob.animal.VerdigrisMothEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ToothPecksEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

public class VenomRibCentipedeEntity extends PathfinderMob {
	private static final EntityDataAccessor<Boolean> RIBS_RAISED =
			SynchedEntityData.defineId(VenomRibCentipedeEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> STRIKE_TICKS =
			SynchedEntityData.defineId(VenomRibCentipedeEntity.class, EntityDataSerializers.INT);

	public VenomRibCentipedeEntity(EntityType<? extends VenomRibCentipedeEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, VenomRibCentipedeRules.MAX_HEALTH)
				.add(Attributes.MOVEMENT_SPEED, VenomRibCentipedeRules.MOVEMENT_SPEED)
				.add(Attributes.ATTACK_DAMAGE, VenomRibCentipedeRules.ATTACK_DAMAGE)
				.add(Attributes.FOLLOW_RANGE, VenomRibCentipedeRules.FOLLOW_RANGE);
	}

	public static boolean canSpawnHere(EntityType<? extends VenomRibCentipedeEntity> type, LevelAccessor level,
			MobSpawnType spawnType, BlockPos pos, RandomSource random) {
		BlockPos below = pos.below();
		boolean openAir = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
				&& level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
		return level.getDifficulty() != Difficulty.PEACEFUL
				&& VenomRibCentipedeRules.canNaturalSpawn(level.getBlockState(below).isSolidRender(level, below),
				openAir, level.getMaxLocalRawBrightness(pos));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(RIBS_RAISED, false);
		builder.define(STRIKE_TICKS, 0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.18D, true));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
		this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, ChitiniteEntity.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, FerventChitiniteEntity.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, HemolymphopodaEntity.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, ToothPecksEntity.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, VerdigrisMothEntity.class, true));
	}

	public boolean areRibsRaised() {
		return this.entityData.get(RIBS_RAISED);
	}

	public int getStrikeTicks() {
		return this.entityData.get(STRIKE_TICKS);
	}

	public float getStrikeProgress(float partialTick) {
		int ticks = this.getStrikeTicks();
		if (ticks <= 0) {
			return 0.0F;
		}
		return Math.min(1.0F, (ticks - partialTick) / (float) VenomRibCentipedeRules.STRIKE_ANIMATION_TICKS);
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hurt = super.doHurtTarget(target);
		this.entityData.set(STRIKE_TICKS, VenomRibCentipedeRules.STRIKE_ANIMATION_TICKS);
		if (hurt && target instanceof LivingEntity living && this.level().getDifficulty() != Difficulty.PEACEFUL) {
			living.addEffect(new MobEffectInstance(MobEffects.POISON, VenomRibCentipedeRules.POISON_DURATION_TICKS, 0));
			living.addEffect(new MobEffectInstance(EffectInit.blood_binding,
					VenomRibCentipedeRules.BLOOD_BINDING_DURATION_TICKS, 0));
		}
		return hurt;
	}

	@Override
	public void tick() {
		super.tick();
		int strikeTicks = this.getStrikeTicks();
		if (strikeTicks > 0) {
			this.entityData.set(STRIKE_TICKS, strikeTicks - 1);
		}
		if (!this.level().isClientSide()) {
			this.updateTerritorialTargeting();
		}
	}

	private void updateTerritorialTargeting() {
		if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
			this.setTarget(null);
			this.entityData.set(RIBS_RAISED, false);
			return;
		}
		Player warningPlayer = this.findNearestVulnerablePlayer(VenomRibCentipedeRules.WARNING_RANGE_BLOCKS);
		this.entityData.set(RIBS_RAISED, warningPlayer != null);
		Player strikePlayer = this.findNearestVulnerablePlayer(VenomRibCentipedeRules.STRIKE_RANGE_BLOCKS);
		if (strikePlayer != null && VenomRibCentipedeRules.shouldStrike(strikePlayer.isAlive(),
				strikePlayer.isCreative(), strikePlayer.isSpectator(), this.distanceTo(strikePlayer))) {
			this.setTarget(strikePlayer);
		} else if (this.getTarget() instanceof Player) {
			this.setTarget(null);
		}
	}

	private Player findNearestVulnerablePlayer(double range) {
		AABB bounds = this.getBoundingBox().inflate(range);
		return this.level().getEntitiesOfClass(Player.class, bounds, player -> VenomRibCentipedeRules.shouldWarn(
						player.isAlive(), player.isCreative(), player.isSpectator(), this.distanceTo(player)))
				.stream()
				.min(Comparator.comparingDouble(this::distanceToSqr))
				.orElse(null);
	}

	@Override
	protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
		super.actuallyHurt(damageSource, damageAmount);
		if (!this.level().isClientSide() && this.level().getDifficulty() != Difficulty.PEACEFUL) {
			this.entityData.set(RIBS_RAISED, true);
		}
	}
}
