package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.ManipulationAmbientParticles;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Cosmetic activation or confirmed-hit accents; carries no gameplay state. */
public record ManipulationAccentPacket(EnumBloodTendency primary, EnumBloodTendency secondary,
        Vec3 position, Vec3 direction, int targetId, boolean impact) implements CustomPacketPayload {
    public static final Type<ManipulationAccentPacket> TYPE=new Type<>(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID,"manipulation_accent"));
    public static final StreamCodec<FriendlyByteBuf,ManipulationAccentPacket> STREAM_CODEC=StreamCodec.of(
            (buf,p)->{
                buf.writeEnum(p.primary);buf.writeBoolean(p.secondary!=null);
                if(p.secondary!=null)buf.writeEnum(p.secondary);
                buf.writeVec3(p.position);buf.writeVec3(p.direction);buf.writeVarInt(p.targetId);buf.writeBoolean(p.impact);
            },buf->new ManipulationAccentPacket(buf.readEnum(EnumBloodTendency.class),
                    buf.readBoolean()?buf.readEnum(EnumBloodTendency.class):null,buf.readVec3(),buf.readVec3(),
                    buf.readVarInt(),buf.readBoolean()));

    public static void handle(ManipulationAccentPacket packet,IPayloadContext context) {
        context.enqueueWork(()->ManipulationAmbientParticles.accept(packet));
    }

    @Override public Type<? extends CustomPacketPayload> type() {return TYPE;}
}
