package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The safety net under a rigging tower: knotted crimson thread strung on an iron frame, a thin sheet that breaks a
 * fall as a hay bale does.
 */
public class TroupeNetBlock extends Block {
	/** The share of fall damage a net lets through. */
	public static final float FALL_DAMAGE = 0.2F;
	private static final VoxelShape SHAPE = box(0, 0, 0, 16, 3, 16);

	public TroupeNetBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
		entity.causeFallDamage(fallDistance, FALL_DAMAGE, level.damageSources().fall());
	}
}
