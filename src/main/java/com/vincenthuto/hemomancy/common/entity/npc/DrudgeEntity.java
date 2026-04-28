package com.vincenthuto.hemomancy.common.entity.npc;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * The Drudge — a persistent, player-owned semi-organic construct that holds a
 * single {@link BloodManipulation} (via a Blood Memory item) and executes it
 * autonomously within a leash radius anchored to a Semi-Sentient Construct (SSC)
 * block. The SSC refills the Drudge's internal blood charge; when the charge
 * runs dry and the Drudge cannot return, it enters the Rogue state and turns
 * hostile.
 */
public class DrudgeEntity extends PathfinderMob implements OwnableEntity {

    // ── Synched data keys ─────────────────────────────────────────────────────

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(DrudgeEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_HOME_POS =
            SynchedEntityData.defineId(DrudgeEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Float> DATA_BLOOD_CHARGE =
            SynchedEntityData.defineId(DrudgeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_ROGUE =
            SynchedEntityData.defineId(DrudgeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_PASSIVE_MODE =
            SynchedEntityData.defineId(DrudgeEntity.class, EntityDataSerializers.BOOLEAN);

    // ── Constants ─────────────────────────────────────────────────────────────

    public static final float MAX_BLOOD_CHARGE = 3000f;
    /** mL refilled per tick when the drudge is in contact with the SSC */
    public static final float REFILL_RATE = 50f;
    /** Blood charge threshold below which the drudge heads home to refill */
    private static final float LOW_CHARGE_THRESHOLD = 200f;
    /** Squared distance threshold used to decide when the drudge has "arrived" at the SSC (2 blocks). */
    private static final double ARRIVED_DIST_SQ = 4.0;
    /** Health ratio below which the Drudge heals itself when no player needs healing. */
    private static final float SELF_HEAL_THRESHOLD = 0.6f;
    /** Vertical search range (min) below the drudge's position for torch placement. */
    private static final int TORCH_SCAN_Y_MIN = -2;
    /** Vertical search range (max) above the drudge's position for torch placement. */
    private static final int TORCH_SCAN_Y_MAX = 4;
    /** Maximum block-light level at which the Lux tendency places a torch. */
    private static final int MIN_LIGHT_FOR_TORCH = 10;
    /** Entity event byte for gore-particle death burst */
    private static final byte BURST_EVENT = 61;

    // ── Server-side fields ────────────────────────────────────────────────────

    /** The equipped manipulation — server-side only (saved to NBT). */
    @Nullable
    private BloodManipulation equippedMemory = null;

    /** Cooldown counter shared by all execute-memory firings. */
    private int memoryCooldownTicks = 0;

    /** Whether we have received an electrode "fire now" signal this tick. */
    private boolean electrodeSignalPending = false;

    /** Ticks the drudge has spent unable to return to the SSC (rogue timer). */
    private int rogueTimer = 0;

    // ── Constructor ───────────────────────────────────────────────────────────

    public DrudgeEntity(EntityType<? extends DrudgeEntity> type, Level level) {
        super(type, level);
    }

    // ── Attributes ────────────────────────────────────────────────────────────

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // ── Goal registration ─────────────────────────────────────────────────────

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DrudgeReturnToSSCGoal(this));
        this.goalSelector.addGoal(2, new DrudgeExecuteMemoryGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true) {
            @Override
            public boolean canUse() {
                return isRogue() && super.canUse();
            }
            @Override
            public boolean canContinueToUse() {
                return isRogue() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D) {
            @Override
            public boolean canUse() {
                return !isRogue() && getHomePos() != null && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return isRogue() && super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, null) {
            @Override
            public boolean canUse() {
                return isRogue() && super.canUse();
            }
        });
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, null) {
            @Override
            public boolean canUse() {
                return isRogue() && super.canUse();
            }
        });
    }

    // ── Synched data ──────────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_HOME_POS, Optional.empty());
        builder.define(DATA_BLOOD_CHARGE, MAX_BLOOD_CHARGE);
        builder.define(DATA_IS_ROGUE, false);
        builder.define(DATA_PASSIVE_MODE, true);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public BlockPos getHomePos() {
        return this.entityData.get(DATA_HOME_POS).orElse(null);
    }

    public void setHomePos(@Nullable BlockPos pos) {
        this.entityData.set(DATA_HOME_POS, Optional.ofNullable(pos));
    }

    public float getBloodCharge() {
        return this.entityData.get(DATA_BLOOD_CHARGE);
    }

    public void setBloodCharge(float amount) {
        this.entityData.set(DATA_BLOOD_CHARGE, Math.max(0f, Math.min(MAX_BLOOD_CHARGE, amount)));
    }

    public void drainBloodCharge(float amount) {
        setBloodCharge(getBloodCharge() - amount);
    }

    public void fillBloodCharge(float amount) {
        setBloodCharge(getBloodCharge() + amount);
    }

    public float getChargeRatio() {
        return Math.min(1f, getBloodCharge() / MAX_BLOOD_CHARGE);
    }

    public boolean isRogue() {
        return this.entityData.get(DATA_IS_ROGUE);
    }

    public void setRogue(boolean rogue) {
        this.entityData.set(DATA_IS_ROGUE, rogue);
        if (rogue) {
            // Immediately forget the home so pathfinding stops being home-directed
            setHomePos(null);
            if (!level().isClientSide) {
                setCustomName(Component.literal("Rogue Drudge").withStyle(ChatFormatting.DARK_RED));
                setCustomNameVisible(true);
            }
        }
    }

    public boolean isPassiveMode() {
        return this.entityData.get(DATA_PASSIVE_MODE);
    }

    public void setPassiveMode(boolean passive) {
        this.entityData.set(DATA_PASSIVE_MODE, passive);
    }

    @Nullable
    public BloodManipulation getEquippedMemory() {
        return equippedMemory;
    }

    public void setEquippedMemory(@Nullable BloodManipulation manip) {
        this.equippedMemory = manip;
        memoryCooldownTicks = 0;
        if (!level().isClientSide) {
            if (manip != null) {
                setCustomName(Component.literal("[" + manip.getProperName() + "]").withStyle(ChatFormatting.DARK_RED));
                setCustomNameVisible(true);
            } else {
                setCustomName(null);
                setCustomNameVisible(false);
            }
        }
    }

    /** Called by the Drudge Electrode to queue a one-shot fire. */
    public void queueElectrodeSignal() {
        electrodeSignalPending = true;
    }

    public boolean isMemoryOnCooldown() {
        return memoryCooldownTicks > 0;
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        // Tick down the memory cooldown
        if (memoryCooldownTicks > 0) memoryCooldownTicks--;

        // If rogue, no special logic needed — aggression goals handle it
        if (isRogue()) return;

        // ── Rogue check: zero charge + no home reachable ──────────────────────
        BlockPos home = getHomePos();
        if (getBloodCharge() <= 0 && home != null) {
            rogueTimer++;
            int rogueTimeout = HemoServerConfig.DRUDGE_ROGUE_TIMEOUT_TICKS.get();
            // Try to navigate home; if we've been stuck too long, go rogue
            double distSq = distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5);
            if (distSq > ARRIVED_DIST_SQ && rogueTimer > rogueTimeout) {
                setRogue(true);
                rogueTimer = 0;
            }
        } else {
            rogueTimer = 0;
        }

        // ── SSC break / owner gone rogue check ───────────────────────────────
        if (home != null) {
            int leashRadius = HemoServerConfig.DRUDGE_LEASH_RADIUS.get();
            double distSq = distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5);
            if (distSq > (leashRadius + 6) * (leashRadius + 6)) {
                // Far beyond leash range — something went wrong; go rogue
                setRogue(true);
            }
        }
    }

    // ── Interaction ───────────────────────────────────────────────────────────

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;

        // Only owner can interact (or creative)
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null && !ownerUUID.equals(player.getUUID()) && !player.getAbilities().instabuild) {
            player.displayClientMessage(Component.literal("§cThis construct does not answer to you."), true);
            return InteractionResult.FAIL;
        }

        if (isRogue()) {
            player.displayClientMessage(Component.literal("§4It no longer recognizes you."), true);
            return InteractionResult.FAIL;
        }

        ItemStack held = player.getItemInHand(hand);

        // Shift+right-click with empty hand → remove equipped memory and return it
        if (player.isShiftKeyDown() && held.isEmpty()) {
            if (equippedMemory != null) {
                dropMemoryItem(player);
            } else {
                player.displayClientMessage(Component.literal("§8No memory is installed."), true);
            }
            return InteractionResult.SUCCESS;
        }

        // Right-click with a Blood Memory → equip it
        if (!held.isEmpty() && held.getItem() instanceof com.vincenthuto.hemomancy.common.item.memories.BloodMemoryItem memItem) {
            setEquippedMemory(memItem.getManip());
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.displayClientMessage(Component.literal("§7Memory installed: §c" + memItem.getManip().getProperName()), true);
            return InteractionResult.SUCCESS;
        }

        // Right-click with empty hand → toggle mode and show status
        if (held.isEmpty()) {
            setPassiveMode(!isPassiveMode());
            String modeStr = isPassiveMode() ? "§aPassive" : "§eCommanded";
            String memStr = equippedMemory != null ? "§c" + equippedMemory.getProperName() : "§8none";
            player.displayClientMessage(Component.literal(
                    "§7Mode: " + modeStr + " §7| Memory: " + memStr
                    + " §7| Charge: §c" + (int) getBloodCharge() + "/" + (int) MAX_BLOOD_CHARGE + " mL"), true);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    /** Drops the currently equipped memory as an item near the drudge. */
    private void dropMemoryItem(@Nullable Player player) {
        if (equippedMemory == null) return;
        // Find the BloodMemoryItem that matches this manipulation across all item registers.
        // Short-circuits after the first successful match/drop.
        boolean dropped = dropMemoryItemFromRegister(player, com.vincenthuto.hemomancy.common.init.ItemInit.BASEITEMS)
                || dropMemoryItemFromRegister(player, com.vincenthuto.hemomancy.common.init.ItemInit.HANDHELDITEMS)
                || dropMemoryItemFromRegister(player, com.vincenthuto.hemomancy.common.init.ItemInit.SPECIALITEMS);
        if (!dropped && player != null) {
            player.displayClientMessage(Component.literal("§8No matching memory item found."), true);
        }
    }

    private boolean dropMemoryItemFromRegister(@Nullable Player player,
            net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.item.Item> register) {
        if (equippedMemory == null) return false;
        for (var holder : register.getEntries()) {
            if (holder.get() instanceof com.vincenthuto.hemomancy.common.item.memories.BloodMemoryItem bmi) {
                if (bmi.getManip() != null && bmi.getManip().getName().equals(equippedMemory.getName())) {
                    spawnAtLocation(new net.minecraft.world.item.ItemStack(bmi));
                    setEquippedMemory(null);
                    if (player != null) {
                        player.displayClientMessage(Component.literal("§7Memory retrieved."), true);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // ── Death ─────────────────────────────────────────────────────────────────

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) {
            // Drop the equipped memory on death
            if (equippedMemory != null) {
                dropMemoryItem(null);
            }
            level().broadcastEntityEvent(this, BURST_EVENT);
        }
        super.die(source);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == BURST_EVENT) {
            for (int i = 0; i < 12; i++) {
                double dx = (random.nextDouble() - 0.5) * 0.9;
                double dy = random.nextDouble() * 0.5;
                double dz = (random.nextDouble() - 0.5) * 0.9;
                level().addParticle(
                        new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.55f, 0.05f, 0.05f), 1.0f),
                        getX() + dx, getY() + dy, getZ() + dz,
                        dx * 0.2, 0.1, dz * 0.2);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ── Sounds ────────────────────────────────────────────────────────────────

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.3f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    // ── NBT Persistence ───────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getOwnerUUID() != null) tag.putUUID("Owner", getOwnerUUID());
        if (getHomePos() != null) tag.put("HomePos", NbtUtils.writeBlockPos(getHomePos()));
        if (equippedMemory != null) tag.put("EquippedMemory", equippedMemory.serialize());
        tag.putFloat("BloodCharge", getBloodCharge());
        tag.putBoolean("IsRogue", isRogue());
        tag.putBoolean("PassiveMode", isPassiveMode());
        tag.putInt("MemoryCooldown", memoryCooldownTicks);
        tag.putInt("RogueTimer", rogueTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) setOwnerUUID(tag.getUUID("Owner"));
        setHomePos(NbtUtils.readBlockPos(tag, "HomePos").orElse(null));
        if (tag.contains("EquippedMemory")) {
            CompoundTag memTag = tag.getCompound("EquippedMemory");
            BloodManipulation loaded = BloodManipulation.deserialize(memTag);
            setEquippedMemory(loaded);
        }
        if (tag.contains("BloodCharge")) setBloodCharge(tag.getFloat("BloodCharge"));
        if (tag.contains("IsRogue")) setRogue(tag.getBoolean("IsRogue"));
        if (tag.contains("PassiveMode")) setPassiveMode(tag.getBoolean("PassiveMode"));
        if (tag.contains("MemoryCooldown")) memoryCooldownTicks = tag.getInt("MemoryCooldown");
        if (tag.contains("RogueTimer")) rogueTimer = tag.getInt("RogueTimer");
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  AI GOALS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Returns the drudge to its SSC when blood charge is low, when there is nothing
     * to do, or when it has drifted beyond the leash radius. While at the SSC the
     * SSC's {@code tick()} refills the charge — this goal just navigates there.
     */
    private static class DrudgeReturnToSSCGoal extends Goal {
        private final DrudgeEntity drudge;
        private int recalcTimer;

        DrudgeReturnToSSCGoal(DrudgeEntity drudge) {
            this.drudge = drudge;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (drudge.isRogue()) return false;
            BlockPos home = drudge.getHomePos();
            if (home == null) return false;
            // Return when charge is low OR when out of leash range
            int leash = HemoServerConfig.DRUDGE_LEASH_RADIUS.get();
            double distSq = drudge.distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5);
            boolean lowCharge = drudge.getBloodCharge() < LOW_CHARGE_THRESHOLD;
            boolean outsideLeash = distSq > (double) leash * leash;
            return lowCharge || outsideLeash;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() && !isAtHome();
        }

        @Override
        public void start() {
            recalcTimer = 0;
        }

        @Override
        public void stop() {
            drudge.getNavigation().stop();
        }

        @Override
        public void tick() {
            BlockPos home = drudge.getHomePos();
            if (home == null) return;
            if (recalcTimer-- <= 0) {
                drudge.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.0);
                recalcTimer = 20;
            }
        }

        private boolean isAtHome() {
            BlockPos home = drudge.getHomePos();
            if (home == null) return true;
            return drudge.distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5) < ARRIVED_DIST_SQ;
        }
    }

    /**
     * The core goal that executes the equipped blood memory. Determines a work
     * target based on the manipulation's {@link EnumBloodTendency}, drains
     * {@code bloodCharge}, then applies a tendency-specific effect.
     *
     * <p>Passive mode: fires automatically when cooldown expires.
     * Commanded mode: only fires when an electrode signal is queued.
     */
    private static class DrudgeExecuteMemoryGoal extends Goal {
        private final DrudgeEntity drudge;
        private int recalcTimer;

        DrudgeExecuteMemoryGoal(DrudgeEntity drudge) {
            this.drudge = drudge;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (drudge.isRogue()) return false;
            BloodManipulation mem = drudge.equippedMemory;
            if (mem == null) return false;
            if (drudge.getBloodCharge() < LOW_CHARGE_THRESHOLD) return false;
            boolean modeOk = drudge.isPassiveMode() || drudge.electrodeSignalPending;
            if (!modeOk) return false;
            if (!drudge.electrodeSignalPending && drudge.isMemoryOnCooldown()) return false;
            // Stay within leash range
            BlockPos home = drudge.getHomePos();
            if (home != null) {
                int leash = HemoServerConfig.DRUDGE_LEASH_RADIUS.get();
                double distSq = drudge.distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5);
                if (distSq > (double) leash * leash) return false;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return false; // one-shot per activation
        }

        @Override
        public void start() {
            BloodManipulation mem = drudge.equippedMemory;
            if (mem == null) return;

            drudge.electrodeSignalPending = false;

            // Determine work position: centre of the drudge + work radius scan
            int workRadius = HemoServerConfig.DRUDGE_WORK_RADIUS.get();
            BlockPos workPos = drudge.blockPosition();

            boolean executed = executeTendencyBehavior(mem, workPos, workRadius);

            if (executed) {
                // Drain blood charge and apply cooldown
                float costMult = HemoServerConfig.DRUDGE_ACTION_COST_MULTIPLIER.get().floatValue();
                drudge.drainBloodCharge((float) mem.getCost() * costMult);
                int cooldownMult = HemoServerConfig.DRUDGE_COOLDOWN_MULTIPLIER.get();
                drudge.memoryCooldownTicks = Math.max(20, mem.getCooldownTicks() * cooldownMult);

                // For CONTINUOUS manipulations, use a flat long cooldown
                if (mem.getType() == EnumManipulationType.CONTINUOUS) {
                    drudge.memoryCooldownTicks = Math.max(drudge.memoryCooldownTicks, 60);
                }
            }
        }

        /**
         * Applies a tendency-based simplified effect rather than calling the
         * manipulation's full {@code getAction()} (which requires a {@link Player}).
         *
         * @return {@code true} if the effect was fired (blood has been charged).
         */
        private boolean executeTendencyBehavior(BloodManipulation mem, BlockPos centre, int radius) {
            Level world = drudge.level();
            EnumBloodTendency tendency = mem.getTend();
            AABB area = new AABB(centre).inflate(radius);

            switch (tendency) {
                case MORTEM:
                case FLAMMEUS:
                case CONGEATIO: {
                    // Attack nearest hostile mob
                    List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
                    if (mobs.isEmpty()) return false;
                    Monster target = mobs.get(0);
                    double best = Double.MAX_VALUE;
                    for (Monster m : mobs) {
                        double d = drudge.distanceToSqr(m);
                        if (d < best) { best = d; target = m; }
                    }
                    drudge.setTarget(target);
                    target.hurt(world.damageSources().mobAttack(drudge),
                            (float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2);
                    return true;
                }

                case DUCTILIS:
                case ANIMUS: {
                    // Heal nearest damaged player (or self)
                    List<Player> players = world.getEntitiesOfClass(Player.class, area);
                    Player target = null;
                    float lowestHp = Float.MAX_VALUE;
                    for (Player p : players) {
                        if (p.getHealth() < p.getMaxHealth() && p.getHealth() < lowestHp) {
                            lowestHp = p.getHealth();
                            target = p;
                        }
                    }
                    if (target == null) {
                        // Heal self if below half health
                        if (drudge.getHealth() < drudge.getMaxHealth() * SELF_HEAL_THRESHOLD) {
                            drudge.heal(3.0f);
                            return true;
                        }
                        return false;
                    }
                    target.heal(4.0f);
                    spawnHealParticles(target);
                    return true;
                }

                case LUX: {
                    // Place a torch-equivalent glow at the darkest nearby air block (simplified)
                    if (!(world instanceof ServerLevel serverLevel)) return false;
                    BlockPos darkest = null;
                    int darkestLight = 15;
                    for (BlockPos p : BlockPos.betweenClosed(centre.offset(-radius/2, TORCH_SCAN_Y_MIN, -radius/2), centre.offset(radius/2, TORCH_SCAN_Y_MAX, radius/2))) {
                        if (world.getBlockState(p).isAir() && !world.getBlockState(p.below()).isAir()) {
                            int light = world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, p);
                            if (light < darkestLight) {
                                darkestLight = light;
                                darkest = p.immutable();
                            }
                        }
                    }
                    if (darkest == null || darkestLight >= MIN_LIGHT_FOR_TORCH) return false;
                    world.setBlockAndUpdate(darkest, net.minecraft.world.level.block.Blocks.TORCH.defaultBlockState());
                    return true;
                }

                case FERRIC: {
                    // Provide a passive mining haste aura — apply Haste effect to nearby players
                    List<Player> players = world.getEntitiesOfClass(Player.class, area);
                    if (players.isEmpty()) return false;
                    for (Player p : players) {
                        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.DIG_SPEED, 400, 0, false, true));
                    }
                    return true;
                }

                case TENEBRIS: {
                    // Apply stealth effect (Invisibility) to self
                    drudge.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.INVISIBILITY, 600, 0, false, false));
                    return true;
                }

                default:
                    return false;
            }
        }

        private void spawnHealParticles(LivingEntity target) {
            for (int i = 0; i < 6; i++) {
                double dx = (drudge.random.nextDouble() - 0.5) * 0.8;
                double dy = drudge.random.nextDouble() * 0.5 + 0.5;
                double dz = (drudge.random.nextDouble() - 0.5) * 0.8;
                target.level().addParticle(net.minecraft.core.particles.ParticleTypes.HEART,
                        target.getX() + dx, target.getY() + dy, target.getZ() + dz, 0, 0.05, 0);
            }
        }
    }
}
