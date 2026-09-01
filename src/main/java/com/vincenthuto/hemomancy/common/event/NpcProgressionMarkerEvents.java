package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ProgressionDialogueNpc;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.PacketSyncNpcProgressionMarkers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class NpcProgressionMarkerEvents {
	private static final double RANGE = 48.0;
	private static final Map<UUID, Map<Integer, DialogueAttention>> LAST_SENT = new HashMap<>();

	private NpcProgressionMarkerEvents() {
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0) return;

		Map<Integer, DialogueAttention> markers = new LinkedHashMap<>();
		for (var entity : player.level().getEntities(player, player.getBoundingBox().inflate(RANGE),
				candidate -> candidate instanceof ProgressionDialogueNpc)) {
			if (!(entity instanceof LivingEntity living) || !living.isAlive()
					|| player.distanceToSqr(entity) > RANGE * RANGE) continue;
			DialogueAttention attention = ((ProgressionDialogueNpc) entity).progressionAttention(player);
			if (attention != DialogueAttention.NONE) markers.put(entity.getId(), attention);
		}

		Map<Integer, DialogueAttention> snapshot = Map.copyOf(markers);
		if (!snapshot.equals(LAST_SENT.put(player.getUUID(), snapshot))) {
			PacketHandler.sendToPlayer(player, new PacketSyncNpcProgressionMarkers(snapshot));
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		LAST_SENT.remove(event.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		reset(event);
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		reset(event);
	}

	private static void reset(PlayerEvent event) {
		LAST_SENT.remove(event.getEntity().getUUID());
		if (event.getEntity() instanceof ServerPlayer player) {
			PacketHandler.sendToPlayer(player, new PacketSyncNpcProgressionMarkers(Map.of()));
		}
	}
}
