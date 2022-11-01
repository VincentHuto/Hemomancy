package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.container.VisceralRecallerMenu;
import com.vincenthuto.hemomancy.tile.VisceralRecallerBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class ClearRecallerStatePacket {

	public ClearRecallerStatePacket() {
	}

	public static void encode(ClearRecallerStatePacket msg, FriendlyByteBuf buf) {

	}

	public static ClearRecallerStatePacket decode(FriendlyByteBuf buf) {
		return new ClearRecallerStatePacket();
	}

	public static class Handler {

		public static void handle(final ClearRecallerStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
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
				if (container instanceof VisceralRecallerMenu) {
					VisceralRecallerBlockEntity station = ((VisceralRecallerMenu) container).getTe();
					station.getTendCapability().setTendency(blankTend);
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}