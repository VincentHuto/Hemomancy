package com.vincenthuto.hemomancy.client.screen.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class SpecialGourdHudFillPixelsTest {
    private static final Path ROOT = Path.of("src/main/resources/assets/hemomancy/textures/gui/blood_overlay");

    @Test
    void bloodUsesEachFrameMaskAndVolume() throws Exception {
        for (String name : new String[] {"curved_horn", "hemorath_rib"}) {
            BufferedImage image = ImageIO.read(ROOT.resolve(name + "_fill_mask.png").toFile());
            boolean[] mask = new boolean[image.getWidth() * image.getHeight()];
            int top = -1;
            int bottom = -1;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) >>> 24) > 127) {
                        mask[y * image.getWidth() + x] = true;
                        if (top < 0) top = y * image.getWidth() + x;
                        bottom = y * image.getWidth() + x;
                    }
                }
            }
            int[] empty = SpecialGourdHudFillPixels.render(mask, image.getHeight(), 0, 0, 0, 0);
            int[] half = SpecialGourdHudFillPixels.render(mask, image.getHeight(), 0.5, 0, 0, 0);
            int[] full = SpecialGourdHudFillPixels.render(mask, image.getHeight(), 1, 0, 0, 0);
            assertEquals(32 * 64, full.length);
            assertEquals(0, empty[bottom]);
            assertEquals(0, half[top]);
            assertNotEquals(0, half[bottom]);
            assertNotEquals(0, full[top]);
            for (int i = 0; i < full.length; i++) {
                if (i < mask.length && !mask[i]) assertEquals(0, full[i]);
            }
        }
    }
}
