package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncVerbPresence;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * What another player's client needs to draw a caster's verb where the caster's own client does: both verbs' reach,
 * the active verb and the selected manipulation ({@link PacketSyncVerbPresence}). Sent to the caster's trackers when a
 * channel opens, then every {@link #REFRESH_TICKS} while it is held but only when something changed, and to a player
 * who starts tracking a caster mid-channel. Nothing is computed for an idle player or one whose blood sleeps, so no
 * player is given state they did not have.
 *
 * <p>What each caster last sent is a session map keyed by UUID, cleared through {@code TransientServerStateEvents}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class VerbPresence {
	/** How often a held channel checks whether its presence changed. */
	public static final int REFRESH_TICKS = 10;
	private static final Map<UUID, PacketSyncVerbPresence> LAST_SENT = new HashMap<>();

	private VerbPresence() {
	}

	@SubscribeEvent
	public static void onUseTick(LivingEntityUseItemEvent.Tick event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		int held = player.getTicksUsingItem();
		if (held <= 1) refresh(player, true);
		else if (held % REFRESH_TICKS == 0) refresh(player, false);
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer watcher) {
			presence(target).ifPresent(packet -> PacketHandler.sendToPlayer(watcher, packet));
		}
	}

	/**
	 * Sends {@code player}'s presence to their trackers when they channel a verb and it differs from what was last
	 * sent ({@code force} sends it anyway, as a channel opens).
	 */
	public static void refresh(ServerPlayer player, boolean force) {
		Optional<PacketSyncVerbPresence> packet = presence(player);
		if (packet.isEmpty()) return;
		if (!force && packet.get().equals(LAST_SENT.get(player.getUUID()))) return;
		LAST_SENT.put(player.getUUID(), packet.get());
		PacketHandler.sendTracking(player, packet.get());
	}

	/** The presence of a player channelling a verb with awake blood; empty otherwise. */
	public static Optional<PacketSyncVerbPresence> presence(Player player) {
		if (!player.isUsingItem() || !HemoCapabilityAccess.getBloodVolume(player).isActive()) return Optional.empty();
		if (VerbChannel.channelling(player).isEmpty()) return Optional.empty();
		String selected = SculptingDispatcher.selectedManipulation(player).map(BloodManipulation::getName).orElse("");
		return Optional.of(new PacketSyncVerbPresence(player.getId(), (float) VerbReach.of(player, Verb.PROJECT),
				(float) VerbReach.of(player, Verb.ABSORB), VerbChannel.activeVerb(player), selected));
	}

	public static void clearSessionState() {
		LAST_SENT.clear();
	}

	public static void logout(Player player) {
		LAST_SENT.remove(player.getUUID());
	}
}
