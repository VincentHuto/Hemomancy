package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncStationUpgrades(CompoundTag state) implements CustomPacketPayload {
    public static final Type<PacketSyncStationUpgrades> TYPE = new Type<>(Hemomancy.rloc("sync_station_upgrades"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncStationUpgrades> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeNbt(packet.state()),
            buffer -> new PacketSyncStationUpgrades(buffer.readNbt()));

    public static void handle(PacketSyncStationUpgrades packet, IPayloadContext context) {
        context.enqueueWork(() -> HemoCapabilityAccess.stationUpgrades(context.player())
                .deserializeNBT(context.player().registryAccess(), packet.state()));
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
