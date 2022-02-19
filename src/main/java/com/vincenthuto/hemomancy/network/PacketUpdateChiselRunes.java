package com.vincenthuto.hemomancy.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.container.MenuChiselStation;
import com.vincenthuto.hemomancy.recipe.serializer.ChiselRecipe;
import com.vincenthuto.hemomancy.tile.BlockEntityChiselStation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class PacketUpdateChiselRunes {
	public byte[][] pattern;

	public PacketUpdateChiselRunes(byte[][] patternIn) {
		this.pattern = patternIn;
	}

	public static void encode(PacketUpdateChiselRunes msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.pattern.length);
		for (int i = 0; i < msg.pattern.length; ++i) {
			buf.writeByteArray(msg.pattern[i]);
		}
	}

	public static PacketUpdateChiselRunes decode(FriendlyByteBuf buf) {
		int listSize = buf.readInt();
		byte[][] pattern = new byte[listSize][];
		for (int i = 0; i < listSize; ++i) {
			pattern[i] = buf.readByteArray();
		}

		return new PacketUpdateChiselRunes(pattern);
	}

	public byte[][] getPattern() {
		return pattern;
	}

	public static class Handler {

		public static void handle(final PacketUpdateChiselRunes msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				AbstractContainerMenu container = ctx.get().getSender().containerMenu;
				if (container instanceof MenuChiselStation) {
					BlockEntityChiselStation station = ((MenuChiselStation) container).getTe();
					station.setRuneList(msg.getPattern());
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}