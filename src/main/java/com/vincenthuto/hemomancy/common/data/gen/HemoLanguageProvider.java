package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HemoLanguageProvider extends LanguageProvider {


    public HemoLanguageProvider(PackOutput output, String locale) {
        super(output, Hemomancy.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {

        // Jei
        add("hemomancy.jei.memory_weaving", "Memory Weaving");
        add("hemomancy.jei.scar_station", "Scar Station");
        add("hemomancy.jei.enzyme_fruiting", "Enzyme Fruiting");

        // Banner
        addArmBannerTranslation("chitinite");

        addBannerTranslation("hemomancy_heart", "Vascularium");
        addBannerTranslation("hemomancy_veins", "Veins");
        add("item.hemomancy.heart_pattern.desc", "Vascularium Crest");
        add("item.hemomancy.veins_pattern.desc", "Vein Border");
        addKeyBindTranslations();

        add("item_group.hemomancy.hemomancytab", "Hemomancy the Sanguis Noctis");

        add("fluid.hemomancy.blood", "Blood");

        add("item.hemomancy.scout_field_notes.tooltip", "A dying scout's final observations.");
        add("item.hemomancy.scout_field_notes.tooltip.use", "Right-click to tuck the memo into your notes.");
        add("item.hemomancy.field_notes.tooltip.curio", "A worn notebook kept for marginalia and fieldwork.");
        add("item.hemomancy.field_notes.tooltip.legacy", "Memos now gather in your inventory notes until dictated into a Liber.");
        add("item.hemomancy.liber_sanguinum.tooltip.memos", "%s memos written in blood");
        add("item.hemomancy.liber_immaculatus.tooltip.memos", "%s pale notes preserved");
        add("message.hemomancy.memo.invalid", "The whisper cannot be kept.");
        add("message.hemomancy.memo.no_field_notes", "Your notes no longer need a separate field book.");
        add("message.hemomancy.memo.no_ink_path", "Your notes no longer need field ink.");
        add("message.hemomancy.memo.wrong_ink_path", "This testimony waits for a matching Liber.");
        add("message.hemomancy.memo.wrong_refill_ink", "Field ink is only a curio now.");
        add("message.hemomancy.memo.already_noted", "This memo is already waiting in your notes.");
        add("message.hemomancy.memo.already_written", "The Liber already preserves this memo.");
        add("message.hemomancy.memo.field_notes_full", "Your notes can carry this without blank pages.");
        add("message.hemomancy.memo.noted", "You make a note before it fades.");
        add("message.hemomancy.memo.no_memos", "There are no pending memos to dictate.");
        add("message.hemomancy.memo.wrong_liber", "No pending notes belong in that Liber.");
        add("message.hemomancy.memo.mixed_notes", "Some pending notes belong in another Liber.");
        add("message.hemomancy.memo.no_new_memos", "The Liber already knows these memos.");
        add("message.hemomancy.memo.not_enough_blood", "Dictation requires %s blood.");
        add("message.hemomancy.memo.dictated", "%s memos become written in blood. Cost: %s blood.");
        add("message.hemomancy.memo.not_enough_xp", "Dictation requires %s XP level.");
        add("message.hemomancy.memo.dictated_unstained", "%s pale memos settle into the Liber. Cost: %s XP level.");
        add("message.hemomancy.memo.field_notes_refilled", "The Field Notes drink the ink.");
        add("message.hemomancy.memo.field_notes_refilled_unstained", "The Field Notes take the pale ink.");
        add("message.hemomancy.memo.liber_placed", "You place the Liber on the Dictation Table.");
        add("message.hemomancy.memo.liber_removed", "You take the Liber from the Dictation Table.");
        add("hemomancy.dialogue.memo.make_note", "[Make a note before it fades.]");
        add("screen.hemomancy.virtual_field_notes.title", "Field Notes");
        add("screen.hemomancy.virtual_field_notes.total", "Pending: %s");
        add("screen.hemomancy.virtual_field_notes.harbinger", "Harbinger: %s");
        add("screen.hemomancy.virtual_field_notes.unstained", "Unstained: %s");
        add("screen.hemomancy.virtual_field_notes.shared", "Shared: %s");
        add("screen.hemomancy.virtual_field_notes.hint", "Use a Dictation Table with a placed Liber.");
        add("screen.hemomancy.virtual_field_notes.status", "Dictate pending notes at a Dictation Table with a placed Liber.");

        add("item.hemomancy.hematic_suture_needle.need_degree", "Degree %s is required to bind a hematic suture.");
        add("item.hemomancy.hematic_suture_needle.invalid_target", "The needle finds no blood route to stitch here.");
        add("item.hemomancy.hematic_suture_needle.bound", "Suture bound: %s");
        add("item.hemomancy.hematic_suture_needle.mode", "Suture mode: %s");
        add("item.hemomancy.hematic_suture_needle.mode.nearby", "Nearby");
        add("item.hemomancy.hematic_suture_needle.mode.fane", "Fane");
        add("item.hemomancy.hematic_suture_needle.mode.fane_bloodline", "Fane + Bloodline");
        add("item.hemomancy.hematic_suture_needle.fane_unavailable", "This link is outside your fane or awaits Degree 5.");
        add("item.hemomancy.hematic_suture_needle.opt_in_on", "You open your bloodline to fane routing.");
        add("item.hemomancy.hematic_suture_needle.opt_in_off", "You close your bloodline to fane routing.");
        add("item.hemomancy.hematic_suture_needle.inactive", "Your blood is not yet awakened.");
        add("item.hemomancy.hematic_suture_needle.status", "Use on a blood machine or node. Sneak-use to toggle bloodline opt-in.");

        for (DeferredHolder<EntityType<?>, ? extends EntityType<?>> e : EntityInit.ENTITY_TYPES.getEntries()) {
            addEntityType(e,
                    HLTextUtils.convertInitToLang(e.get().getDescriptionId().replace("entity.hemomancy.", "")));
        }
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
            addBlock(b,
                    HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
        }
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.BASEBLOCKS.getEntries()) {
            addBlock(b,
                    HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
        }

        for (DeferredHolder<Block, ? extends Block> b : BlockInit.SLABBLOCKS.getEntries()) {
            addBlock(b,
                    HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
        }

        for (DeferredHolder<Block, ? extends Block> b : BlockInit.STAIRBLOCKS.getEntries()) {
            addBlock(b,
                    HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
        }

        for (DeferredHolder<Block, ? extends Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
            addBlock(b,
                    HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
        }
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.MODELEDBLOCKS.getEntries()) {
            addBlock(b,
                    HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
        }
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.POTTEDBLOCKS.getEntries()) {
            addBlock(b,
                    HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
        }
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.OBJBLOCKS.getEntries()) {
            addBlock(b,
                    HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
        }
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.COLUMNBLOCKS.getEntries()) {
            addBlock(b,
                    HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
        }
        for (DeferredHolder<Item, ? extends Item> i : ItemInit.BASEITEMS.getEntries()) {
            String descriptionId = i.get().asItem().getDescriptionId();
            if (!descriptionId.startsWith("block.hemomancy.")) {
                addItem(i, HLTextUtils.convertInitToLang(descriptionId.replace("item.hemomancy.", "")));
            }
        }
        for (DeferredHolder<Item, ? extends Item> i : ItemInit.HANDHELDITEMS.getEntries()) {
            addItem(i,
                    HLTextUtils.convertInitToLang(i.get().asItem().getDescriptionId().replace("item.hemomancy.", "")));
        }
        for (DeferredHolder<Item, ? extends Item> i : ItemInit.SPECIALITEMS.getEntries()) {
            addItem(i,
                    HLTextUtils.convertInitToLang(i.get().asItem().getDescriptionId().replace("item.hemomancy.", "")));
        }
        for (DeferredHolder<Item, ? extends Item> i : ItemInit.SPAWNEGGS.getEntries()) {
            addItem(i,
                    HLTextUtils.convertInitToLang(i.get().asItem().getDescriptionId().replace("item.hemomancy.", "")));
        }

        add("entity.minecraft.villager.hemomancy.hemopothecary", "Hemopothecary");


        for (DeferredHolder<MobEffect, ? extends MobEffect> i : EffectInit.EFFECTS.getEntries()) {
            add("item.minecraft.potion.effect.potion_of_" + i.getId().getPath(),
                    "Potion of " + HLTextUtils.convertInitToLang(i.getId().getPath()));
            add("item.minecraft.splash_potion.effect.potion_of_" + i.getId().getPath(),
                    "Spash Potion of " + HLTextUtils.convertInitToLang(i.getId().getPath()));
            add("item.minecraft.lingering_potion.effect.potion_of_" + i.getId().getPath(),
                    "Lingering Potion of " + HLTextUtils.convertInitToLang(i.getId().getPath()));
            add("item.minecraft.tipped_arrow.effect.potion_of_" + i.getId().getPath(),
                    "Arrow of " + HLTextUtils.convertInitToLang(i.getId().getPath()));
            addEffect(() -> i.get(), HLTextUtils.convertInitToLang(i.getId().getPath()));
        }

    }

    public void addKeyBindTranslations() {
        add("key.hemomancy.category", "Hemomancy");
        add("key.hemomancy.bloodcrafting.desc", "Activate Blood Construct");
        add("key.hemomancy.bloodformation.desc", "Blood Formation");
        add("key.hemomancy.drawtest.desc", "Blood Draw");
        add("key.hemomancy.morphjarpickup.desc", "Toggle Morphling Jar Pickup");
        add("key.hemomancy.openjar.desc", "Open Morphling Jar");
        add("key.hemomancy.quickusemanip.desc", "Use Quick Manipulation");
        add("key.hemomancy.scarbinderpickup.desc", "Toggle Scar Binder Pickup");
        add("key.hemomancy.contusemanip.desc", "Use Continuous Manipulation");
        add("key.hemomancy.cyclemanip.desc", "Cycle Known Manipulations");

    }

    public void addBannerTranslation(String regName, String transName) {
        add("block.minecraft.banner." + regName + ".black", "Black " + transName);
        add("block.minecraft.banner." + regName + ".red", "Red " + transName);
        add("block.minecraft.banner." + regName + ".green", "Green " + transName);
        add("block.minecraft.banner." + regName + ".brown", "Brown " + transName);
        add("block.minecraft.banner." + regName + ".blue", "Blue " + transName);
        add("block.minecraft.banner." + regName + ".purple", "Purple " + transName);
        add("block.minecraft.banner." + regName + ".cyan", "Cyan " + transName);
        add("block.minecraft.banner." + regName + ".silver", "Light Gray " + transName);
        add("block.minecraft.banner." + regName + ".gray", "Gray " + transName);
        add("block.minecraft.banner." + regName + ".pink", "Pink " + transName);
        add("block.minecraft.banner." + regName + ".lime", "Lime " + transName);
        add("block.minecraft.banner." + regName + ".yellow", "Yellow " + transName);
        add("block.minecraft.banner." + regName + ".lightBlue", "Light " + transName);
        add("block.minecraft.banner." + regName + ".magenta", "Magenta " + transName);
        add("block.minecraft.banner." + regName + ".orange", "Orange " + transName);
        add("block.minecraft.banner." + regName + ".white", "White " + transName);
    }

    public void addArmBannerTranslation(String prefix) {
        add("item.hemomancy." + prefix + "_arm_banner.black",
                "Black " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.red", "Red " + HLTextUtils.convertInitToLang("_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.green",
                "Green " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.brown",
                "Brown " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.blue",
                "Blue " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.purple",
                "Purple " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.cyan",
                "Cyan " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.silver",
                "Light Gray " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.gray",
                "Gray " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.pink",
                "Pink " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.lime",
                "Lime " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.yellow",
                "Yellow " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.lightBlue",
                "Light " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.magenta",
                "Magenta " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.orange",
                "Orange " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
        add("item.hemomancy." + prefix + "_arm_banner.white",
                "White " + HLTextUtils.convertInitToLang(prefix + "_arm_banner"));
    }
}
