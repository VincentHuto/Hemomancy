package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncTrackingAvatarPacket implements CustomPacketPayload {

	public static void encode(FriendlyByteBuf buf, SyncTrackingAvatarPacket msg) {
		msg.toBytes(buf);
	}

	public static SyncTrackingAvatarPacket decode(FriendlyByteBuf buf) {
		return new SyncTrackingAvatarPacket(buf);
	}

	public static final Type<SyncTrackingAvatarPacket> TYPE = new Type<>(Hemomancy.rloc("sync_tracking_avatar_packet"));
	public static final StreamCodec<FriendlyByteBuf, SyncTrackingAvatarPacket> STREAM_CODEC = StreamCodec.of(SyncTrackingAvatarPacket::encode, SyncTrackingAvatarPacket::decode);

	public int playerId;
	public String avatarForm;

	public SyncTrackingAvatarPacket(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.avatarForm = buf.readUtf();
	}

	public SyncTrackingAvatarPacket(int playerId, String avatarForm) {
		this.playerId = playerId;
		this.avatarForm = avatarForm != null ? avatarForm : "";
	}

	public SyncTrackingAvatarPacket(int playerId, boolean isActive) {
		this(playerId, isActive ? "summon_avatar" : "");
	}

	public static void handle(final SyncTrackingAvatarPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Entity p = Minecraft.getInstance().level.getEntity(msg.playerId);
			if (p instanceof Player) {
				HemoCapabilityAccess.getKnownManipulations(p).ifPresent(b -> {
					b.setActiveAvatarForm(msg.avatarForm);
				});
			}
		});
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeUtf(this.avatarForm);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
