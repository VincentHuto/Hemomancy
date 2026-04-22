package com.vincenthuto.hemomancy.common.data.gen;

import java.util.concurrent.CompletableFuture;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class HemoEntityTagProvider extends TagsProvider<EntityType<?>> {

	public HemoEntityTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider,
			ExistingFileHelper existingFileHelper) {
		super(output, Registries.ENTITY_TYPE, lookupProvider, Hemomancy.MOD_ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return "Entity Type Tags";
	}

	@Override
	protected void addTags(Provider p_256380_) {
		tag(EntityInit.FUNGAL_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.fungling.getId()));
		tag(EntityInit.FERRIC_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.fungling.getId()));
		tag(EntityInit.NEUROTIC_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.fungling.getId()));
		tag(EntityInit.UMBRAL_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.fungling.getId()));
		tag(EntityInit.INCANDESCENT_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.fungling.getId()));
		tag(EntityInit.FRIGID_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.fungling.getId()));
		tag(EntityInit.FERVENT_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.fungling.getId()));
		tag(EntityInit.RUINOUS_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.fungling.getId()));
		tag(EntityInit.VIVACIOUS_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.fungling.getId()));

		// All hemomancy monster entities — used by PurityGainEvents for kill rewards
		tag(EntityInit.HEMOMANCY_MOB)
				.add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.fargone.getId()))
				.add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.thirster.getId()))
				.add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.abhorent_thought.getId()))
				.add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.erythromycelium_eruptus.getId()))
				.add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.blood_drunk_puppeteer.getId()))
				.add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.enthralled_doll.getId()))
				.add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.lump_of_thought.getId()))
				.add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.morphling_polyp.getId()))
				.add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.chthonian.getId()))
				.add(ResourceKey.create(Registries.ENTITY_TYPE, EntityInit.chthonian_queen.getId()));
	}

}
