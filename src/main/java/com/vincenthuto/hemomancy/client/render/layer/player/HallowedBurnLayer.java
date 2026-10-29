package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.data.MausoleumWardClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/**
 * An Apotheos burning on a Saint's hallowed ground: the body drawn again in the mausoleum ward's shader, its hyphae
 * charred black and the ward's runes crawling over it in the Saint's colour. It holds while the server's burn pulses
 * arrive ({@link MausoleumWardClientState}), fading in and out; the smoke rises from the same state.
 */
public final class HallowedBurnLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	public HallowedBurnLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch) {
		if (!MausoleumWardClientState.anyBurning() || player.isInvisible()) return;
		float burn = MausoleumWardClientState.burn(player.getId(), partialTicks);
		if (burn <= 0.0F) return;
		int color = MausoleumWardClientState.burnColor(player.getId());
		int argb = Mth.clamp(Math.round(burn * 255.0F), 0, 255) << 24 | color & 0xFFFFFF;
		getParentModel().renderToBuffer(poseStack,
				buffer.getBuffer(HemoRenderTypes.hallowedBurn(player.getSkin().texture(),
						AnimationTime.gameTime(player.level(), partialTicks))),
				packedLight, OverlayTexture.NO_OVERLAY, argb);
	}
}
