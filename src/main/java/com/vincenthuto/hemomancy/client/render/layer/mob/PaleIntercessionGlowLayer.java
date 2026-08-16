package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.summon.PaleIntercessionModel;
import com.vincenthuto.hemomancy.client.render.entity.summon.PaleIntercessionRenderer;
import com.vincenthuto.hemomancy.common.entity.summon.PaleIntercessionEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class PaleIntercessionGlowLayer extends RenderLayer<PaleIntercessionEntity, PaleIntercessionModel> {
	public PaleIntercessionGlowLayer(RenderLayerParent<PaleIntercessionEntity, PaleIntercessionModel> parent) { super(parent); }
	@Override public void render(PoseStack pose, MultiBufferSource buffer, int light, PaleIntercessionEntity entity,
			float swing, float swingAmount, float partial, float age, float yaw, float pitch) {
		VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(PaleIntercessionRenderer.EMISSIVE));
		getParentModel().renderToBuffer(pose, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xDFFFFFFF);
	}
}
