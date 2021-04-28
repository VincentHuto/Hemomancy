package com.huto.hemomancy.data;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.font.ModTextFormatting;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.init.PotionInit;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
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

		add("itemGroup.hemomancy", "Hemomancy; Sanguine Mastery");

		for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
			addBlock(b, ModTextFormatting
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
			addBlock(b, ModTextFormatting
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
			addBlock(b, ModTextFormatting
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.MODELEDBLOCKS.getEntries()) {
			addBlock(b, ModTextFormatting
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.OBJBLOCKS.getEntries()) {
			addBlock(b, ModTextFormatting
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.COLUMNBLOCKS.getEntries()) {
			addBlock(b, ModTextFormatting
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.HANDHELDITEMS.getEntries()) {
			addItem(i, ModTextFormatting
					.convertInitToLang(i.get().asItem().getTranslationKey().replace("item.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.SPECIALITEMS.getEntries()) {
			addItem(i, ModTextFormatting
					.convertInitToLang(i.get().asItem().getTranslationKey().replace("item.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.SPAWNEGGS.getEntries()) {
			addItem(i, ModTextFormatting
					.convertInitToLang(i.get().asItem().getTranslationKey().replace("item.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.BASEITEMS.getEntries()) {
			addItem(i, ModTextFormatting
					.convertInitToLang(i.get().asItem().getTranslationKey().replace("item.hemomancy.", "")));
		}

		for (RegistryObject<Effect> i : PotionInit.EFFECTS.getEntries()) {

			addEffect(() -> i.get(), ModTextFormatting.convertInitToLang(i.getId().getPath()));
		}

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
				"Black " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.red",
				"Red " + ModTextFormatting.convertInitToLang("_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.green",
				"Green " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.brown",
				"Brown " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.blue",
				"Blue " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.purple",
				"Purple " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.cyan",
				"Cyan " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.silver",
				"Light Gray " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.gray",
				"Gray " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.pink",
				"Pink " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.lime",
				"Lime " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.yellow",
				"Yellow " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.lightBlue",
				"Light " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.magenta",
				"Magenta " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.orange",
				"Orange " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
		add("item.hemomancy." + prefix + "_arm_banner.white",
				"White " + ModTextFormatting.convertInitToLang(prefix + "_arm_banner"));
	}
}
