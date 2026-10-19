package com.vincenthuto.hemomancy.client.render.world.zone;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.common.rite.zone.RiteSpaceSyncPacket;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.List;
import java.util.Optional;

/** The rite spaces of the dimension the local player is in, as last sent by the server. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RiteSpaceClientData {
	private static volatile List<RiteSpaceSyncPacket.Space> spaces = List.of();

	private RiteSpaceClientData() {}

	public static void set(List<RiteSpaceSyncPacket.Space> synced) {
		spaces = List.copyOf(synced);
	}

	public static List<RiteSpaceSyncPacket.Space> spaces() {
		return spaces;
	}

	public static Optional<CardinalRitePurpose> purposeAt(BlockPos pos) {
		for (RiteSpaceSyncPacket.Space space : spaces) {
			if (space.covers(pos)) return Optional.of(space.purpose());
		}
		return Optional.empty();
	}

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		spaces = List.of();
	}
}
