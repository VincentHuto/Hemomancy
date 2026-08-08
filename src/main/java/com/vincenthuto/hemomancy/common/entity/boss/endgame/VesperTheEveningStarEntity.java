package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class VesperTheEveningStarEntity extends Monster {
    private static final int FINAL_DEATH_TICKS = 200;
	private static final EntityDataAccessor<Integer> DATA_TENDENCY = SynchedEntityData.defineId(
			VesperTheEveningStarEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_STANCE_TICK = SynchedEntityData.defineId(
			VesperTheEveningStarEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_AWAITING_ABSORPTION = SynchedEntityData.defineId(
			VesperTheEveningStarEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_DOWNED_TICKS = SynchedEntityData.defineId(
			VesperTheEveningStarEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_WEAPON_ACTION = SynchedEntityData.defineId(
			VesperTheEveningStarEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ACTION_TICK = SynchedEntityData.defineId(
			VesperTheEveningStarEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ACTION_VARIANT = SynchedEntityData.defineId(
			VesperTheEveningStarEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_RAGING = SynchedEntityData.defineId(
			VesperTheEveningStarEntity.class, EntityDataSerializers.BOOLEAN);

    private int deathTicks;
	private long tendencySeed;
	private int stanceIndex = -1;
	private UUID ordealOwner;
	private long bloomOrigin;
	private boolean ordealResolved;
	private float defeatAbsorptionProgress;
	private VesperWeaponAction lastWeaponAction = VesperWeaponAction.NONE;
	private Vec3 lockedActionAim = Vec3.ZERO;
	private Vec3 lockedActionOrigin = Vec3.ZERO;
	private int actionCooldown;
	private int actionHitMask;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hemomancy.vesper_evening_star"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_12);

    public VesperTheEveningStarEntity(EntityType<? extends VesperTheEveningStarEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 180;
    }

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_TENDENCY, EnumBloodTendency.FERRIC.ordinal());
		builder.define(DATA_STANCE_TICK, 0);
		builder.define(DATA_AWAITING_ABSORPTION, false);
		builder.define(DATA_DOWNED_TICKS, 0);
		builder.define(DATA_WEAPON_ACTION, VesperWeaponAction.NONE.ordinal());
		builder.define(DATA_ACTION_TICK, 0);
		builder.define(DATA_ACTION_VARIANT, 0);
		builder.define(DATA_RAGING, false);
	}

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
		// Ordeal victory delivers its owner-bound reward atomically with progression.
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 640.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 18.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.12D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
			if (isAwaitingAbsorption()) {
				entityData.set(DATA_DOWNED_TICKS, getDownedTicks() + 1);
				this.bossEvent.setProgress(1.0F - defeatAbsorptionProgress
						/ VesperCombatRules.DEFEAT_ABSORPTION_REQUIRED);
				this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
			} else {
				this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
				tickTendencyCombat();
			}
        }
    }

    @Override
    public void tick() {
        super.tick();
		if (!isAwaitingAbsorption()) EndgameBossActions.tickVesperClientParticles(this);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

	public void setOrdeal(UUID owner, long bloomOrigin) {
		this.ordealOwner = owner;
		this.bloomOrigin = bloomOrigin;
	}

	public UUID getOrdealOwner() { return ordealOwner; }
	public long getBloomOrigin() { return bloomOrigin; }

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (ordealOwner != null) tag.putUUID("OrdealOwner", ordealOwner);
		tag.putLong("BloomOrigin", bloomOrigin);
		tag.putLong("TendencySeed", tendencySeed);
		tag.putInt("StanceIndex", stanceIndex);
		tag.putInt("StanceTick", getStanceTick());
		tag.putInt("ActiveTendency", getActiveTendency().ordinal());
		tag.putBoolean("AwaitingBloodAbsorption", isAwaitingAbsorption());
		tag.putInt("DownedTicks", getDownedTicks());
		tag.putFloat("DefeatAbsorptionProgress", defeatAbsorptionProgress);
		tag.putInt("WeaponAction", getWeaponAction().ordinal());
		tag.putInt("WeaponActionTick", getActionTick());
		tag.putInt("WeaponActionVariant", getActionVariant());
		tag.putInt("LastWeaponAction", lastWeaponAction.ordinal());
		tag.putInt("WeaponActionCooldown", actionCooldown);
		tag.putInt("WeaponActionHitMask", actionHitMask);
		tag.putBoolean("Raging", isRaging());
		tag.putDouble("WeaponAimX", lockedActionAim.x);
		tag.putDouble("WeaponAimY", lockedActionAim.y);
		tag.putDouble("WeaponAimZ", lockedActionAim.z);
		tag.putDouble("WeaponOriginX", lockedActionOrigin.x);
		tag.putDouble("WeaponOriginY", lockedActionOrigin.y);
		tag.putDouble("WeaponOriginZ", lockedActionOrigin.z);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		ordealOwner = tag.hasUUID("OrdealOwner") ? tag.getUUID("OrdealOwner") : null;
		bloomOrigin = tag.getLong("BloomOrigin");
		tendencySeed = tag.getLong("TendencySeed");
		stanceIndex = tag.contains("StanceIndex") ? tag.getInt("StanceIndex") : -1;
		entityData.set(DATA_STANCE_TICK, tag.getInt("StanceTick"));
		int tendency = tag.getInt("ActiveTendency");
		entityData.set(DATA_TENDENCY, Math.max(0, Math.min(EnumBloodTendency.values().length - 1, tendency)));
		entityData.set(DATA_AWAITING_ABSORPTION, tag.getBoolean("AwaitingBloodAbsorption"));
		entityData.set(DATA_DOWNED_TICKS, tag.getInt("DownedTicks"));
		defeatAbsorptionProgress = tag.getFloat("DefeatAbsorptionProgress");
		entityData.set(DATA_WEAPON_ACTION, safeActionOrdinal(tag.getInt("WeaponAction")));
		entityData.set(DATA_ACTION_TICK, tag.getInt("WeaponActionTick"));
		entityData.set(DATA_ACTION_VARIANT, tag.getInt("WeaponActionVariant"));
		lastWeaponAction = VesperWeaponAction.values()[safeActionOrdinal(tag.getInt("LastWeaponAction"))];
		actionCooldown = tag.getInt("WeaponActionCooldown");
		actionHitMask = tag.getInt("WeaponActionHitMask");
		entityData.set(DATA_RAGING, tag.getBoolean("Raging"));
		lockedActionAim = new Vec3(tag.getDouble("WeaponAimX"), tag.getDouble("WeaponAimY"),
				tag.getDouble("WeaponAimZ"));
		lockedActionOrigin = new Vec3(tag.getDouble("WeaponOriginX"), tag.getDouble("WeaponOriginY"),
				tag.getDouble("WeaponOriginZ"));
		if (isAwaitingAbsorption()) {
			setNoAi(true);
			ensureSilentArchonShame();
		}
	}

	@Override
	protected void actuallyHurt(DamageSource source, float amount) {
		if (isAwaitingAbsorption()) return;
		super.actuallyHurt(source, amount);
		if (!level().isClientSide && getHealth() <= 0.0F) {
			setHealth(1.0F);
			enterAwaitingAbsorption();
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return !isAwaitingAbsorption() && super.hurt(source, amount);
	}

    @Override
    protected void tickDeath() {
        this.deathTicks++;
        EndgameBossActions.tickVesperDeathParticles(this, this.deathTicks);
        if (this.deathTicks >= FINAL_DEATH_TICKS) {
            if (!this.level().isClientSide) {
                EndgameBossActions.finishWithExplosion(this, 2.5F);
                this.bossEvent.removeAllPlayers();
            }
            this.deathTime = 19;
            super.tickDeath();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
		return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.COW_STEP, 0.2F, 0.55F);
    }

    @Override
    protected float getSoundVolume() {
        return 0.45F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundInit.ENTITY_VESPER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundInit.ENTITY_VESPER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundInit.ENTITY_VESPER_DEATH.get();
    }

	public EnumBloodTendency getActiveTendency() {
		return EnumBloodTendency.values()[Math.max(0, Math.min(EnumBloodTendency.values().length - 1,
				entityData.get(DATA_TENDENCY)))];
	}

	public int getStanceTick() { return entityData.get(DATA_STANCE_TICK); }
	public boolean isAwaitingAbsorption() { return entityData.get(DATA_AWAITING_ABSORPTION); }
	public int getDownedTicks() { return entityData.get(DATA_DOWNED_TICKS); }
	public VesperWeaponAction getWeaponAction() {
		return VesperWeaponAction.values()[safeActionOrdinal(entityData.get(DATA_WEAPON_ACTION))];
	}
	public int getActionTick() { return entityData.get(DATA_ACTION_TICK); }
	public int getActionVariant() { return entityData.get(DATA_ACTION_VARIANT); }
	public boolean isRaging() { return entityData.get(DATA_RAGING); }
	VesperWeaponAction getLastWeaponAction() { return lastWeaponAction; }
	Vec3 getLockedActionAim() { return lockedActionAim; }
	Vec3 getLockedActionOrigin() { return lockedActionOrigin; }
	int getActionCooldown() { return actionCooldown; }
	int getActionHitMask() { return actionHitMask; }
	void setActionTick(int tick) { entityData.set(DATA_ACTION_TICK, tick); }
	void setActionCooldown(int ticks) { actionCooldown = Math.max(0, ticks); }
	void setActionHitMask(int mask) { actionHitMask = mask; }

	void beginWeaponAction(VesperWeaponAction action, Vec3 aim) {
		lastWeaponAction = action;
		lockedActionAim = aim;
		lockedActionOrigin = position();
		actionHitMask = 0;
		entityData.set(DATA_WEAPON_ACTION, action.ordinal());
		entityData.set(DATA_ACTION_TICK, 0);
		entityData.set(DATA_ACTION_VARIANT, getActionVariant() + 1);
	}

	void clearWeaponAction() {
		entityData.set(DATA_WEAPON_ACTION, VesperWeaponAction.NONE.ordinal());
		entityData.set(DATA_ACTION_TICK, 0);
		actionHitMask = 0;
	}

	public boolean canBeAbsorbedBy(LivingEntity user) {
		if (level().isClientSide) return isAwaitingAbsorption();
		return isAwaitingAbsorption() && !ordealResolved && user instanceof ServerPlayer player
				&& ordealOwner != null && ordealOwner.equals(player.getUUID());
	}

	public float absorbWithBlood(ServerPlayer player, float progress) {
		if (!canBeAbsorbedBy(player) || progress <= 0.0F) return 0.0F;
		float before = defeatAbsorptionProgress;
		defeatAbsorptionProgress = VesperCombatRules.advanceDefeatAbsorption(before, progress);
		if (VesperCombatRules.isDefeatAbsorptionComplete(defeatAbsorptionProgress)) {
			completeBloodAbsorption();
		}
		return defeatAbsorptionProgress - before;
	}

	private void enterAwaitingAbsorption() {
		VesperPhaseTwoCombat.cancel(this);
		entityData.set(DATA_AWAITING_ABSORPTION, true);
		entityData.set(DATA_DOWNED_TICKS, 0);
		setHealth(1.0F);
		setNoAi(true);
		setTarget(null);
		navigation.stop();
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		EndgameBossActions.clearVesperPuppets(this);
		ensureSilentArchonShame();
		bossEvent.setName(Component.literal("Vesper, the Shamed Evening Star"));
		playSound(SoundEvents.BEACON_DEACTIVATE, 1.4F, 0.45F);
	}

	private void ensureSilentArchonShame() {
		if (!hasEffect(EffectInit.monolithic_dislocation)) {
			addEffect(new MobEffectInstance(EffectInit.monolithic_dislocation, Integer.MAX_VALUE,
					0, false, false, false));
		}
	}

	private void completeBloodAbsorption() {
		if (ordealResolved || level().isClientSide) return;
		ordealResolved = true;
		bossEvent.removeAllPlayers();
		EndgameBossActions.clearVesperPuppets(this);
		VesperOrdealManager.completeVictory(this);
		discard();
	}

	public ItemStack getLivingWeaponStack() {
		if (isRaging()) return new ItemStack(ItemInit.living_sickle.get());
		return new ItemStack(switch (VesperCombatRules.profile(getActiveTendency()).weapon()) {
			case "blade" -> ItemInit.living_blade.get();
			case "axe" -> ItemInit.living_axe.get();
			case "spear" -> ItemInit.living_spear.get();
			case "claws" -> ItemInit.living_baghnakh.get();
			case "crossbow" -> ItemInit.living_crossbow.get();
			case "torch" -> ItemInit.living_torch.get();
			case "flail" -> ItemInit.living_flail.get();
			default -> ItemInit.living_staff.get();
		});
	}

	private void tickTendencyCombat() {
		if (!isRaging() && VesperRageCombatRules.isRageThreshold(getHealth(), getMaxHealth())) enterRage();
		if (isRaging()) {
			VesperPhaseTwoCombat.tickRage(this);
			return;
		}
		if (tendencySeed == 0L) tendencySeed = getUUID().getMostSignificantBits() ^ getUUID().getLeastSignificantBits();
		if (stanceIndex < 0) advanceStance();
		int stanceTick = getStanceTick() + 1;
		entityData.set(DATA_STANCE_TICK, stanceTick);
		VesperPhaseTwoCombat.tick(this, getActiveTendency(), stanceTick);
		if (VesperWeaponCombatRules.mayAdvanceStance(stanceTick,
				VesperCombatRules.stanceDuration(getHealth(), getMaxHealth()), getWeaponAction())) advanceStance();
	}

	private void enterRage() {
		VesperPhaseTwoCombat.cancel(this);
		entityData.set(DATA_RAGING, true);
		entityData.set(DATA_STANCE_TICK, 30);
		setActionCooldown(14);
		bossEvent.setName(Component.translatable("entity.hemomancy.vesper_evening_star"));
		playSound(SoundEvents.WITHER_SPAWN, 1.25F, 1.45F);
		if (level() instanceof ServerLevel server) {
			server.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE,
					getX(), getY() + 1.4D, getZ(), 48, 1.0D, 1.5D, 1.0D, 0.08D);
		}
	}

	private void advanceStance() {
		VesperPhaseTwoCombat.cancel(this);
		stanceIndex++;
		entityData.set(DATA_TENDENCY, VesperCombatRules.tendencyAt(tendencySeed, stanceIndex).ordinal());
		entityData.set(DATA_STANCE_TICK, 0);
		playSound(SoundEvents.BEACON_POWER_SELECT, 1.15F, 0.7F + getActiveTendency().ordinal() * 0.055F);
	}

	private static int safeActionOrdinal(int ordinal) {
		return Math.max(0, Math.min(VesperWeaponAction.values().length - 1, ordinal));
	}
}
