package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

/** A Living Syringe landed a sample. {@code load} is the syringe's model load with this sample counted. */
public record PacketLivingSyringeDraw(int entityId, int targetId, InteractionHand hand, boolean right, int bloodColor,
        int load) implements CustomPacketPayload {
    public static final Type<PacketLivingSyringeDraw> TYPE = new Type<>(Hemomancy.rloc("living_syringe_draw"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketLivingSyringeDraw> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.entityId);
                buf.writeVarInt(packet.targetId);
                buf.writeEnum(packet.hand);
                buf.writeBoolean(packet.right);
                buf.writeInt(packet.bloodColor);
                buf.writeVarInt(packet.load);
            }, buf -> new PacketLivingSyringeDraw(buf.readVarInt(), buf.readVarInt(), buf.readEnum(InteractionHand.class),
                    buf.readBoolean(), buf.readInt(), buf.readVarInt()));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
