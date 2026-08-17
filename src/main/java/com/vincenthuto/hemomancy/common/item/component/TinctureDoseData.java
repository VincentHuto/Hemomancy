package com.vincenthuto.hemomancy.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record TinctureDoseData(int remaining, int maximum) {
    public static final Codec<TinctureDoseData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("remaining").forGetter(TinctureDoseData::remaining),
            Codec.INT.fieldOf("maximum").forGetter(TinctureDoseData::maximum))
            .apply(instance, TinctureDoseData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, TinctureDoseData> STREAM_CODEC = StreamCodec.of(
            (buffer, data) -> {
                buffer.writeVarInt(data.remaining);
                buffer.writeVarInt(data.maximum);
            },
            buffer -> new TinctureDoseData(buffer.readVarInt(), buffer.readVarInt()));

    public TinctureDoseData {
        maximum = Math.max(1, maximum);
        remaining = Math.max(0, Math.min(maximum, remaining));
    }
}
