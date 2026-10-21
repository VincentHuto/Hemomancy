package com.vincenthuto.hemomancy.client.render.outpouring;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OutpouringScreenPassesTest {
    @Test void capsAtEightNearest() {
        List<double[]> entries = new ArrayList<>();
        for (int i = 12; i >= 1; i--) entries.add(new double[]{i, 0, 0, 1});
        var nearest = OutpouringScreenPasses.nearest(entries, 8);
        assertEquals(8, nearest.size());
        for (int i = 0; i < 8; i++) assertEquals(i + 1, nearest.get(i)[0]);
    }

    @Test void packIsColumnMajor() {
        List<double[]> entries = new ArrayList<>();
        for (int i = 0; i < 6; i++) entries.add(new double[]{i, i + .1, i + .2, i + .3});
        var packed = OutpouringScreenPasses.pack(entries);
        assertEquals(6, packed.count());
        assertEquals(0f, packed.values()[0]);
        assertEquals(.3f, packed.values()[3], 1e-6);
        assertEquals(4f, packed.values()[16]);
        assertEquals(4.3f, packed.values()[19], 1e-6);
    }

    @Test void emptyPackHasZeroCount() {
        assertEquals(0, OutpouringScreenPasses.pack(List.of()).count());
    }

    @Test void shadeInfoPairsStrengthAndSelf() {
        float[] info = OutpouringScreenPasses.shadeInfo(List.of(new double[]{.7, 0}, new double[]{.4, 1}));
        assertEquals(.7f, info[0], 1e-6);
        assertEquals(0f, info[1]);
        assertEquals(.4f, info[2], 1e-6);
        assertEquals(1f, info[3]);
    }

    @Test void passesRunAfterTheLevelAndAvoidBuiltIns() throws IOException {
        String passes = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringScreenPasses.java"));
        assertTrue(passes.contains("Stage.AFTER_LEVEL"));
        for (String name : new String[]{"cast_lumen.fsh", "cast_umbra.fsh"}) {
            String fsh = Files.readString(Path.of("src/main/resources/assets/hemomancy/shaders/core/screen/" + name));
            assertTrue(fsh.contains("Sampler1"), name + " reads scene depth");
            assertFalse(fsh.matches("(?s).*\\b(half|packed|distance|offset)\\s*[=;,)].*"), name);
        }
    }
    @Test void bothPassesKeepTheCastersBodyReadable() throws IOException {
        String passes = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringScreenPasses.java"));
        assertTrue(passes.contains("OutpouringClientState.casters(camera, partial, school)"), "each pass clears only its own casters");
        assertTrue(passes.contains("\"Casters\"") && passes.contains("\"CasterCount\""));
        for (String name : new String[]{"cast_lumen", "cast_umbra"}) {
            String fsh = Files.readString(Path.of("src/main/resources/assets/hemomancy/shaders/core/screen/" + name + ".fsh"));
            String json = Files.readString(Path.of("src/main/resources/assets/hemomancy/shaders/core/screen/" + name + ".json"));
            assertTrue(fsh.contains("bodyCover("), name + " must clear the caster's body");
            assertTrue(json.contains("\"Casters\"") && json.contains("\"CasterCount\""), name + " declares the caster uniforms");
        }
        String lumen = Files.readString(Path.of("src/main/resources/assets/hemomancy/shaders/core/screen/cast_lumen.fsh"));
        assertTrue(lumen.contains("knee("), "Lux relight must roll highlights off rather than clip to white");
    }
}
