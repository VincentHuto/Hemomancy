package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.server.level.ServerPlayer;

/**
 * Banks the acts an {@link NpcTradeWant.Act} card spends. Only a living, non-spectator player counts as having watched
 * one through (the pavilion observation rule).
 */
public final class NpcTradeActs {
	private NpcTradeActs() {}

	/** {@code player} watched {@code act} through to the end; returns whether it was banked. */
	public static boolean witnessed(ServerPlayer player, String act) {
		if (!player.isAlive() || player.isSpectator() || !NpcTradeWant.Act.ACTS.contains(act)) return false;
		HemoCapabilityAccess.npcTrades(player).recordAct(act);
		return true;
	}
}
