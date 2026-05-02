package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineConduitBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Minimal block entity for the {@link SanguineConduitBlock}.
 * Holds no persistent data; its sole purpose is to be the anchor for the BER
 * ({@link com.vincenthuto.hemomancy.client.render.tile.functional.SanguineConduitBlockRenderer})
 * that draws the pulsing blood-oath rings around the placed conduit.
 */
public class SanguineConduitBlockEntity extends BlockEntity {

	public SanguineConduitBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.sanguine_conduit.get(), pos, state);
	}

	/**
	 * Expand the render AABB so the ring geometry (which extends several blocks
	 * outward) is never frustum-culled while the block itself is visible.
	 */
	public AABB getRenderBoundingBox() {
		BlockPos c = getBlockPos();
		return new AABB(c.getX() - 8, c.getY() - 1, c.getZ() - 8,
				c.getX() + 9, c.getY() + 2, c.getZ() + 9);
	}
}
