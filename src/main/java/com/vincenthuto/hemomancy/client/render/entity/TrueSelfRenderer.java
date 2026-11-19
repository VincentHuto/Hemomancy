package com.vincenthuto.hemomancy.client.render.entity;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.entity.npc.circus.TrueSelf;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * Draws a creature as its physical self or, in a professor's Chamber, as its true self (the Chamber ordeals,
 * 2026-11-17): two whole renderers, picked each frame by the synced {@link TrueSelf} flag. The true self may be drawn
 * larger than the body it stands in for, and may be a bust sunk to the waist (its waist at the entity's feet): a
 * colossus wading round its ring, its waist fading into the Chamber's dark so only the torso, arms and head rise out of
 * it from any angle.
 */
public final class TrueSelfRenderer<T extends Entity & TrueSelf> extends EntityRenderer<T> {
	private final EntityRenderer<T> physical;
	private final EntityRenderer<T> trueSelf;
	private final float trueScale;
	/** For a bust sunk to the waist, the haze's radius round the waist in model blocks; 0 for a true self that stands. */
	private final double hazeRadius;
	/** The Chamber's dark, which the waist fades into. */
	private static final int HAZE = 0x120607;
	private static final int HAZE_SIDES = 24;

	public TrueSelfRenderer(EntityRendererProvider.Context context, EntityRenderer<T> physical, EntityRenderer<T> trueSelf,
			float trueScale) {
		this(context, physical, trueSelf, trueScale, 0.0D);
	}

	public TrueSelfRenderer(EntityRendererProvider.Context context, EntityRenderer<T> physical, EntityRenderer<T> trueSelf,
			float trueScale, double hazeRadius) {
		super(context);
		this.physical = physical;
		this.trueSelf = trueSelf;
		this.trueScale = trueScale;
		this.hazeRadius = hazeRadius;
	}

	private EntityRenderer<T> pick(T entity) {
		return entity.isTrueSelf() ? trueSelf : physical;
	}

	@Override
	public void render(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
		// The dispatcher draws the shadow from this renderer's own size after rendering: carry the drawn renderer's, scaled
		// with the true self (a bust sunk to the waist casts none).
		float inner = ((com.vincenthuto.hemomancy.mixin.core.EntityRendererShadowAccessor) pick(entity)).hemomancy$shadowRadius();
		this.shadowRadius = !entity.isTrueSelf() ? inner : hazeRadius > 0.0D ? 0.0F : inner * trueScale;
		if (!entity.isTrueSelf()) {
			physical.render(entity, yaw, partialTick, poseStack, buffers, light);
			return;
		}
		poseStack.pushPose();
		poseStack.scale(trueScale, trueScale, trueScale);
		trueSelf.render(entity, yaw, partialTick, poseStack, buffers, light);
		poseStack.popPose();
		if (hazeRadius > 0.0D) haze(poseStack.last().pose(), buffers.getBuffer(HemoRenderTypes.SAINT_TELEGRAPH_TINT));
	}

	/**
	 * Two soft walls of the Chamber's dark round the waist: opaque below it, thinning to nothing a third of the way up
	 * the torso, so the cut where the legs would be is never seen.
	 */
	private void haze(Matrix4f pose, VertexConsumer v) {
		double[] heights = {-2.0D, 0.0D, trueScale * 0.28D};
		float[] alphas = {1.0F, 0.92F, 0.0F};
		for (double radius : new double[]{trueScale * hazeRadius, trueScale * hazeRadius * 1.2D}) {
			for (int i = 0; i < HAZE_SIDES; i++) {
				double a0 = Math.PI * 2.0D * i / HAZE_SIDES, a1 = Math.PI * 2.0D * (i + 1) / HAZE_SIDES;
				float x0 = (float) (Math.cos(a0) * radius), z0 = (float) (Math.sin(a0) * radius);
				float x1 = (float) (Math.cos(a1) * radius), z1 = (float) (Math.sin(a1) * radius);
				for (int k = 0; k < heights.length - 1; k++) {
					float lo = (float) heights[k], hi = (float) heights[k + 1];
					vertex(v, pose, x0, lo, z0, alphas[k]);
					vertex(v, pose, x1, lo, z1, alphas[k]);
					vertex(v, pose, x1, hi, z1, alphas[k + 1]);
					vertex(v, pose, x0, hi, z0, alphas[k + 1]);
				}
			}
		}
	}

	private static void vertex(VertexConsumer v, Matrix4f pose, float x, float y, float z, float alpha) {
		v.addVertex(pose, x, y, z).setColor((HAZE >> 16) & 255, (HAZE >> 8) & 255, HAZE & 255, (int) (alpha * 255.0F));
	}

	@Override
	public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
		if (!entity.isTrueSelf() || hazeRadius <= 0.0D) return pick(entity).shouldRender(entity, frustum, x, y, z);
		// The entity's own box is a man's; the drawn colossus is many times wider and taller, his arms reaching across the ring.
		double half = trueScale * 10.0D;
		AABB drawn = new AABB(entity.getX() - half, entity.getY() - 2.0D, entity.getZ() - half, entity.getX() + half,
				entity.getY() + trueScale * 4.0D, entity.getZ() + half);
		return entity.shouldRender(x, y, z) && frustum.isVisible(drawn);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return pick(entity).getTextureLocation(entity);
	}
}
