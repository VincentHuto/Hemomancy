package com.vincenthuto.hemomancy.common.enchanting;

import java.util.LinkedHashMap;

public final class ResonantForgeRules {
    public static final String CURSE_SELECTION = "hemomancy:scriptorium_curse";
    public static final int BLOOD_CAPACITY = 5_000;
    public static final int MASTER_SURCHARGE = 1_000;
    public static final int OPERATION_TICKS = 160;
    public static final int MASTER_OPERATION_TICKS = 240;
    public static final int HAMMER_SERVICE_INTERVAL = 64;
    public static final int WHEEL_SERVICE_INTERVAL = 32;
    public static final int HAMMER_REPAIR_BLOOD = 500;
    public static final int SCALPEL_REPAIR_DAMAGE = 8;

    private ResonantForgeRules() {}

    public static int grindingCost(ResonantPattern pattern) {
        return 100 + pattern.recordedLoad() * 25;
    }

    public static int hammeringCost(ResonantPattern pattern) {
        return 200 + pattern.recordedLoad() * 50;
    }

    public static boolean canSelectIndividual(ResonantForgeTier tier) {
        return tier.ordinal() >= ResonantForgeTier.PRECISION.ordinal();
    }

    public static boolean canUseMaster(ResonantForgeTier tier) {
        return tier == ResonantForgeTier.MASTERWORK;
    }

    public static boolean hammerServiceRequired(int completedApplications) {
        return completedApplications >= HAMMER_SERVICE_INTERVAL;
    }

    public static boolean wheelServiceRequired(int completedGrindingOperations) {
        return completedGrindingOperations >= WHEEL_SERVICE_INTERVAL;
    }

    public static boolean isBlankCylinder(boolean ancientRecording, boolean audioRecording, boolean resonantPattern) {
        return !ancientRecording && !audioRecording && !resonantPattern;
    }

    public static ResonantPattern select(ResonantPattern complete, String selection) {
        if (complete == null || selection == null || selection.isBlank()) return complete;
        if (CURSE_SELECTION.equals(selection))
            return new ResonantPattern(java.util.List.of(), complete.curse(), complete.curseSeverity(), 0, complete.master());
        return new ResonantPattern(complete.enchantments().stream()
                .filter(entry -> entry.enchantment().equals(selection)).toList(), "", 0, 0, complete.master());
    }

    public static Merge merge(ResonantPattern existing, ResonantPattern recorded) {
        if (recorded == null || recorded.isEmpty()) return Merge.failure(Failure.EMPTY_PATTERN);
        if (!existing.curse().isBlank() && !recorded.curse().isBlank()
                && !existing.curse().equals(recorded.curse()))
            return Merge.failure(Failure.DIFFERENT_SCRIPTORIUM_CURSE);

        var merged = new LinkedHashMap<String, Integer>();
        existing.enchantments().forEach(entry -> merged.put(entry.enchantment(), entry.level()));
        recorded.enchantments().forEach(entry -> merged.merge(entry.enchantment(), entry.level(), Math::max));
        String curse = existing.curse().isBlank() ? recorded.curse() : existing.curse();
        int severity = Math.max(existing.curseSeverity(), recorded.curseSeverity());
        var result = new ResonantPattern(merged.entrySet().stream()
                .map(entry -> new ResonantPattern.Entry(entry.getKey(), entry.getValue())).toList(),
                curse, severity, Math.max(existing.excessLoad(), recorded.excessLoad()), false);
        if (sameContents(existing, result)) return Merge.failure(Failure.NO_CHANGE);
        return new Merge(result, Failure.NONE);
    }

    private static boolean sameContents(ResonantPattern first, ResonantPattern second) {
        return first.enchantments().equals(second.enchantments())
                && first.curse().equals(second.curse())
                && first.curseSeverity() == second.curseSeverity()
                && first.excessLoad() == second.excessLoad();
    }

    public enum Failure {
        NONE,
        EMPTY_PATTERN,
        DIFFERENT_SCRIPTORIUM_CURSE,
        NO_CHANGE
    }

    public record Merge(ResonantPattern pattern, Failure failure) {
        public static Merge failure(Failure failure) {
            return new Merge(ResonantPattern.EMPTY, failure);
        }

        public boolean changed() {
            return failure == Failure.NONE;
        }
    }
}
