package com.vincenthuto.hemomancy.common.rite.unstained;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LetheanBaptismCanonSourceTest {
	private LetheanBaptismCanonSourceTest() {}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/UnstainedCardinalRiteEvents.java"));
		assertContains(source, "ItemInit.absolution_dagger");
		assertContains(source, "firstBaptism");
	}

	private static void assertContains(String source, String expected) {
		if (!source.contains(expected)) throw new AssertionError("missing " + expected);
	}
}
