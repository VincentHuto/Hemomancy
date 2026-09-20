package com.vincenthuto.hemomancy.client.screen.overlay;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class MicroscopeSpecialSampleVisualSourceTest {
    private static final Path ROOT = Path.of(System.getProperty("user.dir"));
    private static final Path TEXTURES = ROOT.resolve("src/main/resources/assets/hemomancy/textures/gui/microscope");

    @Test
    void specialSamplesUseExplicitSpecimenRulesAndAuthoredSprites() throws Exception {
        String samples = source("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodSampleData.java");
        String overlay = source("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/HematicMicroscopeOverlay.java");

        assertTrue(samples.contains("CleansingHemolymphItem"));
        assertTrue(samples.contains("ConsecratedSyringeItem.getSaintType(stack) != null"));
        assertTrue(overlay.contains("MicroscopeSpecimenVisual.of(sample)"));
        assertTrue(overlay.contains("drawHemolymph"));
        assertTrue(overlay.contains("drawSaintBlood"));

        for (String sprite : List.of(
                "special/hemolymph_cell.png", "special/hemolymph_spot.png", "special/hemolymph_filament.png",
                "special/hemorath_iron.png", "special/hemorath_void.png",
                "special/seraphae_halo.png", "special/seraphae_mote.png",
                "special/putriciel_ember.png", "special/putriciel_spore.png",
                "special/velorum_crystal.png", "special/velorum_shadow.png")) {
            Path path = TEXTURES.resolve(sprite);
            assertTrue(Files.isRegularFile(path), () -> "Missing authored microscope sprite: " + sprite);
            var image = ImageIO.read(path.toFile());
            assertNotNull(image, () -> "Unreadable microscope sprite: " + sprite);
            assertTrue(image.getWidth() <= 32 && image.getHeight() <= 32,
                    () -> "Floating microscope sprites must stay native-grid sized: " + sprite);
        }
    }

    @Test
    void specialSampleDrawingDoesNotPaintRuntimePrimitives() throws Exception {
        String overlay = source("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/HematicMicroscopeOverlay.java");
        int start = overlay.indexOf("private static void drawHemolymph");
        int end = overlay.indexOf("private static void drawSlide", start);
        assertTrue(start >= 0 && end > start, "special drawing methods must precede drawSlide");
        String specialDrawing = overlay.substring(start, end);
        assertFalse(specialDrawing.contains("gfx.fill("));
        assertFalse(specialDrawing.contains("fillGradient("));
        assertTrue(specialDrawing.contains("sprite(gfx"));
    }

    private static String source(String relative) throws Exception {
        return Files.readString(ROOT.resolve(relative)).replace("\r\n", "\n");
    }
}
