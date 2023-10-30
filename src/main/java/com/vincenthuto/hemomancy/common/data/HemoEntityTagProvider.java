package com.vincenthuto.hemomancy.common.data;

import java.util.concurrent.CompletableFuture;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.registry.EntityInit;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

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
		tag(EntityInit.FUNGAL_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE,
				ForgeRegistries.ENTITY_TYPES.getKey(EntityInit.fungling.get())));
		tag(EntityInit.FERRIC_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE,
				ForgeRegistries.ENTITY_TYPES.getKey(EntityInit.fungling.get())));
		tag(EntityInit.NEUROTIC_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE,
				ForgeRegistries.ENTITY_TYPES.getKey(EntityInit.fungling.get())));
		tag(EntityInit.UMBRAL_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE,
				ForgeRegistries.ENTITY_TYPES.getKey(EntityInit.fungling.get())));
		tag(EntityInit.INCANDESCENT_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE,
				ForgeRegistries.ENTITY_TYPES.getKey(EntityInit.fungling.get())));
		tag(EntityInit.FRIGID_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE,
				ForgeRegistries.ENTITY_TYPES.getKey(EntityInit.fungling.get())));
		tag(EntityInit.FERVENT_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE,
				ForgeRegistries.ENTITY_TYPES.getKey(EntityInit.fungling.get())));
		tag(EntityInit.RUINOUS_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE,
				ForgeRegistries.ENTITY_TYPES.getKey(EntityInit.fungling.get())));
		tag(EntityInit.VIVACIOUS_TAG).add(ResourceKey.create(Registries.ENTITY_TYPE,
				ForgeRegistries.ENTITY_TYPES.getKey(EntityInit.fungling.get())));
	}

}
