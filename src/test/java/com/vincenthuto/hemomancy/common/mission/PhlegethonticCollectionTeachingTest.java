package com.vincenthuto.hemomancy.common.mission;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PhlegethonticCollectionTeachingTest {
    @Test
    void commissionExplainsHowToRecoverTheSyringeSampleWithoutProcessingIt() throws IOException {
        assertSampleRecovery("hemomancy.alchemist.phlegethontic_commission.task");
    }

    @Test
    void ledgerExplainsHowToRecoverTheSyringeSampleWithoutProcessingIt() throws IOException {
        assertSampleRecovery("screen.hemomancy.harbinger_assignment_ledger.step.phlegethontic_sample.desc");
    }

    private static void assertSampleRecovery(String key) throws IOException {
        var language = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
        String text = language.get(key).getAsString().toLowerCase(Locale.ROOT);
        assertTrue(text.contains("sneak-use"), key + " must explain syringe rack ejection");
        assertTrue(text.contains("rack") && text.contains("centrifuge"),
                key + " must explain the filled-rack transfer");
        assertTrue(text.contains("remove") && text.contains("without spinning"),
                key + " must preserve the loose specimen instead of separating it");
        assertTrue(text.contains("main hand"), key + " must explain how the Alchemist reads the sample");
    }
}
