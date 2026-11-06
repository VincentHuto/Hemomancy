package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.MonolithDialogueContext;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SanguineMonolithDialogueTrees;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Whatever holds a Trade box: its speaker id is the one its offers are filed under. A progression NPC speaks as its
 * dialogue id; the Circus entities say so themselves; the Sanguine Monolith is a block entity.
 */
public interface NpcTradeSpeaker {
	String tradeSpeakerId();

	/** Whether this speaker will deal with {@code player} at all right now (the Ringmaster keeps silent until the pact). */
	default boolean tradesWith(ServerPlayer player) {
		return true;
	}

	/**
	 * The speaker a trade payload names: for the block speaker's id, the stone the player's open conversation is on;
	 * otherwise a living entity within {@code reach}.
	 */
	@Nullable
	static NpcTradeSpeaker near(ServerPlayer player, int id, double reach) {
		return id == SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID ? MonolithDialogueContext.speakingTo(player)
				: ServerboundGuards.nearLivingEntity(player, id, reach, NpcTradeSpeaker.class);
	}
}
