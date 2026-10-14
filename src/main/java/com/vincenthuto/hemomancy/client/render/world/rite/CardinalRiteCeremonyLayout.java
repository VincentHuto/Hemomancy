package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.client.render.world.CardinalRiteBoundaryGeometry;
import com.vincenthuto.hemomancy.client.render.world.floor.AnchorSores;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMath;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything about a ceremony's boundary that depends only on its authored
 * anchors: ring radii, socket clearances, neighbouring sweeps and the static
 * floor-stain mesh. Built once per recipe instead of every frame.
 */
public final class CardinalRiteCeremonyLayout {
	public static final float SOCKET_RADIUS = 0.34F;
	public static final float SOCKET_CORE_WIDTH = 0.055F;
	/** Pieces per full circle for boundary rings and their stains. */
	public static final int RING_SEGMENTS = 96;
	private static final int STAIN_RADIAL_SEGMENTS = 6;
	/** Floats per stain vertex: x, z, alpha factor, outer-colour flag. */
	public static final int STAIN_VERTEX_STRIDE = 4;

	private final List<CardinalRiteCeremonyDefinition.Anchor> anchors;
	private final int ringCount;
	private final float[] ringRadii;
	private final List<List<Double>> socketEndpoints;
	private final float[][] stainMeshes;
	private final double[] anchorAngles;
	private final double[] anchorClearances;
	private final double[] forwardSweeps;
	private final double[] backwardSweeps;
	private final double[] segmentEndClearances;
	private final double fallbackClearance;

	private CardinalRiteCeremonyLayout(CardinalRiteCeremonyDefinition ceremony) {
		this.anchors = List.copyOf(ceremony.anchors());
		int maxRing = -1;
		for (CardinalRiteCeremonyDefinition.Anchor anchor : anchors) maxRing = Math.max(maxRing, anchor.ring());
		this.ringCount = maxRing + 1;
		float throat = CardinalRiteBoundaryGeometry.socketThroatRadius(SOCKET_RADIUS, SOCKET_CORE_WIDTH);

		ringRadii = new float[Math.max(0, ringCount)];
		for (int ring = 0; ring < ringCount; ring++) {
			List<BlockPos> offsets = new ArrayList<>();
			for (CardinalRiteCeremonyDefinition.Anchor anchor : anchors) {
				if (anchor.ring() == ring) offsets.add(anchor.offset());
			}
			ringRadii[ring] = CardinalRiteBoundaryGeometry.anchorAlignedRingRadius(
					CardinalRiteBoundaryGeometry.interactiveRingRadius(ring), offsets);
		}
		float firstRadius = ringCount > 0 ? ringRadii[0] : CardinalRiteBoundaryGeometry.interactiveRingRadius(0);
		fallbackClearance = CardinalRiteBoundaryGeometry.socketClearanceAngle(firstRadius, firstRadius, throat);

		int anchorCount = anchors.size();
		anchorAngles = new double[anchorCount];
		anchorClearances = new double[anchorCount];
		forwardSweeps = new double[anchorCount];
		backwardSweeps = new double[anchorCount];
		segmentEndClearances = new double[anchorCount];
		for (int index = 0; index < anchorCount; index++) {
			CardinalRiteCeremonyDefinition.Anchor anchor = anchors.get(index);
			anchorAngles[index] = Math.atan2(anchor.z(), anchor.x());
			float ringRadius = ringRadius(anchor.ring());
			float socketCenterRadius = (float) Math.hypot(anchor.x(), anchor.z());
			anchorClearances[index] = CardinalRiteBoundaryGeometry.socketClearanceAngle(
					ringRadius, socketCenterRadius, throat);
			segmentEndClearances[index] = Double.NaN;
		}

		List<CardinalRiteBoundaryProgress.Segment> authored = CardinalRiteBoundaryProgress.authoredSegments(anchors);
		for (CardinalRiteBoundaryProgress.Segment segment : authored) {
			int start = segment.startAnchorIndex();
			if (start < 0 || start >= anchorCount) continue;
			forwardSweeps[start] = segment.sweepAngle();
			int end = nearestAnchorOnRing(segment.ring(), segment.startAngle() + segment.sweepAngle());
			if (end >= 0) backwardSweeps[end] = segment.sweepAngle();
		}

		socketEndpoints = new ArrayList<>(ringCount);
		stainMeshes = new float[Math.max(0, ringCount)][];
		for (int ring = 0; ring < ringCount; ring++) {
			List<Double> endpoints = new ArrayList<>();
			for (int index = 0; index < anchorCount; index++) {
				if (anchors.get(index).ring() != ring) continue;
				endpoints.add(anchorAngles[index] - anchorClearances[index]);
				endpoints.add(anchorAngles[index] + anchorClearances[index]);
			}
			socketEndpoints.add(List.copyOf(endpoints));
			List<CardinalRiteBoundaryProgress.Segment> backing = new ArrayList<>();
			for (CardinalRiteBoundaryProgress.Segment segment : authored) {
				if (segment.ring() == ring) backing.add(segment);
			}
			stainMeshes[ring] = buildStainMesh(ring, ringRadii[ring], backing);
		}
	}

	public static CardinalRiteCeremonyLayout of(CardinalRiteCeremonyDefinition ceremony) {
		return ceremony == null ? null : new CardinalRiteCeremonyLayout(ceremony);
	}

	public List<CardinalRiteCeremonyDefinition.Anchor> anchors() {
		return anchors;
	}

	public int ringCount() {
		return ringCount;
	}

	/** The ring's radius through its authored socket centres, or the nominal radius for a ring with none. */
	public float ringRadius(int ring) {
		if (ring >= 0 && ring < ringRadii.length) return ringRadii[ring];
		return CardinalRiteBoundaryGeometry.interactiveRingRadius(ring);
	}

	/** Angles where the ring meets its sockets, where the boundary's undulation is pinned. */
	public List<Double> socketEndpoints(int ring) {
		return ring >= 0 && ring < socketEndpoints.size() ? socketEndpoints.get(ring) : List.of();
	}

	public double anchorAngle(int index) {
		return index >= 0 && index < anchorAngles.length ? anchorAngles[index] : 0.0D;
	}

	/** Sweep from this anchor to the next one along its ring. */
	public double forwardSweep(int index) {
		return index >= 0 && index < forwardSweeps.length ? forwardSweeps[index] : 0.0D;
	}

	/** Sweep from this anchor back to the previous one along its ring. */
	public double backwardSweep(int index) {
		return index >= 0 && index < backwardSweeps.length ? backwardSweeps[index] : 0.0D;
	}

	public double startClearance(CardinalRiteBoundaryProgress.Segment segment) {
		int start = segment.startAnchorIndex();
		return start >= 0 && start < anchorClearances.length ? anchorClearances[start] : fallbackClearance;
	}

	public double endClearance(CardinalRiteBoundaryProgress.Segment segment) {
		int start = segment.startAnchorIndex();
		if (start < 0 || start >= anchorClearances.length) return fallbackClearance;
		if (Double.isNaN(segmentEndClearances[start])) {
			int end = nearestAnchorOnRing(segment.ring(), segment.startAngle() + segment.sweepAngle());
			segmentEndClearances[start] = end < 0 ? fallbackClearance : anchorClearances[end];
		}
		return segmentEndClearances[start];
	}

	/**
	 * Static floor-stain mesh for a ring, as quads of {@link #STAIN_VERTEX_STRIDE}
	 * floats per vertex, or an empty array when the ring has no authored arcs.
	 */
	public float[] stainMesh(int ring) {
		return ring >= 0 && ring < stainMeshes.length ? stainMeshes[ring] : new float[0];
	}

	private int nearestAnchorOnRing(int ring, double angle) {
		double target = CardinalRiteMath.normalizeAngle(angle);
		int nearest = -1;
		double nearestDistance = Double.POSITIVE_INFINITY;
		for (int index = 0; index < anchors.size(); index++) {
			if (anchors.get(index).ring() != ring) continue;
			double distance = CardinalRiteMath.angularDistance(anchorAngles[index], target);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearest = index;
			}
		}
		return nearest;
	}

	private float[] buildStainMesh(int ring, float ringRadius, List<CardinalRiteBoundaryProgress.Segment> backing) {
		if (backing.isEmpty()) return new float[0];
		float halfWidth = CardinalRiteBoundaryGeometry.boundaryStainWidth() * 0.5F;
		float inner = ringRadius - halfWidth;
		float outer = ringRadius + halfWidth;
		// The stain clears the whole sore and feathers back in only past its inflamed halo, so it never leaves dark
		// crescents biting into the sore's sides.
		float featherStart = SOCKET_RADIUS;
		float socketOuter = AnchorSores.HALF * 1.15F;
		List<CardinalRiteCeremonyDefinition.Anchor> ringAnchors = new ArrayList<>();
		for (CardinalRiteCeremonyDefinition.Anchor anchor : anchors) {
			if (anchor.ring() == ring) ringAnchors.add(anchor);
		}
		int quads = 0;
		for (CardinalRiteBoundaryProgress.Segment arc : backing) {
			quads += CardinalRiteBoundaryGeometry.pieceCount(arc.sweepAngle(), RING_SEGMENTS) * STAIN_RADIAL_SEGMENTS;
		}
		float[] mesh = new float[quads * 4 * STAIN_VERTEX_STRIDE];
		int cursor = 0;
		for (CardinalRiteBoundaryProgress.Segment arc : backing) {
			int pieces = CardinalRiteBoundaryGeometry.pieceCount(arc.sweepAngle(), RING_SEGMENTS);
			double pieceSweep = arc.sweepAngle() / Math.max(1, pieces);
			for (int piece = 0; piece < pieces; piece++) {
				double a1 = arc.startAngle() + pieceSweep * piece;
				double a2 = a1 + pieceSweep;
				float cos1 = (float) Math.cos(a1);
				float sin1 = (float) Math.sin(a1);
				float cos2 = (float) Math.cos(a2);
				float sin2 = (float) Math.sin(a2);
				for (int radial = 0; radial < STAIN_RADIAL_SEGMENTS; radial++) {
					float t0 = radial / (float) STAIN_RADIAL_SEGMENTS;
					float t1 = (radial + 1) / (float) STAIN_RADIAL_SEGMENTS;
					float radius0 = inner + (outer - inner) * t0;
					float radius1 = inner + (outer - inner) * t1;
					cursor = stainVertex(mesh, cursor, cos1 * radius0, sin1 * radius0, t0, 0.0F,
							ringAnchors, featherStart, socketOuter);
					cursor = stainVertex(mesh, cursor, cos1 * radius1, sin1 * radius1, t1, 1.0F,
							ringAnchors, featherStart, socketOuter);
					cursor = stainVertex(mesh, cursor, cos2 * radius1, sin2 * radius1, t1, 1.0F,
							ringAnchors, featherStart, socketOuter);
					cursor = stainVertex(mesh, cursor, cos2 * radius0, sin2 * radius0, t0, 0.0F,
							ringAnchors, featherStart, socketOuter);
				}
			}
		}
		return mesh;
	}

	private static int stainVertex(float[] mesh, int cursor, float x, float z, float radialPosition,
			float outerColour, List<CardinalRiteCeremonyDefinition.Anchor> ringAnchors,
			float featherStart, float socketOuter) {
		float mask = 1.0F;
		for (CardinalRiteCeremonyDefinition.Anchor anchor : ringAnchors) {
			mask = Math.min(mask, CardinalRiteBoundaryGeometry.boundaryStainSocketMask(
					x, z, anchor.x(), anchor.z(), featherStart, socketOuter - featherStart));
		}
		float profile = Math.max(0.0F, 1.0F - Math.abs(radialPosition * 2.0F - 1.0F));
		mesh[cursor] = x;
		mesh[cursor + 1] = z;
		mesh[cursor + 2] = profile * mask;
		mesh[cursor + 3] = outerColour;
		return cursor + STAIN_VERTEX_STRIDE;
	}
}
