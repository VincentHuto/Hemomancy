package com.vincenthuto.hemomancy.client.render.outpouring;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class CastVeinsSourceTest {
    private static final Path CLIENT = Path.of("src/main/java/com/vincenthuto/hemomancy/client");

    @Test void layerIsRegisteredOnPlayerSkins() throws IOException {
        assertTrue(Files.readString(CLIENT.resolve("event/LayerEvents.java")).contains("new CastVeinsLayer"));
    }

    @Test void firstPersonArmGetsVeins() throws IOException {
        assertTrue(Files.readString(CLIENT.resolve("event/CastingAnimationClientEvents.java")).contains("CastVeinsLayer.firstPersonArm("));
    }

    @Test void veinsShowOverArmourAndYieldToTheAvatar() throws IOException {
        String layer = Files.readString(CLIENT.resolve("render/outpouring/CastVeinsLayer.java"));
        assertTrue(layer.contains("CubeDeformation(1.1f)"), "armoured parts take a shell just outside the armour");
        assertTrue(layer.contains("AvatarManifestationTransition"), "a blood avatar encases the body, so veins step aside");
        assertFalse(layer.contains("ParticleTypes."));
    }

    @Test void shaderSamplesTheAtlasAndAvoidsBuiltIns() throws IOException {
        String fsh = Files.readString(Path.of("src/main/resources/assets/hemomancy/shaders/core/entity/cast_veins.fsh"));
        assertTrue(fsh.contains("Sampler0"));
        assertFalse(Pattern.compile("\\b(distance|offset|packed)\\s*[=;,)]").matcher(fsh).find(), "shadowed GLSL name");
    }
}
