package com.huto.hemomancy.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.container.ContainerVisceralRecaller;
import com.huto.hemomancy.tile.TileEntityVisceralRecaller;

import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketClearRecallerState {

	public PacketClearRecallerState() {
	}

	public static void encode(PacketClearRecallerState msg, PacketBuffer buf) {

	}

	public static PacketClearRecallerState decode(PacketBuffer buf) {
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
				Container container = ctx.get().getSender().openContainer;
				if (container instanceof ContainerVisceralRecaller) {
					TileEntityVisceralRecaller station = ((ContainerVisceralRecaller) container).getTe();
					station.getTendCapability().setTendency(blankTend);
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}