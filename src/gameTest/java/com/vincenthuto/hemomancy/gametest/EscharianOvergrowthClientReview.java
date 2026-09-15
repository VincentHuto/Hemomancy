package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.Cell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

final class EscharianOvergrowthClientReview {
    static void place(ServerPlayer player) {
        var level=player.getServer().getLevel(Level.NETHER);
        if(player.level()!=level)throw new IllegalStateException("Enter the Nether review area before running the fixture");
        player.setGameMode(GameType.SURVIVAL);
        BlockPos acceptanceOrigin=new BlockPos(1000,202,1000);
        for(int chunkX=62;chunkX<=64;chunkX++)for(int chunkZ=61;chunkZ<=63;chunkZ++)level.getChunk(chunkX,chunkZ);
        player.teleportTo(level,acceptanceOrigin.getX(),acceptanceOrigin.getY()+3,acceptanceOrigin.getZ(),java.util.Set.of(),0,45);
        try { EscharianScyphusAcceptance.run(player,acceptanceOrigin); }
        finally { player.setGameMode(GameType.SPECTATOR); }
        for(Direction face:Direction.values()) {
            BlockPos support=new BlockPos(1000+face.ordinal()*4,202,1000);
            level.setBlock(support,Blocks.STONE.defaultBlockState(),3);
            level.setBlock(support.relative(face),BlockInit.escharian_scyphus.get().defaultBlockState()
                    .setValue(EscharianScyphusBlock.FACING,face),3);
            level.setBlock(support.offset(0,4,0),Blocks.LIGHT.defaultBlockState(),3);
        }
        for(int count=1;count<=5;count++) {
            BlockPos support=new BlockPos(996+count*4,202,1010);
            level.setBlock(support,BlockInit.escharian_overgrowth.get().defaultBlockState(),3);
            level.setBlock(support.above(),BlockInit.escharian_scyphus.get().defaultBlockState()
                    .setValue(EscharianScyphusBlock.COUNT,count),3);
            level.setBlock(support.above(4),Blocks.LIGHT.defaultBlockState(),3);
        }
        for(int index=0;index<4;index++) {
            Cell anchor=new Cell(1000+index*18,200,1020);
            var plan=EscharianOvergrowthLayout.pile(index,anchor,c -> c.y()<200,c -> c.y()>=200);
            for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)level.setBlock(new BlockPos(anchor.x()+x,199,anchor.z()+z),BlockInit.blood_scorched_scab.get().defaultBlockState(),3);
            var substrate=com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthSubstrate.generate(index,plan,
                    c -> level.getBlockState(new BlockPos(c.x(),c.y(),c.z())).is(com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTags.OVERGROWTH_SUPPORT),
                    c -> level.isEmptyBlock(new BlockPos(c.x(),c.y(),c.z())));
            substrate.forEach((c,material) -> level.setBlock(new BlockPos(c.x(),c.y(),c.z()),
                    (material==com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthSubstrate.Material.INFESTED
                            ? BlockInit.infested_venous_stone.get():BlockInit.venous_stone.get()).defaultBlockState(),3));
            plan.backing().forEach((c,layer) -> level.setBlock(new BlockPos(c.x(),c.y(),c.z()),
                    (layer==EscharianOvergrowthLayout.Layer.RIM?BlockInit.escharian_overgrowth_rim.get():BlockInit.escharian_overgrowth.get()).defaultBlockState(),3));
            plan.plants().forEach((c,plant) -> level.setBlock(new BlockPos(c.x(),c.y(),c.z()),
                    BlockInit.escharian_scyphus.get().defaultBlockState()
                            .setValue(EscharianScyphusBlock.FACING,plant.facing())
                            .setValue(EscharianScyphusBlock.COUNT,plant.count()),3));
            level.setBlock(new BlockPos(anchor.x(),206,anchor.z()),Blocks.LIGHT.defaultBlockState(),3);
        }
        player.setGameMode(GameType.SPECTATOR);
        player.teleportTo(level,1010,208,1032,java.util.Set.of(),180,32);
    }
}
