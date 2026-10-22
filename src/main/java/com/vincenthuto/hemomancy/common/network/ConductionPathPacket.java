package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.ConductorGraph;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Empty nodes remove the cosmetic network; positions never authorize gameplay. */
public record ConductionPathPacket(UUID networkId, List<BlockPos> nodes, long expiresAt)
        implements CustomPacketPayload {
    public static final Type<ConductionPathPacket> TYPE = new Type<>(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID,"conduction_path"));
    public static final StreamCodec<FriendlyByteBuf,ConductionPathPacket> STREAM_CODEC =
            StreamCodec.of(ConductionPathPacket::encode, ConductionPathPacket::decode);

    public ConductionPathPacket {
        if (nodes.size() > ConductorGraph.MAX_NODES) throw new IllegalArgumentException("Conduction node limit");
        nodes = nodes.stream().map(BlockPos::immutable).toList();
    }

    private static void encode(FriendlyByteBuf buf, ConductionPathPacket packet) {
        buf.writeUUID(packet.networkId()); buf.writeLong(packet.expiresAt());
        buf.writeVarInt(packet.nodes().size());
        for (BlockPos node : packet.nodes()) buf.writeBlockPos(node);
    }

    private static ConductionPathPacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID(); long expiry = buf.readLong(); int count = buf.readVarInt();
        if (count < 0 || count > ConductorGraph.MAX_NODES) throw new DecoderException("Conduction node limit");
        List<BlockPos> nodes = new ArrayList<>(count);
        for (int i=0;i<count;i++) nodes.add(buf.readBlockPos());
        return new ConductionPathPacket(id,nodes,expiry);
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
