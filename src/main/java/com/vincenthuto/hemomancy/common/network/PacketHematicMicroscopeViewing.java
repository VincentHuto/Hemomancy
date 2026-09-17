package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.HematicMicroscopeClientState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketHematicMicroscopeViewing(int entityId, long session, long startedAt, Phase phase,
        boolean right, ItemStack instrument, ItemStack sample, boolean examining) implements CustomPacketPayload {
    public enum Phase { START, COMPLETE, RELEASE, CANCEL }
    public static final Type<PacketHematicMicroscopeViewing> TYPE = new Type<>(Hemomancy.rloc("microscope_viewing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketHematicMicroscopeViewing> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.entityId); buf.writeLong(p.session); buf.writeLong(p.startedAt);
                buf.writeEnum(p.phase); buf.writeBoolean(p.right);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, p.instrument);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, p.sample);
                buf.writeBoolean(p.examining);
            }, buf -> new PacketHematicMicroscopeViewing(buf.readVarInt(), buf.readLong(), buf.readLong(),
                    buf.readEnum(Phase.class), buf.readBoolean(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), buf.readBoolean()));
    public PacketHematicMicroscopeViewing { instrument = instrument.copy(); sample = sample.copy(); }
    public static void handle(PacketHematicMicroscopeViewing packet, IPayloadContext context) {
        context.enqueueWork(() -> HematicMicroscopeClientState.accept(packet));
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
