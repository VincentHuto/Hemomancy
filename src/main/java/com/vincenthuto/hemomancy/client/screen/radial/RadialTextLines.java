package com.vincenthuto.hemomancy.client.screen.radial;

import java.util.Arrays;
import java.util.List;

public final class RadialTextLines {
	private RadialTextLines() {
	}

	public static List<String> split(String text) {
		if (text == null || text.isEmpty()) {
			return List.of();
		}
		return Arrays.stream(text.split("\\R", -1))
				.filter(line -> !line.isEmpty())
				.toList();
	}
}
