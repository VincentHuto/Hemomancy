package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncMuscleMemory(int playerId, CompoundTag state) implements CustomPacketPayload {
    public static final Type<PacketSyncMuscleMemory> TYPE =
            new Type<>(Hemomancy.rloc("sync_muscle_memory"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncMuscleMemory> STREAM_CODEC =
            StreamCodec.of(PacketSyncMuscleMemory::encode, PacketSyncMuscleMemory::decode);

    public PacketSyncMuscleMemory(int playerId, MuscleMemoryState state) {
        this(playerId, state.serializeNBT(null));
    }

    private static void encode(FriendlyByteBuf buffer, PacketSyncMuscleMemory packet) {
        buffer.writeVarInt(packet.playerId);
        buffer.writeNbt(packet.state);
    }

    private static PacketSyncMuscleMemory decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readVarInt();
        CompoundTag state = buffer.readNbt();
        return new PacketSyncMuscleMemory(playerId, state == null ? new CompoundTag() : state);
    }

    public static void handle(PacketSyncMuscleMemory packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var entity = context.player().level().getEntity(packet.playerId);
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                player.getData(HemoAttachmentTypes.MUSCLE_MEMORY).deserializeNBT(null, packet.state);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
