package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class CardinalFocusBlockEntity extends BlockEntity {
	private BlockPos templeDisplay;
	private UUID mediumOwner;

	public CardinalFocusBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.cardinal_focus.get(), pos, state);
	}

	public void linkTempleDisplay(BlockPos display) {
		templeDisplay = display == null ? null : display.immutable();
		setChanged();
	}

	public BlockPos getTempleDisplay() {
		return templeDisplay;
	}

	public void arm(UUID player) {
		mediumOwner = player;
		setChanged();
	}

	public boolean isArmedBy(UUID player) {
		return player != null && player.equals(mediumOwner);
	}

	public boolean consumeMedium(UUID player) {
		if (!isArmedBy(player)) return false;
		mediumOwner = null;
		setChanged();
		return true;
	}

	public boolean hasMedium() {
		return mediumOwner != null;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (templeDisplay != null) tag.putLong("TempleDisplay", templeDisplay.asLong());
		if (mediumOwner != null) tag.putUUID("MediumOwner", mediumOwner);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		templeDisplay = tag.contains("TempleDisplay") ? BlockPos.of(tag.getLong("TempleDisplay")) : null;
		mediumOwner = tag.hasUUID("MediumOwner") ? tag.getUUID("MediumOwner") : null;
	}
}
