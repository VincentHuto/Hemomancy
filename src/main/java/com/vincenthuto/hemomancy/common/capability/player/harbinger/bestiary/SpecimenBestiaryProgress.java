package com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary;

import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class SpecimenBestiaryProgress implements INBTSerializable<CompoundTag> {
	private static final String RECORDED_SPECIMENS_KEY = "RecordedSpecimens";
	private static final String SURRENDERED_SPECIMENS_KEY = "SurrenderedSpecimens";
	private static final String RECORDED_MORPHLING_LAYERS_KEY = "RecordedMorphlingLayers";

	private final SpecimenBestiaryState state = new SpecimenBestiaryState();

	public boolean recordSpecimen(ResourceLocation id) {
		return id != null && state.recordSpecimen(id.toString());
	}

	public boolean recordSpecimen(String id) {
		return state.recordSpecimen(id);
	}

	public boolean hasRecordedSpecimen(ResourceLocation id) {
		return id != null && state.hasRecordedSpecimen(id.toString());
	}

	public boolean hasRecordedSpecimen(String id) {
		return state.hasRecordedSpecimen(id);
	}

	public int recordedSpecimenCount() {
		return state.recordedSpecimenCount();
	}

	public Set<String> recordedSpecimens() {
		return state.recordedSpecimens();
	}

	public boolean surrenderSpecimen(ResourceLocation id) {
		return id != null && state.surrenderSpecimen(id.toString());
	}

	public boolean surrenderSpecimen(String id) {
		return state.surrenderSpecimen(id);
	}

	public boolean hasSurrenderedSpecimen(ResourceLocation id) {
		return id != null && state.hasSurrenderedSpecimen(id.toString());
	}

	public boolean hasSurrenderedSpecimen(String id) {
		return state.hasSurrenderedSpecimen(id);
	}

	public int surrenderedSpecimenCount() {
		return state.surrenderedSpecimenCount();
	}

	public Set<String> surrenderedSpecimens() {
		return state.surrenderedSpecimens();
	}

	public boolean recordMorphlingLayer(MorphlingPolypLayer layer) {
		return state.recordMorphlingLayer(layer);
	}

	public boolean hasRecordedMorphlingLayer(MorphlingPolypLayer layer) {
		return state.hasRecordedMorphlingLayer(layer);
	}

	public int recordedMorphlingLayerCount() {
		return state.recordedMorphlingLayerCount();
	}

	public Set<String> recordedMorphlingLayers() {
		return state.recordedMorphlingLayers();
	}

	public void replaceFromSync(Collection<String> recordedSpecimens, Collection<String> surrenderedSpecimens,
			Collection<String> recordedMorphlingLayers) {
		state.replaceFromSync(recordedSpecimens, surrenderedSpecimens, recordedMorphlingLayers);
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.put(RECORDED_SPECIMENS_KEY, writeStringList(state.recordedSpecimens()));
		tag.put(SURRENDERED_SPECIMENS_KEY, writeStringList(state.surrenderedSpecimens()));
		tag.put(RECORDED_MORPHLING_LAYERS_KEY, writeStringList(state.recordedMorphlingLayers()));
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		state.clear();
		readStringList(nbt, RECORDED_SPECIMENS_KEY, state::recordSpecimen);
		readStringList(nbt, SURRENDERED_SPECIMENS_KEY, state::surrenderSpecimen);
		readStringList(nbt, RECORDED_MORPHLING_LAYERS_KEY, this::recordMorphlingLayerByName);
	}

	public String serializeForTest() {
		return state.serializeForTest();
	}

	public void deserializeForTest(String encoded) {
		state.deserializeForTest(encoded);
	}

	private void recordMorphlingLayerByName(String layerName) {
		for (MorphlingPolypLayer layer : MorphlingPolypLayer.values()) {
			if (layer.serializedName().equals(layerName)) {
				state.recordMorphlingLayer(layer);
				return;
			}
		}
	}

	private static ListTag writeStringList(Collection<String> values) {
		ListTag list = new ListTag();
		for (String value : new TreeSet<>(values)) {
			list.add(StringTag.valueOf(value));
		}
		return list;
	}

	private static void readStringList(CompoundTag tag, String key, java.util.function.Consumer<String> consumer) {
		if (!tag.contains(key, Tag.TAG_LIST)) {
			return;
		}
		ListTag list = tag.getList(key, Tag.TAG_STRING);
		for (int i = 0; i < list.size(); i++) {
			consumer.accept(list.getString(i));
		}
	}
}
