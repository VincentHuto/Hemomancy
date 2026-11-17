package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncVerbPresence;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Other players' verb presence on this client, by entity id: what their server last sent while they channelled
 * ({@link PacketSyncVerbPresence}). The local player is never answered from here; their own state is on hand.
 */
public final class OnlookerVerbState {
	private static final Map<Integer, PacketSyncVerbPresence> PRESENCE = new HashMap<>();

	private OnlookerVerbState() {
	}

	public static void set(PacketSyncVerbPresence packet) {
		PRESENCE.put(packet.entityId(), packet);
	}

	public static void clear() {
		PRESENCE.clear();
	}

	public static OptionalDouble reach(Player player, Verb verb) {
		PacketSyncVerbPresence presence = of(player);
		if (presence == null) return OptionalDouble.empty();
		return OptionalDouble.of(verb == Verb.ABSORB ? presence.absorbReach() : presence.projectReach());
	}

	public static Optional<String> selection(Player player) {
		PacketSyncVerbPresence presence = of(player);
		return presence == null ? Optional.empty() : Optional.of(presence.selected());
	}

	public static Optional<Verb> verb(Player player) {
		PacketSyncVerbPresence presence = of(player);
		return presence == null ? Optional.empty() : Optional.of(presence.activeVerb());
	}

	private static PacketSyncVerbPresence of(Player player) {
		if (player == null || player == Minecraft.getInstance().player) return null;
		return PRESENCE.get(player.getId());
	}
}
