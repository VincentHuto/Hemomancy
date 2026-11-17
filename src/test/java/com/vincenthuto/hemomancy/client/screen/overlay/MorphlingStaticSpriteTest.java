package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

final class MorphlingStaticSpriteTest {
    @Test
    void attachedSpritesUseOneNative32PixelFrame() throws Exception {
        assertEquals(32, EquippedMorphlingOverlayPlacement.ATTACHED_SIZE);
        for (MorphlingStrain strain : MorphlingStrains.ALL) {
            String name = strain.hudTexture();
            var visual = MorphlingHudVisuals.forItemPath(strain.itemPath());
            assertNotNull(visual, name);
            assertEquals(name, visual.textureName());
            var file = Path.of("src/main/resources/assets/hemomancy/textures/gui/morphling_overlay/" + name + ".png");
            var image = ImageIO.read(file.toFile());
            assertEquals(32, image.getWidth(), name);
            assertEquals(32, image.getHeight(), name);
            assertFalse(Files.exists(Path.of(file + ".mcmeta")), "Static sprite must not carry animation metadata");
            int visible = 0;
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 32; x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = argb >>> 24;
                    assertTrue(alpha == 0 || alpha == 255, name + " binary alpha");
                    if (alpha != 0) visible++;
                }
            }
            assertTrue(visible > 0 && visible < 1024, name + " transparent silhouette");
            assertEquals(255, image.getRGB(5, 16) >>> 24, name + " feeding point");
        }
    }

    @Test
    void mirroredUvsKeepPositiveGeometryWidth() {
        var normal = EquippedMorphlingOverlayPlacement.spriteBlit(false);
        var mirrored = EquippedMorphlingOverlayPlacement.spriteBlit(true);
        assertEquals(32, normal.width());
        assertEquals(32, mirrored.width());
        assertEquals(0, normal.uOffset());
        assertEquals(32, normal.uWidth());
        assertEquals(32, mirrored.uOffset());
        assertEquals(-32, mirrored.uWidth());
    }

    @Test
    void feedingPulseRepeatsTheSubtleDoubleBeat() {
        float rest = EquippedMorphlingOverlayPlacement.feedingPulseScale(0.45f);
        float primary = EquippedMorphlingOverlayPlacement.feedingPulseScale(0.08f);
        float secondary = EquippedMorphlingOverlayPlacement.feedingPulseScale(0.21f);
        assertEquals(1f, rest, 0.001f);
        assertEquals(1.032f, primary, 0.001f);
        assertTrue(primary > secondary && secondary > rest);
        for (float time : new float[] {0f, 0.08f, 0.21f, 0.45f}) {
            assertEquals(EquippedMorphlingOverlayPlacement.feedingPulseScale(time),
                    EquippedMorphlingOverlayPlacement.feedingPulseScale(time + 0.9f), 0.0001f);
        }
    }

    @Test
    void staticSpritePulsesWithoutSamplingAnimationFrames() throws Exception {
        var source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/EquippedMorphlingOverlay.java"));
        assertFalse(source.contains("feedingFrame"));
        assertTrue(source.contains("EquippedMorphlingOverlayPlacement.morphlingRenderScale("));
        assertFalse(source.contains("RENDER_MORPHLING_FEEDING_ANIMATION"));
        assertTrue(source.contains("EquippedMorphlingOverlayPlacement.jarScale("), "the jar, not the config, sizes the Morphling");
        String bondMeter = source.substring(source.indexOf("private void renderBondMeter"), source.indexOf("private void renderPrimalMotes"));
        assertFalse(bondMeter.contains("gfx.pose()"), "Bond meter must not leak a transform into the shared HUD pose");
    }
}
