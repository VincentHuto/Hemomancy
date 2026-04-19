package com.vincenthuto.hemomancy.client.render.world;

import java.util.Random;

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

/**
 * Sky renderer for animated blood-vein tendrils radiating from the moon's
 * position when a Blood Moon is active.
 *
 * <p>Uses the same pixel-art squiggle algorithm as
 * {@link com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen}'s
 * blood theme background, but tendrils are anchored to the top-centre of the
 * screen and fan outward in a downward arc, giving the impression of veins
 * growing from the moon into the sky.
 */
@OnlyIn(Dist.CLIENT)
public class BloodMoonVeinSkyRenderer {

	private static final int TENDRIL_COUNT = 22;
	private static final float MOON_PLANE_Y = -100.0F;
	private static final float STEP_LENGTH = 0.38F;
	private static final float WORLD_SCALE = 0.18F;
	private static final float POINT_SIZE = 0.24F;

	/**
	 * Per-tendril parameters (static, seeded once):
	 * [0] startDX  — X offset from moon centre (pixels)
	 * [1] startDY  — Y offset from moon centre (pixels)
	 * [2] baseAngle — initial direction (radians; 0=right, PI/2=down)
	 * [3] speed     — animation speed multiplier
	 * [4] amplitude — perpendicular squiggle width
	 * [5] frequency — squiggle spatial frequency
	 * [6] length    — number of steps
	 * [7] reserved
	 * [8] brightness — colour brightness multiplier [0,1]
	 */
	private static final float[][] TENDRIL_PARAMS = new float[TENDRIL_COUNT][9];

	static {
		Random rand = new Random(7331L);
		for (int i = 0; i < TENDRIL_COUNT; i++) {
			// Start positions clustered within the moon disc (~16px radius)
			TENDRIL_PARAMS[i][0] = (rand.nextFloat() - 0.5f) * 32f;
			TENDRIL_PARAMS[i][1] = (rand.nextFloat() - 0.5f) * 32f;
			TENDRIL_PARAMS[i][2] = (float) (rand.nextFloat() * Math.PI * 2.0);
			TENDRIL_PARAMS[i][3] = 0.12f + rand.nextFloat() * 0.35f;
			TENDRIL_PARAMS[i][4] = 2.5f + rand.nextFloat() * 8f;
			TENDRIL_PARAMS[i][5] = 0.035f + rand.nextFloat() * 0.055f;
			TENDRIL_PARAMS[i][6] = 90 + rand.nextInt(130);
			TENDRIL_PARAMS[i][7] = 1f;
			TENDRIL_PARAMS[i][8] = 0.35f + rand.nextFloat() * 0.65f;
		}
	}

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

		float time = (level.getGameTime() + partialTick) * 0.05F;
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		for (int i = 0; i < TENDRIL_COUNT; i++) {
			drawTendril(buffer, matrix, i, time, weatherFade);
		}

		BufferUploader.drawWithShader(buffer.end());
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
	}

	private static void drawTendril(BufferBuilder buffer, Matrix4f matrix, int index, float time, float weatherFade) {
		float[] p = TENDRIL_PARAMS[index];
		float startX = p[0] * WORLD_SCALE;
		float startZ = p[1] * WORLD_SCALE;
		float baseAngle = p[2];
		float speed    = p[3];
		float amplitude = p[4];
		float frequency = p[5];
		int   length   = (int) p[6];
		float brightness = p[8];

		float angleDrift = baseAngle + 0.10f * Mth.sin(time * speed * 0.25f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);
		float timeOffset = time * speed * 1.6f;

		int baseRed   = (int) (30 + 55 * brightness);
		int baseGreen = (int) (1  +  5 * brightness);
		int baseBlue  = (int) (3  +  5 * brightness);

		for (int step = 0; step < length; step++) {
			float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
			float micro    = (amplitude * 0.28f)
					* Mth.sin(frequency * 2.4f * step + timeOffset * 1.35f + index);
			float disp = squiggle + micro;

			float x = startX + (step * cosA * STEP_LENGTH - disp * sinA * WORLD_SCALE);
			float z = startZ + (step * sinA * STEP_LENGTH + disp * cosA * WORLD_SCALE);

			float tipFade = 1f;
			if (step < 12) tipFade = step / 12f;
			else if (step > length - 12) tipFade = (length - step) / 12f;

			float pulse = 0.65f + 0.35f * Mth.sin(time * 1.3f + index * 0.55f + step * 0.018f);

			int a = (int) (Mth.clamp(tipFade * pulse * 95f, 4f, 100f) * weatherFade);
			int r = (int) Mth.clamp(baseRed   * pulse,        0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue  * pulse * 0.4f, 0, 255);

			addPointQuad(buffer, matrix, x, MOON_PLANE_Y, z, POINT_SIZE, r, g, b, a);
		}
	}

	private static void addPointQuad(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z,
			float halfSize, int r, int g, int b, int a) {
		buffer.vertex(matrix, x - halfSize, y, z - halfSize).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, x + halfSize, y, z - halfSize).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, x + halfSize, y, z + halfSize).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, x - halfSize, y, z + halfSize).color(r, g, b, a).endVertex();
	}
}
