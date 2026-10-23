package com.vincenthuto.hemomancy.common.enchanting;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Each enchantment's tendencies for the Enzymatic Scriptorium, from {@code enchantment_affinity/<ns>/<path>.json}. */
public final class ScriptoriumAffinities extends JsonResourceReloadListener<ScriptoriumAffinities.AffinityFile> {
    private static final Codec<AffinityFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("enchantment").forGetter(AffinityFile::enchantment),
            Codec.STRING.fieldOf("primary").forGetter(AffinityFile::primary),
            Codec.STRING.optionalFieldOf("secondary").forGetter(AffinityFile::secondary)).apply(instance, AffinityFile::new));
    private static volatile Map<ResourceLocation, Affinity> current = Map.of();

    public record Affinity(EnumBloodTendency primary, EnumBloodTendency secondary) {}

    record AffinityFile(ResourceLocation enchantment, String primary, Optional<String> secondary) {}

    public ScriptoriumAffinities() {
        super("enchantment_affinity", "Scriptorium affinity", LogManager.getLogger(), ScriptoriumAffinities::parse,
                ScriptoriumAffinities::publish);
    }

    public static Affinity get(ResourceLocation enchantment) { return current.get(enchantment); }

    private static AffinityFile parse(ResourceLocation id, JsonObject root) {
        AffinityFile file = decoding(CODEC).apply(id, root);
        if (!id.getPath().equals(file.enchantment().getNamespace() + "/" + file.enchantment().getPath()))
            throw new IllegalArgumentException("Affinity path does not match enchantment id");
        tendency(file.primary());
        file.secondary().ifPresent(ScriptoriumAffinities::tendency);
        return file;
    }

    private static void publish(Map<ResourceLocation, AffinityFile> loaded) {
        Map<ResourceLocation, Affinity> byEnchantment = new HashMap<>();
        loaded.values().forEach(file -> byEnchantment.put(file.enchantment(), new Affinity(tendency(file.primary()),
                file.secondary().map(ScriptoriumAffinities::tendency).orElse(null))));
        current = Map.copyOf(byEnchantment);
    }

    private static EnumBloodTendency tendency(String id) {
        return EnumBloodTendency.valueOf(ResourceLocation.parse(id).getPath().toUpperCase(Locale.ROOT));
    }
}
