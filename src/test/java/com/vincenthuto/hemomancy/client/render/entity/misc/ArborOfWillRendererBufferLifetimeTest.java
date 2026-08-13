package com.vincenthuto.hemomancy.client.render.entity.misc;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ArborOfWillRendererBufferLifetimeTest {
    @Test
    void rendererDoesNotReuseCoreConsumerAfterSwitchingRenderTypes() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/client/render/entity/misc/ArborOfWillRenderer.java"));
        assertFalse(source.contains("VertexConsumer core = buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);"),
                "A cached BufferSource consumer becomes invalid when the renderer switches to the glow type");
    }
}
