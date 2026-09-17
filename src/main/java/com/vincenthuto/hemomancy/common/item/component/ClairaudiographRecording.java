package com.vincenthuto.hemomancy.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import java.util.Set;

/** Raw strings deliberately preserve unknown values as an unreadable recording, never a blank. */
public record ClairaudiographRecording(String source, String sound, String kind, float pitch) {
    public static final Set<String> KINDS = Set.of("ambient", "hurt", "death", "step", "attack", "warning", "celebration", "special");
    public static final Codec<ClairaudiographRecording> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.orElse("").optionalFieldOf("source", "").forGetter(ClairaudiographRecording::source),
        Codec.STRING.orElse("").optionalFieldOf("sound", "").forGetter(ClairaudiographRecording::sound),
        Codec.STRING.orElse("").optionalFieldOf("kind", "").forGetter(ClairaudiographRecording::kind),
        Codec.FLOAT.orElse(0F).optionalFieldOf("pitch", 1F).forGetter(ClairaudiographRecording::pitch)
    ).apply(i, ClairaudiographRecording::new));
    public static final StreamCodec<FriendlyByteBuf, ClairaudiographRecording> STREAM_CODEC = StreamCodec.of(
        (b,r) -> { b.writeUtf(r.source); b.writeUtf(r.sound); b.writeUtf(r.kind); b.writeFloat(r.pitch); },
        b -> new ClairaudiographRecording(b.readUtf(), b.readUtf(), b.readUtf(), b.readFloat()));
    public boolean readable() {
        return source.contains(":") && sound.contains(":") && ResourceLocation.tryParse(source) != null
            && ResourceLocation.tryParse(sound) != null && KINDS.contains(kind) && Float.isFinite(pitch) && pitch >= .5F && pitch <= 2F;
    }
}
