package com.vincenthuto.hemomancy.common.enchanting;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Each enchantment's tempers for the Enzymatic Scriptorium, from {@code enchantment_affinity/<ns>/<path>.json}. */
public final class ScriptoriumAffinities extends JsonResourceReloadListener<ScriptoriumAffinities.AffinityFile> {
    private static final Codec<AffinityFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("enchantment").forGetter(AffinityFile::enchantment),
            Codec.STRING.fieldOf("primary").forGetter(AffinityFile::primary),
            Codec.STRING.optionalFieldOf("secondary").forGetter(AffinityFile::secondary)).apply(instance, AffinityFile::new));
    private static volatile Map<ResourceLocation, Affinity> current = Map.of();

    public record Affinity(EnumBloodTemper primary, EnumBloodTemper secondary) {}

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
        temper(file.primary());
        file.secondary().ifPresent(ScriptoriumAffinities::temper);
        return file;
    }

    private static void publish(Map<ResourceLocation, AffinityFile> loaded) {
        Map<ResourceLocation, Affinity> byEnchantment = new HashMap<>();
        // Sorted by file id, as listResources was: the last of two files for one enchantment wins, every reload.
        new TreeMap<>(loaded).values().forEach(file -> byEnchantment.put(file.enchantment(), new Affinity(temper(file.primary()),
                file.secondary().map(ScriptoriumAffinities::temper).orElse(null))));
        current = Map.copyOf(byEnchantment);
    }

    private static EnumBloodTemper temper(String id) {
        return EnumBloodTemper.valueOf(ResourceLocation.parse(id).getPath().toUpperCase(Locale.ROOT));
    }
}
