package com.vincenthuto.hemomancy.common.capability;

import java.util.Optional;

import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

import net.minecraft.world.entity.player.Player;

public final class HemoCapabilityAccess {

	private HemoCapabilityAccess() {
	}

	public static Optional<IBloodVolume> getBloodVolume(Player player) {
		return player.getCapability(BloodVolumeProvider.VOLUME_CAPA);
	}

	public static IBloodVolume requireBloodVolume(Player player) {
		return getBloodVolume(player).orElseThrow(IllegalStateException::new);
	}

	public static Optional<IBloodTendency> getBloodTendency(Player player) {
		return player.getCapability(BloodTendencyProvider.TENDENCY_CAPA);
	}

	public static IBloodTendency requireBloodTendency(Player player) {
		return getBloodTendency(player).orElseThrow(IllegalStateException::new);
	}
}
