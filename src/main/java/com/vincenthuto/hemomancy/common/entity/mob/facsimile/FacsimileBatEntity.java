package com.vincenthuto.hemomancy.common.entity.mob.facsimile;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;

/** The Gardens' copy of a bat: it flies and roosts like one, and is fungus underneath. */
public class FacsimileBatEntity extends Bat {
	public FacsimileBatEntity(EntityType<? extends FacsimileBatEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Bat.createAttributes();
	}

	/** Anywhere open in the Gardens' air: they are its fauna, not cave dwellers waiting for the dark. */
	public static boolean canSpawnHere(EntityType<FacsimileBatEntity> type, LevelAccessor level, MobSpawnType reason,
			BlockPos pos, RandomSource random) {
		return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
	}
}
