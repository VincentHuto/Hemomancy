package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MorphlingFeedingAnimationConfigTest {
    @Test
    void feedingAnimationDefaultsToDisabled() {
        HemoClientConfig.registerClientConfig(new ModConfigSpec.Builder());

        assertEquals(Boolean.FALSE, HemoClientConfig.RENDER_MORPHLING_FEEDING_ANIMATION.getDefault());
    }

    @Test
    void morphlingHudScaleDefaultsToOne() {
        HemoClientConfig.registerClientConfig(new ModConfigSpec.Builder());

        assertEquals(1.0D, HemoClientConfig.MORPHLING_HUD_SCALE.getDefault());
    }

    @Test
    void disabledFeedingAnimationStaysOnFirstFrame() {
        assertEquals(0, EquippedMorphlingOverlayPlacement.feedingFrame(1.0f, false));
    }

    @Test
    void enabledFeedingAnimationStillAdvancesFrames() {
        assertEquals(1, EquippedMorphlingOverlayPlacement.feedingFrame(0.18f, true));
    }

    @Test
    void feedingPulseHasASubtleHeartbeatShape() {
        float restingScale = EquippedMorphlingOverlayPlacement.feedingPulseScale(0.45f);
        float primaryBeatScale = EquippedMorphlingOverlayPlacement.feedingPulseScale(0.08f);
        float secondaryBeatScale = EquippedMorphlingOverlayPlacement.feedingPulseScale(0.21f);

        assertEquals(1.0f, restingScale, 0.001f);
        assertTrue(primaryBeatScale > restingScale);
        assertTrue(secondaryBeatScale > restingScale);
        assertTrue(primaryBeatScale > secondaryBeatScale);
        assertTrue(primaryBeatScale < 1.05f);
    }

    @Test
    void configuredHudScaleMultipliesTheHeartbeatScale() {
        float time = 0.08f;

        assertEquals(1.5f * EquippedMorphlingOverlayPlacement.feedingPulseScale(time),
                EquippedMorphlingOverlayPlacement.morphlingRenderScale(1.5f, time), 0.001f);
    }
}
