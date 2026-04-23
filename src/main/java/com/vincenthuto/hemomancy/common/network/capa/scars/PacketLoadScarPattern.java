package com.vincenthuto.hemomancy.common.network.capa.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarStationMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Sent from client to server when a player places an ItemScarPattern
 * into the pattern slot. The server will read the pattern from the item
 * and auto-fill the scar grid.
 */
public class PacketLoadScarPattern implements CustomPacketPayload {

	public static final Type<PacketLoadScarPattern> TYPE = new Type<>(Hemomancy.rloc("packet_load_scar_pattern"));
	public static final StreamCodec<FriendlyByteBuf, PacketLoadScarPattern> STREAM_CODEC = StreamCodec.of(PacketLoadScarPattern::encode, PacketLoadScarPattern::decode);

	public PacketLoadScarPattern() {
	}

	public static void encode(FriendlyByteBuf buf, PacketLoadScarPattern msg) {
	}

	public static PacketLoadScarPattern decode(FriendlyByteBuf buf) {
		return new PacketLoadScarPattern();
	}

	public static void handle(final PacketLoadScarPattern msg, final IPayloadContext ctx) {
		Handler.handle(msg, ctx);
	}

	public static class Handler {

		public static void handle(final PacketLoadScarPattern msg, final IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				AbstractContainerMenu container = ctx.player().containerMenu;
				if (container instanceof ScarStationMenu) {
					ScarStationBlockEntity station = ((ScarStationMenu) container).getTe();
					station.tryLoadPatternFromSlot();
				}
			});
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
