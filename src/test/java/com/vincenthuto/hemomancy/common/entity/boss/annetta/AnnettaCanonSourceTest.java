package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AnnettaCanonSourceTest {
	private AnnettaCanonSourceTest() {}
	public static void main(String[] args) throws IOException {
		String annetta = read("src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/AnnettaKnowlesEntity.java");
		String infection = read("src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/LatentAnnettaInfectionEntity.java");
		assertContains(annetta, "type == EntityInit.tooth_pecks.get()");
		assertContains(annetta, "infection.setCuringPlayer(player.getUUID())");
		assertContains(annetta, "degree.hasFoundedBloodline()");
		assertContains(infection, "progress.setAnnettaSeveranceUnlocked(true)");
	}
	private static String read(String path) throws IOException { return Files.readString(Path.of(path)); }
	private static void assertContains(String source, String expected) { if (!source.contains(expected)) throw new AssertionError("missing " + expected); }
}
