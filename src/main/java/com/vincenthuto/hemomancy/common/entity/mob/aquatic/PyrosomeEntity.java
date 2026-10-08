package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** The whole hollow colony is one animal; zooids are model parts, never entities. */
public class PyrosomeEntity extends PelagicAnimal {
    public PyrosomeEntity(EntityType<? extends PyrosomeEntity> type, Level level) { super(type, level); }
    @Override public Species habitat() { return Species.PYROSOME; }
    public float pulse(float age, int ring) { return .5F + .5F * (float)Math.sin(age * .12F - ring * .8F); }
}
