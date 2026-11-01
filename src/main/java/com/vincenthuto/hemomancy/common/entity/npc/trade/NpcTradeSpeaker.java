package com.vincenthuto.hemomancy.common.entity.npc.trade;

import net.minecraft.server.level.ServerPlayer;

/**
 * An entity that holds a Trade box: its speaker id is the one its offers are filed under. A progression NPC speaks as
 * its dialogue id; the Circus entities say so themselves.
 */
public interface NpcTradeSpeaker {
	String tradeSpeakerId();

	/** Whether this entity will deal with {@code player} at all right now (the Ringmaster keeps silent until the pact). */
	default boolean tradesWith(ServerPlayer player) {
		return true;
	}
}
