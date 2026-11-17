package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffGrowthRules;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRampartSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSkills;
import com.vincenthuto.hemomancy.common.world.vein.EarthenVeinTravelRules;
import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Each number a core-loop lesson says aloud, tied to the constant it comes from (UN-69, UN-22). A tuned constant fails
 * here until its line is rewritten. Numbers that live only in data or config are not listed.
 */
class CoreLoopLessonNumbersTest {
	private static final Map<Double, String> WORDS = Map.ofEntries(
			Map.entry(0.5D, "half"), Map.entry(1.5D, "half again"), Map.entry(2.0D, "two"),
			Map.entry(3.5D, "three and a half"), Map.entry(8.0D, "eight"), Map.entry(10.0D, "ten"),
			Map.entry(12.0D, "twelve"), Map.entry(20.0D, "twenty"), Map.entry(80.0D, "eighty"),
			Map.entry(125.0D, "one hundred and twenty-five"));

	@Test
	void everySpokenNumberMatchesItsConstant() {
		List<String> problems = new ArrayList<>();
		says(problems, "hemomancy.anchorite.craft.carried.price.line1",
				word(EarthenVeinTravelRules.LIVING_VEIN_COST) + " millilitres");
		says(problems, "hemomancy.anchorite.craft.walls.iron.line2", word(FerricRampartSculpting.MAX_SEGMENTS) + " blocks");
		says(problems, "hemomancy.artificer.craft.staff.line2", String.format(Locale.ROOT,
				"%,.0f mL, then %,.0f, %,.0f, %,.0f and %,.0f", LivingStaffGrowthRules.threshold(1),
				LivingStaffGrowthRules.threshold(2), LivingStaffGrowthRules.threshold(3),
				LivingStaffGrowthRules.threshold(4), LivingStaffGrowthRules.threshold(5)));
		says(problems, "hemomancy.artificer.craft.strand_line.twin.line2", word(StrandSkills.TWIN_COST) + " as much blood");
		says(problems, "hemomancy.artificer.craft.climb.root.line2",
				String.format(Locale.ROOT, "Rooting costs %.0f mL", ClingRules.ROOT_BLOOD));
		says(problems, "hemomancy.alchemist.craft.temper.line2",
				"Humilis cast burns " + word(TemperRules.castCost(EnumManipulationRank.HUMILIS)));
		says(problems, "hemomancy.alchemist.craft.temper.line2",
				"Mediocritas " + word(TemperRules.castCost(EnumManipulationRank.MEDIOCRITAS)));
		says(problems, "hemomancy.alchemist.craft.temper.line2",
				"highest rank " + word(TemperRules.castCost(EnumManipulationRank.PERFECTUS)));
		says(problems, "hemomancy.alchemist.craft.temper.line2",
				word(TemperRules.burnPerSecond(1.0D)) + " a measure a second");
		says(problems, "hemomancy.alchemist.craft.temper.line2",
				"up to " + word(TemperRules.burnPerSecond(1_000.0D)));
		says(problems, "hemomancy.alchemist.craft.temper.line2", word(TemperRules.BASE_CAP) + " of any school");
		says(problems, "hemomancy.alchemist.craft.temper.line2", "up to " + word(TemperRules.MAX_CAP));
		says(problems, "hemomancy.alchemist.craft.temper.fill.line2", word(TemperRules.WILL_PER_TIER) + " for each tier");
		says(problems, "hemomancy.alchemist.craft.temper.dry.line2", "takes " + word(TemperRules.KEPT_ON_DEATH));
		assertEquals(List.of(), problems);
	}

	private static String word(double value) {
		String word = WORDS.get(value);
		if (word == null) throw new AssertionError(value + " has no spoken form here; rewrite its lesson line and this table");
		return word;
	}

	private static void says(List<String> problems, String key, String expected) {
		String line = EnglishText.of(key);
		if (!line.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT))) {
			problems.add(key + " should say \"" + expected + "\": " + line);
		}
	}
}
