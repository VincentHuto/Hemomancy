package com.hemomancy.data;

import com.hemomancy.Hemomancy;
import com.hemomancy.font.ModTextFormatting;
import com.hemomancy.registries.BlockInit;
import com.hemomancy.registries.ItemInit;

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
		for (RegistryObject<Block> b : BlockInit.BLOCKS.getEntries()) {
			addBlock(b, ModTextFormatting
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
			addBlock(b, ModTextFormatting
					.convertInitToLang(b.get().asItem().getTranslationKey().replace("block.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.ITEMS.getEntries()) {
			addItem(i, ModTextFormatting
					.convertInitToLang(i.get().asItem().getTranslationKey().replace("item.hemomancy.", "")));
		}
		for (RegistryObject<Item> i : ItemInit.BASEITEMS.getEntries()) {
			addItem(i, ModTextFormatting
					.convertInitToLang(i.get().asItem().getTranslationKey().replace("item.hemomancy.", "")));
		}
	}
}
