package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QliphothBloomRenderRulesTest {
    @Test
    void bloomMustBeInRangeAndInsideTheFrustum() {
        assertTrue(QliphothBloomRenderRules.shouldRender(64.0, true));
        assertFalse(QliphothBloomRenderRules.shouldRender(64.0, false));
        assertTrue(QliphothBloomRenderRules.shouldRender(256.0 * 256.0, true));
        assertFalse(QliphothBloomRenderRules.shouldRender(256.0 * 256.0 + 1.0, true));
    }

    @Test
    void onlyNearbyBloomsRenderDynamicEffects() {
        assertTrue(QliphothBloomRenderRules.shouldRenderDynamicEffects(48.0 * 48.0));
        assertFalse(QliphothBloomRenderRules.shouldRenderDynamicEffects(48.0 * 48.0 + 1.0));
    }
}
