package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client → Server: Leader/progenitor removes a player member from their bloodline.
 */
public class PacketKickBloodlinePlayer {

	private final UUID targetUUID;

	public PacketKickBloodlinePlayer(UUID targetUUID) {
		this.targetUUID = targetUUID;
	}

	public static void encode(PacketKickBloodlinePlayer msg, FriendlyByteBuf buf) {
		buf.writeUUID(msg.targetUUID);
	}

	public static PacketKickBloodlinePlayer decode(FriendlyByteBuf buf) {
		return new PacketKickBloodlinePlayer(buf.readUUID());
	}

	public static void handle(PacketKickBloodlinePlayer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer leader = ctx.get().getSender();
			if (leader == null) return;

			leader.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
				Bloodline localLine = volume.getBloodLine();
				if (!localLine.isValid()) {
					leader.displayClientMessage(
							Component.translatable("hemomancy.bloodline.kick.no_bloodline")
									.withStyle(ChatFormatting.RED),
							false);
					return;
				}

				if (!leader.getUUID().equals(localLine.getLeaderUUID())) {
					leader.displayClientMessage(
							Component.translatable("hemomancy.bloodline.kick.not_leader")
									.withStyle(ChatFormatting.RED),
							false);
					return;
				}

				if (msg.targetUUID.equals(localLine.getLeaderUUID())) {
					leader.displayClientMessage(
							Component.translatable("hemomancy.bloodline.kick.cannot_kick_leader")
									.withStyle(ChatFormatting.RED),
							false);
					return;
				}

				ServerLevel overworld = leader.server.overworld();
				BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
				Bloodline globalLine = savedData.getBloodline(localLine.getBloodlineUUID());
				if (globalLine == null || !globalLine.hasMember(msg.targetUUID)) {
					leader.displayClientMessage(
							Component.translatable("hemomancy.bloodline.kick.not_member")
									.withStyle(ChatFormatting.GRAY),
							false);
					return;
				}

				savedData.removeMember(globalLine.getBloodlineUUID(), msg.targetUUID);
				Bloodline updatedLine = savedData.getBloodline(globalLine.getBloodlineUUID());
				if (updatedLine == null) {
					return;
				}

				for (ServerPlayer online : leader.server.getPlayerList().getPlayers()) {
					online.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(memberVolume -> {
						if (online.getUUID().equals(msg.targetUUID)) {
							memberVolume.setBloodLine(Bloodline.NOBLOODLINE);
							BloodVolumeEvents.syncVolume(online, memberVolume);
							online.displayClientMessage(
									Component.translatable("hemomancy.bloodline.kick.you_were_removed",
											leader.getName().getString())
											.withStyle(ChatFormatting.DARK_RED),
									false);
						} else if (updatedLine.hasMember(online.getUUID())) {
							memberVolume.setBloodLine(updatedLine);
							BloodVolumeEvents.syncVolume(online, memberVolume);
						}
					});
				}

				String removedName = leader.server.getProfileCache().get(msg.targetUUID)
						.map(GameProfile::getName)
						.orElse(msg.targetUUID.toString());
				leader.displayClientMessage(
						Component.translatable("hemomancy.bloodline.kick.success", removedName)
								.withStyle(ChatFormatting.DARK_RED),
						false);
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
