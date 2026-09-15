package com.vincenthuto.hemomancy.common.block.harbinger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;

/** Pointed-dripstone geometry whose support chain recognizes this custom block. */
public final class BloodScabPointedDripstoneBlock extends PointedDripstoneBlock {
    public BloodScabPointedDripstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canSurvive(BlockState state,LevelReader level,BlockPos pos) {
        Direction direction=state.getValue(TIP_DIRECTION);
        BlockPos supportPos=pos.relative(direction.getOpposite());
        BlockState support=level.getBlockState(supportPos);
        return support.isFaceSturdy(level,supportPos,direction) || sameDirection(support,direction);
    }

    @Override
    protected BlockState updateShape(BlockState state,Direction changedSide,BlockState neighbor,
                                     LevelAccessor level,BlockPos pos,BlockPos neighborPos) {
        if(state.getValue(WATERLOGGED))level.scheduleTick(pos,Fluids.WATER,Fluids.WATER.getTickDelay(level));
        if(changedSide.getAxis()!=Direction.Axis.Y)return state;
        Direction direction=state.getValue(TIP_DIRECTION);
        if(changedSide==direction.getOpposite() && !canSurvive(state,level,pos))level.scheduleTick(pos,this,1);
        BlockState ahead=level.getBlockState(pos.relative(direction));
        BlockState behind=level.getBlockState(pos.relative(direction.getOpposite()));
        boolean sameAhead=sameDirection(ahead,direction),sameBehind=sameDirection(behind,direction);
        DripstoneThickness aheadThickness=sameAhead?ahead.getValue(THICKNESS):DripstoneThickness.TIP;
        return state.setValue(THICKNESS,BloodScabDripstoneProfile.connectedThickness(sameAhead,sameBehind,aheadThickness));
    }

    @Override
    protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        if(!canSurvive(state,level,pos))level.destroyBlock(pos,true);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred=context.getNearestLookingVerticalDirection().getOpposite();
        Direction direction=validPlacement(context.getLevel(),context.getClickedPos(),preferred)?preferred:preferred.getOpposite();
        if(!validPlacement(context.getLevel(),context.getClickedPos(),direction))return null;
        BlockState ahead=context.getLevel().getBlockState(context.getClickedPos().relative(direction));
        BlockState behind=context.getLevel().getBlockState(context.getClickedPos().relative(direction.getOpposite()));
        boolean sameAhead=sameDirection(ahead,direction),sameBehind=sameDirection(behind,direction);
        DripstoneThickness aheadThickness=sameAhead?ahead.getValue(THICKNESS):DripstoneThickness.TIP;
        return defaultBlockState().setValue(TIP_DIRECTION,direction)
                .setValue(THICKNESS,BloodScabDripstoneProfile.connectedThickness(sameAhead,sameBehind,aheadThickness))
                .setValue(WATERLOGGED,context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    private boolean validPlacement(LevelReader level,BlockPos pos,Direction direction) {
        BlockPos supportPos=pos.relative(direction.getOpposite());
        BlockState support=level.getBlockState(supportPos);
        return support.isFaceSturdy(level,supportPos,direction) || sameDirection(support,direction);
    }

    private boolean sameDirection(BlockState state,Direction direction) {
        return state.is(this) && state.getValue(TIP_DIRECTION)==direction;
    }

}
