package com.vincenthuto.hemomancy.common.mission;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public final class VeinMasonMissionSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private VeinMasonMissionSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		anchoriteEntityAndClientWiringExists();
		anchoriteLessonUsesHighestTendencyPattern();
		vicarDirectsAdeptPlayersToMasonsRespite();
		masonsRespiteWorldgenTemplateExists();
		ledgerTracksVeinMasonAssignment();
	}

	private static void anchoriteEntityAndClientWiringExists() throws IOException {
		String entityInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/EntityInit.java"));
		String clientEvents = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String layerEvents = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/entity/npc/HarbingerCicatrixAnchoriteRenderer.java"));
		String entity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerCicatrixAnchoriteEntity.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("anchorite entity is registered", entityInit, "harbinger_cicatrix_anchorite");
		assertContains("anchorite attributes are registered", entityInit,
				"HarbingerCicatrixAnchoriteEntity.setAttributes()");
		assertContains("anchorite renderer is registered", clientEvents, "HarbingerCicatrixAnchoriteRenderer::new");
		assertContains("anchorite layer is registered", layerEvents, "HarbingerCicatrixAnchoriteModel.LAYER_LOCATION");
		assertContains("anchorite renderer uses existing texture", renderer,
				"textures/entity/harbinger_cicatrix_anchorite/harbinger_cicatrix_anchorite.png");
		assertContains("anchorite is not recruitable", entity, "HarbingerCicatrixAnchoriteDialogueTrees.forState");
		assertDoesNotContain("anchorite does not use recruitment rules", entity, "HarbingerRecruitmentRules");
		assertContains("anchorite language exists", lang,
				"\"entity.hemomancy.harbinger_cicatrix_anchorite\": \"Cicatrix Anchorite\"");
	}

	private static void anchoriteLessonUsesHighestTendencyPattern() throws IOException {
		String lesson = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/VeinMasonScarLesson.java"));
		String trees = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerCicatrixAnchoriteDialogueTrees.java"));
		String eventHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));
		String advancementGranter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("lesson maps ANIMUS", lesson, "EnumBloodTendency.ANIMUS, lesson(ItemInit.scar_pattern_heart");
		assertContains("lesson maps FLAMMEUS", lesson, "EnumBloodTendency.FLAMMEUS, lesson(ItemInit.scar_pattern_pyre");
		assertContains("lesson maps DUCTILIS", lesson, "EnumBloodTendency.DUCTILIS, lesson(ItemInit.scar_pattern_feral");
		assertContains("lesson maps LUX", lesson, "EnumBloodTendency.LUX, lesson(ItemInit.scar_pattern_halo");
		assertContains("lesson maps MORTEM", lesson, "EnumBloodTendency.MORTEM, lesson(ItemInit.scar_pattern_blight");
		assertContains("lesson maps CONGEATIO", lesson, "EnumBloodTendency.CONGEATIO, lesson(ItemInit.scar_pattern_rime");
		assertContains("lesson maps FERRIC", lesson, "EnumBloodTendency.FERRIC, lesson(ItemInit.scar_pattern_thorn");
		assertContains("lesson maps TENEBRIS", lesson, "EnumBloodTendency.TENEBRIS, lesson(ItemInit.scar_pattern_shade");
		assertContains("event handler grants blank scar", eventHandler, "ItemInit.scar_blank.get()");
		assertContains("event handler grants knapper when missing", eventHandler, "ItemInit.hematic_iron_knapper.get()");
		assertContains("event handler grants continuation reward", eventHandler, "handleVeinMasonContinuationReward");
		assertContains("reward grants second strongest tendency pattern", lesson, "continuationForPlayer");
		assertContains("reward grants motif paper stack", eventHandler, "ItemInit.runic_motif_paper.get(), 4");
		assertContains("lesson chooses highest tendency", lesson, "getAlignmentByTendency(candidate)");
		assertContains("lesson falls back to enum order for ties", lesson, "EnumBloodTendency.values()");
		assertContains("anchorite first lesson event exists", trees, "EVENT_FIRST_LESSON");
		assertContains("anchorite reward event exists", trees, "EVENT_CONTINUATION_REWARD");
		assertContains("anchorite checks first lesson advancement", trees, "veinMasonFirstLesson");
		assertContains("anchorite checks reward advancement", trees, "veinMasonRewardClaimed");
		assertContains("event handler handles first lesson", eventHandler, "handleVeinMasonFirstLesson");
		assertContains("event handler grants first lesson advancement", eventHandler,
				"ADV_VEIN_MASON_FIRST_LESSON");
		assertContains("event handler grants reward advancement", eventHandler,
				"ADV_VEIN_MASON_REWARD_CLAIMED");
		assertContains("advancement constant exists", advancementGranter, "ADV_VEIN_MASON_FIRST_LESSON");
		assertContains("reward advancement constant exists", advancementGranter, "ADV_VEIN_MASON_REWARD_CLAIMED");
		assertContains("first lesson advancement resource exists", read(RESOURCE_ROOT.resolve(
				"data/hemomancy/advancement/hemomancy/vein_mason_first_lesson.json")),
				"advancements.hemomancy.vein_mason_first_lesson.title");
		assertContains("reward advancement resource exists", read(RESOURCE_ROOT.resolve(
				"data/hemomancy/advancement/hemomancy/vein_mason_reward_claimed.json")),
				"advancements.hemomancy.vein_mason_reward_claimed.title");
		assertContains("anchorite line mentions Vein-Mason title", lang, "Vein-Mason");
		assertContains("anchorite line coaches cerebral station", lang, "Cerebral Scarring Station");
		assertContains("anchorite line coaches brazier", lang, "Anastomotic Brazier");
	}

	private static void vicarDirectsAdeptPlayersToMasonsRespite() throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String vicarTrees = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java"));
		String eventHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));
		String advancementGranter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java"));
		String mapItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/shared/MasonsRespiteMapItem.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("masons respite map item registered", itemInit, "masons_respite_map");
		assertContains("vicar has directive event", vicarTrees, "EVENT_MASONS_RESPITE_DIRECTIVE");
		assertContains("vicar adept branch receives directive state", vicarTrees, "masonsRespiteDirective");
		assertContains("vicar offers masons respite option", vicarTrees,
				"hemomancy.dialogue.vicar.option.seek_vein_mason");
		assertContains("event handler handles directive", eventHandler, "handleVicarMasonsRespiteDirective");
		assertContains("event handler grants map", eventHandler, "ItemInit.masons_respite_map.get()");
		assertContains("event handler grants directive advancement", eventHandler,
				"ADV_VICAR_MASONS_RESPITE_DIRECTIVE");
		assertContains("directive advancement constant exists", advancementGranter,
				"ADV_VICAR_MASONS_RESPITE_DIRECTIVE");
		assertContains("map targets masons respite structure tag", mapItem, "masons_respite_map_targets");
		assertContains("map has not-found language", lang, "item.hemomancy.masons_respite_map.not_found");
	}

	private static void masonsRespiteWorldgenTemplateExists() throws IOException {
		String structureInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/StructureInit.java"));
		String structureClass = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/worldgen/structure/MasonsRespiteStructure.java"));
		String structureJson = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/worldgen/structure/masons_respite.json"));
		String structureSet = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/worldgen/structure_set/masons_respite.json"));
		String startPool = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/worldgen/template_pool/masons_respite/start_pool.json"));
		String biomeTag = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/tags/worldgen/biome/has_structure/masons_respite.json"));
		String mapTag = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/tags/worldgen/structure/masons_respite_map_targets.json"));
		String templateText = readCompressedTemplate(RESOURCE_ROOT.resolve(
				"data/hemomancy/structure/masons_respite.nbt"));

		assertContains("masons respite structure type is registered", structureInit, "masons_respite");
		assertContains("masons respite uses jigsaw placement", structureClass, "JigsawPlacement.addPieces");
		assertContains("masons respite spawns anchorite after placement", structureClass,
				"EntityInit.harbinger_cicatrix_anchorite.get()");
		assertContains("masons respite structure json uses registered type", structureJson,
				"\"type\": \"hemomancy:masons_respite\"");
		assertContains("masons respite structure set references structure", structureSet,
				"\"structure\": \"hemomancy:masons_respite\"");
		assertContains("masons respite template pool references nbt", startPool,
				"\"location\": \"hemomancy:masons_respite\"");
		assertContains("masons respite biome tag exists", biomeTag, "minecraft:forest");
		assertContains("masons respite map target tag references structure", mapTag, "hemomancy:masons_respite");
		assertContains("masons respite template has scar station", templateText, "hemomancy:scar_station");
		assertContains("masons respite template has mason effigy", templateText, "hemomancy:mason_effigy");
		assertContains("masons respite template has brazier", templateText, "hemomancy:anastomotic_brazier");
	}

	private static void ledgerTracksVeinMasonAssignment() throws IOException {
		String ledgerItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java"));
		String packet = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/mission/OpenHarbingerAssignmentLedgerPacket.java"));
		String screen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String docs = read(Path.of("docs/HEMOMANCY_REFERENCE.md"));

		assertContains("ledger item sends directive state", ledgerItem, "isVicarMasonsRespiteDirective");
		assertContains("ledger item sends first lesson state", ledgerItem, "isVeinMasonFirstLesson");
		assertContains("ledger item sends effigy pattern state", ledgerItem, "isVeinMasonFirstEffigyPattern");
		assertContains("ledger packet carries directive state", packet, "vicarMasonsRespiteDirective");
		assertContains("ledger packet carries first lesson state", packet, "veinMasonFirstLesson");
		assertContains("ledger packet carries effigy pattern state", packet, "veinMasonFirstEffigyPattern");
		assertContains("ledger screen renders vein mason assignment", screen, "renderVeinMason");
		assertContains("ledger shows four visible vein mason parts", screen, "drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, veinMasonProgress(), 4)");
		assertContains("ledger part one is finding the Vein-Mason", screen, "if (veinMasonFirstLesson) completed++");
		assertContains("ledger part two is learned scar after carve and burn", screen, "if (veinMasonFirstScarLearned) completed++");
		assertContains("ledger part three is prepared effigy pattern", screen, "if (veinMasonFirstEffigyPattern) completed++");
		assertContains("ledger part four is committed loadout", screen, "if (veinMasonFirstEffigyLoadout) completed++");
		assertDoesNotContain("ledger does not count map directive as a visible part", screen, "if (vicarMasonsRespiteDirective) completed++");
		assertDoesNotContain("ledger does not count carved scar as a separate visible part", screen, "if (veinMasonFirstScarCarved) completed++");
		assertContains("ledger language title exists", lang,
				"screen.hemomancy.harbinger_assignment_ledger.vein_mason.title");
		assertContains("ledger language names reward", lang,
				"screen.hemomancy.harbinger_assignment_ledger.vein_mason.reward");
		assertContains("docs mention Masons Respite", docs, "Masons Respite");
		assertContains("docs mention Vein-Mason", docs, "Vein-Mason");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static String readCompressedTemplate(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(Files.readAllBytes(path)))) {
			return new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": unexpected " + unexpected);
		}
	}
}
