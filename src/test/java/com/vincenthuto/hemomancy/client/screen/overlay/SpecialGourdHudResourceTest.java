package com.vincenthuto.hemomancy.client.screen.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class SpecialGourdHudResourceTest {
    private static final Path ROOT = Path.of("src/main/resources/assets/hemomancy/textures/gui/blood_overlay");

    @Test
    void curvedHornAndRibFramesHaveOpenBloodChannels() throws Exception {
        check("curved_horn", 48);
        check("hemorath_rib", 64);
    }

    private static void check(String name, int height) throws Exception {
        BufferedImage frame = image(name + "_frame.png");
        BufferedImage back = image(name + "_back.png");
        BufferedImage mask = image(name + "_fill_mask.png");
        BufferedImage halo = image(name + "_halo.png");
        for (BufferedImage image : new BufferedImage[] {frame, back, mask}) {
            assertEquals(32, image.getWidth());
            assertEquals(height, image.getHeight());
        }
        assertEquals(44, halo.getWidth());
        assertEquals(height + 12, halo.getHeight());

        int channelPixels = 0;
        int shellPixels = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < 32; x++) {
                int frameAlpha = frame.getRGB(x, y) >>> 24;
                assertTrue(frameAlpha == 0 || frameAlpha >= 16);
                if ((mask.getRGB(x, y) >>> 24) > 127) {
                    channelPixels++;
                    assertEquals(0, frameAlpha);
                    assertEquals(255, back.getRGB(x, y) >>> 24);
                } else if (frameAlpha > 200) {
                    shellPixels++;
                }
            }
        }
        assertTrue(channelPixels > 50);
        assertTrue(shellPixels > 100);
    }

    private static BufferedImage image(String name) throws Exception {
        return ImageIO.read(ROOT.resolve(name).toFile());
    }
}
