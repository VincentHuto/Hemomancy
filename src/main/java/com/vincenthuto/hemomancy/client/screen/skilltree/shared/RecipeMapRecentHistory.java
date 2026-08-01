package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class RecipeMapRecentHistory {
	private final int capacity;
	private final Deque<RecipeMapKey> entries = new ArrayDeque<>();

	public RecipeMapRecentHistory(int capacity) {
		if (capacity < 1) throw new IllegalArgumentException("capacity must be positive");
		this.capacity = capacity;
	}

	public void touch(RecipeMapKey key) {
		entries.remove(key);
		entries.addFirst(key);
		while (entries.size() > capacity) entries.removeLast();
	}

	public List<RecipeMapKey> entries() {
		return List.copyOf(new ArrayList<>(entries));
	}
}
