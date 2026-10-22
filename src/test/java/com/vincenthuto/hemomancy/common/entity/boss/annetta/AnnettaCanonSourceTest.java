package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AnnettaCanonSourceTest {
	private AnnettaCanonSourceTest() {}
	public static void main(String[] args) throws IOException {
		String annetta = read("src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/AnnettaKnowlesEntity.java");
		String infection = read("src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/LatentAnnettaInfectionEntity.java");
		String priestess = read("src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/StainedPriestessEntity.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String lore = read("src/main/resources/data/hemomancy/books/libersanguinium/cosmic_forces/pages/annetta_knowles.json");
		assertContains(annetta, "type == EntityInit.tooth_pecks.get()");
		assertBefore(annetta, "SpecimenJarData.releaseSpecimen", "SpecimenJarData.clearSpecimen(jarStack)");
		assertContains(annetta, "infection.setCuredAnnetta(this.getUUID())");
		assertBefore(annetta, "if (!server.addFreshEntity(infection))", "brewStack.shrink(1)");
		assertBefore(annetta, "if (!server.addFreshEntity(priestess))", "this.discard()");
		assertNotContains(annetta, "PendingSeverancePlayer");
		assertNotContains(annetta, "grantPendingSeverance");
		assertNotContains(annetta, "hasFoundedBloodline()");
		assertContains(annetta, "getUnstainedProgress(player).hasClarityUnlocked() && !isHarbinger(player)");
		assertContains(infection, "tag.putUUID(\"CuredAnnetta\", curedAnnetta)");
		assertContains(infection, "server.getEntity(curedAnnetta) instanceof AnnettaKnowlesEntity");
		assertContains(infection, "annetta.markResolvedAfterCure()");
		assertNotContains(infection, "CuringPlayer");
		assertNotContains(infection, "AnnettaSeveranceUnlocked");
		assertContains(priestess, "getInitiatoryDegree(player).hasFoundedBloodline()");
		assertContains(priestess, "progress.setAnnettaSeveranceUnlocked(true)");
		// Staged scenes hold her facing so the look goals cannot spin her body mid-scene.
		assertBefore(annetta, "holdSceneFacing();", "case BREAKING -> tickBreak(server);");
		assertContains(annetta, "state == EncounterState.COWERING || state == EncounterState.BREAKING || state == EncounterState.PURGING");
		// Peaceful keeps Annetta, so it must keep what rises out of her, or the break and the purge stall.
		assertContains(priestess, "protected boolean shouldDespawnInPeaceful() {");
		assertContains(infection, "protected boolean shouldDespawnInPeaceful() {");
		// Her boss bar reads the entity name; the main lang file shadows the generated one.
		assertContains(lang, "\"entity.hemomancy.annetta_knowles\": \"Annetta Knowles\"");
		assertNotContains(lang, "she understood what they had smelled in her");
		assertNotContains(lang, "Annetta's breath catches on the shape of the truth");
		assertContains(lore, "never knew she was infected");
		assertContains(lore, "Annetta dies");
	}
	private static String read(String path) throws IOException { return Files.readString(Path.of(path)); }
	private static void assertContains(String source, String expected) { if (!source.contains(expected)) throw new AssertionError("missing " + expected); }
	private static void assertNotContains(String source, String forbidden) { if (source.contains(forbidden)) throw new AssertionError("unexpected " + forbidden); }
	private static void assertBefore(String source, String first, String second) {
		int firstIndex = source.indexOf(first);
		int secondIndex = source.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex >= secondIndex) {
			throw new AssertionError(first + " must occur before " + second);
		}
	}
}
