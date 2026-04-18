package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.IMultiBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Block entity for the Sanguine Monolith. Tracks ambient state and
 * provides a render bounding box that covers the full 1×3×1 structure.
 */
public class SanguineMonolithBlockEntity extends BlockEntity implements IMultiBlockEntity {

	/** Ticks since the monolith was placed; used for ambient glow animation. */
	private int tickCount = 0;

	public SanguineMonolithBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.sanguine_monolith.get(), pos, state);
	}

	@Override
	public AABB getRenderBoundingBox() {
		return IMultiBlockEntity.computeMultiBlockAABB(this);
	}

	public int getTickCount() {
		return tickCount;
	}

	public void tick() {
		tickCount++;
		if (tickCount % 100 == 0) {
			setChanged();
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		tag.putInt("TickCount", tickCount);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("TickCount", tickCount);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		tickCount = tag.getInt("TickCount");
	}
}
