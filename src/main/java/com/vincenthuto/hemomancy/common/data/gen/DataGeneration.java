package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class DataGeneration {

	@SubscribeEvent
	public static void generate(GatherDataEvent event) {

		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		generator.addProvider(event.includeClient(),
				new HemoBlockStateProvider(packOutput, event.getExistingFileHelper()));
		generator.addProvider(event.includeClient(),
				new HemoItemModelProvider(packOutput, event.getExistingFileHelper()));
		generator.addProvider(event.includeClient(), new HemoLanguageProvider(packOutput, "en_us"));
//		generator.addProvider(event.includeServer(), new HemoBlockTagProvider(packOutput, lookupProvider, event.getExistingFileHelper()));
//		generator.addProvider(event.includeServer(), new HemoRecipeProvider(packOutput, event.getExistingFileHelper()));
//		generator.addProvider(event.includeServer(), new HemoEntityTagProvider(packOutput, lookupProvider, event.getExistingFileHelper()));
//		generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Set.of(), List.of(new SubProviderEntry(HemoEntityLootProvider::new, LootContextParamSets.ENTITY),
//				new SubProviderEntry(HemoBlockLootTableProvider::new, LootContextParamSets.BLOCK))));
	}
}
