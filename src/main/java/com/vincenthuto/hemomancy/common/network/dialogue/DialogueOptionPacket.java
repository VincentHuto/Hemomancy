package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client → Server packet sent when a player selects a dialogue option that
 * carries an event id. The server fires a {@link DialogueEvent} so that any
 * listener can react to the player's choice.
 */
public class DialogueOptionPacket implements CustomPacketPayload {

	public static final Type<DialogueOptionPacket> TYPE = new Type<>(Hemomancy.rloc("dialogue_option_packet"));
	public static final StreamCodec<FriendlyByteBuf, DialogueOptionPacket> STREAM_CODEC = StreamCodec.of(DialogueOptionPacket::encode, DialogueOptionPacket::decode);

	private final String eventId;
	private final int entityId;

	public DialogueOptionPacket(String eventId, int entityId) {
		this.eventId = eventId;
		this.entityId = entityId;
	}

	public static void encode(FriendlyByteBuf buf, DialogueOptionPacket msg) {
		buf.writeUtf(msg.eventId);
		buf.writeInt(msg.entityId);
	}

	public static DialogueOptionPacket decode(FriendlyByteBuf buf) {
		return new DialogueOptionPacket(buf.readUtf(), buf.readInt());
	}

	public static void handle(final DialogueOptionPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.player();
			if (sender != null && msg.eventId != null && !msg.eventId.isEmpty()) {
				if (msg.entityId == 0) {
					// Disembodied voice (e.g. fungal whispers) — no entity to validate
					NeoForge.EVENT_BUS.post(
							new DialogueEvent(sender, msg.eventId, msg.entityId));
				} else {
					// Validate entity exists and is within interaction range
					net.minecraft.world.entity.Entity entity = sender.level().getEntity(msg.entityId);
					if (entity != null && sender.distanceTo(entity) <= 8.0) {
						NeoForge.EVENT_BUS.post(
								new DialogueEvent(sender, msg.eventId, msg.entityId));
					}
				}
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
