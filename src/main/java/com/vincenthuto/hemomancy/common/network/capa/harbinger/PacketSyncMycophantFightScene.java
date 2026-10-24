package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PacketSyncMycophantFightScene(boolean active, BlockPos center) implements CustomPacketPayload {
	public static final Type<PacketSyncMycophantFightScene> TYPE = new Type<>(Hemomancy.rloc("sync_mycophant_fight_scene"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncMycophantFightScene> STREAM_CODEC = StreamCodec.of(PacketSyncMycophantFightScene::encode, PacketSyncMycophantFightScene::decode);
	public static PacketSyncMycophantFightScene activate(BlockPos center) { return new PacketSyncMycophantFightScene(true, center); }
	public static PacketSyncMycophantFightScene clearScene() { return new PacketSyncMycophantFightScene(false, BlockPos.ZERO); }
	private static void encode(FriendlyByteBuf buffer, PacketSyncMycophantFightScene packet) { buffer.writeBoolean(packet.active); buffer.writeBlockPos(packet.center); }
	private static PacketSyncMycophantFightScene decode(FriendlyByteBuf buffer) { return new PacketSyncMycophantFightScene(buffer.readBoolean(), buffer.readBlockPos()); }
	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
