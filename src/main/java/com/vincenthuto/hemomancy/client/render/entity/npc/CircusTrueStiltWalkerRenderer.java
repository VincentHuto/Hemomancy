package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusTrueStiltWalkerModel;
import com.vincenthuto.hemomancy.common.encounter.ordeal.StiltWalkerMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusStiltWalkerEntity;
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
 * The Stilt Walker's true self in their Chamber (2026-11-17): the twelve-block rig on its stilt legs, the eyes and the
 * seeping joins lit, and the spectral limbs that reach into the ring for their moves: stilt-legs in translucent red
 * light, and their shadow drawn near black with its eyes still burning.
 */
public class CircusTrueStiltWalkerRenderer extends MobRenderer<CircusStiltWalkerEntity, CircusTrueStiltWalkerModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_stilt_walker.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_stilt_walker_glow.png"));
	private static final RenderType GHOST = RenderType.entityTranslucentEmissive(TEXTURE);
	/** The spectral stilt-legs: their own leg in red light, two-thirds seen. */
	private static final int LEG_TINT = 0xA8FF4636;
	/** The long shadow: near black with a red cast, mostly opaque. */
	private static final int SHADOW_TINT = 0xD8301014;
	private static final int JOLT_TICKS = 9;

	public CircusTrueStiltWalkerRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusTrueStiltWalkerModel(context.bakeLayer(CircusTrueStiltWalkerModel.LAYER_LOCATION)), 0.0F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
		addLayer(new RenderLayer<>(this) {
			@Override
			public void render(PoseStack pose, MultiBufferSource buffers, int light, CircusStiltWalkerEntity entity,
					float limbSwing, float limbSwingAmount, float partialTick, float age, float yaw, float pitch) {
				getParentModel().renderGhosts(pose, buffers.getBuffer(GHOST), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						LEG_TINT, SHADOW_TINT);
				getParentModel().renderGhosts(pose, buffers.getBuffer(GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						0xFFFFFFFF, 0xFFFFFFFF);
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusStiltWalkerEntity entity) {
		return TEXTURE;
	}

	/**
	 * The camera's answer to the Stilt Walker's moves, {@code age} ticks into gesture {@code gesture}: a hard jolt as
	 * the planted stilt strikes, a knock at every footfall of the stride, a low rumble while the stilts spin, and the
	 * heaviest jolt when the shadow lands.
	 */
	public static float cameraShake(int gesture, float age) {
		return switch (gesture) {
			case CircusStiltWalkerEntity.GESTURE_PLANT -> jolt(age - StiltWalkerMoves.PLANT_WINDUP, 2.4F);
			case CircusStiltWalkerEntity.GESTURE_STRIDE -> {
				float shake = 0.0F;
				for (int k = 0; k < StiltWalkerMoves.STRIDE_STEPS; k++)
					shake = Math.max(shake, jolt(age - StiltWalkerMoves.strideLands(k), 1.3F));
				yield shake;
			}
			case CircusStiltWalkerEntity.GESTURE_SPIN -> age >= StiltWalkerMoves.SPIN_WINDUP
					&& age < StiltWalkerMoves.SPIN_WINDUP + StiltWalkerMoves.SPIN_TICKS ? .3F : 0.0F;
			case CircusStiltWalkerEntity.GESTURE_FALL -> jolt(age - StiltWalkerMoves.SHADOW_WINDUP, 3.2F);
			default -> 0.0F;
		};
	}

	private static float jolt(float since, float peak) {
		if (since < 0.0F || since >= JOLT_TICKS) return 0.0F;
		float left = 1.0F - since / JOLT_TICKS;
		return peak * left * left;
	}
}
