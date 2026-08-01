package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class RecipeMapQuery {
	private RecipeMapQuery() {}

	public static List<RecipeMapEntry> match(List<RecipeMapEntry> entries, String query,
			Integer column, String family) {
		String needle = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
		return entries.stream()
				.filter(RecipeMapEntry::visible)
				.filter(entry -> column == null || entry.column() == column)
				.filter(entry -> family == null || family.isBlank() || family.equals(entry.family()))
				.filter(entry -> needle.isEmpty()
						|| entry.displayName().toLowerCase(Locale.ROOT).contains(needle)
						|| entry.id().toString().toLowerCase(Locale.ROOT).contains(needle))
				.sorted(Comparator.comparingInt(RecipeMapEntry::column)
						.thenComparingInt(RecipeMapEntry::order)
						.thenComparing(RecipeMapEntry::displayName))
				.toList();
	}
}
