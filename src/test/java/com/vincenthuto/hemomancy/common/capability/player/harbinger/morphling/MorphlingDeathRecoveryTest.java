package com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling;

import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmRules;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MorphlingDeathRecoveryTest {

    @Test
    void ScarletVanityEquipmentIsNeverEmittedAsDeathDrops() {
        assertFalse(VasculariumCharmRules.shouldDropEquippedSlot(true));
        assertFalse(VasculariumCharmRules.shouldDropEquippedSlot(false));
    }

    @Test
    void equippedMorphlingReplacesItsJarEntryWhenARecoveryJarExists() throws Exception {
        String recovery = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/MorphlingDeathRecovery.java"));

        assertTrue(recovery.contains("MorphlingIdentity.matches(stored, morphling)"));
        assertTrue(recovery.contains("jar.setStackInSlot(slot, morphling.copy())"));
        assertTrue(recovery.contains("jar.save()"));
    }

    @Test
    void equippedMorphlingCannotRecoverWithoutAJar() throws Exception {
        String recovery = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/MorphlingDeathRecovery.java"));

        assertTrue(recovery.contains("return false;"),
                "the recovery helper must report failure so the death hook can create an item drop");
    }

    @Test
    void deathHandlingIsAttachedToTheServerDropPathAndEquipmentDropHookIsGone() throws Exception {
        String morphlingEvents = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java"));
        String equipmentEvents = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java"));
        String attachments = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/com/vincenthuto/hemomancy/common/capability/HemoAttachmentTypes.java"));

        assertTrue(morphlingEvents.contains("MorphlingDeathRecovery.returnToJar"));
        assertTrue(morphlingEvents.contains("onPlayerDeath(LivingDeathEvent event)"));
        assertTrue(morphlingEvents.contains("player.level().addFreshEntity(drop)"));
        assertFalse(morphlingEvents.contains("onPlayerDrops(LivingDropsEvent event)"));
        assertFalse(equipmentEvents.contains("dropItemsAt(player, event.getDrops())"));
        assertTrue(attachments.contains("HARBINGER_EQUIPMENT"));
        assertTrue(attachments.contains("AttachmentType.serializable(() -> new HarbingerEquipmentContainer()).copyOnDeath().build()"));
    }
}
