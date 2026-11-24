package com.vincenthuto.hemomancy.common.rite.sigil;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * An awakened form peels up off its pattern and holds a point over the pattern's centre: a small bob and a slow
 * turn in place, at a rate that is the sigil's own.
 */
public final class AwakenedIchorianSigilMotion {
	public static final int PEEL_TICKS = 30;
	/** Above the surface the form rose from. */
	public static final float HOVER_HEIGHT = 2.2F;
	public static final float BOB_AMPLITUDE = 0.08F;
	public static final float BOB_PERIOD_TICKS = 80.0F;
	private static final float DEFAULT_TURN_RATE = 0.5F;

	private AwakenedIchorianSigilMotion() {
	}

	/**
	 * False for a form's save from before it hovered (it has only the old orbit keys). Read here rather than on the
	 * entity so the check loads no entity class.
	 */
	public static boolean hasSavedHover(net.minecraft.nbt.CompoundTag tag) {
		return tag.contains("HoverX", net.minecraft.nbt.Tag.TAG_ANY_NUMERIC)
				&& tag.contains("HoverY", net.minecraft.nbt.Tag.TAG_ANY_NUMERIC)
				&& tag.contains("HoverZ", net.minecraft.nbt.Tag.TAG_ANY_NUMERIC);
	}

	public static float peelProgress(int ageTicks) {
		return Math.max(0.0F, Math.min(1.0F, ageTicks / (float) PEEL_TICKS));
	}

	public static Position smoothStep(Position current, Position target, int remainingSteps) {
		if (remainingSteps <= 1) return target;
		double progress = 1.0D / remainingSteps;
		return new Position(
				HemoEasing.lerp(current.x(), target.x(), progress),
				HemoEasing.lerp(current.y(), target.y(), progress),
				HemoEasing.lerp(current.z(), target.z(), progress));
	}

	/** The fixed point the form holds: the sigil's node centroid in the world, HOVER_HEIGHT above surfaceY. */
	public static Position hoverPoint(IchorianSigilDefinition sigil,
			double placementX, double surfaceY, double placementZ) {
		double sumX = 0.0D;
		double sumZ = 0.0D;
		for (IchorianSigilDefinition.Node node : sigil.nodes()) {
			sumX += node.x();
			sumZ += node.z();
		}
		int count = sigil.nodes().size();
		double centroidX = count == 0 ? 0.0D : sumX / count;
		double centroidZ = count == 0 ? 0.0D : sumZ / count;
		return new Position(placementX + centroidX, surfaceY + HOVER_HEIGHT, placementZ + centroidZ);
	}

	/** Peel from the origin to the hover point, then hover there with a vertical bob only. */
	public static Position position(double originX, double originY, double originZ,
			double hoverX, double hoverY, double hoverZ, int ageTicks) {
		int hoverTicks = Math.max(0, ageTicks - PEEL_TICKS);
		double bobY = hoverY + BOB_AMPLITUDE * Math.sin(hoverTicks * Math.PI * 2.0D / BOB_PERIOD_TICKS);
		double peel = peelProgress(ageTicks);
		if (peel >= 1.0D) return new Position(hoverX, bobY, hoverZ);
		return new Position(
				HemoEasing.lerp(originX, hoverX, peel),
				HemoEasing.lerp(originY, bobY, peel),
				HemoEasing.lerp(originZ, hoverZ, peel));
	}

	/** Degrees. A slow turn in place; the rate is the sigil's own. */
	public static float turnYaw(ResourceLocation sigilId, float phaseDegrees, float ageTicks) {
		return (float) Mth.wrapDegrees(phaseDegrees + (double) turnRate(sigilId) * ageTicks);
	}

	/** Degrees per tick: the shunt's quickness down to the seal's stillness. */
	public static float turnRate(ResourceLocation sigilId) {
		String path = sigilId == null ? "" : sigilId.getPath();
		return switch (path) {
			case "shunt" -> 1.2F;
			case "suture" -> 0.9F;
			case "lens" -> 0.7F;
			case "mnemonic" -> 0.6F;
			case "hematic_lattice" -> 0.5F;
			case "cage" -> 0.45F;
			case "bastion" -> 0.35F;
			case "reservoir" -> 0.3F;
			case "seal" -> 0.12F;
			default -> DEFAULT_TURN_RATE;
		};
	}

	public record Position(double x, double y, double z) {
	}
}
