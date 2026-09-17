package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.*;

public final class BloodSampleData {
    private BloodSampleData() {}
    private static final Map<EnumBloodTendency, TagKey<EntityType<?>>> TENDENCY_TAGS = Map.of(
        EnumBloodTendency.ANIMUS, EntityInit.VIVACIOUS_TAG, EnumBloodTendency.FLAMMEUS, EntityInit.FERVENT_TAG,
        EnumBloodTendency.DUCTILIS, EntityInit.NEUROTIC_TAG, EnumBloodTendency.LUX, EntityInit.INCANDESCENT_TAG,
        EnumBloodTendency.MORTEM, EntityInit.RUINOUS_TAG, EnumBloodTendency.CONGEATIO, EntityInit.FRIGID_TAG,
        EnumBloodTendency.FERRIC, EntityInit.FERRIC_TAG, EnumBloodTendency.TENEBRIS, EntityInit.UMBRAL_TAG);

    public record Profile(ResourceLocation sourceEntityId, List<EnumBloodTendency> tendencies,
                          List<ResourceLocation> properties, boolean identified) {
        public Profile {
            tendencies = tendencies.stream().distinct().sorted().toList();
            properties = properties.stream().distinct().sorted(Comparator.comparing(ResourceLocation::toString)).toList();
        }
    }
    public static boolean isFilled(ItemStack stack) {
        var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.contains(BloodVialItem.TAG_ENTITY_TYPE) || data.getBoolean(BloodVialItem.TAG_STATE);
    }
    /** A complete source-bearing vial; resolution may fail when its source mod is absent. */
    public static boolean isStorableSample(ItemStack stack) {
        if (!(stack.getItem() instanceof BloodVialItem) || sourceId(stack) == null) return false;
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return !tag.contains(BloodVialItem.TAG_STATE) || tag.getBoolean(BloodVialItem.TAG_STATE);
    }
    public static String rawSource(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(BloodVialItem.TAG_ENTITY_TYPE);
    }
    public static ResourceLocation sourceId(ItemStack stack) {
        String raw = rawSource(stack);
        return raw.contains(":") ? ResourceLocation.tryParse(raw) : null;
    }
    public static EntityType<?> entityType(ItemStack stack) {
        var id = sourceId(stack);
        return id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
    }
    public static ItemStack emptyVessel(ItemStack sample) {
        var empty = sample.copyWithCount(1);
        var tag = empty.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(BloodVialItem.TAG_ENTITY_TYPE);
        tag.remove(BloodVialItem.TAG_STATE);
        if (tag.isEmpty()) empty.remove(DataComponents.CUSTOM_DATA);
        else empty.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        empty.remove(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get());
        return empty;
    }
    public static boolean identified(ItemStack stack) {
        return stack.getOrDefault(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get(), false);
    }
    /** Call from the server-side examination transaction only. */
    public static boolean identify(ItemStack stack) {
        if (!(stack.getItem() instanceof BloodVialItem) || entityType(stack) == null) return false;
        stack.set(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get(), true);
        return true;
    }
    public static Profile profile(ItemStack stack, Collection<ResourceLocation> supportedProperties) {
        var type = entityType(stack);
        var tendencies = new ArrayList<EnumBloodTendency>();
        var properties = new TreeSet<ResourceLocation>(Comparator.comparing(ResourceLocation::toString));
        if (type != null) {
            // Entity tags are synchronized by vanilla, independently of injection responses.
            type.builtInRegistryHolder().tags().map(TagKey::location)
                    .filter(id -> id.getPath().startsWith("blood_properties/"))
                    .forEach(properties::add);
            for (var tendency : EnumBloodTendency.values()) if (type.is(TENDENCY_TAGS.get(tendency))) tendencies.add(tendency);
            for (var property : supportedProperties) if (type.is(TagKey.create(Registries.ENTITY_TYPE, property))) properties.add(property);
            if (type.is(EntityInit.FUNGAL_TAG)) properties.add(EntityInit.FUNGAL_TAG.location());
        }
        return new Profile(sourceId(stack), tendencies, new ArrayList<>(properties), identified(stack));
    }
}
