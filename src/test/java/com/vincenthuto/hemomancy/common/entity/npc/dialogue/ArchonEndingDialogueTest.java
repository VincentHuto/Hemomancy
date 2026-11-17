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
    void returnedRevelationExplainsThatBothAnswersStillRequireCompletion() throws Exception {
        JsonObject language = language();
        String requirements = language.get("hemomancy.whisper.core_witness.line3").getAsString().toLowerCase();
        for (String detail : List.of("your own bloom", "both", "vesper", "memory", "silentium",
                "eighth degree", "rite of apotheos")) {
            assertTrue(requirements.contains(detail), "The decision omits " + detail);
        }
        assertTrue(!requirements.contains("seventh degree"), "Silence is no longer a seventh-Degree end");
        assertTrue(!language.get("hemomancy.whisper.core_witness.option.silence")
                .getAsString().toLowerCase().contains("go home"), "Refusal is not a completed ending");
    }

    @Test
    void pendingRefusalConfirmationTeachesTheActualTwoCutEntry() throws Exception {
        String confirmation = language().get("hemomancy.dialogue.event.archon_choice_silence")
                .getAsString().toLowerCase();
        for (String detail : List.of("not yet", "base living weapon", "bloom", "temporary living sickle",
                "second", "wound", "both", "vesper", "memory", "iron brazier", "living staff", "silentium",
                "eighth degree")) {
            assertTrue(confirmation.contains(detail), "Pending-refusal guidance omits " + detail);
        }
        assertTrue(!confirmation.contains("seventh degree"), "Silence is no longer a seventh-Degree end");
    }

    @Test
    void theHiveAddressesAnApotheosAsApotheos() throws Exception {
        JsonObject language = language();
        for (String key : List.of("hemomancy.whisper.archon.v0.line2", "hemomancy.whisper.archon.v2.truth2",
                "hemomancy.communion.v2.line2", "hemomancy.communion.v3.line3")) {
            assertTrue(language.get(key).getAsString().contains("Archon"), key + " no longer names the Archon");
            String apotheos = language.get(key + ".apotheos").getAsString();
            assertTrue(apotheos.contains("Apotheos") && !apotheos.contains("Archon"), key + ".apotheos");
        }
        DialogueTree communion = AncestralCommunionDialogueTrees.forVariant(3, ".apotheos");
        assertTrue(communion.getStartNode().lines().contains("hemomancy.communion.v3.line3.apotheos"));
        assertEquals(AncestralCommunionDialogueTrees.forVariant(3).getStartNode().lines(),
                AncestralCommunionDialogueTrees.forVariant(3, "").getStartNode().lines());
    }

    @Test
    void aSilentiumIsToldWhyTheHiveIsQuiet() throws Exception {
        JsonObject language = language();
        for (String key : List.of("hemomancy.whisper.silenced",
                "message.hemomancy.cardinal_rite_activation.ancestral_communion_silentium")) {
            assertTrue(language.has(key) && !language.get(key).getAsString().isBlank(), key);
        }
    }

    private static JsonObject language() throws Exception {
        return JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
    }

    @Test
    void archonResponsesFollowTheCanonicalPath() {
        DialogueTree tree = tree();
        assertResponse(tree, "vicar", 7, EnumArchonPath.NONE, "undecided");
        assertResponse(tree, "vicar", 7, EnumArchonPath.SILENT_PENDING, "silent_pending");
        assertResponse(tree, "vicar", 8, EnumArchonPath.SILENTIUM, "silent_archon");
        assertResponse(tree, "vicar", 7, EnumArchonPath.APOTHEOS_PENDING, "apotheos_pending");
        assertResponse(tree, "vicar", 8, EnumArchonPath.APOTHEOS, "apotheos");
        for (String npc : List.of("alchemist", "artificer", "mnemonist")) {
            assertResponse(tree, npc, 7, EnumArchonPath.NONE, "undecided");
            assertResponse(tree, npc, 7, EnumArchonPath.SILENT_PENDING, "silent_pending");
            assertResponse(tree, npc, 8, EnumArchonPath.SILENTIUM, "silent_archon");
            assertResponse(tree, npc, 7, EnumArchonPath.APOTHEOS_PENDING, "apotheos_pending");
            assertResponse(tree, npc, 8, EnumArchonPath.APOTHEOS, "apotheos");
        }
    }

    @Test
    void aBeatenVesperPointsTheTeachersAtTheBrazier() {
        DialogueTree tree = tree();
        for (String npc : List.of("vicar", "alchemist", "artificer", "mnemonist")) {
            DialogueTree result = DialogueHubFactory.withArchonAcknowledgment(tree, npc, 7,
                    EnumArchonPath.SILENT_PENDING, true, false);
            assertEquals(List.of("greeting", "hemomancy.dialogue.ending." + npc + ".silent_absorb"),
                    result.getStartNode().lines());
        }
    }

    /** PT-78: after Communion, with the Spine not yet used, the Alchemist names it; the others stay undecided. */
    @Test
    void theAlchemistNamesAnUnusedSpine() throws Exception {
        DialogueTree tree = tree();
        assertEquals(List.of("greeting", "hemomancy.dialogue.ending.alchemist.communed"),
                DialogueHubFactory.withArchonAcknowledgment(tree, "alchemist", 7, EnumArchonPath.NONE, false, true)
                        .getStartNode().lines());
        for (String npc : List.of("vicar", "artificer", "mnemonist")) {
            assertEquals(List.of("greeting", "hemomancy.dialogue.ending." + npc + ".undecided"),
                    DialogueHubFactory.withArchonAcknowledgment(tree, npc, 7, EnumArchonPath.NONE, false, true)
                            .getStartNode().lines());
        }
        String line = language().get("hemomancy.dialogue.ending.alchemist.communed").getAsString().toLowerCase();
        for (String detail : List.of("fungal spine", "ninth husk", "hand", "blood", "body stays behind")) {
            assertTrue(line.contains(detail), "The Spine line omits " + detail);
        }
    }

    /** PT-79: a Silence who has not yet met Vesper hears the Vicar name the Severance, as hearsay. */
    @Test
    void theVicarNamesTheSeveranceToAPendingSilence() throws Exception {
        String line = language().get("hemomancy.dialogue.ending.vicar.silent_pending").getAsString().toLowerCase();
        for (String detail : List.of("severance", "living sickle", "bloom", "vesper", "read")) {
            assertTrue(line.contains(detail), "The Vicar's Severance line omits " + detail);
        }
    }

    @Test
    void lowerDegreesAndOtherNpcsKeepTheirDialogue() {
        DialogueTree tree = tree();
        assertSame(tree, DialogueHubFactory.withArchonAcknowledgment(tree, "vicar", 6, EnumArchonPath.NONE));
        assertSame(tree, DialogueHubFactory.withArchonAcknowledgment(tree, "hermit", 8, EnumArchonPath.SILENTIUM));
    }

    @Test
    void everyEndingResponseHasTranslatedText() throws Exception {
        JsonObject language = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
        for (String npc : List.of("vicar", "alchemist", "artificer", "mnemonist")) {
            for (String state : List.of("undecided", "silent_pending", "silent_absorb", "silent_archon",
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
