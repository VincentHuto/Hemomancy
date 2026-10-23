package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Whispers a player has not listened to yet, oldest first. Each entry keeps the shape it had as a root key:
 * {@code id} (UUID), {@code tree} (the dialogue tree's network bytes) and {@code decision}. {@link PendingWhispers}
 * reads and edits it.
 */
public class PendingWhisperInbox implements INBTSerializable<CompoundTag> {
	private static final String ENTRIES = "Entries";

	private ListTag entries = new ListTag();

	/** The entries themselves, edited in place. */
	ListTag entries() {
		return entries;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.put(ENTRIES, entries.copy());
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		entries = tag.getList(ENTRIES, Tag.TAG_COMPOUND).copy();
	}
}
