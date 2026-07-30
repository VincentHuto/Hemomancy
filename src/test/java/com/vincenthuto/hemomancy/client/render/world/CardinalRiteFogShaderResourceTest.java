package com.vincenthuto.hemomancy.client.render.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteFogShaderResourceTest {
	private static final String ROOT = "/assets/hemomancy/shaders/core/world/cardinal_rite_fog";

	@Test
	void shaderProgramShipsWithItsVertexAndFragmentStages() {
		JsonObject program = readJson(ROOT + ".json");

		assertEquals("hemomancy:world/cardinal_rite_fog", program.get("vertex").getAsString());
		assertEquals("hemomancy:world/cardinal_rite_fog", program.get("fragment").getAsString());
		assertResourceExists(ROOT + ".vsh");
		assertResourceExists(ROOT + ".fsh");
	}

	@Test
	void shaderProgramExposesTheFogAnimationContract() {
		JsonObject program = readJson(ROOT + ".json");
		Set<String> uniforms = StreamSupport.stream(
						program.getAsJsonArray("uniforms").spliterator(), false)
				.map(element -> element.getAsJsonObject().get("name").getAsString())
				.collect(Collectors.toSet());

		assertTrue(uniforms.containsAll(Set.of("HemoTime", "FogSeed")));
		assertFalse(uniforms.contains("FogLayer"));
		assertFalse(uniforms.contains("FogDensity"));
		assertFalse(uniforms.contains("FogBaseCoverage"));
		program.getAsJsonArray("uniforms").forEach(element -> {
			JsonObject uniform = element.getAsJsonObject();
			assertEquals(uniform.get("count").getAsInt(),
					uniform.getAsJsonArray("values").size(),
					"wrong default value count for " + uniform.get("name").getAsString());
		});
	}

	private static JsonObject readJson(String path) {
		InputStream stream = CardinalRiteFogShaderResourceTest.class.getResourceAsStream(path);
		assertNotNull(stream, "missing resource " + path);
		return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
	}

	private static void assertResourceExists(String path) {
		assertNotNull(CardinalRiteFogShaderResourceTest.class.getResource(path), "missing resource " + path);
	}
}
