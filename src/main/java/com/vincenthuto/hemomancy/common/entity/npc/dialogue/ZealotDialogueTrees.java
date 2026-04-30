package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
    public static DialogueTree pleaDialogue(int entityId) {
        return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                .theme(DialogueTheme.UNSTAINED)
                .addNode(new DialogueNode("greeting", List.of(
                        "hemomancy.zealot.plea.line1",
                        "hemomancy.zealot.plea.line2"
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

        if (clarityUnlocked) {
            return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
                    .theme(DialogueTheme.UNSTAINED)
                    .addNode(new DialogueNode("root", List.of(
                            "hemomancy.zealot.clarity_phase.line1",
                            "hemomancy.zealot.clarity_phase.line2"
                    ), List.of(
                            new DialogueOption("hemomancy.dialogue.zealot.option.about_verdigris", "verdigris_info", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.ask_about_item", "item_hint", null),
                            new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
                    )))
                    .addNode(new DialogueNode("verdigris_info", List.of(
                            "hemomancy.zealot.verdigris_info.line1",
                            "hemomancy.zealot.verdigris_info.line2"
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
                .build();
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
     * Item inquiry for the Zealot. Responds to Unstained-path items. Some items are
     * purity-gated (pallid_infusion requires 75+). Harbinger items and unknowns are
     * dismissed with no mention of Harbinger NPCs.
     */
    public static DialogueTree itemInquiry(ItemStack item, int entityId, float purity, boolean clarityUnlocked) {
        Item it = item.getItem();
        if (it == ItemInit.hemolytic_vial.get()) return zealotHemolyticInquiry(entityId);
        if (it == ItemInit.draught_of_still_waters.get()) return draughtInquiry(entityId);
        if (it == ItemInit.pale_humor_flask.get()) return paleHumorInquiry(entityId);
        if (it == ItemInit.pallid_infusion.get()) return pallidInfusionInquiry(purity, entityId);
        if (it == BlockInit.altar_of_cleansing.get().asItem()) return altarInquiry(entityId);
        if (it == ItemInit.lethean_dew.get() || it == ItemInit.lethean_brew.get()) return letheanInquiry(entityId);
        if (it == ItemInit.unstained_helm.get() || it == ItemInit.unstained_chestplate.get()
                || it == ItemInit.unstained_leggings.get() || it == ItemInit.unstained_boots.get())
            return unstainedArmorInquiry(entityId);
        if (it == ItemInit.hallowed_residuum_hemorath.get() || it == ItemInit.hallowed_residuum_seraphae.get()
                || it == ItemInit.hallowed_residuum_putriciel.get() || it == ItemInit.hallowed_residuum_velorum.get())
            return zealotResiduumInquiry(entityId);
        return zealotUnknownInquiry(entityId);
    }

    private static DialogueTree zealotHemolyticInquiry(int entityId) {
        return itemInquiryTree(entityId,
                "hemomancy.zealot.item_inquiry.hemolytic_vial.line1",
                "hemomancy.zealot.item_inquiry.hemolytic_vial.line2");
    }

    private static DialogueTree draughtInquiry(int entityId) {
        return itemInquiryTree(entityId,
                "hemomancy.zealot.item_inquiry.draught_of_still_waters.line1",
                "hemomancy.zealot.item_inquiry.draught_of_still_waters.line2");
    }

    private static DialogueTree paleHumorInquiry(int entityId) {
        return itemInquiryTree(entityId,
                "hemomancy.zealot.item_inquiry.pale_humor_flask.line1",
                "hemomancy.zealot.item_inquiry.pale_humor_flask.line2");
    }

    private static DialogueTree pallidInfusionInquiry(float purity, int entityId) {
        if (purity < 75f) {
            return itemInquiryTree(entityId,
                    "hemomancy.zealot.item_inquiry.pallid_infusion.not_yet");
        }
        return itemInquiryTree(entityId,
                "hemomancy.zealot.item_inquiry.pallid_infusion.line1",
                "hemomancy.zealot.item_inquiry.pallid_infusion.line2");
    }

    private static DialogueTree altarInquiry(int entityId) {
        return itemInquiryTree(entityId,
                "hemomancy.zealot.item_inquiry.altar_of_cleansing.line1",
                "hemomancy.zealot.item_inquiry.altar_of_cleansing.line2");
    }

    private static DialogueTree letheanInquiry(int entityId) {
        return itemInquiryTree(entityId,
                "hemomancy.zealot.item_inquiry.lethean.line1",
                "hemomancy.zealot.item_inquiry.lethean.line2");
    }

    private static DialogueTree unstainedArmorInquiry(int entityId) {
        return itemInquiryTree(entityId,
                "hemomancy.zealot.item_inquiry.unstained_armor.line1",
                "hemomancy.zealot.item_inquiry.unstained_armor.line2");
    }

    private static DialogueTree zealotResiduumInquiry(int entityId) {
        return itemInquiryTree(entityId,
                "hemomancy.zealot.item_inquiry.hallowed_residuum.line1",
                "hemomancy.zealot.item_inquiry.hallowed_residuum.line2");
    }

    private static DialogueTree zealotUnknownInquiry(int entityId) {
        return itemInquiryTree(entityId,
                "hemomancy.zealot.item_inquiry.unknown");
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
