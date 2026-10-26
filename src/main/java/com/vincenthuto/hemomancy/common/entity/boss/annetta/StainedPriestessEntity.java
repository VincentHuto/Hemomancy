package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules.Mode;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Annetta's body, worn by the infection. She rises out of the blood with tendril wings and a thorned head, pulls the
 * lancea from the pool, then fights from the air: volleys of blood and spore, swoops with the spear, landings for
 * spear combos, wings wrapped round her as a shield and flung open as a sickly wind.
 */
public class StainedPriestessEntity extends Monster {
    private static final BloodAccounting.Source TENDRIL_DRAIN = BloodAccounting.Source.of("stained_priestess_drain", Category.HAZARD, false, false);
    private static final EntityDataAccessor<Integer> DATA_MODE =
            SynchedEntityData.defineId(StainedPriestessEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MODE_TICK =
            SynchedEntityData.defineId(StainedPriestessEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(StainedPriestessEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_TICK =
            SynchedEntityData.defineId(StainedPriestessEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_WING =
            SynchedEntityData.defineId(StainedPriestessEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(StainedPriestessEntity.class, EntityDataSerializers.BOOLEAN);

    private static final ParticleColor BLOOD_COLOUR = new ParticleColor(150, 14, 26);
    private static final ParticleOptions BLOOD = HemoParticleData.bloodCell(BLOOD_COLOUR);
    private static final ParticleOptions CLOT = HemoParticleData.bloodCell(new ParticleColor(96, 8, 20));
    private static final ParticleOptions ICHOR = HemoParticleData.bloodCell(new ParticleColor(214, 150, 48));
    private static final ParticleOptions SPORE = HemoParticleData.sporiticSpore(new ParticleColor(196, 160, 80));
    private static final ParticleOptions GLOW = HemoParticleData.glow(new ParticleColor(220, 40, 52));
    private static final ParticleOptions HIT = HemoParticleData.hitGlow(BLOOD_COLOUR);

    public static final int ACTION_NONE = 0;
    public static final int ACTION_VOLLEY = 1;
    public static final int ACTION_LANCE = 2;
    public static final int ACTION_COMBO = 3;
    /** Down on one knee after a plummet, the spear sunk in the floor. */
    public static final int ACTION_PLANTED = 4;
    private static final int VOLLEY_TICKS = 20;
    private static final int BLOOD_DRAIN = 300;

    private final BossBarBinding bossBar = new BossBarBinding("entity.hemomancy.stained_priestess", BossPresentationIds.FRAME_STAINED_PRIESTESS, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_12);
    // Client clocks: the synced counters arrive on the network's schedule, not every client tick, so the model and its
    // layers read these instead. They advance with the client's own ticks and resync when the server disagrees;
    // each read syncs, so the first frame after loading already reads the synced value.
    private final BossReplayClock modeClock = new BossReplayClock();
    private final BossReplayClock actionClock = new BossReplayClock(ACTION_NONE);

    private int modeLength = PriestessCombatRules.EMERGE_LENGTH;
    private float orbitAngle;
    private float recentDamage;
    private int shieldCooldown = 200;
    private int comboCooldown;
    private boolean swoopHit;
    @Nullable private Vec3 swoopAim;
    @Nullable private Vec3 plummetAim;

    public StainedPriestessEntity(EntityType<? extends StainedPriestessEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 180;
        equipLancea();
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        // A Priestess that did not rise from Annetta (a spawn egg, a command) starts by taking to the air.
        if (!level.isClientSide) setMode(Mode.TAKEOFF);
    }

    /** The lancea in her hand is drawn, not looted; her own drop is spawned on death. */
    private void equipLancea() {
        ItemStack lancea = new ItemStack(ItemInit.annettas_sanguis_lancea.get());
        // Visual only: without this the lancea's +6 attack would stack on her documented 12.
        lancea.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        this.setItemSlot(EquipmentSlot.MAINHAND, lancea);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        // The wings spread about five blocks either side of the body.
        return super.getBoundingBoxForCulling().inflate(5.5D, 2.5D, 5.5D);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 420.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ARMOR, 10.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MODE, Mode.TAKEOFF.ordinal());
        builder.define(DATA_MODE_TICK, 0);
        builder.define(DATA_ACTION, ACTION_NONE);
        builder.define(DATA_ACTION_TICK, 0);
        builder.define(DATA_WING, PriestessCombatRules.Wing.SPREAD.ordinal());
        builder.define(DATA_FLYING, false);
    }

    // ---------------------------------------------------------------- synced state

    public Mode getMode() {
        return Mode.values()[Math.floorMod(this.entityData.get(DATA_MODE), Mode.values().length)];
    }

    public int getModeTick() {
        return this.entityData.get(DATA_MODE_TICK);
    }

    public int getAction() {
        return this.entityData.get(DATA_ACTION);
    }

    public int getActionTick() {
        return this.entityData.get(DATA_ACTION_TICK);
    }

    public PriestessCombatRules.Wing getWing() {
        return PriestessCombatRules.Wing.values()[Math.floorMod(this.entityData.get(DATA_WING), 3)];
    }

    public boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    /** Ticks into the current flight mode, smooth across frames on the client. */
    public float modeTime(float partialTick) {
        if (!this.level().isClientSide || AnnettaKnowlesEntity.reviewHoldClock) return getModeTick();
        return modeClock.sample(getMode().ordinal(), getModeTick(), this.tickCount, partialTick, 1);
    }

    /** Ticks into the current action, smooth across frames on the client. */
    public float actionTime(float partialTick) {
        if (!this.level().isClientSide || AnnettaKnowlesEntity.reviewHoldClock) return getActionTick();
        return actionClock.sample(getAction(), getActionTick(), this.tickCount, partialTick, 1);
    }

    /** How grown the tendril wings are (0 while she lies in the pool, 1 once she stands). */
    public float wingGrowth(float partialTick) {
        return getMode() == Mode.EMERGING ? PriestessCombatRules.wingGrowth(modeTime(partialTick)) : 1.0F;
    }

    public float headGrowth(float partialTick) {
        return getMode() == Mode.EMERGING ? PriestessCombatRules.headGrowth(modeTime(partialTick)) : 1.0F;
    }

    private void setMode(Mode mode) {
        this.entityData.set(DATA_MODE, mode.ordinal());
        this.entityData.set(DATA_MODE_TICK, 0);
        this.modeLength = PriestessCombatRules.length(mode, this.random.nextInt(60));
        this.swoopAim = null;
        this.plummetAim = null;
        this.swoopHit = false;
        setFlying(PriestessCombatRules.airborne(mode)
                || ((mode == Mode.SHIELD || mode == Mode.WIND_BLAST) && isFlying()));
    }

    private void setFlying(boolean flying) {
        this.entityData.set(DATA_FLYING, flying);
        this.setNoGravity(flying);
    }

    private void setAction(int action) {
        this.entityData.set(DATA_ACTION, action);
        this.entityData.set(DATA_ACTION_TICK, 0);
    }

    /** She lies where Annetta fell and rises out of the reanimating blood; nothing touches her until she stands. */
    @Override
    protected boolean shouldDespawnInPeaceful() {
        // Annetta's encounter is authored, not a spawn: Peaceful keeps what rises out of her as it keeps her.
        return false;
    }

    public void beginEmerging() {
        setMode(Mode.EMERGING);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossBar.show(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.hide(player);
    }

    // ---------------------------------------------------------------- ticking

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        ServerLevel server = (ServerLevel) this.level();
        this.bossBar.trackHealth(this);
        int modeTick = getModeTick() + 1;
        this.entityData.set(DATA_MODE_TICK, modeTick);
        if (getAction() != ACTION_NONE) {
            int tick = getActionTick() + 1;
            if (tick >= actionLength(getAction())) setAction(ACTION_NONE);
            else this.entityData.set(DATA_ACTION_TICK, tick);
        }
        recentDamage *= 0.96F;
        shieldCooldown--;
        comboCooldown--;
        if (this.tickCount % 10 == 0) {
            server.sendParticles(CLOT, this.getX(), this.getY() + 1.0D, this.getZ(), 3, 0.3D, 0.5D, 0.3D, 0.01D);
        }

        Mode mode = getMode();
        LivingEntity target = mode == Mode.EMERGING ? null : acquireTarget(server);
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 0.0025D;
        this.entityData.set(DATA_WING, PriestessCombatRules.wing(mode, moving).ordinal());

        if ((mode == Mode.HOVER || mode == Mode.LANDED) && recentDamage >= PriestessCombatRules.SHIELD_TRIGGER
                && shieldCooldown <= 0) {
            shieldCooldown = 260;
            recentDamage = 0.0F;
            setMode(Mode.SHIELD);
            server.playSound(null, blockPosition(), SoundInit.ENTITY_STAINED_PRIESTESS_FLARE.get(), SoundSource.HOSTILE, 1.4F, 1.0F);
            return;
        }

        switch (mode) {
            case EMERGING -> tickEmerging(server, modeTick);
            case TAKEOFF -> this.setDeltaMovement(this.getDeltaMovement().scale(0.6D).add(0.0D, 0.22D * (1.0F - modeTick / 16.0F) + 0.04D, 0.0D));
            case HOVER -> tickHover(server, target, modeTick);
            case SWOOP -> tickSwoop(server, target, modeTick);
            case PLUMMET -> tickPlummet(server, target, modeTick);
            case LANDED -> tickLanded(server, target);
            case SHIELD -> tickShield();
            case WIND_BLAST -> {
                tickShield();
                if (modeTick == PriestessCombatRules.WIND_BLAST_RELEASE) windBlast(server);
            }
        }
        if (target != null && mode != Mode.EMERGING) face(target);
        if (modeTick >= modeLength) finishMode(target);
    }

    private int actionLength(int action) {
        return switch (action) {
            case ACTION_VOLLEY -> VOLLEY_TICKS;
            case ACTION_LANCE -> PriestessCombatRules.JAVELIN_LENGTH;
            case ACTION_COMBO -> PriestessCombatRules.COMBO_LENGTH;
            case ACTION_PLANTED -> PriestessCombatRules.PLUMMET_RECOVERY;
            default -> 1;
        };
    }

    private void finishMode(@Nullable LivingEntity target) {
        Mode finished = getMode();
        boolean far = target != null && target.distanceTo(this) > 14.0F;
        Mode next = PriestessCombatRules.next(finished, this.random.nextInt(100), far);
        if (next == Mode.HOVER && !isFlying()) next = Mode.TAKEOFF;
        setMode(next);
        if (next == Mode.LANDED) setFlying(false);
    }

    @Nullable
    private LivingEntity acquireTarget(ServerLevel server) {
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && target.distanceToSqr(this) < 48.0D * 48.0D
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
        this.setYRot(Mth.rotLerp(0.3F, this.getYRot(), yaw));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    private void tickEmerging(ServerLevel server, int tick) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
        if (tick % 4 == 0) {
            server.sendParticles(BLOOD, getX(), getY() + 0.1D, getZ(), 4, 0.8D, 0.05D, 0.8D, 0.02D);
        }
        if (tick == PriestessCombatRules.LANCEA_AT) {
            // The lancea is drawn up out of the pool, blue and red together.
            equipLancea();
            server.sendParticles(BLOOD, getX() + 0.5D, getY() + 0.2D, getZ(), 30, 0.1D, 0.8D, 0.1D, 0.12D);
            server.sendParticles(HemoParticleData.glow(new ParticleColor(150, 190, 255)), getX() + 0.5D, getY() + 0.6D, getZ(),
                    16, 0.1D, 0.7D, 0.1D, 0.05D);
            server.playSound(null, blockPosition(), SoundInit.ENTITY_STAINED_PRIESTESS_SUMMON.get(), SoundSource.HOSTILE, 1.6F, 1.0F);
        }
        if (tick == PriestessCombatRules.STAND_END) {
            server.playSound(null, blockPosition(), SoundInit.ENTITY_STAINED_PRIESTESS_SCREAM.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
        }
    }

    /** Headroom over the target, and over the side of her orbit she is on, whichever is lower. */
    private double ceilingAbove(LivingEntity target) {
        double overTarget = headroom(target, target.position());
        double overHer = headroom(target, new Vec3(getX(), target.getY(), getZ()));
        return Math.min(overTarget, overHer);
    }

    private double headroom(LivingEntity target, Vec3 base) {
        Vec3 from = base.add(0.0D, 1.0D, 0.0D);
        var hit = this.level().clip(new ClipContext(from, from.add(0.0D, 12.0D, 0.0D), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.MISS ? 13.0D : hit.getLocation().y - base.y;
    }

    /** Damped steering toward a point, like a heavy thing on wings. */
    private void flyToward(Vec3 point, double maxSpeed) {
        Vec3 delta = point.subtract(position());
        double distance = delta.length();
        Vec3 desired = distance < 1.0E-3D ? Vec3.ZERO : delta.scale(Math.min(maxSpeed, distance * 0.25D) / distance);
        this.setDeltaMovement(this.getDeltaMovement().lerp(desired, 0.18D));
        this.hasImpulse = true;
    }

    private void tickHover(ServerLevel server, @Nullable LivingEntity target, int tick) {
        if (target == null) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            return;
        }
        orbitAngle += 0.025F;
        double[] point = PriestessCombatRules.hoverPoint(target.getX(), target.getY(), target.getZ(), orbitAngle, 6.5D,
                4.5D + Mth.sin(tick * 0.05F) * 0.6D, ceilingAbove(target));
        flyToward(new Vec3(point[0], point[1], point[2]), 0.45D);
        if (getAction() == ACTION_NONE && this.hasLineOfSight(target)) {
            if (tick % 44 == 20) setAction(ACTION_VOLLEY);
            else if (tick % 97 == 60) setAction(ACTION_LANCE);
        }
        if (getAction() == ACTION_VOLLEY && getActionTick() == 10) volley(server, target);
        if (getAction() == ACTION_LANCE) {
            int t = getActionTick();
            if (t < PriestessCombatRules.JAVELIN_RELEASE) chargeJavelin(server, t);
            if (t == PriestessCombatRules.JAVELIN_RELEASE) throwJavelin(server, target);
            if (t == PriestessCombatRules.JAVELIN_REFORM_START) reformSpear(server);
        }
    }

    /** Where her right hand is, roughly, for effects: above and behind the shoulder when she draws back to throw. */
    private Vec3 throwingHand() {
        Vec3 facing = facing();
        Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);
        return position().add(right.scale(0.45D)).add(facing.scale(-0.35D)).add(0.0D, 2.25D, 0.0D);
    }

    /** Blood is drawn out of the air into the lancea as she holds the throw. */
    private void chargeJavelin(ServerLevel server, int tick) {
        if (tick < 4) return;
        Vec3 hand = throwingHand();
        for (int i = 0; i < 3; i++) {
            Vec3 from = hand.add((this.random.nextDouble() - 0.5D) * 2.6D, (this.random.nextDouble() - 0.3D) * 2.0D,
                    (this.random.nextDouble() - 0.5D) * 2.6D);
            Vec3 pull = hand.subtract(from).scale(0.16D);
            server.sendParticles(i == 0 ? GLOW : BLOOD, from.x, from.y, from.z, 0, pull.x, pull.y, pull.z, 1.0D);
        }
        if (tick == 6) server.playSound(null, blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    /** The lancea leaves her hand; badly hurt she tears two more out of her own blood and throws a fan. */
    private void throwJavelin(ServerLevel server, LivingEntity target) {
        Vec3 hand = throwingHand();
        int count = PriestessCombatRules.javelinCount(this.getHealth() / this.getMaxHealth());
        // Lead the target a little so strafing in a straight line does not dodge every throw.
        Vec3 aim = target.getEyePosition().add(target.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).scale(6.0D))
                .subtract(0.0D, 0.4D, 0.0D).subtract(hand);
        for (int i = 0; i < count; i++) {
            Vec3 dir = aim.yRot((i - (count - 1) / 2.0F) * 0.24F);
            StainedLanceEntity lance = new StainedLanceEntity(server, this);
            lance.setPos(hand.x, hand.y, hand.z);
            lance.shoot(dir.x, dir.y, dir.z, 1.9F, 0.6F);
            server.addFreshEntity(lance);
        }
        server.sendParticles(GLOW, hand.x, hand.y, hand.z, 10, 0.2D, 0.2D, 0.2D, 0.06D);
        server.sendParticles(BLOOD, hand.x, hand.y, hand.z, 18, 0.25D, 0.25D, 0.25D, 0.12D);
        server.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE, 1.6F, 0.5F);
        server.playSound(null, this.blockPosition(), SoundInit.ENTITY_STAINED_PRIESTESS_YELL.get(), SoundSource.HOSTILE, 1.4F, 1.0F);
    }

    /** The spent lancea's blood runs back and clots into a new one in her hand. */
    private void reformSpear(ServerLevel server) {
        Vec3 hand = position().add(0.0D, 1.3D, 0.0D);
        server.sendParticles(CLOT, hand.x, hand.y, hand.z, 14, 0.4D, 0.4D, 0.4D, 0.02D);
        server.playSound(null, blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.HOSTILE, 1.0F, 0.7F);
    }


    private void volley(ServerLevel server, LivingEntity target) {
        Vec3 from = position().add(0.0D, 1.5D, 0.0D);
        Vec3 aim = target.getEyePosition().subtract(from);
        for (int i = -1; i <= 1; i++) {
            InfectedBoltEntity.Kind kind = i == 0 ? InfectedBoltEntity.Kind.SPORE : InfectedBoltEntity.Kind.BLOOD;
            InfectedBoltEntity bolt = new InfectedBoltEntity(server, this, kind);
            bolt.setPos(from.x, from.y, from.z);
            Vec3 dir = aim.yRot(i * 0.16F);
            bolt.shoot(dir.x, dir.y + (kind == InfectedBoltEntity.Kind.SPORE ? dir.horizontalDistance() * 0.08D : 0.0D), dir.z,
                    kind == InfectedBoltEntity.Kind.SPORE ? 0.75F : 1.0F, 2.0F);
            server.addFreshEntity(bolt);
        }
        server.playSound(null, blockPosition(), SoundEvents.SLIME_ATTACK, SoundSource.HOSTILE, 1.2F, 0.5F);
    }

    /** Pull up and lock on, dive through where they stood, then climb back into the air. */
    private void tickSwoop(ServerLevel server, @Nullable LivingEntity target, int tick) {
        if (tick < PriestessCombatRules.SWOOP_TELEGRAPH) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.7D).add(0.0D, 0.06D, 0.0D));
            if (target != null) swoopAim = target.position().add(0.0D, 0.6D, 0.0D);
            if (swoopAim != null && tick % 3 == 0) telegraph(server, swoopAim);
            if (tick == 2) {
                server.playSound(null, blockPosition(), SoundEvents.PHANTOM_SWOOP, SoundSource.HOSTILE, 1.5F, 0.6F);
                server.playSound(null, blockPosition(), SoundInit.ENTITY_STAINED_PRIESTESS_YELL.get(), SoundSource.HOSTILE, 1.6F, 0.9F);
            }
            return;
        }
        if (tick < PriestessCombatRules.SWOOP_DIVE_END && swoopAim != null) {
            Vec3 through = swoopAim.add(swoopAim.subtract(position()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(3.0D));
            Vec3 delta = through.subtract(position());
            this.setDeltaMovement(delta.normalize().scale(1.35D));
            this.hasImpulse = true;
            server.sendParticles(BLOOD, getX(), getY() + 1.0D, getZ(), 3, 0.3D, 0.3D, 0.3D, 0.02D);
            if (!swoopHit) {
                for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(1.2D),
                        e -> e != this && e.isAlive())) {
                    if (victim.hurt(this.damageSources().mobAttack(this), 14.0F)) {
                        swoopHit = true;
                        Vec3 push = getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize();
                        victim.push(push.x * 1.2D, 0.45D, push.z * 1.2D);
                        drain(victim);
                        PacketHandler.sendClawSlash(victim.position().add(0.0D, 1.0D, 0.0D), getDeltaMovement().normalize(),
                                BLOOD_COLOUR, false, 1.3F, 48.0D, server);
                        server.sendParticles(HIT, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.05D);
                        server.sendParticles(BLOOD, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 24, 0.3D, 0.4D, 0.3D, 0.18D);
                        server.playSound(null, victim.blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 1.4F, 0.5F);
                        // Run through, the blood behind the wound wakes: a thorn bursts where they were struck.
                        PriestessEruptionEntity.spawn(server, this, victim.position(), PriestessCombatRules.ERUPTION_TELL / 2, false, 6.0F);
                    }
                }
            }
            if (this.verticalCollision && this.random.nextBoolean()) {
                setMode(Mode.LANDED);
                setFlying(false);
            }
            return;
        }
        if (target != null) {
            double[] point = PriestessCombatRules.hoverPoint(target.getX(), target.getY(), target.getZ(), orbitAngle, 6.5D,
                    4.5D, ceilingAbove(target));
            flyToward(new Vec3(point[0], point[1], point[2]), 0.55D);
        }
    }

    private void telegraph(ServerLevel server, Vec3 at) {
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI * 2.0D / 12.0D;
            server.sendParticles(ICHOR, at.x + Math.cos(a) * 1.4D, at.y - 0.5D, at.z + Math.sin(a) * 1.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void tickLanded(ServerLevel server, @Nullable LivingEntity target) {
        if (getAction() == ACTION_PLANTED || target == null) {
            this.getNavigation().stop();
            return;
        }
        double distance = this.distanceTo(target);
        if (getAction() == ACTION_COMBO) {
            this.getNavigation().stop();
            tickCombo(server, target, getActionTick());
            return;
        }
        if (distance > 2.8D) this.getNavigation().moveTo(target, 1.15D);
        else this.getNavigation().stop();
        if (distance < 4.2D && comboCooldown <= 0 && getAction() == ACTION_NONE) {
            setAction(ACTION_COMBO);
            comboCooldown = PriestessCombatRules.COMBO_LENGTH + 20;
        }
    }

    /** The spear combo's blows land on the ticks the model strikes them. */
    private void tickCombo(ServerLevel server, LivingEntity target, int t) {
        if (t == PriestessCombatRules.LUNGE_WIND_END) {
            this.setDeltaMovement(facing().scale(0.9D).add(0.0D, 0.04D, 0.0D));
            this.hasImpulse = true;
            server.playSound(null, blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.HOSTILE, 1.0F, 1.2F);
        }
        if (t == PriestessCombatRules.LUNGE_HIT) lunge(server, target);
        if (t == PriestessCombatRules.REAP_START) {
            server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.4F, 0.4F);
        }
        if (t == PriestessCombatRules.REAP_HIT) reap(server);
        if (t == PriestessCombatRules.LEAP_JUMP) {
            Vec3 toward = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            // Land a couple of blocks short, so the plunge's thorn line runs out into them, not behind.
            double reach = Mth.clamp((toward.length() - 2.0D) / 5.0D, 0.0D, 1.0D);
            Vec3 hop = toward.lengthSqr() < 1.0E-4D ? Vec3.ZERO : toward.normalize().scale(0.36D * reach);
            this.setDeltaMovement(hop.x, 0.6D, hop.z);
            this.hasImpulse = true;
            server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 1.2F, 1.3F);
            server.sendParticles(BLOOD, getX(), getY() + 0.1D, getZ(), 16, 0.6D, 0.05D, 0.6D, 0.06D);
        }
        if (t == PriestessCombatRules.PLUNGE_DRIVE) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.3D, 0.0D, 0.3D).add(0.0D, -1.4D, 0.0D));
            this.hasImpulse = true;
        }
        if (t == PriestessCombatRules.PLUNGE_HIT) plunge(server, target);
    }

    private Vec3 facing() {
        return Vec3.directionFromRotation(0.0F, this.yBodyRot);
    }

    private boolean strike(LivingEntity target, float damage, double reach, double minDot, double knock) {
        Vec3 toward = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        if (toward.length() > reach || toward.lengthSqr() < 1.0E-4D || facing().dot(toward.normalize()) < minDot) return false;
        if (!target.hurt(this.damageSources().mobAttack(this), damage)) return false;
        Vec3 push = toward.normalize();
        target.knockback(knock, -push.x, -push.z);
        drain(target);
        return true;
    }

    /** Driven in along the shaft: long reach, a narrow line, and a spray out of the back of whatever it runs through. */
    private void lunge(ServerLevel server, LivingEntity target) {
        Vec3 facing = facing();
        Vec3 tip = position().add(facing.scale(3.2D)).add(0.0D, 1.25D, 0.0D);
        boolean hit = strike(target, 11.0F, 4.8D, 0.75D, 0.6D);
        server.playSound(null, blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 1.2F, hit ? 0.5F : 0.8F);
        PacketHandler.sendClawSlash(position().add(facing.scale(2.0D)).add(0.0D, 1.25D, 0.0D), facing, BLOOD_COLOUR, false, 1.1F, 48.0D, server);
        for (int i = 0; i < 10; i++) {
            Vec3 at = position().add(facing.scale(0.8D + i * 0.32D)).add(0.0D, 1.25D, 0.0D);
            server.sendParticles(GLOW, at.x, at.y, at.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
        Vec3 spray = facing.scale(0.35D);
        server.sendParticles(BLOOD, tip.x, tip.y, tip.z, 0, spray.x, 0.05D, spray.z, 1.0D);
        if (hit) server.sendParticles(BLOOD, target.getX(), target.getY() + 1.0D, target.getZ(), 26, 0.2D, 0.3D, 0.2D, 0.2D);
    }

    /** A full turn at arm's length: everything around her is cut, whichever way it stood. */
    private void reap(ServerLevel server) {
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.4D, 1.0D, 4.4D),
                e -> e != this && e.isAlive() && !(e instanceof StainedPriestessEntity))) {
            if (victim.distanceTo(this) > 4.6D) continue;
            Vec3 out = victim.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            if (victim.hurt(this.damageSources().mobAttack(this), 9.0F) && out.lengthSqr() > 1.0E-4D) {
                Vec3 push = out.normalize();
                victim.knockback(1.0D, -push.x, -push.z);
                drain(victim);
                server.sendParticles(BLOOD, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 16, 0.2D, 0.3D, 0.2D, 0.14D);
            }
        }
        for (int i = 0; i < 28; i++) {
            double a = i * Math.PI * 2.0D / 28.0D;
            double r = 3.4D;
            server.sendParticles(i % 2 == 0 ? GLOW : BLOOD, getX() + Math.cos(a) * r, getY() + 1.1D, getZ() + Math.sin(a) * r,
                    0, -Math.sin(a) * 0.25D, 0.0D, Math.cos(a) * 0.25D, 1.0D);
        }
        for (int i = 0; i < 4; i++) {
            Vec3 dir = facing().yRot(i * Mth.HALF_PI);
            PacketHandler.sendClawSlash(position().add(dir.scale(2.4D)).add(0.0D, 1.1D, 0.0D), dir.yRot(Mth.HALF_PI),
                    BLOOD_COLOUR, false, 1.2F, 48.0D, server);
        }
        server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.6F, 0.55F);
    }

    /**
     * The spear goes into the floor point first and the blood under it answers: a line of thorns bursts out toward
     * the target, one after another, with a ring round the spear itself.
     */
    private void plunge(ServerLevel server, LivingEntity target) {
        Vec3 facing = facing();
        Vec3 point = position().add(facing.scale(1.0D));
        hitAround(server, point, 2.2D, 12.0F, 0.6D);
        Vec3 toward = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        Vec3 line = toward.lengthSqr() < 1.0E-4D ? facing : toward.normalize();
        point = position();
        for (int i = 0; i < 7; i++) {
            PriestessEruptionEntity.spawn(server, this, point.add(line.scale(1.8D + i * 1.4D)), PriestessCombatRules.lineDelay(i), false, 8.0F);
        }
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2.0D + Math.PI / 4.0D;
            PriestessEruptionEntity.spawn(server, this, point.add(Math.cos(a) * 1.6D, 0.0D, Math.sin(a) * 1.6D),
                    PriestessCombatRules.ERUPTION_TELL - 2, false, 6.0F);
        }
        impactDust(server, point, 1.0F);
        server.playSound(null, blockPosition(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.HOSTILE, 1.6F, 0.5F);
        server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.6F, 1.3F);
    }

    /** Damage and a lift for everything near a point. */
    private void hitAround(ServerLevel server, Vec3 point, double radius, float damage, double lift) {
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(radius, 2.0D, radius),
                e -> e != this && e.isAlive() && !(e instanceof StainedPriestessEntity))) {
            Vec3 out = victim.position().subtract(point).multiply(1.0D, 0.0D, 1.0D);
            if (out.length() > radius) continue;
            if (victim.hurt(this.damageSources().mobAttack(this), damage)) {
                Vec3 push = out.lengthSqr() < 1.0E-4D ? facing() : out.normalize();
                victim.push(push.x * 0.6D, lift, push.z * 0.6D);
                victim.hurtMarked = true;
                drain(victim);
            }
        }
    }

    private void impactDust(ServerLevel server, Vec3 point, float size) {
        server.sendParticles(BLOOD, point.x, getY() + 0.15D, point.z, (int) (36 * size), 1.1D * size, 0.1D, 1.1D * size, 0.12D);
        server.sendParticles(CLOT, point.x, getY() + 0.1D, point.z, (int) (20 * size), 1.4D * size, 0.05D, 1.4D * size, 0.04D);
        server.sendParticles(ICHOR, point.x, getY() + 0.2D, point.z, (int) (14 * size), 0.9D * size, 0.1D, 0.9D * size, 0.06D);
        server.sendParticles(HIT, point.x, getY() + 0.4D, point.z, (int) (4 * size), 0.3D, 0.2D, 0.3D, 0.02D);
    }

    /**
     * Climb over the target and hang with the spear reversed while the floor under them is marked, then drop on
     * them point first. Where she lands the blood bursts out in a ring and she is left kneeling, spear sunk.
     */
    private void tickPlummet(ServerLevel server, @Nullable LivingEntity target, int tick) {
        if (!isFlying()) {
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
            if (getAction() == ACTION_NONE) finishMode(target);
            return;
        }
        if (tick < PriestessCombatRules.PLUMMET_HANG_END) {
            if (target != null) {
                plummetAim = target.position();
                double height = Math.max(2.5D, Math.min(7.0D, ceilingAbove(target) - 2.6D));
                Vec3 over = target.position().add(0.0D, height, 0.0D);
                if (tick < PriestessCombatRules.PLUMMET_RISE_END) flyToward(over, 0.8D);
                else this.setDeltaMovement(this.getDeltaMovement().scale(0.55D).add(over.subtract(position()).scale(0.05D)));
            }
            if (plummetAim != null && tick >= PriestessCombatRules.PLUMMET_RISE_END - 4 && tick % 2 == 0) markPlummet(server, plummetAim, tick);
            if (tick == PriestessCombatRules.PLUMMET_RISE_END) {
                server.playSound(null, blockPosition(), SoundEvents.PHANTOM_SWOOP, SoundSource.HOSTILE, 1.6F, 0.45F);
                server.playSound(null, blockPosition(), SoundInit.ENTITY_STAINED_PRIESTESS_SCREAM.get(), SoundSource.HOSTILE, 2.0F, 0.9F);
            }
            return;
        }
        Vec3 aim = plummetAim != null ? plummetAim : position().subtract(0.0D, 8.0D, 0.0D);
        Vec3 drop = aim.subtract(position());
        this.setDeltaMovement(new Vec3(drop.x * 0.25D, -1.8D, drop.z * 0.25D));
        this.hasImpulse = true;
        server.sendParticles(BLOOD, getX(), getY() + 1.2D, getZ(), 3, 0.25D, 0.4D, 0.25D, 0.02D);
        if (this.onGround() || this.verticalCollision || tick >= PriestessCombatRules.PLUMMET_LATEST_IMPACT) {
            plummetImpact(server);
        }
    }

    /** A ring of ichor closing on the spot she will land on. */
    private void markPlummet(ServerLevel server, Vec3 at, int tick) {
        double radius = 3.4D * (1.0D - 0.5D * Math.min(1.0D, (tick - PriestessCombatRules.PLUMMET_RISE_END) / 14.0D));
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI * 2.0D / 16.0D + tick * 0.1D;
            server.sendParticles(ICHOR, at.x + Math.cos(a) * radius, at.y + 0.15D, at.z + Math.sin(a) * radius, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        server.sendParticles(GLOW, at.x, at.y + 0.1D, at.z, 2, 0.3D, 0.0D, 0.3D, 0.0D);
    }

    private void plummetImpact(ServerLevel server) {
        setFlying(false);
        this.setDeltaMovement(Vec3.ZERO);
        setAction(ACTION_PLANTED);
        Vec3 point = position();
        hitAround(server, point, 3.2D, 14.0F, 0.75D);
        PriestessEruptionEntity.spawn(server, this, point, 2, true, 6.0F);
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0D;
            PriestessEruptionEntity.spawn(server, this, point.add(Math.cos(a) * 3.4D, 0.0D, Math.sin(a) * 3.4D),
                    PriestessCombatRules.ERUPTION_TELL, false, 8.0F);
        }
        impactDust(server, point, 1.6F);
        server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2F, 0.7F);
        server.playSound(null, blockPosition(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.HOSTILE, 2.0F, 0.45F);
    }

    private void tickShield() {
        this.getNavigation().stop();
        if (isFlying()) this.setDeltaMovement(this.getDeltaMovement().scale(0.7D).add(0.0D, 0.01D, 0.0D));
        else this.setDeltaMovement(this.getDeltaMovement().multiply(0.3D, 1.0D, 0.3D));
    }

    /** Wings flung open: a sickly wind that throws everything in front of her back. */
    private void windBlast(ServerLevel server) {
        Vec3 facing = facing();
        server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 2.0F, 0.6F);
        server.playSound(null, blockPosition(), SoundInit.ENTITY_STAINED_PRIESTESS_YELL.get(), SoundSource.HOSTILE, 1.8F, 0.75F);
        for (int i = 0; i < 40; i++) {
            Vec3 dir = facing.yRot((this.random.nextFloat() - 0.5F) * 2.2F).scale(0.6D + this.random.nextDouble() * 0.6D);
            server.sendParticles(SPORE, getX(), getY() + 1.2D, getZ(), 0, dir.x, 0.05D, dir.z, 1.0D);
        }
        server.sendParticles(BLOOD, getX(), getY() + 1.2D, getZ(), 30, 1.5D, 0.6D, 1.5D, 0.1D);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8.0D),
                e -> e != this && e.isAlive())) {
            Vec3 toward = victim.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            if (toward.length() > 8.0D || toward.lengthSqr() < 1.0E-4D || facing.dot(toward.normalize()) < 0.3D) continue;
            Vec3 push = toward.normalize();
            victim.hurt(this.damageSources().magic(), 4.0F);
            victim.push(push.x * 1.7D, 0.5D, push.z * 1.7D);
            victim.hurtMarked = true;
            victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
        }
    }

    private void drain(LivingEntity target) {
        if (target instanceof Player player) {
            var volume = HemoCapabilityAccess.getBloodVolume(player);
            if (volume.isActive() && volume.getBloodVolume() >= BLOOD_DRAIN) {
                BloodAccounting.pay(player, BLOOD_DRAIN, TENDRIL_DRAIN);
                this.heal(3.0F);
            }
        }
    }

    // ---------------------------------------------------------------- damage

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        Mode mode = getMode();
        if (mode == Mode.EMERGING) return false;
        if (mode == Mode.SHIELD || (mode == Mode.WIND_BLAST && getModeTick() < PriestessCombatRules.WIND_BLAST_RELEASE)) {
            if (source.getDirectEntity() instanceof Projectile) {
                this.level().playSound(null, blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 0.6F);
                return false;
            }
            amount *= 0.2F;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt) recentDamage += amount;
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && target instanceof LivingEntity living && !this.level().isClientSide) drain(living);
        return result;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return getMode() != Mode.EMERGING && super.isPushable();
    }

    @Override
    public void die(DamageSource source) {
        setFlying(false);
        if (!this.level().isClientSide && source.getEntity() instanceof ServerPlayer player
                && HemoCapabilityAccess.getInitiatoryDegree(player).hasFoundedBloodline()) {
            var progress = HemoCapabilityAccess.getUnstainedProgress(player);
            progress.setAnnettaSeveranceUnlocked(true);
            UnstainedProgressEvents.syncProgress(player, progress);
            player.displayClientMessage(Component.translatable("message.hemomancy.stained_priestess_entity.the_infection_s_collapse_reveals_where"), false);
        }
        super.die(source);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(new ItemStack(ItemInit.annettas_sanguis_lancea.get()));
        this.spawnAtLocation(new ItemStack(ItemInit.hematic_iron_scrap.get(), 4));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("PriestessMode", getMode().name());
        tag.putInt("PriestessModeTick", getModeTick());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        Mode mode = Mode.TAKEOFF;
        try {
            if (tag.contains("PriestessMode")) mode = Mode.valueOf(tag.getString("PriestessMode"));
        } catch (IllegalArgumentException ignored) {
        }
        // Mid-flight saves resume by taking off again; a rise in progress picks up where it was.
        if (mode != Mode.EMERGING) mode = Mode.TAKEOFF;
        setMode(mode);
        if (mode == Mode.EMERGING) this.entityData.set(DATA_MODE_TICK, tag.getInt("PriestessModeTick"));
        if (this.getMainHandItem().isEmpty() && (mode != Mode.EMERGING || getModeTick() >= PriestessCombatRules.LANCEA_AT)) {
            equipLancea();
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundInit.ENTITY_STAINED_PRIESTESS_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundInit.ENTITY_STAINED_PRIESTESS_HURT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundInit.ENTITY_STAINED_PRIESTESS_DEATH.get();
    }
}
