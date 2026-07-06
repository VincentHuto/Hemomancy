package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.PacketSyncManipulationCostDiagnostics;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.PacketSyncManipulationSlotDiagnostics;
import net.minecraft.server.level.ServerPlayer;

public final class ManipulationDiagnosticsSync {
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
	}

	private static ManipulationCostSnapshot selectedCostSnapshot(ServerPlayer player) {
		return HemoCapabilityAccess.getKnownManipulations(player)
				.map(ManipulationDiagnosticsSync::selectedManipulation)
				.map(manipulation -> ManipulationCostLedger.collect(player, manipulation))
				.orElse(ManipulationCostSnapshot.EMPTY);
	}

	private static BloodManipulation selectedManipulation(IKnownManipulations known) {
		BloodManipulation selected = known.getSelectedManip();
		return selected == null ? BloodManipulation.BLANK : selected;
	}
}
