package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.IMultiBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

/**
 * Block entity for the Qliphoth Bloom. Stores the owner UUID and chunk
 * radius for informational purposes (the actual effect processing is still
 * driven by {@link com.vincenthuto.hemomancy.common.rite.QliphothBloomSavedData}).
 * <p>
 * Implements {@link IMultiBlockEntity} so the render bounding box covers
 * the full 1×1×8 multi-block, preventing frustum culling.
 */
public class QliphothBloomBlockEntity extends BlockEntity implements IMultiBlockEntity {

	private UUID ownerUUID;
	private int chunkRadius = 3;

	public QliphothBloomBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.qliphoth_bloom.get(), pos, state);
	}

	public void setOwnerUUID(UUID ownerUUID) {
		this.ownerUUID = ownerUUID;
		setChanged();
	}

	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	public void setChunkRadius(int chunkRadius) {
		this.chunkRadius = chunkRadius;
		setChanged();
	}

	public int getChunkRadius() {
		return chunkRadius;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		if (ownerUUID != null) {
			tag.putUUID("Owner", ownerUUID);
		}
		tag.putInt("ChunkRadius", chunkRadius);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		if (tag.hasUUID("Owner")) {
			ownerUUID = tag.getUUID("Owner");
		}
		chunkRadius = tag.getInt("ChunkRadius");
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		saveAdditional(tag, provider);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
