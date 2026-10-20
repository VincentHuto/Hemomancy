package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VeinEncodingTest {
    @Test void roundTrip() {
        for (var school : EnumBloodTendency.values()) for (var section : EnumVeinSections.values())
            for (var rank : EnumManipulationRank.values()) {
                var secondary = EnumBloodTendency.values()[(school.ordinal() + 3) % 8];
                var packed = VeinEncoding.pack(school, secondary, section, rank, .62f, .37f, .81f, true, true, 1234);
                float[] out = VeinEncoding.unpack(packed);
                assertEquals(school.ordinal(), out[VeinEncoding.SCHOOL]);
                assertEquals(section.ordinal(), out[VeinEncoding.SECTION]);
                assertEquals(rank.ordinal(), out[VeinEncoding.RANK]);
                assertEquals(secondary.ordinal(), out[VeinEncoding.SECONDARY]);
                assertEquals(.62f, out[VeinEncoding.STRENGTH], 1 / 255f);
                assertEquals(.37f, out[VeinEncoding.CONTACT], 1 / 1000f);
                assertEquals(.81f, out[VeinEncoding.FADE], 1 / 255f);
                assertEquals(1, out[VeinEncoding.OFF_HAND]);
                assertEquals(234, out[VeinEncoding.SEED]);
                assertEquals(1, out[VeinEncoding.LEFT_MAIN]);
            }
    }

    @Test void noSecondaryIsEight() {
        var packed = VeinEncoding.pack(EnumBloodTendency.LUX, null, EnumVeinSections.HEAD, EnumManipulationRank.HUMILIS,
                1, 0, 1, false, false, 0);
        assertEquals(8, VeinEncoding.unpack(packed)[VeinEncoding.SECONDARY]);
        assertEquals(0, VeinEncoding.unpack(packed)[VeinEncoding.OFF_HAND]);
        assertEquals(0, VeinEncoding.unpack(packed)[VeinEncoding.LEFT_MAIN]);
    }
}
