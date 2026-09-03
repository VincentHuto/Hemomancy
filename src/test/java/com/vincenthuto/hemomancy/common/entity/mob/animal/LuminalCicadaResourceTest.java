package com.vincenthuto.hemomancy.common.entity.mob.animal;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public final class LuminalCicadaResourceTest {
	private static final Path SOURCE = Path.of("src/main/java");
	private static final Path RESOURCES = Path.of("src/main/resources");

	@Test
	void resourcesAreComplete() throws IOException {
		main(new String[0]);
	}

	public static void main(String[] args) throws IOException {
		String entityInit = source("com/vincenthuto/hemomancy/common/init/EntityInit.java");
		String itemInit = source("com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String clientEvents = source("com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String layers = source("com/vincenthuto/hemomancy/client/event/LayerEvents.java");
		String capturable = resource("data/hemomancy/tags/entity_type/specimen_jar_capturable.json");
		String spawn = resource("data/hemomancy/neoforge/biome_modifier/add_luminal_cicada.json");

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
		if (image == null || image.getWidth() != 64 || image.getHeight() != 64) {
			throw new AssertionError("Luminal Cicada texture must be a 64x64 PNG");
		}
		for (int y = 0; y < 64; y++) {
			for (int x = 0; x < 64; x++) {
				if (!isUsedUv(x, y) && (image.getRGB(x, y) >>> 24) != 0) {
					throw new AssertionError("Luminal Cicada paints unused UV space at " + x + "," + y);
				}
			}
		}
		if ((image.getRGB(8, 25) >>> 24) != 255 || (image.getRGB(23, 41) >>> 24) != 255) {
			throw new AssertionError("Luminal Cicada body and lantern UVs must be opaque");
		}
	}

	private static boolean isUsedUv(int x, int y) {
		return in(x, y, 8, 0, 24, 8) || in(x, y, 8, 9, 24, 17)
				|| in(x, y, 0, 20, 22, 30) || in(x, y, 0, 32, 40, 40)
				|| in(x, y, 20, 40, 42, 44);
	}

	private static boolean in(int x, int y, int left, int top, int right, int bottom) {
		return x >= left && x < right && y >= top && y < bottom;
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
