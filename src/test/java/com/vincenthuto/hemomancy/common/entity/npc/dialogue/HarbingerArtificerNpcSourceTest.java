package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerArtificerNpcSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final String[] INQUIRY_ITEMS = {
			"hemomancy/hematic_armature",
			"hemomancy/hematic_iron_scrap",
			"hemomancy/aculeate_vitriol",
			"hemomancy/sclerotic_oleum",
			"hemomancy/chromatic_sublimate",
			"hemomancy/crimson_lacquer",
			"hemomancy/vicars_consecration_kit",
			"hemomancy/monolithic_cornerstone",
			"hemomancy/monolith_imbued_cloth",
			"hemomancy/tengu_mask",
			"hemomancy/grinning_mask",
			"hemomancy/lodestone_faceplate",
			"hemomancy/velorum_mask",
			"hemomancy/living_staff",
			"hemomancy/living_weapon_graft",
			"hemomancy/iron_brazier",
			"hemomancy/memory_of_vesper",
			"minecraft/iron_helmet",
			"minecraft/iron_chestplate",
			"minecraft/iron_leggings",
			"minecraft/iron_boots"
	};

	private HarbingerArtificerNpcSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String entity = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerArtificerEntity.java");
		String dialogue = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerArtificerDialogueTrees.java");
		String entityInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java");
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String layerEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/HarbingerArtificerRenderer.java");
		String model = read("src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc/HarbingerArtificerModel.java");
		String outpost = read("src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/HarbingerOutpostStructure.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String docs = read("docs/HEMOMANCY_REFERENCE.md");
		String lore = read("docs/LORE_REFERENCE.md");

		assertContains("entity opens Artificer dialogue", entity, "HarbingerArtificerDialogueTrees.forState");
		assertContains("entity gates graft branch by living staff bond", entity, "ILivingStaffProgress::hasLivingStaffBond");
		assertContains("entity keeps dialogue while holding inquiry items", entity, "DialogueItemInquiryNodes.withHeldItemInquiry");
		assertContains("entity uses Artificer inquiry speaker id", entity, "\"artificer\"");
		assertContains("entity has Artificer unknown inquiry fallback", entity,
				"hemomancy.artificer.item_inquiry.unknown");
		assertNotContains("entity must not subclass or delegate to Alchemist", entity, "HarbingerAlchemist");

		assertContains("dialogue has unawakened blood gate", dialogue, "!activeBlood || degree <= 0");
		assertContains("dialogue has purifying and clarity refusal", dialogue, "purifying || clarity");
		assertContains("dialogue offers D1 armature hint", dialogue, "degree == 1");
		assertContains("dialogue offers D2 armature tutorial", dialogue, "degree >= 2");
		assertContains("dialogue offers D3 armor forks", dialogue, "degree >= 3");
		assertContains("dialogue offers living grafts only with staff bond", dialogue, "livingStaffBond");
		assertContains("dialogue offers D5 late armature", dialogue, "degree >= 5");
		assertContains("dialogue offers D7 monolithic armature", dialogue, "degree >= 7");
		assertContains("dialogue has held item hint node", dialogue, "\"item_hint\"");

		assertContains("entity type registered", entityInit, "harbinger_artificer");
		assertContains("entity attributes registered", entityInit, "HarbingerArtificerEntity.setAttributes().build()");
		assertContains("spawn egg registered", itemInit, "spawn_egg_harbinger_artificer");
		assertContains("client renderer registered", clientEvents,
				"event.registerEntityRenderer(EntityInit.harbinger_artificer.get(), HarbingerArtificerRenderer::new)");
		assertContains("client model layer registered", layerEvents,
				"HarbingerArtificerModel.LAYER_LOCATION");
		assertContains("outpost spawns Artificer", outpost, "EntityInit.harbinger_artificer.get()");
		assertContains("outpost places Artificer in unused eastern workshop quadrant", outpost, "centerX + halfWidth");
		assertContains("outpost places Artificer in unused northern workshop quadrant", outpost, "centerZ - halfDepth");

		assertContains("renderer uses dedicated Artificer texture path", renderer,
				"textures/entity/harbinger_artificer/harbinger_artificer.png");
		assertContains("model has dedicated Artificer layer", model, "Hemomancy.rloc(\"harbinger_artificer\")");
		assertNotContains("renderer must not use Alchemist renderer/model classes", renderer, "HarbingerAlchemist");
		assertNotContains("renderer must not point at Alchemist texture path", renderer, "harbinger_alchemist");
		assertNotContains("model must not use Alchemist entity/model classes", model, "HarbingerAlchemist");
		assertNotContains("model must not point at Alchemist texture path", model, "harbinger_alchemist");
		assertExists("dedicated placeholder Artificer texture",
				"src/main/resources/assets/hemomancy/textures/entity/harbinger_artificer/harbinger_artificer.png");
		assertExists("Artificer spawn egg item model",
				"src/main/resources/assets/hemomancy/models/item/spawn_egg_harbinger_artificer.json");

		for (String inquiryItem : INQUIRY_ITEMS) {
			assertExists("Artificer inquiry for " + inquiryItem,
					"src/main/resources/data/hemomancy/dialogue_inquiry/artificer/" + inquiryItem + ".json");
		}

		assertContains("lang has Artificer entity name", lang,
				"\"entity.hemomancy.harbinger_artificer\": \"Hematic Artificer\"");
		assertContains("lang has Artificer spawn egg name", lang,
				"\"item.hemomancy.spawn_egg_harbinger_artificer\": \"Hematic Artificer Spawn Egg\"");
		assertContains("lang has Redwright identity line", lang, "hemomancy.artificer.identity.line1");
		assertContains("lang has armature tutorial line", lang, "hemomancy.artificer.armature.line1");
		assertContains("lang has living graft line", lang, "hemomancy.artificer.grafts.line1");
		assertContains("lang has unknown inquiry fallback", lang, "hemomancy.artificer.item_inquiry.unknown");
		assertContains("reference docs mention Redwright role", docs, "Hematic Artificer / Redwright");
		assertContains("lore docs mention Artificer role split", lore, "Artificer / Redwright");
	}

	private static String read(String path) throws IOException {
		Path absolute = ROOT.resolve(path);
		if (!Files.exists(absolute)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(absolute).replace("\r\n", "\n");
	}

	private static void assertExists(String label, String path) {
		if (!Files.exists(ROOT.resolve(path))) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": unexpected " + unexpected);
		}
	}
}
