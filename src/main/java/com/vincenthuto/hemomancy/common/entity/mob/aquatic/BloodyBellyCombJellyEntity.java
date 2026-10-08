package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class BloodyBellyCombJellyEntity extends PelagicAnimal {
    public BloodyBellyCombJellyEntity(EntityType<? extends BloodyBellyCombJellyEntity> type, Level level) { super(type, level); }
    @Override public Species habitat() { return Species.COMB_JELLY; }
}
