package com.huto.hemomancy.data;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.font.ModTextFormatting;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.RegistryObject;

public class GeneratorLanguage extends LanguageProvider {
	public GeneratorLanguage(DataGenerator gen) {
		super(gen, Hemomancy.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		
		
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
	}
}
