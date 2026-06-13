package com.vincenthuto.hemomancy.common.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LanternTickResourceTest {
	private static final Path ROOT = Path.of("src/main/resources");
	private static final Path ASSETS = ROOT.resolve("assets/hemomancy");
	private static final Path DATA = ROOT.resolve("data/hemomancy");
	private static final Path SRC = Path.of("src/main/java/com/vincenthuto/hemomancy");

	private LanternTickResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertRegistries();
		assertServerBehavior();
		assertClientBehavior();
		assertResources();
	}

	private static void assertRegistries() throws IOException {
		String itemInit = read(SRC.resolve("common/init/ItemInit.java"));
		String entityInit = read(SRC.resolve("common/init/EntityInit.java"));
		String clientEvents = read(SRC.resolve("client/event/ClientEvents.java"));
		String layerEvents = read(SRC.resolve("client/event/LayerEvents.java"));
		assertContains("helmet item registered", itemInit, "lantern_tick_helmet");
		assertContains("spawn egg registered", itemInit, "spawn_egg_lantern_tick");
		assertContains("mob entity registered", entityInit, "lantern_tick");
		assertContains("attributes registered", entityInit, "LanternTickEntity.setAttributes()");
		assertContains("spawn placement registered", entityInit, "LanternTickEntity::canSpawnHere");
		assertContains("renderer registered", clientEvents, "LanternTickRenderer::new");
		assertContains("model layer registered", layerEvents, "LanternTickModel.LAYER_LOCATION");
		assertContains("helmet layer registered", layerEvents, "LanternTickHelmetLayer");
	}

	private static void assertServerBehavior() throws IOException {
		String entity = read(SRC.resolve("common/entity/mob/arthropod/LanternTickEntity.java"));
		String helmet = read(SRC.resolve("common/item/shared/armor/LanternTickHelmetArmorItem.java"));
		String itemInit = read(SRC.resolve("common/init/ItemInit.java"));
		String light = read(SRC.resolve("common/effect/LanternTickLightManager.java"));
		assertContains("lantern tick stores latched target", entity, "LATCHED_PLAYER_ID");
		assertContains("lantern tick leaps before latching", entity, "LATCH_LEAP_STRENGTH");
		assertContains("lantern tick drains blood first", entity, "HemoCapabilityAccess.getBloodVolume");
		assertContains("lantern tick falls back to damage", entity, "FALLBACK_DAMAGE_PER_BITE");
		assertContains("lantern tick can lure underground", entity, "shouldGlowAsLure");
		assertContains("helmet uses armor slot", itemInit, "ArmorItem.Type.HELMET");
		assertContains("helmet light manager places light blocks", light, "Blocks.LIGHT");
		assertContains("helmet light manager cleans stale lights", light, "trackedLights");
	}

	private static void assertClientBehavior() throws IOException {
		assertJsonExists(SRC.resolve("client/model/entity/mob/arthropod/LanternTickModel.java"));
		assertJsonExists(SRC.resolve("client/render/entity/mob/arthropod/LanternTickRenderer.java"));
		assertJsonExists(SRC.resolve("client/model/armor/LanternTickHeadArmorModel.java"));
		assertJsonExists(SRC.resolve("client/render/layer/player/LanternTickHelmetLayer.java"));
		assertJsonExists(SRC.resolve("client/render/item/LanternTickHelmetItemRenderer.java"));
	}

	private static void assertResources() throws IOException {
		assertJsonExists(ASSETS.resolve("models/item/lantern_tick_helmet.json"));
		assertJsonExists(ASSETS.resolve("models/item/spawn_egg_lantern_tick.json"));
		assertFileExists(ASSETS.resolve("textures/entity/lantern_tick/model_lantern_tick.png"));
		assertFileExists(ASSETS.resolve("textures/item/lantern_tick_helmet.png"));
		assertJsonExists(DATA.resolve("loot_table/entities/lantern_tick.json"));
		assertJsonExists(DATA.resolve("neoforge/biome_modifier/add_lantern_tick.json"));
		assertJsonExists(DATA.resolve("tags/worldgen/biome/lantern_tick_spawnlist.json"));
		assertContains("loot table drops helmet", read(DATA.resolve("loot_table/entities/lantern_tick.json")),
				"hemomancy:lantern_tick_helmet");
		assertContains("spawn modifier uses lantern tick", read(DATA.resolve("neoforge/biome_modifier/add_lantern_tick.json")),
				"hemomancy:lantern_tick");
	}

	private static void assertJsonExists(Path path) throws IOException {
		assertFileExists(path);
		read(path);
	}

	private static void assertFileExists(Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
