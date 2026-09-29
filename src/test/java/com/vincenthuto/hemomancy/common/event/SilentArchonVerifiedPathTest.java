package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillBendRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillOrigin;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillPhase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SilentArchonVerifiedPathTest {
    @Test
    void onlyProvenSilentArchonReceivesExclusivePowers() {
        assertFalse(HarbingerPathPermissions.isProvenSilentArchon(7, EnumArchonPath.NONE));
        assertFalse(HarbingerPathPermissions.isProvenSilentArchon(7, EnumArchonPath.SILENT_PENDING));
        assertFalse(HarbingerPathPermissions.isProvenSilentArchon(7, EnumArchonPath.APOTHEOS_PENDING));
        assertFalse(HarbingerPathPermissions.isProvenSilentArchon(8, EnumArchonPath.APOTHEOS));
        assertTrue(HarbingerPathPermissions.isProvenSilentArchon(7, EnumArchonPath.SILENT_ARCHON));

        assertFalse(SilentArchonArmorRules.canRefuseDeath(true, 7, EnumArchonPath.SILENT_PENDING,
                4000, 3000, 2000, 0));
        assertTrue(SilentArchonArmorRules.canRefuseDeath(true, 7, EnumArchonPath.SILENT_ARCHON,
                4000, 3000, 2000, 0));
        assertTrue(SilentArchonArmorRules.canRefuseDeath(true, 7, "silent",
                4000, 3000, 2000, 0));

        assertEquals(56, commandeerCost(EnumArchonPath.SILENT_PENDING));
        assertEquals(28, commandeerCost(EnumArchonPath.SILENT_ARCHON));
    }

    private static int commandeerCost(EnumArchonPath path) {
        return WillBendRules.resolve(WillOrigin.BROKEN, WillPhase.FALTERING, 7,
                WillBendRules.HeldItemKind.MARIONETTE_CROSSBAR, false, true,
                HarbingerPathPermissions.isProvenSilentArchon(7, path)).threadCost();
    }
}
