package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server interruption clears held charge and requires a fresh key press. */
public record ManipulationInterruptedPacket() implements CustomPacketPayload {
    public static final ManipulationInterruptedPacket INSTANCE=new ManipulationInterruptedPacket();
    public static final Type<ManipulationInterruptedPacket> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID,"manipulation_interrupted"));
    public static final StreamCodec<FriendlyByteBuf,ManipulationInterruptedPacket> STREAM_CODEC=StreamCodec.unit(INSTANCE);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
