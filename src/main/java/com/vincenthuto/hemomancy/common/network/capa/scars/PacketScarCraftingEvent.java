package com.vincenthuto.hemomancy.common.network.capa.scars;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarStationMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PacketScarCraftingEvent implements CustomPacketPayload {

	public static final Type<PacketScarCraftingEvent> TYPE = new Type<>(Hemomancy.rloc("packet_scar_crafting_event"));
	public static final StreamCodec<FriendlyByteBuf, PacketScarCraftingEvent> STREAM_CODEC = StreamCodec.of(PacketScarCraftingEvent::encode, PacketScarCraftingEvent::decode);

	/** Minimum initiatory degree required to use the Cerebral Scar Station. */
	private static final int REQUIRED_DEGREE = 4;

	public PacketScarCraftingEvent() {
	}

	public static void encode(FriendlyByteBuf buf, PacketScarCraftingEvent msg) {
	}

	public static PacketScarCraftingEvent decode(FriendlyByteBuf buf) {
		return new PacketScarCraftingEvent();
	}

	public static void handle(final PacketScarCraftingEvent msg, final IPayloadContext ctx) {
		Handler.handle(msg, ctx);
	}

	public static class Handler {

		public static void handle(final PacketScarCraftingEvent msg, final IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				Player packetPlayer = ctx.player();
				if (!(packetPlayer instanceof ServerPlayer player)) return;

				// ── Degree gate: Scar crafting requires Adept (degree 4) ──
				int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
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
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
