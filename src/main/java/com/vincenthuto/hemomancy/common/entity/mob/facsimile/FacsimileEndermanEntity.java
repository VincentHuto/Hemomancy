package com.vincenthuto.hemomancy.common.entity.mob.facsimile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

/** The Gardens' copy of an enderman. The hive never taught it to carry blocks, so it leaves the terrain alone. */
public class FacsimileEndermanEntity extends EnderMan {
	public FacsimileEndermanEntity(EntityType<? extends FacsimileEndermanEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return EnderMan.createAttributes();
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		// Vanilla's take-block and leave-block goals are package-private; drop them by name.
		goalSelector.removeAllGoals(goal -> goal.getClass().getSimpleName().endsWith("BlockGoal"));
	}
}
