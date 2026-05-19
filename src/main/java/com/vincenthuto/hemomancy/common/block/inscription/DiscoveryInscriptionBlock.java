package com.vincenthuto.hemomancy.common.block.inscription;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.discovery.OpenInscriptionPacket;
import com.vincenthuto.hemomancy.common.tile.DiscoveryInscriptionBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class DiscoveryInscriptionBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape FLOOR_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);
	private static final VoxelShape WALL_NORTH_SHAPE = Block.box(1.0, 1.0, 14.0, 15.0, 15.0, 16.0);
	private static final VoxelShape WALL_SOUTH_SHAPE = Block.box(1.0, 1.0, 0.0, 15.0, 15.0, 2.0);
	private static final VoxelShape WALL_EAST_SHAPE = Block.box(0.0, 1.0, 1.0, 2.0, 15.0, 15.0);
	private static final VoxelShape WALL_WEST_SHAPE = Block.box(14.0, 1.0, 1.0, 16.0, 15.0, 15.0);
	private final boolean wallMounted;

	public DiscoveryInscriptionBlock(BlockBehaviour.Properties properties) {
		this(properties, false);
	}

	public DiscoveryInscriptionBlock(BlockBehaviour.Properties properties, boolean wallMounted) {
		super(properties.sound(SoundType.STONE).noOcclusion());
		this.wallMounted = wallMounted;
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		boolean waterlogged = WaterloggedBlockSupport.waterloggedForPlacement(context);
		if (!wallMounted) {
			return this.defaultBlockState()
					.setValue(FACING, context.getHorizontalDirection().getOpposite())
					.setValue(WATERLOGGED, waterlogged);
		}
		for (Direction direction : context.getNearestLookingDirections()) {
			if (direction.getAxis().isHorizontal()) {
				BlockState state = this.defaultBlockState()
						.setValue(FACING, direction.getOpposite())
						.setValue(WATERLOGGED, waterlogged);
				if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
					return state;
				}
			}
		}
		return null;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return wallMounted ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		if (!wallMounted) {
			return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
		}
		Direction facing = state.getValue(FACING);
		BlockPos supportPos = pos.relative(facing.getOpposite());
		return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
			LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, currentPos);
		if (!state.canSurvive(level, currentPos)) {
			return WaterloggedBlockSupport.survivorOrWater(state);
		}
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (!wallMounted) {
			return FLOOR_SHAPE;
		}
		return switch (state.getValue(FACING)) {
			case NORTH -> WALL_NORTH_SHAPE;
			case SOUTH -> WALL_SOUTH_SHAPE;
			case EAST -> WALL_EAST_SHAPE;
			case WEST -> WALL_WEST_SHAPE;
			default -> WALL_SOUTH_SHAPE;
		};
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		return wallMounted ? Shapes.empty() : super.getCollisionShape(state, level, pos, context);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DiscoveryInscriptionBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		return interact(level, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		return interact(level, pos, player).consumesAction()
				? ItemInteractionResult.SUCCESS
				: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	private InteractionResult interact(Level level, BlockPos pos, Player player) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}
		if (!(level.getBlockEntity(pos) instanceof DiscoveryInscriptionBlockEntity inscription)) {
			return InteractionResult.PASS;
		}

		DiscoveryInscriptionRegistry.get(inscription.getInscriptionId()).ifPresentOrElse(definition ->
				readDefinition(serverPlayer, definition),
				() -> serverPlayer.displayClientMessage(
						Component.translatable("message.hemomancy.discovery_inscription.blank"), true));
		return InteractionResult.SUCCESS;
	}

	private static void readDefinition(ServerPlayer player, DiscoveryInscriptionDefinition definition) {
		int pathLevel = levelFor(player, definition.path());
		if (!definition.isReadable(pathLevel)) {
			PacketDistributor.sendToPlayer(player, new OpenInscriptionPacket(
					obscuredTitle(definition), definition.obscuredText(), false, null));
			return;
		}

		HemomancyDiscoverySource source = definition.kind() == DiscoveryInscriptionDefinition.Kind.BLOOD_ECHO
				? HemomancyDiscoverySource.BLOOD_ECHO
				: HemomancyDiscoverySource.RITE_FRAGMENT;
		LiberKnowledgeHelper.unlockEntries(player, definition.liberEntries(), source);
		PacketHandler.sendToPlayer(player, new OpenInscriptionPacket(
				definition.title(), revealedText(definition), true, definition.riteId()));
	}

	private static int levelFor(Player player, DiscoveryInscriptionDefinition.Path path) {
		return path == DiscoveryInscriptionDefinition.Path.UNSTAINED
				? HemoCapabilityAccess.getPlayerUnstainedLevel(player)
				: HemoCapabilityAccess.getPlayerDegreeNumber(player);
	}

	private static List<String> revealedText(DiscoveryInscriptionDefinition definition) {
		return definition.revealedText().isEmpty() ? definition.obscuredText() : definition.revealedText();
	}

	private static String obscuredTitle(DiscoveryInscriptionDefinition definition) {
		return definition.kind() == DiscoveryInscriptionDefinition.Kind.BLOOD_ECHO
				? "Obscured Blood Echo"
				: "Obscured Rite Fragment";
	}
}
