package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusTrueBeastTamerModel;
import com.vincenthuto.hemomancy.common.encounter.ordeal.BeastTamerMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
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
 * The Beast Tamer's true self in their Chamber (2026-11-17): the jointed rig with the beast's pelt and skull, their eyes,
 * the skull's eyes and the lash's vein lit, and what reaches into the ring for their moves: the lash, a strand of their
 * own blood, and the beasts out of the dark, both drawn as translucent red light with lit eyes.
 */
public class CircusTrueBeastTamerRenderer extends MobRenderer<CircusFacultyEntity, CircusTrueBeastTamerModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_beast_tamer.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_beast_tamer_glow.png"));
	private static final RenderType GHOST = RenderType.entityTranslucentEmissive(TEXTURE);
	/** The manifestations' tint: red light, three-quarters seen. */
	private static final int GHOST_TINT = 0xC0FF4636;
	/** The lash reaches the far rim and the beasts come from past the bars: wider than the entity's own box. */
	private static final double REACH = 26.0D;
	private static final int JOLT_TICKS = 9;

	public CircusTrueBeastTamerRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusTrueBeastTamerModel(context.bakeLayer(CircusTrueBeastTamerModel.LAYER_LOCATION)), 0.0F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
		addLayer(new RenderLayer<>(this) {
			@Override
			public void render(PoseStack pose, MultiBufferSource buffers, int light, CircusFacultyEntity entity, float limbSwing,
					float limbSwingAmount, float partialTick, float age, float yaw, float pitch) {
				CircusTrueBeastTamerModel model = getParentModel();
				model.renderLash(pose, buffers.getBuffer(GHOST), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, GHOST_TINT);
				model.renderLash(pose, buffers.getBuffer(GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
				model.renderBeasts(pose, buffers.getBuffer(GHOST), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, GHOST_TINT);
				model.renderBeasts(pose, buffers.getBuffer(GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusFacultyEntity entity) {
		return TEXTURE;
	}

	@Override
	public boolean shouldRender(CircusFacultyEntity entity, Frustum frustum, double x, double y, double z) {
		AABB drawn = new AABB(entity.getX() - REACH, entity.getY() - 2.0D, entity.getZ() - REACH, entity.getX() + REACH,
				entity.getY() + 8.0D, entity.getZ() + REACH);
		return entity.shouldRender(x, y, z) && frustum.isVisible(drawn);
	}

	/**
	 * How hard the Beast Tamer's moves shake the camera, {@code age} ticks into the gesture: a sharp snap on each crack
	 * of the lash, a thump as the hoop lights, and a jolt as each beast lands.
	 */
	public static float cameraShake(int gesture, float age) {
		return switch (gesture) {
			case BeastTamerMoves.GESTURE_CRACK -> jolt(age - BeastTamerMoves.CRACK_WINDUP, 1.5F);
			case BeastTamerMoves.GESTURE_HOOP -> jolt(age - BeastTamerMoves.HOOP_WINDUP, 1.2F);
			case BeastTamerMoves.GESTURE_GOAD -> jolt(age - BeastTamerMoves.GOAD_WINDUP, .8F);
			case BeastTamerMoves.GESTURE_BEASTS -> {
				float shake = 0.0F;
				for (int i = 0; i < BeastTamerMoves.BEASTS; i++)
					shake = Math.max(shake, jolt(age - BeastTamerMoves.beastLands(i) - BeastTamerMoves.BEAST_RUN, 1.7F));
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
