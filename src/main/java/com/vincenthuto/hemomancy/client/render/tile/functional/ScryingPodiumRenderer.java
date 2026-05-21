package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.tile.RadiantPortalRendertype;
import com.vincenthuto.hemomancy.common.tile.functional.ScryingPodiumBlockEntity;
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
import org.joml.Matrix4f;

public class ScryingPodiumRenderer implements BlockEntityRenderer<ScryingPodiumBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final int FULL_BRIGHT = 15728880;
	private static final float POOL_WAVE_HEIGHT = 0.04f;
	private static final float POOL_WAVE_INTENSITY = 30f;

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
		ResourceLocation GLASSTEXTURE = Hemomancy.rloc("textures/block/sanguine_tran_pane.png");

		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5f, 1.0075f, 0.5f);
		matrixStackIn.scale(0.18f, 0.18f, 0.18f);
		matrixStackIn.mulPose(new Quaternion(Vector3.XN, -90, true).toMoj());

		Matrix4f mat = matrixStackIn.last().pose();

		float time = (te.getLevel().getGameTime() + partialTicks) / 10;
		VertexConsumer vertex = bufferIn.getBuffer(RadiantPortalRendertype.textWithWaterShader(GLASSTEXTURE));

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
					addWavyVertex(vertex, mat, i, g, time, uvValue, uvValueg, r, gr, b);
					addWavyVertex(vertex, mat, i, g + mod, time, uvValue, uvValueg + mod * 0.5f, r, gr, b);
					addWavyVertex(vertex, mat, i + mod, g + mod, time,
							uvValue + mod * 0.5f, uvValueg + mod * 0.5f, r, gr, b);
					addWavyVertex(vertex, mat, i + mod, g, time, uvValue + mod * 0.5f, uvValueg, r, gr, b);

					addWavyVertex(vertex, mat, i + mod, g, time, uvValue + mod * 0.5f, uvValueg, r, gr, b);
					addWavyVertex(vertex, mat, i + mod, g + mod, time,
							uvValue + mod * 0.5f, uvValueg + mod * 0.5f, r, gr, b);
					addWavyVertex(vertex, mat, i, g + mod, time, uvValue, uvValueg + mod * 0.5f, r, gr, b);
					addWavyVertex(vertex, mat, i, g, time, uvValue, uvValueg, r, gr, b);
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

	private static void addWavyVertex(VertexConsumer vertex, Matrix4f mat, float x, float y, float time, float u, float v,
			float r, float g, float b) {
		float distance = (float) Math.sqrt(x * x + y * y);
		float wave = (float) Math.sin(distance * POOL_WAVE_INTENSITY + time) * POOL_WAVE_HEIGHT;
		vertex.addVertex(mat, x, y, wave).setColor(r, g, b, 1f).setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT);
	}
}
