package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;

/**
 * Dialogue trees for the {@link com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedScoutEntity}.
 *
 * <p>The scout is a dying Unstained field researcher encountered inside a tooth
 * geode. She barely has the strength to speak, but begs the player to take her
 * notes before she expires — she has made an observation about Annetta Knowles
 * that must not be lost.</p>
 */
public final class UnstainedScoutDialogueTrees {

    private static final ResourceLocation SCOUT_ICON = Hemomancy.rloc("textures/entity/unstained_scout/unstained_scout.png");
    private static final String SPEAKER = "entity.hemomancy.unstained_scout";

    private UnstainedScoutDialogueTrees() {}

    /**
     * The only dialogue tree for the scout. Branches give the player room to
     * ask a few questions before taking the notes; taking the notes fires the
     * {@code scout_give_notes} event and leads to a short farewell node.
     */
    public static DialogueTree dyingScout(int entityId) {
        return DialogueTree.builder(SPEAKER, SCOUT_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)

                // ── Root ── dying plea
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.scout.root.line1",
                        "hemomancy.scout.root.line2",
                        "hemomancy.scout.root.line3"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.scout.option.take_notes", "farewell", "scout_give_notes"),
                        new DialogueOption("hemomancy.dialogue.scout.option.what_happened", "what_happened", null),
                        new DialogueOption("hemomancy.dialogue.scout.option.leave", null, null)
                )))

                // ── What happened to you? ──
                .addNode(new DialogueNode("what_happened", List.of(
                        "hemomancy.scout.what_happened.line1",
                        "hemomancy.scout.what_happened.line2"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.scout.option.what_did_you_see", "observation_hint", null),
                        new DialogueOption("hemomancy.dialogue.scout.option.take_notes", "farewell", "scout_give_notes"),
                        new DialogueOption("hemomancy.dialogue.scout.option.leave", null, null)
                )))

                // ── What did you observe? ──
                .addNode(new DialogueNode("observation_hint", List.of(
                        "hemomancy.scout.observation_hint.line1",
                        "hemomancy.scout.observation_hint.line2",
                        "hemomancy.scout.observation_hint.line3"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.scout.option.take_notes", "farewell", "scout_give_notes"),
                        new DialogueOption("hemomancy.dialogue.scout.option.leave", null, null)
                )))

                // ── Farewell — notes have been given ──
                .addNode(new DialogueNode("farewell", List.of(
                        "hemomancy.scout.farewell.line1"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.scout.option.go_in_peace", null, null)
                )))

                .build();
    }
}
