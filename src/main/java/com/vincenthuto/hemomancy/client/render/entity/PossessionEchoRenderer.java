package com.vincenthuto.hemomancy.client.render.entity;

import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.entity.PossessionEchoEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/** The echo: the owner's own skin on a still, head-bowed body, drawn as a dark translucent shadow. */
public class PossessionEchoRenderer extends EntityRenderer<PossessionEchoEntity> {
	private static final int SHADOW = 0xA0181420;
	private final PlayerModel<Player> model;

	public PossessionEchoRenderer(EntityRendererProvider.Context context) {
		super(context);
		model = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
		model.young = false;
	}

	@Override
	public void render(PossessionEchoEntity echo, float yaw, float partialTick, PoseStack pose,
			MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.scale(-1.0F, -1.0F, 1.0F);
		pose.translate(0.0, -1.501, 0.0);
		pose.mulPose(Axis.YP.rotationDegrees(180.0F - echo.getYRot()));
		model.setAllVisible(true);
		model.head.xRot = 0.45F;
		model.hat.copyFrom(model.head);
		float sway = Mth.sin(AnimationTime.age(echo, partialTick) * 0.05F) * 0.03F;
		model.rightArm.zRot = 0.05F + sway;
		model.leftArm.zRot = -0.05F - sway;
		model.rightSleeve.copyFrom(model.rightArm);
		model.leftSleeve.copyFrom(model.leftArm);
		var vertices = buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(echo)));
		model.renderToBuffer(pose, vertices, light, OverlayTexture.NO_OVERLAY, SHADOW);
		pose.popPose();
		super.render(echo, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(PossessionEchoEntity echo) {
		UUID owner = echo.owner().orElse(echo.getUUID());
		var connection = Minecraft.getInstance().getConnection();
		PlayerInfo info = connection == null ? null : connection.getPlayerInfo(owner);
		return info != null ? info.getSkin().texture() : DefaultPlayerSkin.get(owner).texture();
	}
}
