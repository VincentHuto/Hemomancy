package com.vincenthuto.hemomancy.client.model.entity.npc.court;

import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * A Brined Court body: the shared jointed humanoid ({@link CourtHumanoidModel}) or a generator-built body on the same
 * joint conventions (the Kindled elder). The generated subclasses are written by the role generators in
 * {@code tools/model_export/court_*.py}; their poses live in the {@code _anim.java} beside each generator.
 */
public abstract class CourtMemberModel extends HierarchicalModel<CourtMemberEntity> {
	/** Rotations a pose writes, blended toward from what the stance set. */
	protected static void toward(ModelPart part, float weight, float x, float y, float z) {
		part.xRot = Mth.lerp(weight, part.xRot, x);
		part.yRot = Mth.lerp(weight, part.yRot, y);
		part.zRot = Mth.lerp(weight, part.zRot, z);
	}

	/** In, held, out: 0 before a, eased to 1 by b, held to c, eased back to 0 by d. */
	protected static float window(float t, float a, float b, float c, float d) {
		return HemoEasing.smoothstep(a, b, t) * (1.0F - HemoEasing.smoothstep(c, d, t));
	}

	protected static void set(ModelPart part, float x, float y, float z) {
		part.xRot = x;
		part.yRot = y;
		part.zRot = z;
	}

	protected static void add(ModelPart part, float x, float y, float z) {
		part.xRot += x;
		part.yRot += y;
		part.zRot += z;
	}
}
