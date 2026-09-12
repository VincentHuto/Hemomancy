package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSchoolCombatS2CPacket(int entityId, CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncSchoolCombatS2CPacket> TYPE = new Type<>(Hemomancy.rloc("school_combat"));
    public static final StreamCodec<FriendlyByteBuf, SyncSchoolCombatS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> { buffer.writeVarInt(packet.entityId); buffer.writeNbt(packet.data); },
            buffer -> new SyncSchoolCombatS2CPacket(buffer.readVarInt(), buffer.readNbt()));

    public static void handle(SyncSchoolCombatS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level != null && packet.data != null && level.getEntity(packet.entityId) instanceof LivingEntity target)
                SchoolStates.data(target).deserializeNBT(level.registryAccess(), packet.data);
        });
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

