package com.vincenthuto.hemomancy.common.rite.sigil;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic capillary deformation shared by grounded and awakened sigils.
 * Authored nodes remain fixed while the tissue between them flexes.
 */
public final class IchorianSigilOrganicGeometry {
	private IchorianSigilOrganicGeometry() {
	}

	public static Sample sample(double startX, double startY, double startZ,
			double endX, double endY, double endZ,
			float time, long seed, int step, int steps, float baseHalfWidth) {
		int safeSteps = Math.max(1, steps);
		double progress = Math.max(0.0D, Math.min(1.0D, step / (double) safeSteps));
		double x = lerp(startX, endX, progress);
		double y = lerp(startY, endY, progress);
		double z = lerp(startZ, endZ, progress);
		double dx = endX - startX;
		double dz = endZ - startZ;
		double horizontalLength = Math.hypot(dx, dz);
		double envelope = Math.sin(Math.PI * progress);
		double phase = phase(seed);
		double primary = Math.sin(progress * Math.PI * 2.0D + time * 0.075D + phase);
		double secondary = Math.sin(progress * Math.PI * 5.0D - time * 0.043D + phase * 1.7D);
		double lateral = envelope * (primary * 0.075D + secondary * 0.025D);
		if (horizontalLength > 1.0E-6D) {
			x += -dz / horizontalLength * lateral;
			z += dx / horizontalLength * lateral;
		}
		y += envelope * Math.sin(progress * Math.PI * 3.0D
				+ time * 0.061D + phase * 0.73D) * 0.025D;
		float beat = 0.90F + 0.10F * (float) Math.sin(
				time * 0.14D - progress * 2.5D + phase);
		float taper = 0.78F + 0.22F * (float) envelope;
		return new Sample(x, y, z, Math.max(0.001F, baseHalfWidth * beat * taper));
	}

	public static float nodePulse(float time, long seed, int nodeIndex) {
		double phase = phase(seed + nodeIndex * 0x9E3779B97F4A7C15L);
		return 1.0F + 0.10F * (float) Math.sin(time * 0.14D + phase);
	}

	public static List<RibbonJoint> ribbonJoints(List<Sample> samples) {
		if (samples.isEmpty()) return List.of();
		List<RibbonJoint> joints = new ArrayList<>(samples.size());
		for (int index = 0; index < samples.size(); index++) {
			Sample current = samples.get(index);
			Sample previous = samples.get(Math.max(0, index - 1));
			Sample next = samples.get(Math.min(samples.size() - 1, index + 1));
			double dx = next.x() - previous.x();
			double dz = next.z() - previous.z();
			double length = Math.hypot(dx, dz);
			if (length < 1.0E-8D) {
				dx = 1.0D;
				dz = 0.0D;
				length = 1.0D;
			}
			double normalX = -dz / length;
			double normalZ = dx / length;
			joints.add(new RibbonJoint(
					current.x(), current.y(), current.z(),
					current.x() - normalX * current.halfWidth(),
					current.z() - normalZ * current.halfWidth(),
					current.x() + normalX * current.halfWidth(),
					current.z() + normalZ * current.halfWidth()));
		}
		return List.copyOf(joints);
	}

	public static List<RibbonSegment> ribbonSegments(List<Sample> samples) {
		List<RibbonJoint> joints = ribbonJoints(samples);
		if (joints.size() < 2) return List.of();
		List<RibbonSegment> segments = new ArrayList<>(joints.size() - 1);
		for (int index = 1; index < joints.size(); index++) {
			segments.add(new RibbonSegment(joints.get(index - 1), joints.get(index)));
		}
		return List.copyOf(segments);
	}

	public static List<TubeFrame> tubeFrames(List<Sample> samples) {
		if (samples.isEmpty()) return List.of();
		List<TubeFrame> frames = new ArrayList<>(samples.size());
		for (int index = 0; index < samples.size(); index++) {
			Sample current = samples.get(index);
			Sample previous = samples.get(Math.max(0, index - 1));
			Sample next = samples.get(Math.min(samples.size() - 1, index + 1));
			double tangentX = next.x() - previous.x();
			double tangentY = next.y() - previous.y();
			double tangentZ = next.z() - previous.z();
			double tangentLength = Math.sqrt(
					tangentX * tangentX + tangentY * tangentY + tangentZ * tangentZ);
			if (tangentLength < 1.0E-8D) {
				tangentX = 1.0D;
				tangentY = 0.0D;
				tangentZ = 0.0D;
				tangentLength = 1.0D;
			}
			tangentX /= tangentLength;
			tangentY /= tangentLength;
			tangentZ /= tangentLength;

			double sideX = -tangentZ;
			double sideY = 0.0D;
			double sideZ = tangentX;
			double sideLength = Math.hypot(sideX, sideZ);
			if (sideLength < 1.0E-8D) {
				sideX = 0.0D;
				sideY = tangentZ;
				sideZ = -tangentY;
				sideLength = Math.sqrt(sideY * sideY + sideZ * sideZ);
			}
			sideX /= sideLength;
			sideY /= sideLength;
			sideZ /= sideLength;
			double verticalX = tangentY * sideZ - tangentZ * sideY;
			double verticalY = tangentZ * sideX - tangentX * sideZ;
			double verticalZ = tangentX * sideY - tangentY * sideX;
			double verticalLength = Math.sqrt(
					verticalX * verticalX + verticalY * verticalY + verticalZ * verticalZ);
			verticalX /= verticalLength;
			verticalY /= verticalLength;
			verticalZ /= verticalLength;
			frames.add(new TubeFrame(current.x(), current.y(), current.z(), current.halfWidth(),
					sideX * current.halfWidth(), sideY * current.halfWidth(),
					sideZ * current.halfWidth(),
					verticalX * current.halfWidth(), verticalY * current.halfWidth(),
					verticalZ * current.halfWidth()));
		}
		return List.copyOf(frames);
	}

	private static double phase(long seed) {
		long mixed = seed ^ (seed >>> 33);
		mixed *= 0xff51afd7ed558ccdL;
		mixed ^= mixed >>> 33;
		return (mixed & 0xFFFFL) / 65535.0D * Math.PI * 2.0D;
	}

	private static double lerp(double start, double end, double progress) {
		return start + (end - start) * progress;
	}

	public record Sample(double x, double y, double z, float halfWidth) {
	}

	public record RibbonJoint(double centerX, double centerY, double centerZ,
			double leftX, double leftZ, double rightX, double rightZ) {
	}

	public record RibbonSegment(RibbonJoint start, RibbonJoint end) {
	}

	public record TubeFrame(double centerX, double centerY, double centerZ, float radius,
			double sideX, double sideY, double sideZ,
			double verticalX, double verticalY, double verticalZ) {
	}
}
