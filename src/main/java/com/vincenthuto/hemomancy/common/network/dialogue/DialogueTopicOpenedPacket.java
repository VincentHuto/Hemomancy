package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DialogueTopicOpenedPacket(ResourceLocation topicId, int entityId) implements CustomPacketPayload {
	public static final Type<DialogueTopicOpenedPacket> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath("hemomancy", "dialogue_topic_opened_packet"));
	public static final StreamCodec<FriendlyByteBuf, DialogueTopicOpenedPacket> STREAM_CODEC =
			StreamCodec.of(DialogueTopicOpenedPacket::encode, DialogueTopicOpenedPacket::decode);

	private static void encode(FriendlyByteBuf buf, DialogueTopicOpenedPacket packet) {
		buf.writeResourceLocation(packet.topicId);
		buf.writeInt(packet.entityId);
	}

	private static DialogueTopicOpenedPacket decode(FriendlyByteBuf buf) {
		return new DialogueTopicOpenedPacket(buf.readResourceLocation(), buf.readInt());
	}

	public static boolean isValidTopicId(ResourceLocation id) {
		return id != null && "hemomancy".equals(id.getNamespace())
				&& !id.getPath().isBlank() && id.getPath().length() <= 128;
	}

	public static void handle(DialogueTopicOpenedPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !isValidTopicId(packet.topicId)) return;
			if (packet.entityId != 0) {
				var entity = player.level().getEntity(packet.entityId);
				if (entity == null || player.distanceTo(entity) > 8.0F) return;
			}
			player.getData(HemoAttachmentTypes.DIALOGUE_KNOWLEDGE).markRead(packet.topicId);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
