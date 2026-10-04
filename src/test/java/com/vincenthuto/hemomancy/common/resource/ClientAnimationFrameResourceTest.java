package com.vincenthuto.hemomancy.common.resource;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientAnimationFrameResourceTest {
    @Test void explicitFramesStayWithinTheirAuthoredPng() throws Exception {
        var problems = new ArrayList<String>();
        Path textures = Path.of("src/main/resources/assets/hemomancy/textures");
        try (var paths = Files.walk(textures)) {
            for (Path metadata : paths.filter(p -> p.toString().endsWith(".png.mcmeta")).toList()) {
                var definition = JsonParser.parseString(Files.readString(metadata)).getAsJsonObject();
                if (!definition.has("animation")) continue;
                var animation = definition.getAsJsonObject("animation");
                if (!animation.has("frames")) continue;
                Path png = Path.of(metadata.toString().substring(0, metadata.toString().length() - ".mcmeta".length()));
                var image = ImageIO.read(png.toFile());
                assertNotNull(image, png.toString());
                int width = image.getWidth(), height = image.getHeight();
                int frameWidth = animation.has("width") ? animation.get("width").getAsInt()
                        : animation.has("height") ? width : Math.min(width, height);
                int frameHeight = animation.has("height") ? animation.get("height").getAsInt()
                        : animation.has("width") ? height : Math.min(width, height);
                assertTrue(frameWidth > 0 && frameHeight > 0, metadata.toString());
                assertEquals(0, width % frameWidth, metadata.toString());
                assertEquals(0, height % frameHeight, metadata.toString());
                int count = (width / frameWidth) * (height / frameHeight);
                for (var frame : animation.getAsJsonArray("frames")) {
                    int index = frame.isJsonObject() ? frame.getAsJsonObject().get("index").getAsInt() : frame.getAsInt();
                    if (index < 0 || index >= count) problems.add(metadata + " index " + index + " outside " + count + " frames");
                }
            }
        }
        assertEquals(List.of(), problems);
    }
}
