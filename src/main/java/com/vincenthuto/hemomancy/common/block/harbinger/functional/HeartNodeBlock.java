package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * An Apotheos's heart-node: the seed of their private sanctum in the Gardens. Planting and privacy are gated in
 * {@code HeartNodeEvents}; the block entity grows the ribs. Removing it forgets the node and the spawn it held.
 */
public class HeartNodeBlock extends Block implements EntityBlock {
	private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 11, 13);

	public HeartNodeBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
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
			HeartNodeSavedData.get(server).remove(node.owner(), pos);
			ServerPlayer owner = server.getServer().getPlayerList().getPlayer(node.owner());
			if (owner != null && level.dimension().equals(owner.getRespawnDimension())
					&& pos.above().equals(owner.getRespawnPosition())) {
				owner.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false);
			}
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}
}
