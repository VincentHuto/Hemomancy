package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public interface IBloodReservoir extends ProjectedBloodSink {
	@Nullable
	default IBloodVolume getBloodCapability() {
		return this instanceof BlockEntity blockEntity ? ReservoirBlood.settled(blockEntity) : null;
	}

	/**
	 * A living vessel (the Gourdvine Tap; later vats and living veins) keeps its blood fresh: it never clots. Every
	 * other reservoir is an inert container and clots by {@link CoagulationRules}.
	 */
	default boolean isLivingVessel() {
		return false;
	}

	/**
	 * A store keeps blood for others (the Venous Basin, the Gourdvine Tap, vats); every other tank is a machine. A
	 * machine under its working reserve pulls blood down a living vein; a store pulls only while something draws from it
	 * (NQ-02-3).
	 */
	default boolean isBloodStore() {
		return isLivingVessel();
	}

	/** False for a block that answers projection but keeps no tank (the Somatic Loom, the Covenant Throne). */
	default boolean holdsBlood() {
		return true;
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
	 * Projection fills the reservoir's own attachment and charges the player. While the tank holds clot, part of the
	 * pour wakes it ({@link BlockBloodVolume#revive}, CQ-6, LP-3); the player pays only what the tank took.
	 * A station that refuses blood ({@link #canReceiveBlood()}, a rite lock among others) takes none and costs
	 * nothing (F-12).
	 */
	@Override
	default double takeProjectedBlood(Player player, BlockPos hitPos, double rate, boolean livingStaff) {
		if (!canReceiveBlood() || !(this instanceof BlockEntity blockEntity)) return 0.0D;
		BlockBloodVolume volume = ReservoirBlood.settled(blockEntity);
		double offered = Math.min(rate, Math.max(0.0D, HemoCapabilityAccess.getBloodVolume(player).getBloodVolume()));
		double accepted = volume.pourOf(offered, CoagulationRules.reviveShare(), CoagulationRules.reviveYield()).taken();
		if (accepted <= 0.0D) return 0.0D;
		BloodAccounting.pay(player, accepted, PROJECTION);
		volume.revive(accepted, CoagulationRules.reviveShare(), CoagulationRules.reviveYield());
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
