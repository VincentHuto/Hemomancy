package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import com.vincenthuto.hemomancy.client.particle.factory.AbsorbedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.block.shared.FillerBlock;
import com.vincenthuto.hemomancy.common.block.shared.HorizontalFacingRotationHelper;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialCrucibleBlockEntity;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.EmberParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class MycelialCrucibleBlock extends Block implements EntityBlock, IMultiBlock, SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    /** 3x2x1 footprint in south-facing local space; the main block is the lower center. */
    private static final BlockPos[] FILLER_OFFSETS = new BlockPos[] {
            new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0),
            new BlockPos(-1, 1, 0), new BlockPos(0, 1, 0), new BlockPos(1, 1, 0)
    };
    // Particle anchors for model elements "chain_l" and "chain_r", expressed in block-model pixels.
    private static final ModelPoint CHAIN_L_SPOUT = new ModelPoint(-1.0D, 20.0D, 8.0D);
    private static final ModelPoint CHAIN_L_BASIN = new ModelPoint(6.5D, 9.75D, 8.0D);
    private static final ModelPoint CHAIN_R_SPOUT = new ModelPoint(17.0D, 20.0D, 8.0D);
    private static final ModelPoint CHAIN_R_BASIN = new ModelPoint(9.5D, 9.75D, 8.0D);

    public MycelialCrucibleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(WATERLOGGED, false));
    }

    @Override
    public BlockPos[] getFillerOffsets() { return FILLER_OFFSETS; }

    private static BlockPos[] rotatedFillerOffsets(Direction facing) {
        return HorizontalFacingRotationHelper.rotateSouthOffsets(FILLER_OFFSETS, facing);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) != 0) {
            return;
        }

        Direction facing = state.getValue(FACING);
        spawnSpoutTrail(level, pos, random, facing, CHAIN_L_SPOUT, CHAIN_L_BASIN,
                EmberParticleFactory.createData(ParticleColor.GREEN, 1.25F, 0.16F, 34));
        spawnSpoutTrail(level, pos, random, facing, CHAIN_R_SPOUT, CHAIN_R_BASIN,
                AbsorbedBloodCellParticleFactory.createData(ParticleColor.BLOOD));
    }

    private static void spawnSpoutTrail(Level level, BlockPos pos, RandomSource random, Direction facing,
            ModelPoint source, ModelPoint target, ParticleOptions particle) {
        ModelPoint rotatedSource = rotateModelPointForFacing(source, facing);
        ModelPoint rotatedTarget = rotateModelPointForFacing(target, facing);
        int count = 4 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            double t = (i + random.nextDouble() * 0.65D) / count;
            double curve = Math.sin(t * Math.PI) * 0.12D;
            double x = lerp(rotatedSource.x(), rotatedTarget.x(), t) / 16.0D;
            double y = (lerp(rotatedSource.y(), rotatedTarget.y(), t) / 16.0D) - curve;
            double z = lerp(rotatedSource.z(), rotatedTarget.z(), t) / 16.0D;
            double jitter = 0.0125D;
            level.addParticle(particle,
                    pos.getX() + x + random.triangle(0.0D, jitter),
                    pos.getY() + y + random.triangle(0.0D, jitter),
                    pos.getZ() + z + random.triangle(0.0D, jitter),
                    random.triangle(0.0D, 0.006D), -0.018D - random.nextDouble() * 0.012D,
                    random.triangle(0.0D, 0.006D));
        }
    }

    private static ModelPoint rotateModelPointForFacing(ModelPoint point, Direction facing) {
        return switch (facing) {
            case SOUTH -> new ModelPoint(16.0D - point.x(), point.y(), 16.0D - point.z());
            case EAST -> new ModelPoint(16.0D - point.z(), point.y(), point.x());
            case WEST -> new ModelPoint(point.z(), point.y(), 16.0D - point.x());
            default -> point;
        };
    }

    private static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    private record ModelPoint(double x, double y, double z) {
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos   = context.getClickedPos();
        Level    level = (Level) context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (pos.getY() + 1 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos, facing)) {
            return defaultBlockState().setValue(FACING, facing).setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
        }
        return null;
    }

    @Override
    public boolean canPlaceMultiBlock(Level level, BlockPos mainPos) {
        Direction facing = level.getBlockState(mainPos).hasProperty(FACING)
                ? level.getBlockState(mainPos).getValue(FACING)
                : Direction.SOUTH;
        return canPlaceMultiBlock(level, mainPos, facing);
    }

    private boolean canPlaceMultiBlock(Level level, BlockPos mainPos, Direction facing) {
        for (BlockPos offset : rotatedFillerOffsets(facing)) {
            if (!level.getBlockState(mainPos.offset(offset)).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MycelialCrucibleBlockEntity(pos, state);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param) {
        super.triggerEvent(state, world, pos, id, param);
        BlockEntity be = world.getBlockEntity(pos);
        return be != null && be.triggerEvent(id, param);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> candidate, BlockEntityType<E> desired, BlockEntityTicker<? super E> ticker) {
        return desired == candidate ? (BlockEntityTicker<A>) ticker : null;
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide) {
            return createTickerHelper(type, BlockEntityInit.mycelial_crucible.get(),
                    MycelialCrucibleBlockEntity::clientTick);
        } else {
            return createTickerHelper(type, BlockEntityInit.mycelial_crucible.get(),
                    MycelialCrucibleBlockEntity::serverTick);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) placeFillers(level, pos, state);
    }

    @Override
    public void placeFillers(Level level, BlockPos mainPos, BlockState mainState) {
        Direction facing = mainState.hasProperty(FACING) ? mainState.getValue(FACING) : Direction.SOUTH;
        for (BlockPos offset : rotatedFillerOffsets(facing)) {
            BlockPos fillerPos = mainPos.offset(offset);
            level.setBlockAndUpdate(fillerPos, BlockInit.filler_block.get().defaultBlockState()
                    .setValue(FillerBlock.WATERLOGGED, level.getFluidState(fillerPos).is(FluidTags.WATER)));
            if (level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler) {
                filler.setMainBlockPos(mainPos);
            }
        }
    }

    @Override
    public void removeFillers(Level level, BlockPos mainPos) {
        BlockState mainState = level.getBlockState(mainPos);
        Direction facing = mainState.hasProperty(FACING) ? mainState.getValue(FACING) : Direction.SOUTH;
        removeFillers(level, mainPos, facing);
    }

    private void removeFillers(Level level, BlockPos mainPos, Direction facing) {
        for (BlockPos offset : rotatedFillerOffsets(facing)) {
            removeFillerAt(level, mainPos.offset(offset));
        }
        removeLinkedFillerBlocks(level, mainPos);
    }

    private void removeLinkedFillerBlocks(Level level, BlockPos mainPos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos fillerPos = mainPos.offset(x, y, z);
                    if (isLinkedFiller(level, fillerPos, mainPos)) {
                        removeFillerAt(level, fillerPos);
                    }
                }
            }
        }
    }

    private static boolean isLinkedFiller(Level level, BlockPos fillerPos, BlockPos mainPos) {
        return level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler
                && mainPos.equals(filler.getMainBlockPos());
    }

    private static void removeFillerAt(Level level, BlockPos fillerPos) {
        BlockState fillerState = level.getBlockState(fillerPos);
        if (fillerState.is(BlockInit.filler_block.get())) {
            if (fillerState.getValue(FillerBlock.WATERLOGGED)) {
                level.setBlockAndUpdate(fillerPos, Blocks.WATER.defaultBlockState());
            } else {
                level.removeBlock(fillerPos, false);
            }
        }
    }

    private InteractionResult handleInteraction(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MycelialCrucibleBlockEntity te) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(level, pos);
            ((ServerPlayer) player).openMenu(te, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult result) {
        return handleInteraction(level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        handleInteraction(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MycelialCrucibleBlockEntity te) {
                Containers.dropContents(level, pos, te);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            if (!level.isClientSide) removeFillers(level, pos, state.getValue(FACING));
            super.onRemove(state, level, pos, newState, moving);
        }
    }
	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

}
