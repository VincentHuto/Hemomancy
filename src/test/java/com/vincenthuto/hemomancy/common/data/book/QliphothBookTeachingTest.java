package com.vincenthuto.hemomancy.common.data.book;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QliphothBookTeachingTest {
    @Test
    void bloomTeachingUsesTheRiteAndOwnershipRatherThanDivineAuthority() throws Exception {
        String text = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/hemomancy/books/libersanguinium/cosmic_forces/pages/the_qliphoth.json")))
                .getAsJsonObject().get("text").getAsString().toLowerCase(Locale.ROOT);

        assertFalse(text.contains("cheat god"), "The Qliphoth is not a divine authority");
        assertTrue(text.contains("bloom of the qliphoth"), "Name the actual rite");
        assertTrue(text.contains("owner"), "Explain why a placed block is insufficient");
        assertTrue(text.contains("nine"), "Teach the registered Bloom's pome lifecycle");
        assertFalse(text.contains("vesper"), "Keep the later revelation out of this introductory page");
    }
}
