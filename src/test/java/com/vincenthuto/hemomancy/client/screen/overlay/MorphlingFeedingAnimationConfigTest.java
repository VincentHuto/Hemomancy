package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
