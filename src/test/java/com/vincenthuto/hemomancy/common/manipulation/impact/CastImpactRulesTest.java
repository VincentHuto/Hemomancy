package com.vincenthuto.hemomancy.common.manipulation.impact;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPurpose;
import com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.Delivery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CastImpactRulesTest {
    @Test void projectilesWinOverEverythingElse() {
        assertEquals(Delivery.PROJECTILE, CastImpactRules.delivery(true, true, CastPurpose.AREA));
        assertEquals(Delivery.PROJECTILE, CastImpactRules.delivery(true, false, null));
    }

    @Test void weaponsAndWillStrikesAreMelee() {
        assertEquals(Delivery.MELEE, CastImpactRules.delivery(false, true, null));
    }

    @Test void castPurposeChoosesTheShape() {
        assertEquals(Delivery.PROJECTILE, CastImpactRules.delivery(false, false, CastPurpose.PROJECTILE));
        assertEquals(Delivery.AREA, CastImpactRules.delivery(false, false, CastPurpose.AREA));
        assertEquals(Delivery.AREA, CastImpactRules.delivery(false, false, CastPurpose.SELF));
        assertEquals(Delivery.AREA, CastImpactRules.delivery(false, false, CastPurpose.CONSTRUCTION));
        assertEquals(Delivery.TETHER, CastImpactRules.delivery(false, false, CastPurpose.TARGETED));
        assertEquals(Delivery.MELEE, CastImpactRules.delivery(false, false, CastPurpose.CONJURATION));
        assertEquals(Delivery.MELEE, CastImpactRules.delivery(false, false, CastPurpose.TRAVEL));
        assertEquals(Delivery.TETHER, CastImpactRules.delivery(false, false, null), "unlisted casts default like CastPresentation");
    }

    @Test void countRoundTripsEverySchoolDeliveryAndWeight() {
        for (var primary : EnumBloodTendency.values())
            for (var secondary : new EnumBloodTendency[]{null, EnumBloodTendency.LUX, EnumBloodTendency.TENEBRIS})
                for (var delivery : Delivery.values())
                    for (int weight = 0; weight < 3; weight++) {
                        int count = CastImpactRules.encode(primary, secondary, delivery, weight);
                        assertTrue(count > 0, "count must be positive so packets never read as empty");
                        var d = CastImpactRules.decode(count);
                        assertEquals(primary, d.primary());
                        assertEquals(secondary, d.secondary());
                        assertEquals(delivery, d.delivery());
                        assertEquals(weight, d.weight());
                    }
    }

    @Test void weightTiersFollowDamage() {
        assertEquals(0, CastImpactRules.weight(1.5f));
        assertEquals(1, CastImpactRules.weight(5f));
        assertEquals(2, CastImpactRules.weight(12f));
        assertEquals(.75, CastImpactRules.scale(0), 1e-9);
        assertEquals(1, CastImpactRules.scale(1), 1e-9);
        assertEquals(1.35, CastImpactRules.scale(2), 1e-9);
    }

    @Test void throttleKeepsOneImpactPerTargetUnlessTheHitIsHeavy() {
        assertTrue(CastImpactRules.allow(-1, 100, 0), "first hit always shows");
        assertFalse(CastImpactRules.allow(98, 100, 1));
        assertTrue(CastImpactRules.allow(95, 100, 1));
        assertTrue(CastImpactRules.allow(99, 100, 2), "heavy hits always read");
    }

    @Test void timelineFlashesBurstsAndFrays() {
        assertTrue(CastImpactRules.FLASH_END < CastImpactRules.BURST_END && CastImpactRules.BURST_END < CastImpactRules.LIFE);
        assertEquals(1, CastImpactRules.flash(0), 1e-6);
        assertEquals(0, CastImpactRules.flash(CastImpactRules.FLASH_END + 1), 1e-6);
        assertEquals(0, CastImpactRules.burst(0), 1e-6);
        assertEquals(1, CastImpactRules.burst(CastImpactRules.BURST_END), 1e-6);
        assertEquals(1, CastImpactRules.presence(3), 1e-6);
        assertTrue(CastImpactRules.presence(8) < 1 && CastImpactRules.presence(8) > 0, "the fray eases out");
        assertEquals(0, CastImpactRules.presence(CastImpactRules.LIFE), 1e-6);
    }
}
