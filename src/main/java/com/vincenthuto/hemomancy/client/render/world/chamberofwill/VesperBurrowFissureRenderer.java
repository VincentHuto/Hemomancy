package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.data.VesperBurrowClientData;
import com.vincenthuto.hemomancy.client.data.VesperFightClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * The tunnelling mount's crack across the ordeal floor: a glowing fissure that tears open behind the hidden trail
 * and slowly closes. Drawn only on the authored arena floor; elsewhere the server's chip particles carry the trail.
 */
public final class VesperBurrowFissureRenderer {
	private static final float LIFT = 0.012F;

	private VesperBurrowFissureRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || !VesperFightClientData.isActive()) return;
		List<VesperBurrowClientData.Point> points = VesperBurrowClientData.points();
		if (points.size() < 2) return;
		Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
		float now = minecraft.level.getGameTime() + partialTick;
		poseStack.pushPose();
		poseStack.translate(-camera.x, -camera.y, -camera.z);
		Matrix4f matrix = poseStack.last().pose();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		for (int i = 1; i < points.size(); i++) {
			VesperBurrowClientData.Point from = points.get(i - 1);
			VesperBurrowClientData.Point to = points.get(i);
			if (to.breakSegment()) continue;
			float age = now - to.gameTime();
			// Freshly torn cracks flare hot, then cool and narrow as the floor settles back.
			float life = 1.0F - Mth.clamp(age / VesperBurrowClientData.FADE_TICKS, 0.0F, 1.0F);
			float heat = Mth.clamp(1.0F - age / 20.0F, 0.0F, 1.0F);
			float jag = (float) Math.sin(to.gameTime() * 1.37D + i * 2.1D) * 0.22F;
			float width = (0.16F + 0.18F * heat) * (0.5F + 0.5F * life);
			int red = (int) Mth.lerp(heat, 120.0F, 235.0F);
			int green = (int) Mth.lerp(heat, 6.0F, 46.0F);
			int blue = (int) Mth.lerp(heat, 12.0F, 40.0F);
			ribbon(buffer, matrix, from, to, jag, width * 3.0F, red, green, blue, (int) (70 * life));
			ribbon(buffer, matrix, from, to, jag, width, Math.min(255, red + 30), green + 10, blue + 10,
					(int) (215 * life));
		}
		MeshData mesh = buffer.build();
		if (mesh != null) BufferUploader.drawWithShader(mesh);
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		poseStack.popPose();
	}

	private static void ribbon(BufferBuilder buffer, Matrix4f matrix, VesperBurrowClientData.Point from,
			VesperBurrowClientData.Point to, float jag, float width, int red, int green, int blue, int alpha) {
		float dx = (float) (to.x() - from.x());
		float dz = (float) (to.z() - from.z());
		float length = Mth.sqrt(dx * dx + dz * dz);
		if (length < 1.0E-4F || alpha <= 0) return;
		float sideX = -dz / length;
		float sideZ = dx / length;
		float x0 = (float) from.x();
		float z0 = (float) from.z();
		float x1 = (float) to.x() + sideX * jag;
		float z1 = (float) to.z() + sideZ * jag;
		float y0 = (float) from.y() + LIFT;
		float y1 = (float) to.y() + LIFT;
		buffer.addVertex(matrix, x0 - sideX * width, y0, z0 - sideZ * width).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, x0 + sideX * width, y0, z0 + sideZ * width).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, x1 + sideX * width, y1, z1 + sideZ * width).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, x1 - sideX * width, y1, z1 - sideZ * width).setColor(red, green, blue, alpha);
	}
}
