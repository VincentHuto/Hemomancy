package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Shared conservative sweep used before forced movement on both sides. */
public final class PhlegethosomaMovement {
    private PhlegethosomaMovement() {}
    public static boolean safe(Entity entity,Vec3 movement) {
        var level=entity.level();
        for(int i=1;i<=4;i++) {
            Vec3 step=movement.scale(i/4.0);
            var bounds=entity.getBoundingBox().move(step);
            if(!level.hasChunkAt(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ))
                    || !level.hasChunkAt(BlockPos.containing(bounds.maxX,bounds.minY,bounds.maxZ))
                    || !level.noCollision(entity,bounds))return false;
            for(int x=(int)Math.floor(bounds.minX);x<=Math.floor(bounds.maxX);x++)
                for(int z=(int)Math.floor(bounds.minZ);z<=Math.floor(bounds.maxZ);z++) {
                    BlockPos feet=BlockPos.containing(x,bounds.minY,z);
                    if(!level.hasChunkAt(feet) || level.getFluidState(feet).is(FluidTags.LAVA))return false;
                    boolean supported=false;
                    for(int y=0;y<=2;y++) {
                        var p=feet.below(y);
                        if(level.getFluidState(p).is(FluidTags.LAVA))return false;
                        if(!level.getBlockState(p).getCollisionShape(level,p).isEmpty())supported=true;
                    }
                    if(!supported)return false;
                }
        }
        return true;
    }
}
