package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.UUID;

public record ClosePendingWhisperPacket(UUID id) implements CustomPacketPayload {
    public static final Type<ClosePendingWhisperPacket> TYPE = new Type<>(Hemomancy.rloc("close_pending_whisper"));
    public static final StreamCodec<FriendlyByteBuf, ClosePendingWhisperPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUUID(packet.id()), buf -> new ClosePendingWhisperPacket(buf.readUUID()));
    public static void handle(ClosePendingWhisperPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) PendingWhispers.close(player, packet.id());
        });
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
