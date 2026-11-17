package com.vincenthuto.hemomancy.common.block.harbinger.ground;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.ground.VeinNodeBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * A knot of living tissue set at the end of a gouge run (core-loop Track 6). Projection into it grows tissue along the
 * connected gouges, turning them into living vein; between two nodes the vein moves a little blood from the tank one
 * node touches to the tank the other touches, while the owner is near (CL-14). It holds no blood of its own.
 */
public class VeinNodeBlock extends BaseEntityBlock {
	public static final MapCodec<VeinNodeBlock> CODEC = simpleCodec(VeinNodeBlock::new);
	private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 9, 13);

	public VeinNodeBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<VeinNodeBlock> codec() {
		return CODEC;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VeinNodeBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : HemoTickers.create(type, BlockEntityInit.vein_node.get(), VeinNodeBlockEntity::serverTick);
	}

	/** The node remembers who set it down: only they may grow from it. */
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide && placer != null && level.getBlockEntity(pos) instanceof VeinNodeBlockEntity node) {
			node.setOwner(placer.getUUID(), level.getGameTime());
		}
	}
}
