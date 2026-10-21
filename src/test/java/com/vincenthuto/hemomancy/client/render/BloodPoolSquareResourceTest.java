package com.vincenthuto.hemomancy.client.render;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** A cauldron's blood fills its square mouth, so the shared pool shader can trim to a square as well as a disc. */
class BloodPoolSquareResourceTest {
    private static final Path SHADERS = Path.of("src/main/resources/assets/hemomancy/shaders/core/world");
    private static final Path JAVA = Path.of("src/main/java/com/vincenthuto/hemomancy");

    @Test
    void theShaderDeclaresAndUsesSquare() throws IOException {
        assertTrue(Files.readString(SHADERS.resolve("blood_pool.json")).contains("\"Square\""));
        String fragment = Files.readString(SHADERS.resolve("blood_pool.fsh"));
        assertTrue(fragment.contains("uniform float Square;"), "fragment shader lacks the Square uniform");
        assertTrue(fragment.contains("max(abs(p.x), abs(p.y))"), "fragment shader never measures a square rim");
    }

    @Test
    void theCondenserDrawsASquarePoolOverItsWholeMouth() throws IOException {
        assertTrue(Files.readString(JAVA.resolve("common/init/ShaderInit.java")).contains("\"Ragged\", \"Square\""));
        String renderer = Files.readString(JAVA.resolve(
                "client/render/tile/harbinger/functional/SanguineCondenserRenderer.java"));
        assertTrue(renderer.contains("HemoRenderTypes.squareBloodPool("), "the Condenser still draws a round pool");
        assertTrue(renderer.contains("POOL_HALF_WIDTH = 6.0F / 16.0F"), "the pool must span the 12/16 cauldron mouth");
    }
}
