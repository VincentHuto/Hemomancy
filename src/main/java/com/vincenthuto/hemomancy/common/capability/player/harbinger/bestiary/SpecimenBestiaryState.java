package com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary;

import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class SpecimenBestiaryState {
	private final Set<String> recordedSpecimens = new LinkedHashSet<>();
	private final Set<String> surrenderedSpecimens = new LinkedHashSet<>();
	private final Set<String> recordedMorphlingLayers = new LinkedHashSet<>();

	public boolean recordSpecimen(String id) {
		return addNormalized(recordedSpecimens, id);
	}

	public boolean hasRecordedSpecimen(String id) {
		return containsNormalized(recordedSpecimens, id);
	}

	public int recordedSpecimenCount() {
		return recordedSpecimens.size();
	}

	public Set<String> recordedSpecimens() {
		return Set.copyOf(recordedSpecimens);
	}

	public boolean surrenderSpecimen(String id) {
		return addNormalized(surrenderedSpecimens, id);
	}

	public boolean hasSurrenderedSpecimen(String id) {
		return containsNormalized(surrenderedSpecimens, id);
	}

	public int surrenderedSpecimenCount() {
		return surrenderedSpecimens.size();
	}

	public Set<String> surrenderedSpecimens() {
		return Set.copyOf(surrenderedSpecimens);
	}

	public boolean recordMorphlingLayer(MorphlingPolypLayer layer) {
		return layer != null && addNormalized(recordedMorphlingLayers, layer.serializedName());
	}

	public boolean hasRecordedMorphlingLayer(MorphlingPolypLayer layer) {
		return layer != null && containsNormalized(recordedMorphlingLayers, layer.serializedName());
	}

	public boolean hasRecordedMorphlingLayer(String layerName) {
		return containsNormalized(recordedMorphlingLayers, layerName);
	}

	public int recordedMorphlingLayerCount() {
		return recordedMorphlingLayers.size();
	}

	public Set<String> recordedMorphlingLayers() {
		return Set.copyOf(recordedMorphlingLayers);
	}

	public void replaceFromSync(Collection<String> recordedSpecimens, Collection<String> surrenderedSpecimens,
			Collection<String> recordedMorphlingLayers) {
		clear();
		if (recordedSpecimens != null) {
			for (String id : recordedSpecimens) {
				recordSpecimen(id);
			}
		}
		if (surrenderedSpecimens != null) {
			for (String id : surrenderedSpecimens) {
				surrenderSpecimen(id);
			}
		}
		if (recordedMorphlingLayers != null) {
			for (String layer : recordedMorphlingLayers) {
				addNormalized(this.recordedMorphlingLayers, layer);
			}
		}
	}

	public void clear() {
		recordedSpecimens.clear();
		surrenderedSpecimens.clear();
		recordedMorphlingLayers.clear();
	}

	public String serializeForTest() {
		return encodeForTest(recordedSpecimens) + "|" + encodeForTest(surrenderedSpecimens) + "|"
				+ encodeForTest(recordedMorphlingLayers);
	}

	public void deserializeForTest(String encoded) {
		clear();
		String[] parts = encoded == null ? new String[0] : encoded.split("\\|", -1);
		if (parts.length > 0) readForTest(parts[0], recordedSpecimens);
		if (parts.length > 1) readForTest(parts[1], surrenderedSpecimens);
		if (parts.length > 2) readForTest(parts[2], recordedMorphlingLayers);
	}

	private static boolean addNormalized(Set<String> values, String value) {
		String normalized = normalize(value);
		return !normalized.isEmpty() && values.add(normalized);
	}

	private static boolean containsNormalized(Set<String> values, String value) {
		String normalized = normalize(value);
		return !normalized.isEmpty() && values.contains(normalized);
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase();
	}

	private static String encodeForTest(Collection<String> values) {
		return String.join(",", new TreeSet<>(values));
	}

	private static void readForTest(String encoded, Set<String> target) {
		if (encoded == null || encoded.isBlank()) {
			return;
		}
		for (String value : encoded.split(",")) {
			addNormalized(target, value);
		}
	}
}
