package com.vincenthuto.hemomancy.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.tile.ScryingPodiumBlockEntity;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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
		Player player = HLClientUtils.getClientPlayer();
		Minecraft mc = HLClientUtils.getClient();
		Quaternion quaternion = Vector3.YP.rotationDegrees(180.0F);
		matrixStackIn.translate(0.5, 1, 0.5);
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