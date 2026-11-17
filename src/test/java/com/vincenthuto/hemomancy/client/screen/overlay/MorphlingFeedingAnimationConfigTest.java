package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

final class MorphlingFeedingAnimationConfigTest {
    @Test
    void feedingAnimationDefaultsToDisabled() {
        HemoClientConfig.registerClientConfig(new ModConfigSpec.Builder());

    }

}
