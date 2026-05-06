package com.vincenthuto.hemomancy.common.network.capa;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
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

import java.util.UUID;

/**
 * Client → Server: Leader/progenitor removes a player member from their bloodline.
 */
public class PacketKickBloodlinePlayer implements CustomPacketPayload {

	public static final Type<PacketKickBloodlinePlayer> TYPE = new Type<>(Hemomancy.rloc("packet_kick_bloodline_player"));
	public static final StreamCodec<FriendlyByteBuf, PacketKickBloodlinePlayer> STREAM_CODEC = StreamCodec.of(PacketKickBloodlinePlayer::encode, PacketKickBloodlinePlayer::decode);

	private final UUID targetUUID;

	public PacketKickBloodlinePlayer(UUID targetUUID) {
		this.targetUUID = targetUUID;
	}

	public static void encode(FriendlyByteBuf buf, PacketKickBloodlinePlayer msg) {
		buf.writeUUID(msg.targetUUID);
	}

	public static PacketKickBloodlinePlayer decode(FriendlyByteBuf buf) {
		return new PacketKickBloodlinePlayer(buf.readUUID());
	}

	public static void handle(final PacketKickBloodlinePlayer msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (!(player instanceof ServerPlayer leader)) return;

			HemoCapabilityAccess.getBloodVolume(leader).ifPresent(volume -> {
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
					if (online.getUUID().equals(msg.targetUUID)) {
						notifyKickedPlayer(online, leader);
					} else if (updatedLine.hasMember(online.getUUID())) {
						syncMemberToUpdatedLine(online, updatedLine);
					}
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
	}

	/** Clears the kicked player's bloodline, syncs them to client, and sends the removal notice. */
	private static void notifyKickedPlayer(ServerPlayer kicked, ServerPlayer leader) {
		HemoCapabilityAccess.getBloodVolume(kicked).ifPresent(vol -> {
			vol.setBloodLine(Bloodline.NOBLOODLINE);
			BloodVolumeEvents.syncVolume(kicked, vol);
			kicked.displayClientMessage(
					Component.translatable("hemomancy.bloodline.kick.you_were_removed",
							leader.getName().getString())
							.withStyle(ChatFormatting.DARK_RED),
					false);
		});
	}

	/** Updates an ongoing bloodline member's local capability to the freshly updated bloodline. */
	private static void syncMemberToUpdatedLine(ServerPlayer member, Bloodline updatedLine) {
		HemoCapabilityAccess.getBloodVolume(member).ifPresent(vol -> {
			vol.setBloodLine(updatedLine);
			BloodVolumeEvents.syncVolume(member, vol);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
