package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.VisceralGeometry;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * One continuous tube through a run of centres: each ring's frame is carried along from the last (parallel transport),
 * so neighbouring rings share their faces and the surface never twists, cracks or restarts between them. A closed
 * loop spreads any leftover twist around its length so its last ring meets its first.
 */
final class SeamTube {
	private SeamTube() {
	}

	/** Ring vertices, `sides` per centre, each ring at its own radius. */
	static Vec3[][] rings(List<Vec3> centres, double[] radii, int sides, boolean closed) {
		int n = centres.size();
		Vec3[] tangents = new Vec3[n], normals = new Vec3[n];
		for (int i = 0; i < n; i++) {
			Vec3 before = closed ? centres.get(Math.floorMod(i - 1, n)) : centres.get(Math.max(i - 1, 0));
			Vec3 after = closed ? centres.get((i + 1) % n) : centres.get(Math.min(i + 1, n - 1));
			Vec3 t = after.subtract(before);
			tangents[i] = t.lengthSqr() < 1.0E-12D ? new Vec3(1, 0, 0) : t.normalize();
		}
		normals[0] = VisceralGeometry.side(tangents[0]);
		for (int i = 1; i < n; i++) normals[i] = transport(normals[i - 1], tangents[i]);
		if (closed && n > 2) {
			// The frame carried all the way round may not land on the first one: unwind that twist evenly.
			Vec3 carried = transport(normals[n - 1], tangents[0]);
			Vec3 binormal = tangents[0].cross(normals[0]);
			double twist = Math.atan2(carried.dot(binormal), carried.dot(normals[0]));
			for (int i = 1; i < n; i++) normals[i] = rotate(normals[i], tangents[i], -twist * i / n);
		}
		Vec3[][] rings = new Vec3[n][sides];
		for (int i = 0; i < n; i++) {
			Vec3 side = normals[i], up = tangents[i].cross(side).normalize();
			for (int f = 0; f < sides; f++) {
				double angle = f * Math.PI * 2 / sides;
				rings[i][f] = centres.get(i).add(side.scale(Math.cos(angle) * radii[i])).add(up.scale(Math.sin(angle) * radii[i]));
			}
		}
		return rings;
	}

	/** Draws the tube, colour per ring blended along it, texture running continuously along its length. */
	static void draw(PoseStack pose, VertexConsumer v, List<Vec3> centres, double[] radii, int[] colours, float alpha,
			boolean closed) {
		int n = centres.size();
		if (n < 2 || alpha <= 0) return;
		int sides = 8;
		Vec3[][] rings = rings(centres, radii, sides, closed);
		double[] along = new double[n + 1];
		for (int i = 1; i <= n; i++) along[i] = along[i - 1] + centres.get(i % n).distanceTo(centres.get(i - 1));
		int segments = closed ? n : n - 1;
		for (int i = 0; i < segments; i++) {
			int j = (i + 1) % n;
			double c0 = Math.PI * 2 * radii[i], c1 = Math.PI * 2 * radii[j];
			for (int f = 0; f < sides; f++) {
				int g = (f + 1) % sides;
				// Light falls from above the face, as VisceralMesh shades its tubes.
				double light = 0.72 + 0.28 * Math.cos(f * Math.PI * 2 / sides - 0.6);
				vertex(pose, v, rings[i][f], colours[i], light, alpha, f / (double) sides * c0, along[i]);
				vertex(pose, v, rings[i][g], colours[i], light, alpha, (f + 1) / (double) sides * c0, along[i]);
				vertex(pose, v, rings[j][g], colours[j], light, alpha, (f + 1) / (double) sides * c1, along[i + 1]);
				vertex(pose, v, rings[j][f], colours[j], light, alpha, f / (double) sides * c1, along[i + 1]);
			}
		}
	}

	private static Vec3 transport(Vec3 normal, Vec3 tangent) {
		Vec3 n = normal.subtract(tangent.scale(normal.dot(tangent)));
		return n.lengthSqr() < 1.0E-12D ? VisceralGeometry.side(tangent) : n.normalize();
	}

	/** Rotates `v` about the unit `axis` by `angle` (Rodrigues). */
	private static Vec3 rotate(Vec3 v, Vec3 axis, double angle) {
		double cos = Math.cos(angle), sin = Math.sin(angle);
		return v.scale(cos).add(axis.cross(v).scale(sin)).add(axis.scale(axis.dot(v) * (1 - cos))).normalize();
	}

	private static void vertex(PoseStack pose, VertexConsumer v, Vec3 p, int colour, double light, float alpha, double u, double tex) {
		v.addVertex(pose.last().pose(), (float) p.x, (float) p.y, (float) p.z)
				.setColor((int) Math.min(255, ((colour >> 16) & 255) * light), (int) Math.min(255, ((colour >> 8) & 255) * light),
						(int) Math.min(255, (colour & 255) * light), (int) (Mth.clamp(alpha, 0, 1) * 255))
				.setUv((float) u, (float) tex);
	}
}
