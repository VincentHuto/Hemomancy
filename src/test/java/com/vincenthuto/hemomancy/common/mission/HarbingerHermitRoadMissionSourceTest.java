package com.vincenthuto.hemomancy.common.mission;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public final class HarbingerHermitRoadMissionSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private HarbingerHermitRoadMissionSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		mortalDisplayLeavesPersonalBloodStainedStone();
		ledgerIsSeparateFromFieldNotesAndUsesGuideRenderer();
		vicarGrantsLedgerAfterFirstWildHermitageReport();
		alchemistRedTaxonomyAssignmentExists();
		mnemonistWovenVesselAssignmentExists();
		firstHermitageRemnantEvidenceExists();
		hermitageRemnantWorldgenTemplateExists();
	}

	private static void mortalDisplayLeavesPersonalBloodStainedStone() throws IOException {
		String source = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/MortalDisplayBlock.java"));
		assertContains("mortal display imports block registry", source, "import com.vincenthuto.hemomancy.common.init.BlockInit;");
		assertContains("mortal display leaves blood stained stone marker", source,
				"BlockInit.placed_blood_stained_stone.get().defaultBlockState()");
		assertContains("mortal display no longer explodes for active blood players", source,
				"hemomancy.mortal_display.already_invited");
		assertDoesNotContain("mortal display no longer calls explosion", source, "worldIn.explode(");
	}

	private static void ledgerIsSeparateFromFieldNotesAndUsesGuideRenderer() throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String ledgerItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java"));
		String clientEvents = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String model = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/harbinger_assignment_ledger.json"));
		String progressScreen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/HarbingerProgressScreen.java"));
		String ledgerScreen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));
		String milestoneDrawer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/shared/MilestoneDrawerView.java"));

		assertContains("assignment ledger is registered", itemInit, "harbinger_assignment_ledger");
		assertNotMatching("field notes item remains unregistered", itemInit,
				"(?m)^\\s*public static final DeferredHolder<Item, Item> field_notes =");
		assertContains("assignment ledger uses guide book base for renderer", ledgerItem, "extends ItemGuideBook");
		assertContains("assignment ledger texture is field-note style", itemInit,
				"textures/entity/field_notes.png");
		assertContains("guide renderer covers item guide books", clientEvents, "item instanceof ItemGuideBook");
		assertContains("guide renderer is the Liber-style guide renderer", clientEvents, "RenderItemGuideBook");
		assertContains("assignment ledger language exists", language,
				"\"item.hemomancy.harbinger_assignment_ledger\"");
		assertContains("assignment ledger has 3D entity item model", model, "\"parent\": \"builtin/entity\"");
		assertContains("assignment ledger model uses Liber-style texture", model, "hemomancy:items/liber_sanguinum");
		assertContains("assignment ledger opens screen packet", ledgerItem, "OpenHarbingerAssignmentLedgerPacket");
		assertDoesNotContain("assignment ledger no longer prints chat status", ledgerItem,
				"displayClientMessage");
		assertContains("assignment ledger packet is registered", read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java")),
				"OpenHarbingerAssignmentLedgerPacket.TYPE");
		assertContains("assignment ledger packet opens screen", read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/mission/OpenHarbingerAssignmentLedgerPacket.java")),
				"HarbingerAssignmentLedgerScreen.open");
		assertContains("assignment ledger screen exists", ledgerScreen, "renderAssignmentCard");
		assertContains("assignment ledger uses progress-screen tendril background", ledgerScreen,
				"VeinBackgroundRenderer");
		assertContains("assignment ledger uses progress-screen border chrome", ledgerScreen,
				"ScreenDrawUtils.drawBorder");
		assertContains("assignment ledger sizes to screen margin like progress screen", ledgerScreen,
				"panelWidth = this.width - SCREEN_MARGIN * 2");
		assertContains("assignment ledger has stable assignment row spacing", ledgerScreen,
				"ASSIGNMENT_CARD_HEIGHT");
		assertContains("assignment ledger reserves room for collapsed milestone tab", ledgerScreen,
				"COLLAPSED_DRAWER_RESERVED_WIDTH");
		assertContains("assignment ledger tracks assignment list scroll", ledgerScreen,
				"assignmentScrollOffset");
		assertContains("assignment ledger clips assignment list to panel", ledgerScreen,
				"enableScissor(x, listY, x + w - SCROLLBAR_WIDTH");
		assertContains("assignment ledger draws assignment scrollbar", ledgerScreen,
				"drawAssignmentScrollbar");
		assertContains("assignment ledger scrolls assignment panel separately", ledgerScreen,
				"isOverAssignmentPanel");
		assertContains("assignment ledger separates header metadata from assignment titles", ledgerScreen,
				"renderAssignmentHeader");
		assertContains("assignment ledger separates taxonomy progress from title", ledgerScreen,
				"renderTaxonomyHeader");
		assertDoesNotContain("assignment ledger no longer uses vanilla done button", ledgerScreen,
				"Button.builder");
		assertDoesNotContain("assignment ledger no longer references vanilla done text", ledgerScreen,
				"gui.done");
		assertDoesNotContain("assignment ledger no longer uses local single-pixel border helper", ledgerScreen,
				"private static void drawBorder");
		assertContains("assignment ledger owns milestone drawer state", ledgerScreen, "MilestoneDrawerState milestoneState");
		assertContains("assignment ledger draws milestone drawer", ledgerScreen, "MilestoneDrawerView.draw");
		assertContains("assignment ledger scrolls milestone drawer", ledgerScreen, "MilestoneDrawerView.isOverDrawer");
		assertContains("assignment ledger toggles milestone drawer", ledgerScreen, "MilestoneDrawerView.isOverToggle");
		assertContains("assignment ledger reserves thinner milestone drawer", ledgerScreen,
				"DRAWER_RESERVED_WIDTH = 176");
		assertContains("assignment ledger truncates long assignment descriptions with ellipsis", ledgerScreen,
				"truncateWithEllipsis");
		assertContains("assignment ledger tracks truncated assignment tooltip", ledgerScreen,
				"hoveredAssignmentDescription");
		assertContains("assignment ledger renders tooltip for truncated assignment descriptions", ledgerScreen,
				"renderAssignmentDescriptionTooltip");
		assertContains("assignment ledger uses right-anchored tooltip placement", ledgerScreen,
				"renderRightAnchoredTooltip");
		assertContains("assignment ledger anchors tooltips to the right of the cursor", ledgerScreen,
				"TOOLTIP_CURSOR_OFFSET");
		assertDoesNotContain("assignment ledger no longer uses vanilla tooltip placement", ledgerScreen,
				"renderTooltip(font,");
		assertContains("milestone drawer is thinner", milestoneDrawer,
				"DRAWER_W      = 140");
		assertContains("milestone drawer uses assignment-style panel fill", milestoneDrawer,
				"PANEL_BG");
		assertContains("milestone drawer uses shared gradient-style border", milestoneDrawer,
				"ScreenDrawUtils.drawBorder");
		assertContains("milestone drawer has visible scrollbar", milestoneDrawer,
				"drawMilestoneScrollbar");
		assertContains("milestone drawer has scrollbar width", milestoneDrawer,
				"MILESTONE_SCROLLBAR_WIDTH");
		assertDoesNotContain("progress screen no longer owns milestone drawer state", progressScreen,
				"MilestoneDrawerState milestoneState");
		assertDoesNotContain("progress screen no longer draws milestone drawer", progressScreen,
				"MilestoneDrawerView.draw");
		assertDoesNotContain("progress screen no longer toggles milestone drawer", progressScreen,
				"MilestoneDrawerView.isOverToggle");
	}

	private static void vicarGrantsLedgerAfterFirstWildHermitageReport() throws IOException {
		String vicarEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java"));
		String vicarTrees = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java"));
		String eventHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));
		String advancementGranter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java"));

		assertContains("vicar checks first hermitage remnant advancement", vicarEntity,
				"ADV_HERMIT_ROAD_FIRST_REMNANT");
		assertContains("vicar checks ledger claim advancement", vicarEntity,
				"ADV_HERMIT_ROAD_LEDGER_GRANTED");
		assertContains("vicar tree receives hermit road evidence state", vicarTrees,
				"hasFoundHermitRoadRemnant");
		assertContains("vicar tree offers report option", vicarTrees,
				"hemomancy.dialogue.vicar.option.report_hermit_road");
		assertContains("vicar report event exists", vicarTrees,
				"EVENT_HERMIT_ROAD_REPORT");
		assertContains("vicar tree has post-ledger report branch", vicarTrees,
				"hermit_road_followup");
		assertContains("vicar tree uses followup language", vicarTrees,
				"hemomancy.vicar.hermit_road.followup.line1");
		assertContains("vicar neophyte degree hint mentions next assignment opportunity", vicarTrees,
				"hemomancy.vicar.neophyte.degree_hint.assignment");
		assertContains("dialogue event grants assignment ledger", eventHandler,
				"ItemInit.harbinger_assignment_ledger.get()");
		assertContains("dialogue event grants ledger advancement", eventHandler,
				"ADV_HERMIT_ROAD_LEDGER_GRANTED");
		assertContains("advancement constant exists for first remnant", advancementGranter,
				"ADV_HERMIT_ROAD_FIRST_REMNANT");
		assertContains("advancement constant exists for ledger grant", advancementGranter,
				"ADV_HERMIT_ROAD_LEDGER_GRANTED");
		assertContains("post-ledger followup line is translated", read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/lang/en_us.json")),
				"hemomancy.vicar.hermit_road.followup.line1");
	}

	private static void alchemistRedTaxonomyAssignmentExists() throws IOException {
		String alchemistTrees = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java"));
		String alchemistEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerAlchemistEntity.java"));
		String eventHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));
		String advancementGranter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java"));
		String ledgerItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java"));
		String ledgerPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/mission/OpenHarbingerAssignmentLedgerPacket.java"));
		String ledgerScreen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("alchemist has red taxonomy event prefix", alchemistTrees,
				"EVENT_RED_TAXONOMY_PREFIX");
		assertContains("alchemist detects held taxonomy samples", alchemistEntity,
				"findHeldRedTaxonomySample");
		assertContains("alchemist passes held taxonomy sample into dialogue", alchemistEntity,
				"heldRedTaxonomySample");
		assertContains("alchemist has infected fungus response", alchemistTrees,
				"hemomancy.alchemist.red_taxonomy.infected_fungus.line1");
		assertContains("alchemist has stinkhorn response", alchemistTrees,
				"hemomancy.alchemist.red_taxonomy.stinkhorn_fungus.line1");
		assertContains("alchemist has sarcodes response", alchemistTrees,
				"hemomancy.alchemist.red_taxonomy.sarcodes.line1");
		assertContains("alchemist has bleeding heart response", alchemistTrees,
				"hemomancy.alchemist.red_taxonomy.bleeding_heart.line1");
		assertContains("alchemist has rafflesia response", alchemistTrees,
				"hemomancy.alchemist.red_taxonomy.rafflesia.line1");
		assertContains("alchemist has devils tooth response", alchemistTrees,
				"hemomancy.alchemist.red_taxonomy.devils_tooth.line1");
		assertContains("alchemist has puffball control response", alchemistTrees,
				"hemomancy.alchemist.red_taxonomy.puffball_fungus.line1");
		assertContains("event handler handles red taxonomy", eventHandler,
				"handleAlchemistRedTaxonomy");
		assertContains("red taxonomy turn-in consumes sample", eventHandler,
				"held.shrink(1)");
		assertContains("red taxonomy placeholder reward message exists", eventHandler,
				"hemomancy.dialogue.event.alchemist_red_taxonomy_placeholder_reward");
		assertContains("advancement constant exists for assay completion", advancementGranter,
				"ADV_RED_TAXONOMY_COMPLETE");
		assertContains("advancement helper counts red taxonomy specimens", advancementGranter,
				"getRedTaxonomySpecimenCount");
		assertContains("ledger item sends red taxonomy count", ledgerItem,
				"getRedTaxonomySpecimenCount");
		assertContains("ledger packet carries red taxonomy count", ledgerPacket,
				"redTaxonomyCount");
		assertContains("ledger screen renders red taxonomy assignment", ledgerScreen,
				"renderRedTaxonomy");
		assertContains("red taxonomy language title exists", language,
				"screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.title");
		assertContains("red taxonomy completion advancement exists", read(RESOURCE_ROOT.resolve(
				"data/hemomancy/advancement/hemomancy/red_taxonomy_complete.json")),
				"advancements.hemomancy.red_taxonomy_complete.title");
	}

	private static void mnemonistWovenVesselAssignmentExists() throws IOException {
		String alchemistTrees = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java"));
		String alchemistEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerAlchemistEntity.java"));
		String mnemonistTrees = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerMnemonistDialogueTrees.java"));
		String mnemonistEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerMnemonistEntity.java"));
		String somaticLoom = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/tile/crafting/SomaticLoomBlockEntity.java"));
		String eventHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));
		String advancementGranter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java"));
		String ledgerItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java"));
		String ledgerPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/mission/OpenHarbingerAssignmentLedgerPacket.java"));
		String ledgerScreen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));
		String recipe = read(RESOURCE_ROOT.resolve("data/hemomancy/recipe/hematic_memory.json"));
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String docs = read(Path.of("docs/HEMOMANCY_REFERENCE.md"));

		assertContains("hematic memory recipe uses neurotic enzyme", recipe, "\"item\": \"hemomancy:neurotic_enzyme\"");
		assertDoesNotContain("hematic memory recipe no longer uses redstone", recipe, "\"item\": \"minecraft:redstone\"");
		assertContains("advancement constant exists for woven vessel", advancementGranter,
				"ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE");
		assertContains("advancement helper checks woven vessel completion", advancementGranter,
				"isMnemonistWovenVesselComplete");
		assertContains("advancement constant exists for first loom weave", advancementGranter,
				"ADV_MNEMONIST_FIRST_WEAVE_COMPLETE");
		assertContains("advancement helper checks first loom weave", advancementGranter,
				"isMnemonistFirstWeaveComplete");
		assertContains("loom grants first weave assignment record", somaticLoom,
				"ADV_MNEMONIST_FIRST_WEAVE_COMPLETE");
		assertContains("alchemist d3 breadcrumb points to mnemonist", alchemistTrees,
				"hemomancy.alchemist.initiate.mnemonist_breadcrumb");
		assertDoesNotContain("alchemist d3 breadcrumb no longer requires taxonomy completion", alchemistTrees,
				"redTaxonomyComplete");
		assertContains("alchemist breadcrumb points to mnemonist", alchemistTrees,
				"hemomancy.alchemist.initiate.mnemonist_breadcrumb");
		assertContains("mnemonist receives assignment state", mnemonistEntity,
				"isMnemonistWovenVesselComplete(serverPlayer)");
		assertContains("mnemonist has woven vessel event id", mnemonistTrees,
				"EVENT_WOVEN_VESSEL_TURN_IN");
		assertContains("mnemonist explains blank memory recipe", mnemonistTrees,
				"hemomancy.mnemonist.woven_vessel.recipe.line1");
		assertContains("event handler handles woven vessel turn-in", eventHandler,
				"handleMnemonistWovenVessel");
		assertContains("woven vessel requires a blank hematic memory", eventHandler,
				"ItemInit.hematic_memory.get()");
		assertContains("woven vessel consumes archive book", eventHandler, "Items.BOOK");
		assertContains("woven vessel consumes archive ink", eventHandler, "Items.INK_SAC");
		assertContains("woven vessel consumes archive paper", eventHandler, "Items.PAPER");
		assertContains("woven vessel rewards bleeding bulb", eventHandler, "ItemInit.bleeding_bulb.get()");
		assertContains("woven vessel rewards vivacious enzyme", eventHandler, "ItemInit.vivacious_enzyme.get()");
		assertContains("ledger item sends blank memory state", ledgerItem, "hasBlankHematicMemory");
		assertContains("ledger item sends first weave state", ledgerItem, "isMnemonistFirstWeaveComplete");
		assertContains("ledger packet carries woven vessel completion", ledgerPacket, "mnemonistWovenVesselComplete");
		assertContains("ledger packet carries first weave completion", ledgerPacket, "mnemonistFirstWeaveComplete");
		assertContains("ledger screen renders woven vessel assignment", ledgerScreen, "renderWovenVessel");
		assertContains("woven vessel progress is computed", ledgerScreen, "wovenVesselProgress()");
		assertContains("woven vessel progress displays three steps", ledgerScreen,
				"progress, 3, mnemonistFirstWeaveComplete");
		assertDoesNotContain("woven vessel progress no longer includes red taxonomy", ledgerScreen,
				"if (redTaxonomyComplete) completed++");
		assertDoesNotContain("woven vessel progress no longer includes degree reached", ledgerScreen,
				"if (degree >= 3) completed++");
		assertContains("woven vessel language title exists", language,
				"screen.hemomancy.harbinger_assignment_ledger.woven_vessel.title");
		assertContains("mnemonist recipe dialogue names self", language, "a piece of yourself");
		assertContains("mnemonist recipe dialogue names history", language, "a piece of history");
		assertContains("mnemonist recipe dialogue names nervous tissue", language, "a piece of living nervous tissue");
		assertContains("woven vessel completion advancement exists", read(RESOURCE_ROOT.resolve(
				"data/hemomancy/advancement/hemomancy/mnemonist_woven_vessel_complete.json")),
				"advancements.hemomancy.mnemonist_woven_vessel_complete.title");
		assertContains("first loom weave advancement exists", read(RESOURCE_ROOT.resolve(
				"data/hemomancy/advancement/hemomancy/mnemonist_first_weave_complete.json")),
				"advancements.hemomancy.mnemonist_first_weave_complete.title");
		assertContains("docs mention woven vessel assignment", docs, "The Woven Vessel");
	}

	private static void firstHermitageRemnantEvidenceExists() throws IOException {
		String inscription = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/discovery_inscription/hermitage_remnant/first_invitation_stone.json"));
		String advancement = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/advancement/hemomancy/hermit_road_first_remnant.json"));
		String ledgerAdvancement = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/advancement/hemomancy/hermit_road_ledger_granted.json"));
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("first remnant inscription is harbinger blood echo", inscription, "\"kind\": \"blood_echo\"");
		assertContains("first remnant requires degree one", inscription, "\"required_level\": 1");
		assertContains("first remnant inscription names Hermit Road", inscription, "The Hermit Road");
		assertContains("first remnant advancement is impossible trigger", advancement, "\"minecraft:impossible\"");
		assertContains("ledger advancement is impossible trigger", ledgerAdvancement, "\"minecraft:impossible\"");
		assertContains("advancement language title exists", language,
				"advancements.hemomancy.hermit_road_first_remnant.title");
	}

	private static void hermitageRemnantWorldgenTemplateExists() throws IOException {
		String structureInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/StructureInit.java"));
		String structureClass = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/worldgen/structure/HermitageRemnantStructure.java"));
		String structureJson = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/worldgen/structure/hermitage_remnant.json"));
		String structureSet = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/worldgen/structure_set/hermitage_remnant.json"));
		String startPool = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/worldgen/template_pool/hermitage_remnant/start_pool.json"));
		String biomeTag = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/tags/worldgen/biome/has_structure/hermitage_remnant.json"));
		String templateText = readCompressedTemplate(RESOURCE_ROOT.resolve(
				"data/hemomancy/structure/hermitage_remnant.nbt"));

		assertContains("hermitage remnant structure type is registered", structureInit, "hermitage_remnant");
		assertContains("hermitage remnant uses jigsaw placement", structureClass, "JigsawPlacement.addPieces");
		assertContains("hermitage remnant structure json uses registered type", structureJson,
				"\"type\": \"hemomancy:hermitage_remnant\"");
		assertContains("hermitage remnant structure projects to surface", structureJson,
				"\"project_start_to_heightmap\": \"WORLD_SURFACE_WG\"");
		assertContains("hermitage remnant structure set references structure", structureSet,
				"\"structure\": \"hemomancy:hermitage_remnant\"");
		assertContains("hermitage remnant template pool references nbt", startPool,
				"\"location\": \"hemomancy:hermitage_remnant\"");
		assertContains("hermitage remnant biome tag exists", biomeTag, "minecraft:forest");
		assertContains("hermitage remnant template has blood stained stone", templateText,
				"hemomancy:placed_blood_stained_stone");
		assertContains("hermitage remnant template has blood echo inscription", templateText,
				"hemomancy:blood_echo_inscription");
		assertContains("hermitage remnant template references first remnant inscription", templateText,
				"hemomancy:hermitage_remnant/first_invitation_stone");
		assertContains("hermitage remnant template has venous rubble", templateText,
				"hemomancy:venous_stone");
		assertContains("hermitage remnant template has blackstone bricks", templateText,
				"minecraft:polished_blackstone_bricks");
		assertContains("hermitage remnant template has flame accents", templateText,
				"hemomancy:crimson_flames");
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
			return new String(input.readAllBytes(), java.nio.charset.StandardCharsets.ISO_8859_1);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": still contains " + forbidden);
		}
	}

	private static void assertNotMatching(String label, String text, String forbiddenPattern) {
		if (Pattern.compile(forbiddenPattern).matcher(text).find()) {
			throw new AssertionError(label + ": still matches " + forbiddenPattern);
		}
	}
}
