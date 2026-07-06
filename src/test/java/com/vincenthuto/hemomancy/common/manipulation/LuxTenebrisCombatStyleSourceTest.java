package com.vincenthuto.hemomancy.common.manipulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LuxTenebrisCombatStyleSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LuxTenebrisCombatStyleSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		manipulationsAreRegistered();
		memoryItemsAndResourcesExist();
		playerImplementationsCarryTrueOffense();
		tunedExistingOffenseIsDocumentedInSource();
	}

	private static void manipulationsAreRegistered() throws IOException {
		String manipInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");
		assertContains("hematic flare registered", manipInit, "MANIPS.register(\"hematic_flare\"");
		assertContains("hematic flare class", manipInit, "new HematicFlareManip(\"hematic_flare\", 125");
		assertContains("hematic flare is Humilis Lux", manipInit,
				"EnumManipulationRank.HUMILIS, EnumBloodTendency.LUX, EnumVeinSections.HEAD");
		assertContains("hematic flare secondary", manipInit, ".setSecondaryTend(EnumBloodTendency.FLAMMEUS)");
		assertContains("hematic flare cooldown", manipInit, ".setCooldownTicks(30)");

		assertContains("gloam laceration registered", manipInit, "MANIPS.register(\"gloam_laceration\"");
		assertContains("gloam laceration class", manipInit, "new GloamLacerationManip(\"gloam_laceration\", 175");
		assertContains("gloam laceration is Humilis Tenebris", manipInit,
				"EnumManipulationRank.HUMILIS, EnumBloodTendency.TENEBRIS, EnumVeinSections.ARMS");
		assertContains("gloam laceration secondary", manipInit, ".setSecondaryTend(EnumBloodTendency.MORTEM)");
		assertContains("gloam laceration cooldown", manipInit, ".setCooldownTicks(35)");
	}

	private static void memoryItemsAndResourcesExist() throws IOException {
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		assertContains("hematic flare memory registered", itemInit,
				"memory_hematic_flare = BASEITEMS.register(\"memory_hematic_flare\"");
		assertContains("gloam laceration memory registered", itemInit,
				"memory_gloam_laceration = BASEITEMS.register(\"memory_gloam_laceration\"");
		assertContains("hematic flare lang", lang,
				"\"item.hemomancy.memory_hematic_flare\": \"Memory Hematic Flare\"");
		assertContains("gloam laceration lang", lang,
				"\"item.hemomancy.memory_gloam_laceration\": \"Memory Gloam Laceration\"");
		assertContains("hematic flare inquiry lang", lang,
				"\"hemomancy.mnemonist.item_inquiry.memory_hematic_flare.line1\"");
		assertContains("gloam laceration inquiry lang", lang,
				"\"hemomancy.mnemonist.item_inquiry.memory_gloam_laceration.line1\"");

		assertResource("hematic flare model", "src/main/resources/assets/hemomancy/models/item/memory_hematic_flare.json");
		assertResource("gloam laceration model", "src/main/resources/assets/hemomancy/models/item/memory_gloam_laceration.json");
		assertResource("hematic flare overlay",
				"src/main/resources/assets/hemomancy/textures/item/memories/memory_hematic_flare_overlay.png");
		assertResource("gloam laceration overlay",
				"src/main/resources/assets/hemomancy/textures/item/memories/memory_gloam_laceration_overlay.png");
		assertResource("hematic flare dialogue",
				"src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/memory_hematic_flare.json");
		assertResource("gloam laceration dialogue",
				"src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/memory_gloam_laceration.json");

		String flareRecipe = compact(read("src/main/resources/data/hemomancy/recipe/memory_weaving/memory_hematic_flare.json"));
		assertContains("hematic flare recipe result", flareRecipe, "\"result\":\"hemomancy:memory_hematic_flare\"");
		assertContains("hematic flare catalyst", flareRecipe, "\"item\":\"minecraft:glow_ink_sac\"");
		assertContains("hematic flare lux enzyme", flareRecipe, "\"lux\":1");
		assertContains("hematic flare flammeus enzyme", flareRecipe, "\"flammeus\":1");
		assertContains("hematic flare blood cost", flareRecipe, "\"blood\":100");

		String gloamRecipe = compact(read("src/main/resources/data/hemomancy/recipe/memory_weaving/memory_gloam_laceration.json"));
		assertContains("gloam laceration recipe result", gloamRecipe, "\"result\":\"hemomancy:memory_gloam_laceration\"");
		assertContains("gloam laceration catalyst", gloamRecipe, "\"item\":\"minecraft:phantom_membrane\"");
		assertContains("gloam laceration tenebris enzyme", gloamRecipe, "\"tenebris\":1");
		assertContains("gloam laceration mortem enzyme", gloamRecipe, "\"mortem\":1");
		assertContains("gloam laceration blood cost", gloamRecipe, "\"blood\":100");
	}

	private static void playerImplementationsCarryTrueOffense() throws IOException {
		String flare = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/HematicFlareManip.java");
		assertContains("flare base damage", flare, "BASE_DAMAGE = 3.0F");
		assertContains("flare concealed bonus", flare, "CONCEALED_BONUS = 2.0F");
		assertContains("flare applies glowing", flare, "MobEffects.GLOWING");
		assertContains("flare strips invisibility", flare, "removeEffect(MobEffects.INVISIBILITY)");
		assertContains("flare scales affinity", flare, "TendencyAffinityRules.adjustManipulationDamage");

		String slash = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/GloamLacerationManip.java");
		assertContains("slash base damage", slash, "BASE_DAMAGE = 3.5F");
		assertContains("slash ambush bonus", slash, "AMBUSH_BONUS = 2.5F");
		assertContains("slash checks shroud", slash, "MobEffects.INVISIBILITY");
		assertContains("slash checks darkness", slash, "BlackVeilCovenantManager.isDarkEnough");
		assertContains("slash applies blood loss", slash, "EffectInit.blood_loss");
		assertContains("slash applies weakness", slash, "MobEffects.WEAKNESS");
		assertContains("slash scales affinity", slash, "TendencyAffinityRules.adjustManipulationDamage");
	}

	private static void tunedExistingOffenseIsDocumentedInSource() throws IOException {
		String reproof = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/PrismaticReproofManip.java");
		assertContains("reproof has base and glowing damage", reproof, "target.hasEffect(MobEffects.GLOWING) ? 4.0F : 2.0F");
		String eclipse = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/BloodEclipseManip.java");
		assertContains("blood eclipse true offense damage", eclipse, "SHADOW_DAMAGE = 3.0f");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static String compact(String text) {
		return text.replaceAll("\\s+", "");
	}

	private static void assertResource(String label, String path) {
		if (!Files.exists(ROOT.resolve(path))) {
			throw new AssertionError(label + " missing at " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}
}
