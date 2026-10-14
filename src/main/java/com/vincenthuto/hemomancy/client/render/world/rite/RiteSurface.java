package com.vincenthuto.hemomancy.client.render.world.rite;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Allocation-free surfaces for the living-blood ({@code AnimusMortemRenderTypes.ANIMUS}) and decay
 * ({@code AnimusMortemRenderTypes.MORTEM}) materials, using the same UV encoding as the manipulation geometry: the
 * shader shape and a stable seed ride in otherwise unused UV tiles. Positions are doubles in the caller's local frame
 * and are written through the pose matrix, so a renderer translates once to its origin and draws relative to it.
 */
public final class RiteSurface {
	public static final int RIBBON = 0, WISP = 1, POOL = 2, FOCUS = 4, RIPPLE = 6, SLASH = 7, SHELL = 9;
	/** Base tint; the material supplies the hue. */
	public static final int TINT = 0xFFFFFF;
	/** Hot pale highlight accent. */
	public static final int CORE = 0xFFD2C4;
	/** Dark clot accent for depth. */
	public static final int CLOT = 0x7A1020;

	private RiteSurface() {
	}

	/** Camera position and billboard axes, expressed in the frame geometry is drawn in. */
	public static final class View {
		double camX, camY, camZ;
		double rightX, rightY, rightZ, upX, upY, upZ;
		private final Vector3f scratch = new Vector3f();

		/** Sets the camera relative to the drawing origin and derives billboard axes from its rotation. */
		public View set(double cameraX, double cameraY, double cameraZ, double originX, double originY,
				double originZ, Quaternionf rotation) {
			camX = cameraX - originX;
			camY = cameraY - originY;
			camZ = cameraZ - originZ;
			scratch.set(1, 0, 0).rotate(rotation);
			rightX = scratch.x();
			rightY = scratch.y();
			rightZ = scratch.z();
			scratch.set(0, 1, 0).rotate(rotation);
			upX = scratch.x();
			upY = scratch.y();
			upZ = scratch.z();
			return this;
		}

		/** Sets explicit axes; used by tests and by flat ground cards. */
		public View set(double cameraX, double cameraY, double cameraZ, double rx, double ry, double rz,
				double ux, double uy, double uz) {
			camX = cameraX;
			camY = cameraY;
			camZ = cameraZ;
			rightX = rx;
			rightY = ry;
			rightZ = rz;
			upX = ux;
			upY = uy;
			upZ = uz;
			return this;
		}

		public double distanceSqr(double x, double y, double z) {
			double dx = x - camX, dy = y - camY, dz = z - camZ;
			return dx * dx + dy * dy + dz * dz;
		}
	}

	/** A reusable polyline with a half-width per station. */
	public static final class Path {
		private double[] x = new double[24], y = new double[24], z = new double[24], w = new double[24];
		private int size;

		public Path clear() {
			size = 0;
			return this;
		}

		public Path add(double px, double py, double pz, double halfWidth) {
			if (size == x.length) {
				int capacity = size * 2;
				x = java.util.Arrays.copyOf(x, capacity);
				y = java.util.Arrays.copyOf(y, capacity);
				z = java.util.Arrays.copyOf(z, capacity);
				w = java.util.Arrays.copyOf(w, capacity);
			}
			x[size] = px;
			y[size] = py;
			z[size] = pz;
			w[size] = halfWidth;
			size++;
			return this;
		}

		public int size() {
			return size;
		}

		public double x(int i) {
			return x[i];
		}

		public double y(int i) {
			return y[i];
		}

		public double z(int i) {
			return z[i];
		}
	}

	/** The whole path as one camera-facing ribbon. */
	public static void ribbon(Matrix4f m, VertexConsumer v, View view, Path path, int shape, int seed, int color,
			float alpha) {
		ribbon(m, v, view, path, 0, path.size, 1.0D, shape, seed, color, alpha);
	}

	/**
	 * Stations {@code [from, to)} of a path as a camera-facing ribbon, each half-width multiplied by
	 * {@code widthScale}. Tapering strands draw consecutive sub-ranges with different scales and materials.
	 */
	public static void ribbon(Matrix4f m, VertexConsumer v, View view, Path p, int from, int to, double widthScale,
			int shape, int seed, int color, float alpha) {
		from = Math.max(0, from);
		to = Math.min(p.size, to);
		int count = to - from;
		if (count < 2 || alpha <= 0.003F || widthScale <= 0) return;
		double plx = 0, ply = 0, plz = 0, prx = 0, pry = 0, prz = 0, psx = 0, psy = 0, psz = 0;
		boolean previous = false;
		for (int k = 0; k < count; k++) {
			int i = from + k;
			int a = Math.max(from, i - 1), b = Math.min(to - 1, i + 1);
			double tx = p.x[b] - p.x[a], ty = p.y[b] - p.y[a], tz = p.z[b] - p.z[a];
			double vx = view.camX - p.x[i], vy = view.camY - p.y[i], vz = view.camZ - p.z[i];
			double sx = ty * vz - tz * vy, sy = tz * vx - tx * vz, sz = tx * vy - ty * vx;
			double length = Math.sqrt(sx * sx + sy * sy + sz * sz);
			if (length < 1e-9) {
				if (previous) {
					sx = psx;
					sy = psy;
					sz = psz;
				} else {
					sx = 1;
					sy = 0;
					sz = 0;
				}
			} else {
				sx /= length;
				sy /= length;
				sz /= length;
			}
			if (previous && sx * psx + sy * psy + sz * psz < 0) {
				sx = -sx;
				sy = -sy;
				sz = -sz;
			}
			double width = p.w[i] * widthScale;
			// Keep inner edges from folding past neighbouring stations on tight curls.
			if (k > 0) width = Math.min(width, joinWidth(p, i - 1, i, sx, sy, sz));
			if (k + 1 < count) width = Math.min(width, joinWidth(p, i, i + 1, sx, sy, sz));
			double lx = p.x[i] - sx * width, ly = p.y[i] - sy * width, lz = p.z[i] - sz * width;
			double rx = p.x[i] + sx * width, ry = p.y[i] + sy * width, rz = p.z[i] + sz * width;
			if (previous) {
				quad(m, v, plx, ply, plz, lx, ly, lz, rx, ry, rz, prx, pry, prz, shape, seed, color, alpha,
						(k - 1.0D) / (count - 1.0D), k / (count - 1.0D));
			}
			plx = lx;
			ply = ly;
			plz = lz;
			prx = rx;
			pry = ry;
			prz = rz;
			psx = sx;
			psy = sy;
			psz = sz;
			previous = true;
		}
	}

	private static double joinWidth(Path p, int a, int b, double sx, double sy, double sz) {
		double dx = p.x[b] - p.x[a], dy = p.y[b] - p.y[a], dz = p.z[b] - p.z[a];
		double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length < 1e-9) return Double.MAX_VALUE;
		double along = Math.abs((dx * sx + dy * sy + dz * sz) / length);
		return length * 0.45D / Math.max(0.001D, along);
	}

	/** A camera-facing card. */
	public static void card(Matrix4f m, VertexConsumer v, View view, double x, double y, double z, double halfWidth,
			double halfHeight, int shape, int seed, int color, float alpha) {
		if (alpha <= 0.003F || halfWidth <= 0 || halfHeight <= 0) return;
		double ax = view.rightX * halfWidth, ay = view.rightY * halfWidth, az = view.rightZ * halfWidth;
		double bx = view.upX * halfHeight, by = view.upY * halfHeight, bz = view.upZ * halfHeight;
		quad(m, v, x - ax - bx, y - ay - by, z - az - bz, x + ax - bx, y + ay - by, z + az - bz,
				x + ax + bx, y + ay + by, z + az + bz, x - ax + bx, y - ay + by, z - az + bz,
				shape, seed, color, alpha, 0, 1);
	}

	/** A flat horizontal card, for pools and ripples lying on the floor. */
	public static void ground(Matrix4f m, VertexConsumer v, double x, double y, double z, double radius, int shape,
			int seed, int color, float alpha) {
		if (alpha <= 0.003F || radius <= 0) return;
		quad(m, v, x - radius, y, z - radius, x + radius, y, z - radius, x + radius, y, z + radius,
				x - radius, y, z + radius, shape, seed, color, alpha, 0, 1);
	}

	/**
	 * A hemispherical membrane whose opacity rises toward its silhouette, with continuous UVs so the shell shader's
	 * flow noise runs unbroken across the dome.
	 */
	public static void dome(Matrix4f m, VertexConsumer v, View view, double cx, double cy, double cz, double radius,
			int latitudes, int longitudes, int seed, int color, float faceAlpha, float rimAlpha) {
		if (radius <= 0 || (faceAlpha <= 0.003F && rimAlpha <= 0.003F)) return;
		for (int lat = 0; lat < latitudes; lat++) {
			for (int lon = 0; lon < longitudes; lon++) {
				domeVertex(m, v, view, cx, cy, cz, radius, lon / (double) longitudes, lat / (double) latitudes, seed,
						color, faceAlpha, rimAlpha);
				domeVertex(m, v, view, cx, cy, cz, radius, (lon + 1) / (double) longitudes, lat / (double) latitudes,
						seed, color, faceAlpha, rimAlpha);
				domeVertex(m, v, view, cx, cy, cz, radius, (lon + 1) / (double) longitudes,
						(lat + 1) / (double) latitudes, seed, color, faceAlpha, rimAlpha);
				domeVertex(m, v, view, cx, cy, cz, radius, lon / (double) longitudes, (lat + 1) / (double) latitudes,
						seed, color, faceAlpha, rimAlpha);
			}
		}
	}

	private static void domeVertex(Matrix4f m, VertexConsumer v, View view, double cx, double cy, double cz,
			double radius, double u, double t, int seed, int color, float faceAlpha, float rimAlpha) {
		double azimuth = u * Math.PI * 2.0D;
		double elevation = t * Math.PI * 0.5D;
		double ring = Math.cos(elevation);
		double nx = Math.cos(azimuth) * ring, ny = Math.sin(elevation), nz = Math.sin(azimuth) * ring;
		double x = cx + nx * radius, y = cy + ny * radius, z = cz + nz * radius;
		double vx = view.camX - x, vy = view.camY - y, vz = view.camZ - z;
		double length = Math.sqrt(vx * vx + vy * vy + vz * vz);
		double facing = length < 1e-6 ? 1.0D : Math.abs((nx * vx + ny * vy + nz * vz) / length);
		float alpha = (float) (faceAlpha + rimAlpha * (1.0D - facing) * (1.0D - facing));
		vertex(m, v, x, y, z, color, alpha, SHELL * 2 + u, Math.floorMod(seed, 127) * 2 + t);
	}

	/** A quad in the encoded-UV format: {@code u} runs along a ribbon, the seed tile across it. */
	public static void quad(Matrix4f m, VertexConsumer v, double ax, double ay, double az, double bx, double by,
			double bz, double cx, double cy, double cz, double dx, double dy, double dz, int shape, int seed,
			int color, float alpha, double u0, double u1) {
		if (alpha <= 0.003F) return;
		int phase = Math.floorMod(seed, 127);
		vertex(m, v, ax, ay, az, color, alpha, shape * 2 + u0, phase * 2);
		vertex(m, v, bx, by, bz, color, alpha, shape * 2 + u1, phase * 2);
		vertex(m, v, cx, cy, cz, color, alpha, shape * 2 + u1, phase * 2 + 1);
		vertex(m, v, dx, dy, dz, color, alpha, shape * 2 + u0, phase * 2 + 1);
	}

	private static void vertex(Matrix4f m, VertexConsumer v, double x, double y, double z, int color, float alpha,
			double u, double texV) {
		v.addVertex(m, (float) x, (float) y, (float) z)
				.setColor((color >> 16) & 255, (color >> 8) & 255, color & 255,
						(int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F))
				.setUv((float) u, (float) texV);
	}

	/**
	 * A soft flat-colour disc for {@code POSITION_COLOR} glow types: opaque at the centre, clear at the rim. Used where
	 * a colour must survive, since the blood materials multiply every tint into crimson.
	 */
	public static void colourDisc(Matrix4f m, VertexConsumer v, View view, double x, double y, double z,
			double radius, int segments, int rgb, float alpha) {
		if (alpha <= 0.003F || radius <= 0) return;
		int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
		int a = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);
		for (int i = 0; i < segments; i++) {
			double a0 = i * Math.PI * 2.0D / segments, a1 = (i + 1) * Math.PI * 2.0D / segments;
			double c0 = Math.cos(a0) * radius, s0 = Math.sin(a0) * radius;
			double c1 = Math.cos(a1) * radius, s1 = Math.sin(a1) * radius;
			v.addVertex(m, (float) x, (float) y, (float) z).setColor(r, g, b, a);
			v.addVertex(m, (float) (x + view.rightX * c0 + view.upX * s0), (float) (y + view.rightY * c0 + view.upY * s0),
					(float) (z + view.rightZ * c0 + view.upZ * s0)).setColor(r, g, b, 0);
			v.addVertex(m, (float) (x + view.rightX * c1 + view.upX * s1), (float) (y + view.rightY * c1 + view.upY * s1),
					(float) (z + view.rightZ * c1 + view.upZ * s1)).setColor(r, g, b, 0);
			v.addVertex(m, (float) x, (float) y, (float) z).setColor(r, g, b, a);
		}
	}
}
