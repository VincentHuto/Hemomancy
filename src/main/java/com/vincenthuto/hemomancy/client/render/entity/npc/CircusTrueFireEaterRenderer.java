package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusTrueFireEaterModel;
import com.vincenthuto.hemomancy.common.encounter.ordeal.FireEaterChoreography;
import com.vincenthuto.hemomancy.common.encounter.ordeal.FireEaterOrdealGeometry;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFireEaterEntity;
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
 * The Fire Breather's true self in their Chamber (2026-11-17): the masked performer made a flue, the fire in their
 * eyes, mouth, chest, flues and burning hems lit, and their breath drawn as a body of flame from the mouth into the
 * ring ({@link CircusTrueFireEaterModel#renderBreath}): the flames once in hot light, once more in their glow.
 */
public class CircusTrueFireEaterRenderer extends MobRenderer<CircusFireEaterEntity, CircusTrueFireEaterModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_fire_eater.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_fire_eater_glow.png"));
	private static final RenderType BREATH = RenderType.entityTranslucentEmissive(TEXTURE);
	/** The breath's tint: its own flame in hot light, mostly seen. */
	private static final int BREATH_TINT = 0xD8FFE0C0;

	public CircusTrueFireEaterRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusTrueFireEaterModel(context.bakeLayer(CircusTrueFireEaterModel.LAYER_LOCATION)), 0.0F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
		addLayer(new RenderLayer<>(this) {
			@Override
			public void render(PoseStack pose, MultiBufferSource buffers, int light, CircusFireEaterEntity entity, float limbSwing,
					float limbSwingAmount, float partialTick, float age, float yaw, float pitch) {
				getParentModel().renderBreath(pose, buffers.getBuffer(BREATH), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						BREATH_TINT);
				getParentModel().renderBreath(pose, buffers.getBuffer(GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						0xFFFFFFFF);
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusFireEaterEntity entity) {
		return TEXTURE;
	}

	/** How long a jolt takes to die away, in ticks. */
	private static final float JOLT_TICKS = 9.0F;

	/**
	 * How hard the Fire Breather's interventions shake the camera, {@code age} ticks into the gesture: a roar running
	 * down the ring with the long breath, a knock for each ember landing, a hard jolt as the ring lights and a grinding
	 * heat while it closes, and the heaviest jolt as the cinder rain erupts, a knock for each cinder after.
	 */
	public static float cameraShake(int gesture, float age) {
		return switch (gesture) {
			case CircusFireEaterEntity.GESTURE_BREATH -> {
				float since = age - FireEaterChoreography.BREATH_IMPACT;
				yield since < 0.0F ? 0.0F : Math.max(jolt(since, 1.4F), since < 14.0F ? .45F : 0.0F);
			}
			case CircusFireEaterEntity.GESTURE_SPIT -> {
				float shake = 0.0F;
				for (int i = 0; i < 3; i++)
					shake = Math.max(shake, jolt(age - FireEaterChoreography.SPIT_IMPACT - i * FireEaterChoreography.SPIT_GAP, .9F));
				yield shake;
			}
			case CircusFireEaterEntity.GESTURE_RING -> {
				float since = age - FireEaterChoreography.RING_IMPACT;
				yield since < 0.0F ? 0.0F : Math.max(jolt(since, 2.0F),
						since < FireEaterOrdealGeometry.RING_CLOSE_TICKS ? .22F : 0.0F);
			}
			case CircusFireEaterEntity.GESTURE_RAIN -> {
				float shake = jolt(age - FireEaterChoreography.RAIN_IMPACT, 2.6F);
				for (int i = 0; i < FireEaterOrdealGeometry.RAIN_MARKS; i++)
					shake = Math.max(shake, jolt(age - FireEaterChoreography.lands(i), .7F));
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
