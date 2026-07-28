package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;

public final class AwakenedIchorianSigilMotion {
	public static final int PEEL_TICKS = 30;
	private static final double ORBIT_RADIANS_PER_TICK = Math.PI / 120.0D;

	private AwakenedIchorianSigilMotion() {
	}

	public static float peelProgress(int ageTicks) {
		return Math.max(0.0F, Math.min(1.0F, ageTicks / (float) PEEL_TICKS));
	}

	public static Position smoothStep(Position current, Position target, int remainingSteps) {
		if (remainingSteps <= 1) return target;
		double progress = 1.0D / remainingSteps;
		return new Position(
				lerp(current.x(), target.x(), progress),
				lerp(current.y(), target.y(), progress),
				lerp(current.z(), target.z(), progress));
	}

	public static Position position(double originX, double originY, double originZ,
			double centerX, double orbitY, double centerZ,
			double orbitRadius, double startingAngle, int ageTicks) {
		return position(null, originX, originY, originZ, centerX, orbitY, centerZ,
				orbitRadius, startingAngle, ageTicks);
	}

	public static Position position(ResourceLocation sigilId,
			double originX, double originY, double originZ,
			double centerX, double orbitY, double centerZ,
			double orbitRadius, double startingAngle, int ageTicks) {
		int orbitTicks = Math.max(0, ageTicks - PEEL_TICKS);
		Offset offset = offsetFor(sigilId, orbitRadius, startingAngle, orbitTicks);
		double orbitX = centerX + offset.x();
		double orbitZ = centerZ + offset.z();
		double orbitBobY = orbitY + offset.y();
		double peel = peelProgress(ageTicks);
		return new Position(
				lerp(originX, orbitX, peel),
				lerp(originY, orbitBobY, peel),
				lerp(originZ, orbitZ, peel));
	}

	private static Offset offsetFor(ResourceLocation sigilId, double radius,
			double startingAngle, int orbitTicks) {
		String path = sigilId == null ? "" : sigilId.getPath();
		return switch (path) {
			case "reservoir" -> reservoir(radius, startingAngle, orbitTicks);
			case "bastion" -> bastion(radius, startingAngle, orbitTicks);
			case "hematic_lattice" -> lattice(radius, startingAngle, orbitTicks);
			case "mnemonic" -> mnemonic(radius, startingAngle, orbitTicks);
			case "suture" -> suture(radius, startingAngle, orbitTicks);
			case "shunt" -> shunt(radius, startingAngle, orbitTicks);
			case "seal" -> seal(radius, startingAngle, orbitTicks);
			case "cage" -> cage(radius, startingAngle, orbitTicks);
			case "lens" -> lens(radius, startingAngle, orbitTicks);
			default -> circular(radius, startingAngle, orbitTicks);
		};
	}

	private static Offset reservoir(double radius, double phase, int ticks) {
		double angle = phase + ticks * Math.PI / 180.0D;
		double breathingRadius = radius * (0.70D + Math.sin(ticks * Math.PI / 60.0D) * 0.08D);
		return polar(breathingRadius, angle,
				-0.55D + Math.cos(ticks * Math.PI / 60.0D) * 0.08D);
	}

	private static Offset bastion(double radius, double phase, int ticks) {
		double contraction = Math.sin(ticks * Math.PI / 26.0D);
		double angle = phase + ticks * Math.PI / 75.0D + contraction * 0.10D;
		double patrolRadius = radius * (0.94D + contraction * 0.05D);
		return polar(patrolRadius, angle, contraction * 0.07D);
	}

	private static Offset lattice(double radius, double phase, int ticks) {
		double angle = phase + ticks * Math.PI / 120.0D;
		return polar(radius * 0.88D, angle, Math.sin(angle * 4.0D) * 0.22D);
	}

	private static Offset mnemonic(double radius, double phase, int ticks) {
		double angle = phase + ticks * Math.PI / 90.0D;
		return bounded(radius, radius * 0.88D * Math.cos(angle),
				0.28D + Math.cos(angle) * 0.12D,
				radius * 0.44D * Math.sin(angle * 2.0D));
	}

	private static Offset suture(double radius, double phase, int ticks) {
		double angle = phase + ticks * Math.PI / 72.0D;
		return bounded(radius,
				radius * 0.80D * Math.cos(angle),
				Math.sin(angle * 6.0D) * 0.18D,
				radius * (0.80D * Math.sin(angle) + 0.12D * Math.sin(angle * 5.0D)));
	}

	private static Offset shunt(double radius, double phase, int ticks) {
		double angle = phase + ticks * Math.PI / 36.0D;
		return bounded(radius, radius * Math.cos(angle),
				0.10D + Math.sin(angle * 2.0D) * 0.22D,
				radius * 0.28D * Math.sin(angle));
	}

	private static Offset seal(double radius, double phase, int ticks) {
		double angle = phase + ticks * Math.PI / 210.0D;
		return polar(radius * 0.25D, angle, -0.05D + Math.sin(angle) * 0.06D);
	}

	private static Offset cage(double radius, double phase, int ticks) {
		double constriction = Math.cos(ticks * Math.PI / 34.0D);
		double angle = phase + ticks * Math.PI / 85.0D
				+ Math.sin(ticks * Math.PI / 22.0D) * 0.08D;
		double constrictingRadius = radius * (0.66D + constriction * 0.07D);
		return polar(constrictingRadius, angle, 0.18D + constriction * 0.08D);
	}

	private static Offset lens(double radius, double phase, int ticks) {
		double angle = phase + ticks * Math.PI / 100.0D;
		return bounded(radius, radius * Math.cos(angle),
				0.70D + Math.sin(angle) * 0.15D,
				radius * 0.55D * Math.sin(angle));
	}

	private static Offset circular(double radius, double phase, int ticks) {
		double angle = phase + ticks * ORBIT_RADIANS_PER_TICK;
		return polar(radius, angle, Math.sin(angle * 2.0D) * 0.16D);
	}

	private static Offset polar(double radius, double angle, double y) {
		return new Offset(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
	}

	private static Offset bounded(double radius, double x, double y, double z) {
		double distance = Math.hypot(x, z);
		if (distance <= radius || distance == 0.0D) return new Offset(x, y, z);
		double scale = radius / distance;
		return new Offset(x * scale, y, z * scale);
	}

	private static double lerp(double start, double end, double progress) {
		return start + (end - start) * progress;
	}

	public record Position(double x, double y, double z) {
	}

	private record Offset(double x, double y, double z) {
	}
}
