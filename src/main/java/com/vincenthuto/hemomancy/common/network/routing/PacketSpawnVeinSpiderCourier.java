package com.vincenthuto.hemomancy.common.network.routing;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.VeinSpiderCourierClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSpawnVeinSpiderCourier(BlockPos from, BlockPos to, ItemStack stack, int seed)
        implements CustomPacketPayload {
    public static final Type<PacketSpawnVeinSpiderCourier> TYPE =
            new Type<>(Hemomancy.rloc("spawn_vein_spider_courier"));
    public static final StreamCodec<FriendlyByteBuf, PacketSpawnVeinSpiderCourier> STREAM_CODEC =
            StreamCodec.of(PacketSpawnVeinSpiderCourier::encode, PacketSpawnVeinSpiderCourier::decode);

    public static void encode(FriendlyByteBuf buf, PacketSpawnVeinSpiderCourier msg) {
        buf.writeBlockPos(msg.from);
        buf.writeBlockPos(msg.to);
        ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, msg.stack);
        buf.writeInt(msg.seed);
    }

    public static PacketSpawnVeinSpiderCourier decode(FriendlyByteBuf buf) {
        return new PacketSpawnVeinSpiderCourier(buf.readBlockPos(), buf.readBlockPos(),
                ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf), buf.readInt());
    }

    public static void handle(final PacketSpawnVeinSpiderCourier msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> VeinSpiderCourierClientData.spawn(msg.from, msg.to, msg.stack, msg.seed));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
