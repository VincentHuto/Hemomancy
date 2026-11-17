package com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment;

import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HarbingerEquipmentTypeTest {
    @Test
    void theBandolierHasASlotOfItsOwn() {
        assertEquals(9, HarbingerEquipmentMenu.BANDOLIER_SLOT_INDEX);
        assertTrue(HarbingerEquipmentType.BANDOLIER.hasSlot(HarbingerEquipmentMenu.BANDOLIER_SLOT_INDEX));
        for (HarbingerEquipmentType type : HarbingerEquipmentType.values()) {
            if (type != HarbingerEquipmentType.BANDOLIER) {
                assertFalse(type.hasSlot(HarbingerEquipmentMenu.BANDOLIER_SLOT_INDEX), type + " cannot take the bandolier's slot");
            }
        }
    }
}
