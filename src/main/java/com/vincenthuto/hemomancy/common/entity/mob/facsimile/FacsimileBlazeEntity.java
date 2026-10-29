package com.vincenthuto.hemomancy.common.entity.mob.facsimile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;

/** The Gardens' copy of a blaze: the hive learned its fire before it learned its body. */
public class FacsimileBlazeEntity extends Blaze {
	public FacsimileBlazeEntity(EntityType<? extends FacsimileBlazeEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Blaze.createAttributes();
	}
}
