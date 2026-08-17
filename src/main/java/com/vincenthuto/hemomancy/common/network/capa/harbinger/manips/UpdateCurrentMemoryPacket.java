package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEntryKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemorySlotRef;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateCurrentMemoryPacket(String storageKey) implements CustomPacketPayload {
    public static final Type<UpdateCurrentMemoryPacket> TYPE =
            new Type<>(Hemomancy.rloc("update_current_memory"));
    public static final StreamCodec<FriendlyByteBuf, UpdateCurrentMemoryPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeUtf(packet.storageKey),
            buffer -> new UpdateCurrentMemoryPacket(buffer.readUtf()));

    public static void handle(UpdateCurrentMemoryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            var known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
            if (known == null) return;
            MemorySlotRef ref = MemorySlotRef.fromStorageKey(packet.storageKey);
            if (ref.kind() != MemoryEntryKind.MUSCLE_MEMORY || !known.isMemoryEquipped(ref)
                    || ref.muscleMemory().filter(player.getData(HemoAttachmentTypes.MUSCLE_MEMORY)::knows).isEmpty()) {
                return;
            }
            known.setSelectedMemoryRef(ref);
            PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
        });
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
