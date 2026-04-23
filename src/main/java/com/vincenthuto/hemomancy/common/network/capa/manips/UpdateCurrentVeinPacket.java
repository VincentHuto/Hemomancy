package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class UpdateCurrentVeinPacket implements CustomPacketPayload {

	public static final Type<UpdateCurrentVeinPacket> TYPE = new Type<>(Hemomancy.rloc("update_current_vein_packet"));
	public static final StreamCodec<FriendlyByteBuf, UpdateCurrentVeinPacket> STREAM_CODEC = StreamCodec.of(UpdateCurrentVeinPacket::encode, UpdateCurrentVeinPacket::decode);
	public static void handle(final UpdateCurrentVeinPacket msg, final IPayloadContext ctx) {
		Handler.handle(msg, ctx);
	}

	public static class Handler {
		public static void handle(final UpdateCurrentVeinPacket msg, final IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				Player player = ctx.player();
				if (player == null)
					return;
				if (!player.level().isClientSide) {
					IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
							.orElseThrow(NullPointerException::new);
					List<VeinLocation> manips = known.getVeinList();
					if (manips.get(msg.selected) != null) {
						known.setSelectedVein(manips.get(msg.selected));
						player.displayClientMessage(
								Component.literal("Selected:" + known.getVeinList().get(msg.selected).getName()), true);
					}
				}
			});
		}
	}

	public static UpdateCurrentVeinPacket decode(FriendlyByteBuf buf) {
		return new UpdateCurrentVeinPacket(buf.readInt());
	}

	public static void encode(FriendlyByteBuf buf, UpdateCurrentVeinPacket msg) {
		buf.writeInt(msg.selected);
	}

	private int selected;

	public UpdateCurrentVeinPacket(int selectedIn) {
		this.selected = selectedIn;

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
