package com.vincenthuto.hemomancy.common.mission;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** First Bloodcraft teaches the cauldron route for formations and the stone route for Venous Stone. */
class CondenserTeachingResourceTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final JsonObject LANG = lang();

    @Test
    void everyFormationLessonNamesTheCauldron() {
        for (String key : new String[]{"hemomancy.vicar.blood_crafting.projection",
                "hemomancy.vicar.neophyte.projection_hint", "hemomancy.vicar.neophyte.degree_hint",
                "hemomancy.vicar.neophyte.first_bloodcraft_reward.unready", "hemomancy.hermit.neophyte.hint",
                "screen.hemomancy.harbinger_assignment_ledger.step.project_formation.desc",
                "hemomancy.alchemist.item_inquiry.sanguine_formation.line1"})
            assertTrue(text(key).contains("cauldron"), key + " does not teach the cauldron route");
        assertTrue(text("hemomancy.vicar.neophyte.projection_hint").contains("Venous Stone"),
                "the hint must keep the two projections apart");
        assertEquals("How do I condense a formation, and make Venous Stone?",
                text("hemomancy.dialogue.vicar.option.projection_hint"));
        assertEquals("Condense a Formation", text("screen.hemomancy.harbinger_assignment_ledger.step.project_formation"));
        assertTrue(text("skill.hemomancy.skill_sanguine_crystallization.desc").contains("Sanguine Cauldron"));
    }

    @Test
    void noTextStillPromisesFormationsFromBareBlocks() {
        for (var entry : LANG.entrySet()) {
            String value = entry.getValue().getAsString();
            assertFalse(value.contains("onto solid blocks") || value.contains("toward a solid block")
                    || value.contains("against a solid block") || value.contains("onto a solid block"),
                    entry.getKey() + " still teaches bare-block formations");
        }
    }

    @Test
    void liberAndInquiriesFollowTheCondenser() throws IOException {
        String degrees = Files.readString(RESOURCES.resolve(
                "data/hemomancy/books/libersanguinium/the_hematic_order/pages/degrees.json"));
        assertTrue(degrees.contains("cauldron"), "Liber degrees page still teaches bare-block formations");
        Path inquiry = RESOURCES.resolve("data/hemomancy/dialogue_inquiry/vicar/hemomancy/sanguine_condenser.json");
        assertTrue(Files.exists(inquiry), "the Vicar has no Condenser inquiry");
        for (var line : JsonParser.parseString(Files.readString(inquiry)).getAsJsonObject().getAsJsonArray("lines"))
            assertTrue(LANG.has(line.getAsString()), "missing inquiry line " + line.getAsString());
    }

    @Test
    void onlyTheCondenserRecordsTheFormationProof() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            var callers = files.filter(path -> path.toString().endsWith(".java")).filter(path -> {
                try {
                    return Files.readString(path).contains("FirstBloodcraftAssignment.recordFormation(");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            }).map(path -> path.getFileName().toString()).toList();
            assertEquals(java.util.List.of("SanguineCondenserBlockEntity.java"), callers);
        }
    }

    private static String text(String key) {
        assertTrue(LANG.has(key), "missing lang key " + key);
        return LANG.get(key).getAsString();
    }

    private static JsonObject lang() {
        try {
            return JsonParser.parseString(Files.readString(RESOURCES.resolve("assets/hemomancy/lang/en_us.json")))
                    .getAsJsonObject();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
