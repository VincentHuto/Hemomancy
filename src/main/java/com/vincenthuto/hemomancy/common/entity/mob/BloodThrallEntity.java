package com.vincenthuto.hemomancy.common.entity.mob;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

/**
 * Blood Thrall — a summoned blood golem that physically carries blood between
 * a designated source IBloodTile and destination IBloodTile. It pathfinds
 * between them, absorbing blood at the source and depositing at the destination.
 * Can be killed by mobs. Multiple thralls = more throughput.
 */
public class BloodThrallEntity extends PathfinderMob implements OwnableEntity {

    // ── Synched data ──
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(BloodThrallEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_SOURCE_POS =
            SynchedEntityData.defineId(BloodThrallEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_DEST_POS =
            SynchedEntityData.defineId(BloodThrallEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Float> DATA_CARRIED_BLOOD =
            SynchedEntityData.defineId(BloodThrallEntity.class, EntityDataSerializers.FLOAT);

    /** Maximum mL the thrall can carry in a single trip */
    public static final float MAX_CARRY = 1000f;
    /** mL absorbed/deposited per tick when at a node */
    public static final float TRANSFER_RATE = 50f;

    // ── State machine ──
    public enum ThrallState { IDLE, GOING_TO_SOURCE, ABSORBING, GOING_TO_DEST, DEPOSITING }
    private ThrallState thrallState = ThrallState.IDLE;
    private int workTimer = 0;

    // ── Constructor ──

    public BloodThrallEntity(EntityType<? extends BloodThrallEntity> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0f);
    }

    public BloodThrallEntity(Level level, Player owner, BlockPos source, BlockPos dest) {
        super(EntityInit.blood_thrall.get(), level);
        this.setMaxUpStep(1.0f);
        setOwnerUUID(owner.getUUID());
        setSourcePos(source);
        setDestPos(dest);
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
        this.entityData.define(DATA_SOURCE_POS, Optional.empty());
        this.entityData.define(DATA_DEST_POS, Optional.empty());
        this.entityData.define(DATA_CARRIED_BLOOD, 0f);
    }

    // ── Getters / Setters ──

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
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

    public ThrallState getThrallState() {
        return thrallState;
    }

    /** How "full" the thrall is, 0..1 — used for rendering scale/color */
    public float getCarryRatio() {
        return Math.min(1f, getCarriedBlood() / MAX_CARRY);
    }

    // ── Tick & Particles ──

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnBloodParticles();
        }
    }

    private void spawnBloodParticles() {
        float ratio = getCarryRatio();
        if (ratio <= 0) return;
        if (random.nextFloat() > ratio * 0.4f) return;
        double dx = (random.nextDouble() - 0.5) * 0.4;
        double dy = random.nextDouble() * 0.5 + 0.1;
        double dz = (random.nextDouble() - 0.5) * 0.4;
        float size = 0.4f + ratio * 0.6f;
        level().addParticle(
                new DustParticleOptions(new Vector3f(0.55f, 0.05f, 0.05f), size),
                getX() + dx, getY() + dy, getZ() + dz,
                0, 0.01, 0
        );
    }

    // ── Persistence ──

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getOwnerUUID() != null) tag.putUUID("Owner", getOwnerUUID());
        if (getSourcePos() != null) tag.put("SourcePos", NbtUtils.writeBlockPos(getSourcePos()));
        if (getDestPos() != null) tag.put("DestPos", NbtUtils.writeBlockPos(getDestPos()));
        tag.putFloat("CarriedBlood", getCarriedBlood());
        tag.putInt("ThrallState", thrallState.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) setOwnerUUID(tag.getUUID("Owner"));
        if (tag.contains("SourcePos")) setSourcePos(NbtUtils.readBlockPos(tag.getCompound("SourcePos")));
        if (tag.contains("DestPos")) setDestPos(NbtUtils.readBlockPos(tag.getCompound("DestPos")));
        setCarriedBlood(tag.getFloat("CarriedBlood"));
        int stOrd = tag.getInt("ThrallState");
        if (stOrd >= 0 && stOrd < ThrallState.values().length) {
            thrallState = ThrallState.values()[stOrd];
        }
    }

    // ── Packet ──

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

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

    // ── Pickup ──

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide && player.isShiftKeyDown()) {
            // Only owner (or creative) can pick up
            UUID owner = getOwnerUUID();
            if (owner != null && !owner.equals(player.getUUID()) && !player.getAbilities().instabuild) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§cThis thrall doesn't belong to you."), true);
                return InteractionResult.FAIL;
            }

            // Build an item with the source/dest bindings preserved
            ItemStack stack = new ItemStack(ItemInit.blood_thrall_effigy.get());
            CompoundTag tag = stack.getOrCreateTag();
            if (getSourcePos() != null) tag.put("ThrallSource", NbtUtils.writeBlockPos(getSourcePos()));
            if (getDestPos() != null)   tag.put("ThrallDest", NbtUtils.writeBlockPos(getDestPos()));

            // Give carried blood back to the player
            if (getCarriedBlood() > 0) {
                IBloodVolume playerVol = player.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
                if (playerVol != null) {
                    playerVol.fill(getCarriedBlood());
                }
            }

            // Drop the item to the player
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }

            playSound(SoundEvents.SLIME_SQUISH, 0.6f, 1.2f);
            discard();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    // ── Drop carried blood on death ──

    @Override
    public void die(DamageSource source) {
        // When killed, the carried blood is lost (spills out visually via particles)
        if (!level().isClientSide && getCarriedBlood() > 0) {
            // Spawn extra blood particles on death — handled client-side via broadcastEntityEvent
            level().broadcastEntityEvent(this, (byte) 60);
        }
        super.die(source);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 60) {
            // Blood splatter particles on death
            for (int i = 0; i < 20; i++) {
                double dx = (random.nextDouble() - 0.5) * 1.0;
                double dy = random.nextDouble() * 0.6;
                double dz = (random.nextDouble() - 0.5) * 1.0;
                level().addParticle(
                        new DustParticleOptions(new Vector3f(0.6f, 0.02f, 0.02f), 1.2f),
                        getX() + dx, getY() + dy, getZ() + dz,
                        dx * 0.3, 0.15, dz * 0.3
                );
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
            return thrall.getSourcePos() != null && thrall.getDestPos() != null;
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
            IBloodVolume srcVol = be.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
            if (srcVol == null) return false;

            float space = MAX_CARRY - thrall.getCarriedBlood();
            float toTransfer = Math.min(TRANSFER_RATE, space);
            if (toTransfer <= 0) return false;

            if (srcVol.getBloodVolume() >= toTransfer) {
                srcVol.drain(toTransfer);
                thrall.setCarriedBlood(thrall.getCarriedBlood() + toTransfer);
                if (be instanceof IBloodTile bt) bt.sendUpdates();
                return true;
            }
            return false;
        }

        /** Try to deposit blood into the destination tile. Returns true if work was done. */
        private boolean tryDeposit() {
            BlockEntity be = thrall.level().getBlockEntity(thrall.getDestPos());
            if (be == null) return false;
            IBloodVolume destVol = be.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
            if (destVol == null) return false;

            float toTransfer = Math.min(TRANSFER_RATE, thrall.getCarriedBlood());
            if (toTransfer <= 0) return false;

            if (!destVol.isFull()) {
                destVol.fill(toTransfer);
                thrall.setCarriedBlood(thrall.getCarriedBlood() - toTransfer);
                if (be instanceof IBloodTile bt) bt.sendUpdates();
                return true;
            }
            return false;
        }
    }
}
