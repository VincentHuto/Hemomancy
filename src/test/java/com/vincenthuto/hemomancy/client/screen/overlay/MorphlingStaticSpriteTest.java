package com.vincenthuto.hemomancy.client.screen.overlay;

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
        for (String name : List.of("deadmans_purse", "gravecap", "witchs_ear", "lumenlace",
                "bootlace", "irontooth", "emberfang", "winter_shroud")) {
            var visual = MorphlingHudVisuals.forItemPath("morphling_" + name);
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
    void mouthStaysOnVesselThroughMirroringAndScaling() {
        for (float scale : new float[] {0.25f, 1f, 1.5f, 4f}) {
            for (boolean left : new boolean[] {true, false}) {
                var a = EquippedMorphlingOverlayPlacement.attachment(left, 100f, 80f, scale);
                assertEquals(100f, a.left() + a.mouthX() * scale, 0.0001f);
                assertEquals(80f, a.top() + 16.5f * scale, 0.0001f);
                assertEquals(32f * scale, a.size(), 0.0001f);
                assertEquals(left ? 5.5f : 26.5f, a.mouthX());
                assertEquals(!left, EquippedMorphlingOverlayPlacement.shouldMirror(left));
            }
        }
    }

    @Test
    void bondMeterAvoidsTheBodyAtTopAndBottomHudPositions() {
        for (float anchorY : new float[] {47.2f, 177.2f}) {
            for (float scale : new float[] {0.25f, 1f, 1.5f, 4f}) {
                for (boolean left : new boolean[] {true, false}) {
                    var a = EquippedMorphlingOverlayPlacement.attachment(left, 150, anchorY, scale);
                    int x = a.bondMeterX(left);
                    int y = a.bondMeterY();
                    assertTrue(y >= 2);
                    boolean above = y + 14 < a.top();
                    boolean beside = left ? x - 1 > a.left() + a.size() : x + 33 < a.left();
                    assertTrue(above || beside, "Meter must avoid the scaled body on either HUD side");
                }
            }
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
    void pulseKeepsTheFeedingPointFixedOnBothSides() {
        for (boolean left : new boolean[] {true, false}) {
            for (float configured : new float[] {0.25f, 1f, 1.5f, 4f}) {
                float scale = EquippedMorphlingOverlayPlacement.morphlingRenderScale(configured, 0.08f);
                assertEquals(configured * 1.032f, scale, 0.001f);
                var pulsed = EquippedMorphlingOverlayPlacement.attachment(left, 100, 80, scale);
                assertEquals(100f, pulsed.left() + pulsed.mouthX() * scale, 0.0001f);
                assertEquals(80f, pulsed.top() + 16.5f * scale, 0.0001f);
            }
        }
    }

    @Test
    void staticSpritePulsesWithoutSamplingAnimationFrames() throws Exception {
        var source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/EquippedMorphlingOverlay.java"));
        assertFalse(source.contains("feedingFrame"));
        assertTrue(source.contains("EquippedMorphlingOverlayPlacement.morphlingRenderScale("));
        assertFalse(source.contains("RENDER_MORPHLING_FEEDING_ANIMATION"));
        assertTrue(source.contains("HemoClientConfig.MORPHLING_HUD_SCALE.get()"));
        String bondMeter = source.substring(source.indexOf("private void renderBondMeter"), source.indexOf("private void renderLegacyIcon"));
        assertFalse(bondMeter.contains("gfx.pose()"), "Bond meter must not leak a transform into the shared HUD pose");
    }
}
