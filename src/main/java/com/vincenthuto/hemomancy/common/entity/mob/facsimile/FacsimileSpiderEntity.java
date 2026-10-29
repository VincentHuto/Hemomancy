package com.vincenthuto.hemomancy.common.entity.mob.facsimile;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.DifficultyInstance;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

/** The Gardens' copy of a spider: eight legs, not all of them the same length. */
public class FacsimileSpiderEntity extends Spider {
	public FacsimileSpiderEntity(EntityType<? extends FacsimileSpiderEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Spider.createAttributes();
	}

	/** A vanilla spider sometimes arrives carrying a skeleton; the Gardens hold no vanilla life, so this one never does. */
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
			SpawnGroupData data) {
		SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
		for (Entity rider : List.copyOf(getPassengers())) {
			rider.stopRiding();
			rider.discard();
		}
		return result;
	}
}
