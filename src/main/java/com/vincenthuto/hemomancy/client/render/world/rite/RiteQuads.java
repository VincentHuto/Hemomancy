package com.vincenthuto.hemomancy.client.render.world.rite;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

/**
 * Flat POSITION_COLOR quad and band primitives shared by the rite boundary,
 * Unstained boundary and blood-craft ring renderers.
 */
public final class RiteQuads {
	/** Segments used for small closed circles (discs, flash rings, beads). */
	public static final int CIRCLE_SEGMENTS = 12;
	private static final float[] CIRCLE_COS = new float[CIRCLE_SEGMENTS + 1];
	private static final float[] CIRCLE_SIN = new float[CIRCLE_SEGMENTS + 1];

	static {
		for (int index = 0; index <= CIRCLE_SEGMENTS; index++) {
			double angle = Math.PI * 2.0D * index / CIRCLE_SEGMENTS;
			CIRCLE_COS[index] = (float) Math.cos(angle);
			CIRCLE_SIN[index] = (float) Math.sin(angle);
		}
	}

	private RiteQuads() {
	}

	public static void emitQuad(VertexConsumer vc, Matrix4f mat,
			float x1, float y1, float z1, float r1, float g1, float b1, float a1,
			float x2, float y2, float z2, float r2, float g2, float b2, float a2,
			float x3, float y3, float z3, float r3, float g3, float b3, float a3,
			float x4, float y4, float z4, float r4, float g4, float b4, float a4) {
		vc.addVertex(mat, x1, y1, z1).setColor(r1, g1, b1, a1);
		vc.addVertex(mat, x2, y2, z2).setColor(r2, g2, b2, a2);
		vc.addVertex(mat, x3, y3, z3).setColor(r3, g3, b3, a3);
		vc.addVertex(mat, x4, y4, z4).setColor(r4, g4, b4, a4);
	}

	/**
	 * One angular piece of a radial band around {@code (originX, originZ)}: the
	 * quad between {@code inner} and {@code outer} radii at two angles, each end
	 * with its own radii and height. Inner and outer edges carry their own
	 * colour and alpha so the same call draws cores, halos and gradients.
	 */
	public static void bandPiece(VertexConsumer vc, Matrix4f mat, float originX, float originZ,
			float cos1, float sin1, float y1, float inner1, float outer1,
			float cos2, float sin2, float y2, float inner2, float outer2,
			float innerR, float innerG, float innerB, float innerA,
			float outerR, float outerG, float outerB, float outerA) {
		emitQuad(vc, mat,
				originX + cos1 * inner1, y1, originZ + sin1 * inner1, innerR, innerG, innerB, innerA,
				originX + cos1 * outer1, y1, originZ + sin1 * outer1, outerR, outerG, outerB, outerA,
				originX + cos2 * outer2, y2, originZ + sin2 * outer2, outerR, outerG, outerB, outerA,
				originX + cos2 * inner2, y2, originZ + sin2 * inner2, innerR, innerG, innerB, innerA);
	}

	/**
	 * {@link #bandPiece} with each angular end's alpha scaled by its own fade, so a band can dissolve along its length.
	 */
	public static void fadedBandPiece(VertexConsumer vc, Matrix4f mat, float fade1, float fade2,
			float cos1, float sin1, float y1, float inner1, float outer1,
			float cos2, float sin2, float y2, float inner2, float outer2,
			float innerR, float innerG, float innerB, float innerA,
			float outerR, float outerG, float outerB, float outerA) {
		emitQuad(vc, mat,
				cos1 * inner1, y1, sin1 * inner1, innerR, innerG, innerB, innerA * fade1,
				cos1 * outer1, y1, sin1 * outer1, outerR, outerG, outerB, outerA * fade1,
				cos2 * outer2, y2, sin2 * outer2, outerR, outerG, outerB, outerA * fade2,
				cos2 * inner2, y2, sin2 * inner2, innerR, innerG, innerB, innerA * fade2);
	}

	/**
	 * A flat closed annulus with an alpha gradient from its inner edge to its
	 * outer edge. With {@code inner == 0} it is a soft disc; with a narrow width
	 * it is a flash ring.
	 */
	public static void annulus(VertexConsumer vc, Matrix4f mat, float centerX, float centerZ, float y,
			float inner, float outer, float red, float green, float blue, float innerAlpha, float outerAlpha) {
		if (outer <= 0.0F || (innerAlpha <= 0.0F && outerAlpha <= 0.0F)) return;
		float safeInner = Math.max(0.0F, inner);
		for (int index = 0; index < CIRCLE_SEGMENTS; index++) {
			bandPiece(vc, mat, centerX, centerZ,
					CIRCLE_COS[index], CIRCLE_SIN[index], y, safeInner, outer,
					CIRCLE_COS[index + 1], CIRCLE_SIN[index + 1], y, safeInner, outer,
					red, green, blue, innerAlpha, red, green, blue, outerAlpha);
		}
	}

	/** A soft disc fading from {@code alpha} at its centre to nothing at {@code radius}. */
	public static void disc(VertexConsumer vc, Matrix4f mat, float centerX, float centerZ, float y,
			float radius, float red, float green, float blue, float alpha) {
		annulus(vc, mat, centerX, centerZ, y, 0.0F, radius, red, green, blue, alpha, 0.0F);
	}

	/** A ring of the given width whose inner edge is opaque and outer edge feathers out. */
	public static void flashRing(VertexConsumer vc, Matrix4f mat, float centerX, float centerZ, float y,
			float radius, float width, float red, float green, float blue, float alpha) {
		annulus(vc, mat, centerX, centerZ, y, radius - width * 0.5F, radius + width * 0.5F,
				red, green, blue, alpha, 0.0F);
	}
}
