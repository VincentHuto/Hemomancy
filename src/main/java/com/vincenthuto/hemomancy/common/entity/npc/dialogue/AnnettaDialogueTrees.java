package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class AnnettaDialogueTrees {
    private static final ResourceLocation ANNETTA_ICON = Hemomancy.rloc("textures/entity/blank.png");
    private static final String SPEAKER = "entity.hemomancy.annetta_knowles";

    /** Dialogue events handled by {@code AnnettaKnowlesEntity.handleDialogueChoice}. */
    public static final String EVENT_UNSEAL_JAR = "annetta_unseal_jar";
    public static final String EVENT_OFFER_DRAUGHT = "annetta_offer_draught";

    private AnnettaDialogueTrees() {
    }

    /** A Harbinger stands over her with the jar of Tooth Pecks; she knows what they have always wanted from her. */
    public static DialogueTree coweringJar(int entityId) {
        return DialogueTree.builder(SPEAKER, ANNETTA_ICON, entityId)
                .theme(DialogueTheme.BLOOD)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.annetta.jar.line1",
                        "hemomancy.annetta.jar.line2",
                        "hemomancy.annetta.jar.line3"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.annetta.option.unseal", null, EVENT_UNSEAL_JAR),
                        new DialogueOption("hemomancy.dialogue.annetta.option.lower_jar", null, null)
                )))
                .build();
    }

    /** An Unstained with Clarity holds out the Draught of Still Mercy. */
    public static DialogueTree offerDraught(int entityId) {
        return DialogueTree.builder(SPEAKER, ANNETTA_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.annetta.draught.line1",
                        "hemomancy.annetta.draught.line2"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.annetta.option.what_is_it", "truth", null),
                        new DialogueOption("hemomancy.dialogue.annetta.option.drink", null, EVENT_OFFER_DRAUGHT),
                        new DialogueOption("hemomancy.dialogue.annetta.option.lower_vial", null, null)
                )))
                .addNode(new DialogueNode("truth", List.of(
                        "hemomancy.annetta.draught.truth.line1",
                        "hemomancy.annetta.draught.truth.line2",
                        "hemomancy.annetta.draught.truth.line3"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.annetta.option.drink", null, EVENT_OFFER_DRAUGHT),
                        new DialogueOption("hemomancy.dialogue.annetta.option.lower_vial", null, null)
                )))
                .startNode("root")
                .build();
    }

    public static DialogueTree coweringNeutral(int entityId) {
        return DialogueTree.builder(SPEAKER, ANNETTA_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.annetta.cowering.neutral.line1",
                        "hemomancy.annetta.cowering.neutral.line2"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.annetta.option.step_back", null, null)
                )))
                .build();
    }

    public static DialogueTree coweringHarbingerHint(int entityId) {
        return DialogueTree.builder(SPEAKER, ANNETTA_ICON, entityId)
                .theme(DialogueTheme.BLOOD)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.annetta.cowering.harbinger_hint.line1",
                        "hemomancy.annetta.cowering.harbinger_hint.line2"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.annetta.option.step_back", null, null)
                )))
                .build();
    }

    public static DialogueTree coweringUnstainedHint(int entityId) {
        return DialogueTree.builder(SPEAKER, ANNETTA_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.annetta.cowering.unstained_hint.line1",
                        "hemomancy.annetta.cowering.unstained_hint.line2"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.annetta.option.step_back", null, null)
                )))
                .build();
    }

    public static DialogueTree curedSupport(int entityId) {
        return DialogueTree.builder(SPEAKER, ANNETTA_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.annetta.cured_support.line1",
                        "hemomancy.annetta.cured_support.line2"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.annetta.option.hold_fast", null, null)
                )))
                .build();
    }

    public static DialogueTree resolved(int entityId) {
        return DialogueTree.builder(SPEAKER, ANNETTA_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.annetta.resolved.line1",
                        "hemomancy.annetta.resolved.line2"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.annetta.option.how_long", "how_long", null),
                        new DialogueOption("hemomancy.dialogue.annetta.option.what_now", "what_now", null),
                        new DialogueOption("hemomancy.dialogue.annetta.option.step_back", null, null)
                )))
                .addNode(new DialogueNode("how_long", List.of(
                        "hemomancy.annetta.resolved.how_long.line1",
                        "hemomancy.annetta.resolved.how_long.line2",
                        "hemomancy.annetta.resolved.how_long.line3"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.annetta.option.what_now", "what_now", null),
                        new DialogueOption("hemomancy.dialogue.annetta.option.step_back", null, null)
                )))
                .addNode(new DialogueNode("what_now", List.of(
                        "hemomancy.annetta.resolved.what_now.line1",
                        "hemomancy.annetta.resolved.what_now.line2"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.annetta.option.step_back", null, null)
                )))
                .startNode("root")
                .build();
    }
}
