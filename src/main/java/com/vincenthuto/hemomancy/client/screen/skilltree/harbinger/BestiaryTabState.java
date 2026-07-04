package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BestiaryTabState {
	enum Kind {
		SPECIMEN,
		MORPHLING
	}

	enum PreviewDragMode {
		NONE,
		ROTATE
	}

	final List<Entry> entries = new ArrayList<>();
	int selectedIndex = -1;
	int listScroll;
	int infoScroll;
	int recordedSpecimenCount;
	int surrenderedSpecimenCount;
	int recordedMorphlingLayerCount;
	float previewPanX;
	float previewPanY;
	float previewZoom = 1.0F;
	float previewRotationAngle;
	float previewPitchAngle;
	boolean previewDragging;
	PreviewDragMode previewDragMode = PreviewDragMode.NONE;
	double previewDragLastX;
	double previewDragLastY;
	String previewKey;
	ClientLevel previewLevel;
	LivingEntity previewEntity;

	void rebuild(Set<String> recordedSpecimens, Set<String> surrenderedSpecimens, Set<String> recordedMorphlingLayers) {
		String selectedKey = selectedEntry() == null ? "" : selectedEntry().key();
		entries.clear();
		for (SpecimenBestiaryDefinitions.ResearchEntry entry : SpecimenBestiaryDefinitions.orderedResearchEntries()) {
			String id = entry.id().toString();
			entries.add(new Entry(Kind.SPECIMEN, id, entry.titleKey(), entry.descriptionKey(), entry.sourceKey(),
					"", recordedSpecimens.contains(id), surrenderedSpecimens.contains(id)));
		}
		for (SpecimenBestiaryDefinitions.MorphlingEntry entry : SpecimenBestiaryDefinitions.orderedMorphlingEntries()) {
			String id = entry.layer().serializedName();
			entries.add(new Entry(Kind.MORPHLING, id, entry.titleKey(), entry.descriptionKey(), entry.sourceKey(),
					entry.binomialKey(), recordedMorphlingLayers.contains(id), false));
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
		int newIndex = entries.indexOf(entry);
		if (newIndex != selectedIndex) {
			resetPreviewView();
		}
		selectedIndex = newIndex;
		infoScroll = 0;
	}

	void resetPreviewView() {
		previewPanX = 0.0F;
		previewPanY = 0.0F;
		previewZoom = 1.0F;
		previewRotationAngle = 0.0F;
		previewPitchAngle = 0.0F;
		previewDragging = false;
		previewDragMode = PreviewDragMode.NONE;
		previewDragLastX = 0.0D;
		previewDragLastY = 0.0D;
		previewKey = null;
		previewLevel = null;
		previewEntity = null;
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

	record Entry(Kind kind, String key, String titleKey, String descriptionKey, String sourceKey, String classificationKey,
			boolean discovered, boolean surrendered) {
		ResourceLocation previewId() {
			if (kind != Kind.SPECIMEN) {
				return null;
			}
			return ResourceLocation.tryParse(key);
		}
	}
}
