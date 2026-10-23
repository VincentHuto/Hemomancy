package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpecimenJarBlockEntity extends HemoBlockEntity {
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
		BlockEntitySync.markAndSync(this);
	}

	public void clearSpecimen() {
		this.specimen = new CompoundTag();
		BlockEntitySync.markAndSync(this);
	}

	public void suppressNextRelease() {
		this.suppressNextRelease = true;
	}

	public boolean consumeSuppressNextRelease() {
		boolean suppressed = suppressNextRelease;
		suppressNextRelease = false;
		return suppressed;
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
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
