package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import com.vincenthuto.hemomancy.common.entity.SyncedOwner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import java.util.UUID;

public final class TemporaryCoagulumEntity extends Entity {
    private static final SyncedOwner OWNER=SyncedOwner.define(TemporaryCoagulumEntity.class);
    private long expires;
    private float health=10;
    public TemporaryCoagulumEntity(EntityType<?> type,Level level) {super(type,level);setNoGravity(true);}
    public UUID ownerId() {return OWNER.get(this);}
    public void configure(PhlegethosomaEntity boss) {OWNER.set(this,boss.getUUID());expires=level().getGameTime()+160;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) {OWNER.defineDefault(b);}
    @Override public boolean canBeCollidedWith() {return isAlive();}
    @Override public boolean canCollideWith(Entity other) {return isAlive();}
    @Override public boolean isPickable() {return isAlive();}
    @Override public boolean isPushable() {return false;}
    @Override public boolean hurt(DamageSource source,float amount) {
        if(level().isClientSide || isInvulnerableTo(source) || amount<=0)return false;
        health-=amount;if(health<=0)discard();return true;
    }
    @Override public void tick() {
        super.tick();
        if(level() instanceof ServerLevel s && (s.getGameTime()>=expires || ownerId()==null
                || !(s.getEntity(ownerId()) instanceof PhlegethosomaEntity boss) || !boss.isAlive()
                || boss.action()==PhlegethosomaEntity.Action.DORMANT))discard();
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {OWNER.save(this,tag);tag.putLong("Expiry",expires);}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {OWNER.load(this,tag);expires=0;}
}
