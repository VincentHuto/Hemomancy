package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public final class CircusPerformerRenderer<T extends CircusPerformerEntity, M extends HumanoidModel<T>>
		extends MobRenderer<T, M> {
	private final ResourceLocation[] textures;

	public CircusPerformerRenderer(EntityRendererProvider.Context context, M model, String textureName,
			float shadowRadius) {
		super(context, model, shadowRadius);
		addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
		textures = new ResourceLocation[] {
				Hemomancy.rloc("textures/entity/circus/" + textureName + "_0.png"),
				Hemomancy.rloc("textures/entity/circus/" + textureName + "_1.png")
		};
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return textures[Math.floorMod(entity.getVariant(), textures.length)];
	}
}
