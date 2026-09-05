package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusRingmasterModel;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;

public final class CircusRingmasterRenderer extends MobRenderer<CircusRingmasterEntity, CircusRingmasterModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/npc/harbinger/circus/ringmaster.png");
	private static final RenderType GLOW = RenderType.eyes(
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/ringmaster_glow.png"));

	public CircusRingmasterRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusRingmasterModel(context.bakeLayer(CircusRingmasterModel.LAYER_LOCATION)), 0.45F);
		addLayer(new EyesLayer<>(this) {
			@Override public RenderType renderType() { return GLOW; }
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusRingmasterEntity entity) {
		return TEXTURE;
	}

	@Override
	public boolean shouldRender(CircusRingmasterEntity entity, Frustum frustum,
			double cameraX, double cameraY, double cameraZ) {
		return frustum.isVisible(entity.getBoundingBoxForCulling().inflate(1.0D));
	}
}
