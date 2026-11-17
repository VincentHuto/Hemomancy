package com.vincenthuto.hemomancy.common.mission.alchemist;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/** Per-player evidence, separate from a vial's transferable identification. */
public final class ClinicalBloodProgress implements INBTSerializable<CompoundTag> {
    public enum Lesson {
        MICROSCOPE("alchemist", "hematic_microscope"),
        INJECTION("alchemist", null),
        CABINET("alchemist", "phlebotomists_cabinet"),
        FIELD_REFERRAL("alchemist", null),
        FIELD_CASE("artificer", "phlebotomists_field_case"),
        ECHO_REFERRAL("alchemist", null),
        CLAIRAUDIOGRAPH("mnemonist", "clairaudiograph");

        public final String teacher;
        public final String recipe;
        Lesson(String teacher, String recipe) { this.teacher = teacher; this.recipe = recipe; }
        public String key() { return name().toLowerCase(java.util.Locale.ROOT); }
        public String event() { return "clinical_lesson_" + key(); }
    }

    /** Creature types (and other sources) this player has examined: what their blood is, for this player only. */
    private final Set<String> examinedSources = new LinkedHashSet<>();
    /**
     * Second-level research (core-loop Track 5): per creature type, the distinct individuals (or a kept creature at
     * distinct Vigor bands) examined, up to {@link #STUDY_INDIVIDUALS}. At that many the type's yield is known.
     */
    private final java.util.Map<String, Set<String>> individuals = new java.util.LinkedHashMap<>();
    public static final int STUDY_INDIVIDUALS = 3;
    /**
     * Creature types this player has met at close quarters (core-loop round two, P6): sampled, led into glass, jarred,
     * bred, or looked at through a vessel's screen. The Living Bestiary lists a met kind with its blood unread.
     */
    private final Set<String> metSources = new LinkedHashSet<>();
    private final EnumSet<Lesson> lessons = EnumSet.noneOf(Lesson.class);
    public boolean collected, cabinetCrafted, cabinetInserted, cabinetWithdrawn;
    public boolean hematicIronObtained, artificerMet, mnemonistMet;

    public int sourceCount() { return examinedSources.size(); }
    public boolean recordExamination(String source) {
        if (source == null || !source.contains(":")) return false;
        collected = true;
        return examinedSources.add(source);
    }
    /** Whether this player has examined {@code source}'s blood; an unexamined creature's blood is unlabelled to them. */
    public boolean knowsSource(String source) { return source != null && examinedSources.contains(source); }
    /** Records one examined individual of {@code source}; true when it was a new one. */
    public boolean recordIndividual(String source, String individual) {
        if (source == null || individual == null || individual.isBlank()) return false;
        var seen = individuals.computeIfAbsent(source, key -> new LinkedHashSet<>());
        return seen.size() < STUDY_INDIVIDUALS && seen.add(individual);
    }
    public int individuals(String source) { var seen = individuals.get(source); return seen == null ? 0 : seen.size(); }
    /** Whether {@code source} has been studied at three individuals or Vigor levels: its yield is known. */
    public boolean studied(String source) { return individuals(source) >= STUDY_INDIVIDUALS; }
    /** Records that this player met a creature of {@code source}; true when it was new. */
    public boolean recordMet(String source) {
        return source != null && source.contains(":") && metSources.add(source);
    }
    public boolean hasMet(String source) { return source != null && metSources.contains(source); }
    public Set<String> metSources() { return java.util.Collections.unmodifiableSet(metSources); }
    public Set<String> examinedSources() { return java.util.Collections.unmodifiableSet(examinedSources); }
    public boolean knows(Lesson lesson) { return lessons.contains(lesson); }
    public boolean learn(Lesson lesson) { return lessons.add(lesson); }
    public boolean canLearn(Lesson lesson, int degree) {
        if (degree < 2 || knows(lesson)) return false;
        return switch (lesson) {
            case MICROSCOPE -> collected;
            case INJECTION -> sourceCount() > 0 && knows(Lesson.MICROSCOPE);
            case CABINET -> sourceCount() >= 3 && knows(Lesson.INJECTION);
            case FIELD_REFERRAL -> degree >= 2 && knows(Lesson.CABINET) && cabinetCrafted
                    && cabinetInserted && cabinetWithdrawn && hematicIronObtained && artificerMet;
            case FIELD_CASE -> degree >= 2 && knows(Lesson.FIELD_REFERRAL) && cabinetCrafted
                    && cabinetInserted && cabinetWithdrawn && hematicIronObtained && artificerMet;
            case ECHO_REFERRAL -> degree >= 2 && sourceCount() >= 3;
            case CLAIRAUDIOGRAPH -> degree >= 3 && sourceCount() >= 3 && mnemonistMet
                    && knows(Lesson.ECHO_REFERRAL);
        };
    }

    @Override public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        var sources = new ListTag();
        examinedSources.forEach(id -> sources.add(StringTag.valueOf(id)));
        tag.put("ExaminedSources", sources);
        var studied = new CompoundTag();
        individuals.forEach((source, seen) -> {
            var list = new ListTag();
            seen.forEach(id -> list.add(StringTag.valueOf(id)));
            studied.put(source, list);
        });
        if (!studied.isEmpty()) tag.put("Individuals", studied);
        if (!metSources.isEmpty()) {
            var met = new ListTag();
            metSources.forEach(id -> met.add(StringTag.valueOf(id)));
            tag.put("Met", met);
        }
        for (Lesson lesson : lessons) tag.putBoolean(lesson.key(), true);
        tag.putBoolean("Collected", collected);
        tag.putBoolean("CabinetCrafted", cabinetCrafted);
        tag.putBoolean("CabinetInserted", cabinetInserted);
        tag.putBoolean("CabinetWithdrawn", cabinetWithdrawn);
        tag.putBoolean("HematicIronObtained", hematicIronObtained);
        tag.putBoolean("ArtificerMet", artificerMet);
        tag.putBoolean("MnemonistMet", mnemonistMet);
        return tag;
    }
    @Override public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        examinedSources.clear();
        var sources = tag.getList("ExaminedSources", 8);
        for (int i = 0; i < sources.size(); i++) recordExamination(sources.getString(i));
        individuals.clear();
        var studied = tag.getCompound("Individuals");
        for (String source : studied.getAllKeys()) {
            var list = studied.getList(source, 8);
            for (int i = 0; i < list.size(); i++) recordIndividual(source, list.getString(i));
        }
        metSources.clear();
        var met = tag.getList("Met", 8);
        for (int i = 0; i < met.size(); i++) recordMet(met.getString(i));
        lessons.clear();
        for (Lesson lesson : Lesson.values()) if (tag.getBoolean(lesson.key())) lessons.add(lesson);
        collected = tag.getBoolean("Collected") || !examinedSources.isEmpty();
        cabinetCrafted = tag.getBoolean("CabinetCrafted");
        cabinetInserted = tag.getBoolean("CabinetInserted");
        cabinetWithdrawn = tag.getBoolean("CabinetWithdrawn");
        hematicIronObtained = tag.getBoolean("HematicIronObtained");
        artificerMet = tag.getBoolean("ArtificerMet");
        mnemonistMet = tag.getBoolean("MnemonistMet");
    }
}
