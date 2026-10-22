package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

public record OpenPendingWhisperPacket(UUID id, DialogueTree tree) implements CustomPacketPayload {
    public static final Type<OpenPendingWhisperPacket> TYPE = new Type<>(Hemomancy.rloc("open_pending_whisper"));
    public static final StreamCodec<FriendlyByteBuf, OpenPendingWhisperPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> { buf.writeUUID(packet.id()); packet.tree().toNetwork(buf); },
            buf -> new OpenPendingWhisperPacket(buf.readUUID(), DialogueTree.fromNetwork(buf)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
