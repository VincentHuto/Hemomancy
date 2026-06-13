package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.LastDeathMemory;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.LastDeathMemorySnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncLastDeathMemory(LastDeathMemorySnapshot snapshot) implements CustomPacketPayload {
    public static final Type<PacketSyncLastDeathMemory> TYPE =
            new Type<>(Hemomancy.rloc("sync_last_death_memory"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncLastDeathMemory> STREAM_CODEC =
            StreamCodec.of(PacketSyncLastDeathMemory::encode, PacketSyncLastDeathMemory::decode);

    public static void encode(FriendlyByteBuf buf, PacketSyncLastDeathMemory msg) {
        buf.writeBoolean(msg.snapshot.present());
        buf.writeUtf(msg.snapshot.dimensionId());
        buf.writeLong(msg.snapshot.packedPosition());
        buf.writeLong(msg.snapshot.gameTime());
    }

    public static PacketSyncLastDeathMemory decode(FriendlyByteBuf buf) {
        return new PacketSyncLastDeathMemory(new LastDeathMemorySnapshot(buf.readBoolean(), buf.readUtf(),
                buf.readLong(), buf.readLong()));
    }

    public static void handle(PacketSyncLastDeathMemory msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player != null) {
                LastDeathMemory memory = HemoCapabilityAccess.getLastDeathMemory(player);
                msg.snapshot.applyTo(memory);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
