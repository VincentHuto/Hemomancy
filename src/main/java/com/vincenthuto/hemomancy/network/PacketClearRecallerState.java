package com.vincenthuto.hemomancy.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.container.MenuVisceralRecaller;
import com.vincenthuto.hemomancy.tile.BlockEntityVisceralRecaller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketClearRecallerState {

	public PacketClearRecallerState() {
	}

	public static void encode(PacketClearRecallerState msg, FriendlyByteBuf buf) {

	}

	public static PacketClearRecallerState decode(FriendlyByteBuf buf) {
		return new PacketClearRecallerState();
	}

	public static class Handler {

		public static void handle(final PacketClearRecallerState msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				@SuppressWarnings("serial")
				Map<EnumBloodTendency, Float> blankTend = new HashMap<EnumBloodTendency, Float>() {
					{
						put(EnumBloodTendency.ANIMUS, 0f);
						put(EnumBloodTendency.MORTEM, 0f);
						put(EnumBloodTendency.DUCTILIS, 0f);
						put(EnumBloodTendency.FERRIC, 0f);
						put(EnumBloodTendency.LUX, 0f);
						put(EnumBloodTendency.TENEBRIS, 0f);
						put(EnumBloodTendency.FLAMMEUS, 0f);
						put(EnumBloodTendency.CONGEATIO, 0f);

					}
				};
				AbstractContainerMenu container = ctx.get().getSender().containerMenu;
				if (container instanceof MenuVisceralRecaller) {
					BlockEntityVisceralRecaller station = ((MenuVisceralRecaller) container).getTe();
					station.getTendCapability().setTendency(blankTend);
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}