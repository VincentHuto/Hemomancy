package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * F-3: WillEntity runs on the dedicated server, so it must not reference any client class, by import or by
 * fully-qualified name. Its particle data comes from the common-side {@code HemoParticleData}.
 */
class WillEntityClientIsolationTest {
	private static final List<String> CLIENT_PACKAGES =
			List.of("net.minecraft.client.", "com.vincenthuto.hemomancy.client.");

	@Test
	void willEntityReferencesNoClientClass() {
		List<String> offending = SourceAssertions.readMainJava("common/entity/mob/monster/will/WillEntity.java")
				.lines()
				.map(String::strip)
				.filter(line -> CLIENT_PACKAGES.stream().anyMatch(line::contains))
				.toList();
		assertEquals(List.of(), offending);
	}
}
