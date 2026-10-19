package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnettaCombatSourceTest {
    private static final String DIR = "src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/";

    private static String read(String file) throws IOException {
        return Files.readString(Path.of(DIR + file), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static String method(String source, String signature) {
        int start = source.indexOf(signature);
        assertTrue(start >= 0, "missing " + signature);
        int open = source.indexOf('{', start);
        int depth = 0;
        for (int i = open; i < source.length(); i++) {
            if (source.charAt(i) == '{') depth++;
            if (source.charAt(i) == '}' && --depth == 0) return source.substring(start, i + 1);
        }
        throw new AssertionError("unbalanced " + signature);
    }

    @Test
    void adminDamageAlwaysGetsThrough() throws IOException {
        for (String file : new String[]{"AnnettaKnowlesEntity.java", "StainedPriestessEntity.java", "LatentAnnettaInfectionEntity.java"}) {
            String hurt = method(read(file), "public boolean hurt(DamageSource source, float amount)");
            int bypass = hurt.indexOf("DamageTypeTags.BYPASSES_INVULNERABILITY");
            assertTrue(bypass >= 0 && bypass < hurt.indexOf("return false"), file + ": /kill and the void must still work");
        }
    }

    @Test
    void onlyAcceptedHitsCountTowardTheBreak() throws IOException {
        String hurt = method(read("AnnettaKnowlesEntity.java"), "public boolean hurt(DamageSource source, float amount)");
        int landed = hurt.indexOf("boolean landed = super.hurt(source, allowed)");
        assertTrue(landed >= 0 && hurt.indexOf("damageTaken +=") > landed, "the 50-damage counter counts landed hits only");
    }

    @Test
    void droppedPropsCannotBeCollected() throws IOException {
        String drop = method(read("AnnettaKnowlesEntity.java"), "private void dropProp(ServerLevel server, EquipmentSlot slot)");
        assertFalse(drop.contains("new ItemEntity"), "hoppers can collect item entities");
        assertTrue(drop.contains("ITEM_DISPLAY"));
    }

    @Test
    void theAllyIsReleasedIfHerInfectionIsLost() throws IOException {
        String ally = method(read("AnnettaKnowlesEntity.java"), "private void tickAlly(ServerLevel server)");
        assertTrue(ally.contains("foeMissingTicks") && ally.contains("markResolvedAfterCure()"));
        String die = method(read("LatentAnnettaInfectionEntity.java"), "public void die(DamageSource source)");
        assertTrue(die.contains("annetta.isPurging()"), "an infection killed during the purge still frees her");
    }

    @Test
    void idleLookingNeverFightsHerCombatGaze() throws IOException {
        String goals = method(read("AnnettaKnowlesEntity.java"), "protected void registerGoals()");
        assertTrue(goals.contains("LookAtPlayerGoal") && goals.contains("RandomLookAroundGoal"));
        assertTrue(goals.length() - goals.replace("calm()", "").length() >= 4 * "calm()".length(),
                "both look goals start and continue only while calm");
    }

    @Test
    void modelReadsClientClocks() throws IOException {
        String model = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta/AnnettaKnowlesModel.java"));
        assertTrue(model.contains("entity.actionTime(partial)") && model.contains("entity.sceneTime(partial)"),
                "synced ticks arrive on the network's schedule; the model must use the smoothed clocks");
        assertFalse(model.contains("getActionTick()"));
        assertTrue(method(read("AnnettaKnowlesEntity.java"), "public boolean hurt(DamageSource source, float amount)")
                .contains("ACTION_SHIELD_BASH"), "a blocked blow is answered with the shield");
    }
}
