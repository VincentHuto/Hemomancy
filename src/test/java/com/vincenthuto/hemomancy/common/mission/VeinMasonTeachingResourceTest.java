package com.vincenthuto.hemomancy.common.mission;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VeinMasonTeachingResourceTest {
    @Test
    void teacherLedgerAndAdvancementTeachOfferingAbsorptionInsteadOfSneakUse() throws IOException {
        JsonObject lang = read("assets/hemomancy/lang/en_us.json");
        for (String key : new String[]{
                "hemomancy.anchorite.carve_reminder.line2",
                "hemomancy.anchorite.loadout_reminder.line2",
                "hemomancy.anchorite.inquiry.brazier.line2",
                "hemomancy.alchemist.item_inquiry.scar_blank.line2",
                "hemomancy.mnemonist.item_inquiry.scar_pattern.loadout",
                "screen.hemomancy.harbinger_assignment_ledger.vein_mason.burn_scar",
                "screen.hemomancy.harbinger_assignment_ledger.vein_mason.commit_loadout",
                "advancements.hemomancy.vein_mason_first_scar_learned.desc"}) {
            String text = lang.get(key).getAsString().toLowerCase(Locale.ROOT);
            assertTrue(text.contains("blood absorption"), key + " must name the burn control");
            assertFalse(text.contains("sneak-use"), key + " must not teach the retired shortcut");
        }
        String lesson = lang.get("hemomancy.anchorite.carve_reminder.line2").getAsString();
        assertTrue(lesson.contains("Blood Projection"));
        assertTrue(lesson.contains("100 mL"));
        assertTrue(lang.get("hemomancy.anchorite.loadout_reminder.line2").getAsString().contains("50 mL"));
        assertTrue(lang.get("hemomancy.anchorite.inquiry.brazier.line2").getAsString().contains("retrieve"));
    }

    @Test
    void effigyLessonNamesMotifIngredientsAndCostBeforeTheFinalCommit() throws IOException {
        JsonObject lang = read("assets/hemomancy/lang/en_us.json");
        String text = lang.get("hemomancy.anchorite.effigy_instruction.line2").getAsString();
        String eligibility = lang.get("hemomancy.anchorite.effigy_instruction.line1").getAsString();
        assertTrue(eligibility.contains("must know all its scars") && eligibility.contains("have room"));
        for (String required : new String[]{"Paper", "Charcoal", "Sanguine Formation", "500 mL", "prepared",
                "Selection stays fixed", "stop and resume", "returns its paper, not spent blood"}) {
            assertTrue(text.contains(required), "Effigy lesson must explain " + required);
        }
    }

    @Test
    void liberPreservesTheWholeCerebralScarCycleAndSeparatesFungalImplantation() throws IOException {
        JsonObject page = read("data/hemomancy/books/libersanguinium/the_hematic_order/pages/scar_practice.json");
        String text = page.get("text").getAsString();
        for (String required : new String[]{"Degree 4", "Vicar", "Masons Respite", "Vein-Mason",
                "purple", "red", "Blood Projection", "Blood Absorption", "100 mL", "500 mL", "50 mL",
                "Runic Motif Paper", "Charcoal", "Sanguine Formation", "Fungal Implantation Pylon",
                "Selection stays fixed", "retains the partial charge", "returns its paper, not spent blood",
                "must know every selected scar", "have room for the loadout", "ordinary use",
                "until you learn the scar", "your own crafted station", "Sneaking retrieves",
                "D4 Main assignment", "not worn items"}) {
            assertTrue(text.contains(required), "Scar page must explain " + required);
        }
    }

    private static JsonObject read(String path) throws IOException {
        return JsonParser.parseString(Files.readString(Path.of("src/main/resources").resolve(path))).getAsJsonObject();
    }
}
