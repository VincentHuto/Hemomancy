package com.huto.hemomancy.data;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.init.PotionInit;
import com.hutoslib.util.TextFormatingUtil;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.RegistryObject;

public class GeneratorLanguage extends LanguageProvider {
	public GeneratorLanguage(DataGenerator gen) {
		super(gen, Hemomancy.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		addArmBannerTranslation("leather");
		addArmBannerTranslation("iron");
		addArmBannerTranslation("gold");
		addArmBannerTranslation("diamond");
		addArmBannerTranslation("netherite");
		addArmBannerTranslation("chitinite");

		addBannerTranslation("hemomancy_heart", "Vascularium");
		addBannerTranslation("hemomancy_veins", "Veins");
		add("item.hemomancy.heart_pattern.desc", "Vascularium Crest");
		add("item.hemomancy.veins_pattern.desc", "Vein Border");
		addKeyBindTranslations();

		add("itemGroup.hemomancy", "Hemomancy; Sanguine Mastery");

		for (RegistryObject<EntityType<?>> e : EntityInit.ENTITY_TYPES.getEntries()) {
			addEntityType(e,
					TextFormatingUtil.convertInitToLang(e.get().getTranslationKey().replace("entity.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
			addBlock(b, TextFormatingUtil
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
			addBlock(b, TextFormatingUtil
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
			addBlock(b, TextFormatingUtil
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.MODELEDBLOCKS.getEntries()) {
			addBlock(b, TextFormatingUtil
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.OBJBLOCKS.getEntries()) {
			addBlock(b, TextFormatingUtil
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.COLUMNBLOCKS.getEntries()) {
			addBlock(b, TextFormatingUtil
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.HANDHELDITEMS.getEntries()) {
			addItem(i, TextFormatingUtil
					.convertInitToLang(i.get().asItem().getTranslationKey().replace("item.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.SPECIALITEMS.getEntries()) {
			addItem(i, TextFormatingUtil
					.convertInitToLang(i.get().asItem().getTranslationKey().replace("item.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.SPAWNEGGS.getEntries()) {
			addItem(i, TextFormatingUtil
					.convertInitToLang(i.get().asItem().getTranslationKey().replace("item.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.BASEITEMS.getEntries()) {
			addItem(i, TextFormatingUtil
					.convertInitToLang(i.get().asItem().getTranslationKey().replace("item.hemomancy.", "")));
		}

		for (RegistryObject<Effect> i : PotionInit.EFFECTS.getEntries()) {
			add("item.minecraft.potion.effect.potion_of_" + i.getId().getPath(),
					"Potion of " + TextFormatingUtil.convertInitToLang(i.getId().getPath()));
			add("item.minecraft.splash_potion.effect.potion_of_" + i.getId().getPath(),
					"Spash Potion of " + TextFormatingUtil.convertInitToLang(i.getId().getPath()));
			add("item.minecraft.lingering_potion.effect.potion_of_" + i.getId().getPath(),
					"Lingering Potion of " + TextFormatingUtil.convertInitToLang(i.getId().getPath()));
			add("item.minecraft.tipped_arrow.effect.potion_of_" + i.getId().getPath(),
					"Arrow of " + TextFormatingUtil.convertInitToLang(i.getId().getPath()));
			addEffect(() -> i.get(), TextFormatingUtil.convertInitToLang(i.getId().getPath()));
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
		add("key.hemomancy.runebinderpickup.desc", "Toggle Rune Binder Pickup");
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
				"Black " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.red",
				"Red " + TextFormatingUtil.convertInitToLang("_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.green",
				"Green " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.brown",
				"Brown " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.blue",
				"Blue " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.purple",
				"Purple " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.cyan",
				"Cyan " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.silver",
				"Light Gray " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.gray",
				"Gray " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.pink",
				"Pink " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.lime",
				"Lime " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.yellow",
				"Yellow " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.lightBlue",
				"Light " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.magenta",
				"Magenta " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.orange",
				"Orange " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.white",
				"White " + TextFormatingUtil.convertInitToLang(prefix + "_arm_banner"));
	}
}
