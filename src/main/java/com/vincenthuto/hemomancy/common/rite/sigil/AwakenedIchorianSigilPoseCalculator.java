package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AwakenedIchorianSigilPoseCalculator {
	private AwakenedIchorianSigilPoseCalculator() {
	}

	public static AwakenedIchorianSigilPose calculate(
			IchorianSigilDefinition definition, float ageTicks) {
		return calculate(definition, ageTicks, 0.0F);
	}

	public static AwakenedIchorianSigilPose calculate(
			IchorianSigilDefinition definition, float ageTicks, float movementSpeed) {
		return calculate(definition, ageTicks, ageTicks, movementSpeed);
	}

	public static AwakenedIchorianSigilPose calculate(
			IchorianSigilDefinition definition, float morphAgeTicks,
			float animationAgeTicks, float movementSpeed) {
		IchorianSigilAnatomy anatomy = definition.awakenedForm().orElseThrow();
		float detachment = stage(morphAgeTicks, 0, 10);
		float migration = stage(morphAgeTicks, 6, 32);
		float quickening = stage(morphAgeTicks, 18, 40);
		float extent = tierScale(definition.tier());
		float radiusScale = radiusScale(definition.tier());
		List<IchorianSigilAnatomy.Landmark> authored = anatomy.landmarks().stream()
				.sorted(Comparator.comparingInt(IchorianSigilAnatomy.Landmark::source)).toList();
		List<Vec3> targetPositions = normalize(
				authored.stream().map(IchorianSigilAnatomy.Landmark::position).toList(), extent);
		List<AwakenedIchorianSigilPose.Landmark> landmarks = new ArrayList<>(authored.size());
		for (int source = 0; source < authored.size(); source++) {
			IchorianSigilAnatomy.Landmark landmark = authored.get(source);
			Vec3 ground = normalizedGroundPosition(definition, source);
			Vec3 target = targetPositions.get(source);
			Vec3 position = ground.lerp(target, migration);
			float wave = casteWave(anatomy.animation().style(), landmark, animationAgeTicks);
			double offset = wave * anatomy.animation().flex() * quickening * 0.08D;
			position = position.add(offset * ((source & 1) == 0 ? 1 : -1),
					offset * roleVertical(landmark.role()), offset * 0.45D);
			position = position.add(flightArticulation(
					anatomy.animation().style(), landmark, animationAgeTicks,
					movementSpeed, extent).scale(quickening));
			float radius = landmark.radius() * radiusScale
					* (1.0F + wave * anatomy.animation().pulse() * quickening * 0.12F);
			landmarks.add(new AwakenedIchorianSigilPose.Landmark(
					source, position, landmark.role(), radius, quickening));
		}
		List<AwakenedIchorianSigilPose.Vessel> primary = new ArrayList<>();
		for (int index = 1; index < landmarks.size(); index++) {
			primary.add(new AwakenedIchorianSigilPose.Vessel(
					index - 1, index, 0.055F * extent, detachment));
		}
		List<AwakenedIchorianSigilPose.Vessel> secondary = anatomy.vessels().stream()
				.map(vessel -> new AwakenedIchorianSigilPose.Vessel(
						vessel.from(), vessel.to(), vessel.thickness() * extent, quickening))
				.toList();
		List<AwakenedIchorianSigilPose.Membrane> membranes = anatomy.membranes().stream()
				.map(membrane -> new AwakenedIchorianSigilPose.Membrane(
						membrane.a(), membrane.b(), membrane.c(), quickening))
				.toList();
		return new AwakenedIchorianSigilPose(landmarks, primary, secondary, membranes,
				detachment, migration, quickening, extent);
	}

	static Vec3 normalizedGroundPosition(IchorianSigilDefinition definition, int source) {
		List<Vec3> points = definition.nodes().stream()
				.map(node -> new Vec3(node.x(), 0, node.z())).toList();
		return normalize(points, tierScale(definition.tier())).get(source);
	}

	public static float tierScale(int tier) {
		return switch (Mth.clamp(tier, 1, 5)) {
			case 1 -> 0.8F;
			case 2 -> 1.0F;
			case 3 -> 1.2F;
			case 4 -> 1.4F;
			default -> 1.6F;
		};
	}

	public static float radiusScale(int tier) {
		return switch (Mth.clamp(tier, 1, 5)) {
			case 1 -> 0.8F;
			case 2 -> 0.9F;
			case 3 -> 1.0F;
			case 4 -> 1.1F;
			default -> 1.2F;
		};
	}

	private static List<Vec3> normalize(List<Vec3> points, float extent) {
		double minX = points.stream().mapToDouble(point -> point.x).min().orElse(0);
		double maxX = points.stream().mapToDouble(point -> point.x).max().orElse(0);
		double minY = points.stream().mapToDouble(point -> point.y).min().orElse(0);
		double maxY = points.stream().mapToDouble(point -> point.y).max().orElse(0);
		double minZ = points.stream().mapToDouble(point -> point.z).min().orElse(0);
		double maxZ = points.stream().mapToDouble(point -> point.z).max().orElse(0);
		double span = Math.max(1.0E-6D, Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ)));
		Vec3 center = new Vec3((minX + maxX) * 0.5D, (minY + maxY) * 0.5D,
				(minZ + maxZ) * 0.5D);
		double factor = extent / span;
		return points.stream().map(point -> point.subtract(center).scale(factor)).toList();
	}

	private static float casteWave(IchorianSigilAnatomy.Style style,
			IchorianSigilAnatomy.Landmark landmark, float age) {
		double speed = 0.09D + style.ordinal() * 0.007D;
		double phase = style.ordinal() * 0.71D + landmark.source() * (0.42D + style.ordinal() * 0.03D);
		if (style == IchorianSigilAnatomy.Style.FIVE_LIPPED_SHUTTER) {
			phase += landmark.source() * 0.8D;
		}
		if (style == IchorianSigilAnatomy.Style.OPTIC_STALK_VEIL) {
			phase -= landmark.source() * 0.18D;
		}
		return (float) Math.sin(age * speed + phase);
	}

	private static double roleVertical(IchorianSigilAnatomy.Role role) {
		return switch (role) {
			case ORGAN -> 0.8D;
			case HOOK, LIMB_TIP, MEMBRANE_TIP -> 0.45D;
			case RIB -> -0.35D;
			default -> 0.15D;
		};
	}

	private static Vec3 flightArticulation(IchorianSigilAnatomy.Style style,
			IchorianSigilAnatomy.Landmark landmark, float age, float movementSpeed,
			float extent) {
		float activity = Mth.clamp(movementSpeed * 18.0F, 0.0F, 1.0F);
		if (activity <= 0.0F) return Vec3.ZERO;
		double roleAmplitude = switch (landmark.role()) {
			case HOOK, LIMB_TIP, MEMBRANE_TIP -> 1.20D;
			case EYE, ORGAN -> 0.62D;
			case RIB -> 0.82D;
			default -> 1.0D;
		};
		double amplitude = (0.045D + extent * 0.012D) * roleAmplitude * activity;
		double phase = age * (0.035D + style.ordinal() * 0.002D)
				+ landmark.source() * 0.86D + style.ordinal() * 0.43D;
		double counter = (landmark.source() & 1) == 0 ? 1.0D : -1.0D;
		return new Vec3(
				Math.sin(phase) * amplitude * counter,
				Math.sin(phase * 0.72D + 0.8D) * amplitude * 0.45D,
				Math.cos(phase + 0.35D) * amplitude * 0.75D * counter);
	}

	private static float stage(float age, float start, float end) {
		float linear = Mth.clamp((age - start) / (end - start), 0.0F, 1.0F);
		return linear * linear * (3.0F - 2.0F * linear);
	}
}
