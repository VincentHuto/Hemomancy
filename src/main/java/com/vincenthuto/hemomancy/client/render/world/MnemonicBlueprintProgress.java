package com.vincenthuto.hemomancy.client.render.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Compact, stable aggregation of the unfulfilled positions in a projection. */
public final class MnemonicBlueprintProgress {
	private static final Summary COMPLETE = new Summary(0, List.of());

	private MnemonicBlueprintProgress() {
	}

	public static Summary complete() {
		return COMPLETE;
	}

	public static Summary summarize(List<String> missingMaterialIds) {
		if (missingMaterialIds == null || missingMaterialIds.isEmpty()) return COMPLETE;
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (String id : missingMaterialIds) {
			if (id != null && !id.isBlank()) counts.merge(id, 1, Integer::sum);
		}
		List<Entry> entries = counts.entrySet().stream()
				.map(entry -> new Entry(entry.getKey(), entry.getValue()))
				.sorted(Comparator.comparingInt(Entry::count).reversed()
						.thenComparing(Entry::materialId))
				.toList();
		int remaining = entries.stream().mapToInt(Entry::count).sum();
		return remaining == 0 ? COMPLETE : new Summary(remaining, entries);
	}

	public record Entry(String materialId, int count) {
	}

	public record Summary(int remaining, List<Entry> entries) {
		public Summary {
			entries = List.copyOf(entries == null ? new ArrayList<>() : entries);
		}

		public List<Entry> visibleEntries(int limit) {
			return entries.subList(0, Math.min(entries.size(), Math.max(0, limit)));
		}

		public int hiddenTypes(int limit) {
			return Math.max(0, entries.size() - Math.max(0, limit));
		}
	}
}
