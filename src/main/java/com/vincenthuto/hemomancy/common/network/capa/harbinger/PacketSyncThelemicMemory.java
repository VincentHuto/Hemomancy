package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncThelemicMemory(int playerId, CompoundTag state) implements CustomPacketPayload {
    public static final Type<PacketSyncThelemicMemory> TYPE =
            new Type<>(Hemomancy.rloc("sync_thelemic_memory"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncThelemicMemory> STREAM_CODEC =
            StreamCodec.of(PacketSyncThelemicMemory::encode, PacketSyncThelemicMemory::decode);

    public PacketSyncThelemicMemory(int playerId, ThelemicMemoryState state) {
        this(playerId, state.serializeNBT(null));
    }

    private static void encode(FriendlyByteBuf buffer, PacketSyncThelemicMemory packet) {
        buffer.writeVarInt(packet.playerId);
        buffer.writeNbt(packet.state);
    }

    private static PacketSyncThelemicMemory decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readVarInt();
        CompoundTag state = buffer.readNbt();
        return new PacketSyncThelemicMemory(playerId, state == null ? new CompoundTag() : state);
    }

    public static void handle(PacketSyncThelemicMemory packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var entity = context.player().level().getEntity(packet.playerId);
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                HemoCapabilityAccess.thelemicMemory(player).deserializeNBT(null, packet.state);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
