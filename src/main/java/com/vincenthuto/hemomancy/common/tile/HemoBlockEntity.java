package com.vincenthuto.hemomancy.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * L-7: a Hemomancy block entity that syncs to clients. Its update packet is the vanilla one carrying
 * {@link #getUpdateTag}; resend after a change with {@code BlockEntitySync.markAndSync}. Blood lives only in the
 * {@code block_blood_volume} attachment, which {@code saveAdditional} writes and {@code loadAdditional} reads.
 */
public abstract class HemoBlockEntity extends BlockEntity {
	protected HemoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
