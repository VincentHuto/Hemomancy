package com.vincenthuto.hemomancy.common.capability.player.harbinger.summon;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.summon.KnownSummonsServerPacket;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinition;
import net.minecraft.server.level.ServerPlayer;

public class KnownSummonEvents {
	public static void sync(ServerPlayer player, IKnownSummons known) {
		if (player == null || player.connection == null) {
			return;
		}
		PacketHandler.sendToPlayer(player, new KnownSummonsServerPacket(known));
	}

	public static boolean grantSummon(ServerPlayer player, PuppeteerSummonDefinition definition) {
		if (UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(player))) return false;
		var known = HemoCapabilityAccess.getKnownSummons(player);
		boolean learned = known.learn(definition);
		if (learned) {
			sync(player, known);
		}
		return learned;
	}
}
