package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.unstained.CleansingHemolymphItem;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.*;

public final class BloodSampleData {
    public static boolean examinable(ItemStack stack) {
        return stack.getItem() instanceof CleansingHemolymphItem
                || ConsecratedSyringeItem.getSaintType(stack) != null
                || com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.readable(stack)
                || entityType(stack) != null;
    }
    private BloodSampleData() {}
    public record Profile(ResourceLocation sourceEntityId, List<EnumBloodTendency> tendencies,
                          List<ResourceLocation> properties, boolean identified) {
        public Profile {
            tendencies = tendencies.stream().distinct().sorted().toList();
            properties = properties.stream().distinct().sorted(Comparator.comparing(ResourceLocation::toString)).toList();
        }
    }
    public static boolean isFilled(ItemStack stack) {
        var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return stack.getItem() instanceof CleansingHemolymphItem
                || ConsecratedSyringeItem.getSaintType(stack) != null
                || com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.is(stack)
                || data.contains(BloodVialItem.TAG_ENTITY_TYPE) || data.getBoolean(BloodVialItem.TAG_STATE);
    }
    public static boolean isSpecimenVessel(ItemStack stack) {
        return stack.getItem() instanceof BloodVialItem
                || stack.getItem() instanceof CleansingHemolymphItem
                || stack.getItem() instanceof ConsecratedSyringeItem
                || stack.is(ItemInit.ahaematic_colloid.get());
    }
    /** A complete source-bearing vial; resolution may fail when its source mod is absent. */
    public static boolean isStorableSample(ItemStack stack) {
        if (!isSpecimenVessel(stack) || (sourceId(stack) == null && !com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.is(stack))) return false;
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return !tag.contains(BloodVialItem.TAG_STATE) || tag.getBoolean(BloodVialItem.TAG_STATE);
    }
    public static String rawSource(ItemStack stack) {
        if (stack.getItem() instanceof CleansingHemolymphItem) return "hemomancy:cleansing_hemolymph";
        var saint = ConsecratedSyringeItem.getSaintType(stack);
        if (saint != null) return "hemomancy:saint/" + saint.name().toLowerCase(Locale.ROOT);
        var ahaematic = com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.get(stack);
        if (ahaematic != null) return ahaematic.source();
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(BloodVialItem.TAG_ENTITY_TYPE);
    }
    public static ResourceLocation sourceId(ItemStack stack) {
        String raw = rawSource(stack);
        return raw.contains(":") ? ResourceLocation.tryParse(raw) : null;
    }
    public static EntityType<?> entityType(ItemStack stack) {
        if (stack.getItem() instanceof CleansingHemolymphItem
                || stack.getItem() instanceof ConsecratedSyringeItem
                || com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.is(stack)) return null;
        var id = sourceId(stack);
        return id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
    }
    public static ItemStack emptyVessel(ItemStack sample) {
        var empty = sample.is(ItemInit.ahaematic_colloid.get()) ? new ItemStack(ItemInit.bloody_vial.get()) : sample.copyWithCount(1);
        var tag = sample.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(BloodVialItem.TAG_ENTITY_TYPE);
        tag.remove(BloodVialItem.TAG_STATE);
        tag.remove(com.vincenthuto.hemomancy.common.succession.SuccessionSamples.KEY);
        if (tag.isEmpty()) empty.remove(DataComponents.CUSTOM_DATA);
        else empty.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        empty.remove(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get());
        empty.remove(DataComponentInit.AHAEMATIC_SAMPLE.get());
        return empty;
    }
    public static boolean identified(ItemStack stack) {
        return stack.getOrDefault(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get(), false);
    }
    /** Call from the server-side examination transaction only. */
    public static boolean identify(ItemStack stack) {
        if (!isSpecimenVessel(stack) || !examinable(stack)) return false;
        stack.set(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get(), true);
        return true;
    }
    public static Profile profile(ItemStack stack, boolean clientSide) {
        var saint = ConsecratedSyringeItem.getSaintType(stack);
        if (saint != null) return new Profile(sourceId(stack),
                List.of(saint.getPrimaryTendency(), saint.getSecondaryTendency()), List.of(), identified(stack));
        if (stack.getItem() instanceof CleansingHemolymphItem) return new Profile(sourceId(stack), List.of(),
                List.of(ResourceLocation.fromNamespaceAndPath("hemomancy", "blood_properties/aquatic")), identified(stack));
        var definition = BloodProfileData.profile(entityType(stack), clientSide);
        return new Profile(sourceId(stack), definition.tendencies(), definition.properties(), identified(stack));
    }
}
