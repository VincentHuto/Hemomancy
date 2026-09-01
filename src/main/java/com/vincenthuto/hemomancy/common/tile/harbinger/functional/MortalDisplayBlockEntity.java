package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class MortalDisplayBlockEntity extends BlockEntity {
	private UUID linkedHermit;
	private UUID claimant;
	private boolean claimed;

	public MortalDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.mortal_display.get(), pos, state);
	}

	public UUID getLinkedHermit() {
		return linkedHermit;
	}

	public boolean isClaimed() {
		return claimed;
	}

	public boolean isClaimedBy(UUID player) {
		return claimed && player != null && player.equals(claimant);
	}

	public void linkHermit(UUID hermit) {
		linkedHermit = hermit;
		setChanged();
	}

	public void claim(UUID player) {
		claimed = true;
		claimant = player;
		setChanged();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (linkedHermit != null) tag.putUUID("LinkedHermit", linkedHermit);
		if (claimant != null) tag.putUUID("Claimant", claimant);
		tag.putBoolean("Claimed", claimed);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		linkedHermit = tag.hasUUID("LinkedHermit") ? tag.getUUID("LinkedHermit") : null;
		claimant = tag.hasUUID("Claimant") ? tag.getUUID("Claimant") : null;
		claimed = tag.getBoolean("Claimed");
	}

}
