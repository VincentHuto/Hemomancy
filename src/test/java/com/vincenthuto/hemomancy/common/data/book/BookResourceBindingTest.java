package com.vincenthuto.hemomancy.common.data.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.vincenthuto.hutoslib.common.data.book.*;
import com.vincenthuto.hutoslib.client.book.BookEntryContent;
import com.vincenthuto.hutoslib.client.book.BookPaginator;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class BookResourceBindingTest {
    @Test
    void allAuthoredBookResourcesBindWithoutDroppingOrRenamingPages() throws Exception {
        class Reader extends BookPlaceboReloadListener {
            BookDataTemplate read(JsonObject json) { return serializers.read(json); }
        }
        Path root = Path.of("src/main/resources/data/hemomancy/books");
        Reader reader = new Reader();
        Map<ResourceLocation, BookDataTemplate> definitions = new LinkedHashMap<>();
        try (var files = Files.walk(root)) {
            for (Path path : files.filter(file -> file.toString().endsWith(".json")).toList()) {
                String relative = root.relativize(path).toString().replace('\\', '/');
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath("hemomancy", relative.substring(0, relative.length() - 5));
                BookDataTemplate template = reader.read(JsonParser.parseString(Files.readString(path)).getAsJsonObject());
                template.setId(id);
                definitions.put(id, template);
            }
        }
        reader.bindBooks(definitions);
        assertEquals(2, reader.getBooks().size());
        long expected = definitions.values().stream().filter(PageTemplate.class::isInstance).count();
        assertEquals(expected, reader.getBooks().stream().mapToInt(BookCodeModel::getTotalPages).sum());
        for (var definition : definitions.entrySet()) {
            if (definition.getValue() instanceof PageTemplate page) {
                var target = reader.findTarget(definition.getKey()).orElseThrow();
                assertSame(page, target.template());
                assertEquals(page.getText(), ((PageTemplate) target.template()).getText());
                assertTrue(reader.getBookByTitle(target.bookId()).getSourceIndex().entry(page.getId()).isPresent());
                var content = BookEntryContent.localize(page, target.chapterId(), value -> value);
                var blocks = content.blocks().entrySet().stream().map(block -> new BookPaginator.Block(
                        block.getKey(), block.getValue(), BookPaginator.Kind.BODY, 9, "", null)).toList();
                for (int width : new int[]{142, 146}) {
                    var leaves = BookPaginator.paginate(blocks, java.util.List.of(), false, width == 142,
                            width, 108, run -> run.text().codePointCount(0, run.text().length()) * 6, "continued");
                    for (var block : blocks) {
                        String rebuilt = leaves.stream().flatMap(leaf -> leaf.lines().stream())
                                .filter(line -> line.blockId().equals(block.id()) && !line.label())
                                .map(BookPaginator.Line::sourceText).reduce("", String::concat);
                        assertEquals(block.text().plainText(), rebuilt, page.getId() + "/" + block.id());
                    }
                    assertTrue(leaves.stream().flatMap(leaf -> leaf.lines().stream())
                            .allMatch(line -> line.y() + line.height() <= 108 && line.width() <= width));
                }
            }
        }
    }

    @Test
    void bloodStructurePagesKeepTheirGateAndPresentationAcrossTheCodec() {
        JsonObject source = JsonParser.parseString("""
                {"ordinality":0,"texture":"test:page","title":"Structure","subtitle":"","text":"Body",
                 "icon":"minecraft:book","structureloc":"test:structure","requiresEntry":"test:gate",
                 "layout":"custom","revealLevel":3,"margin":"Annotation"}
                """).getAsJsonObject();
        var page = BloodStructurePageTemplate.CODEC.parse(JsonOps.INSTANCE, source).getOrThrow();
        var encoded = BloodStructurePageTemplate.CODEC.encodeStart(JsonOps.INSTANCE, page).getOrThrow().getAsJsonObject();
        for (String field : new String[]{"structureloc", "requiresEntry", "layout", "revealLevel", "margin"})
            assertEquals(source.get(field), encoded.get(field), field);
    }
}
