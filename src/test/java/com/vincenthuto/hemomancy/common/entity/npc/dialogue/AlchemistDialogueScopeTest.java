package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AlchemistDialogueScopeTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void alchemistDoesNotTeachArtificerOrVicarSubjects() throws IOException {
        String alchemist = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java");
        String artificer = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerArtificerDialogueTrees.java");
        String vicar = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java");
        String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");

        assertFalse(alchemist.contains("tell_me_about_armature"));
        assertFalse(alchemist.contains("blood_structure_intro"));
        assertFalse(alchemist.contains("blood_crafting_lore"));
        assertFalse(lang.contains("hemomancy.alchemist.votary.armature_lore"));
        assertFalse(lang.contains("hemomancy.alchemist.votary.blood_structure_intro"));
        assertFalse(lang.contains("hemomancy.alchemist.illuminatus.blood_crafting_lore"));
        assertTrue(artificer.contains("teach_armature"));
        assertTrue(vicar.contains("ask_about_blood_crafting"));
    }

    @Test
    void neophyteTeachesCentrifugeAndVotaryOwnsAlembic() throws IOException {
        String source = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java");
        String neophyte = between(source, "public static DialogueTree neophyte", "public static DialogueTree votary");
        String votary = between(source, "public static DialogueTree votary", "public static DialogueTree initiate");

        assertTrue(neophyte.contains("tell_me_about_centrifuge"));
        assertTrue(neophyte.contains("neophyte.centrifuge_lore"));
        assertFalse(neophyte.contains("tell_me_about_alembic"));
        assertFalse(neophyte.contains("alembic_leak"));
        assertTrue(votary.contains("tell_me_about_alembic"));
        assertTrue(votary.contains("votary.alembic_lore"));
    }

    private static String between(String source, String start, String end) {
        int from = source.indexOf(start);
        int to = source.indexOf(end, from + start.length());
        assertTrue(from >= 0 && to > from, "Dialogue source boundary not found");
        return source.substring(from, to);
    }

    private static String read(String path) throws IOException {
        return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
    }
}
