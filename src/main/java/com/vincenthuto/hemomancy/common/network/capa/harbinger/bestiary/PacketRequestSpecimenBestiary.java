package com.vincenthuto.hemomancy.common.network.capa.harbinger.bestiary;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketRequestSpecimenBestiary implements CustomPacketPayload {
	public static final Type<PacketRequestSpecimenBestiary> TYPE =
			new Type<>(Hemomancy.rloc("request_specimen_bestiary"));
	public static final StreamCodec<FriendlyByteBuf, PacketRequestSpecimenBestiary> STREAM_CODEC =
			StreamCodec.of(PacketRequestSpecimenBestiary::encode, PacketRequestSpecimenBestiary::decode);

	public PacketRequestSpecimenBestiary() {
	}

	public static void encode(FriendlyByteBuf buf, PacketRequestSpecimenBestiary msg) {
	}

	public static PacketRequestSpecimenBestiary decode(FriendlyByteBuf buf) {
		return new PacketRequestSpecimenBestiary();
	}

	public static void handle(PacketRequestSpecimenBestiary msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player packetPlayer = ctx.player();
			if (!(packetPlayer instanceof ServerPlayer player)) {
				return;
			}
			HemoCapabilityAccess.getSpecimenBestiary(player)
					.ifPresent(progress -> SpecimenBestiaryEvents.sync(player, progress));
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
