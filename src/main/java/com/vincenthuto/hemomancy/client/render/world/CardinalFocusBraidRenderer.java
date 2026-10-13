package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/**
 * Four vessels braided into a torus around the Focus mouth, where the floor's roots gather: a thick red artery
 * that swells on the heartbeat, a thinner dark blue vein, an erratic fibrous nerve, and a beaded dark green lymph
 * vessel. Each strand dives into the floor at two points and runs on across the membrane as a tributary of its
 * own colour, so the braid reads as the knot where the floor's vessels gather. It is ordinary lit geometry: it shades with the world's light, keeping the membrane's faint night
 * glow.
 */
final class CardinalFocusBraidRenderer {
	private static final ResourceLocation MEMBRANE = Hemomancy.rloc("textures/block/dura_membrane.png");
	private static final ResourceLocation MYELIN = Hemomancy.rloc("textures/block/myelin_sheath.png");
	private static final int SEGMENTS = 128;
	private static final int SIDES = 10;
	private static final int TWISTS = 5;
	private static final int UV_REPEAT = 8;
	private static final float RING_RADIUS = 0.56F;
	private static final float RING_Y = 1.125F;
	private static final float PLY_OFFSET = 0.078F;
	/** How far a diving strand sinks below the braid and swings outward to meet its tributary. */
	private static final float DIVE_DEPTH = 0.17F;
	private static final float DIVE_REACH = 0.14F;

	private enum Strand {
		// texture, tint over the greyscale texture, tube radius, swell on each heartbeat
		ARTERY(MEMBRANE, 0xD8323A, 0.058F, 0.26F),
		VEIN(MEMBRANE, 0x3A4AA6, 0.036F, 0.07F),
		NERVE(MYELIN, 0xB8AA88, 0.030F, 0.0F),
		LYMPH(MEMBRANE, 0x5A7E30, 0.044F, 0.04F);

		final ResourceLocation texture;
		final int tint;
		final float radius;
		final float swell;

		Strand(ResourceLocation texture, int tint, float radius, float swell) {
			this.texture = texture;
			this.tint = tint;
			this.radius = radius;
			this.swell = swell;
		}
	}

	private CardinalFocusBraidRenderer() {
	}

	/**
	 * @param grow 0 hides the braid, 1 is full size; it swells in as the floor forms and shrinks as it withers
	 */
	static void draw(PoseStack pose, MultiBufferSource buffers, float time, float grow, float beat, int light) {
		if (grow <= 0.01F) return;
		PoseStack.Pose last = pose.last();
		Strand[] strands = Strand.values();
		for (int index = 0; index < strands.length; index++) {
			Strand strand = strands[index];
			// Each strand finishes its own buffer before the next texture is requested.
			VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(strand.texture));
			drawStrand(consumer, last, strand, index, strands.length, time, grow, beat, light);
		}
	}

	private static void drawStrand(VertexConsumer consumer, PoseStack.Pose pose, Strand strand, int index,
			int count, float time, float grow, float beat, int light) {
		int[] tint = {(strand.tint >> 16) & 0xFF, (strand.tint >> 8) & 0xFF, strand.tint & 0xFF};
		float phase = (float) (Math.PI * 2.0D * index / count);
		Vector3f[][] rings = new Vector3f[SEGMENTS + 1][];
		Vector3f[][] normals = new Vector3f[SEGMENTS + 1][];
		float[][] shades = new float[SEGMENTS + 1][SIDES + 1];
		for (int i = 0; i <= SEGMENTS; i++) {
			float theta = (float) (Math.PI * 2.0D * i / SEGMENTS);
			Vector3f centre = centre(strand, theta, phase, time, grow);
			Vector3f core = core(theta, time);
			Vector3f tangent = centre(strand, theta + 0.01F, phase, time, grow)
					.sub(centre(strand, theta - 0.01F, phase, time, grow)).normalize();
			Vector3f side = tangent.cross(0.0F, 1.0F, 0.0F, new Vector3f()).normalize();
			Vector3f up = side.cross(tangent, new Vector3f()).normalize();
			float radius = strand.radius * grow * (1.0F + strand.swell * beat) * lengthProfile(strand, theta, time);
			rings[i] = new Vector3f[SIDES + 1];
			normals[i] = new Vector3f[SIDES + 1];
			for (int j = 0; j <= SIDES; j++) {
				float phi = (float) (Math.PI * 2.0D * j / SIDES);
				Vector3f normal = new Vector3f(side).mul((float) Math.cos(phi))
						.add(new Vector3f(up).mul((float) Math.sin(phi)));
				normals[i][j] = normal;
				rings[i][j] = new Vector3f(normal).mul(radius * crossProfile(strand, theta, phi)).add(centre);
				shades[i][j] = occlusion(normal, centre, core);
			}
		}
		for (int i = 0; i < SEGMENTS; i++) {
			float u0 = (i % UV_REPEAT) / (float) UV_REPEAT;
			float u1 = (i % UV_REPEAT + 1) / (float) UV_REPEAT;
			for (int j = 0; j < SIDES; j++) {
				float v0 = j / (float) SIDES;
				float v1 = (j + 1) / (float) SIDES;
				vertex(consumer, pose, rings[i][j], normals[i][j], u0, v0, tint, shades[i][j], light);
				vertex(consumer, pose, rings[i + 1][j], normals[i + 1][j], u1, v0, tint, shades[i + 1][j], light);
				vertex(consumer, pose, rings[i + 1][j + 1], normals[i + 1][j + 1], u1, v1, tint,
						shades[i + 1][j + 1], light);
				vertex(consumer, pose, rings[i][j + 1], normals[i][j + 1], u0, v1, tint, shades[i][j + 1], light);
			}
		}
	}

	/** The braid's spine: a gently undulating ring around the Focus mouth. */
	private static Vector3f core(float theta, float time) {
		float ring = ringRadius(theta, time);
		return new Vector3f(0.5F + (float) Math.cos(theta) * ring, ringY(theta, time),
				0.5F + (float) Math.sin(theta) * ring);
	}

	private static float ringRadius(float theta, float time) {
		return RING_RADIUS + 0.015F * (float) Math.sin(theta * 5.0F + time * 0.05F);
	}

	private static float ringY(float theta, float time) {
		return RING_Y + 0.012F * (float) Math.sin(theta * 3.0F - time * 0.07F);
	}

	/**
	 * Baked shading on top of the world light: a strand darkens where it faces into the bundle or down toward the
	 * floor, so the braid reads as wound tissue resting in the membrane rather than a lit decal.
	 */
	private static float occlusion(Vector3f normal, Vector3f centre, Vector3f core) {
		Vector3f outward = new Vector3f(centre).sub(core);
		float outwardFacing = outward.lengthSquared() < 1.0e-6F ? 0.5F
				: 0.5F + 0.5F * normal.dot(outward.normalize());
		float upFacing = 0.5F + 0.5F * normal.y();
		return (0.45F + 0.55F * outwardFacing) * (0.55F + 0.45F * upFacing);
	}

	/** One strand's centre: the four spiral around the spine, and the twist creeps along it. */
	private static Vector3f centre(Strand strand, float theta, float phase, float time, float grow) {
		float twist = TWISTS * theta + phase + time * 0.04F;
		float writhe = 1.0F + 0.16F * (float) Math.sin(time * 0.09F + theta * 4.0F + phase * 1.7F);
		float ply = PLY_OFFSET * writhe * (0.5F + 0.5F * grow);
		float dive = CardinalRiteFloorVisualRules.braidDive(strand.ordinal(), theta);
		float radial = ringRadius(theta, time) + ply * (float) Math.cos(twist) + DIVE_REACH * dive;
		float y = ringY(theta, time) + ply * (float) Math.sin(twist) - DIVE_DEPTH * dive;
		if (strand == Strand.NERVE) {
			// The nerve never lies still: a fine tremor, and now and then a sharp twitch running along it.
			float twitch = (float) Math.pow(Math.max(0.0D, Math.sin(time * 0.13D + theta * 2.0D)), 12.0D);
			float jitter = 0.010F + 0.03F * twitch;
			radial += jitter * (float) (Math.sin(theta * 23.0D + time * 0.7D) + 0.6D * Math.sin(theta * 41.0D - time));
			y += jitter * (float) (Math.cos(theta * 29.0D - time * 0.8D) + 0.5D * Math.sin(theta * 53.0D + time * 1.3D));
		} else if (strand == Strand.LYMPH) {
			// Lymph creeps sluggishly.
			radial += 0.008F * (float) Math.sin(theta * 7.0F - time * 0.02F);
		}
		return new Vector3f(0.5F + (float) Math.cos(theta) * radial, y, 0.5F + (float) Math.sin(theta) * radial);
	}

	/** How each strand's thickness varies along its length. */
	private static float lengthProfile(Strand strand, float theta, float time) {
		return switch (strand) {
			case ARTERY -> 0.92F + 0.08F * (float) Math.sin(theta * 6.0F - time * 0.2F);
			case VEIN -> 0.9F + 0.1F * (float) Math.sin(theta * 4.0F + 1.1F);
			case NERVE -> 0.85F + 0.15F * (float) Math.sin(theta * 17.0F + time * 0.3F);
			// Beaded like a lymph vessel at its valves: swollen lumps with pinched necks, irregularly spaced.
			case LYMPH -> {
				double bead = 0.5D + 0.5D * Math.sin(theta * 11.0D + 1.3D) + 0.3D * Math.sin(theta * 29.0D + 0.4D);
				yield (float) (0.62D + 0.55D * Math.max(0.0D, bead) * Math.max(0.0D, bead));
			}
		};
	}

	/** How each strand's cross-section departs from a circle. */
	private static float crossProfile(Strand strand, float theta, float phi) {
		return switch (strand) {
			case ARTERY, VEIN -> 1.0F;
			// Bundled fibres give the nerve fine ridges running along it.
			case NERVE -> 1.0F + 0.2F * (float) Math.sin(phi * 6.0F + theta * 3.0F);
			// The lymph vessel is lumpy all round, not just along its length.
			case LYMPH -> 1.0F + 0.14F * (float) Math.sin(phi * 3.0F + theta * 7.0F)
					+ 0.08F * (float) Math.sin(phi * 5.0F - theta * 13.0F);
		};
	}

	private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vector3f position, Vector3f normal,
			float u, float v, int[] tint, float shade, int light) {
		consumer.addVertex(pose, position.x(), position.y(), position.z())
				.setColor(Math.round(tint[0] * shade), Math.round(tint[1] * shade), Math.round(tint[2] * shade), 255)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.setNormal(pose, normal.x(), normal.y(), normal.z());
	}
}
