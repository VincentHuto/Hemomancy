package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Static factory that produces the {@link DialogueTree} variants used by
 * the Unstained Zealot entity depending on the player's progression state.
 */
public final class ZealotDialogueTrees {

    private static final ResourceLocation ZEALOT_ICON = Hemomancy.rloc("textures/entity/unstained_zealot/unstained_zealot.png");
    private static final String SPEAKER = "entity.hemomancy.unstained_zealot";

    private ZealotDialogueTrees() {
    }

    /**
     * Player has active blood at VOTARY+ degree — the full plea with choices.
     */
    public static DialogueTree pleaDialogue(int entityId, int degree) {
        return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("greeting", List.of(
                        "hemomancy.zealot.plea.line1",
                        pleaDegreeLine(degree)
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.tell_me_more", "explain", null),
                        new DialogueOption("hemomancy.dialogue.zealot.option.not_interested", "reject", "zealot_reject_help")
                )))
                .addNode(new DialogueNode("explain", List.of(
                        "hemomancy.zealot.plea.line3",
                        "hemomancy.zealot.plea.line4"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.how_craft_hemolytic", "craft_hemolytic", null),
                        new DialogueOption("hemomancy.dialogue.zealot.option.accept_purification", null, "zealot_accept_purification"),
                        new DialogueOption("hemomancy.dialogue.zealot.option.accept_church", null, "zealot_accept_church"),
                        new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .addNode(new DialogueNode("craft_hemolytic", List.of(
                        "hemomancy.zealot.craft_hemolytic.line1",
                        "hemomancy.zealot.craft_hemolytic.line2",
                        "hemomancy.zealot.craft_hemolytic.line3"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.accept_purification", null, "zealot_accept_purification"),
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .addNode(new DialogueNode("reject", List.of(
                        "hemomancy.dialogue.zealot.reject_response"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .addNode(new DialogueNode("item_hint", List.of(
                        "hemomancy.zealot.item_hint"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .build();
    }

    /**
     * Player is already on the purification path — stage-aware guidance.
     */
    public static DialogueTree alreadyOnPath(int entityId) {
        return alreadyOnPath(entityId, 0f, false, false);
    }

    /**
     * Player is already on the purification path — stage-aware guidance based on progress.
     */
    public static DialogueTree alreadyOnPath(int entityId, float purity, boolean clarityUnlocked, boolean enlightened) {
        if (enlightened) {
            return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                    .theme(DialogueTheme.UNSTAINED)
                    .addNode(new DialogueNode("root", List.of(
                            "hemomancy.zealot.enlightened.line1",
                            "hemomancy.zealot.enlightened.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_annetta", "annetta_map", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("annetta_map", List.of(
                            "hemomancy.zealot.annetta_map.line1",
                            "hemomancy.zealot.annetta_map.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.take_stained_map", null, "give_stained_church_map"),
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("item_hint", List.of(
                            "hemomancy.zealot.item_hint"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .build();
        }

        if (clarityUnlocked) {
            return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                    .theme(DialogueTheme.UNSTAINED)
                    .addNode(new DialogueNode("root", List.of(
                            "hemomancy.zealot.clarity_phase.line1",
                            "hemomancy.zealot.clarity_phase.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.about_verdigris", "verdigris_info", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_annetta", "annetta_map", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("verdigris_info", List.of(
                            "hemomancy.zealot.verdigris_info.line1",
                            "hemomancy.zealot.verdigris_info.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("annetta_map", List.of(
                            "hemomancy.zealot.annetta_map.line1",
                            "hemomancy.zealot.annetta_map.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.take_stained_map", null, "give_stained_church_map"),
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("item_hint", List.of(
                            "hemomancy.zealot.item_hint"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .build();
        }

        if (purity >= 75f) {
            return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                    .theme(DialogueTheme.UNSTAINED)
                    .addNode(new DialogueNode("root", List.of(
                            "hemomancy.zealot.absolved.line1",
                            "hemomancy.zealot.absolved.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.about_clarity_rite", "clarity_rite_info", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("clarity_rite_info", List.of(
                            "hemomancy.zealot.clarity_rite_info.line1",
                            "hemomancy.zealot.clarity_rite_info.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("item_hint", List.of(
                            "hemomancy.zealot.item_hint"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .build();
        }

        if (purity >= 50f) {
            return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                    .theme(DialogueTheme.UNSTAINED)
                    .addNode(new DialogueNode("root", List.of(
                            "hemomancy.zealot.cleansing.line1",
                            "hemomancy.zealot.cleansing.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.about_altar", "altar_info", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("altar_info", List.of(
                            "hemomancy.zealot.altar_info.line1",
                            "hemomancy.zealot.altar_info.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("item_hint", List.of(
                            "hemomancy.zealot.item_hint"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .build();
        }

        if (purity >= 25f) {
            return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                    .theme(DialogueTheme.UNSTAINED)
                    .addNode(new DialogueNode("root", List.of(
                            "hemomancy.zealot.tainted.line1",
                            "hemomancy.zealot.tainted.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.about_silver_ward", "silver_ward_info", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("silver_ward_info", List.of(
                            "hemomancy.zealot.silver_ward_info.line1",
                            "hemomancy.zealot.silver_ward_info.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("item_hint", List.of(
                            "hemomancy.zealot.item_hint"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .build();
        }

        // Default: still at Corrupted level
        return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.zealot.already_on_path"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .addNode(new DialogueNode("item_hint", List.of(
                        "hemomancy.zealot.item_hint"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .build();
    }

    private static String pleaDegreeLine(int degree) {
        return switch (degree) {
            case 0 -> "hemomancy.zealot.plea.degree0.line2";
            case 1 -> "hemomancy.zealot.plea.degree1.line2";
            case 2 -> "hemomancy.zealot.plea.degree2.line2";
            case 3 -> "hemomancy.zealot.plea.degree3.line2";
            case 4 -> "hemomancy.zealot.plea.degree4.line2";
            default -> "hemomancy.zealot.plea.degree5.line2";
        };
    }

    /**
     * Player has no blood magic active.
     */
    public static DialogueTree noBlood(int entityId) {
        return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.zealot.no_blood"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .addNode(new DialogueNode("item_hint", List.of(
                        "hemomancy.zealot.item_hint"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .build();
    }

    /**
     * Player is initiated but not yet VOTARY.
     */
    public static DialogueTree tooEarly(int entityId) {
        return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.zealot.too_early"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .addNode(new DialogueNode("item_hint", List.of(
                        "hemomancy.zealot.item_hint"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .build();
    }

    /**
     * Player is beyond the normal cure window.
     */
    public static DialogueTree tooDeep(int entityId) {
        return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.zealot.too_deep"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .addNode(new DialogueNode("item_hint", List.of(
                        "hemomancy.zealot.item_hint"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .build();
    }

    /**
     * Player is uninitiated (degree 0) with active blood.
     */
    public static DialogueTree uninitiated(int entityId) {
        return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(
                        "hemomancy.zealot.uninitiated"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .addNode(new DialogueNode("item_hint", List.of(
                        "hemomancy.zealot.item_hint"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .build();
    }

    /**
     * Item inquiry for the Zealot. Responds to Unstained-path items. Purity-gated
     * responses (e.g. pallid_infusion requires 75+) are expressed as {@code min_purity}
     * conditions in the JSON entries. Harbinger items and unknowns are dismissed with
     * no mention of Harbinger NPCs.
     */
    public static DialogueTree itemInquiry(ItemStack item, int entityId, float purity, boolean clarityUnlocked) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
        return ItemInquiryRegistry.INSTANCE
                .resolve("zealot", itemId, 0, purity)
                .map(lines -> itemInquiryTree(entityId, lines.toArray(String[]::new)))
                .orElseGet(() -> itemInquiryTree(entityId, "hemomancy.zealot.item_inquiry.unknown"));
    }

    private static DialogueTree itemInquiryTree(int entityId, String... lineKeys) {
        return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("root", List.of(lineKeys), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .addNode(new DialogueNode("item_hint", List.of(
                        "hemomancy.zealot.item_hint"
                ), List.of(
                        new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                )))
                .build();
    }
}
