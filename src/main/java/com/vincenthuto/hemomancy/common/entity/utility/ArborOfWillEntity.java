package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborOfWillVisualRules;
import com.vincenthuto.hemomancy.common.entity.SyncedOwner;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.OpenArborSkillsPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

/** Owner-bound, non-authoritative presentation anchor for the Chamber's Arbor of Will. */
public final class ArborOfWillEntity extends Entity {
    private static final SyncedOwner OWNER = SyncedOwner.define(ArborOfWillEntity.class);
    private static final EntityDataAccessor<Integer> DEGREE =
            SynchedEntityData.defineId(ArborOfWillEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> POMES =
            SynchedEntityData.defineId(ArborOfWillEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHAMBER_RADIUS =
            SynchedEntityData.defineId(ArborOfWillEntity.class, EntityDataSerializers.INT);

    public ArborOfWillEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        OWNER.defineDefault(builder);
        builder.define(DEGREE, 0);
        builder.define(POMES, 0);
        builder.define(CHAMBER_RADIUS, 4);
    }

    public void configure(ServerPlayer owner, int chamberRadius, int pomes) {
        OWNER.set(this, owner.getUUID());
        entityData.set(DEGREE, ArborOfWillVisualRules.visualDegree(HemoCapabilityAccess.getPlayerDegreeNumber(owner),
                HemoCapabilityAccess.getInitiatoryDegree(owner).getArchonPath()));
        entityData.set(POMES, Math.max(0, Math.min(9, pomes)));
        entityData.set(CHAMBER_RADIUS, Math.max(3, chamberRadius));
    }

    public Optional<UUID> ownerId() { return Optional.ofNullable(OWNER.get(this)); }
    public boolean isOwnedBy(Player player) { return ownerId().filter(player.getUUID()::equals).isPresent(); }
    public int degree() { return entityData.get(DEGREE); }
    public int pomesConsumed() { return entityData.get(POMES); }
    public int chamberRadius() { return entityData.get(CHAMBER_RADIUS); }

    @Override public void tick() { super.tick(); setDeltaMovement(0, 0, 0); }
    @Override public boolean isPickable() { return true; }
    @Override public boolean isInvulnerable() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override public void push(Entity entity) { }
    @Override public void push(double x, double y, double z) { }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !isOwnedBy(player)) return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(serverPlayer, new OpenArborSkillsPacket(-1));
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        OWNER.load(this, tag);
        entityData.set(DEGREE, tag.getInt("Degree"));
        entityData.set(POMES, tag.getInt("Pomes"));
        entityData.set(CHAMBER_RADIUS, Math.max(3, tag.getInt("ChamberRadius")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        OWNER.save(this, tag);
        tag.putInt("Degree", degree());
        tag.putInt("Pomes", pomesConsumed());
        tag.putInt("ChamberRadius", chamberRadius());
    }
}
