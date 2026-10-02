package com.vincenthuto.hemomancy.client.screen.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class BloodFillPixelsTest {
    private static final Path ROOT = Path.of("src/main/resources/assets/hemomancy/textures/gui/blood_overlay");

    @Test
    void fillHeightControlsVisibleLiquidRows() throws Exception {
        BufferedImage back = texture("vessel_back.png");
        int[] empty = render(back, 0, 0, 0, 0);
        int[] half = render(back, 0.5, 0, 0, 0);
        int[] full = render(back, 1, 0, 0, 0);
        assertEquals(back.getWidth() * back.getHeight(), full.length);
        assertEquals(0, empty[90 * back.getWidth() + 32]);
        assertEquals(0, half[40 * back.getWidth() + 32]);
        assertNotEquals(0, half[90 * back.getWidth() + 32]);
        assertNotEquals(0, full[40 * back.getWidth() + 32]);
        assertEquals(0, full[0]);
    }

    @Test
    void communionCorruptionChangesBloodColor() throws Exception {
        BufferedImage back = texture("vessel_back.png");
        int[] clear = render(back, 0.5, 7, 0, 1);
        int[] corrupted = render(back, 0.5, 7, 9, 1);
        assertNotEquals(clear[90 * back.getWidth() + 32], corrupted[90 * back.getWidth() + 32]);
    }

    @Test
    void fullFillCoversTheAuthoredVisibleWindow() throws Exception {
        BufferedImage back = texture("vessel_back.png");
        for (int degree = 0; degree < 8; degree++) {
            BufferedImage frame = texture("base_degree_" + degree + ".png");
            int[] fill = render(back, 1, degree, degree == 7 ? 9 : 0, 0);
            for (int y = 0; y < back.getHeight(); y++) {
                for (int x = 0; x < back.getWidth(); x++) {
                    if ((back.getRGB(x, y) >>> 24) == 0 || (frame.getRGB(x, y) >>> 24) != 0) continue;
                    assertNotEquals(0, fill[y * back.getWidth() + x],
                            "Unfilled visible window in degree " + degree + " at " + x + "," + y);
                }
            }
        }
    }

    @Test
    void fillNeverEscapesTheBackingMaskAtAnyVolume() throws Exception {
        BufferedImage back = texture("vessel_back.png");
        for (double ratio : new double[]{0, 0.01, 0.25, 0.5, 0.99, 1}) {
            int[] fill = render(back, ratio, 7, 9, 2);
            for (int y = 0; y < back.getHeight(); y++) {
                for (int x = 0; x < back.getWidth(); x++) {
                    if ((back.getRGB(x, y) >>> 24) == 0)
                        assertEquals(0, fill[y * back.getWidth() + x], "Escaped backing at " + x + "," + y);
                }
            }
        }
    }

    @Test
    void apotheosFramesCoverTheAuthoredVisibleWindow() throws Exception {
        BufferedImage back = texture("vessel_back.png");
        BufferedImage frame = texture("base_degree_8.png");
        for (int index = 0; index < 16; index++) {
            BufferedImage fill = texture("fill_apotheos_" + index + ".png");
            assertEquals(back.getWidth(), fill.getWidth());
            assertEquals(back.getHeight(), fill.getHeight());
            for (int y = 0; y < back.getHeight(); y++) {
                for (int x = 0; x < back.getWidth(); x++) {
                    if ((back.getRGB(x, y) >>> 24) == 0 || (frame.getRGB(x, y) >>> 24) != 0) continue;
                    assertNotEquals(0, fill.getRGB(x, y) >>> 24,
                            "Unfilled Apotheos window in frame " + index + " at " + x + "," + y);
                }
            }
        }
    }

    @Test
    void degreeFramesRetainTheirOpenInteriorWithoutARequiredPalette() throws Exception {
        BufferedImage back = texture("vessel_back.png");
        assertEquals(64, back.getWidth());
        assertEquals(128, back.getHeight());
        for (int degree = 0; degree <= 8; degree++) {
            BufferedImage frame = texture("base_degree_" + degree + ".png");
            assertEquals(back.getWidth(), frame.getWidth());
            assertEquals(back.getHeight(), frame.getHeight());
            assertEquals(0, frame.getRGB(32, 65) >>> 24, "Degree frame obstructed the middle of the vessel");
            assertNotEquals(0, frame.getRGB(21, 65) >>> 24, "Degree frame lost its interior border");
        }
    }

    @Test
    void emptyMaskRendersNoBlood() {
        int[] pixels = BloodFillPixels.render(new boolean[9], 3, 3, 1, 0, 0, 0);
        for (int pixel : pixels) assertEquals(0, pixel);
    }

    private static BufferedImage texture(String name) throws Exception {
        BufferedImage image = ImageIO.read(ROOT.resolve(name).toFile());
        assertTrue(image != null, "Unreadable blood HUD texture: " + name);
        return image;
    }

    private static int[] render(BufferedImage back, double ratio, int degree, int pomes, float time) {
        boolean[] mask = new boolean[back.getWidth() * back.getHeight()];
        for (int y = 0; y < back.getHeight(); y++) {
            for (int x = 0; x < back.getWidth(); x++) mask[y * back.getWidth() + x] = (back.getRGB(x, y) >>> 24) != 0;
        }
        return BloodFillPixels.render(mask, back.getWidth(), back.getHeight(), ratio, degree, pomes, time);
    }
}
