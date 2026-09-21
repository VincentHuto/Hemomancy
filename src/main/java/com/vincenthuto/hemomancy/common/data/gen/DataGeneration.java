package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;


@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class DataGeneration {

	@SubscribeEvent
	public static void generate(GatherDataEvent event) {

		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		generator.addProvider(event.includeClient(),
				new HemoBlockStateProvider(packOutput, event.getExistingFileHelper()));
		generator.addProvider(event.includeClient(),
				new HemoItemModelProvider(packOutput, event.getExistingFileHelper()));
		generator.addProvider(event.includeClient(), new HemoLanguageProvider(packOutput, "en_us"));
		generator.addProvider(event.includeServer(), new CircusPavilionStructureProvider(packOutput));
		// DynastyCastlePieceProvider is intentionally NOT registered: the dynasty castle pieces are now
		// hand-editable assets under src/main/resources/data/hemomancy/structure/dynasty_castle. The
		// generator (DynastyCastlePieces / DynastyCastlePieceProvider) is kept as a dormant regeneration
		// tool -- re-add this line and delete the baked .nbt to rebuild the pieces from code.
	}
}
