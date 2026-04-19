package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.common.worldevent.BloodMoonClientState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BloodMoonVeinSkyRenderer {

	private static final int TENDRIL_COUNT = 18;
	private static final int TENDRIL_SEGMENTS = 28;
	private static final float MOON_PLANE_Y = -100.0F;
	private static final float MOON_RADIUS = 15.0F;
	private static final float TENDRIL_LENGTH = 22.0F;

	public static void renderInSky(PoseStack poseStack, ClientLevel level, float partialTick) {
		if (!BloodMoonClientState.isActive()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		float weatherFade = 1.0F - level.getRainLevel(partialTick);
		if (weatherFade <= 0.01F) return;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		float time = (level.getGameTime() + partialTick) * 0.04F;
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		drawMoonShade(buffer, matrix, weatherFade);
		for (int i = 0; i < TENDRIL_COUNT; i++) {
			drawTendril(buffer, matrix, i, time, weatherFade, level.getGameTime() + partialTick);
		}

		BufferUploader.drawWithShader(buffer.end());
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
	}

	private static void drawMoonShade(BufferBuilder buffer, Matrix4f matrix, float weatherFade) {
		int outerAlpha = (int) (95.0F * weatherFade);
		int innerAlpha = (int) (165.0F * weatherFade);
		addPointQuad(buffer, matrix, 0.0F, MOON_PLANE_Y, 0.0F, MOON_RADIUS * 1.35F, 85, 0, 0, outerAlpha);
		addPointQuad(buffer, matrix, 0.0F, MOON_PLANE_Y, 0.0F, MOON_RADIUS, 185, 15, 20, innerAlpha);
	}

	private static void drawTendril(BufferBuilder buffer, Matrix4f matrix, int index, float time, float weatherFade, float gameTime) {
		float indexRatio = (float) index / (float) TENDRIL_COUNT;
		float baseAngle = indexRatio * Mth.TWO_PI + seeded(index * 11 + 3) * 0.8F;
		float startRadius = MOON_RADIUS * (0.25F + seeded(index * 13 + 7) * 0.7F);
		float startX = Mth.cos(baseAngle) * startRadius;
		float startZ = Mth.sin(baseAngle) * startRadius;
		float direction = baseAngle + (seeded(index * 17 + 5) - 0.5F) * 1.1F;
		float dirX = Mth.cos(direction);
		float dirZ = Mth.sin(direction);
		float sideX = -dirZ;
		float sideZ = dirX;

		float wobbleSpeed = 1.1F + seeded(index * 19 + 9) * 1.0F;
		float wobbleAmount = 1.3F + seeded(index * 23 + 2) * 1.7F;
		float length = TENDRIL_LENGTH * (0.65F + seeded(index * 29 + 4) * 0.65F);

		for (int step = 0; step < TENDRIL_SEGMENTS; step++) {
			float t = (float) step / (float) (TENDRIL_SEGMENTS - 1);
			float distance = t * length;
			float wave = Mth.sin(time * wobbleSpeed + step * 0.55F + index * 0.85F) * wobbleAmount * (1.0F - t * 0.55F);
			float x = startX + dirX * distance + sideX * wave;
			float z = startZ + dirZ * distance + sideZ * wave;

			float pulse = 0.7F + 0.3F * Mth.sin(gameTime * 0.06F + index * 0.5F + step * 0.25F);
			float fade = (1.0F - t * 0.65F);
			float tipBoost = 1.0F - Mth.abs(t - 0.25F) * 0.35F;

			int alpha = (int) Mth.clamp(140.0F * weatherFade * fade * pulse, 20.0F, 180.0F);
			int red = (int) Mth.clamp(170.0F * tipBoost + 60.0F * pulse, 0.0F, 255.0F);
			int green = (int) Mth.clamp(6.0F + 10.0F * pulse, 0.0F, 255.0F);
			int blue = (int) Mth.clamp(8.0F + 12.0F * pulse, 0.0F, 255.0F);
			float size = 0.52F - t * 0.30F;

			addPointQuad(buffer, matrix, x, MOON_PLANE_Y, z, size, red, green, blue, alpha);
		}
	}

	private static float seeded(int seed) {
		int h = seed * 0x45d9f3b;
		h = (h ^ (h >>> 16)) * 0x45d9f3b;
		h = h ^ (h >>> 16);
		return (h & 0x7fffffff) / (float) Integer.MAX_VALUE;
	}

	private static void addPointQuad(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z,
			float halfSize, int r, int g, int b, int a) {
		buffer.vertex(matrix, x - halfSize, y, z - halfSize).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, x + halfSize, y, z - halfSize).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, x + halfSize, y, z + halfSize).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, x - halfSize, y, z + halfSize).color(r, g, b, a).endVertex();
	}
}
