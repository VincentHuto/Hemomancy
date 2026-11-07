package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The colour grammar in her textures: white vessel light and gold nerve light never mix, and no texel of her base atlas
 * reads as copper, brass or gilt (no warm metal on a blood-side body).
 */
class SeraphaeTexturePaletteTest {
    private static final Path TEXTURES = ResourceReader.path("assets/hemomancy/textures/entity/boss/seraphae");

    private static BufferedImage read(String name) throws Exception {
        return ImageIO.read(TEXTURES.resolve(name).toFile());
    }

    private static float[] hsb(int argb) {
        return Color.RGBtoHSB((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, null);
    }

    @Test void theVesselMaskIsNearWhiteOnly() throws Exception {
        BufferedImage mask = read("seraphae_vessels.png");
        int lit = 0;
        for (int y = 0; y < mask.getHeight(); y++) for (int x = 0; x < mask.getWidth(); x++) {
            int argb = mask.getRGB(x, y);
            if ((argb >>> 24) == 0) continue;
            lit++;
            assertTrue(hsb(argb)[1] < 0.12F, "vessel light is white, never gold, at " + x + "," + y);
        }
        assertTrue(lit > 0);
    }

    @Test void theNerveMaskIsGoldOnly() throws Exception {
        BufferedImage mask = read("seraphae_nerves.png");
        int lit = 0;
        for (int y = 0; y < mask.getHeight(); y++) for (int x = 0; x < mask.getWidth(); x++) {
            int argb = mask.getRGB(x, y);
            if ((argb >>> 24) == 0) continue;
            lit++;
            float[] c = hsb(argb);
            float hue = c[0] * 360.0F;
            assertTrue(hue >= 35.0F && hue <= 50.0F && c[1] > 0.3F, "nerve light is gold at " + x + "," + y + ": " + hue);
        }
        assertTrue(lit > 0);
    }

    @Test void noWarmMetalTexelInTheBaseAtlas() throws Exception {
        BufferedImage base = read("seraphae.png");
        for (int y = 0; y < base.getHeight(); y++) for (int x = 0; x < base.getWidth(); x++) {
            int argb = base.getRGB(x, y);
            if ((argb >>> 24) == 0) continue;
            float[] c = hsb(argb);
            float hue = c[0] * 360.0F;
            assertFalse(hue >= 15.0F && hue <= 40.0F && c[1] > 0.35F && c[2] < 0.75F,
                    "copper or brass texel at " + x + "," + y);
        }
    }
}
