package com.vincenthuto.hemomancy.common.block.shared;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.shared.ChamberGateBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The far end of a Chamber of Will's corridor. The manager places and moves it; it has no item and cannot be broken.
 * It draws nothing itself: its block entity holds the portal and the corridor's arches. It has no collision, because
 * the room's own bounds stop a body at its plane.
 */
public class ChamberGateBlock extends BaseEntityBlock {
	public static final MapCodec<ChamberGateBlock> CODEC = simpleCodec(ChamberGateBlock::new);
	private static final VoxelShape OUTLINE = box(6, 0, 7, 10, 16, 9);

	public ChamberGateBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return OUTLINE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ChamberGateBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return HemoTickers.create(type, BlockEntityInit.chamber_gate.get(), level.isClientSide
				? (lvl, pos, st, gate) -> gate.clientTick()
				: (lvl, pos, st, gate) -> gate.serverTick());
	}
}
