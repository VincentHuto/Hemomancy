package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SummonsTabState {
	public final Map<Integer, List<PuppeteerSummonDefinition>> summonsByDegree = new LinkedHashMap<>();
	public final List<String> knownSummonNames = new ArrayList<>();

	public Integer selectedDegree = null;
	public int selectedSummonIndex = 0;

	public int sidebarScroll = 0;
	public int infoScroll = 0;

	public float previewRotationAngle = 0f;
	public boolean previewDragging = false;
	public double previewDragLastX = 0;
	public String previewName = null;
	public LivingEntity previewEntity = null;

	public void rebuildTierMap() {
		summonsByDegree.clear();
		for (PuppeteerSummonDefinition definition : PuppeteerSummonDefinitions.all()) {
			summonsByDegree.computeIfAbsent(definition.requiredDegree(), ignored -> new ArrayList<>()).add(definition);
		}
	}

	public void autoSelectFirstDegree() {
		if (selectedDegree != null && summonsByDegree.containsKey(selectedDegree)) {
			return;
		}
		selectedDegree = summonsByDegree.keySet().stream().findFirst().orElse(null);
		selectedSummonIndex = 0;
	}

	public Optional<PuppeteerSummonDefinition> selectedDefinition() {
		if (selectedDegree == null) {
			return Optional.empty();
		}
		List<PuppeteerSummonDefinition> definitions = summonsByDegree.getOrDefault(selectedDegree, List.of());
		if (definitions.isEmpty()) {
			return Optional.empty();
		}
		if (selectedSummonIndex >= definitions.size()) {
			selectedSummonIndex = 0;
		}
		return Optional.of(definitions.get(selectedSummonIndex));
	}

	public boolean isKnown(PuppeteerSummonDefinition definition) {
		return knownSummonNames.contains(definition.name());
	}

	public int sidebarContentH() {
		int rowH = 22;
		int total = 0;
		for (Map.Entry<Integer, List<PuppeteerSummonDefinition>> entry : summonsByDegree.entrySet()) {
			total += rowH + 2;
			if (entry.getKey().equals(selectedDegree)) {
				total += entry.getValue().size() * 18;
			}
		}
		return total;
	}

	public void clampSidebarScroll(int visibleH) {
		sidebarScroll = Math.min(sidebarScroll, Math.max(0, sidebarContentH() - visibleH));
	}
}
