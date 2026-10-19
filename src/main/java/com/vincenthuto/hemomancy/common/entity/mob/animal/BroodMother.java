package com.vincenthuto.hemomancy.common.entity.mob.animal;

import net.minecraft.world.phys.Vec3;

/** Something that bore brood funglings and reacts when one of them dies (the Mycophant grieves). */
public interface BroodMother {
	void onBroodLost(FunglingEntity brood, Vec3 where);
}
