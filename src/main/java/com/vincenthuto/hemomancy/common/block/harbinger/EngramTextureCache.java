package com.vincenthuto.hemomancy.common.block.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches pixel data for each engram texture (a-z, indices 0-25). For each
 * engram, stores a boolean[16][16] grid (true = non-transparent pixel) and an
 * int[16][16] ARGB color grid for particle tinting. Textures are loaded
 * directly from the mod's classpath resources.
 */
public class EngramTextureCache {

	private static final Map<Integer, boolean[][]> PIXEL_CACHE = new HashMap<>();
	private static final Map<Integer, int[][]> COLOR_CACHE = new HashMap<>();
	private static final String[] ENGRAM_NAMES = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
			"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

	/**
	 * Loads all engram textures from the classpath and extracts pixel opacity +
	 * color data. Safe to call from either side. Should be called once on server
	 * start.
	 */
	public static void loadAll() {
		if (isLoaded()) {
			return; // Already loaded
		}
		PIXEL_CACHE.clear();
		COLOR_CACHE.clear();
		for (int i = 0; i < ENGRAM_NAMES.length; i++) {
			String path = "/assets/" + Hemomancy.MOD_ID + "/textures/block/engrams/engram_" + ENGRAM_NAMES[i] + ".png";
			try (InputStream stream = EngramTextureCache.class.getResourceAsStream(path)) {
				if (stream != null) {
					BufferedImage image = ImageIO.read(stream);
					boolean[][] pixels = new boolean[16][16];
					int[][] colors = new int[16][16];
					extractPixelData(image, pixels, colors);
					PIXEL_CACHE.put(i, pixels);
					COLOR_CACHE.put(i, colors);
					Hemomancy.LOGGER.debug("Loaded engram texture for index {} ({}): {}x{}", i, ENGRAM_NAMES[i],
							image.getWidth(), image.getHeight());
				} else {
					Hemomancy.LOGGER.warn("Engram texture not found on classpath: {}", path);
				}
			} catch (IOException e) {
				Hemomancy.LOGGER.error("Failed to load engram texture: {}", path, e);
			}
		}
		Hemomancy.LOGGER.info("EngramTextureCache loaded {} engram textures", PIXEL_CACHE.size());
	}

	/**
	 * Extracts a 16x16 grid of transparency and color data from the image. If the
	 * texture is not exactly 16x16, it scales sampling accordingly.
	 */
	private static void extractPixelData(BufferedImage image, boolean[][] pixels, int[][] colors) {
		int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();

		for (int px = 0; px < 16; px++) {
			for (int py = 0; py < 16; py++) {
				int sampleX = Math.min((px * imgWidth) / 16, imgWidth - 1);
				int sampleY = Math.min((py * imgHeight) / 16, imgHeight - 1);

				int argb = image.getRGB(sampleX, sampleY);
				int alpha = (argb >> 24) & 0xFF;
				pixels[px][py] = alpha > 0;
				colors[px][py] = argb;
			}
		}
	}

	/**
	 * Gets the cached transparency data for the given character index (0-25).
	 *
	 * @param characterIndex The CHARACTERINDEX blockstate value (0=a, 25=z)
	 * @return boolean[x][y] grid where true = non-transparent pixel, or null if not
	 *         loaded
	 */
	public static boolean[][] getPixels(int characterIndex) {
		return PIXEL_CACHE.get(characterIndex);
	}

	/**
	 * Gets the cached ARGB color data for the given character index (0-25).
	 *
	 * @param characterIndex The CHARACTERINDEX blockstate value (0=a, 25=z)
	 * @return int[x][y] grid of ARGB color values, or null if not loaded
	 */
	public static int[][] getColors(int characterIndex) {
		return COLOR_CACHE.get(characterIndex);
	}

	/**
	 * @return true if the cache has been populated
	 */
	public static boolean isLoaded() {
		return !PIXEL_CACHE.isEmpty();
	}

	/**
	 * Clears the cache.
	 */
	public static void clear() {
		PIXEL_CACHE.clear();
		COLOR_CACHE.clear();
	}
}

