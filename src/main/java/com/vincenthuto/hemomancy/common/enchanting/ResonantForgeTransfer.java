package com.vincenthuto.hemomancy.common.enchanting;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

public final class ResonantForgeTransfer {
    private ResonantForgeTransfer() {}

    public static ResonantPattern read(ItemStack stack) {
        List<ResonantPattern.Entry> enchantments = new ArrayList<>();
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            entry.getKey().unwrapKey().ifPresent(key -> enchantments.add(
                    new ResonantPattern.Entry(key.location().toString(), entry.getIntValue())));
        }
        enchantments.sort(java.util.Comparator.comparing(ResonantPattern.Entry::enchantment));
        ScriptoriumProvenance provenance = stack.get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        return new ResonantPattern(enchantments,
                provenance == null ? "" : provenance.curse(),
                provenance == null ? 0 : provenance.severity(),
                provenance == null ? 0 : provenance.excessLoad(), false);
    }

    public static Capture capture(ItemStack source, HolderLookup.Provider registries, String selection, boolean master) {
        if (source.is(Items.BOOK) || source.is(Items.ENCHANTED_BOOK))
            return Capture.failure(Failure.UNSUPPORTED_ITEM);
        ResonantPattern complete = read(source);
        ResonantPattern selected = selection == null || selection.isBlank()
                ? complete : ResonantForgeRules.select(complete, selection);
        if (selected == null || selected.isEmpty()) return Capture.failure(Failure.EMPTY_PATTERN);
        if (selection != null && !selection.isBlank() && !ResonantForgeRules.CURSE_SELECTION.equals(selection))
            selected = new ResonantPattern(selected.enchantments(), "", 0,
                    recordedExcessLoad(selected, registries), selected.master());
        if (master) selected = selected.asMaster();

        ItemStack result = source.copy();
        ResonantPattern recorded = selected;
        EnchantmentHelper.updateEnchantments(result, mutable -> {
            if (selection == null || selection.isBlank()) mutable.removeIf(holder -> true);
            else if (!ResonantForgeRules.CURSE_SELECTION.equals(selection))
                mutable.removeIf(holder -> holder.unwrapKey().map(key -> key.location().toString().equals(selection)).orElse(false));
        });

        ScriptoriumProvenance old = result.get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        String remainingCurse = old == null ? "" : old.curse();
        int remainingSeverity = old == null ? 0 : old.severity();
        if (selection == null || selection.isBlank() || ResonantForgeRules.CURSE_SELECTION.equals(selection)) {
            remainingCurse = "";
            remainingSeverity = 0;
        }
        writeProvenance(result, registries, remainingCurse, remainingSeverity);
        return new Capture(result, recorded, Failure.NONE);
    }

    public static Application apply(ItemStack target, ResonantPattern pattern, HolderLookup.Provider registries) {
        if (target.is(Items.BOOK) || target.is(Items.ENCHANTED_BOOK))
            return Application.failure(Failure.UNSUPPORTED_ITEM);
        if (pattern == null || pattern.isEmpty()) return Application.failure(Failure.EMPTY_PATTERN);
        HolderLookup.RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        List<Resolved> resolved = new ArrayList<>();
        for (ResonantPattern.Entry entry : pattern.enchantments()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.enchantment());
            if (id == null) return Application.failure(Failure.UNKNOWN_ENCHANTMENT);
            var holder = lookup.get(ResourceKey.create(Registries.ENCHANTMENT, id));
            if (holder.isEmpty()) return Application.failure(Failure.UNKNOWN_ENCHANTMENT);
            if (!target.supportsEnchantment(holder.get())) return Application.failure(Failure.UNSUPPORTED_ENCHANTMENT);
            resolved.add(new Resolved(holder.get(), entry.level()));
        }

        List<Holder<Enchantment>> all = new ArrayList<>(EnchantmentHelper.getEnchantmentsForCrafting(target).keySet());
        for (Resolved addition : resolved) {
            for (Holder<Enchantment> present : all) {
                if (!present.equals(addition.enchantment) && !Enchantment.areCompatible(present, addition.enchantment))
                    return Application.failure(Failure.INCOMPATIBLE_ENCHANTMENTS);
            }
            all.add(addition.enchantment);
        }

        ScriptoriumProvenance old = target.get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        if (old != null && !old.curse().isBlank() && !pattern.curse().isBlank()
                && !old.curse().equals(pattern.curse()))
            return Application.failure(Failure.DIFFERENT_SCRIPTORIUM_CURSE);
        if (!pattern.curse().isBlank() && !pattern.curse().equals(ScriptoriumCurseRules.applicableCurse(target)))
            return Application.failure(Failure.UNSUPPORTED_SCRIPTORIUM_CURSE);

        ItemStack result = target.copy();
        boolean changed = false;
        for (Resolved addition : resolved) {
            int before = EnchantmentHelper.getEnchantmentsForCrafting(result).getLevel(addition.enchantment);
            if (addition.level > before) changed = true;
            result.enchant(addition.enchantment, Math.max(before, addition.level));
        }
        String curse = old == null || old.curse().isBlank() ? pattern.curse() : old.curse();
        int severity = Math.max(old == null ? 0 : old.severity(), pattern.curseSeverity());
        if (!curse.isBlank() && (old == null || !curse.equals(old.curse()) || severity > old.severity())) changed = true;
        if (!changed) return Application.failure(Failure.NO_CHANGE);
        writeProvenance(result, registries, curse, severity);
        return new Application(result, Failure.NONE);
    }

    private static void writeProvenance(ItemStack stack, HolderLookup.Provider registries, String curse, int severity) {
        int excess = excessLoad(stack, registries);
        if (curse.isBlank() && excess == 0) stack.remove(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        else stack.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(),
                new ScriptoriumProvenance(curse, curse.isBlank() ? 0 : severity, excess));
    }

    private static int excessLoad(ItemStack stack, HolderLookup.Provider registries) {
        int excess = 0;
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet())
            excess += Math.max(0, entry.getIntValue() - entry.getKey().value().getMaxLevel());
        return excess;
    }

    private static int recordedExcessLoad(ResonantPattern pattern, HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        int excess = 0;
        for (ResonantPattern.Entry entry : pattern.enchantments()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.enchantment());
            if (id == null) continue;
            var enchantment = lookup.get(ResourceKey.create(Registries.ENCHANTMENT, id));
            if (enchantment.isPresent()) excess += Math.max(0, entry.level() - enchantment.get().value().getMaxLevel());
        }
        return excess;
    }

    private record Resolved(Holder<Enchantment> enchantment, int level) {}

    public enum Failure {
        NONE,
        UNSUPPORTED_ITEM,
        EMPTY_PATTERN,
        UNKNOWN_ENCHANTMENT,
        UNSUPPORTED_ENCHANTMENT,
        INCOMPATIBLE_ENCHANTMENTS,
        DIFFERENT_SCRIPTORIUM_CURSE,
        UNSUPPORTED_SCRIPTORIUM_CURSE,
        NO_CHANGE
    }

    public record Capture(ItemStack equipment, ResonantPattern pattern, Failure failure) {
        static Capture failure(Failure failure) { return new Capture(ItemStack.EMPTY, ResonantPattern.EMPTY, failure); }
        public boolean success() { return failure == Failure.NONE; }
    }

    public record Application(ItemStack equipment, Failure failure) {
        static Application failure(Failure failure) { return new Application(ItemStack.EMPTY, failure); }
        public boolean success() { return failure == Failure.NONE; }
    }
}
