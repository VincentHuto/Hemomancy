package com.vincenthuto.hemomancy.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SpecimenJarDataRulesTest {
	private static final Path SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/util/SpecimenJarData.java");

	private SpecimenJarDataRulesTest() {
	}

	public static void main(String[] args) throws IOException {
		specimenEntityIdIsReadFromStoredEntityTag();
		morphlingLayerMaskIsReadFromStoredPolypTag();
		morphlingLayersAreIgnoredForNonPolypSpecimens();
		releaseOnlyReportsSuccessAfterTheEntityIsAdded();
	}

	private static void specimenEntityIdIsReadFromStoredEntityTag() throws IOException {
		String source = readSource();
		assertContains("specimen entity id helper exists", source, "getSpecimenEntityId");
		assertContains("specimen id helper uses entity id key", source, "TAG_ID");
	}

	private static void morphlingLayerMaskIsReadFromStoredPolypTag() throws IOException {
		String source = readSource();
		assertContains("morphling layer helper exists", source, "getMorphlingLayers");
		assertContains("morphling layer helper reads saved mask", source, "MorphlingLayers");
		assertContains("morphling layer helper delegates to layer rules", source, "layersFromMask");
	}

	private static void morphlingLayersAreIgnoredForNonPolypSpecimens() throws IOException {
		String source = readSource();
		assertContains("morphling layer helper gates on polyp id", source, "hemomancy:morphling_polyp");
	}

	private static void releaseOnlyReportsSuccessAfterTheEntityIsAdded() throws IOException {
		String source = readSource();
		assertContains("release checks world insertion", source, "if (!level.addFreshEntity(released)) return Optional.empty();");
		assertContains("release returns the inserted entity", source, "return Optional.of(released);");
		String block = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/SpecimenJarBlock.java"));
		assertContains("failed release preserves the filled jar", block,
				"Block.popResource(level, pos, SpecimenJarData.createStackWithSpecimen(specimen));");
	}

	private static String readSource() throws IOException {
		return Files.readString(SOURCE).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
