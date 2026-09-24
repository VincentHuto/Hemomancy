package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedSpawnRules;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("excoriated_spawn_validation")
@PrefixGameTestTemplate(false)
public final class ExcoriatedSpawnGameTests {
    @GameTest(template="empty",batch="excoriated_spawn",timeoutTicks=60)
    public static void shoreSupportsRaisedBanksAndRequiresClearFooting(GameTestHelper h) {
        var level=h.getLevel();
        BlockPos anchor=h.absolutePos(new BlockPos(8,4,8));
        var biome=level.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(BiomeInit.PHLEGETHONTIC_BASIN);
        for(int x=(anchor.getX()-16)>>4;x<=(anchor.getX()+16)>>4;x++)
            for(int z=(anchor.getZ()-16)>>4;z<=(anchor.getZ()+16)>>4;z++)
                level.getChunk(x,z).fillBiomesFromNoise((qx,qy,qz,sampler) -> biome,
                        level.getChunkSource().randomState().sampler());
        for(int x=4;x<13;x++)for(int z=4;z<13;z++)
            h.setBlock(new BlockPos(x,3,z),BlockInit.blood_scorched_scab.get());
        for(int x=6;x<=10;x++)for(int z=6;z<=10;z++)for(int y=4;y<=7;y++)
            h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        h.setBlock(new BlockPos(12,1,8),BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get());
        h.assertTrue(ExcoriatedSpawnRules.validShore(level,anchor),
                "Raised shore three blocks above ichor must permit spawning");
        h.setBlock(new BlockPos(8,6,8),Blocks.BLACKSTONE);
        h.assertTrue(!ExcoriatedSpawnRules.validShore(level,anchor),"Third block of headroom is required");
        h.setBlock(new BlockPos(8,6,8),Blocks.AIR);
        h.setBlock(new BlockPos(7,3,8),Blocks.AIR);
        h.assertTrue(!ExcoriatedSpawnRules.validShore(level,anchor),"Missing edge support must reject the full footprint");
        h.succeed();
    }
}
