package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import com.vincenthuto.hemomancy.common.worldgen.terrain.phlegethontic.PhlegethonticTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

/** The Phlegethosoma no longer spawns naturally (CR-7); a summons still asks for a pool broad enough to hold it. */
public final class PhlegethosomaSpawnRules {
    private PhlegethosomaSpawnRules() {}

    /** A broad connected pool, rather than a fully supported dry bank. No fluids are placed or moved. */
    public static boolean validPool(LevelAccessor level, BlockPos pos) {
        for (int x=-3;x<=3;x++) for (int z=-3;z<=3;z++) {
            BlockPos sample=pos.offset(x,0,z);
            if (!level.hasChunk(sample.getX()>>4,sample.getZ()>>4)
                    || !level.getFluidState(sample).is(PhlegethonticTags.ICHOR)) return false;
        }
        return level.noCollision(new AABB(pos.getX()-.2,pos.getY(),pos.getZ()-.2,
                pos.getX()+1.2,pos.getY()+.85,pos.getZ()+1.2));
    }
}
