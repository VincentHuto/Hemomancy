package com.vincenthuto.hemomancy.data;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.EntityInit;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

public class EntityTagGenerator extends TagsProvider<EntityType<?>> {

	public EntityTagGenerator(DataGenerator pGenerator, ExistingFileHelper existingFileHelper) {
		super(pGenerator, Registry.ENTITY_TYPE, Hemomancy.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags() {
		this.tag(EntityInit.FUNGAL_TAG).add(EntityInit.fungling.get());
		this.tag(EntityInit.FERRIC_TAG).add(EntityInit.fungling.get());
		this.tag(EntityInit.NEUROTIC_TAG).add(EntityInit.fungling.get());
		this.tag(EntityInit.UMBRAL_TAG).add(EntityInit.fungling.get());
		this.tag(EntityInit.INCANDESCENT_TAG).add(EntityInit.fungling.get());
		this.tag(EntityInit.FRIGID_TAG).add(EntityInit.fungling.get());
		this.tag(EntityInit.FERVENT_TAG).add(EntityInit.fungling.get());
		this.tag(EntityInit.RUINOUS_TAG).add(EntityInit.fungling.get());
		this.tag(EntityInit.VIVACIOUS_TAG).add(EntityInit.fungling.get());
	}

	@Override
	public String getName() {
		return "Entity Type Tags";
	}

}
