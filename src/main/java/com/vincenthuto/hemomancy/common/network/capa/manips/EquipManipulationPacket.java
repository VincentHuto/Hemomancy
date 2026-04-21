package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client → Server packet to equip or unequip a manipulation in the player's
 * limited manipulation slots.
 */
public class EquipManipulationPacket {

	private final String manipName;
	private final boolean equip; // true = equip, false = unequip

	public EquipManipulationPacket(String manipName, boolean equip) {
		this.manipName = manipName;
		this.equip = equip;
	}

	public static void encode(EquipManipulationPacket msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.manipName);
		buf.writeBoolean(msg.equip);
	}

	public static EquipManipulationPacket decode(FriendlyByteBuf buf) {
		return new EquipManipulationPacket(buf.readUtf(), buf.readBoolean());
	}

	public static void handle(EquipManipulationPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null) return;

			IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
					.orElse(null);
			if (known == null) return;

			if (msg.equip) {
				int maxSlots = ManipSlotHelper.getMaxSlots(player);
				if (known.equipManip(msg.manipName, maxSlots)) {
					player.displayClientMessage(
							Component.literal("Manipulation equipped: " + msg.manipName)
									.withStyle(ChatFormatting.GREEN), true);
				} else {
					player.displayClientMessage(
							Component.literal("Cannot equip — no free slots! (" +
									known.getEquippedManipNames().size() + "/" + maxSlots + ")")
									.withStyle(ChatFormatting.RED), true);
				}
			} else {
				if (known.unequipManip(msg.manipName)) {
					player.displayClientMessage(
							Component.literal("Manipulation unequipped: " + msg.manipName)
									.withStyle(ChatFormatting.YELLOW), true);
				}
			}

			// Sync updated state back to client
			PacketHandler.sendToPlayer((ServerPlayer) player, new KnownManipulationServerPacket(known));
		});
		ctx.get().setPacketHandled(true);
	}
}
