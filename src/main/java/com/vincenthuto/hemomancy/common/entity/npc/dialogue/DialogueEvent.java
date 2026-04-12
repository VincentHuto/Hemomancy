package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the <b>Forge event bus</b> when a player selects a dialogue option
 * that carries an {@code eventId}. Listeners can react to choices such as
 * accepting an invitation or rejecting help.
 */
public class DialogueEvent extends Event {

	private final ServerPlayer player;
	private final String eventId;
	private final int entityId;

	public DialogueEvent(ServerPlayer player, String eventId, int entityId) {
		this.player = player;
		this.eventId = eventId;
		this.entityId = entityId;
	}

	/** The player who picked the dialogue option. */
	public ServerPlayer getPlayer() {
		return player;
	}

	/** Arbitrary identifier set in the {@link DialogueOption} (e.g. {@code "accept_church"}). */
	public String getEventId() {
		return eventId;
	}

	/** Numeric entity id of the entity the player was conversing with. */
	public int getEntityId() {
		return entityId;
	}
}
