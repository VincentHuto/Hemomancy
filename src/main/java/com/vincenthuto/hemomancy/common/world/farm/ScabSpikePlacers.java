package com.vincenthuto.hemomancy.common.world.farm;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Who placed each hanging scab spike in a chunk, keyed by the spike's root (CQ-3, NQ-01-15). A spike drips only from a
 * tank its placer may draw; a spike with no placer (grown by a drip, raised from the floor, made by the world) drips
 * only from tanks nobody owns. The {@code hemomancy:scab_spike_placers} chunk attachment, read through
 * {@code HemoCapabilityAccess}.
 */
public final class ScabSpikePlacers implements INBTSerializable<CompoundTag> {
	private final Long2ObjectOpenHashMap<UUID> placers = new Long2ObjectOpenHashMap<>();

	@Nullable
	public UUID placer(BlockPos root) {
		return placers.get(root.asLong());
	}

	public void record(BlockPos root, UUID placer) {
		placers.put(root.asLong(), placer);
	}

	public boolean forget(BlockPos root) {
		return placers.remove(root.asLong()) != null;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		ListTag list = new ListTag();
		for (var entry : placers.long2ObjectEntrySet()) {
			CompoundTag spike = new CompoundTag();
			spike.putLong("Root", entry.getLongKey());
			spike.putUUID("Placer", entry.getValue());
			list.add(spike);
		}
		tag.put("Spikes", list);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		placers.clear();
		for (Tag element : tag.getList("Spikes", Tag.TAG_COMPOUND)) {
			CompoundTag spike = (CompoundTag) element;
			if (spike.hasUUID("Placer")) placers.put(spike.getLong("Root"), spike.getUUID("Placer"));
		}
	}
}
