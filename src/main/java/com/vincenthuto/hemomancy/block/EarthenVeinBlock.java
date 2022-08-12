package com.vincenthuto.hemomancy.block;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.tile.EarthenVeinBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;

public class EarthenVeinBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty STENTED = BooleanProperty.create("stented");
	public static final BooleanProperty NAMED = BooleanProperty.create("named");
	private static final VoxelShape SHAPE_N = Block.box(2, 0, 2, 14, 10, 14);

	public EarthenVeinBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(STENTED, false)
				.setValue(NAMED, false).setValue(WATERLOGGED, Boolean.valueOf(false)));

	}

	@Override
	public BlockState updateShape(BlockState p_53323_, Direction p_53324_, BlockState p_53325_, LevelAccessor p_53326_,
			BlockPos p_53327_, BlockPos p_53328_) {
		if (p_53323_.getValue(WATERLOGGED)) {
			p_53326_.scheduleTick(p_53327_, Fluids.WATER, Fluids.WATER.getTickDelay(p_53326_));
		}

		return super.updateShape(p_53323_, p_53324_, p_53325_, p_53326_, p_53327_, p_53328_);
	}

	@Override
	public FluidState getFluidState(BlockState p_52362_) {
		return p_52362_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_52362_);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState p_52348_, BlockGetter p_52349_, BlockPos p_52350_) {
		return !p_52348_.getValue(WATERLOGGED);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return type == BlockEntityInit.earthen_vein.get() ? EarthenVeinBlockEntity::tick : null;
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {
		IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);
		if (worldIn.getBlockEntity(pos)instanceof EarthenVeinBlockEntity te) {
			ItemStack stack = player.getItemInHand(handIn);
			if (!state.getValue(STENTED)) {
				if (stack.getItem() == ItemInit.living_stent.get()) {
					// If you already claimed this position
					if (known.getVeinBlockList().contains(te.getLoc().getPosition())) {
						// does the current vein have the same name(Say it was destroyed and replaced
						// somehow)?
						if (known.getVeinNameList().contains(te.getLoc().getName())) {
							player.displayClientMessage(new TextComponent("You have already claimed this vein!"), true);
						} else {
							// Get the old vein from that location, remove it, and add the new one
							known.getVeinList().removeIf((v) -> v.getPosition().equals(te.getLoc().getPosition()));
							stack.shrink(1);
							known.getVeinList().add(te.getLoc());
							BlockState newState = state.setValue(STENTED, true);
							worldIn.setBlock(pos, newState, 10);
						}
						// If you don't already claim a version of this
					} else {
						stack.shrink(1);
						known.getVeinList().add(te.getLoc());
						BlockState newState = state.setValue(STENTED, true);
						worldIn.setBlock(pos, newState, 10);
					}

				}
			}
			// If Claimed rename
			if (stack.getItem() == Items.NAME_TAG) {
				if (known.getVeinBlockList().contains(te.getLoc().getPosition())) {
					if (state.getValue(STENTED)) {
						te.getLoc().setName(stack.getHoverName().getString());
						if (stack.hasCustomHoverName()) {
							te.setName(stack.getHoverName().getString());
							stack.shrink(1);
							BlockState newState = state.setValue(NAMED, true);
							worldIn.setBlock(pos, newState, 10);
						}
					} else {
						player.displayClientMessage(new TextComponent("Only Stented Veins can be Renamed!"), true);

					}
				} else {
					if (!worldIn.isClientSide) {
						player.displayClientMessage(new TextComponent("You do not control this vein!"), true);
					}
				}
			}
			if (!worldIn.isClientSide) {
				PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
						new KnownManipulationServerPacket(known));

			}
		}
		return InteractionResult.SUCCESS;

	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(state, world, pos, neighbor);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());

		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(STENTED, false).setValue(NAMED, false)
				.setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, STENTED, NAMED, WATERLOGGED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new EarthenVeinBlockEntity(p_153215_, p_153216_);
	}

}
