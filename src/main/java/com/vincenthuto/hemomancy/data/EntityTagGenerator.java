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
	public String getName() {
		return "Entity Type Tags";
	}

	@Override
	protected void addTags() {
		this.tag(EntityInit.FUNGAL).add(EntityInit.fungling.get());
		this.tag(EntityInit.FERRIC).add(EntityInit.fungling.get());
		this.tag(EntityInit.NEUROTIC).add(EntityInit.fungling.get());
		this.tag(EntityInit.UMBRAL).add(EntityInit.fungling.get());
		this.tag(EntityInit.INCANDESCENT).add(EntityInit.fungling.get());
		this.tag(EntityInit.FRIGID).add(EntityInit.fungling.get());
		this.tag(EntityInit.FERVENT).add(EntityInit.fungling.get());
		this.tag(EntityInit.RUINOUS).add(EntityInit.fungling.get());
		this.tag(EntityInit.VIVACIOUS).add(EntityInit.fungling.get());

	}

}
