package com.vincenthuto.hemomancy.common.capability.player.harbinger.rite;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncIchorianKnowledge;
import net.minecraft.server.level.ServerPlayer;

public final class IchorianKnowledgeEvents {
	private IchorianKnowledgeEvents() {
	}

	public static void sync(ServerPlayer player) {
		PacketHandler.sendToPlayer(player,
						new PacketSyncIchorianKnowledge(HemoCapabilityAccess.getIchorianKnowledge(player)));
	}
}
