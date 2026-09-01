package com.vincenthuto.hemomancy.common.capability.player.harbinger.rite;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;

/**
 * Personal, copy-on-death notebook of correctly traced Ichorian Sigil nodes.
 * Incorrect guesses are intentionally excluded: they cost blood in the rite,
 * but never poison the learned canonical shape.
 */
public final class IchorianKnowledge implements INBTSerializable<CompoundTag> {
	private final Map<ResourceLocation, BitSet> discoveredNodes = new TreeMap<>();
	private final Set<ResourceLocation> knownSigils = new TreeSet<>();

	/**
	 * @return true only when this addition completes the sigil for the first time.
	 */
	public boolean recordNode(ResourceLocation sigil, int nodeIndex, int totalNodes) {
		if (sigil == null || totalNodes <= 0 || nodeIndex < 0 || nodeIndex >= totalNodes) return false;
		boolean wasKnown = isKnown(sigil);
		BitSet nodes = discoveredNodes.computeIfAbsent(sigil, ignored -> new BitSet(totalNodes));
		nodes.set(nodeIndex);
		if (nodes.nextClearBit(0) >= totalNodes) knownSigils.add(sigil);
		return !wasKnown && isKnown(sigil);
	}

	public boolean isKnown(ResourceLocation sigil) {
		return knownSigils.contains(sigil);
	}

	public int discoveredNodeCount(ResourceLocation sigil) {
		BitSet nodes = discoveredNodes.get(sigil);
		return nodes == null ? 0 : nodes.cardinality();
	}

	public boolean hasDiscoveredNode(ResourceLocation sigil, int nodeIndex) {
		BitSet nodes = discoveredNodes.get(sigil);
		return nodes != null && nodeIndex >= 0 && nodes.get(nodeIndex);
	}

	public double discoveryProgress(ResourceLocation sigil, int totalNodes) {
		return totalNodes <= 0 ? 0.0D : Math.min(1.0D, discoveredNodeCount(sigil) / (double) totalNodes);
	}

	public Set<ResourceLocation> knownSigils() {
		return Set.copyOf(knownSigils);
	}

	public Map<ResourceLocation, BitSet> partialKnowledge() {
		Map<ResourceLocation, BitSet> copy = new TreeMap<>();
		discoveredNodes.forEach((id, nodes) -> copy.put(id, (BitSet) nodes.clone()));
		return Map.copyOf(copy);
	}

	public void replaceFrom(Map<ResourceLocation, BitSet> partial, Set<ResourceLocation> known) {
		discoveredNodes.clear();
		knownSigils.clear();
		partial.forEach((id, nodes) -> discoveredNodes.put(id, (BitSet) nodes.clone()));
		knownSigils.addAll(known);
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		ListTag entries = new ListTag();
		discoveredNodes.forEach((id, nodes) -> {
			CompoundTag entry = new CompoundTag();
			entry.putString("Id", id.toString());
			entry.putLongArray("Nodes", nodes.toLongArray());
			entry.putBoolean("Known", knownSigils.contains(id));
			entries.add(entry);
		});
		tag.put("Sigils", entries);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		discoveredNodes.clear();
		knownSigils.clear();
		ListTag entries = tag.getList("Sigils", Tag.TAG_COMPOUND);
		for (int i = 0; i < entries.size(); i++) {
			CompoundTag entry = entries.getCompound(i);
			ResourceLocation id = ResourceLocation.tryParse(entry.getString("Id"));
			if (id == null) continue;
			discoveredNodes.put(id, BitSet.valueOf(entry.getLongArray("Nodes")));
			if (entry.getBoolean("Known")) knownSigils.add(id);
		}
	}
}
