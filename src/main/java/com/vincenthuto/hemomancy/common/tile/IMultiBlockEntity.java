package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/**
 * Interface for block entities whose block implements {@link IMultiBlock}.
 * Provides a static helper that computes a render bounding box covering
 * all filler offsets, preventing frustum culling from hiding the model
 * when only part of the multi-block structure is in view.
 * <p>
 * Usage: have your block entity implement this interface, then override
 * {@code getRenderBoundingBox()} with a single call:
 * <pre>
 * {@code @Override}
 * public AABB getRenderBoundingBox() {
 *     return IMultiBlockEntity.computeMultiBlockAABB(this);
 * }
 * </pre>
 */
public interface IMultiBlockEntity {

    /**
     * Computes an AABB that covers the main block plus all filler offsets.
     * Falls back to the default render bounding box if the block does not
     * implement {@link IMultiBlock}.
     */
    static AABB computeMultiBlockAABB(BlockEntity be) {
        if (be.getBlockState().getBlock() instanceof IMultiBlock multiBlock) {
            BlockPos pos = be.getBlockPos();
            int minX = 0, minY = 0, minZ = 0;
            int maxX = 1, maxY = 1, maxZ = 1;

            for (BlockPos offset : multiBlock.getFillerOffsets()) {
                minX = Math.min(minX, offset.getX());
                minY = Math.min(minY, offset.getY());
                minZ = Math.min(minZ, offset.getZ());
                maxX = Math.max(maxX, offset.getX() + 1);
                maxY = Math.max(maxY, offset.getY() + 1);
                maxZ = Math.max(maxZ, offset.getZ() + 1);
            }

            return new AABB(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            );
        }
        return AABB.INFINITE;
    }
}

