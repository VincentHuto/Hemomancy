package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeTransferRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public interface IBloodReservoir extends ProjectedBloodSink {
	@Nullable
	default IBloodVolume getBloodCapability() {
		return this instanceof BlockEntity blockEntity
				? blockEntity.getData(HemoAttachmentTypes.BLOCK_BLOOD_VOLUME)
				: null;
	}

	default void sendUpdates() {
		if (this instanceof BlockEntity blockEntity) BlockEntitySync.markAndSync(blockEntity);
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

	/**
	 * Projection fills the reservoir's own attachment, as the old generic projection did, and charges the player.
	 * F-12 (pending approval): unlike {@link #receiveBlood}, it does not ask {@link #canReceiveBlood()}, so a
	 * rite-locked station still takes projected blood.
	 */
	@Override
	default double takeProjectedBlood(Player player, BlockPos hitPos, double rate, boolean livingStaff) {
		if (!(this instanceof BlockEntity blockEntity)) return 0.0D;
		IBloodVolume volume = blockEntity.getData(HemoAttachmentTypes.BLOCK_BLOOD_VOLUME);
		double accepted = BloodVolumeTransferRules.transferableAmount(volume,
				HemoCapabilityAccess.getBloodVolume(player), rate);
		if (accepted <= 0.0D) return 0.0D;
		BloodAccounting.pay(player, accepted, PROJECTION);
		volume.fill(accepted);
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
