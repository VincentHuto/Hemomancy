package com.vincenthuto.hemomancy.common.network.capa.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import com.vincenthuto.hemomancy.common.unstained.stillarts.StillArt;

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
				known.setSelectedArt(art);
				KnownStillArtEvents.sync(player, known);
				player.displayClientMessage(Component.literal("Still Art selected: " + art.getProperName()), true);
			});
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
