package com.vincenthuto.hemomancy.common.brewing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

public record AdvancedBrewData(String kind, List<PotionContents> parts, String attachment, int doses) {
    private record Raw(String kind, List<PotionContents> parts, String attachment, int doses) {}

    private static final Codec<Raw> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("kind").forGetter(Raw::kind),
            PotionContents.CODEC.listOf().fieldOf("parts").forGetter(Raw::parts),
            Codec.STRING.optionalFieldOf("attachment", "").forGetter(Raw::attachment),
            Codec.INT.optionalFieldOf("doses", 0).forGetter(Raw::doses)
    ).apply(instance, Raw::new));

    public static final Codec<AdvancedBrewData> CODEC = RAW_CODEC.flatXmap(raw -> {
        try {
            return DataResult.success(new AdvancedBrewData(raw.kind(), raw.parts(), raw.attachment(), raw.doses()));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }, data -> DataResult.success(new Raw(data.kind(), data.parts(), data.attachment(), data.doses())));

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedBrewData> STREAM_CODEC = StreamCodec.of(
            (buffer, data) -> {
                buffer.writeUtf(data.kind);
                buffer.writeVarInt(data.parts.size());
                for (PotionContents part : data.parts) PotionContents.STREAM_CODEC.encode(buffer, part);
                buffer.writeUtf(data.attachment);
                buffer.writeVarInt(data.doses);
            }, buffer -> {
                String kind = buffer.readUtf();
                int size = buffer.readVarInt();
                if (size < 1 || size > 3) throw new IllegalArgumentException("Invalid brew effect count");
                List<PotionContents> parts = new ArrayList<>(size);
                for (int i = 0; i < size; i++) parts.add(PotionContents.STREAM_CODEC.decode(buffer));
                return new AdvancedBrewData(kind, parts, buffer.readUtf(), buffer.readVarInt());
            });

    public AdvancedBrewData {
        parts = List.copyOf(parts);
        int required = switch (kind) {
            case "refined" -> 1;
            case "compound" -> 2;
            case "vessel" -> 3;
            default -> throw new IllegalArgumentException("Unknown brew kind");
        };
        if (parts.size() != required || doses < 0 || doses > 3 || (!kind.equals("vessel") && doses != 0))
            throw new IllegalArgumentException("Invalid brew data");
        if (kind.equals("vessel") && !attachment.isEmpty()
                || kind.equals("compound") && !(attachment.isEmpty() || attachment.equals("ferric_poise"))
                || kind.equals("refined") && !(attachment.equals("ferric_poise")
                    || attachment.equals("nerve_surge") || attachment.equals("fervent_reprisal")))
            throw new IllegalArgumentException("Unsupported brew attachment");
        java.util.Set<Object> effects = new java.util.HashSet<>();
        for (PotionContents part : parts) {
            if (part == null) throw new IllegalArgumentException("Missing brew effect");
            if (kind.equals("vessel") && (part.potion().isEmpty() || !part.customEffects().isEmpty()))
                throw new IllegalArgumentException("Unregistered vessel ingredient");
            int count = 0;
            for (MobEffectInstance effect : part.getAllEffects()) {
                if (++count > 1 || effect.getDuration() <= 0 || effect.getEffect().value().isInstantenous()
                        || !effects.add(effect.getEffect()))
                    throw new IllegalArgumentException("Invalid or duplicate brew effect");
            }
            if (count != 1) throw new IllegalArgumentException("Missing brew effect");
        }
    }

    public AdvancedBrewData withDoses(int count) {
        return new AdvancedBrewData(kind, parts, attachment, count);
    }

    public boolean matchesOutputContents(PotionContents contents) {
        if (contents == null) return false;
        List<MobEffectInstance> actual = new ArrayList<>();
        for (MobEffectInstance effect : contents.getAllEffects()) actual.add(effect);
        if (actual.size() != parts.size()) return false;
        for (int i = 0; i < parts.size(); i++) {
            MobEffectInstance expected = parts.get(i).getAllEffects().iterator().next();
            MobEffectInstance effect = actual.get(i);
            if (!expected.getEffect().equals(effect.getEffect())
                    || expected.getAmplifier() != effect.getAmplifier()
                    || expected.getDuration() != effect.getDuration()) return false;
        }
        return true;
    }
}
