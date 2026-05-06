package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: Player sends a message to all online members of their shared bloodline.
 * The message is broadcast as a dark red whisper with a flavour prefix.
 */
public class PacketBloodlineMessage implements CustomPacketPayload {

	public static final Type<PacketBloodlineMessage> TYPE = new Type<>(Hemomancy.rloc("packet_bloodline_message"));
	public static final StreamCodec<FriendlyByteBuf, PacketBloodlineMessage> STREAM_CODEC = StreamCodec.of(PacketBloodlineMessage::encode, PacketBloodlineMessage::decode);

	private static final int MAX_LENGTH = 256;

	private final String message;

	public PacketBloodlineMessage(String message) {
		this.message = message;
	}

	public static void encode(FriendlyByteBuf buf, PacketBloodlineMessage msg) {
		buf.writeUtf(msg.message, MAX_LENGTH);
	}

	public static PacketBloodlineMessage decode(FriendlyByteBuf buf) {
		return new PacketBloodlineMessage(buf.readUtf(MAX_LENGTH));
	}

	public static void handle(final PacketBloodlineMessage msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (!(player instanceof ServerPlayer sender)) return;

			String trimmed = msg.message.trim();
			if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH) return;

			HemoCapabilityAccess.getBloodVolume(sender).ifPresent(volume -> {
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
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
