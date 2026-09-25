package com.vincenthuto.hemomancy.common.enchanting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record ResonantPattern(List<Entry> enchantments, String curse, int curseSeverity,
                              int excessLoad, boolean master) {
    public static final ResonantPattern EMPTY = new ResonantPattern(List.of(), "", 0, 0, false);

    public static final Codec<ResonantPattern> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("enchantments").forGetter(ResonantPattern::enchantments),
            Codec.STRING.optionalFieldOf("curse", "").forGetter(ResonantPattern::curse),
            Codec.INT.optionalFieldOf("curse_severity", 0).forGetter(ResonantPattern::curseSeverity),
            Codec.INT.optionalFieldOf("excess_load", 0).forGetter(ResonantPattern::excessLoad),
            Codec.BOOL.optionalFieldOf("master", false).forGetter(ResonantPattern::master)
    ).apply(instance, ResonantPattern::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonantPattern> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ResonantPattern decode(RegistryFriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            var enchantments = new java.util.ArrayList<Entry>(size);
            for (int i = 0; i < size; i++) enchantments.add(Entry.STREAM_CODEC.decode(buffer));
            return new ResonantPattern(List.copyOf(enchantments), buffer.readUtf(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ResonantPattern value) {
            buffer.writeVarInt(value.enchantments.size());
            value.enchantments.forEach(entry -> Entry.STREAM_CODEC.encode(buffer, entry));
            buffer.writeUtf(value.curse);
            buffer.writeVarInt(value.curseSeverity);
            buffer.writeVarInt(value.excessLoad);
            buffer.writeBoolean(value.master);
        }
    };

    public ResonantPattern {
        enchantments = enchantments == null ? List.of() : List.copyOf(enchantments);
        curse = curse == null ? "" : curse;
        curseSeverity = Math.max(0, curseSeverity);
        excessLoad = Math.max(0, excessLoad);
    }

    public boolean isEmpty() {
        return enchantments.isEmpty() && curse.isBlank();
    }

    public int recordedLoad() {
        return enchantments.stream().mapToInt(Entry::level).sum() + curseSeverity;
    }

    public ResonantPattern asMaster() {
        return master ? this : new ResonantPattern(enchantments, curse, curseSeverity, excessLoad, true);
    }

    public record Entry(String enchantment, int level) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Entry::enchantment),
                Codec.INT.fieldOf("level").forGetter(Entry::level)
        ).apply(instance, Entry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Entry::enchantment,
                ByteBufCodecs.VAR_INT, Entry::level,
                Entry::new);

        public Entry {
            enchantment = enchantment == null ? "" : enchantment;
            level = Math.max(1, level);
        }
    }
}
