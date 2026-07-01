package com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary;

import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;

public final class SpecimenBestiaryProgressTest {
	private SpecimenBestiaryProgressTest() {
	}

	public static void main(String[] args) {
		recordingSpecimensIsUnique();
		surrenderProgressIsTrackedSeparately();
		recordingPolypLayersIsUnique();
		progressSerializesAndRestores();
		syncReplacementOverwritesAllCollections();
	}

	private static void recordingSpecimensIsUnique() {
		SpecimenBestiaryState progress = new SpecimenBestiaryState();
		String desiccant = "hemomancy:desiccant";

		assertTrue("first specimen record is new", progress.recordSpecimen(desiccant));
		assertFalse("duplicate specimen record is not new", progress.recordSpecimen(desiccant));
		assertTrue("specimen remains recorded", progress.hasRecordedSpecimen(desiccant));
		assertEquals("recorded specimen count", 1, progress.recordedSpecimenCount());
	}

	private static void surrenderProgressIsTrackedSeparately() {
		SpecimenBestiaryState progress = new SpecimenBestiaryState();
		String cuttle = "hemomancy:prism_cuttle";

		assertTrue("first surrender is new", progress.surrenderSpecimen(cuttle));
		assertFalse("duplicate surrender is not new", progress.surrenderSpecimen(cuttle));
		assertTrue("surrender remains tracked", progress.hasSurrenderedSpecimen(cuttle));
		assertFalse("surrender does not imply recorded", progress.hasRecordedSpecimen(cuttle));
		assertEquals("surrendered specimen count", 1, progress.surrenderedSpecimenCount());
	}

	private static void recordingPolypLayersIsUnique() {
		SpecimenBestiaryState progress = new SpecimenBestiaryState();

		assertTrue("first fungal layer is new", progress.recordMorphlingLayer(MorphlingPolypLayer.FUNGAL));
		assertFalse("duplicate fungal layer is not new", progress.recordMorphlingLayer(MorphlingPolypLayer.FUNGAL));
		assertTrue("layer remains recorded", progress.hasRecordedMorphlingLayer(MorphlingPolypLayer.FUNGAL));
		assertEquals("recorded layer count", 1, progress.recordedMorphlingLayerCount());
	}

	private static void progressSerializesAndRestores() {
		SpecimenBestiaryState progress = new SpecimenBestiaryState();
		String polyp = "hemomancy:morphling_polyp";
		String urchin = "hemomancy:barbed_urchin";
		progress.recordSpecimen(polyp);
		progress.surrenderSpecimen(urchin);
		progress.recordMorphlingLayer(MorphlingPolypLayer.URCHIN);
		progress.recordMorphlingLayer(MorphlingPolypLayer.CUTTLEFISH);

		String encoded = progress.serializeForTest();
		SpecimenBestiaryState restored = new SpecimenBestiaryState();
		restored.deserializeForTest(encoded);

		assertTrue("restored specimen", restored.hasRecordedSpecimen(polyp));
		assertTrue("restored surrender", restored.hasSurrenderedSpecimen(urchin));
		assertTrue("restored urchin layer", restored.hasRecordedMorphlingLayer(MorphlingPolypLayer.URCHIN));
		assertTrue("restored cuttlefish layer", restored.hasRecordedMorphlingLayer(MorphlingPolypLayer.CUTTLEFISH));
		assertEquals("restored specimen count", 1, restored.recordedSpecimenCount());
		assertEquals("restored surrender count", 1, restored.surrenderedSpecimenCount());
		assertEquals("restored layer count", 2, restored.recordedMorphlingLayerCount());
	}

	private static void syncReplacementOverwritesAllCollections() {
		SpecimenBestiaryState progress = new SpecimenBestiaryState();
		progress.recordSpecimen("hemomancy:desiccant");
		progress.surrenderSpecimen("hemomancy:desiccant");
		progress.recordMorphlingLayer(MorphlingPolypLayer.FUNGAL);

		progress.replaceFromSync(
				java.util.Set.of("hemomancy:prism_cuttle"),
				java.util.Set.of("hemomancy:barbed_urchin"),
				java.util.Set.of(MorphlingPolypLayer.CUTTLEFISH.serializedName()));

		assertFalse("old recorded specimen is removed", progress.hasRecordedSpecimen("hemomancy:desiccant"));
		assertFalse("old surrendered specimen is removed", progress.hasSurrenderedSpecimen("hemomancy:desiccant"));
		assertFalse("old morphling layer is removed", progress.hasRecordedMorphlingLayer(MorphlingPolypLayer.FUNGAL));
		assertTrue("new recorded specimen is present", progress.hasRecordedSpecimen("hemomancy:prism_cuttle"));
		assertTrue("new surrendered specimen is present", progress.hasSurrenderedSpecimen("hemomancy:barbed_urchin"));
		assertTrue("new morphling layer is present", progress.hasRecordedMorphlingLayer(MorphlingPolypLayer.CUTTLEFISH));
		assertEquals("synced specimen count", 1, progress.recordedSpecimenCount());
		assertEquals("synced surrender count", 1, progress.surrenderedSpecimenCount());
		assertEquals("synced layer count", 1, progress.recordedMorphlingLayerCount());
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
