package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server → Client packet that carries a complete {@link DialogueTree} and
 * triggers the dialogue screen on the receiving client.
 */
public class OpenDialoguePacket implements CustomPacketPayload {

	public static final Type<OpenDialoguePacket> TYPE = new Type<>(Hemomancy.rloc("open_dialogue_packet"));
	public static final StreamCodec<FriendlyByteBuf, OpenDialoguePacket> STREAM_CODEC = StreamCodec.of(OpenDialoguePacket::encode, OpenDialoguePacket::decode);

	private final DialogueTree tree;

	public OpenDialoguePacket(DialogueTree tree) {
		this.tree = tree;
	}

	public static void encode(FriendlyByteBuf buf, OpenDialoguePacket msg) {
		msg.tree.toNetwork(buf);
	}

	public static OpenDialoguePacket decode(FriendlyByteBuf buf) {
		return new OpenDialoguePacket(DialogueTree.fromNetwork(buf));
	}

	public static void handle(final OpenDialoguePacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() != null) {
				com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen.open(msg.tree);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
