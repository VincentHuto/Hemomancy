package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.functional.FungalPodiumBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

public class FungalPodiumBlock extends BaseEntityBlock {
	public static final MapCodec<FungalPodiumBlock> CODEC = simpleCodec(FungalPodiumBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.box(3, 0, 3, 13, 2, 13), Block.box(3, 12, 3, 13, 14, 13),
			Block.box(6, 13, 6, 10, 15, 10), Block.box(6, 13, 6, 10, 15, 10), Block.box(5, 15, 5, 11, 16, 11),
			Block.box(3, 9, 3, 13, 12, 13), Block.box(2, 11, 2, 14, 13, 14), Block.box(4, 1, 4, 12, 3, 12),
			Block.box(4, 8, 4, 12, 10, 12), Block.box(5, 2, 5, 11, 9, 11))
			.reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	public static final double TRAVEL_BLOOD_COST = FungalGardenTravelHelper.TRAVEL_BLOOD_COST;
	public static final int MIN_DEGREE = FungalGardenTravelHelper.MIN_DEGREE;
	public static final int TRAVEL_COOLDOWN = FungalGardenTravelHelper.TRAVEL_COOLDOWN;

	public static final String RETURN_X = FungalGardenTravelHelper.RETURN_X;
	public static final String RETURN_Y = FungalGardenTravelHelper.RETURN_Y;
	public static final String RETURN_Z = FungalGardenTravelHelper.RETURN_Z;
	public static final String RETURN_YROT = FungalGardenTravelHelper.RETURN_YROT;
	public static final String RETURN_XROT = FungalGardenTravelHelper.RETURN_XROT;

	public static final String ARCHON_CHOICE_KEY = FungalGardenTravelHelper.ARCHON_CHOICE_KEY;
	public static final String ARCHON_CHOICE_SILENCE = FungalGardenTravelHelper.ARCHON_CHOICE_SILENCE;
	public static final String ARCHON_CHOICE_APOTHEOS = FungalGardenTravelHelper.ARCHON_CHOICE_APOTHEOS;

	public static final ResourceKey<Level> FUNGAL_GARDENS = FungalGardenTravelHelper.FUNGAL_GARDENS;

	public FungalPodiumBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		super.attack(state, worldIn, pos, player);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_49232_) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState p_153183_,
			BlockEntityType<T> p_153184_) {
		return level.isClientSide
				? createTickerHelper(p_153184_, BlockEntityInit.fungal_podium.get(),
						FungalPodiumBlockEntity::animTick)
				: null;
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
		return new FungalPodiumBlockEntity(arg0, arg1);
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param) {
		super.triggerEvent(state, world, pos, id, param);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity != null && blockEntity.triggerEvent(id, param);
	}

	private InteractionResult handleUse(Level worldIn, Player player) {
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		return FungalGardenTravelHelper.handleTravelUse(serverPlayer, Item.byBlock(this));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleUse(worldIn, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		handleUse(worldIn, player);
		return ItemInteractionResult.SUCCESS;
	}

	public static void performReturnTravel(ServerPlayer player) {
		FungalGardenTravelHelper.performReturnTravel(player);
	}
}
