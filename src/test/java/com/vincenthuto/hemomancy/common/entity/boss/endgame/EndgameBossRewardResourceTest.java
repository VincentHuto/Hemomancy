package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EndgameBossRewardResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private EndgameBossRewardResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String memoryOfVesper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/MemoryOfVesperItem.java"));
		String memoryRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/item/MemoryOfVesperItemRenderer.java"));
		String mycophantTendril = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/bloodline/MycophantTendrilItem.java"));
		String vesper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheEveningStarEntity.java"));
		String vesperOrdeal = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/worldgen/VesperOrdealManager.java"));
		String crownedRefusal = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java"));
		String mycophant = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/boss/endgame/MycophantEntity.java"));
		String mycophantEncounter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/worldgen/MycophantEncounterManager.java"));
		String tendrilLayer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/layer/player/MycophantTendrilFungalizationLayer.java"));
		String vesperLinesLayer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/layer/mob/endgame/VesperEveningStarLinesLayer.java"));
		String crownedRefusalRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/entity/boss/endgame/VesperTheCrownedRefusalRenderer.java"));
		String eveningStarRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/entity/boss/endgame/VesperTheEveningStarRenderer.java"));
		String layerEvents = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		String charmLayer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/layer/player/VascCharmLayer.java"));
		String memoryModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/memory_of_vesper.json"));
		String tendrilModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/mycophant_tendril.json"));
		Path tendrilTexture = RESOURCE_ROOT.resolve("assets/hemomancy/textures/item/mycophant_tendril.png");
		String vesperLoot = read(RESOURCE_ROOT.resolve("data/hemomancy/loot_table/entities/vesper_evening_star.json"));
		String mycophantLoot = read(RESOURCE_ROOT.resolve("data/hemomancy/loot_table/entities/mycophant.json"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("item registry includes Memory of Vesper", itemInit,
				"memory_of_vesper = BASEITEMS.register(\"memory_of_vesper\"");
		assertContains("Memory of Vesper uses its own renderer item class", itemInit,
				"new MemoryOfVesperItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant())");
		assertContains("item registry includes Mycophant Tendril", itemInit,
				"mycophant_tendril = BASEITEMS.register(\"mycophant_tendril\"");
		assertContains("Mycophant Tendril is charm-slot compatible", mycophantTendril,
				"extends VasculariumCharmItem");
		assertContains("Memory of Vesper supplies a custom renderer", memoryOfVesper,
				"return new MemoryOfVesperItemRenderer(");
		assertContains("Memory renderer uses the monolith fragment shader path", memoryRenderer,
				"HemoRenderTypes.monolithFragment");
		assertContains("Memory renderer draws a pome-like silhouette", memoryRenderer,
				"drawPomeSilhouette");
		assertContains("verified Vesper ordeal grants Memory of Vesper", vesperOrdeal,
				"ItemInit.memory_of_vesper.get()");
		assertContains("Vesper ordeal persists interrupted Memory delivery", vesperOrdeal,
				"PENDING_MEMORY_KEY");
		assertNotContains("Evening Star entity cannot duplicate the managed ordeal reward", vesper,
				"ItemInit.memory_of_vesper.get()");
		assertNotContains("Crowned Refusal still has no final Vesper staff loot", crownedRefusal,
				"memory_of_vesper");
		assertNotContains("Mycophant reward is not manually spawned outside its loot table", mycophant,
				"mycophant_tendril");
		assertBefore("managed Mycophant rewards are delivered after leaving the disposable arena",
				mycophantEncounter,
				"ChamberOfWillManager.get(level.getServer()).exitChamber(owner);",
				"if (first) give(owner, new ItemStack(ItemInit.mycophant_tendril.get()));");
		assertBefore("Mycophant victory clears the active encounter before dimension-change callbacks",
				mycophantEncounter,
				"clear(owner);",
				"ChamberOfWillManager.get(level.getServer()).exitChamber(owner);");
		assertContains("Mycophant rewards use player drop fallback", mycophantEncounter,
				"if (!player.addItem(stack)) player.drop(stack, false);");
		assertGuaranteedEntityDrop("Evening Star loot table documents Memory of Vesper", vesperLoot,
				"hemomancy:memory_of_vesper");
		assertGuaranteedEntityDrop("Mycophant loot table guarantees Tendril", mycophantLoot,
				"hemomancy:mycophant_tendril");
		assertContains("Tendril layer checks charm slot", tendrilLayer,
				"inv.getStackInSlot(HarbingerEquipmentMenu.CHARM_SLOT_INDEX).is(ItemInit.mycophant_tendril.get())");
		assertContains("Tendril layer draws fungal head attachment", tendrilLayer,
				"new MorphlingFungalHeadModel");
		assertContains("Evening Star renderer keeps the glow layer", eveningStarRenderer,
				"new VesperEveningStarLinesLayer(this)");
		assertNotContains("Crowned Refusal phase does not use the Evening Star glow layer", crownedRefusalRenderer,
				"VesperEveningStarLinesLayer");
		assertContains("Evening Star glow layer delegates its threshold to the shared presentation rules", vesperLinesLayer,
				"VesperEveningStarPresentationRules.shouldRenderRedLines(");
		assertContains("Tendril layer is registered for player skins", layerEvents,
				"new MycophantTendrilFungalizationLayer");
		assertContains("Charm layer renders the equipped charm stack", charmLayer,
				"renderStatic(charmStack");
		assertContains("Memory of Vesper model delegates to custom renderer", memoryModel,
				"\"parent\": \"builtin/entity\"");
		assertContains("Memory of Vesper model has a particle fallback", memoryModel,
				"\"particle\": \"hemomancy:item/qliphoth_pome\"");
		assertContains("Tendril model uses a generated item sprite", tendrilModel,
				"\"parent\": \"minecraft:item/generated\"");
		assertContains("Tendril model points at its own texture", tendrilModel,
				"\"layer0\": \"hemomancy:item/mycophant_tendril\"");
		assertNotContains("Tendril model no longer borrows erythrocoral visuals", tendrilModel,
				"erythrocoral_tendril");
		assertPngDimensions("Tendril has its own 16x16 sprite texture", tendrilTexture, 16, 16);
		assertContains("Memory of Vesper has a lang key", lang,
				"\"item.hemomancy.memory_of_vesper\": \"Memory of Vesper\"");
		assertContains("Mycophant Tendril has a lang key", lang,
				"\"item.hemomancy.mycophant_tendril\": \"Mycophant Tendril\"");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertPngDimensions(String label, Path path, int expectedWidth, int expectedHeight)
			throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
		byte[] bytes = Files.readAllBytes(path);
		if (bytes.length < 24 || bytes[0] != (byte) 0x89 || bytes[1] != 0x50 || bytes[2] != 0x4E
				|| bytes[3] != 0x47) {
			throw new AssertionError(label + ": not a PNG " + path);
		}
		int width = ((bytes[16] & 0xFF) << 24) | ((bytes[17] & 0xFF) << 16) | ((bytes[18] & 0xFF) << 8)
				| (bytes[19] & 0xFF);
		int height = ((bytes[20] & 0xFF) << 24) | ((bytes[21] & 0xFF) << 16) | ((bytes[22] & 0xFF) << 8)
				| (bytes[23] & 0xFF);
		if (width != expectedWidth || height != expectedHeight) {
			throw new AssertionError(
					label + ": expected " + expectedWidth + "x" + expectedHeight + " but was " + width + "x" + height);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex >= secondIndex) {
			throw new AssertionError(label);
		}
	}

	private static void assertGuaranteedEntityDrop(String label, String lootTable, String itemName) {
		assertContains(label, lootTable, "\"type\": \"minecraft:entity\"");
		assertContains(label, lootTable, "\"rolls\": 1");
		assertContains(label, lootTable, "\"type\": \"minecraft:item\"");
		assertContains(label, lootTable, "\"name\": \"" + itemName + "\"");
		assertNotContains(label, lootTable, "\"conditions\"");
	}
}
