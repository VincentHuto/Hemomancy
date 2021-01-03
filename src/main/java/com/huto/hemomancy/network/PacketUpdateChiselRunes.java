package com.huto.hemomancy.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.huto.hemomancy.containers.ContainerChiselStation;
import com.huto.hemomancy.tile.TileEntityChiselStation;

import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUpdateChiselRunes {
	private List<Integer> runes;

	public PacketUpdateChiselRunes(List<Integer> runesIn) {
		this.runes = runesIn;
	}

	public static void encode(PacketUpdateChiselRunes msg, PacketBuffer buf) {
		buf.writeInt(msg.runes.size());
		for (Integer i : msg.runes) {
			buf.writeInt(i);
		}
	}

	public static PacketUpdateChiselRunes decode(PacketBuffer buf) {
		List<Integer> runesIn = new ArrayList<Integer>();
		int listSize = buf.readInt();
		for (int is = 0; is < listSize; is++) {
			runesIn.add(buf.readInt());
		}
		return new PacketUpdateChiselRunes(runesIn);
	}

	public List<Integer> getRunes() {
		return runes;
	}

	public static class Handler {

		public static void handle(final PacketUpdateChiselRunes msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				Container container = ctx.get().getSender().openContainer;
				if (container instanceof ContainerChiselStation) {
					TileEntityChiselStation station = ((ContainerChiselStation) container).getTe();
					station.setRuneList(msg.getRunes());
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}