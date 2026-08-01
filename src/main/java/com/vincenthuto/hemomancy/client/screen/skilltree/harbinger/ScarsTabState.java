package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Mutable selection and player-knowledge state for the radial cerebral-scar tree. */
public final class ScarsTabState {
	final List<ScarTreeEntry> entries = new ArrayList<>();
	final Map<String, ScarTreeEntry> entriesById = new HashMap<>();
	final Map<String, ScarTreeLayout.Point> positions = new HashMap<>();
	final List<ScarTreeLayout.Edge> edges = new ArrayList<>();
	final Set<ResourceLocation> knownScarIds = new HashSet<>();
	final Set<ResourceLocation> activeScarIds = new HashSet<>();
	private String selectedScarId;

	void rebuild(List<ScarTreeEntry> nextEntries) {
		entries.clear();
		entries.addAll(nextEntries);
		entriesById.clear();
		List<ScarTreeLayout.Node> layoutNodes = new ArrayList<>();
		for (ScarTreeEntry entry : entries) {
			entriesById.put(entry.id().toString(), entry);
			layoutNodes.add(new ScarTreeLayout.Node(entry.id().toString(), entry.tendency(), entry.tier(), entry.sideBranch()));
		}
		ScarTreeLayout.Result layout = ScarTreeLayout.arrange(layoutNodes);
		positions.clear();
		positions.putAll(layout.points());
		edges.clear();
		edges.addAll(layout.edges());
		if (selectedScarId != null && !entriesById.containsKey(selectedScarId)) selectedScarId = null;
	}

	void updateKnowledge(Set<ResourceLocation> known, Set<ResourceLocation> active) {
		knownScarIds.clear();
		knownScarIds.addAll(known);
		activeScarIds.clear();
		activeScarIds.addAll(active);
	}

	ScarTreeEntry selectedEntry() {
		return selectedScarId == null ? null : entriesById.get(selectedScarId);
	}

	String selectedScarId() {
		return selectedScarId;
	}

	void toggleSelection(String scarId) {
		selectedScarId = scarId != null && scarId.equals(selectedScarId) ? null : scarId;
	}

	boolean closeDetails() {
		if (selectedScarId == null) return false;
		selectedScarId = null;
		return true;
	}
}
