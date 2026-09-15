package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedSagittaryEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.worldgen.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;

public class RecallBarbEntity extends AbstractArrow {
    private static final EntityDataAccessor<Integer> VICTIM=SynchedEntityData.defineId(RecallBarbEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ARCHER=SynchedEntityData.defineId(RecallBarbEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AGE=SynchedEntityData.defineId(RecallBarbEntity.class,EntityDataSerializers.INT);
    private boolean tether=true,reloaded;
    private int heldSneak;
    private BlockPos pullAnchor;
    private final java.util.Set<Integer> pierced=new java.util.HashSet<>();
    public RecallBarbEntity(EntityType<? extends RecallBarbEntity> type,Level level) {super(type,level);pickup=Pickup.DISALLOWED;}
    public RecallBarbEntity(Level level,ExcoriatedSagittaryEntity owner,boolean tether) {
        super(EntityInit.recall_barb.get(),owner,level,new ItemStack(Items.BONE),null);
        this.tether=tether;pickup=Pickup.DISALLOWED;entityData.set(ARCHER,owner.getId());
        double yaw=Math.toRadians(owner.yBodyRot);
        Vec3 forward=new Vec3(-Math.sin(yaw),0,Math.cos(yaw));
        Vec3 left=new Vec3(Math.cos(yaw),0,Math.sin(yaw));
        setPos(owner.position().add(0,2.15,0).add(forward.scale(1.5)).add(left.scale(.35)));
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);builder.define(VICTIM,-1);builder.define(ARCHER,-1);builder.define(AGE,0);
    }
    public int victimId() {return entityData.get(VICTIM);}
    public int archerId() {return entityData.get(ARCHER);}
    public int tetherAge() {return entityData.get(AGE);}
    @Override protected ItemStack getDefaultPickupItem() {return new ItemStack(Items.BONE);}
    @Override public byte getPierceLevel() {return (byte)(tether?0:1);}
    @Override protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof ExcoriatedSagittaryEntity) && !pierced.contains(entity.getId()) && super.canHitEntity(entity);
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if(level().isClientSide || !(hit.getEntity() instanceof LivingEntity victim) || victim instanceof ExcoriatedSagittaryEntity)return;
        boolean damaged=victim.hurt(damageSources().arrow(this,getOwner()),4);
        if(!tether) {
            pierced.add(victim.getId());
            if(pierced.size()>=2)discard();
            return;
        }
        if(!damaged || !victim.isAlive()) {discard();return;}
        if(victim instanceof Player p && (p.isCreative()||p.isSpectator())) {discard();return;}
        if(!level().getEntitiesOfClass(RecallBarbEntity.class,victim.getBoundingBox().inflate(32),
                barb -> barb!=this && barb.victimId()==victim.getId()).isEmpty()) {discard();return;}
        entityData.set(VICTIM,victim.getId());setNoPhysics(true);setDeltaMovement(Vec3.ZERO);
        if(victim instanceof ServerPlayer player)player.displayClientMessage(Component.translatable("message.hemomancy.recall_barb_escape"),true);
    }
    @Override protected void onHitBlock(BlockHitResult hit) {super.onHitBlock(hit);if(!level().isClientSide)discard();}
    @Override public void tick() {
        if(reloaded && !level().isClientSide) {discard();return;}
        if(victimId()<0) {super.tick();if(!level().isClientSide && tickCount>100)discard();return;}
        baseTick();
        Entity found=level().getEntity(victimId());
        if(!(found instanceof LivingEntity victim)) {if(!level().isClientSide)discard();return;}
        setPos(victim.getBoundingBox().getCenter());setDeltaMovement(Vec3.ZERO);
        if(level().isClientSide)return;
        Entity owner=getOwner();
        int age=tetherAge()+1;entityData.set(AGE,age);
        if(age>80 || owner==null || owner.isRemoved() || !owner.isAlive() || !victim.isAlive()
                || victim.level()!=level() || owner.level()!=level() || owner.distanceToSqr(victim)>32*32
                || victim instanceof Player player && (player.isCreative()||player.isSpectator()||player.isRemoved())) {discard();return;}
        heldSneak=victim.isShiftKeyDown()?heldSneak+1:0;
        if(heldSneak>=20) {sever(victim);discard();return;}
        if(age<20)return;
        if(age==20) {
            double ratio=victim.getHealth()/Math.max(1,victim.getMaxHealth());
            if(victim instanceof Player player) {
                var volume=HemoCapabilityAccess.getBloodVolume(player).orElse(null);
                if(volume!=null && volume.isActive() && volume.getMaxBloodVolume()>0)ratio=volume.getBloodVolume()/volume.getMaxBloodVolume();
            }
            if(PhlegethonticRules.pullToRiver(ratio))pullAnchor=deeperIchor(victim.blockPosition());
            return;
        }
        Vec3 destination=pullAnchor!=null && level().getFluidState(pullAnchor).is(PhlegethonticTags.ICHOR)
                ?Vec3.atCenterOf(pullAnchor):owner.position();
        Vec3 direction=destination.subtract(victim.position());
        if(direction.lengthSqr()<1)return;
        Vec3 pull=direction.normalize().scale(.055);
        Vec3 velocity=victim.getDeltaMovement().add(pull.x,Math.clamp(pull.y,-.025,.025),pull.z);
        double horizontal=velocity.horizontalDistance();
        if(horizontal>.35)velocity=new Vec3(velocity.x*.35/horizontal,velocity.y,velocity.z*.35/horizontal);
        victim.setDeltaMovement(velocity);victim.hurtMarked=true;
    }
    private BlockPos deeperIchor(BlockPos origin) {
        BlockPos best=null;double score=Double.NEGATIVE_INFINITY;
        for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)for(int y=-6;y<=1;y++) {
            if(x*x+z*z>64)continue;BlockPos p=origin.offset(x,y,z);
            if(!level().hasChunkAt(p) || !level().getFluidState(p).is(PhlegethonticTags.ICHOR))continue;
            double value=-y*3-Math.sqrt(x*x+z*z);
            if(value>score){score=value;best=p;}
        }
        return best;
    }
    private void sever(LivingEntity victim) {
        if(victim instanceof ServerPlayer player) {
            IBloodVolume volume=HemoCapabilityAccess.getBloodVolume(player).orElse(null);
            if(volume!=null && volume.isActive()) {
                double cost=PhlegethonticRules.severCost(volume.getMaxBloodVolume());
                if(cost>0 && volume.getBloodVolume()>=cost) {
                    BloodFlowLedger.applyDrain(player,volume,"recall_barb","Recall Barb",BloodFlowContribution.Category.EFFECT,
                            cost,1,false);return;
                }
            }
        }
        victim.hurt(damageSources().generic(),1);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {super.addAdditionalSaveData(tag);tag.putBoolean("RecallBarbTransient",true);}
    @Override public void readAdditionalSaveData(CompoundTag tag) {super.readAdditionalSaveData(tag);reloaded=true;}
}
