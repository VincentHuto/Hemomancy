package com.vincenthuto.hemomancy.common.network.capa.scars;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarStationMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.NetworkEvent;

public class PacketScarCraftingEvent {

	/** Minimum initiatory degree required to use the Cerebral Scar Station. */
	private static final int REQUIRED_DEGREE = 4;

	public PacketScarCraftingEvent() {
	}

	public static void encode(PacketScarCraftingEvent msg, FriendlyByteBuf buf) {
	}

	public static PacketScarCraftingEvent decode(FriendlyByteBuf buf) {
		return new PacketScarCraftingEvent();
	}

	public static class Handler {

		public static void handle(final PacketScarCraftingEvent msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayer player = ctx.get().getSender();
				if (player == null) return;

				// ── Degree gate: Scar crafting requires Adept (degree 4) ──
				int playerDegree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
				if (playerDegree < REQUIRED_DEGREE) {
					player.displayClientMessage(
							Component.literal("The chisel resists your hand — ")
									.withStyle(ChatFormatting.RED)
									.append(Component.literal("Adept")
											.withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
									.append(Component.literal(" rank (Degree " + REQUIRED_DEGREE
											+ ") is required to carve scars.")
											.withStyle(ChatFormatting.RED)),
							false);
					return;
				}

				AbstractContainerMenu container = player.containerMenu;
				if (container instanceof ScarStationMenu) {
					ScarStationBlockEntity station = ((ScarStationMenu) container).getTe();
					station.getCurrentRecipe();
					station.craftEvent();
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}