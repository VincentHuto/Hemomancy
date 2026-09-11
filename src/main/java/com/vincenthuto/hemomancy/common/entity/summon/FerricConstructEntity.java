package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.Optional;
import java.util.UUID;

/** Player-shaped iron uses an absolute lifetime and solid geometry; legacy encounter modes stay separate. */
public abstract class FerricConstructEntity extends BloodConstructEntity {
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(FerricConstructEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FACING = SynchedEntityData.defineId(FerricConstructEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> BORN = SynchedEntityData.defineId(FerricConstructEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> EXPIRES = SynchedEntityData.defineId(FerricConstructEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> ENERGIZED = SynchedEntityData.defineId(FerricConstructEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(FerricConstructEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected FerricConstructEntity(EntityType<? extends PathfinderMob> type, net.minecraft.world.level.Level level) {
        super(type, level);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KIND, 0); builder.define(FACING, Direction.NORTH.get3DDataValue());
        builder.define(BORN, 0L); builder.define(EXPIRES, 0L); builder.define(ENERGIZED, 0L);
        builder.define(OWNER, Optional.empty());
    }

    public void configure(LivingEntity owner, Kind kind, Direction facing, int duration) {
        setCreator(owner);
        entityData.set(KIND, kind.ordinal()); entityData.set(FACING, facing.get3DDataValue());
        entityData.set(BORN, level().getGameTime()); entityData.set(EXPIRES, level().getGameTime() + duration);
        setNoGravity(true); refreshDimensions(); updateBounds();
    }

    public boolean isPlayerConstruct() { return entityData.get(KIND) != 0; }
    public Kind constructKind() { return Kind.values()[Math.clamp(entityData.get(KIND), 0, Kind.values().length - 1)]; }
    public Direction constructFacing() { return Direction.from3DDataValue(entityData.get(FACING)); }
    public long bornAt() { return entityData.get(BORN); }
    public long expiresAt() { return isPlayerConstruct() ? entityData.get(EXPIRES) : level().getGameTime() + Math.max(0, 120 - tickCount); }
    public long energizedUntil() { return entityData.get(ENERGIZED); }
    public void energize(long until) { entityData.set(ENERGIZED, Math.min(until, expiresAt())); }

    @Override public void setCreator(LivingEntity owner) {
        super.setCreator(owner); entityData.set(OWNER, Optional.ofNullable(owner).map(Entity::getUUID));
    }

    @Override public LivingEntity getCreator() {
        if (creator != null && creator.isAlive() && creator.level() == level()) return creator;
        creator = null;
        var id = entityData.get(OWNER);
        if (id.isPresent() && level() instanceof ServerLevel server && server.getEntity(id.get()) instanceof LivingEntity living)
            creator = living;
        return creator;
    }

    @Override protected boolean usesLegacyConstructLifecycle() { return !isPlayerConstruct(); }
    @Override public boolean canBeCollidedWith() { return isPlayerConstruct() && isAlive() && level().getGameTime() < expiresAt(); }
    @Override public boolean canCollideWith(Entity other) { return canBeCollidedWith() && !isPassengerOfSameVehicle(other); }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isPushedByFluid() { return !isPlayerConstruct() && super.isPushedByFluid(); }
    @Override public void move(net.minecraft.world.entity.MoverType type, net.minecraft.world.phys.Vec3 delta) {
        if(!isPlayerConstruct())super.move(type,delta);
    }
    @Override protected void dropAllDeathLoot(ServerLevel level,net.minecraft.world.damagesource.DamageSource source) {
        if(!isPlayerConstruct())super.dropAllDeathLoot(level,source);
    }
    @Override protected void doPush(Entity other) { }

    private void updateBounds() {
        if (isPlayerConstruct()) setBoundingBox(FerricConstructShapes.bounds(constructKind(), constructFacing(), position()));
    }

    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (KIND.equals(key) || FACING.equals(key)) updateBounds();
    }

    @Override public void tick() {
        if(isPlayerConstruct())setDeltaMovement(0,0,0);
        super.tick();
        if (!level().isClientSide) com.vincenthuto.hemomancy.common.manipulation.ductilis.ConductionManager.tickFerric(this);
        if (!isPlayerConstruct()) return;
        setDeltaMovement(0, 0, 0); updateBounds();
        if (!(level() instanceof ServerLevel server)) return;
        long now = level().getGameTime();
        LivingEntity owner = getCreator();
        if (now >= expiresAt() || owner == null || !owner.isAlive() || owner.level() != level()) {
            discard(); return;
        }
        if (constructKind() == Kind.WALL) {
            // A projectile may traverse the thin plate between ticks: test its next segment too.
            for (Projectile shot : server.getEntitiesOfClass(Projectile.class, getBoundingBox().inflate(4), Entity::isAlive)) {
                var box = getBoundingBox().inflate(shot.getBbWidth() * .5);
                if (box.contains(shot.position()) || box.clip(shot.position(), shot.position().add(shot.getDeltaMovement())).isPresent()) shot.discard();
            }
        } else if (constructKind() == Kind.SPIKE && now - bornAt() >= 6 && owner instanceof Player player) {
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(.12),
                    target -> !(target instanceof IBloodConstruct) && ManipulationCombatHelper.canHarm(player, target))) {
                if (com.vincenthuto.hemomancy.common.manipulation.ferric.FerricContacts.claim(server,player,target)) {
                    target.hurt(damageSources().indirectMagic(this, player), 3.5F);
                }
            }
        }
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FerricShape", entityData.get(KIND)); tag.putInt("FerricFacing", entityData.get(FACING));
        tag.putLong("FerricBorn", bornAt()); tag.putLong("FerricExpiry", entityData.get(EXPIRES));
        tag.putLong("FerricEnergized", energizedUntil()); entityData.get(OWNER).ifPresent(id -> tag.putUUID("FerricOwner", id));
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(KIND, Math.clamp(tag.getInt("FerricShape"), 0, Kind.values().length - 1));
        entityData.set(FACING, tag.getInt("FerricFacing")); entityData.set(BORN, tag.getLong("FerricBorn"));
        entityData.set(EXPIRES, tag.getLong("FerricExpiry")); entityData.set(ENERGIZED, tag.getLong("FerricEnergized"));
        entityData.set(OWNER, tag.hasUUID("FerricOwner") ? Optional.of(tag.getUUID("FerricOwner")) : Optional.empty());
        if (isPlayerConstruct()) { setNoGravity(true); updateBounds(); }
    }
}
