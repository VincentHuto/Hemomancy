package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueRewardClaims;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DialogueRewardChoicePacket(String eventId, int entityId, long requestId) implements CustomPacketPayload {
    public static final Type<DialogueRewardChoicePacket> TYPE = new Type<>(Hemomancy.rloc("dialogue_reward_choice"));
    public static final StreamCodec<FriendlyByteBuf, DialogueRewardChoicePacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> { buf.writeUtf(msg.eventId); buf.writeInt(msg.entityId); buf.writeLong(msg.requestId); },
            buf -> new DialogueRewardChoicePacket(buf.readUtf(), buf.readInt(), buf.readLong()));

    public static void handle(DialogueRewardChoicePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            var event = DialogueRewardClaims.requiresAcknowledgement(msg.eventId)
                    ? DialogueOptionPacket.dispatch(player, msg.eventId, msg.entityId) : null;
            PacketHandler.sendToPlayer(player, new DialogueRewardResultPacket(msg.requestId,
                    event != null && event.wasRewardDelivered()));
        });
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
