package com.vincenthuto.hemomancy.common.network.capa.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarStationMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PacketUpdateScarPattern implements CustomPacketPayload {

	public static final Type<PacketUpdateScarPattern> TYPE = new Type<>(Hemomancy.rloc("packet_update_scar_pattern"));
	public static final StreamCodec<FriendlyByteBuf, PacketUpdateScarPattern> STREAM_CODEC = StreamCodec.of(PacketUpdateScarPattern::encode, PacketUpdateScarPattern::decode);
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

		public static void handle(final PacketUpdateScarPattern msg, final IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				AbstractContainerMenu container = ctx.player().containerMenu;
				if (container instanceof ScarStationMenu) {
					ScarStationBlockEntity station = ((ScarStationMenu) container).getTe();
					station.setScarList(msg.getPattern());
				}
			});
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
