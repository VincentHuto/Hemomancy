package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.heartnode.HeartNodeEnvelopePacket;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import com.vincenthuto.hemomancy.common.world.HeartNodeEntry;
import com.vincenthuto.hemomancy.common.world.HeartNodeRelation;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import com.vincenthuto.hemomancy.common.world.LobeEnvelope;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps each Gardens player's client told of the sanctums near them. A player is sent the list when they enter the
 * Gardens and again whenever it changes (a node planted or lost, a synapse raised, a setting, a stage, or the player
 * walking into range of another); leaving the Gardens sends an empty list. Their own node rides the same packet.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class HeartNodeEnvelopeSync {
	/** Beyond an envelope's widest reach, how far off a player still hears of it. */
	public static final int SYNC_MARGIN = 64;
	private static final int CHECK_TICKS = 10;
	private static final Map<UUID, List<HeartNodeEnvelopePacket.Envelope>> LAST_SENT = new HashMap<>();

	private HeartNodeEnvelopeSync() {}

	/** The sanctums {@code viewer} standing at {@code at} should know of, each with how the viewer stands to it. */
	public static List<HeartNodeEnvelopePacket.Envelope> envelopesFor(Collection<HeartNodeEntry> entries,
			UUID viewer, BlockPos at, long gameTime) {
		List<HeartNodeEnvelopePacket.Envelope> near = new ArrayList<>();
		for (HeartNodeEntry entry : entries) {
			double reach = entry.footprint().maxRadius() + SYNC_MARGIN;
			if (LobeEnvelope.distanceSqr(at, entry.heart()) > reach * reach) continue;
			near.add(new HeartNodeEnvelopePacket.Envelope(entry.owner(), entry.heart(), entry.synapses(),
					entry.settings().bits(), entry.settings().intensity(), HeartNodeRelation.of(entry.owner(), viewer),
					entry.plantedAt(), entry.stage(gameTime)));
		}
		return near;
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (event.getLevel() instanceof ServerLevel level && FungalGardensPlaces.inDimension(level)
				&& level.getGameTime() % CHECK_TICKS == 0) {
			for (ServerPlayer player : level.players()) sync(player);
		}
	}

	/** Resends to everyone near {@code pos} at once, for a change a player should see without waiting for the check. */
	public static void syncAround(ServerLevel level, BlockPos pos) {
		for (ServerPlayer player : level.players()) {
			if (player.blockPosition().distSqr(pos) <= 256.0D * 256.0D) sync(player);
		}
	}

	@SubscribeEvent
	public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) sync(player);
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) sync(player);
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			LAST_SENT.remove(player.getUUID());
			sync(player);
		}
	}

	private static void sync(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel level) || !FungalGardensPlaces.inDimension(level)) {
			List<HeartNodeEnvelopePacket.Envelope> last = LAST_SENT.remove(player.getUUID());
			if (last != null && !last.isEmpty()) PacketHandler.sendToPlayer(player, new HeartNodeEnvelopePacket(List.of()));
			return;
		}
		List<HeartNodeEnvelopePacket.Envelope> near = envelopesFor(HeartNodeSavedData.get(level).envelopes(),
				player.getUUID(), player.blockPosition(), level.getGameTime());
		if (near.equals(LAST_SENT.get(player.getUUID()))) return;
		LAST_SENT.put(player.getUUID(), near);
		PacketHandler.sendToPlayer(player, new HeartNodeEnvelopePacket(near));
	}

	public static void logout(ServerPlayer player) {
		LAST_SENT.remove(player.getUUID());
	}

	public static void clearSessionState() {
		LAST_SENT.clear();
	}
}
