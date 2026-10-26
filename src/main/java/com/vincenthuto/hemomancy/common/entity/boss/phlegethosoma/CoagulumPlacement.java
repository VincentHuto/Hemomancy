package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Preserve existing local exits when adding temporary cover, including cover from other flukes. */
public final class CoagulumPlacement {
    private CoagulumPlacement() {}
    public static boolean preservesExits(Level level,AABB proposed) {
        for(LivingEntity actor:level.getEntitiesOfClass(LivingEntity.class,proposed.inflate(5))) {
            if(actor instanceof PhlegethosomaEntity)continue;
            if(exit(level,actor,null) && !exit(level,actor,proposed))return false;
        }
        return true;
    }
    private static boolean exit(Level level,LivingEntity actor,AABB proposed) {
        BlockPos origin=actor.blockPosition();Set<BlockPos> visited=new HashSet<>();ArrayDeque<BlockPos> pending=new ArrayDeque<>();
        pending.add(origin);visited.add(origin);
        while(!pending.isEmpty()) {
            BlockPos p=pending.removeFirst();
            if(Math.abs(p.getX()-origin.getX())>=4 || Math.abs(p.getZ()-origin.getZ())>=4)return true;
            for(var direction:net.minecraft.core.Direction.Plane.HORIZONTAL) {
                for(int dy:new int[]{0,1,-1}) {
                    BlockPos q=p.relative(direction).above(dy);
                    if(Math.abs(q.getY()-origin.getY())>2 || visited.contains(q) || !level.hasChunkAt(q))continue;
                    Vec3 shift=Vec3.atBottomCenterOf(q).subtract(actor.position());AABB bounds=actor.getBoundingBox().move(shift);
                    if(proposed!=null && proposed.intersects(bounds) || !level.noCollision(actor,bounds))continue;
                    if(level.getBlockState(q.below()).getCollisionShape(level,q.below()).isEmpty())continue;
                    visited.add(q);pending.add(q);break;
                }
            }
        }
        return false;
    }
}
