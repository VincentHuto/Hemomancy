package com.vincenthuto.hemomancy.data;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.init.PotionInit;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;

public class LanguageGenerator extends LanguageProvider {
	public LanguageGenerator(DataGenerator gen, ExistingFileHelper helper) {
		super(gen, Hemomancy.MOD_ID, "en_us");
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

		add("item.hemomancy." + regName + "_arm_banner.black",
				"Black " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.red", "Red " + HLTextUtils.convertInitToLang("_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.green",
				"Green " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.brown",
				"Brown " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.blue",
				"Blue " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.purple",
				"Purple " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.cyan",
				"Cyan " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.silver",
				"Light Gray " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.gray",
				"Gray " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.pink",
				"Pink " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.lime",
				"Lime " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.yellow",
				"Yellow " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.lightBlue",
				"Light " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.magenta",
				"Magenta " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.orange",
				"Orange " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
		add("item.hemomancy." + regName + "_arm_banner.white",
				"White " + HLTextUtils.convertInitToLang(regName + "_arm_banner"));
	}

	public void addKeyBindTranslations() {
		add("key.hemomancy.category", "Hemomancy");
		add("key.hemomancy.bloodcrafting.desc", "Activate Blood Construct");
		add("key.hemomancy.bloodformation.desc", "Blood Formation");
		add("key.hemomancy.drawtest.desc", "Blood Draw");
		add("key.hemomancy.morphjarpickup.desc", "Toggle Morphling Jar Pickup");
		add("key.hemomancy.openjar.desc", "Open Morphling Jar");
		add("key.hemomancy.quickusemanip.desc", "Use Quick Manipulation");
		add("key.hemomancy.runebinderpickup.desc", "Toggle Rune Binder Pickup");
		add("key.hemomancy.contusemanip.desc", "Use Continous Manipulation");
		add("key.hemomancy.cyclemanip.desc", "Cycle Known Manipulations");

	}

	@Override
	protected void addTranslations() {
		// Jei
		add("hemomancy.jei.recaller", "Visceral Recaller");
		add("hemomancy.jei.chisel_station", "Chisel Station");

		// Banner
		addBannerTranslation("hemomancy_heart", "Vascularium");
		addBannerTranslation("hemomancy_veins", "Veins");
		addBannerTranslation("hemomancy_chitinite", "Chitinite");

		add("item.hemomancy.heart_pattern.desc", "Vascularium Crest");
		add("item.hemomancy.veins_pattern.desc", "Vein Border");
		add("item.hemomancy.chitinite_pattern.desc", "Chitinite");

		addKeyBindTranslations();

		add("itemGroup.hemomancy", "Hemomancy the Sanguis Noctis");

		add("fluid.hemomancy.blood", "Blood");

		for (RegistryObject<EntityType<?>> e : EntityInit.ENTITY_TYPES.getEntries()) {
			addEntityType(e,
					HLTextUtils.convertInitToLang(e.get().getDescriptionId().replace("entity.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
			addBlock(b,
					HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
			addBlock(b,
					HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
		}

		for (RegistryObject<Block> b : BlockInit.SLABBLOCKS.getEntries()) {
			addBlock(b,
					HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
		}

		for (RegistryObject<Block> b : BlockInit.STAIRBLOCKS.getEntries()) {
			addBlock(b,
					HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
		}

		for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
			addBlock(b,
					HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.MODELEDBLOCKS.getEntries()) {
			addBlock(b,
					HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.OBJBLOCKS.getEntries()) {
			addBlock(b,
					HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.COLUMNBLOCKS.getEntries()) {
			addBlock(b,
					HLTextUtils.convertInitToLang(b.get().asItem().getDescriptionId().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.BASEITEMS.getEntries()) {
			addItem(i,
					HLTextUtils.convertInitToLang(i.get().asItem().getDescriptionId().replace("item.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.HANDHELDITEMS.getEntries()) {
			addItem(i,
					HLTextUtils.convertInitToLang(i.get().asItem().getDescriptionId().replace("item.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.SPECIALITEMS.getEntries()) {
			addItem(i,
					HLTextUtils.convertInitToLang(i.get().asItem().getDescriptionId().replace("item.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.SPAWNEGGS.getEntries()) {
			addItem(i,
					HLTextUtils.convertInitToLang(i.get().asItem().getDescriptionId().replace("item.hemomancy.", "")));
		}

		for (RegistryObject<MobEffect> i : PotionInit.EFFECTS.getEntries()) {
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

}
