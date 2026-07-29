package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteFootprintRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAnchorVisualRules;
import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * Shared geometry rules for the visible Cardinal Rite rings and the enclosing
 * exterior field. Keeping both calculations together prevents the Fane shell
 * from drifting inside or outside the ring that visually defines the rite.
 */
public final class CardinalRiteBoundaryGeometry {
	private CardinalRiteBoundaryGeometry() {
	}

	public static boolean shouldRenderExterior(int degree) {
		return degree >= 3;
	}

	public static float boundaryPlaneY(int centerY) {
		return (float) CardinalRiteAnchorVisualRules.ritePlaneY(centerY);
	}

	public static float interactiveRingRadius(int ringIndex) {
		return 3.0F + Math.max(0, ringIndex);
	}

	public static float exteriorRadius(int riteSize, int completedRings, boolean legacy) {
		int ringCount = legacy ? Math.max(1, (riteSize - 1) / 2) : Math.max(0, completedRings);
		if (ringCount == 0) return 0.0F;
		return legacy
				? (float) (riteSize / 2.0D + 1.0D + (ringCount - 1) * 2.0D)
				: interactiveRingRadius(ringCount - 1);
	}

	public static float footprintRadius(List<BlockPos> boundaryPoints, List<BlockPos> sigilPoints) {
		return CardinalRiteFootprintRules.radius(boundaryPoints, sigilPoints);
	}

	public static boolean hasVisibleBeamAt(
			List<CardinalRiteBoundaryProgress.Segment> arcs, double angle) {
		double normalizedAngle = normalizeAngle(angle);
		for (CardinalRiteBoundaryProgress.Segment arc : arcs) {
			if (arc.integrity() <= 0.01F) continue;
			double fromStart = normalizeAngle(
					normalizedAngle - normalizeAngle(arc.startAngle()));
			if (fromStart <= arc.sweepAngle()) return true;
		}
		return false;
	}

	public static float integrityWidth(float healthyWidth, float integrity) {
		float clamped = Math.max(0.0F, Math.min(1.0F, integrity));
		return healthyWidth * (0.22F + 0.78F * clamped);
	}

	public static float integrityBrightness(float integrity) {
		float clamped = Math.max(0.0F, Math.min(1.0F, integrity));
		return 0.12F + 0.88F * clamped;
	}

	public static float arterialHighlight(double angle, float time, int ring) {
		double phase = normalizeAngle(angle - arterialHighlightPosition(time, ring));
		double distance = Math.min(phase, Math.PI * 2.0D - phase);
		double normalized = Math.max(0.0D, 1.0D - distance / 0.42D);
		return (float) (normalized * normalized * (3.0D - 2.0D * normalized));
	}

	public static double arterialHighlightPosition(float time, int ring) {
		return normalizeAngle(time * 0.035D + ring * 0.73D);
	}

	private static double normalizeAngle(double angle) {
		double fullCircle = Math.PI * 2.0D;
		double normalized = angle % fullCircle;
		return normalized < 0.0D ? normalized + fullCircle : normalized;
	}
}
