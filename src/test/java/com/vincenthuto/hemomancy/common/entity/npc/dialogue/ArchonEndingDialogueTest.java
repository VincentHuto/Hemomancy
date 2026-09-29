package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchonEndingDialogueTest {
    private static final DialogueOption SERVICE = new DialogueOption("service", null, "claim_reward");

    @Test
    void archonResponsesFollowTheCanonicalPath() {
        DialogueTree tree = tree();
        assertResponse(tree, "vicar", 7, EnumArchonPath.NONE, "undecided");
        assertResponse(tree, "vicar", 7, EnumArchonPath.SILENT_PENDING, "silent_pending");
        assertResponse(tree, "vicar", 7, EnumArchonPath.SILENT_ARCHON, "silent_archon");
        assertResponse(tree, "vicar", 7, EnumArchonPath.APOTHEOS_PENDING, "apotheos_pending");
        assertResponse(tree, "vicar", 8, EnumArchonPath.APOTHEOS, "apotheos");
        for (String npc : List.of("alchemist", "artificer", "mnemonist")) {
            assertResponse(tree, npc, 7, EnumArchonPath.NONE, "undecided");
            assertResponse(tree, npc, 7, EnumArchonPath.SILENT_PENDING, "silent_pending");
            assertResponse(tree, npc, 7, EnumArchonPath.SILENT_ARCHON, "silent_archon");
            assertResponse(tree, npc, 7, EnumArchonPath.APOTHEOS_PENDING, "apotheos_pending");
            assertResponse(tree, npc, 8, EnumArchonPath.APOTHEOS, "apotheos");
        }
    }

    @Test
    void lowerDegreesAndOtherNpcsKeepTheirDialogue() {
        DialogueTree tree = tree();
        assertSame(tree, DialogueHubFactory.withArchonAcknowledgment(tree, "vicar", 6, EnumArchonPath.NONE));
        assertSame(tree, DialogueHubFactory.withArchonAcknowledgment(tree, "hermit", 7, EnumArchonPath.SILENT_ARCHON));
    }

    @Test
    void everyEndingResponseHasTranslatedText() throws Exception {
        JsonObject language = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
        for (String npc : List.of("vicar", "alchemist", "artificer", "mnemonist")) {
            for (String state : List.of("undecided", "silent_pending", "silent_archon",
                    "apotheos_pending", "apotheos")) {
                String key = "hemomancy.dialogue.ending." + npc + "." + state;
                assertTrue(language.has(key) && !language.get(key).getAsString().isBlank(), key);
            }
        }
    }

    @Test
    void undecidedArchonDialogueDoesNotRevealTheUnseenChoice() throws Exception {
        JsonObject language = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
        for (String npc : List.of("vicar", "alchemist", "artificer", "mnemonist")) {
            String line = language.get("hemomancy.dialogue.ending." + npc + ".undecided")
                    .getAsString().toLowerCase();
            for (String spoiler : List.of("spine", "vesper", "refusal", "apotheosis", "return",
                    "choice", "either road")) {
                assertTrue(!line.contains(spoiler), npc + " reveals " + spoiler + " before the projection");
            }
        }
        assertTrue(!language.get("hemomancy.vicar.archon.line2").getAsString()
                .toLowerCase().contains("decide"));
    }

    private static void assertResponse(DialogueTree tree, String npc, int degree, EnumArchonPath path,
            String expectedState) {
        DialogueTree result = DialogueHubFactory.withArchonAcknowledgment(tree, npc, degree, path);
        assertEquals(List.of("greeting", "hemomancy.dialogue.ending." + npc + "." + expectedState),
                result.getStartNode().lines());
        assertEquals(List.of(SERVICE), result.getStartNode().options());
        assertSame(tree.getNode("service"), result.getNode("service"));
    }

    private static DialogueTree tree() {
        return DialogueTree.builder("speaker", ResourceLocation.fromNamespaceAndPath("hemomancy", "portrait"), 42)
                .addNode(new DialogueNode("root", List.of("greeting"), List.of(SERVICE)))
                .addNode(new DialogueNode("service", List.of("service.line"), List.of()))
                .build();
    }
}
