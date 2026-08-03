package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** An ordered filter with an implicit leading "All" option. */
public final class CyclingFamilyFilter<T> {
	private List<T> options = List.of();
	private T selected;

	public CyclingFamilyFilter(Collection<T> options) {
		setOptions(options);
	}

	public void setOptions(Collection<T> nextOptions) {
		options = List.copyOf(new LinkedHashSet<>(nextOptions));
		if (selected != null && !options.contains(selected)) selected = null;
	}

	public T selected() {
		return selected;
	}

	public boolean includes(T family) {
		return selected == null || selected.equals(family);
	}

	public void cycle(int direction) {
		if (options.isEmpty()) {
			selected = null;
			return;
		}
		int current = selected == null ? 0 : options.indexOf(selected) + 1;
		int next = Math.floorMod(current + direction, options.size() + 1);
		selected = next == 0 ? null : options.get(next - 1);
	}
}
