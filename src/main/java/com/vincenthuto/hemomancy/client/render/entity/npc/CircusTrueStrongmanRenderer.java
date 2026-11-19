package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusTrueStrongmanModel;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * The Strongman's true self in his Chamber (2026-11-17): the waist-up colossus rig, his eyes and his seeping blood lit,
 * and the spectral arms that manifest over the ring for his moves, drawn as translucent red light with his veins burning.
 */
public class CircusTrueStrongmanRenderer extends MobRenderer<CircusFacultyEntity, CircusTrueStrongmanModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_strongman.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_strongman_glow.png"));

	private static final RenderType GHOST = RenderType.entityTranslucentEmissive(TEXTURE);
	/** The spectral arms' tint: his own arm in red light, two-thirds seen. */
	private static final int GHOST_TINT = 0xA8FF4636;

	public CircusTrueStrongmanRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusTrueStrongmanModel(context.bakeLayer(CircusTrueStrongmanModel.LAYER_LOCATION)), 0.0F);
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
				getParentModel().renderGhosts(pose, buffers.getBuffer(GHOST), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						GHOST_TINT);
				getParentModel().renderGhosts(pose, buffers.getBuffer(GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
						0xFFFFFFFF);
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusFacultyEntity entity) {
		return TEXTURE;
	}
}
