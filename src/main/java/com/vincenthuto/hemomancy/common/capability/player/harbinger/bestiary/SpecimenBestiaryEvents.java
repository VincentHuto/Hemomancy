package com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.bestiary.PacketSyncSpecimenBestiary;
import net.minecraft.server.level.ServerPlayer;

public final class SpecimenBestiaryEvents {
	private SpecimenBestiaryEvents() {
	}

	public static void sync(ServerPlayer player, SpecimenBestiaryProgress progress) {
		PacketHandler.sendToPlayer(player, new PacketSyncSpecimenBestiary(progress));
	}
}
