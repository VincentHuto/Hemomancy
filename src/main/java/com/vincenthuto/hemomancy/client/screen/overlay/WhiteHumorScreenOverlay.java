package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class WhiteHumorScreenOverlay {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/block/white_humor_overlay.png");

	public static void render(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level == null || minecraft.player.isSpectator()) {
			return;
		}

		Camera camera = minecraft.gameRenderer.getMainCamera();
		if (!isCameraInWhiteHumor(minecraft, camera.getPosition())) {
			return;
		}

		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		float uOffset = -minecraft.player.getYRot() / 80.0F;
		float vOffset = minecraft.player.getXRot() / 80.0F;
		Matrix4f matrix = graphics.pose().last().pose();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(0.88F, 0.95F, 1.0F, 0.38F);

		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.addVertex(matrix, 0.0F, height, -90.0F).setUv(4.0F + uOffset, 4.0F + vOffset);
		buffer.addVertex(matrix, width, height, -90.0F).setUv(0.0F + uOffset, 4.0F + vOffset);
		buffer.addVertex(matrix, width, 0.0F, -90.0F).setUv(0.0F + uOffset, 0.0F + vOffset);
		buffer.addVertex(matrix, 0.0F, 0.0F, -90.0F).setUv(4.0F + uOffset, 0.0F + vOffset);
		BufferUploader.drawWithShader(buffer.buildOrThrow());

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
	}

	private static boolean isCameraInWhiteHumor(Minecraft minecraft, Vec3 cameraPos) {
		BlockPos blockPos = BlockPos.containing(cameraPos);
		FluidState fluidState = minecraft.level.getFluidState(blockPos);
		if (!isWhiteHumor(fluidState)) {
			return false;
		}

		float fluidHeight = fluidState.getHeight(minecraft.level, blockPos);
		return cameraPos.y - blockPos.getY() <= fluidHeight;
	}

	private static boolean isWhiteHumor(FluidState fluidState) {
		return fluidState.is(FluidInit.WHITE_HUMOR.get())
				|| fluidState.is(FluidInit.WHITE_HUMOR_FLOWING.get());
	}
}
