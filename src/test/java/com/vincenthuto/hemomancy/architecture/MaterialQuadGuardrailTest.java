package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 10 (roadmap Phase 8, V-3): the school and rite materials write their encoded-UV vertices through
 * {@code MaterialQuads}. No other class wraps a seed into the shaders' tiles by hand, and the material geometries
 * set no UVs of their own. Ferric wraps into the shared 127 tiles (V-3b) and the Outpouring surface marks write
 * through the encoder too (V-3c).
 */
class MaterialQuadGuardrailTest {
	private static final Pattern SEED_WRAP = Pattern.compile("floorMod\\([^;]*,\\s*12[78]\\s*\\)");
	private static final String ENCODER = "client/render/world/MaterialQuads.java";
	private static final List<String> GEOMETRIES = List.of("client/render/world/LuxUmbraGeometry.java",
			"client/render/world/ThermalGeometry.java", "client/render/world/FerricDuctilisGeometry.java",
			"client/render/world/rite/RiteSurface.java");
	/** Writers of the encoding that cannot use the encoder without changing their output. */
	private static final Map<String, String> HAND_WRITTEN = Map.of();

	@Test
	void onlyTheEncoderWrapsSeeds() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : sources()) {
			if (file.equals(RepoPaths.mainJava(ENCODER))) continue;
			if (SEED_WRAP.matcher(SourceAssertions.read(file)).find()) found.add(RepoPaths.display(file));
		}
		assertEquals(Set.of(), found, "wrap a seed with MaterialQuads.seedTile");
		assertTrue(SourceAssertions.readMainJava(ENCODER).contains("public static final int SEED_TILES = 127;"));
		String ferric = SourceAssertions.readMainJava("client/render/world/FerricDuctilisGeometry.java");
		assertTrue(!ferric.contains("FERRIC_SEED_TILES") && ferric.contains("MaterialQuads.seedTile("),
				"Ferric wraps its seeds into the shared 127 tiles (V-3b)");
	}

	@Test
	void materialGeometriesWriteThroughTheEncoder() {
		for (String geometry : GEOMETRIES) {
			String text = SourceAssertions.readMainJava(geometry);
			assertTrue(text.contains("MaterialQuads.vertex(") || text.contains("MaterialQuads.quad("), geometry);
			assertTrue(!text.contains(".setUv("), geometry + " sets its own UVs; write through MaterialQuads");
		}
	}

	@Test
	void handWrittenEncodingsAreListed() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : sources()) {
			String text = SourceAssertions.read(file);
			if (text.contains("MaterialQuads.seedTile(") && text.contains(".setUv(")) found.add(relative(file));
		}
		assertEquals(new TreeSet<>(HAND_WRITTEN.keySet()), found,
				"write the vertices through MaterialQuads, or list the writer here with why it cannot");
	}

	private static String relative(Path file) {
		return RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
	}

	private static List<Path> sources() throws IOException {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.toString().endsWith(".java")).toList();
		}
	}
}
