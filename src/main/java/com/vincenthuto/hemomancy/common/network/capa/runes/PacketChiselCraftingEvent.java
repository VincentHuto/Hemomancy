package com.vincenthuto.hemomancy.common.network.capa.runes;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.menu.ChiselStationMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.ChiselStationBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class PacketChiselCraftingEvent {

	/** Minimum initiatory degree required to use the Runic Chisel Station. */
	private static final int REQUIRED_DEGREE = 4;

	public PacketChiselCraftingEvent() {
	}

	public static void encode(PacketChiselCraftingEvent msg, FriendlyByteBuf buf) {
	}

	public static PacketChiselCraftingEvent decode(FriendlyByteBuf buf) {
		return new PacketChiselCraftingEvent();
	}

	public static class Handler {

		public static void handle(final PacketChiselCraftingEvent msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayer player = ctx.get().getSender();
				if (player == null) return;

				// ── Degree gate: Rune crafting requires Adept (degree 4) ──
				int playerDegree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
				if (playerDegree < REQUIRED_DEGREE) {
					player.displayClientMessage(
							Component.literal("The chisel resists your hand — ")
									.withStyle(ChatFormatting.RED)
									.append(Component.literal("Adept")
											.withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
									.append(Component.literal(" rank (Degree " + REQUIRED_DEGREE
											+ ") is required to carve runes.")
											.withStyle(ChatFormatting.RED)),
							false);
					return;
				}

				AbstractContainerMenu container = player.containerMenu;
				if (container instanceof ChiselStationMenu) {
					ChiselStationBlockEntity station = ((ChiselStationMenu) container).getTe();
					station.getCurrentRecipe();
					station.craftEvent();
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}