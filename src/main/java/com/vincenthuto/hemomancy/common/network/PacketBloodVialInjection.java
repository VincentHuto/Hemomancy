package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.BloodVialInjectionClientState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketBloodVialInjection(int entityId, long startedAt, Phase phase, InteractionHand hand,
        boolean right, ItemStack vial, EnumBloodTendency tendency) implements CustomPacketPayload {
    public enum Phase { START, IMPACT, CANCEL }

    public static final Type<PacketBloodVialInjection> TYPE = new Type<>(Hemomancy.rloc("blood_vial_injection"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketBloodVialInjection> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.entityId);
                buf.writeLong(packet.startedAt);
                buf.writeEnum(packet.phase);
                buf.writeEnum(packet.hand);
                buf.writeBoolean(packet.right);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, packet.vial);
                buf.writeEnum(packet.tendency);
            }, buf -> new PacketBloodVialInjection(buf.readVarInt(), buf.readLong(), buf.readEnum(Phase.class),
                    buf.readEnum(InteractionHand.class), buf.readBoolean(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    buf.readEnum(EnumBloodTendency.class)));

    public PacketBloodVialInjection { vial = vial.copy(); }

    public static void handle(PacketBloodVialInjection packet, IPayloadContext context) {
        context.enqueueWork(() -> BloodVialInjectionClientState.accept(packet));
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
