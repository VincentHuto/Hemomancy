package com.vincenthuto.hemomancy.common.antecedent;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import java.util.EnumSet;

public final class AntecedentResearch implements INBTSerializable<CompoundTag> {
    public enum Evidence {
        SAMPLE_ANALYZED, ACOUSTIC_RESPONSE, SEVERED_RECORD_HEARD, SAMPLE_RESPONSE,
        REPLAY_REQUESTED, CONTROLLED_REPLAY, VICAR_RECOGNITION, CROSSED_WOOL_TERMINUS,
        DEGRADED_NETWORK, VIGIL_RECORD_READ, ARCHIVE_RESPONSE, EPILOGUE;
        public String key() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    private final EnumSet<Evidence> evidence = EnumSet.noneOf(Evidence.class);
    private CompoundTag preserved = new CompoundTag();
    public boolean has(Evidence fact) { return evidence.contains(fact); }
    public boolean record(Evidence fact) { return evidence.add(fact); }
    public boolean complete() { return has(Evidence.ARCHIVE_RESPONSE) && has(Evidence.VICAR_RECOGNITION); }
    @Override public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = preserved.copy();
        evidence.forEach(fact -> tag.putBoolean(fact.key(), true));
        return tag;
    }
    @Override public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        preserved = tag.copy();
        evidence.clear();
        for (var fact : Evidence.values()) if (tag.getBoolean(fact.key())) evidence.add(fact);
    }
}
