package com.vincenthuto.hemomancy.client.render.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class QliphothMonolithCapShaderTest {

	@Test
	void prunedCapDisablesMonolithVertexWarpWithoutChangingOtherMonolithSurfaces() throws IOException {
		JsonObject program = JsonParser.parseString(Files.readString(Path.of(
				"src/main/resources/assets/hemomancy/shaders/core/item/monolith_fragment.json"))).getAsJsonObject();
		JsonObject vertexWarp = StreamSupport.stream(program.getAsJsonArray("uniforms").spliterator(), false)
				.map(element -> element.getAsJsonObject())
				.filter(uniform -> "VertexWarp".equals(uniform.get("name").getAsString()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("monolith shader is missing its VertexWarp control"));
		assertEquals(1.0f, vertexWarp.getAsJsonArray("values").get(0).getAsFloat(), 0.0001f,
				"ordinary monolith surfaces must retain their existing vertex animation");

		String vertexShader = Files.readString(Path.of(
				"src/main/resources/assets/hemomancy/shaders/core/item/monolith_fragment.vsh"));
		assertTrue(vertexShader.contains("VERTEX_MORPH_STRENGTH * VertexWarp"),
				"the control must scale the actual positional morph");

		String renderTypes = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java"));
		assertTrue(renderTypes.contains("setUniform(shader, \"VertexWarp\", vertexWarp)"),
				"the requested warp strength must reach the shader");

		String qliphothRenderer = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/render/world/QliphothBloomRenderer.java"));
		assertTrue(qliphothRenderer.contains("monolithFragmentRigid("),
				"the pruned cap must select the rigid monolith material variant");
	}
}
