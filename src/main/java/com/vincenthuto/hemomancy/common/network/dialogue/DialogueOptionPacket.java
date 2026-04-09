package com.vincenthuto.hemomancy.common.network.dialogue;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.dialogue.DialogueEvent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client → Server packet sent when a player selects a dialogue option that
 * carries an event id. The server fires a {@link DialogueEvent} so that any
 * listener can react to the player's choice.
 */
public class DialogueOptionPacket {

	private final String eventId;
	private final int entityId;

	public DialogueOptionPacket(String eventId, int entityId) {
		this.eventId = eventId;
		this.entityId = entityId;
	}

	public static void encode(DialogueOptionPacket msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.eventId);
		buf.writeInt(msg.entityId);
	}

	public static DialogueOptionPacket decode(FriendlyByteBuf buf) {
		return new DialogueOptionPacket(buf.readUtf(), buf.readInt());
	}

	public static void handle(DialogueOptionPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			if (sender != null && msg.eventId != null && !msg.eventId.isEmpty()) {
				MinecraftForge.EVENT_BUS.post(
						new DialogueEvent(sender, msg.eventId, msg.entityId));
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
