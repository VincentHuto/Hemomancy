package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import com.vincenthuto.hemomancy.common.init.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

public final class CoagulumProjectileEntity extends ThrowableItemProjectile {
    public CoagulumProjectileEntity(EntityType<? extends CoagulumProjectileEntity> type,Level level) {super(type,level);}
    @Override protected Item getDefaultItem() {return ItemInit.sanguine_formation.get();}
    @Override public void tick() {super.tick();if(!level().isClientSide && (tickCount>80 || !(getOwner() instanceof PhlegethosomaEntity b) || !b.isAlive()))discard();}
    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);if(!(level() instanceof ServerLevel s))return;
        if(getOwner() instanceof PhlegethosomaEntity boss && boss.isAlive()) {
            BlockPos pos=BlockPos.containing(hit.getLocation().add(0,.1,0));
            for(int i=0;i<3 && level().getBlockState(pos.below()).getCollisionShape(level(),pos.below()).isEmpty();i++)pos=pos.below();
            var clot=new TemporaryCoagulumEntity(EntityInit.temporary_coagulum.get(),level());clot.setPos(Vec3.atBottomCenterOf(pos));
            boolean clear=level().noCollision(clot,clot.getBoundingBox().inflate(.3))
                && level().getEntities(clot,clot.getBoundingBox().inflate(2)).stream().noneMatch(e->e instanceof LivingEntity || e instanceof TemporaryCoagulumEntity)
                && !level().getBlockState(pos.below()).getCollisionShape(level(),pos.below()).isEmpty();
            if(clear && CoagulumPlacement.preservesExits(level(),clot.getBoundingBox()) && s.getEntitiesOfClass(TemporaryCoagulumEntity.class,boss.getBoundingBox().inflate(64),c->boss.getUUID().equals(c.ownerId())).size()<6) {
                clot.configure(boss);s.addFreshEntity(clot);
            }
        }
        discard();
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {super.readAdditionalSaveData(tag);discard();}
}
