package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * The door into the Priestess's tendril drawing, which is package-private here, for other bosses' renderers: the lit
 * textured core, the additive glow shell, the tapering radii and the soft floor halo. Centre lines are in model pixels,
 * as {@link PriestessTendrilRules#integrate} produces them. The Spawn Mother's lances, nets and binding tendrils are
 * drawn through here so they read as the same living tissue in her own colours.
 */
public final class TendrilTubeBrush {
	private TendrilTubeBrush() {
	}

	/** Radius per station, tapering from root to tip with a few swollen knots. */
	public static float[] taper(int stations, float root, float tip, float knots, long seed) {
		return TendrilTube.taper(stations, root, tip, knots, seed);
	}

	/** The lit eight-sided core; its texture runs the length. */
	public static void core(PoseStack.Pose pose, VertexConsumer out, float[] line, float[] radii, float vOffset, int light,
			int overlay, float shade) {
		TendrilTube.core(pose, out, line, radii, vOffset, light, overlay, shade);
	}

	/** The additive shell; alpha per station lets a pulse travel along it. */
	public static void glow(PoseStack.Pose pose, VertexConsumer out, float[] line, float[] radii, float widthScale, float red,
			float green, float blue, float[] alpha) {
		TendrilTube.glow(pose, out, line, radii, widthScale, red, green, blue, alpha);
	}

	/** A soft additive disc in the pose's XY plane, fading to nothing at its rim; sizes in pixels. */
	public static void halo(PoseStack.Pose pose, VertexConsumer out, float cx, float cy, float cz, float width, float height,
			float red, float green, float blue, float alpha) {
		TendrilTube.halo(pose, out, cx, cy, cz, width, height, red, green, blue, alpha);
	}
}
