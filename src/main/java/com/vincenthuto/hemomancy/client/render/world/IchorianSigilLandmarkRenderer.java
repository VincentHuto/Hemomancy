package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilLandmarkGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilOrganicGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRenderPalette;

import java.util.List;

/**
 * Shared procedural landmark renderer for grounded and awakened Ichorian
 * anatomy. The caller supplies orientation; grounded landmarks rotate their
 * authored frontal axis upward.
 */
public final class IchorianSigilLandmarkRenderer {
	private IchorianSigilLandmarkRenderer() {
	}

	public static void render(VertexConsumer consumer, PoseStack stack,
			IchorianSigilLandmarkGeometry.Recipe recipe, float radius,
			int authoredColor, float time, long seed, float integrity,
			boolean glow, boolean groundFacing) {
		stack.pushPose();
		if (groundFacing) stack.mulPose(Axis.XP.rotationDegrees(90.0F));
		radius *= IchorianSigilOrganicGeometry.heartbeat(time);
		float damage = Math.max(0.0F, Math.min(1.0F, integrity));
		float dryScale = 0.72F + damage * 0.28F;
		stack.scale(dryScale, dryScale, dryScale);
		List<IchorianSigilLandmarkGeometry.Primitive> primitives =
				glow ? recipe.glow() : recipe.core();
		for (int index = 0; index < primitives.size(); index++) {
			var primitive = primitives.get(index);
			var color = color(primitive.layer(), authoredColor, glow, damage);
			float alpha = glow ? 0.14F * damage : 0.90F;
			if (primitive.layer() == IchorianSigilLandmarkGeometry.Layer.MEMBRANE) alpha *= 0.34F;
			if (primitive.layer() == IchorianSigilLandmarkGeometry.Layer.HIGHLIGHT) alpha = 0.96F;
			stack.pushPose();
			stack.translate(primitive.x() * radius, primitive.y() * radius,
					primitive.z() * radius);
			stack.scale(primitive.scaleX(), primitive.scaleY(), primitive.scaleZ());
			SanguineFormationProjectionRenderer.renderSphere(
					consumer, stack.last().pose(), radius, time,
					seed + index * 31L, color.red(), color.green(), color.blue(), alpha);
			stack.popPose();
		}
		stack.popPose();
	}

	private static IchorianSigilRenderPalette.Color color(
			IchorianSigilLandmarkGeometry.Layer layer, int authoredColor,
			boolean glow, float integrity) {
		IchorianSigilRenderPalette.Color base = switch (layer) {
			case TISSUE -> IchorianSigilRenderPalette.tissue(glow);
			case ICHOR -> IchorianSigilRenderPalette.authoredIchor(authoredColor, glow);
			case MEMBRANE -> IchorianSigilRenderPalette.membrane();
			case IRIS -> new IchorianSigilRenderPalette.Color(0.46F, 0.008F, 0.014F);
			case PUPIL -> new IchorianSigilRenderPalette.Color(0.006F, 0.0F, 0.0F);
			case HIGHLIGHT -> new IchorianSigilRenderPalette.Color(0.92F, 0.72F, 0.74F);
		};
		if (integrity >= 0.999F) return base;
		float gray = (base.red() + base.green() + base.blue()) / 3.0F;
		float saturation = 0.22F + integrity * 0.78F;
		float brightness = 0.20F + integrity * 0.80F;
		return new IchorianSigilRenderPalette.Color(
				(gray + (base.red() - gray) * saturation) * brightness,
				(gray + (base.green() - gray) * saturation) * brightness,
				(gray + (base.blue() - gray) * saturation) * brightness);
	}
}
