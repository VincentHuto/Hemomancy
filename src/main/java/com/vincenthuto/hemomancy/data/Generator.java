package com.vincenthuto.hemomancy.data;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Generator {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		if (event.includeServer())
			registerServerProviders(event.getGenerator(), event);

		if (event.includeClient())
			registerClientProviders(event.getGenerator(), event);
	}

	@SuppressWarnings("unused")
	private static void registerServerProviders(DataGenerator generator, GatherDataEvent event) {
		ExistingFileHelper helper = event.getExistingFileHelper();
		 generator.addProvider(new LootGenerator(generator,helper));
		 generator.addProvider(new BlockTagGenerator(generator, helper));
		generator.addProvider(new RecipeGenerator(generator,helper));
	}

	private static void registerClientProviders(DataGenerator generator, GatherDataEvent event) {
		ExistingFileHelper helper = event.getExistingFileHelper();
		generator.addProvider(new BlockStateGenerator(generator, helper));
		generator.addProvider(new ItemModelGenerator(generator, helper));
		generator.addProvider(new LanguageGenerator(generator,helper));
	}
}