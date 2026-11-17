package com.vincenthuto.hemomancy.common.block.harbinger.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A mark blood leaves on the faces of the blocks around an air cell, after the glow lichen and sculk vein pattern:
 * any face, any surface, no block entity. The ground network's three tiers (stains, gouges, living veins) and the Lux
 * trace share it. A mark has no shape of its own, so the verbs and the hands aim through it at the block it lies on
 * and find it again from the face they hit ({@code GroundNetwork.markAt}); a block placed into its cell replaces it.
 */
public abstract class GroundMarkBlock extends MultifaceBlock {
	private final MultifaceSpreader spreader = new MultifaceSpreader(this);

	protected GroundMarkBlock(Properties properties) {
		super(properties);
	}

	@Override
	public MultifaceSpreader getSpreader() {
		return spreader;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return true;
	}

	/** The faces this mark lies on. */
	public static boolean lies(BlockState state, Direction face) {
		return state.getBlock() instanceof GroundMarkBlock && hasFace(state, face);
	}

	/**
	 * {@code state} with {@code face} taken off, or what the cell holds without it when it was the last: the water over
	 * a wet living vein, air otherwise.
	 */
	public static BlockState without(BlockState state, Direction face) {
		BlockState less = state.setValue(getFaceProperty(face), false);
		if (hasAnyFace(less)) return less;
		return state.getFluidState().isEmpty() ? Blocks.AIR.defaultBlockState() : state.getFluidState().createLegacyBlock();
	}

	/** Whether {@code state} lies on any face. */
	public static boolean anyFace(BlockState state) {
		return hasAnyFace(state);
	}
}
