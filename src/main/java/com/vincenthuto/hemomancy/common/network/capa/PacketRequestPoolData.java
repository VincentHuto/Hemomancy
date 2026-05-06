package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: Request the server to send the latest bloodline pool data.
 */
public class PacketRequestPoolData implements CustomPacketPayload {

	public static final Type<PacketRequestPoolData> TYPE = new Type<>(Hemomancy.rloc("packet_request_pool_data"));
	public static final StreamCodec<FriendlyByteBuf, PacketRequestPoolData> STREAM_CODEC = StreamCodec.of(PacketRequestPoolData::encode, PacketRequestPoolData::decode);

	public PacketRequestPoolData() {
	}

	public static void encode(FriendlyByteBuf buf, PacketRequestPoolData msg) {
	}

	public static PacketRequestPoolData decode(FriendlyByteBuf buf) {
		return new PacketRequestPoolData();
	}

	public static void handle(final PacketRequestPoolData msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player packetPlayer = ctx.player();
			if (!(packetPlayer instanceof ServerPlayer player)) return;

			HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
				Bloodline bloodline = volume.getBloodLine();
				if (bloodline.isValid()) {
					ServerLevel overworld = player.server.overworld();
					BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
					Bloodline globalLine = savedData.getBloodline(bloodline.getBloodlineUUID());
					if (globalLine != null) {
						PacketHandler.sendToPlayer(player, new PacketSyncBloodlinePool(globalLine.getBloodVolume(),
										globalLine.getMaxBloodVolume(),
										globalLine.getPlayerUUIDS().size()));
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
