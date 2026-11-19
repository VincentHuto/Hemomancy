package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusTrueAcrobatModel;
import com.vincenthuto.hemomancy.common.encounter.ordeal.AcrobatOrdealHits;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusAcrobatEntity;
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
 * The Acrobat's true self in their Chamber (2026-11-17): hung from the trapeze, their eyes and their blood-lit lines
 * glowing, and their rigging in the ring for each move (the spectral trapeze, their double, the snare line and the
 * net), drawn as translucent red light with a burning core.
 */
public class CircusTrueAcrobatRenderer extends MobRenderer<CircusAcrobatEntity, CircusTrueAcrobatModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_acrobat.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_acrobat_glow.png"));
	private static final RenderType GHOST = RenderType.entityTranslucentEmissive(TEXTURE);
	/** The rigging's tint: their own blood in red light, mostly seen. */
	private static final int GHOST_TINT = 0xC8FF4636;
	private static final int JOLT_TICKS = 9;

	public CircusTrueAcrobatRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusTrueAcrobatModel(context.bakeLayer(CircusTrueAcrobatModel.LAYER_LOCATION)), 0.0F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
		addLayer(new RenderLayer<>(this) {
			@Override
			public void render(PoseStack pose, MultiBufferSource buffers, int light, CircusAcrobatEntity entity, float limbSwing,
					float limbSwingAmount, float partialTick, float age, float yaw, float pitch) {
				getParentModel().renderGhosts(pose, buffers.getBuffer(GHOST), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						GHOST_TINT);
				getParentModel().renderGhosts(pose, buffers.getBuffer(GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						0xFFFFFFFF);
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusAcrobatEntity entity) {
		return TEXTURE;
	}

	/**
	 * How hard the Acrobat's moves shake the camera, {@code age} ticks into the gesture: a rumble while the trapeze
	 * swings through, a hard jolt when the double lands, a snap when the snare hauls, a thump when the net falls.
	 */
	public static float cameraShake(int gesture, float age) {
		return switch (gesture) {
			case CircusAcrobatEntity.GESTURE_SWING -> {
				float since = age - AcrobatOrdealHits.SWING_WINDUP;
				yield since >= 0.0F && since < AcrobatOrdealHits.SWING_TICKS ? .4F : 0.0F;
			}
			case CircusAcrobatEntity.GESTURE_DROP -> jolt(age - AcrobatOrdealHits.DROP_WINDUP, 2.4F);
			case CircusAcrobatEntity.GESTURE_SNARE -> jolt(age - AcrobatOrdealHits.SNARE_WINDUP, 1.1F);
			case CircusAcrobatEntity.GESTURE_NET -> jolt(age - AcrobatOrdealHits.NET_WINDUP, 1.5F);
			default -> 0.0F;
		};
	}

	private static float jolt(float since, float peak) {
		if (since < 0.0F || since >= JOLT_TICKS) return 0.0F;
		float left = 1.0F - since / JOLT_TICKS;
		return peak * left * left;
	}
}
