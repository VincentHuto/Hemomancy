package com.vincenthuto.hemomancy.common.network.capa.scars;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarStationMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.NetworkEvent;

public class PacketUpdateScarPattern {
	public byte[][] pattern;

	public PacketUpdateScarPattern(byte[][] patternIn) {
		this.pattern = patternIn;
	}

	public static void encode(PacketUpdateScarPattern msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.pattern.length);
		for (int i = 0; i < msg.pattern.length; ++i) {
			buf.writeByteArray(msg.pattern[i]);
		}
	}

	public static PacketUpdateScarPattern decode(FriendlyByteBuf buf) {
		int listSize = buf.readInt();
		byte[][] pattern = new byte[listSize][];
		for (int i = 0; i < listSize; ++i) {
			pattern[i] = buf.readByteArray();
		}

		return new PacketUpdateScarPattern(pattern);
	}

	public byte[][] getPattern() {
		return pattern;
	}

	public static class Handler {

		public static void handle(final PacketUpdateScarPattern msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				AbstractContainerMenu container = ctx.get().getSender().containerMenu;
				if (container instanceof ScarStationMenu) {
					ScarStationBlockEntity station = ((ScarStationMenu) container).getTe();
					station.setScarList(msg.getPattern());
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}