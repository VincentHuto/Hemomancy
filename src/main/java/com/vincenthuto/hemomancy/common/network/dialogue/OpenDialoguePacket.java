package com.vincenthuto.hemomancy.common.network.dialogue;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

/**
 * Server → Client packet that carries a complete {@link DialogueTree} and
 * triggers the dialogue screen on the receiving client.
 */
public class OpenDialoguePacket {

	private final DialogueTree tree;

	public OpenDialoguePacket(DialogueTree tree) {
		this.tree = tree;
	}

	public static void encode(OpenDialoguePacket msg, FriendlyByteBuf buf) {
		msg.tree.toNetwork(buf);
	}

	public static OpenDialoguePacket decode(FriendlyByteBuf buf) {
		return new OpenDialoguePacket(DialogueTree.fromNetwork(buf));
	}

	public static void handle(OpenDialoguePacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Must be handled on the client render thread
			if (Minecraft.getInstance().player != null) {
				com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen.open(msg.tree);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
