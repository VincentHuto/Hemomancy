package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BestiaryTabState {
	enum Kind {
		SPECIMEN,
		MORPHLING
	}

	final List<Entry> entries = new ArrayList<>();
	int selectedIndex = -1;
	int listScroll;
	int infoScroll;
	int recordedSpecimenCount;
	int surrenderedSpecimenCount;
	int recordedMorphlingLayerCount;

	void rebuild(Set<String> recordedSpecimens, Set<String> surrenderedSpecimens, Set<String> recordedMorphlingLayers) {
		String selectedKey = selectedEntry() == null ? "" : selectedEntry().key();
		entries.clear();
		for (SpecimenBestiaryDefinitions.ResearchEntry entry : SpecimenBestiaryDefinitions.orderedResearchEntries()) {
			String id = entry.id().toString();
			entries.add(new Entry(Kind.SPECIMEN, id, entry.titleKey(), entry.descriptionKey(), entry.sourceKey(),
					recordedSpecimens.contains(id), surrenderedSpecimens.contains(id)));
		}
		for (SpecimenBestiaryDefinitions.MorphlingEntry entry : SpecimenBestiaryDefinitions.orderedMorphlingEntries()) {
			String id = entry.layer().serializedName();
			entries.add(new Entry(Kind.MORPHLING, id, entry.titleKey(), entry.descriptionKey(), entry.sourceKey(),
					recordedMorphlingLayers.contains(id), false));
		}
		recordedSpecimenCount = recordedSpecimens.size();
		surrenderedSpecimenCount = surrenderedSpecimens.size();
		recordedMorphlingLayerCount = recordedMorphlingLayers.size();
		selectedIndex = indexOf(selectedKey);
		if (selectedIndex < 0 && !entries.isEmpty()) {
			selectedIndex = 0;
		}
	}

	Entry selectedEntry() {
		if (selectedIndex < 0 || selectedIndex >= entries.size()) {
			return null;
		}
		return entries.get(selectedIndex);
	}

	void select(Entry entry) {
		selectedIndex = entries.indexOf(entry);
		infoScroll = 0;
	}

	private int indexOf(String key) {
		if (key == null || key.isBlank()) {
			return -1;
		}
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).key().equals(key)) {
				return i;
			}
		}
		return -1;
	}

	record Entry(Kind kind, String key, String titleKey, String descriptionKey, String sourceKey,
			boolean discovered, boolean surrendered) {
	}
}
