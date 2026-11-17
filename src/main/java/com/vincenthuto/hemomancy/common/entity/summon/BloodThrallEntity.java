package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.entity.SyncedOwner;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.*;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.core.BlockPos;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Blood Thrall — a summoned blood golem that physically carries blood between
 * a designated source IBloodReservoir and destination IBloodReservoir. It pathfinds
 * between them, absorbing blood at the source and depositing at the destination.
 * Can be killed by mobs. Multiple thralls = more throughput.
 */
public class BloodThrallEntity extends PathfinderMob implements OwnableEntity {

    // ── Synched data ──
    private static final SyncedOwner OWNER = SyncedOwner.define(BloodThrallEntity.class);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_SOURCE_POS =
            SynchedEntityData.defineId(BloodThrallEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_DEST_POS =
            SynchedEntityData.defineId(BloodThrallEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Float> DATA_CARRIED_BLOOD =
            SynchedEntityData.defineId(BloodThrallEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_AWAITING_TARGET =
            SynchedEntityData.defineId(BloodThrallEntity.class, EntityDataSerializers.BOOLEAN);

    /** Maximum mL the thrall can carry in a single trip */
    public static final float MAX_CARRY = 1000f;
    /** mL absorbed/deposited per tick when at a node */
    public static final float TRANSFER_RATE = 50f;

    // ── State machine ──
    public enum ThrallState { IDLE, AWAITING_TARGET, GOING_TO_SOURCE, ABSORBING, GOING_TO_DEST, DEPOSITING }
    private ThrallState thrallState = ThrallState.IDLE;
    private int workTimer = 0;
    /** Server-side counter for how long the thrall has been awaiting target selection */
    private int awaitingTimer = 0;
    /** The age of the blood it carries, from the tank it was taken from; a courier never makes blood younger (NQ-01-5). */
    private long carriedAge = 0L;
    /** Maximum ticks to wait for target selection before discarding (~30 seconds) */
    private static final int AWAITING_TIMEOUT = 600;
    private static final ParticleColor BLOOD = new ParticleColor(150, 14, 26);
    private static final ParticleColor CLOT = new ParticleColor(70, 6, 14);

    // ── Constructor ──

    public BloodThrallEntity(EntityType<? extends BloodThrallEntity> type, Level level) {
        super(type, level);
    }

    public BloodThrallEntity(Level level, Player owner, BlockPos source, BlockPos dest) {
        super(EntityInit.blood_thrall.get(), level);
        setOwnerUUID(owner.getUUID());
        setSourcePos(source);
        setDestPos(dest);
    }

    @Override
    public float maxUpStep() {
        return 1.0f;
    }

    // ── Attributes ──

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
    }

    // ── Goals ──

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ThrallWorkGoal(this));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    // ── Synched data setup ──

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        OWNER.defineDefault(builder);
        builder.define(DATA_SOURCE_POS, Optional.empty());
        builder.define(DATA_DEST_POS, Optional.empty());
        builder.define(DATA_CARRIED_BLOOD, 0f);
        builder.define(DATA_AWAITING_TARGET, false);
    }

    // ── Getters / Setters ──

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return OWNER.get(this);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        OWNER.set(this, uuid);
    }

    @Nullable
    public BlockPos getSourcePos() {
        return this.entityData.get(DATA_SOURCE_POS).orElse(null);
    }

    public void setSourcePos(@Nullable BlockPos pos) {
        this.entityData.set(DATA_SOURCE_POS, Optional.ofNullable(pos));
    }

    @Nullable
    public BlockPos getDestPos() {
        return this.entityData.get(DATA_DEST_POS).orElse(null);
    }

    public void setDestPos(@Nullable BlockPos pos) {
        this.entityData.set(DATA_DEST_POS, Optional.ofNullable(pos));
    }

    public float getCarriedBlood() {
        return this.entityData.get(DATA_CARRIED_BLOOD);
    }

    public void setCarriedBlood(float amount) {
        this.entityData.set(DATA_CARRIED_BLOOD, Math.max(0, Math.min(MAX_CARRY, amount)));
    }

    public long getCarriedAge() {
        return carriedAge;
    }

    /** Takes {@code amount} more blood that is {@code age} ticks old; what it carries blends by volume. */
    public void takeBlood(float amount, long age) {
        carriedAge = CoagulationRules.blendedAge(getCarriedBlood(), carriedAge, amount, age);
        setCarriedBlood(getCarriedBlood() + amount);
    }

    /** How "full" the thrall is, 0..1 — used for rendering scale/color */
    public float getCarryRatio() {
        return Math.min(1f, getCarriedBlood() / MAX_CARRY);
    }

    public boolean getAwaitingTarget() {
        return this.entityData.get(DATA_AWAITING_TARGET);
    }

    public void setAwaitingTarget(boolean awaiting) {
        this.entityData.set(DATA_AWAITING_TARGET, awaiting);
        if (awaiting) {
            this.awaitingTimer = 0;
        }
    }

    // ── Tick & Particles ──

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnBloodParticles();
        }
        // Server-side: timeout for awaiting target selection
        if (!level().isClientSide && getAwaitingTarget()) {
            awaitingTimer++;
            if (awaitingTimer > AWAITING_TIMEOUT) {
                // Timed out waiting for target — discard with blood splatter
                level().broadcastEntityEvent(this, BLOOD_SPLATTER_EVENT);
                playSound(SoundEvents.SLIME_DEATH, 0.6f, 1.0f);
                discard();
            }
        }
    }

    private void spawnBloodParticles() {
        float ratio = getCarryRatio();
        if (ratio <= 0) return;
        if (random.nextFloat() > ratio * 0.4f) return;
        double dx = (random.nextDouble() - 0.5) * 0.4;
        double dy = random.nextDouble() * 0.5 + 0.1;
        double dz = (random.nextDouble() - 0.5) * 0.4;
        level().addParticle(HemoParticleData.bloodCell(BLOOD),
                getX() + dx, getY() + dy, getZ() + dz,
                0, -0.01, 0
        );
    }

    // ── Persistence ──

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        OWNER.save(this, tag);
        if (getSourcePos() != null) tag.put("SourcePos", NbtUtils.writeBlockPos(getSourcePos()));
        if (getDestPos() != null) tag.put("DestPos", NbtUtils.writeBlockPos(getDestPos()));
        tag.putFloat("CarriedBlood", getCarriedBlood());
        tag.putLong("CarriedAge", carriedAge);
        tag.putInt("ThrallState", thrallState.ordinal());
        tag.putBoolean("AwaitingTarget", getAwaitingTarget());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        OWNER.load(this, tag);
        setSourcePos(NbtUtils.readBlockPos(tag, "SourcePos").orElse(null));
        setDestPos(NbtUtils.readBlockPos(tag, "DestPos").orElse(null));
        setCarriedBlood(tag.getFloat("CarriedBlood"));
        carriedAge = tag.getLong("CarriedAge");
        int stOrd = tag.getInt("ThrallState");
        if (stOrd >= 0 && stOrd < ThrallState.values().length) {
            thrallState = ThrallState.values()[stOrd];
        }
        if (tag.contains("AwaitingTarget")) {
            setAwaitingTarget(tag.getBoolean("AwaitingTarget"));
        }
    }

    // ── Packet ──

    // ── Sounds ──

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SLIME_SQUISH_SMALL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160; // less chatty
    }

    // ── Pickup & Absorb ──

    /** Blood returned when absorbing the thrall (half the summoning cost) */
    private static final float ABSORB_BLOOD_RETURN = 250f;
    /** Entity event ID for blood splatter particles (shared with die()) */
    private static final byte BLOOD_SPLATTER_EVENT = 60;

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            // Only owner (or creative) can interact
            UUID owner = getOwnerUUID();
            if (owner != null && !owner.equals(player.getUUID()) && !player.getAbilities().instabuild) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.hemomancy.blood_thrall_entity.this_thrall_doesn_t_belong_to"), true);
                return InteractionResult.FAIL;
            }

            if (player.isShiftKeyDown()) {
                // ── Shift+click: reabsorb thrall back into blood volume ──
                double recovered = reabsorbInto(player);
                player.displayClientMessage(
                        HemoText.translatable("message.hemomancy.blood_thrall_entity.thrall_reabsorbed_recovered_ml",
                                (int) recovered), true);

                level().broadcastEntityEvent(this, BLOOD_SPLATTER_EVENT);
                playSound(SoundEvents.SLIME_SQUISH, 0.6f, 1.2f);
                discard();
                return InteractionResult.SUCCESS;
            } else {
                // ── Right-click: absorb thrall back into blood volume ──
                reabsorbInto(player);

                // Blood splatter particles on absorb
                level().broadcastEntityEvent(this, BLOOD_SPLATTER_EVENT);

                playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.6f, 1.5f);
                discard();
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    /**
     * Returns the thrall's body and what it carried to {@code player} as income the flow ledger records, which also
     * stops at a full volume and at a dormant one. Returns what was actually taken in.
     */
    private double reabsorbInto(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return 0.0D;
        return BloodFlowLedger.applyDirectIncome(serverPlayer, HemoCapabilityAccess.getBloodVolume(serverPlayer),
                "thrall_reabsorb", "message.hemomancy.blood_thrall_entity.reabsorb_source", Category.OTHER,
                ABSORB_BLOOD_RETURN + getCarriedBlood(), 1);
    }

    // ── Drop carried blood on death ──

    @Override
    public void die(DamageSource source) {
        // When killed, the carried blood is lost (spills out visually via particles)
        if (!level().isClientSide && getCarriedBlood() > 0) {
            // Spawn extra blood particles on death — handled client-side via broadcastEntityEvent
            level().broadcastEntityEvent(this, BLOOD_SPLATTER_EVENT);
        }
        super.die(source);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == BLOOD_SPLATTER_EVENT) {
            // Blood splatter particles on death
            for (int i = 0; i < 20; i++) {
                double dx = (random.nextDouble() - 0.5) * 1.0;
                double dy = random.nextDouble() * 0.6;
                double dz = (random.nextDouble() - 0.5) * 1.0;
                level().addParticle(HemoParticleData.bloodCell(BLOOD),
                        getX() + dx, getY() + dy, getZ() + dz,
                        dx * 0.3, 0.04, dz * 0.3
                );
                if (i < 6) {
                    level().addParticle(HemoParticleData.darkGlow(CLOT),
                            getX() + dx, getY() + dy, getZ() + dz,
                            dx * 0.3, 0.04, dz * 0.3
                    );
                }
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  AI Goal — the main work loop
    // ════════════════════════════════════════════════════════════════

    private static class ThrallWorkGoal extends Goal {
        private final BloodThrallEntity thrall;
        private int recalcCooldown = 0;

        ThrallWorkGoal(BloodThrallEntity thrall) {
            this.thrall = thrall;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !thrall.getAwaitingTarget()
                    && thrall.getSourcePos() != null && thrall.getDestPos() != null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            // Decide initial state based on carried blood
            if (thrall.getCarriedBlood() > 0) {
                thrall.thrallState = ThrallState.GOING_TO_DEST;
            } else {
                thrall.thrallState = ThrallState.GOING_TO_SOURCE;
            }
        }

        @Override
        public void tick() {
            if (thrall.level().isClientSide) return;

            switch (thrall.thrallState) {
                case IDLE:
                    thrall.thrallState = ThrallState.GOING_TO_SOURCE;
                    break;

                case GOING_TO_SOURCE:
                    navigateToward(thrall.getSourcePos());
                    if (isNear(thrall.getSourcePos(), 2.0)) {
                        thrall.thrallState = ThrallState.ABSORBING;
                        thrall.workTimer = 0;
                        thrall.getNavigation().stop();
                    }
                    break;

                case ABSORBING:
                    thrall.getLookControl().setLookAt(
                            thrall.getSourcePos().getX() + 0.5,
                            thrall.getSourcePos().getY() + 0.5,
                            thrall.getSourcePos().getZ() + 0.5);
                    if (tryAbsorb()) {
                        thrall.workTimer++;
                    }
                    // Move on when full or source empty or timeout
                    if (thrall.getCarriedBlood() >= MAX_CARRY || thrall.workTimer > 100) {
                        if (thrall.getCarriedBlood() > 0) {
                            thrall.thrallState = ThrallState.GOING_TO_DEST;
                        } else {
                            // Source empty — wait briefly
                            thrall.thrallState = ThrallState.IDLE;
                        }
                    }
                    break;

                case GOING_TO_DEST:
                    navigateToward(thrall.getDestPos());
                    if (isNear(thrall.getDestPos(), 2.0)) {
                        thrall.thrallState = ThrallState.DEPOSITING;
                        thrall.workTimer = 0;
                        thrall.getNavigation().stop();
                    }
                    break;

                case DEPOSITING:
                    thrall.getLookControl().setLookAt(
                            thrall.getDestPos().getX() + 0.5,
                            thrall.getDestPos().getY() + 0.5,
                            thrall.getDestPos().getZ() + 0.5);
                    if (tryDeposit()) {
                        thrall.workTimer++;
                    }
                    // Move on when empty or dest full or timeout
                    if (thrall.getCarriedBlood() <= 0 || thrall.workTimer > 100) {
                        thrall.thrallState = ThrallState.GOING_TO_SOURCE;
                    }
                    break;
            }
        }

        // ── Helpers ──

        private void navigateToward(BlockPos target) {
            if (recalcCooldown-- <= 0) {
                thrall.getNavigation().moveTo(
                        target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0);
                recalcCooldown = 20; // recalculate path every second
            }
        }

        private boolean isNear(BlockPos pos, double range) {
            return thrall.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < range * range;
        }

        /** Try to absorb blood from the source tile. Returns true if work was done. */
        private boolean tryAbsorb() {
            BlockEntity be = thrall.level().getBlockEntity(thrall.getSourcePos());
            if (be == null) return false;
            float transferRate = transferRate();

            if (thrall.level() instanceof ServerLevel serverLevel) {
                DirectBloodLinkData link = BloodRoutingSavedData.get(serverLevel).getLink(thrall.getSourcePos());
                if (link != null) {
                    float space = MAX_CARRY - thrall.getCarriedBlood();
                    double drawn = BloodRoutingHelper.drawForCourier(serverLevel, thrall.getSourcePos(), link,
                            space, Math.min(transferRate, space));
                    if (drawn > 0) {
                        thrall.takeBlood((float) drawn, 0L);
                        return true;
                    }
                }
            }

            if (be instanceof IBloodReservoir closed && !closed.canProvideBlood()) return false;
            // A courier hauls only from its master's machines, their bloodline's, or anyone's if the server allows.
            if (!com.vincenthuto.hemomancy.common.tile.BloodDrawRights.mayDraw(
                    thrall.level() instanceof ServerLevel level ? level : null, thrall.getOwnerUUID(), be)) return false;
            IBloodVolume srcVol = HemoCapabilityAccess.getBloodVolume(be).orElse(null);
            if (srcVol == null) return false;

            float space = MAX_CARRY - thrall.getCarriedBlood();
            float toTransfer = Math.min(transferRate, space);
            if (toTransfer <= 0) return false;

            if (srcVol.getBloodVolume() >= toTransfer) {
                long age = srcVol instanceof BlockBloodVolume tank ? tank.age() : 0L;
                srcVol.drain(toTransfer);
                thrall.takeBlood(toTransfer, age);
                if (be instanceof IBloodReservoir bt) bt.sendUpdates();
                return true;
            }
            return false;
        }

        /** Try to deposit blood into the destination tile. Returns true if work was done. */
        private boolean tryDeposit() {
            BlockEntity be = thrall.level().getBlockEntity(thrall.getDestPos());
            if (be == null || be instanceof IBloodReservoir closed && !closed.canReceiveBlood()) return false;
            IBloodVolume destVol = HemoCapabilityAccess.getBloodVolume(be).orElse(null);
            if (destVol == null) return false;

            float transferRate = transferRate();
            ThrallCourierSource courier = new ThrallCourierSource(thrall.getCarriedBlood(), transferRate,
                    thrall.getCarriedAge());
            double transferred = courier.transferTo(destVol, transferRate);
            if (transferred > 0) {
                thrall.setCarriedBlood((float) courier.getCarriedBlood());
                if (be instanceof IBloodReservoir bt) bt.sendUpdates();
                return true;
            }
            return false;
        }

        private float transferRate() {
            return (float) (TRANSFER_RATE * BloodRoutingRules.servitorThroughputMultiplier(servitorTenderLevel()));
        }

        private int servitorTenderLevel() {
            if (!(thrall.level() instanceof ServerLevel serverLevel) || thrall.getOwnerUUID() == null) {
                return 0;
            }
            Player owner = serverLevel.getPlayerByUUID(thrall.getOwnerUUID());
            return owner == null ? 0 : SkillPointHelper.getServitorTenderLevel(owner);
        }
    }
}
