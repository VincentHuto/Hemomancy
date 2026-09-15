package com.vincenthuto.hemomancy.common.block.harbinger;

import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodScabPointedDripstoneBlockTest {
    @Test void connectedCustomSegmentsKeepTheVanillaBaseToTipProfile() {
        assertEquals(DripstoneThickness.BASE,
                BloodScabDripstoneProfile.connectedThickness(true,false,DripstoneThickness.MIDDLE));
        assertEquals(DripstoneThickness.MIDDLE,
                BloodScabDripstoneProfile.connectedThickness(true,true,DripstoneThickness.FRUSTUM));
        assertEquals(DripstoneThickness.FRUSTUM,
                BloodScabDripstoneProfile.connectedThickness(true,true,DripstoneThickness.TIP));
        assertEquals(DripstoneThickness.TIP,
                BloodScabDripstoneProfile.connectedThickness(false,true,DripstoneThickness.BASE));
    }
}
