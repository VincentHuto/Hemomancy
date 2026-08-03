package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MaterialAtlasFamilyNicknameTest {
	@Test
	void atlasCategoriesProvideCompactFamilyCardNicknames() {
		Map<String, String> harbinger = MaterialAtlasSpec.buckets(MaterialAtlasPath.HARBINGER).stream()
				.collect(Collectors.toMap(MaterialAtlasBucket::id, MaterialAtlasBucket::nickname));
		Map<String, String> unstained = MaterialAtlasSpec.buckets(MaterialAtlasPath.UNSTAINED).stream()
				.collect(Collectors.toMap(MaterialAtlasBucket::id, MaterialAtlasBucket::nickname));

		assertEquals("Bloodcraft", harbinger.get("bloodcraft_core"));
		assertEquals("Mycology", harbinger.get("fungal_ecology"));
		assertEquals("Qliphoth", harbinger.get("qliphoth_reagents"));
		assertEquals("Still Waters", unstained.get("still_waters_core"));
		assertEquals("Instruments", unstained.get("vestments_instruments"));
		assertTrue(harbinger.values().stream().allMatch(name -> !name.isBlank() && name.length() <= 12));
		assertTrue(unstained.values().stream().allMatch(name -> !name.isBlank() && name.length() <= 12));
	}
}
