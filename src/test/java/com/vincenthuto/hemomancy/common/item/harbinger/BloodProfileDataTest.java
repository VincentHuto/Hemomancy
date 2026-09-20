package com.vincenthuto.hemomancy.common.item.harbinger;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class BloodProfileDataTest {
    @Test void snapshotsSupportDormantAddonEntitiesAndMissingDefinitions() {
        var id = ResourceLocation.parse("absent_mod:creature");
        var entries = new HashMap<ResourceLocation, String>();
        entries.put(id, "{\"requires_living_syringe\":true}");
        var snapshot = new BloodProfileData.Snapshot(entries);
        entries.clear();
        assertTrue(snapshot.get(id).requiresLivingSyringe());
        assertEquals(EntityBloodProfile.EMPTY, snapshot.get(ResourceLocation.parse("absent_mod:other")));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.json().clear());
    }

    @Test void clientReplacementAndDisconnectCannotClearServerData() {
        var server = BloodProfileData.snapshot(false);
        var id = ResourceLocation.parse("minecraft:pig");
        try {
            BloodProfileData.receive(Map.of(id, "{\"requires_living_syringe\":true}"));
            assertTrue(BloodProfileData.snapshot(true).get(id).requiresLivingSyringe());
            BloodProfileData.receive(Map.of());
            assertEquals(EntityBloodProfile.EMPTY, BloodProfileData.snapshot(true).get(id));
            BloodProfileData.clearClient();
            assertSame(server, BloodProfileData.snapshot(false));
        } finally { BloodProfileData.clearClient(); }
    }
}
