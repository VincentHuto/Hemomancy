package com.vincenthuto.hemomancy.common.data.book;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.HemoMilestone;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LiberSanguiniumNamingTest {
    @Test
    void bookUsesTheCanonicalName() throws Exception {
        var root = Path.of("src/main/resources/data/hemomancy/books");
        try (var books = Files.walk(root)) {
            var book = books.filter(path -> path.getFileName().toString().equals("book.json"))
                    .map(path -> {
                        try { return JsonParser.parseString(Files.readString(path)).getAsJsonObject(); }
                        catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
                    }).filter(json -> json.get("title").getAsString().equals("Hemomancy"))
                    .findFirst().orElseThrow();
            assertEquals("Liber Sanguinium", book.get("subtitle").getAsString());
        }
    }

    @Test
    void oldPageUnlocksKeepTheirDiscoverySourcesUnderTheNewBookId() {
        var oldEntry = "hemomancy:fanesanguinium/the_infection/pages/clinical_cabinet";
        var entry = ResourceLocation.parse("hemomancy:libersanguinium/the_infection/pages/clinical_cabinet");
        var tag = new CompoundTag();
        var entries = new ListTag();
        entries.add(StringTag.valueOf(oldEntry));
        tag.put("UnlockedEntries", entries);
        var sources = new CompoundTag();
        var sourceNames = new ListTag();
        sourceNames.add(StringTag.valueOf("DIALOGUE"));
        sources.put(oldEntry, sourceNames);
        tag.put("EntrySources", sources);
        var knowledge = new LiberKnowledge();
        knowledge.deserializeNBT(null, tag);
        assertEquals(Set.of(entry), knowledge.getUnlockedEntries());
        assertEquals(Set.of(HemomancyDiscoverySource.DIALOGUE), knowledge.getEntrySources().get(entry));
        var restored = new LiberKnowledge();
        restored.deserializeNBT(null, knowledge.serializeNBT(null));
        assertEquals(knowledge.getUnlockedEntries(), restored.getUnlockedEntries());
        assertEquals(knowledge.getEntrySources(), restored.getEntrySources());
    }

    @Test
    void oldMilestoneCreditResolvesToTheCanonicalMilestone() {
        var milestone = HemoMilestone.byId("fane_sanguinium");
        assertNotNull(milestone);
        assertEquals("liber_sanguinium", milestone.getId());
        assertSame(milestone, HemoMilestone.byId("liber_sanguinium"));
    }
}
