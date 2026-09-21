package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class FargoneVariantSourceTest {

	private static final Path ENTITY = Path.of("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/FargoneEntity.java");
	private static final Path MODEL = Path.of("src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/monster/FargoneModel.java");

	@Test
	void fargoneStoresStableLineageVariant() throws Exception {
		String source = Files.readString(ENTITY);

		assertTrue(source.contains("FargoneVariant"));
		assertTrue(source.contains("VARIANT"));
		assertTrue(source.contains("addAdditionalSaveData"));
		assertTrue(source.contains("readAdditionalSaveData"));
		assertTrue(source.contains("getVariant"));
	}

	@Test
	void modelUsesVariantForReadableSilhouetteChanges() throws Exception {
		String source = Files.readString(MODEL);

		assertTrue(source.contains("getVariant"));
		assertTrue(source.contains("GUARD"));
		assertTrue(source.contains("ELDER"));
		assertTrue(source.contains("RECLAIMED"));
	}
}
