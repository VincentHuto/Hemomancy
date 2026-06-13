package com.vincenthuto.hemomancy.common.resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PhaseFiveCurorLensResourceTest {
	private static final Path ROOT = Path.of("src/main/resources");
	private static final Path DATA = ROOT.resolve("data/hemomancy");
	private static final Path ASSETS = ROOT.resolve("assets/hemomancy");

	private PhaseFiveCurorLensResourceTest() {
	}

	public static void main(String[] args) {
		require(ASSETS.resolve("models/item/curor_lens.json"));
		require(ASSETS.resolve("textures/item/curor_lens.png"));
		requireTransparentAperture(ASSETS.resolve("textures/item/curor_lens.png"));
		require(ASSETS.resolve("textures/item/curor_lens_glass.png"));
		requireTransparentAperture(ASSETS.resolve("textures/item/curor_lens_glass.png"));
		require(ASSETS.resolve("textures/gui/curor_lens_overlay.png"));
		requireSeeThroughOverlay(ASSETS.resolve("textures/gui/curor_lens_overlay.png"));
		requireTransparentAperture(ASSETS.resolve("textures/gui/curor_lens_overlay.png"));
		require(DATA.resolve("recipe/curor_lens.json"));
	}

	private static void require(Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
	}

	private static void requireSeeThroughOverlay(Path path) {
		try {
			BufferedImage image = ImageIO.read(path.toFile());
			if (image == null) {
				throw new AssertionError("unreadable " + path);
			}
			int transparent = 0;
			int veryLightTint = 0;
			int total = image.getWidth() * image.getHeight();
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int alpha = image.getRGB(x, y) >>> 24;
					if (alpha == 0) {
						transparent++;
					}
					if (alpha <= 48) {
						veryLightTint++;
					}
				}
			}
			if (transparent < total / 3) {
				throw new AssertionError("curor lens overlay must contain transparent space");
			}
			if (veryLightTint < total * 3 / 4) {
				throw new AssertionError("curor lens overlay tint is too opaque");
			}
		} catch (IOException e) {
			throw new AssertionError("failed to read " + path, e);
		}
	}

	private static void requireTransparentAperture(Path path) {
		try {
			BufferedImage image = ImageIO.read(path.toFile());
			if (image == null) {
				throw new AssertionError("unreadable " + path);
			}
			int transparent = 0;
			int sampled = 0;
			for (int y = image.getHeight() / 4; y < image.getHeight() * 3 / 4; y++) {
				for (int x = image.getWidth() / 4; x < image.getWidth() * 3 / 4; x++) {
					sampled++;
					int alpha = image.getRGB(x, y) >>> 24;
					if (alpha == 0) {
						transparent++;
					}
				}
			}
			if (transparent < sampled * 3 / 4) {
				throw new AssertionError("curor lens item aperture is not transparent enough: " + path);
			}
		} catch (IOException e) {
			throw new AssertionError("failed to read " + path, e);
		}
	}
}
