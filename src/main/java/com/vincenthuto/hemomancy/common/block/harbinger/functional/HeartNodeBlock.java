package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import com.vincenthuto.hemomancy.common.world.HeartNodeMoment;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import com.vincenthuto.hemomancy.common.world.HeartNodeService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

/**
 * An Apotheos's heart-node: the heart of their personal Fane in the Gardens, the centre of an envelope their Mycelial
 * Synapses extend. Planting and privacy are gated in {@code HeartNodeEvents}; the block entity keeps the node's
 * maturity and shows it as {@link #STAGE}, which picks the model (tools/model_export/heart_node.py). Its owner opens it
 * to tend how the sanctum looks. Removing it forgets the node, its synapse records and the spawn it held, and shows
 * the players near it the node withering.
 */
public class HeartNodeBlock extends Block implements EntityBlock {
	public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, HeartNodeRules.MATURE_STAGE);
	/** The root knot (KNOT in heart_node.py): the only part that stops a body. */
	private static final VoxelShape KNOT = Block.box(4.5, 0, 4.5, 11.5, 3.4, 11.5);
	/** The knot and the heart above it, so the heart can be aimed at and opened through its cage. */
	private static final VoxelShape OUTLINE = Shapes.or(KNOT, Block.box(5, 3.4, 5, 11, 8, 11));

	public HeartNodeBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STAGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return OUTLINE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return KNOT;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("block.hemomancy.heart_node.desc").withStyle(ChatFormatting.DARK_PURPLE,
				ChatFormatting.ITALIC));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (player instanceof ServerPlayer viewer && level.getBlockEntity(pos) instanceof HeartNodeBlockEntity node) {
			HeartNodeService.open(viewer, node);
		}
		return InteractionResult.CONSUME;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HeartNodeBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : HemoTickers.create(type, BlockEntityInit.heart_node.get(),
				HeartNodeBlockEntity::serverTick);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && level instanceof ServerLevel server
				&& level.getBlockEntity(pos) instanceof HeartNodeBlockEntity node && node.owner() != null) {
			// The block goes now; nearby clients keep its last frame as a ghost while it withers.
			HeartNodeService.announce(server, pos, HeartNodeMoment.WITHERED, state.getValue(STAGE));
			HeartNodeSavedData.get(server).remove(node.owner(), pos)
					.ifPresent(entry -> MycelialSynapseBlock.witherAll(server, entry.synapses()));
			ServerPlayer owner = server.getServer().getPlayerList().getPlayer(node.owner());
			if (owner != null && level.dimension().equals(owner.getRespawnDimension())
					&& pos.above().equals(owner.getRespawnPosition())) {
				owner.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false);
			}
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}
}
