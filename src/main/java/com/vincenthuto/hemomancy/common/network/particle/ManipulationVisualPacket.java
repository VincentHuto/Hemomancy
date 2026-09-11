package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.ManipulationVisualRenderer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ManipulationVisualPacket(Form form, int entityId, Vec3 from, Vec3 to,
        float radius, int ticks, int count) implements CustomPacketPayload {
    public static final Type<ManipulationVisualPacket> TYPE = new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "manipulation_visual"));
    public static final StreamCodec<FriendlyByteBuf, ManipulationVisualPacket> STREAM_CODEC =
            StreamCodec.of(ManipulationVisualPacket::encode, ManipulationVisualPacket::decode);

    private static void encode(FriendlyByteBuf buf, ManipulationVisualPacket p) {
        buf.writeEnum(p.form); buf.writeInt(p.entityId);
        buf.writeVec3(p.from); buf.writeVec3(p.to);
        buf.writeFloat(p.radius); buf.writeVarInt(p.ticks); buf.writeVarInt(p.count);
    }

    private static ManipulationVisualPacket decode(FriendlyByteBuf buf) {
        return new ManipulationVisualPacket(buf.readEnum(Form.class), buf.readInt(), buf.readVec3(),
                buf.readVec3(), buf.readFloat(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ManipulationVisualPacket p, IPayloadContext context) {
        context.enqueueWork(() -> ManipulationVisualRenderer.accept(p));
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
