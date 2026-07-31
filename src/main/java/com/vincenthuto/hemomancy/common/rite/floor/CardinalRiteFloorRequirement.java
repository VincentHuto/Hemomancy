package com.vincenthuto.hemomancy.common.rite.floor;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;

import java.util.Locale;
import java.util.Objects;

public record CardinalRiteFloorRequirement(String style, CardinalRiteType minimumTier) {
	public CardinalRiteFloorRequirement {
		style = Objects.requireNonNull(style, "style").trim().toLowerCase(Locale.ROOT);
		if (style.isEmpty()) throw new IllegalArgumentException("Cardinal rite floor style cannot be blank");
		Objects.requireNonNull(minimumTier, "minimumTier");
	}

	public boolean accepts(String actualStyle, CardinalRiteType actualTier) {
		return actualStyle != null
				&& style.equals(actualStyle.trim().toLowerCase(Locale.ROOT))
				&& actualTier != null
				&& actualTier.ordinal() >= minimumTier.ordinal();
	}
}
