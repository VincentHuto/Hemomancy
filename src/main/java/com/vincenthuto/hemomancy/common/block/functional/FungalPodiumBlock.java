package com.vincenthuto.hemomancy.common.block.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.stream.Stream;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.functional.FungalPodiumBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FungalPodiumBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.box(3, 0, 3, 13, 2, 13), Block.box(3, 12, 3, 13, 14, 13),
			Block.box(6, 13, 6, 10, 15, 10), Block.box(6, 13, 6, 10, 15, 10), Block.box(5, 15, 5, 11, 16, 11),
			Block.box(3, 9, 3, 13, 12, 13), Block.box(2, 11, 2, 14, 13, 14), Block.box(4, 1, 4, 12, 3, 12),
			Block.box(4, 8, 4, 12, 10, 12), Block.box(5, 2, 5, 11, 9, 11))
			.reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	/** Blood cost to open a rift through the podium. */
	private static final double TRAVEL_BLOOD_COST = 500.0;
	/** Minimum initiatory degree required (2 = Votary). */
	private static final int MIN_DEGREE = 2;
	/** Player cooldown ticks after using the podium (5 seconds). */
	private static final int TRAVEL_COOLDOWN = 100;

	/** NBT key — stored overworld return X. */
	public static final String RETURN_X = "hemomancy:fungal_return_x";
	/** NBT key — stored overworld return Y. */
	public static final String RETURN_Y = "hemomancy:fungal_return_y";
	/** NBT key — stored overworld return Z. */
	public static final String RETURN_Z = "hemomancy:fungal_return_z";
	/** NBT key — stored overworld return yRot. */
	public static final String RETURN_YROT = "hemomancy:fungal_return_yrot";
	/** NBT key — stored overworld return xRot. */
	public static final String RETURN_XROT = "hemomancy:fungal_return_xrot";

	public static final ResourceKey<Level> FUNGAL_GARDENS = ResourceKey.create(Registries.DIMENSION,
			Hemomancy.rloc("fungal_gardens"));

	public FungalPodiumBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
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

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		boolean inFungalGardens = worldIn.dimension().equals(FUNGAL_GARDENS);

		if (inFungalGardens) {
			// ── Exit: Fungal Gardens → Overworld ──
			if (!serverPlayer.getPersistentData().contains(RETURN_X)) {
				serverPlayer.displayClientMessage(
						Component.literal("You feel no thread back to the surface world.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						true);
				return InteractionResult.SUCCESS;
			}
			performReturnTravel(serverPlayer);
		} else {
			// ── Entry: Overworld → Fungal Gardens ──
			int degree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
			if (degree < MIN_DEGREE) {
				serverPlayer.displayClientMessage(
						Component.literal("The mycelium rejects you — you are not yet sworn.")
								.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
						true);
				return InteractionResult.SUCCESS;
			}

			boolean bloodDrained = drainTravelBlood(serverPlayer);
			if (!bloodDrained) {
				serverPlayer.displayClientMessage(
						Component.literal("Your veins run too thin to feed the rift.")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
						true);
				return InteractionResult.SUCCESS;
			}

			storeReturnPosition(serverPlayer);
			performFungalGardensTravel(serverPlayer);
		}

		serverPlayer.getCooldowns().addCooldown(Item.byBlock(this), TRAVEL_COOLDOWN);
		return InteractionResult.SUCCESS;
	}

	private void storeReturnPosition(ServerPlayer player) {
		var data = player.getPersistentData();
		data.putDouble(RETURN_X, player.getX());
		data.putDouble(RETURN_Y, player.getY());
		data.putDouble(RETURN_Z, player.getZ());
		data.putFloat(RETURN_YROT, player.getYRot());
		data.putFloat(RETURN_XROT, player.getXRot());
	}

	private boolean drainTravelBlood(ServerPlayer player) {
		var opt = HemoCapabilityAccess.getBloodVolume(player);
		if (!opt.isPresent()) return false;
		var volume = opt.orElseThrow(IllegalStateException::new);
		if (!volume.isActive() || volume.getBloodVolume() < TRAVEL_BLOOD_COST) return false;
		volume.drain(TRAVEL_BLOOD_COST);
		BloodVolumeEvents.syncVolume(player, volume);
		return true;
	}

	private void performFungalGardensTravel(ServerPlayer player) {
		ServerLevel destination = player.server.getLevel(FUNGAL_GARDENS);
		if (destination == null) return;

		int x = (int) player.getX();
		int z = (int) player.getZ();
		ChunkPos chunkPos = new ChunkPos(new BlockPos(x, 0, z));
		destination.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, player.getId());

		BlockPos targetPos = findSafePos(destination, x, z);
		player.teleportTo(destination, targetPos.getX() + 0.5, targetPos.getY(),
				targetPos.getZ() + 0.5, player.getYRot(), player.getXRot());

		player.displayClientMessage(
				Component.literal("The flesh-lattice parts. You descend into the dreaming root.")
						.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),
				false);
	}

	private void performReturnTravel(ServerPlayer player) {
		ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
		if (overworld == null) return;

		var data = player.getPersistentData();
		double rx = data.getDouble(RETURN_X);
		double ry = data.getDouble(RETURN_Y);
		double rz = data.getDouble(RETURN_Z);
		float yRot = data.getFloat(RETURN_YROT);
		float xRot = data.getFloat(RETURN_XROT);

		// Validate return Y is still safe (world may have changed)
		BlockPos returnPos = new BlockPos((int) rx, (int) ry, (int) rz);
		ChunkPos chunkPos = new ChunkPos(returnPos);
		overworld.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, player.getId());

		player.teleportTo(overworld, rx + 0.5, ry, rz + 0.5, yRot, xRot);

		// Clear the stored return position — it has been consumed
		data.remove(RETURN_X);
		data.remove(RETURN_Y);
		data.remove(RETURN_Z);
		data.remove(RETURN_YROT);
		data.remove(RETURN_XROT);

		player.displayClientMessage(
				Component.literal("The membrane closes behind you. You surface from the blood-dream.")
						.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),
				false);
	}

	private BlockPos findSafePos(ServerLevel destination, int x, int z) {
		destination.getChunk(x >> 4, z >> 4);

		int y = destination.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		int minY = destination.getMinBuildHeight();
		int maxY = destination.getMaxBuildHeight();

		if (y > minY && y < maxY) {
			return new BlockPos(x, y, z);
		}

		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, minY, z);
		for (int scanY = minY; scanY < maxY - 1; scanY++) {
			mutable.setY(scanY);
			boolean solidBelow = !destination.getBlockState(mutable).isAir();
			mutable.setY(scanY + 1);
			boolean airAtFeet = destination.getBlockState(mutable).isAir();
			mutable.setY(scanY + 2);
			boolean airAtHead = destination.getBlockState(mutable).isAir();

			if (solidBelow && airAtFeet && airAtHead) {
				return new BlockPos(x, scanY + 1, z);
			}
		}

		return new BlockPos(x, 64, z);
	}
}
