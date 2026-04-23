package com.vincenthuto.hemomancy.common.block.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.block.IMultiBlock;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SanguineMonolithDialogueTrees;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.tile.functional.SanguineMonolithBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Sanguine Monolith — a craftable guidance block available to players who have
 * reached at least the 5th initiatory degree (Illuminatus). Inspired by the SEELE
 * Monoliths from Neon Genesis Evangelion, it is a tall, dark, rectangular slab
 * that can be "conversed" with to receive hints about what to do next based on
 * the player's current advancement and progression level.
 *
 * After tier 4 the Harbinger Vicar ceases to guide the player directly;
 * instead, the player crafts and places one of these monoliths to found their
 * own outpost and receive cryptic counsel from it.
 */
public class SanguineMonolithBlock extends Block implements EntityBlock, IMultiBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE = Block.box(0, 0, 4, 16, 16, 12);
	private static final int SHATTER_INTERACTION_THRESHOLD = 2;
	private static final double SHATTER_PACKET_RADIUS = 64.0;

	/** Filler offsets: 1×2×1 — one filler block above the base. */
	private static final BlockPos[] FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 1, 0)
	};

	/** Minimum initiatory degree required to commune with the monolith. */
	private static final int MIN_DEGREE = 5;

	public SanguineMonolithBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return FILLER_OFFSETS;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
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
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = (Level) context.getLevel();
		if (pos.getY() + 1 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos)) {
			return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
		}
		return null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state,
			@Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide) {
			placeFillers(level, pos, state);
			if (level.getBlockEntity(pos) instanceof SanguineMonolithBlockEntity be) {
				be.startRise(level.getGameTime());
			}
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (!level.isClientSide) {
				removeFillers(level, pos);
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SanguineMonolithBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return (lvl, pos, st, be) -> {
			if (be instanceof SanguineMonolithBlockEntity monolith) {
				if (lvl.isClientSide) {
					monolith.clientTick();
				} else {
					monolith.tick();
				}
			}
		};
	}

	private InteractionResult handleInteraction(BlockState state, Level worldIn, BlockPos pos, Player player) {
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}

		HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree -> {
			int degreeNumber = degree.getDegreeNumber();
			if (degreeNumber >= 7 && worldIn.getBlockEntity(pos) instanceof SanguineMonolithBlockEntity monolith) {
				int interactions = monolith.incrementArchonInteractions();
				if (interactions >= SHATTER_INTERACTION_THRESHOLD) {
					explodeIntoBlackShards(worldIn, pos);
					popResource(worldIn, pos.above(), new ItemStack(ItemInit.qliphoth_seed.get()));
					worldIn.removeBlock(pos, false);

					// Fire the post-shatter Fungal Whisper so the Entity comments on what was inside
					PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(FungalWhisperDialogueTrees.postMonolithShatter()));
					return;
				}
				worldIn.sendBlockUpdated(pos, state, state, 3);
			}

			DialogueTree tree = degreeNumber < MIN_DEGREE
					? SanguineMonolithDialogueTrees.unworthy()
					: SanguineMonolithDialogueTrees.forDegree(degreeNumber);

			PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
		});

		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(state, worldIn, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		handleInteraction(state, worldIn, pos, player);
		return ItemInteractionResult.SUCCESS;
	}

	private static void explodeIntoBlackShards(Level level, BlockPos pos) {
		level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f, 0.8f);
		level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0f, 0.6f);
		if (!level.isClientSide) {
			PacketHandler.sendMonolithShatterBurst(
					new net.minecraft.world.phys.Vec3(pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5),
					SHATTER_PACKET_RADIUS,
					(ServerLevel) level);
		}
	}
}
