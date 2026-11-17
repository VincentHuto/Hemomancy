package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodBindingManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodBindingSculpting;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.PyreticForgeRules;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core-loop Track 2: the numbers and data behind the first four Sculptings and the brazier's transmutation rite.
 */
class FirstConversionsDataTest {
	private static final Path RECIPES = RepoPaths.resolve("src/main/resources/data/hemomancy/recipe");

	@Test
	void pyreticForgeSmeltsTwoItemsAPulseByHandAndMoreWithWorkAndMastery() {
		assertEquals(2, PyreticForgeRules.itemsPerPulse(1.0D, 1.0D));
		assertEquals(3, PyreticForgeRules.itemsPerPulse(1.5D, 1.0D));
		assertEquals(4, PyreticForgeRules.itemsPerPulse(1.0D, 2.0D));
		assertEquals(1, PyreticForgeRules.itemsPerPulse(0.0D, 1.0D), "a pulse always smelts something");
	}

	@Test
	void widerBindingsCostMoreAndLingeringOnesTrailLonger() {
		double baseline = BloodBindingSculpting.bloodPerPulse(BloodBindingManip.Mode.BASELINE);
		assertTrue(baseline < BloodBindingSculpting.bloodPerPulse(BloodBindingManip.Mode.LINGERING));
		assertTrue(BloodBindingSculpting.bloodPerPulse(BloodBindingManip.Mode.LINGERING)
				< BloodBindingSculpting.bloodPerPulse(BloodBindingManip.Mode.CHAIN));
		assertTrue(BloodBindingSculpting.bloodPerPulse(BloodBindingManip.Mode.CHAIN)
				< BloodBindingSculpting.bloodPerPulse(BloodBindingManip.Mode.LATTICE));
		assertTrue(BloodBindingSculpting.tailTicks(BloodBindingManip.Mode.LINGERING, false)
				> BloodBindingSculpting.tailTicks(BloodBindingManip.Mode.BASELINE, false));
		assertEquals(BloodBindingSculpting.tailTicks(BloodBindingManip.Mode.BASELINE, false) / 2,
				BloodBindingSculpting.tailTicks(BloodBindingManip.Mode.BASELINE, true), "bosses crawl half as long");
		assertTrue(BloodBindingSculpting.constrictedSpeed() > 0.0D && BloodBindingSculpting.constrictedSpeed() < 0.15D);
	}

	@Test
	void pyreticBlockRecipesNameTheSculptingAndConvertInPlace() throws IOException {
		List<Path> files = json(RECIPES.resolve("pyretic_forge"));
		assertEquals(12, files.size());
		for (Path file : files) {
			JsonObject recipe = parse(file);
			assertEquals("hemomancy:blood_infusion", recipe.get("type").getAsString(), file.toString());
			assertEquals("hemomancy:pyretic_forge", recipe.get("sculpting").getAsString(), file.toString());
			assertTrue(recipe.get("in_place").getAsBoolean(), file.toString());
			assertTrue(recipe.get("blood_cost").getAsDouble() > 0.0D, file.toString());
		}
		for (Path plain : json(RECIPES.resolve("blood_infusion"))) {
			assertFalse(parse(plain).has("sculpting"), "plain infusion keeps working with neither key: " + plain);
		}
	}

	/** CQ-5: every transmutation names its Sculpting; CB-18: the ice chain is 4:1 through the off hand. */
	@Test
	void theBrazierHasADozenTransmutationsEachUnderASculpting() throws IOException {
		List<Path> files = json(RECIPES.resolve("brazier_transmutation"));
		assertEquals(12, files.size());
		Set<String> sculptings = new TreeSet<>();
		boolean catalyst = false;
		boolean byproduct = false;
		for (Path file : files) {
			JsonObject recipe = parse(file);
			assertEquals("hemomancy:brazier_transmutation", recipe.get("type").getAsString(), file.toString());
			assertTrue(recipe.has("input") && recipe.has("result") && recipe.has("blood_cost"), file.toString());
			assertTrue(recipe.has("sculpting"), "a transmutation works under any selection: " + file);
			sculptings.add(recipe.get("sculpting").getAsString());
			catalyst |= recipe.has("catalyst");
			byproduct |= recipe.has("byproduct");
		}
		assertEquals(Set.of("hemomancy:pyretic_forge", "hemomancy:glacial_grasp", "hemomancy:vital_effusion"), sculptings);
		assertEquals("hemomancy:vital_effusion", parse(RECIPES.resolve("brazier_transmutation/bone_meal.json"))
				.get("sculpting").getAsString());
		assertEquals("hemomancy:pyretic_forge", parse(RECIPES.resolve("brazier_transmutation/flint.json"))
				.get("sculpting").getAsString());
		for (String[] chain : new String[][]{{"packed_ice", "minecraft:ice", "30"}, {"blue_ice", "minecraft:packed_ice", "60"}}) {
			JsonObject recipe = parse(RECIPES.resolve("brazier_transmutation/" + chain[0] + ".json"));
			assertEquals(chain[1], recipe.getAsJsonObject("input").get("item").getAsString(), chain[0]);
			assertEquals(chain[1], recipe.getAsJsonObject("catalyst").get("item").getAsString(), chain[0]);
			assertEquals(3, recipe.get("catalyst_count").getAsInt(), chain[0]);
			assertEquals(Double.parseDouble(chain[2]), recipe.get("blood_cost").getAsDouble(), chain[0]);
		}
		assertTrue(catalyst, "at least one transmutation wants a catalyst in the off hand");
		assertTrue(byproduct, "the bone transmutation leaves a ruinous by-product");
	}

	@Test
	void theRetiredItemsAreGoneAndTheirOfferingsReplaced() throws IOException {
		String items = SourceAssertions.readMainJava("common/init/ItemInit.java");
		SourceAssertions.assertNotContains("the Tendon Line item retired", items, "\"tendon_line\"");
		SourceAssertions.assertNotContains("the Constrictor Cord item retired", items, "\"constrictor_cord\"");
		SourceAssertions.assertContains("Sinew Cord replaces the line as an offering", items, "\"sinew_cord\"");
		try (Stream<Path> all = Files.walk(RECIPES)) {
			for (Path file : all.filter(path -> path.toString().endsWith(".json")).toList()) {
				String text = Files.readString(file);
				assertFalse(text.contains("hemomancy:tendon_line") || text.contains("hemomancy:constrictor_cord"),
						"a recipe still names a retired item: " + file);
			}
		}
	}

	private static List<Path> json(Path directory) throws IOException {
		try (Stream<Path> files = Files.list(directory)) {
			return files.filter(path -> path.toString().endsWith(".json")).sorted().toList();
		}
	}

	private static JsonObject parse(Path file) throws IOException {
		return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
	}
}
