package com.vincenthuto.hemomancy.client.screen.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class BloodFillPixelsTest {
    @Test
    void fillHeightControlsVisibleLiquidRows() {
        int[] empty = BloodFillPixels.render(0, 0, 0, 0.0f);
        int[] half = BloodFillPixels.render(46, 0, 0, 0.0f);
        int[] full = BloodFillPixels.render(92, 0, 0, 0.0f);

        assertEquals(25 * 92, full.length);
        assertEquals(0, empty[60 * 25 + 12]);
        assertEquals(0, half[20 * 25 + 12]);
        assertNotEquals(0, half[60 * 25 + 12]);
        assertNotEquals(0, full[20 * 25 + 12]);
        assertEquals(0, full[0]);
    }

    @Test
    void communionCorruptionChangesBloodColor() {
        int[] clear = BloodFillPixels.render(46, 7, 0, 1.0f);
        int[] corrupted = BloodFillPixels.render(46, 7, 9, 1.0f);
        assertNotEquals(clear[60 * 25 + 12], corrupted[60 * 25 + 12]);
    }

    @Test
    void fullFillTopMatchesStaticVesselTextures() throws Exception {
        int[] procedural = BloodFillPixels.render(92, 7, 0, 0.0f);
        assertNotEquals(0, procedural[3]);
        assertNotEquals(0, procedural[21]);

        Path root = Path.of("src/main/resources/assets/hemomancy/textures/gui/blood_overlay");
        for (int frame = -1; frame < 16; frame++) {
            String name = frame < 0 ? "vessel_back.png" : "fill_apotheos_" + frame + ".png";
            BufferedImage texture = ImageIO.read(root.resolve(name).toFile());
            assertEquals(76, texture.getWidth());
            assertEquals(126, texture.getHeight());
            for (int row = 0; row <= 10; row++) {
                for (int col = 0; col < 25; col++) {
                    boolean liquid = procedural[row * 25 + col] != 0;
                    boolean sprite = (texture.getRGB(26 + col, 26 + row) >>> 24) != 0;
                    assertEquals(liquid, sprite, name + " at " + col + "," + row);
                }
            }
        }
    }

    @Test
    void upperFrameShoulderDoesNotCoverFillWithSilverPixels() throws Exception {
        Path root = Path.of("src/main/resources/assets/hemomancy/textures/gui/blood_overlay");
        int[][] shoulder = {{28, 28}, {27, 29}, {27, 30}};
        for (int degree = 0; degree <= 8; degree++) {
            BufferedImage frame = ImageIO.read(root.resolve("base_degree_" + degree + ".png").toFile());
            for (int[] point : shoulder) {
                int color = frame.getRGB(point[0], point[1]);
                int red = (color >>> 16) & 0xFF;
                int green = (color >>> 8) & 0xFF;
                assertNotEquals(0, color >>> 24, "frame " + degree + " at " + point[0] + "," + point[1]);
                assertTrue(red > green * 2,
                        "silver gap in frame " + degree + " at " + point[0] + "," + point[1]);
            }
        }
    }
}
