package com.vincenthuto.hemomancy.data;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
		 generator.addProvider(true,new LootGenerator(generator,helper));
		 generator.addProvider(true,new BlockTagGenerator(generator, helper));
		generator.addProvider(true,new RecipeGenerator(generator,helper));
		generator.addProvider(true,new EntityTagGenerator(generator, helper));
	}

	private static void registerClientProviders(DataGenerator generator, GatherDataEvent event) {
		ExistingFileHelper helper = event.getExistingFileHelper();
		generator.addProvider(true,new BlockStateGenerator(generator, helper));
		generator.addProvider(true,new ItemModelGenerator(generator, helper));
		generator.addProvider(true, new LanguageGenerator(generator,helper));
	}
}