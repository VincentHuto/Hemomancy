package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PhlegethonticCurrentPacket(double x, double z) implements CustomPacketPayload {
    public static final Type<PhlegethonticCurrentPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID,"phlegethontic_current"));
    public static final StreamCodec<FriendlyByteBuf, PhlegethonticCurrentPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> { buf.writeDouble(packet.x); buf.writeDouble(packet.z); },
            buf -> new PhlegethonticCurrentPacket(buf.readDouble(), buf.readDouble()));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public Vec3 apply(Vec3 velocity) {
        if (velocity.horizontalDistanceSqr() >= .09) return velocity;
        Vec3 pushed = velocity.add(x, 0, z);
        double speed = pushed.horizontalDistance();
        return speed > .3 ? new Vec3(pushed.x * .3 / speed, velocity.y, pushed.z * .3 / speed) : pushed;
    }

    public static void handle(PhlegethonticCurrentPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.isAlive() && player.isInFluidType(FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get()))
                player.setDeltaMovement(packet.apply(player.getDeltaMovement()));
        });
    }
}
