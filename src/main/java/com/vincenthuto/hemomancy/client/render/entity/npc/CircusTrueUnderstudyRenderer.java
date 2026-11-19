package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusTrueUnderstudyModel;
import com.vincenthuto.hemomancy.common.encounter.ordeal.UnderstudyChoreography;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * The Retired Understudy's true self in their Chamber (2026-11-17): standing in the prompt corner, the mask's eyes and
 * the cue light lit, and their double, who steps into the ring to play the fighter's part back at them, drawn as a
 * life-sized copy in translucent red light.
 */
public class CircusTrueUnderstudyRenderer extends MobRenderer<CircusFacultyEntity, CircusTrueUnderstudyModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_understudy.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_understudy_glow.png"));
	private static final RenderType DOUBLE = RenderType.entityTranslucentEmissive(TEXTURE);
	/** The double's tint: their own costume in red light, most of it seen. */
	private static final int DOUBLE_TINT = 0xD8FF4A3A;
	private static final int JOLT_TICKS = 9;

	public CircusTrueUnderstudyRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusTrueUnderstudyModel(context.bakeLayer(CircusTrueUnderstudyModel.LAYER_LOCATION)), 0.0F);
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
				getParentModel().renderDouble(pose, buffers.getBuffer(DOUBLE), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						DOUBLE_TINT);
				getParentModel().renderDouble(pose, buffers.getBuffer(GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						0xFFFFFFFF);
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusFacultyEntity entity) {
		return TEXTURE;
	}

	/**
	 * How hard the Understudy's interventions shake the camera, {@code since} ticks after their gesture began: a jolt as
	 * the spotlight burns, a long grind while the curtain crosses, a knock for each reprise beat, three small knocks and a
	 * heavy drop for the trapdoors.
	 */
	public static float cameraShake(int gesture, float since) {
		return switch (gesture) {
			case UnderstudyChoreography.GESTURE_LOCK -> jolt(since - UnderstudyChoreography.LOCK, 1.8F);
			case UnderstudyChoreography.GESTURE_CURTAIN -> Math.max(jolt(since - 24.0F, 1.4F),
					since >= 24.0F && since < 64.0F ? .3F : 0.0F);
			case UnderstudyChoreography.GESTURE_BEAT -> jolt(since - UnderstudyChoreography.LEAD, .9F);
			case UnderstudyChoreography.GESTURE_KNOCK -> {
				float shake = jolt(since - (UnderstudyChoreography.KNOCKS[2] + 2.0F), 2.6F);
				for (int knock : UnderstudyChoreography.KNOCKS) shake = Math.max(shake, jolt(since - knock, .45F));
				yield shake;
			}
			default -> 0.0F;
		};
	}

	/** A hard jolt fading over {@link #JOLT_TICKS}. */
	private static float jolt(float since, float peak) {
		if (since < 0.0F || since >= JOLT_TICKS) return 0.0F;
		float left = 1.0F - since / JOLT_TICKS;
		return peak * left * left;
	}
}
