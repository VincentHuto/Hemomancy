package com.vincenthuto.hemomancy.common.network.capa.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UpdateSelectedStillArtPacket implements CustomPacketPayload {
	public static final Type<UpdateSelectedStillArtPacket> TYPE = new Type<>(Hemomancy.rloc("update_selected_still_art_packet"));
	public static final StreamCodec<FriendlyByteBuf, UpdateSelectedStillArtPacket> STREAM_CODEC =
			StreamCodec.of(UpdateSelectedStillArtPacket::encode, UpdateSelectedStillArtPacket::decode);

	private final String artName;

	public UpdateSelectedStillArtPacket(String artName) {
		this.artName = artName;
	}

	public static void encode(FriendlyByteBuf buf, UpdateSelectedStillArtPacket msg) {
		buf.writeUtf(msg.artName);
	}

	public static UpdateSelectedStillArtPacket decode(FriendlyByteBuf buf) {
		return new UpdateSelectedStillArtPacket(buf.readUtf());
	}

	public static void handle(final UpdateSelectedStillArtPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (!(ctx.player() instanceof ServerPlayer player)) {
				return;
			}
			HemoCapabilityAccess.getKnownStillArts(player).ifPresent(known -> {
				StillArt art = StillArt.byName(msg.artName);
				if (art == StillArt.BLANK) {
					player.displayClientMessage(Component.translatable("hemomancy.still_art.select.invalid")
							.withStyle(ChatFormatting.GRAY), true);
					return;
				}
				if (!known.isKnown(art)) {
					player.displayClientMessage(Component.translatable("hemomancy.still_art.select.unknown")
							.withStyle(ChatFormatting.GRAY), true);
					return;
				}
				if (!art.isUnlockedFor(player)) {
					player.displayClientMessage(Component.translatable("hemomancy.still_art.select.locked")
							.withStyle(ChatFormatting.GRAY), true);
					return;
				}
				known.setSelectedArt(art);
				KnownStillArtEvents.sync(player, known);
				player.displayClientMessage(Component.translatable("hemomancy.still_art.select.success", art.getProperName()), true);
			});
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
