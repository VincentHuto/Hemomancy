package com.vincenthuto.hemomancy.common.item.component;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import java.util.List;

public record FieldCaseContents(List<SpecimenStack> entries) {
    public static final FieldCaseContents EMPTY = new FieldCaseContents(java.util.Collections.nCopies(9, SpecimenStack.EMPTY));
    public static final Codec<FieldCaseContents> CODEC = SpecimenStack.CODEC.listOf(9, 9).xmap(FieldCaseContents::new, FieldCaseContents::entries);
    public static final StreamCodec<RegistryFriendlyByteBuf, FieldCaseContents> STREAM_CODEC = StreamCodec.of(
            (b, data) -> data.entries.forEach(s -> SpecimenStack.STREAM_CODEC.encode(b, s)),
            b -> {
                var entries = new java.util.ArrayList<SpecimenStack>(9);
                for (int i = 0; i < 9; i++) entries.add(SpecimenStack.STREAM_CODEC.decode(b));
                return new FieldCaseContents(entries);
            });
    public FieldCaseContents {
        if (entries.size() != 9) throw new IllegalArgumentException("Field cases have nine cells");
        entries = List.copyOf(entries);
    }
    public int totalCount() { return entries.stream().mapToInt(SpecimenStack::count).sum(); }
    /** Invalid carried data must refuse placement, never silently truncate its contents. */
    public boolean valid() {
        for (int i = 0; i < 9; i++) {
            var entry = entries.get(i);
            var stack = entry.exemplar();
            if (entry.count() == 0 && stack.isEmpty()) continue;
            if (entry.count() < 1 || entry.count() > 16 || !BloodSampleData.isStorableSample(stack)) return false;
            for (int j = 0; j < i; j++) if (entries.get(j).count() > 0 && entries.get(j).matches(stack)) return false;
        }
        return true;
    }
}
