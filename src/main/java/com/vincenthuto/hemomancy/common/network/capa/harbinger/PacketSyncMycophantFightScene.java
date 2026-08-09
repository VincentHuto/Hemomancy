package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.client.data.MycophantFightClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncMycophantFightScene(boolean active, BlockPos center) implements CustomPacketPayload {
	public static final Type<PacketSyncMycophantFightScene> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hemomancy", "sync_mycophant_fight_scene"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncMycophantFightScene> STREAM_CODEC = StreamCodec.of(PacketSyncMycophantFightScene::encode, PacketSyncMycophantFightScene::decode);
	public static PacketSyncMycophantFightScene activate(BlockPos center) { return new PacketSyncMycophantFightScene(true, center); }
	public static PacketSyncMycophantFightScene clearScene() { return new PacketSyncMycophantFightScene(false, BlockPos.ZERO); }
	private static void encode(FriendlyByteBuf buffer, PacketSyncMycophantFightScene packet) { buffer.writeBoolean(packet.active); buffer.writeBlockPos(packet.center); }
	private static PacketSyncMycophantFightScene decode(FriendlyByteBuf buffer) { return new PacketSyncMycophantFightScene(buffer.readBoolean(), buffer.readBlockPos()); }
	public static void handle(PacketSyncMycophantFightScene packet, IPayloadContext context) { context.enqueueWork(() -> { if (packet.active) MycophantFightClientData.activate(packet.center); else MycophantFightClientData.clear(); }); }
	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
