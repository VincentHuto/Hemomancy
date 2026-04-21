package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

/**
 * Client → Server: Player sends a message to all online members of their shared bloodline.
 * The message is broadcast as a dark red whisper with a flavour prefix.
 */
public class PacketBloodlineMessage {

	private static final int MAX_LENGTH = 256;

	private final String message;

	public PacketBloodlineMessage(String message) {
		this.message = message;
	}

	public static void encode(PacketBloodlineMessage msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.message, MAX_LENGTH);
	}

	public static PacketBloodlineMessage decode(FriendlyByteBuf buf) {
		return new PacketBloodlineMessage(buf.readUtf(MAX_LENGTH));
	}

	public static void handle(PacketBloodlineMessage msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			if (sender == null) return;

			String trimmed = msg.message.trim();
			if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH) return;

			sender.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
				Bloodline bloodline = volume.getBloodLine();
				if (!bloodline.isValid()) {
					sender.sendSystemMessage(
							Component.literal("You are not in a bloodline!").withStyle(ChatFormatting.RED));
					return;
				}

				ServerLevel overworld = sender.server.overworld();
				BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
				Bloodline globalLine = savedData.getBloodline(bloodline.getBloodlineUUID());
				if (globalLine == null) return;

				Component prefix = Component.literal(
						"You begin to hear faint whispers as a message seems to manifest in your vision")
						.withStyle(ChatFormatting.DARK_RED);
				Component body = Component.literal("[" + sender.getName().getString() + "]: " + trimmed)
						.withStyle(ChatFormatting.DARK_RED);

				for (ServerPlayer onlinePlayer : sender.server.getPlayerList().getPlayers()) {
					if (globalLine.hasMember(onlinePlayer.getUUID())) {
						onlinePlayer.sendSystemMessage(prefix);
						onlinePlayer.sendSystemMessage(body);
					}
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
