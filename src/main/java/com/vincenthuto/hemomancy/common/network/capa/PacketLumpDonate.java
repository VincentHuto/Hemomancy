package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

/**
 * Client → Server: Player requests a one-time lump donation to the shared bloodline pool.
 */
public class PacketLumpDonate {

	private final double amount;

	public PacketLumpDonate(double amount) {
		this.amount = amount;
	}

	public static void encode(PacketLumpDonate msg, FriendlyByteBuf buf) {
		buf.writeDouble(msg.amount);
	}

	public static PacketLumpDonate decode(FriendlyByteBuf buf) {
		return new PacketLumpDonate(buf.readDouble());
	}

	public static void handle(PacketLumpDonate msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player == null) return;

			player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
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
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> player),
								new PacketSyncBloodlinePool(globalLine.getBloodVolume(),
										globalLine.getMaxBloodVolume(),
										globalLine.getPlayerUUIDS().size()));

						player.displayClientMessage(
								Component.literal("Donated " + String.format("%.0f", donateAmount) + "ml to the bloodline pool!")
										.withStyle(ChatFormatting.DARK_RED), true);
					}
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
