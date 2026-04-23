package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class ToothPecksEntity extends PathfinderMob {

    public static final int MAX_FEED_COUNT = 5;

    private static final EntityDataAccessor<Boolean> LATCHED =
            SynchedEntityData.defineId(ToothPecksEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FEED_COUNT =
            SynchedEntityData.defineId(ToothPecksEntity.class, EntityDataSerializers.INT);

    private int detachHits = 0;
    private static final int HITS_TO_DETACH = 3;

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    public ToothPecksEntity(EntityType<? extends ToothPecksEntity> type, Level world) {
        super(type, world);
        this.setNoGravity(false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LATCHED, false);
        builder.define(FEED_COUNT, 0);
    }

    public boolean isLatched() {
        return this.entityData.get(LATCHED);
    }

    private void setLatched(boolean latched) {
        this.entityData.set(LATCHED, latched);
    }

    public int getFeedCount() {
        return this.entityData.get(FEED_COUNT);
    }

    private void setFeedCount(int count) {
        this.entityData.set(FEED_COUNT, Math.min(count, MAX_FEED_COUNT));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LatchAndFeedGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && isLatched()) {
            if (getTarget() instanceof Player player && player.isAlive()) {
                Vec3 pp = player.position();
                this.setPos(pp.x + 0.35, pp.y + 0.3, pp.z + 0.1);
                this.setDeltaMovement(Vec3.ZERO);

                if (tickCount % 40 == 0) {
                    HemoCapabilityAccess.getBloodVolume(player).ifPresent(v -> v.addDamage(1.0));
                    player.addEffect(new MobEffectInstance(EffectInit.blood_loss, 100, 0));
                    player.hurt(player.damageSources().mobAttack(this), 0.5f);
                    playSound(SoundInit.ENTITY_TOOTH_PECKS_AMBIENT.get(), 0.4F, 1.0F + random.nextFloat() * 0.2F);
                    // Grow redder and larger with each feed
                    setFeedCount(getFeedCount() + 1);
                }
            } else {
                setLatched(false);
                detachHits = 0;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isLatched() && source.getEntity() instanceof Player) {
            detachHits++;
            if (detachHits >= HITS_TO_DETACH) {
                setLatched(false);
                detachHits = 0;
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
            MobSpawnType pReason, SpawnGroupData pSpawnData) {
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Latched", isLatched());
        tag.putInt("DetachHits", detachHits);
        tag.putInt("FeedCount", getFeedCount());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setLatched(tag.getBoolean("Latched"));
        detachHits = tag.getInt("DetachHits");
        setFeedCount(tag.getInt("FeedCount"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundInit.ENTITY_TOOTH_PECKS_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundInit.ENTITY_TOOTH_PECKS_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundInit.ENTITY_TOOTH_PECKS_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.25F;
    }

    // ── Custom goal ────────────────────────────────────────────────────────────

    private static class LatchAndFeedGoal extends net.minecraft.world.entity.ai.goal.Goal {

        private final ToothPecksEntity mob;
        private Player target;

        LatchAndFeedGoal(ToothPecksEntity mob) {
            this.mob = mob;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (mob.isLatched()) return true;
            Player nearest = mob.level().getNearestPlayer(mob, 6.0);
            if (nearest == null || !nearest.isAlive()) return false;
            target = nearest;
            return mob.distanceToSqr(nearest) < 2.25;
        }

        @Override
        public boolean canContinueToUse() {
            return mob.isLatched() && mob.getTarget() instanceof Player p && p.isAlive();
        }

        @Override
        public void start() {
            if (target != null && !mob.isLatched()) {
                mob.setLatched(true);
                mob.setTarget(target);
                mob.getNavigation().stop();
            }
        }

        @Override
        public void stop() {
            target = null;
        }

        @Override
        public void tick() { }
    }
}
