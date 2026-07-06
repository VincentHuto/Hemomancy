package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record ManipulationSlotSnapshot(List<ManipulationSlotContribution> contributions, int requestedSlots,
		int maxSlots, int appliedSlots, int cappedOffSlots) {
	public static final ManipulationSlotSnapshot EMPTY = new ManipulationSlotSnapshot(List.of(), 0, 0, 0, 0);

	public ManipulationSlotSnapshot {
		contributions = Collections.unmodifiableList(new ArrayList<>(
				contributions == null ? List.of() : contributions));
		maxSlots = Math.max(0, maxSlots);
		appliedSlots = Math.min(Math.max(0, appliedSlots), maxSlots);
		cappedOffSlots = Math.max(0, cappedOffSlots);
	}

	public Optional<ManipulationSlotContribution> contribution(String sourceId) {
		for (ManipulationSlotContribution contribution : contributions) {
			if (contribution.sourceId().equals(sourceId)) {
				return Optional.of(contribution);
			}
		}
		return Optional.empty();
	}
}
