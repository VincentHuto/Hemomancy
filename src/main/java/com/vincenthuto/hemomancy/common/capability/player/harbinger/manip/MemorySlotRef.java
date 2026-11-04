package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemory;
import net.minecraft.nbt.CompoundTag;

import java.util.Locale;
import java.util.Optional;

public record MemorySlotRef(MemoryEntryKind kind, String id) {
    private static final String THELEMIC_PREFIX = "thelemic_memory:";

    public MemorySlotRef {
        kind = kind == null ? MemoryEntryKind.MANIPULATION : kind;
        id = id == null ? "" : id.trim();
    }

    public static MemorySlotRef manipulation(String name) {
        return new MemorySlotRef(MemoryEntryKind.MANIPULATION, name);
    }

    public static MemorySlotRef thelemicMemory(ThelemicMemory memory) {
        return new MemorySlotRef(MemoryEntryKind.THELEMIC_MEMORY, memory.id());
    }

    public static MemorySlotRef fromStorageKey(String key) {
        String value = key == null ? "" : key.trim();
        if (value.startsWith(THELEMIC_PREFIX)) {
            return new MemorySlotRef(MemoryEntryKind.THELEMIC_MEMORY, value.substring(THELEMIC_PREFIX.length()));
        }
        return manipulation(value);
    }

    public String storageKey() {
        return kind == MemoryEntryKind.THELEMIC_MEMORY ? THELEMIC_PREFIX + id : id;
    }

    public HematicMemoryExpression expression() {
        return kind == MemoryEntryKind.THELEMIC_MEMORY
                ? HematicMemoryExpression.THELEMIC
                : HematicMemoryExpression.NOETIC;
    }

    public Optional<ThelemicMemory> thelemicMemory() {
        return kind == MemoryEntryKind.THELEMIC_MEMORY ? ThelemicMemory.byId(id) : Optional.empty();
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Kind", kind.name().toLowerCase(Locale.ROOT));
        tag.putString("Id", id);
        return tag;
    }

    public static Optional<MemorySlotRef> fromTag(CompoundTag tag) {
        if (tag == null || !tag.contains("Id") || tag.getString("Id").isBlank()) return Optional.empty();
        MemoryEntryKind kind;
        try {
            kind = MemoryEntryKind.valueOf(tag.getString("Kind").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            kind = MemoryEntryKind.MANIPULATION;
        }
        return Optional.of(new MemorySlotRef(kind, tag.getString("Id")));
    }
}
