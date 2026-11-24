package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MortarboundModelTest {
    @Test
    void clawDrawsBackBeforeStrikeAndRecoversFromContactPose() {
        assertEquals(0F, MortarboundModel.clawSweep(16, 0F));
        assertTrue(MortarboundModel.clawSweep(5, 0F) < 0F);
        assertTrue(MortarboundModel.clawSweep(1, 0F) > 0F);
        assertEquals(MortarboundModel.clawSweep(1, 1F), MortarboundModel.clawSweep(-8, 0F));
        assertTrue(MortarboundModel.clawSweep(-4, 0F) > 0F);
        assertEquals(0F, MortarboundModel.clawSweep(-1, 1F));
        assertEquals(0F, MortarboundModel.clawSweep(0, 0F));
    }
}
