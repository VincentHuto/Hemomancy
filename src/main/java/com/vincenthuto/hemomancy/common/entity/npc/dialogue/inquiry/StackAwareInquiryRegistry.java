
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.util.SpecimenJarData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Ordered built-in providers for stateful inquiry items. */
public final class StackAwareInquiryRegistry {
    private static final List<StackAwareInquiryProvider> PROVIDERS = List.of(
            StackAwareInquiryRegistry::blueprint,
            StackAwareInquiryRegistry::scarPattern,
            StackAwareInquiryRegistry::specimenJar
    );

    private StackAwareInquiryRegistry() {}

    public static Optional<ResolvedStackInquiry> resolve(String speakerId, ItemStack stack,
            ItemInquiryContext context) {
        for (StackAwareInquiryProvider provider : PROVIDERS) {
            Optional<ResolvedStackInquiry> result = provider.resolve(speakerId, stack, context);
            if (result.isPresent()) return result;
        }
        return Optional.empty();
    }

    private static Optional<ResolvedStackInquiry> blueprint(String speakerId, ItemStack stack,
            ItemInquiryContext context) {
        if (!(stack.getItem() instanceof MnemonicBlueprintItem)
                || !("vicar".equals(speakerId) || "mnemonist".equals(speakerId))) return Optional.empty();
        MnemonicBlueprintTarget target = MnemonicBlueprintItem.getTarget(stack);
        String state;
        String detail;
        if (target == null) {
            state = "blank";
            detail = "Blank plan: no rite or blood structure has been impressed into it.";
        } else {
            state = target.type() == MnemonicBlueprintTarget.Type.CARDINAL_RITE ? "rite" : "structure";
            detail = proper(target.recipeId().getPath()) + " (" + target.recipeId() + ")";
        }
        return Optional.of(new ResolvedStackInquiry(
                "blueprint/" + state + "/" + Integer.toHexString(detail.hashCode()),
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                List.of("hemomancy." + speakerId + ".item_inquiry.mnemonic_blueprint." + state,
                        literal(detail))));
    }

    private static Optional<ResolvedStackInquiry> scarPattern(String speakerId, ItemStack stack,
            ItemInquiryContext context) {
        if (!(stack.getItem() instanceof ItemScarPattern)
                || !("mnemonist".equals(speakerId) || "vicar".equals(speakerId))) return Optional.empty();
        List<ResourceLocation> ids = new ArrayList<>(ItemScarPattern.getScarIds(stack));
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        String state = ids.isEmpty() ? "blank" : ids.size() == 1 ? "template" : "loadout";
        String detail = ids.isEmpty() ? "No scar route is written into this motif."
                : "Written routes: " + ids.stream().map(id -> proper(id.getPath())).reduce((a, b) -> a + ", " + b).orElse("");
        String fingerprint = ids.stream().map(ResourceLocation::toString).reduce((a, b) -> a + ";" + b).orElse("blank");
        return Optional.of(new ResolvedStackInquiry(
                "scar_pattern/" + Integer.toHexString(fingerprint.hashCode()),
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                List.of("hemomancy." + speakerId + ".item_inquiry.scar_pattern." + state, literal(detail))));
    }

    private static Optional<ResolvedStackInquiry> specimenJar(String speakerId, ItemStack stack,
            ItemInquiryContext context) {
        if (!SpecimenJarData.hasSpecimen(stack)
                || !("alchemist".equals(speakerId) || "voyager".equals(speakerId)
                || "votary_wayfarer".equals(speakerId))) return Optional.empty();
        Optional<ResourceLocation> specimen = SpecimenJarData.getSpecimenEntityId(stack);
        if (specimen.isEmpty()) return Optional.empty();
        var tag = SpecimenJarData.getSpecimen(stack);
        String layers = SpecimenJarData.getMorphlingLayers(tag).stream()
                .map(layer -> proper(layer.name().toLowerCase(Locale.ROOT)))
                .reduce((a, b) -> a + ", " + b).orElse("");
        String detail = "Contained specimen: " + proper(specimen.get().getPath());
        if (!layers.isBlank()) detail += ". Recorded layers: " + layers;
        return Optional.of(new ResolvedStackInquiry(
                "specimen/" + specimen.get() + "/" + Integer.toHexString(layers.hashCode()),
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                List.of("hemomancy." + speakerId + ".item_inquiry.specimen_jar", literal(detail))));
    }

    private static String proper(String value) {
        String path = value.contains("/") ? value.substring(value.lastIndexOf('/') + 1) : value;
        String[] words = path.replace('-', '_').split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private static String literal(String text) {
        return "@literal:" + text;
    }

    public record ResolvedStackInquiry(String presentationKey, ResourceLocation itemId, List<String> lines) {}
}
