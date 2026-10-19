package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentCombatRules.Attack;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The infection the Draught tears out of Annetta. It rises behind her, slithers on its tail and fights with everything
 * it grew in her: the coiled-vessel whip arm, the lance its clawed arms hold, the claws themselves, a bloom of spores
 * and roots driven up through the floor. Now and then it lashes at the host it lost.
 */
public class LatentAnnettaInfectionEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(LatentAnnettaInfectionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_TICK =
            SynchedEntityData.defineId(LatentAnnettaInfectionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EMERGE_TICK =
            SynchedEntityData.defineId(LatentAnnettaInfectionEntity.class, EntityDataSerializers.INT);

    /** Actions are the attack ordinals offset by one; zero is idle. */
    public static final int ACTION_NONE = 0;
    private static final ParticleColor BLOOD_COLOUR = new ParticleColor(140, 12, 26);
    private static final ParticleOptions BLOOD = HemoParticleData.bloodCell(BLOOD_COLOUR);
    private static final ParticleOptions ICHOR = HemoParticleData.bloodCell(new ParticleColor(214, 150, 48));
    private static final ParticleOptions SPORE = HemoParticleData.sporiticSpore(new ParticleColor(200, 170, 96));

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hemomancy.latent_annetta_infection"),
            BossEvent.BossBarColor.WHITE,
            BossEvent.BossBarOverlay.NOTCHED_10);

    @Nullable private UUID curedAnnetta;
    private int cooldown = 30;
    private int bloomCooldown = 120;
    private int rootsCooldown = 80;
    private int sinceAnnetta;
    private boolean lashingAnnetta;
    @Nullable private Vec3 rootPoint;

    public LatentAnnettaInfectionEntity(EntityType<? extends LatentAnnettaInfectionEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 160;
        this.entityData.set(DATA_EMERGE_TICK, LatentCombatRules.EMERGE_END);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        // The tail trails and the whip arm lashes well past the body box.
        return super.getBoundingBoxForCulling().inflate(5.0D, 1.0D, 5.0D);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 360.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.ARMOR, 8.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTION, ACTION_NONE);
        builder.define(DATA_ACTION_TICK, 0);
        builder.define(DATA_EMERGE_TICK, 0);
    }

    public int getAction() {
        return this.entityData.get(DATA_ACTION);
    }

    @Nullable
    public Attack getAttack() {
        int action = getAction();
        return action <= 0 || action > Attack.values().length ? null : Attack.values()[action - 1];
    }

    public int getActionTick() {
        return this.entityData.get(DATA_ACTION_TICK);
    }

    public int getEmergeTick() {
        return this.entityData.get(DATA_EMERGE_TICK);
    }

    public boolean isEmerging() {
        return getEmergeTick() < LatentCombatRules.EMERGE_END;
    }

    /** Hidden in Annetta until the purge tears it out, then rising behind her. */
    @Override
    protected boolean shouldDespawnInPeaceful() {
        // Annetta's encounter is authored, not a spawn: Peaceful keeps what rises out of her as it keeps her.
        return false;
    }

    public void beginEmerging() {
        this.entityData.set(DATA_EMERGE_TICK, 0);
    }

    public void setCuredAnnetta(UUID annettaUUID) {
        this.curedAnnetta = annettaUUID;
    }

    @Nullable
    private AnnettaKnowlesEntity annetta(ServerLevel server) {
        return curedAnnetta != null && server.getEntity(curedAnnetta) instanceof AnnettaKnowlesEntity annetta ? annetta : null;
    }

    private void setAction(@Nullable Attack attack) {
        this.entityData.set(DATA_ACTION, attack == null ? ACTION_NONE : attack.ordinal() + 1);
        this.entityData.set(DATA_ACTION_TICK, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (curedAnnetta != null) tag.putUUID("CuredAnnetta", curedAnnetta);
        tag.putInt("EmergeTick", getEmergeTick());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        curedAnnetta = tag.hasUUID("CuredAnnetta") ? tag.getUUID("CuredAnnetta") : null;
        this.entityData.set(DATA_EMERGE_TICK, tag.contains("EmergeTick") ? tag.getInt("EmergeTick") : LatentCombatRules.EMERGE_END);
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

    // ---------------------------------------------------------------- ticking

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            if (!this.isInvisible() && this.deathTime == 0) ambientSeep();
            return;
        }
        ServerLevel server = (ServerLevel) this.level();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setVisible(getEmergeTick() >= LatentCombatRules.EMERGE_HIDDEN);
        if (isEmerging()) {
            tickEmerging(server);
            return;
        }
        if (this.tickCount % 12 == 0) {
            server.sendParticles(SPORE, this.getX(), this.getY() + 2.0D, this.getZ(), 4, 0.6D, 1.2D, 0.6D, 0.01D);
        }
        sinceAnnetta++;
        bloomCooldown--;
        rootsCooldown--;
        LivingEntity target = acquireTarget(server);
        Attack attack = getAttack();
        if (attack != null) {
            int tick = getActionTick() + 1;
            this.entityData.set(DATA_ACTION_TICK, tick);
            perform(server, target, attack, tick);
            if (tick >= LatentCombatRules.duration(attack)) {
                cooldown = LatentCombatRules.cooldown(attack);
                lashingAnnetta = false;
                setAction(null);
            }
            return;
        }
        if (target == null) {
            this.getNavigation().stop();
            return;
        }
        face(target);
        double distance = this.distanceTo(target);
        if (distance > 3.2D) this.getNavigation().moveTo(target, 1.0D);
        else this.getNavigation().stop();
        if (--cooldown > 0) return;
        AnnettaKnowlesEntity host = annetta(server);
        if (host != null && host.isCuredSupport() && host.distanceTo(this) < 7.0F
                && LatentCombatRules.turnOnAnnetta(this.random.nextInt(100), sinceAnnetta)) {
            sinceAnnetta = 0;
            lashingAnnetta = true;
            face(host);
            setAction(Attack.WHIP_LASH);
            return;
        }
        Attack next = LatentCombatRules.choose(distance, this.random.nextInt(100), bloomCooldown <= 0, rootsCooldown <= 0);
        if (next == Attack.INFECTION_BLOOM) bloomCooldown = 160;
        if (next == Attack.ROOT_ERUPTION) {
            rootsCooldown = 140;
            rootPoint = target.position();
        }
        setAction(next);
    }

    /** Client only: blood drips off the chest maw and the whip arm, and spores drift up out of the hood. */
    private void ambientSeep() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        double forwardX = -Mth.sin(yaw), forwardZ = Mth.cos(yaw);
        double rise = isEmerging() ? LatentCombatRules.rise(getEmergeTick()) : 1.0D;
        double base = this.getY() - (1.0D - rise) * 4.8D;
        if (this.random.nextInt(3) == 0) {
            this.level().addParticle(this.random.nextInt(5) == 0 ? ICHOR : BLOOD,
                    this.getX() + forwardX * 0.55D + (this.random.nextDouble() - 0.5D) * 0.4D, base + 2.9D + this.random.nextDouble() * 0.5D,
                    this.getZ() + forwardZ * 0.55D + (this.random.nextDouble() - 0.5D) * 0.4D, 0.0D, -0.04D, 0.0D);
        }
        if (this.random.nextInt(4) == 0) {
            // The whip arm hangs off its left shoulder.
            this.level().addParticle(BLOOD, this.getX() + forwardZ * 1.1D + forwardX * 0.3D, base + 2.2D + this.random.nextDouble() * 0.8D,
                    this.getZ() - forwardX * 1.1D + forwardZ * 0.3D, 0.0D, -0.05D, 0.0D);
        }
        if (this.random.nextInt(6) == 0) {
            this.level().addParticle(SPORE, this.getX() + forwardX * 0.4D + (this.random.nextDouble() - 0.5D) * 0.5D, base + 4.4D,
                    this.getZ() + forwardZ * 0.4D + (this.random.nextDouble() - 0.5D) * 0.5D, 0.0D, 0.015D, 0.0D);
        }
    }

    private void tickEmerging(ServerLevel server) {
        int tick = getEmergeTick() + 1;
        this.entityData.set(DATA_EMERGE_TICK, tick);
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
        if (tick >= LatentCombatRules.EMERGE_HIDDEN) {
            server.sendParticles(BLOOD, getX(), getY() + 0.2D, getZ(), 6, 0.9D, 0.1D, 0.9D, 0.06D);
            server.sendParticles(SPORE, getX(), getY() + 1.0D, getZ(), 4, 0.8D, 0.8D, 0.8D, 0.02D);
        }
        if (tick == LatentCombatRules.EMERGE_HIDDEN) {
            server.playSound(null, blockPosition(), SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 1.4F, 0.8F);
        }
        if (tick == LatentCombatRules.EMERGE_END) {
            server.playSound(null, blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 1.2F, 0.9F);
        }
    }

    @Nullable
    private LivingEntity acquireTarget(ServerLevel server) {
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !(target instanceof AnnettaKnowlesEntity)
                && target.distanceToSqr(this) < 48.0D * 48.0D
                && !(target instanceof Player p && (p.isSpectator() || p.isCreative()))) {
            return target;
        }
        Player nearest = server.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 40.0D,
                entity -> entity instanceof Player p && !p.isSpectator() && !p.isCreative() && p.isAlive());
        this.setTarget(nearest);
        return nearest;
    }

    private void face(LivingEntity target) {
        Vec3 delta = target.position().subtract(position());
        float yaw = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
        this.setYRot(Mth.rotLerp(0.25F, this.getYRot(), yaw));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    private Vec3 facing() {
        return Vec3.directionFromRotation(0.0F, this.yBodyRot);
    }

    private void perform(ServerLevel server, @Nullable LivingEntity target, Attack attack, int tick) {
        int hit = LatentCombatRules.hitTick(attack);
        this.getNavigation().stop();
        switch (attack) {
            case WHIP_LASH -> {
                if (tick == hit) whipLash(server);
            }
            case SPEAR_THRUST -> {
                if (tick >= hit - 2 && tick <= hit + 2) this.setDeltaMovement(facing().scale(0.9D));
                if (tick == hit) spearThrust(server);
            }
            case CLAW_RAKE -> {
                if (tick == hit || tick == hit + 8) arcHit(server, 3.9D, 0.0D, 7.0F, 0.5D, false);
            }
            case INFECTION_BLOOM -> {
                if (tick == hit) infectionBloom(server);
            }
            case ROOT_ERUPTION -> {
                if (rootPoint != null && tick < hit && tick % 3 == 0) telegraph(server, rootPoint);
                if (tick == hit && rootPoint != null) roots(server, rootPoint);
            }
        }
    }

    /** The coiled vessels uncoil into a whip and sweep almost all the way round it. */
    private void whipLash(ServerLevel server) {
        server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.4F, 0.4F);
        Vec3 facing = facing();
        for (int i = -6; i <= 6; i++) {
            Vec3 dir = facing.yRot(i * 0.22F);
            Vec3 at = position().add(dir.scale(5.5D)).add(0.0D, 1.6D, 0.0D);
            server.sendParticles(BLOOD, at.x, at.y, at.z, 2, 0.15D, 0.15D, 0.15D, 0.02D);
        }
        if (lashingAnnetta) {
            AnnettaKnowlesEntity host = annetta(server);
            if (host != null && host.distanceTo(this) < 7.5F) host.staggerFrom(this);
            return;
        }
        arcHit(server, 7.0D, -0.15D, 9.0F, 0.9D, true);
    }

    private void spearThrust(ServerLevel server) {
        server.playSound(null, blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 1.2F, 0.5F);
        Vec3 facing = facing();
        for (LivingEntity victim : victims(server, 6.5D)) {
            Vec3 toward = victim.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            if (toward.lengthSqr() < 1.0E-4D || facing.dot(toward.normalize()) < 0.8D) continue;
            if (victim.hurt(this.damageSources().mobAttack(this), 12.0F)) {
                victim.knockback(0.8D, -facing.x, -facing.z);
            }
        }
        PacketHandler.sendClawSlash(position().add(facing.scale(3.0D)).add(0.0D, 1.5D, 0.0D), facing, BLOOD_COLOUR,
                false, 0.8F, 48.0D, server);
    }

    private void arcHit(ServerLevel server, double reach, double minDot, float damage, double knock, boolean bleed) {
        Vec3 facing = facing();
        for (LivingEntity victim : victims(server, reach)) {
            Vec3 toward = victim.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            if (toward.lengthSqr() < 1.0E-4D || facing.dot(toward.normalize()) < minDot) continue;
            if (victim.hurt(this.damageSources().mobAttack(this), damage)) {
                Vec3 push = toward.normalize();
                victim.knockback(knock, -push.x, -push.z);
                if (bleed) victim.addEffect(new MobEffectInstance(EffectInit.blood_loss, 80, 0, false, true));
            }
        }
        if (!bleed) {
            PacketHandler.sendClawSlash(position().add(facing.scale(2.0D)).add(0.0D, 1.4D, 0.0D), facing, BLOOD_COLOUR,
                    false, 0.7F, 48.0D, server);
            server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.HOSTILE, 1.0F, 0.6F);
        }
    }

    private java.util.List<LivingEntity> victims(ServerLevel server, double reach) {
        return server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(reach, 1.5D, reach),
                e -> e != this && e.isAlive() && !(e instanceof AnnettaKnowlesEntity)
                        && e.distanceTo(this) <= reach + 1.0D);
    }

    private void infectionBloom(ServerLevel server) {
        for (int i = 0; i < 32; i++) {
            double a = i * Math.PI * 2.0D / 32.0D;
            server.sendParticles(SPORE, getX() + Math.cos(a) * 6.0D, getY() + 0.1D, getZ() + Math.sin(a) * 6.0D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        server.sendParticles(SPORE, getX(), getY() + 1.5D, getZ(), 70, 2.5D, 1.0D, 2.5D, 0.04D);
        server.sendParticles(ICHOR, getX(), getY() + 1.5D, getZ(), 24, 1.5D, 0.8D, 1.5D, 0.06D);
        server.playSound(null, this.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 1.0F, 0.7F);
        for (LivingEntity victim : victims(server, 6.0D)) {
            victim.hurt(this.damageSources().magic(), 5.0F);
            victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0, false, true));
        }
    }

    private void telegraph(ServerLevel server, Vec3 at) {
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI * 2.0D / 10.0D;
            server.sendParticles(ICHOR, at.x + Math.cos(a) * 1.6D, at.y + 0.05D, at.z + Math.sin(a) * 1.6D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    /** Roots it grew down through her come up through the floor under the target. */
    private void roots(ServerLevel server, Vec3 at) {
        server.sendParticles(BLOOD, at.x, at.y + 0.5D, at.z, 40, 0.5D, 1.2D, 0.5D, 0.08D);
        server.sendParticles(SPORE, at.x, at.y + 1.0D, at.z, 24, 0.6D, 1.0D, 0.6D, 0.03D);
        server.playSound(null, at.x, at.y, at.z, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.6F, 0.5F);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(1.8D, 2.0D, 1.8D),
                e -> e != this && e.isAlive() && !(e instanceof AnnettaKnowlesEntity))) {
            if (victim.hurt(this.damageSources().mobAttack(this), 8.0F)) {
                victim.push(0.0D, 0.6D, 0.0D);
                victim.hurtMarked = true;
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, true));
            }
        }
    }

    // ---------------------------------------------------------------- damage

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        if (getEmergeTick() < LatentCombatRules.EMERGE_HIDDEN + 10) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean isInvisible() {
        return getEmergeTick() < LatentCombatRules.EMERGE_HIDDEN || super.isInvisible();
    }

    @Override
    public boolean isPushable() {
        return !isEmerging() && super.isPushable();
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0, false, true));
        }
        return result;
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel server) {
			if (curedAnnetta != null
					&& server.getEntity(curedAnnetta) instanceof AnnettaKnowlesEntity annetta) {
				if (annetta.isCuredSupport()) annetta.markResolvedAfterCure();
				else if (annetta.isPurging()) annetta.infectionDestroyed();
			}
        }
        super.die(source);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(new ItemStack(ItemInit.annettas_absolution_dagger.get()));
        this.spawnAtLocation(new ItemStack(ItemInit.pale_silver_ingot.get(), 3));
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return isEmerging() ? null : SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }
}
