package com.vincenthuto.hemomancy.client.render.tile;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.ScryingPodiumBlockEntity;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class ScryingPodiumRenderer implements BlockEntityRenderer<ScryingPodiumBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public ScryingPodiumRenderer(BlockEntityRendererProvider.Context p_173636_) {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(ScryingPodiumBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		this.renderPortal(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		this.renderPlayer(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
	}

	public void renderPortal(ScryingPodiumBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		ResourceLocation GLASSTEXTURE = new ResourceLocation(Hemomancy.MOD_ID, "textures/block/sanguine_tran_pane.png");

		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5f, 1.0075f, 0.5f);
		matrixStackIn.scale(0.18f, 0.18f, 0.18f);
		matrixStackIn.mulPose(new Quaternion(Vector3.XN, -90, true).toMoj());

		Matrix4f mat = matrixStackIn.last().pose();

		VertexConsumer vertex;
		float time = (te.getLevel().getGameTime() + partialTicks) / 10;
		RadiantPortalRendertype.WATER_SHADER.safeGetUniform("time").set(time);
		RadiantPortalRendertype.WATER_SHADER.safeGetUniform("modelview").set(mat);
		RadiantPortalRendertype.WATER_SHADER.safeGetUniform("sinModifier").set(0.04f);
		RadiantPortalRendertype.WATER_SHADER.safeGetUniform("intensity").set(30f);
		vertex = bufferIn.getBuffer(RadiantPortalRendertype.textWithWaterShader(GLASSTEXTURE));

		float r;
		float gr;
		float b;
		r = 1f;
		gr = 1;
		b = 1f;

		float mod = 0.125f;
		for (float i = -1; i < 1; i += mod) {
			float uvValue = (i + 1) * 0.5f;
			for (float g = -1; g < 1; g += mod) {
				float uvValueg = (g + 1) * 0.5f;
				float rast = (float) Math.sqrt(i * i + g * g);

				if (rast <= 1.05f) {
					vertex.vertex(i, g, 0).color(r, gr, b, 1f).uv(uvValue, uvValueg)
							.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
					vertex.vertex(i, g + mod, 0).color(r, gr, b, 1f).uv(uvValue, uvValueg + mod * 0.5f)
							.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
					vertex.vertex(i + mod, g + mod, 0).color(r, gr, b, 1f)
							.uv(uvValue + mod * 0.5f, uvValueg + mod * 0.5f).overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880).endVertex();
					vertex.vertex(i + mod, g, 0).color(r, gr, b, 1f).uv(uvValue + mod * 0.5f, uvValueg)
							.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();

					vertex.vertex(i + mod, g, 0).color(r, gr, b, 1f).uv(uvValue + mod * 0.5f, uvValueg)
							.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
					vertex.vertex(i + mod, g + mod, 0).color(r, gr, b, 1f)
							.uv(uvValue + mod * 0.5f, uvValueg + mod * 0.5f).overlayCoords(OverlayTexture.NO_OVERLAY)
							.uv2(15728880).endVertex();
					vertex.vertex(i, g + mod, 0).color(r, gr, b, 1f).uv(uvValue, uvValueg + mod * 0.5f)
							.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
					vertex.vertex(i, g, 0).color(r, gr, b, 1f).uv(uvValue, uvValueg)
							.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
				}
			}

		}
		matrixStackIn.popPose();
	}

	public void renderPlayer(ScryingPodiumBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		Player player = HLClientUtils.getClientPlayer();
		Minecraft mc = HLClientUtils.getClient();
		Quaternion quaternion = Vector3.YP.rotationDegrees(180.0F);
		matrixStackIn.translate(0.5, 1, 0.5);
		matrixStackIn.scale(0.1f, 0.1f, 0.1f);
		matrixStackIn.mulPose(quaternion.toMoj());
		player.yHeadRot = player.getYRot();
		player.yHeadRotO = player.getYRot();
		EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		entityrenderdispatcher.setRenderShadow(false);
		MultiBufferSource.BufferSource bs = Minecraft.getInstance().renderBuffers().bufferSource();
		bs.getBuffer(RenderType.lines());
		entityrenderdispatcher.render(player, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, matrixStackIn, bs, 15728880);
	}
}
