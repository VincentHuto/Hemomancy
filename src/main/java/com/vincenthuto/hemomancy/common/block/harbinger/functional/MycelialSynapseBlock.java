package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.event.HeartNodeEnvelopeSync;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.structure.BloodStructureFormation;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MycelialSynapseBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import com.vincenthuto.hemomancy.common.world.HeartNodeEntry;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A Mycelial Synapse: a Blood Structure that adds a lobe to its raiser's heart-node envelope, the way a Hematic Stake
 * extends a Founding Fane. Raised from a hematic iron block with a Sporite Crystal on any face, it grows toward where
 * the crystal stood. It has one owner and no members. A synapse whose record is gone (its node broken or replanted)
 * withers in place: it adds nothing and drops its inputs when broken, but it never removes itself.
 */
public class MycelialSynapseBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = DirectionalBlock.FACING;
	public static final BooleanProperty WITHERED = BooleanProperty.create("withered");
	/** The stalk in pixels, base on the face it grew from and 14 long toward {@link #FACING}. */
	private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

	static {
		for (Direction facing : Direction.values()) {
			double[] min = { 5, 5, 5 };
			double[] max = { 11, 11, 11 };
			int axis = facing.getAxis().ordinal();
			boolean positive = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE;
			min[axis] = positive ? 0 : 2;
			max[axis] = positive ? 14 : 16;
			SHAPES.put(facing, Block.box(min[0], min[1], min[2], max[0], max[1], max[2]));
		}
	}

	public MycelialSynapseBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(WITHERED, false));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("block.hemomancy.mycelial_synapse.desc").withStyle(ChatFormatting.DARK_PURPLE,
				ChatFormatting.ITALIC));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WITHERED);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
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
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	/** A synapse is only ever raised from its formation; the item is the recipe's face, not a way to place one. */
	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (context.getPlayer() != null && !context.getLevel().isClientSide) {
			context.getPlayer().displayClientMessage(line("raise_only"), true);
		}
		return null;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MycelialSynapseBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide || state.getValue(WITHERED) ? null
				: HemoTickers.create(type, BlockEntityInit.mycelial_synapse.get(), MycelialSynapseBlockEntity::serverTick);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && !state.getValue(WITHERED) && level instanceof ServerLevel server
				&& level.getBlockEntity(pos) instanceof MycelialSynapseBlockEntity synapse && synapse.owner() != null
				&& HeartNodeSavedData.get(server).removeSynapse(synapse.owner(), pos)) {
			HeartNodeEnvelopeSync.syncAround(server, pos);
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	/** Withers each loaded synapse at {@code positions}; unloaded ones wither when their chunk next ticks. */
	public static void witherAll(ServerLevel level, List<BlockPos> positions) {
		for (BlockPos pos : positions) {
			if (level.isLoaded(pos)) wither(level, pos);
		}
	}

	public static void wither(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.is(BlockInit.mycelial_synapse.get()) && !state.getValue(WITHERED)) {
			level.setBlock(pos, state.setValue(WITHERED, true), Block.UPDATE_ALL);
		}
	}

	/**
	 * Whether {@code player} may raise a synapse from the iron at {@code ironPos}, checked before any blood is fed.
	 * Synapses still closing their craft ring count against the budget, so a feed started earlier cannot overrun it.
	 */
	public static Check findFormation(ServerLevel level, ServerPlayer player, BlockPos ironPos, BlockPos crystalPos) {
		Direction facing = Direction.fromDelta(crystalPos.getX() - ironPos.getX(),
				crystalPos.getY() - ironPos.getY(), crystalPos.getZ() - ironPos.getZ());
		if (facing == null || !ApotheosBodyEvents.isApotheos(player)) return Check.refused("not_apotheos");
		if (!FungalGardensPlaces.inDimension(level)) return Check.refused("not_in_gardens");
		String refusal = sanctumRefusal(HeartNodeSavedData.get(level), player.getUUID(), ironPos);
		if (refusal != null) return Check.refused(refusal);
		return new Check(new Formation(player.getUUID(), ironPos.immutable(), crystalPos.immutable(), facing), null);
	}

	/** The envelope's say, in refusal order: {@code no_heart}, {@code budget}, {@code foreign}, {@code unconnected}. */
	@Nullable
	public static String sanctumRefusal(HeartNodeSavedData data, UUID owner, BlockPos ironPos) {
		HeartNodeEntry entry = data.entry(owner);
		if (entry == null) return "no_heart";
		if (entry.synapses().size() + pendingFor(owner) >= HeartNodeRules.SYNAPSE_BUDGET) return "budget";
		if (data.wouldOverlap(owner, ironPos, HeartNodeRules.SYNAPSE_RADIUS)) return "foreign";
		if (!entry.footprint().canAddSynapse(ironPos)) return "unconnected";
		return null;
	}

	private static long pendingFor(UUID owner) {
		return PendingBloodCraftManager.pendingFormations().stream()
				.filter(formation -> formation instanceof Formation synapse && synapse.owner().equals(owner))
				.count();
	}

	private static Component line(String reason) {
		return Component.translatable("block.hemomancy.mycelial_synapse." + reason)
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
	}

	/** A formation to raise, or the reason it can't be. */
	public record Check(@Nullable Formation formation, @Nullable Component refusal) {
		public static Check refused(String reason) {
			return new Check(null, line(reason));
		}
	}

	public record Formation(UUID owner, BlockPos pos, BlockPos crystalPos, Direction facing)
			implements BloodStructureFormation {
		/** Checked again as the ring closes: the inputs still stand, and the envelope still takes the lobe. */
		@Override
		public boolean place(ServerLevel level) {
			HeartNodeSavedData data = HeartNodeSavedData.get(level);
			if (!level.getBlockState(pos).is(BlockInit.hematic_iron_block.get())
					|| !level.getBlockState(crystalPos).is(BlockInit.sporite_crystal.get())
					|| !data.addSynapse(owner, pos)) {
				return false;
			}
			BlockState synapse = BlockInit.mycelial_synapse.get().defaultBlockState().setValue(FACING, facing);
			if (!level.setBlock(pos, synapse, Block.UPDATE_ALL)
					|| !(level.getBlockEntity(pos) instanceof MycelialSynapseBlockEntity entity)) {
				data.removeSynapse(owner, pos);
				return false;
			}
			entity.raise(owner, data.nodeOf(owner));
			HeartNodeEnvelopeSync.syncAround(level, pos);
			ServerPlayer raiser = level.getServer().getPlayerList().getPlayer(owner);
			if (raiser != null) raiser.displayClientMessage(line("raised"), true);
			return true;
		}

		/** A late refusal returns what was set down; the blood is spent. */
		@Override
		public List<ItemStack> fallbackDrops(ItemStack result) {
			return List.of(new ItemStack(BlockInit.hematic_iron_block.get()),
					new ItemStack(BlockInit.sporite_crystal.get()));
		}
	}
}
