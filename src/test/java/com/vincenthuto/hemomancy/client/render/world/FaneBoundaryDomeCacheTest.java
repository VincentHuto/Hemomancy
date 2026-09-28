package com.vincenthuto.hemomancy.client.render.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FaneBoundaryDomeCacheTest {
    @Test
    void memberDomeDrawDoesNotRebuildItsMeshEachFrame() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/client/render/world/FaneBoundaryRenderer.java"));
        int start = source.indexOf("private static void drawDome(");
        int end = source.indexOf("private static float rivalPulse(", start);
        assertTrue(start >= 0 && end > start);

        String drawDome = source.substring(start, end);
        assertFalse(drawDome.contains("Tesselator.getInstance().begin"));
        assertFalse(drawDome.contains("BufferUploader.drawWithShader"));
        assertTrue(drawDome.contains("drawWithShader"));
    }
}
