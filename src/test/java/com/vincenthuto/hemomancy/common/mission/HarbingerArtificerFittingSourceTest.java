package com.vincenthuto.hemomancy.common.mission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class HarbingerArtificerFittingSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final List<String> FITTING_IDS = List.of(
			"worn_vow_fitting",
			"barbed_fitting",
			"chitinite_fitting",
			"prismatic_fitting",
			"crimson_vestment_fitting",
			"monolithic_frame_fitting",
			"assumed_limb_fitting");
	private static final List<String> STAFF_MODEL_IDS = List.of(
			"living_staff_worn_vow",
			"living_staff_barbed_fitting",
			"living_staff_chitinite_fitting",
			"living_staff_prismatic_fitting",
			"living_staff_crimson_vestment",
			"living_staff_monolithic_frame",
			"living_staff_assumed_limb");

	private HarbingerArtificerFittingSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		fittingItemsAreInertHarbingerEquipment();
		fittingSlotIsAddedToScarletVanity();
		artificerRewardsGrantPhysicalFittings();
		staffVisualsUseUnifiedPredicate();
		fittingResourcesExist();
		noArtificerSealIdentifiersRemain();
	}

	private static void fittingItemsAreInertHarbingerEquipment() throws IOException {
		String fittingItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffFittingItem.java"));
		String fittingHelper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffFittingHelper.java"));
		String itemInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/ItemInit.java"));

		assertContains("fitting item is Harbinger equipment", fittingItem, "implements IHarbingerEquipment");
		assertContains("fitting item declares fitting equipment type", fittingItem,
				"HarbingerEquipmentType.FITTING");
		assertContains("fitting item exposes a client visual id", fittingItem, "getStaffVisualId");
		assertContains("fitting item stacks singly", fittingItem, "stacksTo(1)");
		assertDoesNotContain("fitting item has no worn tick behavior", fittingItem, "onWornTick");
		assertDoesNotContain("fitting item has no attack hook", fittingItem, "hurtEnemy");
		assertContains("fitting helper scans the fitting equipment slot", fittingHelper, "FITTING_SLOT_INDEX");
		assertContains("fitting helper returns equipped fitting visuals", fittingHelper,
				"LivingStaffFittingItem fitting");

		for (String id : FITTING_IDS) {
			assertContains("ItemInit registers " + id, itemInit, id);
		}
	}

	private static void fittingSlotIsAddedToScarletVanity() throws IOException {
		String equipmentContainer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/capability/player/harbinger/equipment/HarbingerEquipmentContainer.java"));
		String equipmentType = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/capability/player/harbinger/equipment/HarbingerEquipmentType.java"));
		String menu = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/menu/HarbingerEquipmentMenu.java"));
		String screen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/tile/functional/HarbingerEquipmentScreen.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/functional/ScarletVanityRenderer.java"));
		String layerTogglePacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/ToggleEquipmentLayerVisibilityPacket.java"));

		assertContains("equipment handler has nine slots", equipmentContainer, "SCAR_SLOTS = 9");
		assertContains("equipment type defines fitting slot", equipmentType, "FITTING(8)");
		assertContains("menu exposes fitting slot index", menu, "FITTING_SLOT_INDEX = 8");
		assertContains("menu exposes fitting menu slot", menu, "FITTING_MENU_SLOT = 7");
		assertContains("menu adds fitting slot", menu, "LivingStaffFittingItem.class");
		assertContains("menu supports quick-moving fittings", menu, "instanceof LivingStaffFittingItem");
		assertContains("screen has empty fitting slot art", screen, "EMPTY_FITTING_SLOT");
		assertContains("screen renders the fitting slot background", screen, "FITTING_MENU_SLOT");
		assertContains("screen routes fitting placeholder by menu slot", screen,
				"menuSlot == HarbingerEquipmentMenu.FITTING_MENU_SLOT");
		assertDoesNotContain("screen does not confuse slot index with jar equipment slot", screen,
				"slot.index == HarbingerEquipmentMenu.JAR_SLOT_INDEX");
		assertDoesNotContain("screen does not confuse slot index with fitting equipment slot", screen,
				"slot.index == HarbingerEquipmentMenu.FITTING_SLOT_INDEX");
		assertContains("vanity table renders equipped fitting", renderer,
				"HarbingerEquipmentMenu.FITTING_SLOT_INDEX");
		assertDoesNotContain("fittings do not get a visibility eye toggle", layerTogglePacket, "FITTING_SLOT_INDEX");
		assertDoesNotContain("fittings do not get a visibility eye toggle", screen, "toggleFitting");
	}

	private static void artificerRewardsGrantPhysicalFittings() throws IOException {
		String helper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/mission/artificer/ArtificerAssignments.java"));
		String eventHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));

		assertContains("Hematic Iron set grants Worn Vow fitting", helper, "tryGrantHematicIronFitting");
		assertContains("fork set grants branch fitting", helper, "tryGrantForkFitting");
		assertContains("fork fitting requires Barbed full set", helper, "hasFullBarbedSet");
		assertContains("fork fitting requires Chitinite full set", helper, "hasFullChitiniteSet");
		assertContains("fork fitting requires Prismatic full set", helper, "hasFullPrismaticSet");
		assertContains("Blood Lust set grants Crimson Vestment fitting", helper, "tryGrantBloodLustFitting");
		assertContains("D7 set grants Monolithic Frame fitting", helper, "tryGrantD7Fitting");
		assertContains("living arsenal grants Assumed Limb fitting", helper, "tryGrantLivingArsenalFitting");
		assertContains("dialogue gives physical fitting item", eventHandler, "giveOrDropAtEntity(player, entityId, fitting.copy())");
		assertContains("dialogue reissues only missing fittings", eventHandler, "playerHasFitting");
		assertDoesNotContain("generic fork seal reward removed", helper, "tryGrantForkSeal");

		for (String id : FITTING_IDS) {
			assertContains("reward code references " + id, helper + "\n" + eventHandler, id);
		}
	}

	private static void staffVisualsUseUnifiedPredicate() throws IOException {
		String clientEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String livingStaff = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/living_staff.json"));

		assertContains("living staff registers unified visual predicate", clientEvents,
				"Hemomancy.rloc(\"staff_visual\")");
		assertContains("staff visual checks equipped fittings", clientEvents,
				"LivingStaffFittingHelper.staffVisualFor");
		assertContains("fitting visual wins before morphling visuals", clientEvents, "return fittingVisual;");
		assertDoesNotContain("old living staff morph predicate removed", clientEvents,
				"ItemInit.living_staff.get(), Hemomancy.rloc(\"morph\")");
		assertContains("living staff model uses unified visual predicate", livingStaff,
				"\"hemomancy:staff_visual\"");
		assertDoesNotContain("living staff model no longer uses morph predicate", livingStaff,
				"\"hemomancy:morph\"");

		for (int visual = 0; visual <= 15; visual++) {
			assertContains("living staff has visual value " + visual, livingStaff,
					"\"hemomancy:staff_visual\": " + visual);
		}
		for (String modelId : STAFF_MODEL_IDS) {
			assertContains("living staff references " + modelId, livingStaff, "hemomancy:item/" + modelId);
		}
	}

	private static void fittingResourcesExist() throws IOException {
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		for (String id : FITTING_IDS) {
			assertFileExists("item model exists for " + id,
					RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + id + ".json"));
			assertContains("language names " + id, language, "item.hemomancy." + id);
		}
		for (String modelId : STAFF_MODEL_IDS) {
			assertFileExists("staff fitting model exists for " + modelId,
					RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + modelId + ".json"));
		}
		for (String advancement : List.of(
				"artificer_hematic_iron_fitting",
				"artificer_barbed_fitting",
				"artificer_chitinite_fitting",
				"artificer_prismatic_fitting",
				"artificer_blood_lust_fitting",
				"artificer_d7_fitting",
				"artificer_living_arsenal_fitting")) {
			assertFileExists("fitting advancement exists for " + advancement,
					RESOURCE_ROOT.resolve("data/hemomancy/advancement/hemomancy/" + advancement + ".json"));
		}
	}

	private static void noArtificerSealIdentifiersRemain() throws IOException {
		Pattern artificerSealPattern = Pattern.compile("artificer[_A-Za-z0-9.]*seal", Pattern.CASE_INSENSITIVE);
		for (Path path : List.of(
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java"),
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"),
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerArtificerDialogueTrees.java"),
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/mission/artificer/ArtificerAssignments.java"),
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java"),
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/network/mission/OpenHarbingerAssignmentLedgerPacket.java"),
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"),
				RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"),
				Path.of("docs/HEMOMANCY_REFERENCE.md"),
				Path.of("docs/LORE_REFERENCE.md"))) {
			assertNoRegex("Artificer seal identifier removed from " + path, read(path), artificerSealPattern);
		}

		Path advancementDir = RESOURCE_ROOT.resolve("data/hemomancy/advancement/hemomancy");
		try (Stream<Path> paths = Files.walk(advancementDir)) {
			paths.filter(Files::isRegularFile)
					.map(path -> path.getFileName().toString())
					.filter(name -> name.startsWith("artificer_") && name.contains("_seal"))
					.findFirst()
					.ifPresent(name -> {
						throw new AssertionError("Artificer seal advancement file remains: " + name);
					});
		}
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertFileExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + " missing: " + path);
		}
	}

	private static void assertContains(String label, String text, String needle) {
		if (!text.contains(needle)) {
			throw new AssertionError(label + " missing: " + needle);
		}
	}

	private static void assertDoesNotContain(String label, String text, String needle) {
		if (text.contains(needle)) {
			throw new AssertionError(label + " still contains: " + needle);
		}
	}

	private static void assertNoRegex(String label, String text, Pattern pattern) {
		var matcher = pattern.matcher(text);
		if (matcher.find()) {
			throw new AssertionError(label + " matched: " + matcher.group());
		}
	}
}
