package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.PacketSyncManipulationCostDiagnostics;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.PacketSyncManipulationSlotDiagnostics;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Scrying screen's slot and cost diagnostics for the selected manipulation. {@link #sync} sends both (blood changes,
 * selection changes); {@link #refreshTemperPreview} resends only the cost snapshot, and only when the selected
 * manipulation's Temper line would read differently (the pool emptied or filled, a catalyst was picked up or used), so
 * the preview does not go stale between blood syncs.
 *
 * <p>What each player's preview last showed is a session map keyed by UUID (not an attachment, so reading it never
 * gives a player state they did not have), cleared through {@code TransientServerStateEvents}.
 */
public final class ManipulationDiagnosticsSync {
	private static final String NO_CHARGE = "none";
	private static final Map<UUID, String> LAST_PREVIEW = new HashMap<>();

	private ManipulationDiagnosticsSync() {
	}

	public static void sync(ServerPlayer player) {
		if (player == null) {
			return;
		}
		PacketHandler.sendToPlayer(player,
				new PacketSyncManipulationSlotDiagnostics(ManipulationSlotLedger.collect(player)));
		PacketHandler.sendToPlayer(player,
				new PacketSyncManipulationCostDiagnostics(selectedCostSnapshot(player)));
		LAST_PREVIEW.put(player.getUUID(), temperPreview(player));
	}

	/**
	 * Resends the cost snapshot when the selected manipulation's Temper outcome (or the price it puts on the cast) has
	 * changed since it was last sent; otherwise sends nothing.
	 */
	public static void refreshTemperPreview(ServerPlayer player) {
		if (player == null) {
			return;
		}
		String preview = temperPreview(player);
		if (preview.equals(LAST_PREVIEW.get(player.getUUID()))) {
			return;
		}
		PacketHandler.sendToPlayer(player,
				new PacketSyncManipulationCostDiagnostics(selectedCostSnapshot(player)));
		LAST_PREVIEW.put(player.getUUID(), preview);
	}

	public static void clearSessionState() {
		LAST_PREVIEW.clear();
	}

	public static void logout(Player player) {
		LAST_PREVIEW.remove(player.getUUID());
	}

	/** What the cost snapshot's Temper line shows for the selected manipulation: its outcome and multiplier. */
	private static String temperPreview(ServerPlayer player) {
		BloodManipulation selected = selectedManipulation(HemoCapabilityAccess.getKnownManipulations(player));
		// Nothing selected shows no cost at all (the ledger is empty), and reads no Temper state.
		if (selected == BloodManipulation.BLANK) return NO_CHARGE;
		TemperCharge.Charge charge = TemperCharge.forCast(player, selected, 1.0F, false);
		return charge == null ? NO_CHARGE : charge.outcome().name() + "@" + charge.costMultiplier();
	}

	private static ManipulationCostSnapshot selectedCostSnapshot(ServerPlayer player) {
		return ManipulationCostLedger.collect(player, selectedManipulation(HemoCapabilityAccess.getKnownManipulations(player)));
	}

	private static BloodManipulation selectedManipulation(IKnownManipulations known) {
		BloodManipulation selected = known.getSelectedManip();
		return selected == null ? BloodManipulation.BLANK : selected;
	}
}
