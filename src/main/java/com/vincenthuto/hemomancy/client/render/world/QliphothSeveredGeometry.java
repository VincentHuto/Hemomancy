package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.world.QliphothBloomGeometry.Limb;
import com.vincenthuto.hemomancy.client.render.world.QliphothBloomGeometry.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * The pruned Qliphoth: the grown tree's own trunk, sheared off along one diagonal plane.
 * Every bark column stops exactly on the plane, so the bark wall, its rind and the
 * monolith cross-section all share one rim.
 */
final class QliphothSeveredGeometry {
	static final int SIDES = 10;
	/** Width of the bark rind left around the monolith face, in blocks. */
	static final double RIND = 0.07;
	static final double BARK_REPEATS = 2.4;

	private static final double CUT_CENTER_HEIGHT = 2.05;
	private static final double CUT_X_SLOPE = -1.15;
	private static final double CUT_Z_SLOPE = 0.18;
	private static Stump cached;

	private QliphothSeveredGeometry() {
	}

	static double cutHeight(double x, double z) {
		return CUT_CENTER_HEIGHT + x * CUT_X_SLOPE + z * CUT_Z_SLOPE;
	}

	static double aboveCut(Point point) {
		return point.y() - cutHeight(point.x(), point.z());
	}

	static Point cutNormal() {
		return new Point(-CUT_X_SLOPE, 1, -CUT_Z_SLOPE).normalized();
	}

	static Limb trunk() {
		return QliphothBloomGeometry.snapshot(9).trunk();
	}

	static Stump stump() {
		if (cached == null) cached = build(trunk(), SIDES);
		return cached;
	}

	/**
	 * Builds the bark wall ring by ring. Once a column crosses the plane, the crossing point
	 * stands in for every ring above it, so the upper quads fold flat onto the rim.
	 */
	static Stump build(Limb trunk, int sides) {
		List<Point> points = trunk.points();
		Point[][] frames = QliphothBloomGeometry.tubeFrames(points);
		List<Double> textureV = QliphothBloomGeometry.textureV(trunk, BARK_REPEATS);
		int rows = points.size();
		Point[][] wall = new Point[rows][sides];
		Point[][] normals = new Point[rows][sides];
		double[][] wallV = new double[rows][sides];
		Point[] rim = new Point[sides];
		double[] rimV = new double[sides];
		for (int side = 0; side < sides; side++) {
			double angle = side * Math.PI * 2 / sides;
			for (int row = 0; row < rows; row++) {
				Point normal = frames[0][row].scale(Math.cos(angle)).add(frames[1][row].scale(Math.sin(angle)));
				normals[row][side] = normal;
				if (rim[side] == null) {
					Point point = QliphothBloomGeometry.ringPoint(points.get(row), frames[0][row], frames[1][row],
							trunk.radii().get(row), angle);
					if (aboveCut(point) <= 0) {
						wall[row][side] = point;
						wallV[row][side] = textureV.get(row);
						continue;
					}
					if (row == 0) throw new IllegalStateException("Qliphoth cut passes below the trunk base");
					Point below = wall[row - 1][side];
					double t = aboveCut(below) / (aboveCut(below) - aboveCut(point));
					rim[side] = below.lerp(point, t);
					rimV[side] = textureV.get(row - 1) + (textureV.get(row) - textureV.get(row - 1)) * t;
				}
				wall[row][side] = rim[side];
				wallV[row][side] = rimV[side];
			}
			if (rim[side] == null) throw new IllegalStateException("Qliphoth cut passes above the trunk top");
		}
		Point center = new Point(0, 0, 0);
		for (Point point : rim) center = center.add(point);
		center = center.scale(1.0 / sides);
		Point[] inner = new Point[sides];
		double reach = 0;
		for (int side = 0; side < sides; side++) {
			Point offset = rim[side].subtract(center);
			reach = Math.max(reach, offset.length());
			inner[side] = center.add(offset.scale(Math.max(0, 1 - RIND / offset.length())));
		}
		return new Stump(wall, normals, wallV, rim, inner, center, reach);
	}

	/** Clips a surface vein so it ends just under the cut instead of piercing the face. */
	static List<Point> clipBelowCut(List<Point> path, double clearance) {
		List<Point> result = new ArrayList<>();
		for (int i = 0; i < path.size(); i++) {
			Point point = path.get(i);
			double above = aboveCut(point) + clearance;
			if (above <= 0) {
				result.add(point);
				continue;
			}
			if (i > 0) {
				Point below = path.get(i - 1);
				double belowAbove = aboveCut(below) + clearance;
				result.add(below.lerp(point, belowAbove / (belowAbove - above)));
			}
			break;
		}
		return result;
	}

	/**
	 * @param wall    [row][side] bark positions, folded onto the rim above the cut
	 * @param rim     where each bark column meets the cut plane
	 * @param inner   the rim pulled in by {@link #RIND}; the monolith face fills this polygon
	 * @param reach   largest rim distance from {@code center}, for planar UVs
	 */
	record Stump(Point[][] wall, Point[][] normals, double[][] wallV, Point[] rim, Point[] inner,
			Point center, double reach) {
		float faceU(Point point) {
			return (float) ((point.x() - center.x()) / (reach * 2) + .5);
		}

		float faceV(Point point) {
			return (float) ((point.z() - center.z()) / (reach * 2) + .5);
		}
	}
}
