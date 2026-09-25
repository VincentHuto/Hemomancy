package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumLayout;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ScriptoriumRites {
    private static final String EIGHTFOLD = "cardinal_rite/eightfold_script";
    private static final String MONOLITHIC = "cardinal_rite/monolithic_script";
    private static final int[][] ORBS = {{0, -3}, {2, -2}, {3, 0}, {2, 2}, {0, 3}, {-2, 2}, {-3, 0}, {-2, -2}};

    private ScriptoriumRites() {}

    public static boolean isRite(ResourceLocation recipe) {
        return recipe.getNamespace().equals("hemomancy")
                && (recipe.getPath().equals(EIGHTFOLD) || recipe.getPath().equals(MONOLITHIC));
    }

    public static boolean isMonolithic(ResourceLocation recipe) {
        return recipe.getNamespace().equals("hemomancy") && recipe.getPath().equals(MONOLITHIC);
    }

    public static BlockPos seat(ActiveCardinalRite rite) {
        Direction forward = rite.getFloorForwards();
        return rite.getCenterPos().above().relative(forward == null ? Direction.NORTH : forward.getOpposite(), 2);
    }

    public static BlockPos orb(ActiveCardinalRite rite, int index) {
        Direction forward = rite.getFloorForwards() == null ? Direction.SOUTH : rite.getFloorForwards();
        var right = forward.getNormal().cross(Direction.UP.getNormal());
        int[] local = ORBS[index];
        BlockPos center = rite.getCenterPos();
        return center.offset(right.getX() * local[0] + forward.getStepX() * local[1],
                0, right.getZ() * local[0] + forward.getStepZ() * local[1]);
    }

    public static Vec3 orbSurface(ServerLevel level, ActiveCardinalRite rite, int index) {
        BlockPos floor = orb(rite, index);
        BlockPos center = rite.getCenterPos();
        BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(level, center,
                floor.getX() - center.getX(), floor.getZ() - center.getZ());
        return new Vec3(floor.getX() + 0.5D, surface.getY() + 0.10D, floor.getZ() + 0.5D);
    }

    public static Vec3 tubePoint(ServerLevel level, ActiveCardinalRite rite, int index) {
        BlockPos stationPos = seat(rite);
        double x = ScriptoriumLayout.tubeX(index);
        double z = ScriptoriumLayout.tubeZ(index);
        Direction facing = level.getBlockState(stationPos).hasProperty(EnzymaticScriptoriumBlock.FACING)
                ? level.getBlockState(stationPos).getValue(EnzymaticScriptoriumBlock.FACING) : Direction.NORTH;
        double rotatedX = x, rotatedZ = z;
        switch (facing) {
            case EAST -> { rotatedX = 1 - z; rotatedZ = x; }
            case SOUTH -> { rotatedX = 1 - x; rotatedZ = 1 - z; }
            case WEST -> { rotatedX = z; rotatedZ = 1 - x; }
            default -> { }
        }
        return new Vec3(stationPos.getX() + rotatedX, stationPos.getY() + ScriptoriumLayout.tubeY(), stationPos.getZ() + rotatedZ);
    }

    public static EnzymaticScriptoriumBlockEntity station(ServerLevel level, ActiveCardinalRite rite) {
        return level.getBlockEntity(seat(rite)) instanceof EnzymaticScriptoriumBlockEntity station ? station : null;
    }

    public static boolean validSubject(ServerLevel level, ActiveCardinalRite rite) {
        if (!isRite(rite.getRecipeId())) return true;
        EnzymaticScriptoriumBlockEntity station = station(level, rite);
        int sourceTier = isMonolithic(rite.getRecipeId()) ? 5 : 3;
        int each = isMonolithic(rite.getRecipeId()) ? 2 : 1;
        return station != null && station.getBlockState().getBlock() instanceof EnzymaticScriptoriumBlock block
                && EnzymaticScriptoriumBlock.tier(station.getBlockState()) == sourceTier && station.hasRiteEnzymes(each);
    }

    public static boolean prepare(ServerLevel level, ActiveCardinalRite rite) {
        if (!isRite(rite.getRecipeId())) return true;
        if (!validSubject(level, rite)) return false;
        station(level, rite).setRiteLocked(true);
        return true;
    }

    public static void cleanup(ServerLevel level, ActiveCardinalRite rite) {
        if (!isRite(rite.getRecipeId())) return;
        EnzymaticScriptoriumBlockEntity station = station(level, rite);
        if (station != null) station.setRiteLocked(false);
    }

    public static boolean complete(ServerLevel level, ActiveCardinalRite rite) {
        if (!isRite(rite.getRecipeId())) return true;
        if (!validSubject(level, rite) || rite.getScriptorialStage() < (isMonolithic(rite.getRecipeId()) ? 16 : 8)) return false;
        EnzymaticScriptoriumBlockEntity old = station(level, rite);
        old.consumeRiteEnzymes(isMonolithic(rite.getRecipeId()) ? 2 : 1);
        BlockState replacement = old.getBlockState().setValue(EnzymaticScriptoriumBlock.STAGE,
                isMonolithic(rite.getRecipeId()) ? 2 : 1);
        level.setBlock(seat(rite), replacement, 3);
        old.setRiteLocked(false);
        return true;
    }
}
