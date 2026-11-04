package com.vincenthuto.hemomancy.client.render;

import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every entity base texture shows something. A texture whose pixels are all transparent draws an invisible creature
 * (the Pale Intercession shipped that way, cleanup audit VS-10 / A-4). Glow and emissive sheets may be empty: an
 * empty one means the creature does not glow (seven pelagic creatures ship such sheets). Archived textures under
 * {@code old/} are not shipped and are not checked.
 */
class EntityTextureOpacityTest {
	@Test
	void everyShippedEntityTextureHasVisiblePixels() throws Exception {
		List<String> blank = new ArrayList<>();
		Path root = RepoPaths.ASSETS.resolve("textures/entity");
		try (Stream<Path> files = Files.walk(root)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".png")).toList()) {
				String shown = file.toString().replace('\\', '/');
				if (shown.contains("/old/") || shown.endsWith("_glow.png") || shown.endsWith("emissive.png")) continue;
				BufferedImage image = ImageIO.read(file.toFile());
				if (image != null && !hasVisiblePixel(image)) blank.add(RepoPaths.display(file));
			}
		}
		assertEquals(List.of(), blank, "fully transparent entity textures");
	}

	private static boolean hasVisiblePixel(BufferedImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getRGB(x, y) >>> 24) != 0) return true;
			}
		}
		return false;
	}
}
