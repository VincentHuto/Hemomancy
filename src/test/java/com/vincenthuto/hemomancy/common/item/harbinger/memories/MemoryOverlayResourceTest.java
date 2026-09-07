package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/** Native sprite and item/UI agreement checks; runnable without Minecraft bootstrap. */
public final class MemoryOverlayResourceTest {
	private static final Path JAVA = Path.of("src/main/java/com/vincenthuto/hemomancy");
	private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");

	public static void main(String[] args) throws Exception {
		Path blank = ASSETS.resolve("textures/item/memories/memory_blank.png");
		String blankHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(blank)));
		check(blankHash.equals("4c7aeca95b8fbc0e276689603bef021596b4871f5fff7a69fd6bc663e064069f"), "authored blank changed");
		BufferedImage base = ImageIO.read(blank.toFile());
		Set<Integer> baseColors = new HashSet<>();
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				if ((base.getRGB(x, y) >>> 24) != 0) baseColors.add(base.getRGB(x, y));
			}
		}

		String registry = Files.readString(JAVA.resolve("common/init/ManipulationInit.java"));
		String retirement = Files.readString(JAVA.resolve("common/capability/player/harbinger/manip/ManipulationRetirementRules.java"));
		String retiredBlock = retirement.substring(retirement.indexOf("RETIRED_MANIPULATION_IDS"), retirement.indexOf("RETIRED_MEMORY_ITEM_IDS"));
		Set<String> retired = new HashSet<>();
		Pattern.compile("\"([^\"]+)\"").matcher(retiredBlock).results().forEach(m -> retired.add(m.group(1)));
		String resolver = Files.readString(JAVA.resolve("client/screen/manips/ManipulationIconResolver.java"));
		Map<String, String> aliases = new HashMap<>();
		Pattern.compile("case \"([^\"]+)\" -> \"([^\"]+)\"").matcher(resolver).results()
				.forEach(m -> aliases.put(m.group(1), m.group(2)));
		check(resolver.contains("default -> \"memory_\" + manipulationId + \"_overlay\""), "canonical overlay fallback missing");
		Set<String> silhouettes = new HashSet<>();
		Set<String> textures = new HashSet<>();
		int count = 0;
		for (var match : Pattern.compile("MANIPS.register\\(\"([^\"]+)\"").matcher(registry).results().toList()) {
			String id = match.group(1);
			if (retired.contains(id)) continue;
			String stem = "memory_" + (id.startsWith("conjure_") ? id.replace("conjure_", "living_") : id);
			String texture = stem + "_overlay";
			check(aliases.getOrDefault(id, "memory_" + id + "_overlay").equals(texture), id + " UI borrows another ability's overlay");
			check(textures.add(texture), id + " shares a texture");
			Path file = ASSETS.resolve("textures/item/memories/" + texture + ".png");
			check(Files.exists(file), id + " overlay missing");
			BufferedImage overlay = ImageIO.read(file.toFile());
			check(overlay.getWidth() == 16 && overlay.getHeight() == 16, id + " must be 16x16");
			Set<Integer> colors = new HashSet<>(baseColors);
			StringBuilder silhouette = new StringBuilder();
			int opaque = 0;
			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < 16; x++) {
					int rgb = overlay.getRGB(x, y);
					int alpha = rgb >>> 24;
					check(alpha == 0 || alpha == 255, id + " has partial alpha");
					silhouette.append(alpha == 0 ? '.' : '#');
					if (alpha == 0) continue;
					opaque++;
					check(x >= 4 && x <= 11 && y >= 4 && y <= 11
							&& !((x == 4 || x == 11) && (y == 4 || y == 11)), id + " overwrites the ring");
					colors.add(rgb);
				}
			}
			check(opaque >= 8, id + " has no readable glyph");
			check(colors.size() <= 12, id + " composite exceeds 12 colors");
			check(silhouettes.add(silhouette.toString()), id + " duplicates another silhouette");
			Path model = ASSETS.resolve("models/item/" + stem + ".json");
			if (id.equals("conjure_staff")) model = ASSETS.resolve("models/item/memory_conjure_living_staff.json");
			// Vesper teaches the sickle through its own item; only its manipulation icon uses this base.
			if (!id.equals("conjure_sickle")) {
				check(Files.exists(model), id + " item model missing");
				String json = Files.readString(model);
				check(json.contains("hemomancy:item/memories/memory_blank"), id + " lost the shared base");
				check(json.contains("hemomancy:item/memories/" + texture + "\""), id + " item/UI texture mismatch");
			}
			count++;
		}
		check(count >= 97, "current manipulation coverage unexpectedly fell");
		System.out.println("Validated " + count + " distinct memory overlays, their palettes, frame bounds, and item/UI references.");
	}

	private static void check(boolean condition, String message) {
		if (!condition) throw new AssertionError(message);
	}
}
