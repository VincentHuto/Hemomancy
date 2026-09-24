package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MyelinBorerRegistrationSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void entityTypeIsRegistered() throws IOException {
		String entityInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java");
		assertTrue(entityInit.contains("ENTITY_TYPES.register(\"myelin_borer\""),
				"the Myelin Borer class exists but was never registered, so it cannot spawn at all");
		assertTrue(entityInit.contains("MyelinBorerEntity::new"),
				"the entity type must build from the entity class");
		assertTrue(entityInit.contains("MyelinBorerEntity.setAttributes().build()"),
				"without attributes the game crashes the moment one spawns");
	}

	@Test
	void clientBindingsExist() throws IOException {
		assertTrue(read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java")
				.contains("EntityInit.myelin_borer.get(), MyelinBorerRenderer::new"),
				"an unregistered renderer makes the mob invisible");
		assertTrue(read("src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java")
				.contains("MyelinBorerModel.LAYER_LOCATION"),
				"an unregistered layer definition crashes the renderer");
	}

	@Test
	void spawnEggExists() throws IOException {
		assertTrue(read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java")
				.contains("spawn_egg_myelin_borer"),
				"the lang file already names this spawn egg, so the item must exist");
	}
}
