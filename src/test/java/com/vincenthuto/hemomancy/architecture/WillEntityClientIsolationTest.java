package com.vincenthuto.hemomancy.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * F-3: WillEntity runs on the dedicated server, so it must not reference any client class, by import or by
 * fully-qualified name. Its particle data comes from the common-side {@code HemoParticleData}.
 */
class WillEntityClientIsolationTest {
	private static final Path WILL_ENTITY =
			Path.of("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java");
	private static final List<String> CLIENT_PACKAGES =
			List.of("net.minecraft.client.", "com.vincenthuto.hemomancy.client.");

	@Test
	void willEntityReferencesNoClientClass() throws Exception {
		List<String> offending = Files.readAllLines(WILL_ENTITY).stream()
				.map(String::strip)
				.filter(line -> CLIENT_PACKAGES.stream().anyMatch(line::contains))
				.toList();
		assertEquals(List.of(), offending);
	}
}
