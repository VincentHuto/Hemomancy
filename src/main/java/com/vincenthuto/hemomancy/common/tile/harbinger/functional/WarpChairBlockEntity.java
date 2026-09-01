package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public final class WarpChairBlockEntity extends BlockEntity {
	private boolean paired;
	@Nullable private UUID owner;

	public WarpChairBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.warp_chair.get(), pos, state);
	}

	public boolean isPaired() {
		return paired;
	}

	public Optional<UUID> owner() {
		return Optional.ofNullable(owner);
	}

	public void setPaired(UUID owner) {
		this.paired = true;
		this.owner = owner;
		setChanged();
		if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.putBoolean("Paired", paired);
		if (owner != null) tag.putUUID("Owner", owner);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		paired = tag.getBoolean("Paired");
		owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
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
