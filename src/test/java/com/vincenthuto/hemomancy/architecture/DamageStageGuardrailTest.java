package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * G-1, F-18: every Hemomancy handler of {@code LivingDamageEvent.Pre} runs from the one stage table in
 * {@code DamageStages}, the readers that pay blood or strain run in its last stage, and every Last Rite that answers
 * a hit before it lands sits in its death-save stage.
 */
class DamageStageGuardrailTest {
	private static final String COMMON = "src/main/java/com/vincenthuto/hemomancy/common/";
	private static final String STAGES = "common/damage/DamageStages.java";
	private static final Pattern PRE_SUBSCRIBER = Pattern.compile("@SubscribeEvent(?:\\([^)]*\\))?\\s+public static void \\w+\\("
			+ "\\s*(?:final\\s+)?(?:net\\.neoforged\\.neoforge\\.event\\.entity\\.living\\.)?LivingDamageEvent\\.Pre\\s");
	private static final Pattern PRE_LISTENER = Pattern.compile("addListener\\([^;]*LivingDamageEvent\\.Pre");
	private static final Pattern LAST_RITE_SPEND = Pattern.compile("LastRiteHelper\\.consume\\(");

	@Test
	void everyPreHandlerRunsFromTheStageTable() throws IOException {
		assertEquals(Map.of(COMMON + "damage/DamageStages.java", 4), found(PRE_SUBSCRIBER),
				"call a LivingDamageEvent.Pre handler from its stage in DamageStages instead of subscribing it");
		assertEquals(Map.of(), found(PRE_LISTENER), "no LivingDamageEvent.Pre listener outside the stage table");
	}

	@Test
	void theStagesKeepTheirOrder() {
		String stages = SourceAssertions.readMainJava(STAGES);
		SourceAssertions.assertContains("attackers first", stages,
				"@SubscribeEvent(priority = EventPriority.HIGH)\n\tpublic static void attackers(LivingDamageEvent.Pre event)");
		SourceAssertions.assertContains("defenders next", stages,
				"@SubscribeEvent(priority = EventPriority.NORMAL)\n\tpublic static void defenders(LivingDamageEvent.Pre event)");
		SourceAssertions.assertContains("death-saves after every reduction", stages,
				"@SubscribeEvent(priority = EventPriority.LOW)\n\tpublic static void deathSaves(LivingDamageEvent.Pre event)");
		SourceAssertions.assertContains("readers last", stages,
				"@SubscribeEvent(priority = EventPriority.LOWEST)\n\tpublic static void consumers(LivingDamageEvent.Pre event)");
		String attackers = SourceAssertions.methodBody(stages, "public static void attackers(");
		for (String bonus : new String[] {"applySanguineFists(", "applyAbsolvedBonus(", "applyPenumbralAmbush("}) {
			SourceAssertions.assertContains("attacker bonus " + bonus, attackers, bonus);
		}
		String defenders = SourceAssertions.methodBody(stages, "public static void defenders(");
		SourceAssertions.assertContains("Iron Will is a defender reduction", defenders, "BloodVolumeEvents.applyIronWill(");
		SourceAssertions.assertNotContains("no blood is paid before the saves", defenders, "BloodVolumeEvents.onPlayerDamaged(");
	}

	@Test
	void bloodAndStrainReadWhatLands() {
		String consumers = SourceAssertions.methodBody(SourceAssertions.readMainJava(STAGES), "public static void consumers(");
		SourceAssertions.assertContains("wound blood is a reader", consumers, "BloodVolumeEvents.onPlayerDamaged(");
		SourceAssertions.assertContains("vein strain is a reader", consumers, "VascularSystemEvents.onPlayerDamaged(");
		String blood = SourceAssertions.methodBody(SourceAssertions.readMainJava(
				"common/capability/player/harbinger/bloodvolume/BloodVolumeEvents.java"), "public static void onPlayerDamaged(");
		String strain = SourceAssertions.methodBody(SourceAssertions.readMainJava(
				"common/capability/player/harbinger/vascular/VascularSystemEvents.java"), "public static void onPlayerDamaged(");
		for (String reader : new String[] {blood, strain}) {
			SourceAssertions.assertContains("a reader takes the landed amount", reader, "DamageStages.landing(");
			SourceAssertions.assertNotContains("a reader never changes the amount", reader, "setNewDamage(");
		}
	}

	@Test
	void lastRitesSaveInOneStage() throws IOException {
		// Ink Mantle and Cryptobiosis answer a hit that has landed, from LivingDamageEvent.Post.
		assertEquals(Map.of(COMMON + "event/ArmorSetBonusHandler.java", 1,
						COMMON + "manipulation/ManipulationReactiveEvents.java", 1,
						COMMON + "capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java", 1,
						COMMON + "item/harbinger/morphlings/InkCapMorphlingItem.java", 1,
						COMMON + "item/harbinger/morphlings/HushbellMorphlingItem.java", 1),
				found(LAST_RITE_SPEND), "a new Last Rite that answers a hit before it lands goes in DamageStages.deathSaves");
		String saves = SourceAssertions.methodBody(SourceAssertions.readMainJava(STAGES), "public static void deathSaves(");
		for (String save : new String[] {"trySilentArchonDeathRefusal(", "tryPhoenixDebt(",
				"tryCurvedHornSave("}) {
			SourceAssertions.assertContains("the stage runs " + save, saves, save);
		}
		SourceAssertions.assertNotContains("the Horn no longer cancels a death",
				SourceAssertions.readMainJava("common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java"),
				"LivingDeathEvent");
	}

	private static Map<String, Integer> found(Pattern pattern) throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				Matcher matcher = pattern.matcher(SourceAssertions.read(file));
				int count = 0;
				while (matcher.find()) count++;
				if (count > 0) found.put(RepoPaths.display(file), count);
			}
		}
		return found;
	}
}
