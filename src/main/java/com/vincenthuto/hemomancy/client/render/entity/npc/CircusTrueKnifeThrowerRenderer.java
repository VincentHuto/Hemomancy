package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusTrueKnifeThrowerModel;
import com.vincenthuto.hemomancy.common.encounter.ordeal.KnifeOrdealMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusKnifeThrowerEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * The Dagger Thrower's true self in their Chamber (2026-11-17): the hooded figure on the throwing gallery, eyes lit in
 * the hood, and the spectral blades that cross the ring for their moves, drawn as translucent red light with their
 * edges burning. Also how hard their blows shake the camera ({@link #cameraShake}).
 */
public class CircusTrueKnifeThrowerRenderer extends MobRenderer<CircusKnifeThrowerEntity, CircusTrueKnifeThrowerModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_knife_thrower.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_knife_thrower_glow.png"));
	private static final RenderType GHOST = RenderType.entityTranslucentEmissive(TEXTURE);
	/** The spectral blades' tint: steel in red light, most of the way seen. */
	private static final int GHOST_TINT = 0xD8FF5040;
	/** How far round the true self its blades can fly: across the ring and out past it. */
	private static final double REACH = 30.0D;
	private static final int JOLT_TICKS = 9;

	public CircusTrueKnifeThrowerRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusTrueKnifeThrowerModel(context.bakeLayer(CircusTrueKnifeThrowerModel.LAYER_LOCATION)), 0.0F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
		addLayer(new RenderLayer<>(this) {
			@Override
			public void render(PoseStack pose, MultiBufferSource buffers, int light, CircusKnifeThrowerEntity entity,
					float limbSwing, float limbSwingAmount, float partialTick, float age, float yaw, float pitch) {
				getParentModel().renderGhosts(pose, buffers.getBuffer(GHOST), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						GHOST_TINT);
				getParentModel().renderGhosts(pose, buffers.getBuffer(GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						0xFFFFFFFF);
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusKnifeThrowerEntity entity) {
		return TEXTURE;
	}

	/** The body is a man's box; the blades it throws cross the whole ring, so it is drawn while any of that is in view. */
	@Override
	public boolean shouldRender(CircusKnifeThrowerEntity entity, Frustum frustum, double x, double y, double z) {
		AABB drawn = new AABB(entity.getX() - REACH, entity.getY() - 4.0D, entity.getZ() - REACH, entity.getX() + REACH,
				entity.getY() + 10.0D, entity.getZ() + REACH);
		return entity.shouldRender(x, y, z) && frustum.isVisible(drawn);
	}

	/**
	 * How hard the Dagger Thrower's blows shake the camera, {@code age} ticks into the gesture: sharp when the fan lands
	 * and when the pin goes in, a grind while the ring closes and a clash when it meets, a tick on each outline blade and
	 * the hardest jolt on the bullseye.
	 */
	public static float cameraShake(int gesture, float age) {
		return switch (gesture) {
			case KnifeOrdealMoves.GESTURE_FAN -> jolt(age - KnifeOrdealMoves.FAN_WINDUP - KnifeOrdealMoves.FLIGHT, 1.4F);
			case KnifeOrdealMoves.GESTURE_RING -> {
				float sliding = age - KnifeOrdealMoves.RING_WINDUP;
				yield Math.max(sliding >= 0.0F && sliding < KnifeOrdealMoves.RING_CLOSE ? .3F : 0.0F,
						jolt(sliding - KnifeOrdealMoves.RING_CLOSE, 1.3F));
			}
			case KnifeOrdealMoves.GESTURE_PIN -> jolt(age - KnifeOrdealMoves.PIN_WINDUP, 2.0F);
			case KnifeOrdealMoves.GESTURE_BOARD -> {
				float shake = jolt(age - KnifeOrdealMoves.BULLSEYE_AT, 2.4F);
				for (int k = 0; k < KnifeOrdealMoves.BOARD_KNIVES; k++)
					shake = Math.max(shake, jolt(age - KnifeOrdealMoves.boardLanding(k), .6F));
				yield shake;
			}
			default -> 0.0F;
		};
	}

	private static float jolt(float since, float peak) {
		if (since < 0.0F || since >= JOLT_TICKS) return 0.0F;
		float left = 1.0F - since / JOLT_TICKS;
		return peak * left * left;
	}
}
