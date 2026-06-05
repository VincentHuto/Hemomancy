package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
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
 * Client → Server: Player requests a one-time lump donation to the shared bloodline pool.
 */
public class PacketLumpDonate implements CustomPacketPayload {

	public static final Type<PacketLumpDonate> TYPE = new Type<>(Hemomancy.rloc("packet_lump_donate"));
	public static final StreamCodec<FriendlyByteBuf, PacketLumpDonate> STREAM_CODEC = StreamCodec.of(PacketLumpDonate::encode, PacketLumpDonate::decode);

	private final double amount;

	public PacketLumpDonate(double amount) {
		this.amount = amount;
	}

	public static void encode(FriendlyByteBuf buf, PacketLumpDonate msg) {
		buf.writeDouble(msg.amount);
	}

	public static PacketLumpDonate decode(FriendlyByteBuf buf) {
		return new PacketLumpDonate(buf.readDouble());
	}

	public static void handle(final PacketLumpDonate msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player packetPlayer = ctx.player();
			if (!(packetPlayer instanceof ServerPlayer player)) return;

			HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
				if (!volume.isActive()) return;

				Bloodline bloodline = volume.getBloodLine();
				if (!bloodline.isValid()) {
					player.displayClientMessage(
							Component.literal("You are not in a bloodline!").withStyle(ChatFormatting.RED), true);
					return;
				}

				double donateAmount = Math.min(msg.amount, volume.getBloodVolume());
				if (donateAmount <= 0) {
					player.displayClientMessage(
							Component.literal("Not enough blood to donate!").withStyle(ChatFormatting.RED), true);
					return;
				}

				ServerLevel overworld = player.server.overworld();
				BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
				Bloodline globalLine = savedData.getBloodline(bloodline.getBloodlineUUID());
				if (globalLine != null) {
					if (volume.drain(donateAmount)) {
						globalLine.contributeBlood((float) donateAmount);
						savedData.setDirty();
						BloodVolumeEvents.syncVolume(player, volume);

						// Sync pool data to the donor
						PacketHandler.sendToPlayer(player, new PacketSyncBloodlinePool(globalLine.getBloodVolume(),
										globalLine.getMaxBloodVolume(),
										globalLine.getPlayerUUIDS().size()));

						player.displayClientMessage(
								Component.literal("Donated " + String.format("%.0f", donateAmount) + "ml to the bloodline pool!")
										.withStyle(ChatFormatting.DARK_RED), true);
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
