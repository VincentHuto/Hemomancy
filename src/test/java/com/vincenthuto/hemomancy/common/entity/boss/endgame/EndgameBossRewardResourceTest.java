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
		String vesperStaff = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/tool/living/VespersLivingStaffItem.java"));
		String mycophantTendril = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/bloodline/MycophantTendrilItem.java"));
		String vesper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheEveningStarEntity.java"));
		String crownedRefusal = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java"));
		String mycophant = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/boss/endgame/MycophantEntity.java"));
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
		String vesperModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/vespers_living_staff.json"));
		String tendrilModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/mycophant_tendril.json"));
		Path tendrilTexture = RESOURCE_ROOT.resolve("assets/hemomancy/textures/item/mycophant_tendril.png");
		String vesperLoot = read(RESOURCE_ROOT.resolve("data/hemomancy/loot_table/entities/vesper_evening_star.json"));
		String mycophantLoot = read(RESOURCE_ROOT.resolve("data/hemomancy/loot_table/entities/mycophant.json"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("item registry includes Vesper staff", itemInit,
				"vespers_living_staff = SPECIALITEMS.register(\"vespers_living_staff\"");
		assertContains("Vesper staff uses its own Living Staff subclass", itemInit,
				"new VespersLivingStaffItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant())");
		assertContains("item registry includes Mycophant Tendril", itemInit,
				"mycophant_tendril = BASEITEMS.register(\"mycophant_tendril\"");
		assertContains("Mycophant Tendril is charm-slot compatible", mycophantTendril,
				"extends VasculariumCharmItem");
		assertContains("Vesper staff changes the directed orb behavior", vesperStaff,
				"public void summonDirectedOrb(Level worldIn, Player playerIn)");
		assertNotContains("Evening Star reward is not manually spawned outside its loot table", vesper,
				"vespers_living_staff");
		assertNotContains("Crowned Refusal still has no final Vesper staff loot", crownedRefusal,
				"vespers_living_staff");
		assertNotContains("Mycophant reward is not manually spawned outside its loot table", mycophant,
				"mycophant_tendril");
		assertGuaranteedEntityDrop("Evening Star loot table guarantees Vesper staff", vesperLoot,
				"hemomancy:vespers_living_staff");
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
		assertContains("Evening Star glow layer only renders at half health", vesperLinesLayer,
				"entity.getHealth() > entity.getMaxHealth() * 0.5F");
		assertContains("Tendril layer is registered for player skins", layerEvents,
				"new MycophantTendrilFungalizationLayer");
		assertContains("Charm layer renders the equipped charm stack", charmLayer,
				"renderStatic(charmStack");
		assertContains("Vesper staff model reuses living staff visuals", vesperModel,
				"\"parent\": \"hemomancy:item/living_staff\"");
		assertContains("Tendril model uses a generated item sprite", tendrilModel,
				"\"parent\": \"minecraft:item/generated\"");
		assertContains("Tendril model points at its own texture", tendrilModel,
				"\"layer0\": \"hemomancy:item/mycophant_tendril\"");
		assertNotContains("Tendril model no longer borrows erythrocoral visuals", tendrilModel,
				"erythrocoral_tendril");
		assertPngDimensions("Tendril has its own 16x16 sprite texture", tendrilTexture, 16, 16);
		assertContains("Vesper staff has a lang key", lang,
				"\"item.hemomancy.vespers_living_staff\": \"Vesper's Living Staff\"");
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

	private static void assertGuaranteedEntityDrop(String label, String lootTable, String itemName) {
		assertContains(label, lootTable, "\"type\": \"minecraft:entity\"");
		assertContains(label, lootTable, "\"rolls\": 1");
		assertContains(label, lootTable, "\"type\": \"minecraft:item\"");
		assertContains(label, lootTable, "\"name\": \"" + itemName + "\"");
		assertNotContains(label, lootTable, "\"conditions\"");
	}
}
