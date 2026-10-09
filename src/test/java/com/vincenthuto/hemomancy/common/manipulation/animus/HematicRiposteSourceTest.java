package com.vincenthuto.hemomancy.common.manipulation.animus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class HematicRiposteSourceTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final String ID = "hematic_riposte";

    @Test
    void registeredAsAnAnimusQuickParryWithoutDrudgeSupport() throws IOException {
        String init = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");
        int start = init.indexOf("MANIPS.register(\"" + ID + "\"");
        assertTrue(start >= 0, "registration missing");
        String entry = init.substring(start, init.indexOf(");", start));
        assertTrue(entry.contains("new HematicRiposteManip(\"hematic_riposte\", 60, 10, 0, EnumManipulationType.QUICK"));
        assertTrue(entry.contains("EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.ARMS"));
        assertTrue(entry.contains(".setSecondaryTend(EnumBloodTendency.DUCTILIS)"));
        assertTrue(entry.contains(".setCooldownTicks(" + HematicRiposteRules.COOLDOWN + ")"));
        assertTrue(entry.contains("DrudgeAction.DRUDGE_UNSUPPORTED"));
    }

    @Test
    void memoryAndResourcesExist() throws IOException {
        String items = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
        String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
        assertTrue(items.contains("memory_" + ID + " = registerBloodMemoryItem(\"memory_" + ID + "\", ManipulationInit." + ID + ")"));
        for (String key : new String[] { "hemomancy.manipulation." + ID, "hemomancy.manipulation." + ID + ".desc",
                "item.hemomancy.memory_" + ID, "hemomancy.mnemonist.item_inquiry.memory_" + ID + ".line1",
                "subtitle.hemomancy.manipulation." + ID + ".emerge", "subtitle.hemomancy.manipulation." + ID + ".parry",
                "subtitle.hemomancy.manipulation." + ID + ".swat" })
            assertTrue(lang.contains("\"" + key + "\""), "missing lang " + key);
        for (String path : new String[] { "src/main/resources/assets/hemomancy/models/item/memory_" + ID + ".json",
                "src/main/resources/assets/hemomancy/textures/item/memories/memory_" + ID + "_overlay.png",
                "src/main/resources/data/hemomancy/recipe/memory_weaving/memory_" + ID + ".json",
                "src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/memory_" + ID + ".json" })
            assertTrue(Files.exists(ROOT.resolve(path)), "missing " + path);
        String sounds = read("src/main/resources/assets/hemomancy/sounds.json");
        for (String cue : new String[] { "emerge", "parry", "swat" })
            assertTrue(sounds.contains("\"manipulation." + ID + "." + cue + "\""), "missing sound " + cue);
    }

    @Test
    void treePresentationAndVisualsAreWired() throws IOException {
        assertTrue(read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java")
                .contains("register(\"" + ID + "\""));
        assertTrue(read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/animation/CastPresentation.java")
                .contains("\"" + ID + "\""));
        String materials = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/ManipulationMaterials.java");
        assertTrue(materials.contains("RIPOSTE, RIPOSTE_STRIKE -> ANIMUS"));
        String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/ManipulationVisualRenderer.java");
        assertTrue(renderer.contains("HematicRiposteGeometry.draw"));
        assertTrue(renderer.indexOf("if(HematicRiposteGeometry.handles") < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"),
                "riposte must be drawn before the shared Animus branch");
        String visuals = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationVisuals.java");
        assertTrue(visuals.contains(", RIPOSTE, RIPOSTE_STRIKE\n"),
                "new forms must be appended: the packet encodes forms by ordinal");
    }

    @Test
    void parryCancelsBeforeWardsAndSwatsProjectilesWithoutTakingOwnership() throws IOException {
        String events = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/HematicRiposteEvents.java");
        assertTrue(events.contains("@SubscribeEvent(priority = EventPriority.NORMAL)\n\tpublic static void onIncomingDamage"));
        assertTrue(events.contains("onProjectileImpact(ProjectileImpactEvent event)"));
        assertTrue(events.contains("ManipulationReactiveEvents.isBoss(attacker)"), "bosses must not be marked Conductive");
        assertFalse(events.contains("setOwner("), "a swat never transfers projectile ownership");
        assertTrue(events.contains("capCooldown(player, REFUND_COOLDOWN)"));
        assertTrue(read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java")
                .contains("HematicRiposteEvents.clearSessionState()"));
    }

    @Test
    void documentationMentionsTheParry() throws IOException {
        assertTrue(read("docs/HEMOMANCY_REFERENCE.md").contains("`" + ID + "`"));
        assertTrue(read("wiki/School-Combat.md").contains("`" + ID + "`"));
    }

    private static String read(String path) throws IOException {
        return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
    }
}
