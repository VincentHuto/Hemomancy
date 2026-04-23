package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class VascularSystemClientPacket implements CustomPacketPayload {

	public static final Type<VascularSystemClientPacket> TYPE = new Type<>(Hemomancy.rloc("vascular_system_client_packet"));
	public static final StreamCodec<FriendlyByteBuf, VascularSystemClientPacket> STREAM_CODEC = StreamCodec.of(VascularSystemClientPacket::encode, VascularSystemClientPacket::decode);

	public static VascularSystemClientPacket decode(final FriendlyByteBuf packetBuffer) {
		return new VascularSystemClientPacket();
	}

	public static void encode(final FriendlyByteBuf packetBuffer, final VascularSystemClientPacket msg) {

	}

	public static void handle(final VascularSystemClientPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player instanceof ServerPlayer sender) {
				IVascularSystem BloodFlow = HemoCapabilityAccess.getVascularSystem(sender)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.sendToPlayer(sender, new VascularSystemServerPacket(BloodFlow.getVascularSystem()));
			}
		});
	}

	public VascularSystemClientPacket() {

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
