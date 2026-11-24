package com.vincenthuto.hemomancy.common.entity.mob.animal;

import javax.imageio.ImageIO;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

public final class LuminalCicadaResourceTest {
	private static final Path SOURCE = Path.of("src/main/java");
	private static final Path RESOURCES = Path.of("src/main/resources");

	@Test
	void resourcesAreComplete() throws IOException {
		String entityInit = source("com/vincenthuto/hemomancy/common/init/EntityInit.java");
		String itemInit = source("com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String clientEvents = source("com/vincenthuto/hemomancy/client/registry/EntityRendererRegistrar.java");
		String layers = source("com/vincenthuto/hemomancy/client/event/LayerEvents.java");
		String capturable = resource("data/hemomancy/tags/entity_type/specimen_jar_capturable.json");
		String spawn = resource("data/hemomancy/neoforge/biome_modifier/add_luminal_cicada.json");
		String spawnBiomes = resource("data/hemomancy/tags/worldgen/biome/luminal_cicada_spawnlist.json");
		Set<String> biomes = StreamSupport.stream(JsonParser.parseString(spawnBiomes).getAsJsonObject()
				.getAsJsonArray("values").spliterator(), false)
				.map(value -> value.getAsString()).collect(Collectors.toSet());
		if (!biomes.equals(Set.of("minecraft:dark_forest", "minecraft:lush_caves"))) {
			throw new AssertionError("Luminal Cicada spawn biomes must be dark forest and lush caves: " + biomes);
		}

		contains(entityInit, "luminal_cicada = ENTITY_TYPES.register(");
		contains(entityInit, "LuminalCicadaEntity::canSpawnHere");
		contains(entityInit, "LuminalCicadaEntity.setAttributes().build()");
		contains(itemInit, "spawn_egg_luminal_cicada");
		contains(clientEvents, "LuminalCicadaRenderer::new");
		contains(layers, "LuminalCicadaModel.LAYER_LOCATION");
		contains(capturable, "hemomancy:luminal_cicada");
		contains(spawn, "hemomancy:luminal_cicada");
		resource("data/hemomancy/loot_table/entities/luminal_cicada.json");

		Path texture = RESOURCES.resolve("assets/hemomancy/textures/entity/luminal_cicada/luminal_cicada.png");
		if (!Files.exists(texture)) throw new AssertionError("Missing " + texture);
		BufferedImage image = ImageIO.read(texture.toFile());
		if (image == null || image.getWidth() != 128 || image.getHeight() != 128) {
			throw new AssertionError("Luminal Cicada texture must be a 128x128 PNG");
		}
		var project = JsonParser.parseString(resource(
				"assets/hemomancy/models/entity/bbmodel/LuminalCicadaModel.bbmodel")).getAsJsonObject();
		boolean[][] used = new boolean[128][128];
		for (var element : project.getAsJsonArray("elements")) {
			var faces = element.getAsJsonObject().getAsJsonObject("faces");
			for (var face : faces.entrySet()) {
				var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
				int u = uv.get(0).getAsInt(), v = uv.get(1).getAsInt();
				int endU = uv.get(2).getAsInt(), endV = uv.get(3).getAsInt();
				for (int y = Math.min(v, endV); y < Math.max(v, endV); y++) {
					for (int x = Math.min(u, endU); x < Math.max(u, endU); x++) {
						if (used[y][x]) throw new AssertionError("Overlapping cicada UV at " + x + "," + y);
						used[y][x] = true;
					}
				}
			}
		}
		for (int y = 0; y < 128; y++) {
			for (int x = 0; x < 128; x++) {
				int expectedAlpha = used[y][x] ? 255 : 0;
				if ((image.getRGB(x, y) >>> 24) != expectedAlpha) {
					throw new AssertionError("Incorrect cicada texture coverage at " + x + "," + y);
				}
			}
		}
	}

	private static String source(String path) throws IOException {
		return Files.readString(SOURCE.resolve(path));
	}

	private static String resource(String path) throws IOException {
		Path file = RESOURCES.resolve(path);
		if (!Files.exists(file)) throw new AssertionError("Missing " + file);
		return Files.readString(file);
	}

	private static void contains(String text, String expected) {
		if (!text.contains(expected)) throw new AssertionError("Missing " + expected);
	}
}
