package com.vincenthuto.hemomancy.common.block.harbinger;

import com.vincenthuto.hemomancy.common.world.farm.ScabDrip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;

/**
 * Pointed-dripstone geometry whose support chain recognizes this custom block. A hanging spike drips blood that stands
 * fresh just above its root and feeds the ground below (core-loop Track 7, {@link ScabDrip}); it never drips water or
 * lava, and grows only where it is fed.
 */
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

    /** Only the drip: none of vanilla dripstone's fluid transfer or growth from water. */
    @Override
    protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        ScabDrip.drip(level,pos,state,random);
    }

    /** A player's hanging spike drips only from tanks they may draw (CQ-3). */
    @Override
    public void setPlacedBy(Level level,BlockPos pos,BlockState state,@Nullable LivingEntity placer,ItemStack stack) {
        super.setPlacedBy(level,pos,state,placer,stack);
        if(level instanceof ServerLevel server && placer instanceof Player player)ScabDrip.recordPlacer(server,pos,player.getUUID());
    }

    @Override
    protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState newState,boolean movedByPiston) {
        if(level instanceof ServerLevel server && !newState.is(this))ScabDrip.forgetPlacer(server,pos);
        super.onRemove(state,level,pos,newState,movedByPiston);
    }

    /** No vanilla fluid drips; a drip's particles are sent when it falls. */
    @Override
    public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random) {
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
