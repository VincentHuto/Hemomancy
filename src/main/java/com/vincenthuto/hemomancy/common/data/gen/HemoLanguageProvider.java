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

		// Banner
		addArmBannerTranslation("chitinite");

		addBannerTranslation("hemomancy_heart", "Vascularium");
		addBannerTranslation("hemomancy_veins", "Veins");
		add("item.hemomancy.heart_pattern.desc", "Vascularium Crest");
		add("item.hemomancy.veins_pattern.desc", "Vein Border");
		addKeyBindTranslations();

		add("item_group.hemomancy.hemomancytab", "Hemomancy the Sanguis Noctis");

		add("fluid.hemomancy.blood", "Blood");

		add("item.hemomancy.field_notes.tooltip.memos", "%s memos written, %s blank pages remain");
		add("item.hemomancy.field_notes.tooltip.path_empty", "No ink has prepared these pages.");
		add("item.hemomancy.field_notes.tooltip.path_harbinger", "Prepared with Hematic Field Ink.");
		add("item.hemomancy.field_notes.tooltip.path_unstained", "Prepared with Pale Field Ink.");
		add("item.hemomancy.field_notes.tooltip.use", "Right-click with field ink in your inventory to refresh the blank pages.");
		add("item.hemomancy.liber_sanguinum.tooltip.memos", "%s memos written in blood");
		add("item.hemomancy.liber_immaculatus.tooltip.memos", "%s pale notes preserved");
		add("message.hemomancy.memo.invalid", "The whisper cannot be kept.");
		add("message.hemomancy.memo.no_field_notes", "You need Field Notes to keep this memo.");
		add("message.hemomancy.memo.no_ink_path", "These Field Notes have not been prepared with ink.");
		add("message.hemomancy.memo.wrong_ink_path", "This ink cannot hold that kind of testimony.");
		add("message.hemomancy.memo.wrong_refill_ink", "These notes already bear another ink.");
		add("message.hemomancy.memo.already_noted", "This memo is already in your Field Notes.");
		add("message.hemomancy.memo.field_notes_full", "Your Field Notes have no blank pages.");
		add("message.hemomancy.memo.noted", "You make a note before it fades.");
		add("message.hemomancy.memo.no_memos", "There are no memos to dictate.");
		add("message.hemomancy.memo.wrong_liber", "These notes do not belong in that Liber.");
		add("message.hemomancy.memo.mixed_notes", "The Field Notes contain testimony written under another ink.");
		add("message.hemomancy.memo.no_new_memos", "The Liber already knows these memos.");
		add("message.hemomancy.memo.not_enough_blood", "Dictation requires %s blood.");
		add("message.hemomancy.memo.dictated", "%s memos become written in blood. Cost: %s blood.");
		add("message.hemomancy.memo.not_enough_xp", "Dictation requires %s XP level.");
		add("message.hemomancy.memo.dictated_unstained", "%s pale memos settle into the Liber. Cost: %s XP level.");
		add("message.hemomancy.memo.field_notes_refilled", "The Field Notes drink the ink.");
		add("message.hemomancy.memo.field_notes_refilled_unstained", "The Field Notes take the pale ink.");
		add("message.hemomancy.memo.liber_placed", "You place the Liber on the Dictation Table.");
		add("message.hemomancy.memo.liber_removed", "You take the Liber from the Dictation Table.");
		add("hemomancy.dialogue.memo.make_note", "[Make a field note before it fades.]");

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
			addItem(i,
					HLTextUtils.convertInitToLang(i.get().asItem().getDescriptionId().replace("item.hemomancy.", "")));
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
		add("key.hemomancy.morphjarpickup.desc", "Toggle Morphling Jar P``````````````ickup");
		add("key.hemomancy.openjar.desc", "Open Morphling Jar");
		add("key.hemomancy.quickusemanip.desc", "Use Quick Manipulation");
		add("key.hemomancy.scarbinderpickup.desc", "Toggle Scar Binder Pickup");
		add("key.hemomancy.contusemanip.desc", "Use Continous Manipulation");
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
