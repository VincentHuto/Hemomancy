package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DialogueRewardResultPacket(long requestId, boolean delivered) implements CustomPacketPayload {
    public static final Type<DialogueRewardResultPacket> TYPE = new Type<>(Hemomancy.rloc("dialogue_reward_result"));
    public static final StreamCodec<FriendlyByteBuf, DialogueRewardResultPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> { buf.writeLong(msg.requestId); buf.writeBoolean(msg.delivered); },
            buf -> new DialogueRewardResultPacket(buf.readLong(), buf.readBoolean()));

    public static void handle(DialogueRewardResultPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen
                .receiveRewardResult(msg.requestId, msg.delivered));
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
