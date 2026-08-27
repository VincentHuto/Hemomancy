package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.recipe.serializer.CardinalRiteRecipeSerializer;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteProgressionPolicy;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteProgressionResourceTest {
	private static final Path ROOT = Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite");

	@Test
	void promotionRitesUseTheAgreedFormsAndOfferingsWithoutMaterialRewards() throws IOException {
		assertPromotion("votary_rite", 1, "minor", List.of());
		assertPromotion("initiate_rite", 2, "lesser", List.of("hemomancy:hematic_iron_powder"));
		assertPromotion("sanguine_brotherhood", 3, "lesser", List.of("hemomancy:hematic_iron_powder"));
		assertPromotion("illuminatus_rite", 4, "greater",
				List.of("hemomancy:hematic_memory", "minecraft:glowstone_dust"));
		assertPromotion("sanctified_rite", 5, "greater",
				List.of("hemomancy:sanguine_quintessence", "hemomancy:puppeteering_thread",
						"minecraft:amethyst_shard"));
		assertPromotion("archon_rite", 6, "grand",
				List.of("hemomancy:sanguine_quintessence", "hemomancy:hematic_memory",
						"hemomancy:fervent_husk", "minecraft:echo_shard", "minecraft:ender_eye"));
	}

	@Test
	void utilityRitesOccupyTheirAgreedProgressionDegrees() throws IOException {
		assertDegreeAndType("exsanguination", 3, "lesser");
		assertDegreeAndType("hungering_earth", 3, "lesser");
		assertDegreeAndType("sanguine_fervor", 3, "lesser");
		assertDegreeAndType("sanguine_eclipse", 4, "greater");
		assertDegreeAndType("scarlet_summons", 5, "greater");
		assertDegreeAndType("hematic_unbinding", 5, "greater");
		assertDegreeAndType("founding_fane", 5, "greater");
		assertDegreeAndType("pallid_shadow", 6, "grand");
	}

	@Test
	void materialRitesRetainTheResultsTheirCompletionLogicRequires() throws IOException {
		assertResult("bloodline_founding", "hemomancy:unsigned_ancestral_ledger");
		assertResult("exsanguination", "hemomancy:sanguine_quintessence");
	}

	@Test
	void gourdUpgradesConsumeTheirPreviousVesselAsAVisibleStationOffering() throws IOException {
		assertUpgrade("pallid_vessel_rite", "hemomancy:dried_gourd", "hemomancy:blood_gourd_white");
		assertUpgrade("crimson_vessel_rite", "hemomancy:blood_gourd_white", "hemomancy:blood_gourd_red");
		assertUpgrade("ashen_vessel_rite", "hemomancy:blood_gourd_red", "hemomancy:blood_gourd_black");
		assertUpgrade("horn_of_culmination_rite", "hemomancy:blood_gourd_black", "hemomancy:curved_horn");
		String completion = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java"));
		assertFalse(completion.contains("consumeGourdPrerequisite"),
				"gourd upgrades must not charge a second hidden inventory copy");
	}

	@Test
	void riteRewardsHaveCheckedWorldDropFallbacks() throws IOException {
		String completion = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java"));
		assertTrue(completion.contains("giveOrDropAtRite(sLevel, caster, center, conduit)"));
		assertTrue(completion.contains("if (!sLevel.addFreshEntity(resultDrop)) caster.drop(resultStack, false);"));
		assertTrue(completion.contains("if (!level.addFreshEntity(drop) && !player.addItem(spine)) player.drop(spine, false);"));
	}

	@Test
	void harbingerRitesDoNotChargeAHiddenLumpBloodCost() throws IOException {
		try (var paths = Files.list(ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject json = read(path);
				if (json.has("unstained") && json.get("unstained").getAsBoolean()) continue;
				if ("apotheos_rite.json".equals(path.getFileName().toString())) continue;
				if (json.has("puppeteer_trial")) continue;
				assertEquals(0.0D, json.get("bloodCost").getAsDouble(), 0.0D, path.getFileName().toString());
			}
		}
	}

	@Test
	void apotheosEconomyRemainsOutsideThisProgressionPass() throws IOException {
		JsonObject json = rite("apotheos_rite");
		assertEquals(7000.0D, json.get("bloodCost").getAsDouble(), 0.0D);
		assertEquals("hemomancy:sanguine_quintessence",
				json.getAsJsonObject("result").get("id").getAsString());
	}

	@Test
	void apotheosDeclaresItsCodecIdSoItsProgressionExemptionSurvivesDecode() throws IOException {
		JsonObject json = rite("apotheos_rite");
		assertEquals("hemomancy:cardinal_rite/apotheos_rite", json.get("id").getAsString());
	}

	@Test
	void sampleRitesAreNotShippedAsProductionRecipes() {
		assertFalse(Files.exists(ROOT.resolve("sample_bloodline_vigil.json")));
		assertFalse(Files.exists(ROOT.resolve("sample_circulation.json")));
		assertFalse(Files.exists(ROOT.resolve("sample_grand_gauntlet.json")));
		assertFalse(Files.exists(ROOT.resolve("sample_inscription.json")));
	}

	@Test
	void everyShippedHarbingerCeremonyObeysItsDegreeCeiling() throws Exception {
		Method parser = CardinalRiteRecipeSerializer.class.getDeclaredMethod("ceremonyFromJson",
				JsonObject.class, ResourceLocation.class, CardinalRiteType.class, int.class);
		parser.setAccessible(true);
		try (var paths = Files.list(ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject json = read(path);
				if (json.has("unstained") && json.get("unstained").getAsBoolean()) continue;
				String fileName = path.getFileName().toString();
				String id = fileName.substring(0, fileName.length() - ".json".length());
				int degree = json.get("required_degree").getAsInt();
				CardinalRiteCeremonyDefinition ceremony =
						(CardinalRiteCeremonyDefinition) parser.invoke(null, json,
								ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/" + id),
								CardinalRiteType.byName(json.get("riteType").getAsString()), degree);
				int offerings = json.getAsJsonArray("brazier_signature").asList().stream()
						.mapToInt(element -> element.getAsJsonObject().get("count").getAsInt())
						.sum();
				assertTrue(CardinalRiteProgressionPolicy
								.violations("cardinal_rite/" + id, degree, ceremony, offerings).isEmpty(),
						id + " exceeds degree " + degree + " ceremony ceiling");
			}
		}
	}

	@Test
	void everyEffectOnlyHarbingerRiteHasAnExplicitCompletionPath() throws IOException {
		String completionSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java"));
		try (var paths = Files.list(ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject json = read(path);
				if (json.has("unstained") && json.get("unstained").getAsBoolean()) continue;
				if (json.has("result")) continue;
				if (json.has("puppeteer_trial")) continue;
				String id = path.getFileName().toString().replace(".json", "");
				assertTrue(completionSource.contains("\"cardinal_rite/" + id + "\""),
						id + " has neither a material result nor a named completion path");
			}
		}
	}

	private static void assertPromotion(String id, int degree, String form, List<String> offerings)
			throws IOException {
		JsonObject json = rite(id);
		assertEquals(degree, json.get("required_degree").getAsInt(), id);
		assertEquals(form, json.get("riteType").getAsString(), id);
		assertFalse(json.has("result"), id + " must not grant a material result");
		List<String> actual = json.getAsJsonArray("brazier_signature").asList().stream()
				.map(element -> element.getAsJsonObject().getAsJsonObject("ingredient")
						.get("item").getAsString())
				.toList();
		assertEquals(offerings, actual, id);
	}

	private static void assertDegreeAndType(String id, int degree, String form) throws IOException {
		JsonObject json = rite(id);
		assertEquals(degree, json.get("required_degree").getAsInt(), id);
		assertEquals(form, json.get("riteType").getAsString(), id);
	}

	private static void assertResult(String id, String resultId) throws IOException {
		JsonObject json = rite(id);
		assertTrue(json.has("result"), id + " must declare its material result");
		assertEquals(resultId, json.getAsJsonObject("result").get("id").getAsString(), id);
	}

	private static void assertUpgrade(String id, String inputId, String resultId) throws IOException {
		JsonObject json = rite(id);
		assertEquals(resultId, json.getAsJsonObject("result").get("id").getAsString(), id);
		assertTrue(json.getAsJsonArray("brazier_signature").asList().stream()
				.map(element -> element.getAsJsonObject().getAsJsonObject("ingredient").get("item").getAsString())
				.anyMatch(inputId::equals), id + " must visibly consume " + inputId);
	}

	private static JsonObject rite(String id) throws IOException {
		return read(ROOT.resolve(id + ".json"));
	}

	private static JsonObject read(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}
}
