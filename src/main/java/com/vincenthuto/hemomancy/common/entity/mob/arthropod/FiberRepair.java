package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RotatedPillarBlock;

/** The shared one-block repair rule for Borers and Naeglerophaeon. */
public final class FiberRepair {
    private FiberRepair() {}

    static boolean[] neighbourFlags(LevelReader level,BlockPos pos) {
        return new boolean[] {
                level.getBlockState(pos.west()).is(MyelinBorerEntity.CRAWLABLE),
                level.getBlockState(pos.east()).is(MyelinBorerEntity.CRAWLABLE),
                level.getBlockState(pos.below()).is(MyelinBorerEntity.CRAWLABLE),
                level.getBlockState(pos.above()).is(MyelinBorerEntity.CRAWLABLE),
                level.getBlockState(pos.north()).is(MyelinBorerEntity.CRAWLABLE),
                level.getBlockState(pos.south()).is(MyelinBorerEntity.CRAWLABLE) };
    }

    public static int gapAxis(LevelReader level,BlockPos pos) {
        return level.getBlockState(pos).isAir() ? FiberGapRules.gapAxis(neighbourFlags(level,pos)) : -1;
    }

    public static boolean repair(ServerLevel level,BlockPos pos) {
        int axis=gapAxis(level,pos);
        if(axis<0) return false;
        Direction.Axis direction=switch(axis) {
            case 0 -> Direction.Axis.X;
            case 1 -> Direction.Axis.Y;
            default -> Direction.Axis.Z;
        };
        return level.setBlock(pos,BlockInit.nerve_fiber.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS,direction),3);
    }
}
