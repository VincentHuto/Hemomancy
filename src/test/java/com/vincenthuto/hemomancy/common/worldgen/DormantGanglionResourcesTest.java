package com.vincenthuto.hemomancy.common.worldgen;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.support.ResourceReader;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DormantGanglionResourcesTest {
    @Test
    void awakeningSiteHasNativeGridAnimationAndNoLoot() throws Exception {
        var texture = ResourceReader.path("assets/hemomancy/textures/block/dormant_ganglion.png");
        assertTrue(Files.isRegularFile(texture), "the opt-in block needs its own readable texture");
        var image = ImageIO.read(texture.toFile());
        assertEquals(16, image.getWidth());
        assertTrue(image.getHeight() > 16 && image.getHeight() % 16 == 0);
        var animation = JsonParser.parseString(Files.readString(texture.resolveSibling("dormant_ganglion.png.mcmeta")))
                .getAsJsonObject().getAsJsonObject("animation");
        assertTrue(animation.get("frametime").getAsInt() >= 10, "the dormant pulse should be slow");
        var loot = ResourceReader.data("loot_table/blocks/dormant_ganglion.json");
        assertTrue(loot.getAsJsonArray("pools").isEmpty(), "awakening and resetting must not farm drops");
    }
}
