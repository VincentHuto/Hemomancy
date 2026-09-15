package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;

public final class ExcoriatedSagittarySpawnRules {
    private ExcoriatedSagittarySpawnRules() {}
    public static boolean canSpawn(EntityType<ExcoriatedSagittaryEntity> type,ServerLevelAccessor level,
                                   MobSpawnType reason,BlockPos pos,RandomSource random) { return validShore(level,pos); }
    public static boolean validShore(ServerLevelAccessor level,BlockPos pos) {
        if(level.getDifficulty()==Difficulty.PEACEFUL || !level.getBiome(pos).is(BiomeInit.PHLEGETHONTIC_BASIN)) return false;
        AABB box=new AABB(pos.getX()+.5-.775,pos.getY(),pos.getZ()+.5-.775,pos.getX()+.5+.775,pos.getY()+2.7,pos.getZ()+.5+.775);
        if(!level.noCollision(box)) return false;
        for(int x=(int)Math.floor(box.minX);x<Math.ceil(box.maxX);x++) for(int z=(int)Math.floor(box.minZ);z<Math.ceil(box.maxZ);z++) {
            BlockPos floor=new BlockPos(x,pos.getY()-1,z);
            if(!level.getBlockState(floor).is(PhlegethonticTags.SHORE)
                    || !level.getBlockState(floor).isFaceSturdy(level,floor,Direction.UP)) return false;
        }
        return nearestIchor(level,pos,8,2)!=null;
    }
    @Nullable public static BlockPos nearestIchor(net.minecraft.world.level.LevelReader level,BlockPos pos,int radius,int vertical) {
        BlockPos best=null;double distance=Double.POSITIVE_INFINITY;
        BlockPos.MutableBlockPos probe=new BlockPos.MutableBlockPos();
        for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
            if(x*x+z*z>radius*radius) continue;
            for(int y=-vertical;y<=vertical;y++) {
                probe.setWithOffset(pos,x,y,z);
                if(!level.hasChunk(probe.getX()>>4,probe.getZ()>>4)) continue;
                if(level.getFluidState(probe).is(PhlegethonticTags.ICHOR)) {
                    double d=x*x+y*y+z*z;
                    if(d<distance){distance=d;best=probe.immutable();}
                }
            }
        }
        return best;
    }
}
