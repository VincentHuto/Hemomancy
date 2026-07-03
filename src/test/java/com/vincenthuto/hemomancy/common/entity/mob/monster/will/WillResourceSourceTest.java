package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WillResourceSourceTest {
	private WillResourceSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		entityAndItemRegistrationsExist();
		resourcesExistAndReferenceWills();
		willEntityUsesConfiguredFalterBurstRules();
		drudgeActionIsWidenedSafely();
		oculifloraAnchorGateIsLive();
		willAmbushStateIsAttachmentBacked();
		directorChecksOutpostSanctuary();
	}

	private static void entityAndItemRegistrationsExist() throws IOException {
		String entityInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java");
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		assertContains("will entity registered", entityInit, "EntityType<WillEntity>");
		assertContains("will anchor entity registered", entityInit, "EntityType<WillAnchorEntity>");
		assertContains("faded memory registered", itemInit, "faded_memory");
	}

	private static void resourcesExistAndReferenceWills() throws IOException {
		String tag = read("src/main/resources/data/hemomancy/tags/entity_type/wills.json");
		String loot = read("src/main/resources/data/hemomancy/loot_table/entities/will.json");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String model = read("src/main/resources/assets/hemomancy/models/item/faded_memory.json");
		String sounds = read("src/main/resources/assets/hemomancy/sounds.json");
		assertContains("wills tag includes rogue will", tag, "hemomancy:will");
		assertContains("wills tag includes puppeteer archetype", tag, "hemomancy:blood_drunk_puppeteer");
		assertContains("will loot table is an entity table", loot, "\"type\": \"minecraft:entity\"");
		assertContains("will lang key", lang, "\"entity.hemomancy.will\"");
		assertContains("will anchor lang key", lang, "\"entity.hemomancy.will_anchor\"");
		assertContains("faded memory lang key", lang, "\"item.hemomancy.faded_memory\"");
		assertContains("will spawn egg lang key", lang, "\"item.hemomancy.spawn_egg_will\"");
		assertContains("faded memory model reuses interim memory texture", model, "hemomancy:item/memories/memory_blank");
		assertContains("will ambient sound resource", sounds, "\"entity.will.ambient\"");
		assertContains("will materialize sound resource", sounds, "\"entity.will.materialize\"");
		assertContains("will dissolve sound resource", sounds, "\"entity.will.dissolve\"");
		assertContains("will presence sting sound resource", sounds, "\"entity.will.presence_sting\"");
	}

	private static void willEntityUsesConfiguredFalterBurstRules() throws IOException {
		String will = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java");
		String config = read("src/main/java/com/vincenthuto/hemomancy/config/HemoServerConfig.java");
		assertContains("falter burst fraction uses server config", will, "HemoServerConfig.WILL_FALTER_BURST_FRACTION");
		assertContains("falter burst fraction falls back to rule default", will, "WillCombatRules.falterBurstFraction()");
		assertContains("falter burst window uses server config", will, "HemoServerConfig.WILL_FALTER_BURST_WINDOW_TICKS");
		assertContains("falter burst window falls back to rule default", will, "WillCombatRules.falterBurstWindowTicks()");
		assertContains("falter window uses server config", will, "HemoServerConfig.WILL_FALTER_WINDOW_TICKS");
		assertContains("falter config falls back to rule default", will, "WillCombatRules.falterWindowTicks()");
		assertContains("falter burst fraction is configurable", config, "falterBurstFraction");
		assertContains("falter burst window is configurable", config, "falterBurstWindowTicks");
	}

	private static void drudgeActionIsWidenedSafely() throws IOException {
		String drudgeAction = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/DrudgeAction.java");
		String caster = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/MobManipCaster.java");
		String manipInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");
		assertContains("drudge action accepts generic pathfinder mob", drudgeAction, "PathfinderMob caster");
		assertContains("mob caster bridge exists", caster, "public static boolean cast(PathfinderMob caster");
		assertContains("drudge-only charge path is guarded", manipInit, "instanceof DrudgeEntity drudgeEntity");
	}

	private static void oculifloraAnchorGateIsLive() throws IOException {
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/mob/will/WillAnchorRenderer.java");
		assertContains("anchor renderer checks Oculiflora network sight", renderer,
				"OculifloraReticularisItem.networkSightActive");
	}

	private static void willAmbushStateIsAttachmentBacked() throws IOException {
		String attachmentTypes = read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoAttachmentTypes.java");
		String access = read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityAccess.java");
		String state = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/will/WillAmbushState.java");
		assertContains("will ambush attachment registered", attachmentTypes, "WILL_AMBUSH_STATE");
		assertContains("will ambush access helper", access, "getWillAmbushState");
		assertContains("last ambush serializes", state, "LastAmbushGameTime");
		assertContains("herald serializes", state, "HeraldUntilGameTime");
		assertContains("hive attention serializes", state, "HiveAttention");
	}

	private static void directorChecksOutpostSanctuary() throws IOException {
		String director = read("src/main/java/com/vincenthuto/hemomancy/common/event/WillAmbushDirector.java");
		assertContains("director keys harbinger outpost sanctuary", director, "harbinger_outpost");
		assertContains("director queries placed structure pieces", director, "getStructureWithPieceAt");
		assertContains("director feeds outpost into sanctuary rules", director,
				"WillSanctuaryRules.isSanctuary(insideFane, inChamber, insideOutpost)");
	}

	private static String read(String path) throws IOException {
		return Files.readString(Path.of(path));
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}
}
