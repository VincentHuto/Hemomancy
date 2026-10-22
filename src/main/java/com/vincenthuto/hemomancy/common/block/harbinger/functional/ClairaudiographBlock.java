package com.vincenthuto.hemomancy.common.block.harbinger.functional;
import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ClairaudiographBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.Map;
public class ClairaudiographBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED, ACTIVE = BooleanProperty.create("active");
    /** Footed base, cylinder carriage and crank, then the horn and feed housing at the back. */
    private static final Map<Direction, VoxelShape> SHAPES = ModelShapes.horizontal(ModelShapes.of(
            new double[] {1, 0, 1.5, 15, 3, 14.5}, new double[] {0.75, 3, 1.5, 16, 9, 15},
            new double[] {1.25, 9, 4.5, 15, 14, 15}, new double[] {1.75, 14, 8.75, 14, 16, 13}));
    public ClairaudiographBlock() {
        super(Properties.of().strength(3).sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.BLOCK));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED,false).setValue(ACTIVE,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { b.add(FACING,POWERED,ACTIVE); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) { return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite()); }
    @Override protected BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING,r.rotate(s.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState s, Mirror m) { return s.rotate(m.getRotation(s.getValue(FACING))); }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPES.get(s.getValue(FACING)); }
    @Override public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return new ClairaudiographBlockEntity(p,s); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        return HemoTickers.create(t, BlockEntityInit.clairaudiograph.get(), ClairaudiographBlockEntity::tick);
    }
    private void open(Level l, BlockPos p, Player player) { if(player instanceof ServerPlayer sp && !player.isSpectator() && l.getBlockEntity(p) instanceof ClairaudiographBlockEntity be) sp.openMenu(be,p); }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit) { open(l,p,player); return InteractionResult.sidedSuccess(l.isClientSide); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit) { open(l,p,player); return ItemInteractionResult.sidedSuccess(l.isClientSide); }
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving) {
        if(!s.is(next.getBlock()) && l.getBlockEntity(p) instanceof ClairaudiographBlockEntity be && !l.isClientSide) {
            be.cancelCarve(); be.stopPlayback();
            for(int i=0;i<2;i++) { Containers.dropItemStack(l,p.getX(),p.getY(),p.getZ(),be.inventory.getStackInSlot(i)); be.inventory.setStackInSlot(i,ItemStack.EMPTY); }
        }
        super.onRemove(s,l,p,next,moving);
    }
}
