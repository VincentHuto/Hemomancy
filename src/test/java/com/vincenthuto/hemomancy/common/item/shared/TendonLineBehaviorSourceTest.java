package com.vincenthuto.hemomancy.common.item.shared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TendonLineBehaviorSourceTest {
	private static final Path ITEM = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/shared/TendonLineItem.java");

	private TendonLineBehaviorSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(ITEM);
		assertContains("tendon line marks velocity for player sync", source, "player.hurtMarked = true;");
		assertContains("tendon line resets fall distance through vanilla helper", source, "player.resetFallDistance();");
		assertContains("tendon line exposes client-readable anchor", source, "public static Optional<Anchor> readAnchor");
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + " missing: " + expected);
		}
	}
}
