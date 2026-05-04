package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.util.SpecimenJarData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpecimenJarBlockEntity extends BlockEntity {
	private CompoundTag specimen = new CompoundTag();
	private boolean suppressNextRelease = false;

	public SpecimenJarBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.specimen_jar.get(), pos, state);
	}

	public boolean hasSpecimen() {
		return !specimen.isEmpty();
	}

	public CompoundTag getSpecimenCopy() {
		return specimen.copy();
	}

	public void setSpecimen(CompoundTag specimen) {
		this.specimen = specimen == null ? new CompoundTag() : specimen.copy();
		setChangedAndSync();
	}

	public void clearSpecimen() {
		this.specimen = new CompoundTag();
		setChangedAndSync();
	}

	public void suppressNextRelease() {
		this.suppressNextRelease = true;
	}

	public boolean consumeSuppressNextRelease() {
		boolean suppressed = suppressNextRelease;
		suppressNextRelease = false;
		return suppressed;
	}

	private void setChangedAndSync() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		if (tag.contains(SpecimenJarData.TAG_SPECIMEN, Tag.TAG_COMPOUND)) {
			specimen = tag.getCompound(SpecimenJarData.TAG_SPECIMEN).copy();
		} else {
			specimen = new CompoundTag();
		}
		suppressNextRelease = false;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (!specimen.isEmpty()) {
			tag.put(SpecimenJarData.TAG_SPECIMEN, specimen.copy());
		}
	}
}
