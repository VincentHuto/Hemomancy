package com.vincenthuto.hemomancy.common.succession;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Shared mortal lifecycle; originals retain their profession classes and protection. */
public abstract class ProfessionalHarbingerEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> SUCCESSOR = SynchedEntityData.defineId(ProfessionalHarbingerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<java.util.Optional<java.util.UUID>> SKIN_OWNER = SynchedEntityData.defineId(ProfessionalHarbingerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> MISBEGOTTEN = SynchedEntityData.defineId(ProfessionalHarbingerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> APPEARANCE = SynchedEntityData.defineId(ProfessionalHarbingerEntity.class, EntityDataSerializers.LONG);
    protected ProfessionalHarbingerEntity(EntityType<? extends PathfinderMob> type, Level level) { super(type, level); goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.2, true)); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(SUCCESSOR, false); builder.define(MISBEGOTTEN, false); builder.define(SKIN_OWNER, java.util.Optional.empty()); builder.define(APPEARANCE, 0L);
    }
    public boolean isSuccessor() { return entityData.get(SUCCESSOR); }
    public java.util.UUID skinOwner() { return entityData.get(SKIN_OWNER).orElse(null); }
    public boolean isMisbegotten() { return entityData.get(MISBEGOTTEN); }
    public void initializeMisbegotten(java.util.UUID officiant, long seed, String donorName) {
        entityData.set(MISBEGOTTEN, true); entityData.set(APPEARANCE, seed); setInvulnerable(false);
        getPersistentData().putUUID("FraudulentOfficiant", officiant); entityData.set(SKIN_OWNER, java.util.Optional.of(officiant));
        setCustomName(net.minecraft.network.chat.Component.translatable("entity.hemomancy.misbegotten_successor", donorName));
        setAggressive(true); setHealth(getMaxHealth()); setPersistenceRequired();
    }
    public long appearanceSeed() { return entityData.get(APPEARANCE); }
    public void initializeSuccessor(SuccessorRecord record) {
        setUUID(record.id); entityData.set(SUCCESSOR, true); entityData.set(APPEARANCE, record.seed);
        setInvulnerable(false); setPersistenceRequired(); setCustomName(net.minecraft.network.chat.Component.literal(record.name));
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putBoolean("ManifestedSuccessor", isSuccessor()); tag.putBoolean("Misbegotten", isMisbegotten()); if (skinOwner() != null) tag.putUUID("SkinOwner", skinOwner()); tag.putLong("SuccessorAppearance", appearanceSeed());
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); entityData.set(SUCCESSOR, tag.getBoolean("ManifestedSuccessor")); entityData.set(MISBEGOTTEN, tag.getBoolean("Misbegotten"));
        entityData.set(APPEARANCE, tag.getLong("SuccessorAppearance"));
        entityData.set(SKIN_OWNER, tag.hasUUID("SkinOwner") ? java.util.Optional.of(tag.getUUID("SkinOwner")) : java.util.Optional.empty());
        if (isSuccessor() || isMisbegotten()) { setInvulnerable(false); setPersistenceRequired(); }
    }
    @Override public void tick() {
        super.tick();
        if (isMisbegotten() && level() instanceof ServerLevel serverLevel) { MisbegottenBehavior.tick(serverLevel, this); return; }
        if (level() instanceof ServerLevel level && isAlive() && tickCount % 100 == Math.floorMod(getId(), 100))
            SuccessionResidents.tick(level, this);
    }
    @Override public void die(DamageSource source) {
        if (isSuccessor() && level() instanceof ServerLevel level) SuccessionResidents.died(level, this);
        super.die(source);
    }
    protected boolean successionInteraction(Player player, InteractionHand hand) {
        if (isMisbegotten()) return false;
        if (hand != InteractionHand.MAIN_HAND || !(player instanceof ServerPlayer serverPlayer) || !isSuccessor()) return true;
        if (!SuccessionResidents.mayServe(serverPlayer, this)) {
            SuccessionDialogue.idle(serverPlayer, this); return false;
        }
        var data = SuccessionSavedData.get(serverPlayer.serverLevel()); var record = data.residents.get(getUUID());
        if (record != null) { String key = player.getUUID().toString(); record.encounters.putInt(key, record.encounters.getInt(key) + 1); data.setDirty(); }
        return true;
    }
}
