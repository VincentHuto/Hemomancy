package com.huto.hemomancy.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.huto.hemomancy.container.ContainerChiselStation;
import com.huto.hemomancy.tile.BlockEntityChiselStation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketUpdateChiselRunes {
	private List<Integer> runes;

	public PacketUpdateChiselRunes(List<Integer> runesIn) {
		this.runes = runesIn;
	}

	public static void encode(PacketUpdateChiselRunes msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.runes.size());
		for (Integer i : msg.runes) {
			buf.writeInt(i);
		}
	}

	public static PacketUpdateChiselRunes decode(FriendlyByteBuf buf) {
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
				AbstractContainerMenu container = ctx.get().getSender().containerMenu;
				if (container instanceof ContainerChiselStation) {
					BlockEntityChiselStation station = ((ContainerChiselStation) container).getTe();
					station.setRuneList(msg.getRunes());
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}