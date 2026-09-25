package com.vincenthuto.hemomancy.common.enchanting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ScriptoriumProvenance(String curse, int severity, int excessLoad) {
    public static final Codec<ScriptoriumProvenance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("curse").forGetter(ScriptoriumProvenance::curse),
            Codec.INT.fieldOf("severity").forGetter(ScriptoriumProvenance::severity),
            Codec.INT.fieldOf("excess_load").forGetter(ScriptoriumProvenance::excessLoad)
    ).apply(instance, ScriptoriumProvenance::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScriptoriumProvenance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ScriptoriumProvenance::curse,
            ByteBufCodecs.VAR_INT, ScriptoriumProvenance::severity,
            ByteBufCodecs.VAR_INT, ScriptoriumProvenance::excessLoad,
            ScriptoriumProvenance::new);

    public static final ScriptoriumProvenance CLEAN = new ScriptoriumProvenance("", 0, 0);
}
