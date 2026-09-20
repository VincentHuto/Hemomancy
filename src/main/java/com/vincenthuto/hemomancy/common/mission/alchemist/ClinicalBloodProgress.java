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

    private final Set<String> examinedSources = new LinkedHashSet<>();
    private final EnumSet<Lesson> lessons = EnumSet.noneOf(Lesson.class);
    public boolean collected, cabinetCrafted, cabinetInserted, cabinetWithdrawn;
    public boolean hematicIronObtained, artificerMet, mnemonistMet;

    public int sourceCount() { return examinedSources.size(); }
    public boolean recordExamination(String source) {
        if (source == null || !source.contains(":")) return false;
        collected = true;
        return examinedSources.add(source);
    }
    public boolean knows(Lesson lesson) { return lessons.contains(lesson); }
    public boolean learn(Lesson lesson) { return lessons.add(lesson); }
    public boolean canLearn(Lesson lesson, int degree) {
        if (degree < 1 || knows(lesson)) return false;
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
