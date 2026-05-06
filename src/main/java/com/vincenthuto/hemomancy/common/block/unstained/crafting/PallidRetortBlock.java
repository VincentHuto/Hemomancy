package com.vincenthuto.hemomancy.common.block.unstained.crafting;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.crafting.PallidRetortBlockEntity;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PallidRetortBlock extends BaseEntityBlock implements EntityBlock, IMultiBlock {
	public static final MapCodec<PallidRetortBlock> CODEC = simpleCodec(PallidRetortBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	/** Filler offset: 1×2×1 — one filler block directly above the base. */
	private static final BlockPos[] FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 1, 0)
	};

	public PallidRetortBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(
				this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return FILLER_OFFSETS;
	}

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
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		// Full 1×1×1 collision box — model rendering is handled by entity renderer
		return Shapes.block();
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(LIT)) {
			double x = pos.getX() + 0.5;
			double y = pos.getY() + 1.0;
			double z = pos.getZ() + 0.5;

			// Bubbling / distilling sound
			if (random.nextDouble() < 0.15) {
				level.playLocalSound(x, y, z, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS,
						0.6F, 0.8F + random.nextFloat() * 0.4F, false);
			}

			// Blood drip particles rising from the top like steam
			for (int i = 0; i < 2; i++) {
				double ox = (random.nextDouble() - 0.5) * 0.4;
				double oz = (random.nextDouble() - 0.5) * 0.4;
				level.addParticle(ParticleTypes.DRIPPING_LAVA, x + ox, y + 0.1, z + oz,
						0.0, 0.02, 0.0);
			}

			// Pale particles
			if (random.nextDouble() < 0.3) {
				double ox = (random.nextDouble() - 0.5) * 0.3;
				double oz = (random.nextDouble() - 0.5) * 0.3;
				level.addParticle(ParticleTypes.END_ROD, x + ox, y + 0.15, z + oz, 0.0, 0.015, 0.0);
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
	}

	public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
		return state.getValue(LIT) ? 8 : 0;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = (Level) context.getLevel();
		if (pos.getY() + 1 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos)) {
			return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
		}
		return null; // Prevents placement if there's not enough room
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return level.isClientSide ? null
				: createTickerHelper(type, BlockEntityInit.pallid_retort.get(), PallidRetortBlockEntity::serverTick);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PallidRetortBlockEntity(pos, state);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, level, pos, block, fromPos, isMoving);
		// Re-check heat when a neighbor changes (e.g. fire placed/removed below)
		if (!level.isClientSide) {
			boolean heated = PallidRetortBlockEntity.isHeatSource(level, pos);
			if (state.getValue(LIT) != heated) {
				level.setBlock(pos, state.setValue(LIT, heated), 3);
			}
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof PallidRetortBlockEntity alembic) {
				if (level instanceof ServerLevel serverLevel) {
					Containers.dropContents(level, pos, alembic);
					alembic.getRecipesToAwardAndPopExperience(serverLevel, Vec3.atCenterOf(pos));
				}
				level.updateNeighbourForOutputSignal(pos, this);
			}
			if (!level.isClientSide) {
				removeFillers(level, pos);
			}
			super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	protected void openContainer(Level level, BlockPos pos, Player player) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof PallidRetortBlockEntity te) {
			if (!level.isClientSide) {
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(level, pos);
				((ServerPlayer) player).openMenu(te, pos);
			}
		}
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
		super.setPlacedBy(level, pos, state, entity, stack);
		if (!level.isClientSide) {
			placeFillers(level, pos, state);
		}
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		this.openContainer(level, pos, player);
		return InteractionResult.CONSUME;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		this.openContainer(level, pos, player);
		return ItemInteractionResult.SUCCESS;
	}
}
