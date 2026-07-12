package com.vincenthuto.hemomancy.common.lore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SupportingLoreCanonSourceTest {
	private SupportingLoreCanonSourceTest() {}
	public static void main(String[] args) throws IOException {
		String saints = read("src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/EnumSaintType.java");
		String hemorath = read("src/main/resources/data/hemomancy/recipe/memory_weaving/memory_crimson_tithe.json");
		String seraphae = read("src/main/resources/data/hemomancy/recipe/memory_weaving/memory_unclosing_eye.json");
		String paleSilver = read("src/main/resources/data/hemomancy/recipe/pale_silver_ingot.json");
		String dagger = read("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/AbsolutionDaggerItem.java");
		String rites = read("src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/UnstainedCardinalRiteEvents.java");
		String consecration = read("src/main/java/com/vincenthuto/hemomancy/common/event/ConsecrationHandler.java");
		String whispers = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/FungalWhisperDialogueTrees.java");
		String zealot = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/ZealotDialogueTrees.java");
		String unstainedPacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/unstained/PacketSyncUnstainedProgress.java");
		String degreePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncDegree.java");
		assertContains(saints, "EnumBloodTendency.FERRIC, EnumBloodTendency.MORTEM");
		assertContains(saints, "EnumBloodTendency.ANIMUS, EnumBloodTendency.LUX");
		assertContains(hemorath, "\"ferric\":  1");
		assertContains(seraphae, "\"animus\":  1");
		assertContains(paleSilver, "hemomancy:consecrated_copper_ingot");
		assertNotContains(paleSilver, "minecraft:iron_ingot");
		assertContains(dagger, "target.addEffect(new MobEffectInstance(EffectInit.hemolysis");
		assertNotContains(dagger, "TAG_WHITE_HUMOR_COATED" );
		assertNotContains(rites, "BlockInit.hematic_iron_block.get(), BlockInit.pale_silver_block.get()");
		assertNotContains(consecration, "BlockInit.hematic_iron_block.get(), BlockInit.pale_silver_block.get()");
		assertContains(whispers, "hemomancy.whisper.archon.v2.truth1.canon");
		assertContains(whispers, "hemomancy.whisper.sanctified.v2.line2.canon");
		assertContains(zealot, "if (clarityUnlocked)");
		assertContains(zealot, "option.about_silver_ward\", \"silver_ward_info");
		assertContains(zealot, "option.about_verdigris\", \"verdigris_info");
		assertContains(unstainedPacket, "progress.setInfectionSuppressed(msg.infectionSuppressed)");
		assertContains(unstainedPacket, "progress.setClarityPrepared(msg.clarityPrepared)");
		assertContains(unstainedPacket, "progress.setAnnettaSeveranceUnlocked(msg.annettaSeveranceUnlocked)");
		assertContains(degreePacket, "degree.setHasFoundedBloodline(msg.hasFoundedBloodline)");
		assertContains(degreePacket, "degree.setArchonPath(msg.archonPath)");
	}
	private static String read(String path) throws IOException { return Files.readString(Path.of(path)); }
	private static void assertContains(String source, String expected) { if (!source.contains(expected)) throw new AssertionError("missing " + expected); }
	private static void assertNotContains(String source, String forbidden) { if (source.contains(forbidden)) throw new AssertionError("still contains " + forbidden); }
}
