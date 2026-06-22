package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public interface IBloodReservoir {
	@Nullable
	default IBloodVolume getBloodCapability() {
		return this instanceof BlockEntity blockEntity
				? blockEntity.getData(HemoAttachmentTypes.BLOCK_BLOOD_VOLUME)
				: null;
	}

	default void sendUpdates() {
		if (this instanceof BlockEntity blockEntity && blockEntity.getLevel() != null) {
			blockEntity.setChanged();
			blockEntity.getLevel().sendBlockUpdated(
					blockEntity.getBlockPos(),
					blockEntity.getBlockState(),
					blockEntity.getBlockState(),
					Block.UPDATE_ALL);
		}
	}

	default boolean canReceiveBlood() {
		return true;
	}

	default boolean canProvideBlood() {
		return true;
	}

	default double getBloodVolume() {
		IBloodVolume volume = getBloodCapability();
		return volume != null ? volume.getBloodVolume() : 0.0D;
	}

	default double getMaxBloodVolume() {
		IBloodVolume volume = getBloodCapability();
		return volume != null ? volume.getMaxBloodVolume() : 0.0D;
	}

	default double receiveBlood(double amount) {
		IBloodVolume volume = getBloodCapability();
		if (!canReceiveBlood() || volume == null || amount <= 0.0D || volume.isFull()) {
			return 0.0D;
		}
		double accepted = Math.min(amount, Math.max(0.0D, volume.getMaxBloodVolume() - volume.getBloodVolume()));
		if (accepted <= 0.0D || !volume.fill(accepted)) {
			return 0.0D;
		}
		sendUpdates();
		return accepted;
	}

	default double drainBlood(double amount) {
		IBloodVolume volume = getBloodCapability();
		if (!canProvideBlood() || volume == null || amount <= 0.0D || volume.isEmpty()) {
			return 0.0D;
		}
		double drained = Math.min(amount, volume.getBloodVolume());
		if (drained <= 0.0D || !volume.drain(drained)) {
			return 0.0D;
		}
		sendUpdates();
		return drained;
	}
}
