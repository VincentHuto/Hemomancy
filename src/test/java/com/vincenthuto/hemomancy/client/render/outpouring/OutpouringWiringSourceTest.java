package com.vincenthuto.hemomancy.client.render.outpouring;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** The outpouring is fed by the casting packets, anchored by the third-person layer and drawn after particles. */
class OutpouringWiringSourceTest {
    private static final Path ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy");

    private static String read(String path) throws IOException { return Files.readString(ROOT.resolve(path)); }

    @Test void packetsFeedTheOutpouring() throws IOException {
        String state = read("client/player/CastingAnimationClientState.java");
        assertTrue(state.contains("OutpouringClientState.accept(packet)"));
        assertTrue(state.contains("OutpouringClientState.clear()"));
    }

    @Test void thirdPersonLayerCapturesExits() throws IOException {
        assertTrue(read("client/render/layer/player/CastingAnimationLayer.java").contains("OutpouringAnchors.capture("));
    }

    @Test void rendererDrawsAfterParticles() throws IOException {
        String renderer = read("client/render/outpouring/OutpouringRenderer.java");
        assertTrue(renderer.contains("Stage.AFTER_PARTICLES"));
    }

    @Test void bloodDrawsThroughTheManipulationToolkit() throws IOException {
        String renderer = read("client/render/outpouring/OutpouringRenderer.java");
        assertTrue(renderer.contains("OutpouringBrushes.flush("), "the toolkit batch sorts and flushes the blood");
        assertTrue(renderer.contains("HemoRenderTypes.bloodPool("), "Animus pools are brazier blood");
        assertFalse(Files.exists(ROOT.getParent().getParent().getParent().getParent().resolve(
                "resources/assets/hemomancy/shaders/core/world/cast_outpouring.fsh")), "no private material shader");
    }

    @Test void noVanillaParticlesInTheOutpouring() throws IOException {
        try (Stream<Path> files = Files.list(ROOT.resolve("client/render/outpouring"))) {
            for (Path file : files.toList())
                assertFalse(Files.readString(file).contains("ParticleTypes."), file.getFileName() + " uses vanilla particles");
        }
    }
}
