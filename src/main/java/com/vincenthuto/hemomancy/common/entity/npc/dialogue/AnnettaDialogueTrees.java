package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class AnnettaDialogueTrees {
    private static final ResourceLocation ANNETTA_ICON = Hemomancy.rloc("textures/entity/blank.png");
    private static final String SPEAKER = "entity.hemomancy.annetta_knowles";

    private AnnettaDialogueTrees() {
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
                        new DialogueOption("hemomancy.dialogue.annetta.option.step_back", null, null)
                )))
                .build();
    }
}
