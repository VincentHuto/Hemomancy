
package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DialogueInquiryCanonTest {
    private static final Path LANGUAGE = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");

    @Test
    void currentInquiryProseDoesNotRetainSettledStaleClaims() throws IOException {
        JsonObject language = JsonParser.parseString(Files.readString(LANGUAGE)).getAsJsonObject();
        StringBuilder inquiry = new StringBuilder();
        for (var entry : language.entrySet()) {
            if (entry.getKey().contains(".item_inquiry.")) inquiry.append(entry.getValue().getAsString()).append('\n');
        }
        String text = inquiry.toString().toLowerCase(Locale.ROOT);
        for (String forbidden : List.of(
                "smelt the ore directly",
                "wear the scar",
                "integrate it into your bloodline",
                "a blood key activates",
                "pallid infusion initiates",
                "guardians are not expected to walk",
                "guardian detachments use it",
                "blood that has been purified to near-transparency",
                "five points of purity",
                "five points of clarity")) {
            assertFalse(text.contains(forbidden), () -> "Stale inquiry phrase remains: " + forbidden);
        }
    }
}
