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

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 9 (roadmap Phase 6, G-2): a caster's targets are chosen by {@code ManipulationCombatHelper.canHarm}. A
 * {@code Monster.class} gather passes {@code canHarm} (with the {@code Reach} preset that keeps its old verdicts) in its
 * own arguments; the few that choose targets for a mob's own AI or for an ownerless zone are listed here, per file and
 * count, with the reason. A new or stale entry fails.
 */
class CombatTargetingGuardrailTest {
	private static final String COMMON = "src/main/java/com/vincenthuto/hemomancy/common/";
	private static final String[] UNCOMPILED = {
			"src/main/java/com/vincenthuto/hemomancy/compat/mna/", "src/main/java/com/vincenthuto/hemomancy/compat/curios/" };
	private static final Pattern MONSTER_GATHER = Pattern.compile(
			"(?:getEntitiesOfClass|getNearestEntity|getNearbyEntities)\\(\\s*(?:net\\.minecraft\\.world\\.entity\\.monster\\.)?Monster\\.class");
	private static final Pattern HARM_PREDICATE = Pattern.compile("\\bboolean canHarm\\w*\\(");

	private record Allowed(int count, String reason) {
	}

	private static final Map<String, Allowed> UNGATED_MONSTER_GATHERS = Map.ofEntries(
			entry(COMMON + "entity/mob/monster/will/WillEntity.java",
					new Allowed(1, "The Will's own AI turns a monster on its rival; there is no caster")),
			entry(COMMON + "entity/summon/PaleIntercessionEntity.java",
					new Allowed(1, "Summon AI: canDefendAgainst holds its owner rules")),
			entry(COMMON + "entity/summon/PhantasmalEchoEntity.java",
					new Allowed(3, "Summon AI and the echo's own smoke; canAttack holds its owner rules")),
			entry(COMMON + "item/harbinger/memory/HematicMemoryToolEvents.java",
					new Allowed(1, "Husk Effigy AI retargeting (HuskEffigyRules)")),
			entry(COMMON + "rite/unstained/PaleConsecrationEvents.java",
					new Allowed(1, "An ownerless consecrated zone; there is no caster to ask")));

	private static final Map<String, Allowed> HARM_PREDICATES = Map.of(
			COMMON + "manipulation/ManipulationCombatHelper.java",
			new Allowed(3, "canHarm(Player, target), canHarm(caster, target, Reach) and canHarmFor(source, target)"),
			COMMON + "item/harbinger/tool/living/LivingAxeLodestoneFields.java",
			new Allowed(1, "A field's own bounds and attacker lookup, deferring to canHarm for a player"));

	@Test
	void monsterGathersPassCanHarm() throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String shown = RepoPaths.display(file);
				if (isUncompiled(shown)) continue;
				String text = SourceAssertions.read(file);
				Matcher matcher = MONSTER_GATHER.matcher(text);
				int ungated = 0;
				while (matcher.find()) {
					if (!arguments(text, text.indexOf('(', matcher.start())).contains("canHarm")) ungated++;
				}
				if (ungated > 0) found.put(shown, ungated);
			}
		}
		assertEquals(counts(UNGATED_MONSTER_GATHERS), found,
				"filter the gather with ManipulationCombatHelper.canHarm(caster, target, Reach.MONSTER_FILTER) "
						+ "(or the preset that keeps its verdicts), or list a mob-AI gather here with a reason");
	}

	@Test
	void manipulationCombatHelperIsTheOneHarmPredicate() throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String shown = RepoPaths.display(file);
				if (isUncompiled(shown)) continue;
				Matcher matcher = HARM_PREDICATE.matcher(SourceAssertions.read(file));
				int count = 0;
				while (matcher.find()) count++;
				if (count > 0) found.put(shown, count);
			}
		}
		assertEquals(counts(HARM_PREDICATES), found, "add a Reach preset or a canHarm overload instead of a new predicate");
	}

	@Test
	void areaAndLookTargetsAskCanHarm() {
		String severance = SourceAssertions.methodBody(SourceAssertions.readMainJava(
				"common/armor/ability/SilentArchonArmorAbilityHandler.java"), "public static void activateSilentArchonSeverance(");
		SourceAssertions.assertContains("Silent Severance", severance, "ManipulationCombatHelper.canHarm(player, entity, Reach.UNGATED_SURVIVAL)");
		String stillArts = SourceAssertions.readMainJava("common/init/StillArtInit.java");
		SourceAssertions.assertContains("Silver Rebuke", SourceAssertions.section(stillArts,
				"register(\"silver_rebuke\"", "register(\"lethean_mute\""), "ManipulationCombatHelper.canHarm(player, target, Reach.UNGATED)");
		SourceAssertions.assertContains("Autoimmune Edge", SourceAssertions.section(stillArts,
				"register(\"autoimmune_edge\"", "register(\"pale_intercession\""), "ManipulationCombatHelper.canHarm(player, target, Reach.UNGATED)");
		SourceAssertions.assertContains("morphling look target", SourceAssertions.methodBody(SourceAssertions.readMainJava(
				"common/item/harbinger/morphlings/MorphlingItem.java"), "public static LivingEntity findLookTarget("),
				"ManipulationCombatHelper.canHarm(player, target, Reach.UNGATED)");
		SourceAssertions.assertContains("Prismatic flash", SourceAssertions.methodBody(SourceAssertions.readMainJava(
				"common/event/ArmorSetBonusHandler.java"), "private static void triggerPrismaticFlash("),
				"ManipulationCombatHelper.canHarm(player, target, Reach.MONSTER_FILTER)");
	}

	@Test
	void drudgeWorkLivesInDrudgeActions() {
		String init = SourceAssertions.readMainJava("common/init/ManipulationInit.java");
		SourceAssertions.assertNotContains("ManipulationInit registers Drudge work by reference", init, "setDrudgeAction((");
		String actions = SourceAssertions.readMainJava("common/manipulation/DrudgeActions.java");
		SourceAssertions.assertContains("one nearest-monster lookup", actions, "static Monster nearestMonster(");
		SourceAssertions.assertContains("one work-area lookup", actions,
				"ManipulationCombatHelper.canHarm(drudge, monster, Reach.MONSTER_FILTER_ANY_STATE)");
	}

	/** The text of a call's argument list, from its opening parenthesis to the matching close. */
	private static String arguments(String text, int open) {
		int depth = 0;
		for (int i = open; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '(') depth++;
			else if (c == ')' && --depth == 0) return text.substring(open, i + 1);
		}
		throw new AssertionError("unbalanced call at " + open);
	}

	private static Map<String, Integer> counts(Map<String, Allowed> allowed) {
		Map<String, Integer> counts = new TreeMap<>();
		allowed.forEach((file, entry) -> counts.put(file, entry.count()));
		return counts;
	}

	private static boolean isUncompiled(String shown) {
		for (String prefix : UNCOMPILED) {
			if (shown.startsWith(prefix)) return true;
		}
		return false;
	}
}
