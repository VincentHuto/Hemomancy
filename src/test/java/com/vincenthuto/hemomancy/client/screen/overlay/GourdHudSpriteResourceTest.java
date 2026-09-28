package com.vincenthuto.hemomancy.client.screen.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class GourdHudSpriteResourceTest {
    private static final Path ROOT = Path.of("src/main/resources/assets/hemomancy/textures/gui/blood_overlay");

    @Test
    void shellsLeaveTheBloodWindowOpenAndVariantsRemainDistinct() throws Exception {
        BufferedImage back = image("gourd_back.png");
        BufferedImage white = image("gourd_frame_white.png");
        BufferedImage red = image("gourd_frame_red.png");
        BufferedImage black = image("gourd_frame_black.png");
        BufferedImage halo = image("gourd_halo.png");
        BufferedImage source = ImageIO.read(Path.of("tools/art_source/gourd_hud_concept.png").toFile());

        for (BufferedImage image : new BufferedImage[] {back, white, red, black}) {
            assertEquals(32, image.getWidth());
            assertEquals(64, image.getHeight());
        }
        assertEquals(source.getRGB(8, 24), white.getRGB(8, 24));
        assertEquals(0, white.getRGB(16, 27) >>> 24);
        assertEquals(0, red.getRGB(16, 27) >>> 24);
        assertEquals(0, black.getRGB(16, 27) >>> 24);
        assertTrue(back.getRGB(16, 27) >>> 24 > 0);
        assertEquals(0, back.getRGB(0, 0) >>> 24);
        assertEquals(0, white.getRGB(3, 10) >>> 24);
        for (BufferedImage frame : new BufferedImage[] {white, red, black}) {
            assertEquals(0, frame.getRGB(4, 9) >>> 24);
            assertEquals(0, frame.getRGB(27, 59) >>> 24);
            for (int y = 0; y < frame.getHeight(); y++) {
                for (int x = 0; x < frame.getWidth(); x++) {
                    int alpha = frame.getRGB(x, y) >>> 24;
                    assertTrue(alpha == 0 || alpha >= 16);
                }
            }
        }
        assertNotEquals(white.getRGB(8, 24), red.getRGB(8, 24));
        assertNotEquals(red.getRGB(8, 24), black.getRGB(8, 24));
        assertEquals(white.getRGB(8, 24) >>> 24, red.getRGB(8, 24) >>> 24);
        assertEquals(white.getRGB(8, 24) >>> 24, black.getRGB(8, 24) >>> 24);
        assertEquals(44, halo.getWidth());
        assertEquals(76, halo.getHeight());
        assertTrue(halo.getRGB(22, 38) >>> 24 >= 110);
        assertTrue(halo.getRGB(22, 38) >>> 24 <= 120);
        assertEquals(0, halo.getRGB(3, 38) >>> 24);
        assertEquals(0, halo.getRGB(9, 16) >>> 24);
        for (int y = 0; y < halo.getHeight(); y++) {
            for (int x = 0; x < halo.getWidth(); x++) {
                int pixel = halo.getRGB(x, y);
                if ((pixel >>> 24) != 0) {
                    assertEquals(0xB41C1E, pixel & 0xFFFFFF);
                }
            }
        }
    }

    private static BufferedImage image(String name) throws Exception {
        return ImageIO.read(ROOT.resolve(name).toFile());
    }
}
