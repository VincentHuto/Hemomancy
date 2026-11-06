package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.MonolithDialogueContext;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.OfferedDialogues;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SanguineMonolithDialogueTrees;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DialogueTopicOpenedPacket(ResourceLocation topicId, int entityId) implements CustomPacketPayload {
	public static final Type<DialogueTopicOpenedPacket> TYPE =
			new Type<>(Hemomancy.rloc("dialogue_topic_opened_packet"));
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
			if (!ServerboundGuards.activePlayer(player) || !ServerboundRateLimiter.tryAcquire(player, "dialogue_topic", 2)) return;
			if (packet.entityId == SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID) {
				if (MonolithDialogueContext.speakingTo(player) == null) return;
			} else if (packet.entityId != 0 && ServerboundGuards.nearEntity(player, packet.entityId, 8.0D) == null) return;
			// Only a topic the server actually offered is recorded (RT-21): the read set is saved and copied on death.
			if (!OfferedDialogues.offersTopic(player, packet.entityId, packet.topicId)) return;
			HemoCapabilityAccess.dialogueKnowledge(player).markRead(packet.topicId);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
