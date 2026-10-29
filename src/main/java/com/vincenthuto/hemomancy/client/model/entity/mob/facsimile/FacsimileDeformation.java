package com.vincenthuto.hemomancy.client.model.entity.mob.facsimile;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

/**
 * Applies a facsimile's seeded part scales. The scales are absolute and set on every part every frame, because one
 * model instance draws every entity of its type.
 */
public final class FacsimileDeformation {
	private FacsimileDeformation() {}

	public static void apply(Iterable<ModelPart> parts, Entity entity) {
		long seed = FacsimileDeformationRules.seedOf(entity.getUUID());
		int index = 0;
		for (ModelPart part : parts) {
			float[] scale = FacsimileDeformationRules.scaleFor(seed, index++);
			part.xScale = scale[0];
			part.yScale = scale[1];
			part.zScale = scale[2];
		}
	}
}
