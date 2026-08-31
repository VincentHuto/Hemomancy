package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;

import java.util.List;

public final class MemoryEquipRules {
    public enum AutoEquipResult {
        ADDED,
        REPLACED_SECTION,
        ALREADY_EQUIPPED,
        FULL
    }

    private MemoryEquipRules() {}

    public static MuscleMemory nextPreparedInSection(List<MuscleMemory> prepared, MuscleMemory current,
            EnumVeinSections section) {
        List<MuscleMemory> choices = prepared.stream().filter(memory -> memory.section() == section).toList();
        int nextIndex = current == null ? 0 : choices.indexOf(current) + 1;
        return nextIndex < choices.size() ? choices.get(nextIndex) : null;
    }

    public static AutoEquipResult autoEquipMuscleMemory(List<String> equippedKeys, MuscleMemory memory,
            int maxSlots) {
        ManipulationEquipHelper.normalizeEquippedNames(equippedKeys);
        String newKey = MemorySlotRef.muscleMemory(memory).storageKey();
        if (equippedKeys.contains(newKey)) return AutoEquipResult.ALREADY_EQUIPPED;

        for (int index = 0; index < equippedKeys.size(); index++) {
            MemorySlotRef existing = MemorySlotRef.fromStorageKey(equippedKeys.get(index));
            if (existing.muscleMemory().filter(value -> value.section() == memory.section()).isPresent()) {
                equippedKeys.set(index, newKey);
                return AutoEquipResult.REPLACED_SECTION;
            }
        }

        if (ManipulationEquipHelper.countNormalEquippedNames(equippedKeys) >= Math.max(0, maxSlots)) {
            return AutoEquipResult.FULL;
        }
        equippedKeys.add(newKey);
        return AutoEquipResult.ADDED;
    }
}
