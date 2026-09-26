package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.vincenthuto.hutoslib.common.data.book.*;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class MemoBookPresentationTest {
    @Test
    void registeredUnlockedEntriesRetainChapterMetadataAndTheOriginalBookIndex() {
        var chapter = ChapterTemplate.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {"ordinality":0,"texture":"test:page","color":"1,0,0","title":"Chapter","subtitle":"",
                 "icon":"minecraft:book","layout":"preview","text":"Introduction","margin":"Note","rowLimit":7,
                 "callout":{"icon":"minecraft:book","text":"A callout"}}
                """)).getOrThrow();
        chapter.setId(id("hemomancy:fanesanguinium/intro/chapter"));
        var registered = page("registered");
        var unregistered = page("unregistered");
        var locked = page("locked");
        chapter.setPages(List.of(registered, unregistered, locked));
        var book = new BookCodeModel(id("hemomancy:fanesanguinium"),
                new BookTemplate("test:cover", "test:overlay", "Book", "", "", "minecraft:book"));
        book.setChapters(List.of(chapter));
        book.setSourceIndex(BookSourceIndex.create(book.getChapters()));
        book.setRedactionPredicate((player, level) -> level == 2);
        var filtered = MemoBookFilter.filterEntries(book, Set.of(registered.getId(), locked.getId()),
                Set.of(registered.getId(), unregistered.getId()));
        assertEquals(List.of(registered), filtered.getChapters().getFirst().getPages());
        assertSame(chapter.getPresentation(), filtered.getChapters().getFirst().getPresentation());
        assertSame(book.getSourceIndex(), filtered.getSourceIndex());
        assertTrue(filtered.canReveal(null, 2));
        assertEquals(3, chapter.getPages().size());
        assertTrue(MemoBookFilter.filterEntries(book, Set.of(), Set.of()).getChapters().isEmpty());
    }

    private static PageTemplate page(String name) {
        var page = new PageTemplate(0, "test:page", name, "", "Body", "minecraft:book");
        page.setId(id("hemomancy:fanesanguinium/intro/pages/" + name));
        return page;
    }
    private static ResourceLocation id(String id) { return ResourceLocation.parse(id); }
}
