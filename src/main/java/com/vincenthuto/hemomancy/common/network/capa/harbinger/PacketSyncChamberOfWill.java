package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ChamberOfWillClientData;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSyncChamberOfWill implements CustomPacketPayload {
	public static final Type<PacketSyncChamberOfWill> TYPE =
			new Type<>(Hemomancy.rloc("packet_sync_chamber_of_will"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncChamberOfWill> STREAM_CODEC =
			StreamCodec.of(PacketSyncChamberOfWill::encode, PacketSyncChamberOfWill::decode);

	private final ResourceLocation skyTheme;
	private final int tier;
	private final int radius;
	private final int qliphothPomesConsumed;

	public PacketSyncChamberOfWill(ResourceLocation skyTheme, int tier, int radius, int qliphothPomesConsumed) {
		this.skyTheme = skyTheme != null ? skyTheme : ChamberOfWillManager.THEME_WILL_DEFAULT;
		this.tier = tier;
		this.radius = radius;
		this.qliphothPomesConsumed = Math.max(0, Math.min(9, qliphothPomesConsumed));
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncChamberOfWill msg) {
		buf.writeResourceLocation(msg.skyTheme);
		buf.writeInt(msg.tier);
		buf.writeInt(msg.radius);
		buf.writeInt(msg.qliphothPomesConsumed);
	}

	public static PacketSyncChamberOfWill decode(FriendlyByteBuf buf) {
		return new PacketSyncChamberOfWill(buf.readResourceLocation(), buf.readInt(), buf.readInt(), buf.readInt());
	}

	public static void handle(PacketSyncChamberOfWill msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> ChamberOfWillClientData.set(msg.skyTheme, msg.tier, msg.radius, msg.qliphothPomesConsumed));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
