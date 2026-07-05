package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationDiagnosticsSync;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.CellHandFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class UpdateCurrentManipPacket implements CustomPacketPayload {

	public static final Type<UpdateCurrentManipPacket> TYPE = new Type<>(Hemomancy.rloc("update_current_manip_packet"));
	public static final StreamCodec<FriendlyByteBuf, UpdateCurrentManipPacket> STREAM_CODEC = StreamCodec.of(UpdateCurrentManipPacket::encode, UpdateCurrentManipPacket::decode);
	public static void handle(final UpdateCurrentManipPacket msg, final IPayloadContext ctx) {
		Handler.handle(msg, ctx);
	}

	public static class Handler {
		public static void handle(final UpdateCurrentManipPacket msg, final IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				Player player = ctx.player();
				if (player == null)
					return;
				if (!player.level().isClientSide) {
					IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
							.orElseThrow(NullPointerException::new);
					List<BloodManipulation> manips = known.getManipList();
					if (msg.selected >= 0 && msg.selected < manips.size() && manips.get(msg.selected) != null) {
						BloodManipulation target = manips.get(msg.selected);
						// Only allow selecting equipped manipulations
						if (!known.isManipEquipped(target)) {
							player.displayClientMessage(
									Component.literal("That manipulation is not equipped!")
											.withStyle(net.minecraft.ChatFormatting.RED), true);
							return;
						}
						if (!LivingStaffWeaponFormHelper.applySelection(player, target)) {
							return;
						}
						if (!CellHandFormHelper.applySelection(player, target)) {
							return;
						}
						known.setSelectedManip(target);
						player.displayClientMessage(
								Component.literal("Selected:" + target.getProperName()),
								true);
						PacketHandler.sendToPlayer((ServerPlayer) player, new KnownManipulationServerPacket(known));
						ManipulationDiagnosticsSync.sync((ServerPlayer) player);
					}
				}
			});
		}
	}

	public static UpdateCurrentManipPacket decode(FriendlyByteBuf buf) {
		return new UpdateCurrentManipPacket(buf.readInt());
	}

	public static void encode(FriendlyByteBuf buf, UpdateCurrentManipPacket msg) {
		buf.writeInt(msg.selected);
	}

	private int selected;

	public UpdateCurrentManipPacket(int selectedIn) {
		this.selected = selectedIn;

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
