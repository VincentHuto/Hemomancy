package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.Objects;

public record RecipeMapLink(RecipeMapKey from, RecipeMapKey to, Kind kind) {
	public enum Kind { PROGRESSION, CONCEPTUAL }

	public RecipeMapLink {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");
		Objects.requireNonNull(kind, "kind");
	}
}
