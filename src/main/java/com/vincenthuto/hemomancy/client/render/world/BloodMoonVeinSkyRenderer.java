package com.vincenthuto.hemomancy.client.render.world;

import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.joml.Matrix4f;

/**
 * Draws animated blood-vein tendrils radiating outward from the edge of the
 * moon during a Blood Moon.
 *
 * Each tendril is a ribbon (connected quad strip) that starts at the moon's
 * rim, wiggles outward, and fades to transparent at the tip. The ribbon
 * geometry follows the actual squiggled path so it never looks blocky.
 *
 * Called from MixinLevelRenderer BEFORE getMoonPhase(), so the poseStack is
 * already in moon-local space (Y = -100 is the moon plane).
 */
@OnlyIn(Dist.CLIENT)
public class BloodMoonVeinSkyRenderer {

	private static final int   TENDRIL_COUNT  = 14;
	private static final float MOON_PLANE_Y   = -100.0F;
	private static final float MOON_RADIUS    = 16.0F;  // slightly inside the vanilla 40×40 moon quad
	private static final int   STEPS          = 90;
	private static final float STEP_LEN       = 1.1F;
	private static final float TENDRIL_WIDTH  = 0.55F;  // half-width of ribbon
	private static final float SQUIGGLE_AMP   = 4.5F;
	private static final float SQUIGGLE_FREQ  = 0.10F;

	// Per-tendril variation, seeded so the layout is consistent between frames
	private static final float[] ANGLE_OFFSETS = new float[TENDRIL_COUNT];
	private static final float[] SPEEDS        = new float[TENDRIL_COUNT];
	private static final float[] PHASES        = new float[TENDRIL_COUNT];
	private static final int[]   LENGTHS       = new int[TENDRIL_COUNT];

	static {
		Random rand = new Random(7331L);
		for (int i = 0; i < TENDRIL_COUNT; i++) {
			ANGLE_OFFSETS[i] = (rand.nextFloat() - 0.5f) * 0.45f;
			SPEEDS[i]        = 0.7f + rand.nextFloat() * 0.8f;
			PHASES[i]        = rand.nextFloat() * Mth.TWO_PI;
			LENGTHS[i]       = 55 + rand.nextInt(36);  // 55–90 steps
		}
	}

	// Reused per-frame scratch arrays to avoid per-frame allocation
	private static final float[] PX = new float[STEPS + 1];
	private static final float[] PZ = new float[STEPS + 1];

	public static void renderInSky(PoseStack poseStack, ClientLevel level, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		float weatherFade = 1.0F - level.getRainLevel(partialTick);
		if (weatherFade <= 0.01F) return;

		RenderSystem.enableBlend();
		// Additive blend: tendrils glow/add to the sky rather than replace it.
		// Prevents overlapping roots from creating a dark ring around the moon.
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
			GlStateManager.SourceFactor.ONE,       GlStateManager.DestFactor.ZERO
		);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		float time   = (level.getGameTime() + partialTick) * 0.045F;
		Matrix4f mat = poseStack.last().pose();
		BufferBuilder buf = Tesselator.getInstance().getBuilder();
		buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		for (int i = 0; i < TENDRIL_COUNT; i++) {
			drawTendril(buf, mat, i, time, weatherFade);
		}

		BufferUploader.drawWithShader(buf.end());
		// Restore the texture shader vanilla set before calling getMoonPhase() —
		// without this the moon quad renders as a solid color square.
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		// Restore vanilla's additive blend used for sun/moon: black texels become
		// transparent so the moon texture background doesn't appear as a black square.
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
			GlStateManager.SourceFactor.ONE,       GlStateManager.DestFactor.ZERO
		);
	}

	private static void drawTendril(BufferBuilder buf, Matrix4f mat,
			int index, float time, float weatherFade) {
		// Evenly distribute tendrils around the moon rim, then nudge per-tendril
		float baseAngle = (float) (index * (Math.PI * 2.0 / TENDRIL_COUNT)) + ANGLE_OFFSETS[index];
		float cosB = Mth.cos(baseAngle);
		float sinB = Mth.sin(baseAngle);

		float startX = cosB * MOON_RADIUS;
		float startZ = sinB * MOON_RADIUS;
		float speed  = SPEEDS[index];
		float phase  = PHASES[index];
		int   length = LENGTHS[index];

		// Compute all positions along the tendril first so ribbon normals are accurate
		for (int step = 0; step <= length; step++) {
			float squiggle = SQUIGGLE_AMP * Mth.sin(SQUIGGLE_FREQ * step + time * speed + phase);
			PX[step] = startX + step * STEP_LEN * cosB - squiggle * sinB;
			PZ[step] = startZ + step * STEP_LEN * sinB + squiggle * cosB;
		}

		float pulse = 0.65f + 0.35f * Mth.sin(time * speed * 1.8f + phase);

		// Draw ribbon quad-strip
		for (int step = 0; step < length; step++) {
			float dx = PX[step + 1] - PX[step];
			float dz = PZ[step + 1] - PZ[step];
			float len = Mth.sqrt(dx * dx + dz * dz);
			if (len < 0.001f) continue;

			// Normal perpendicular to the tendril direction in the XZ plane
			float nx = (-dz / len) * TENDRIL_WIDTH;
			float nz = ( dx / len) * TENDRIL_WIDTH;

			float prog0 = (float) step       / length;
			float prog1 = (float) (step + 1) / length;

			int a0 = alpha(prog0, pulse, weatherFade);
			int a1 = alpha(prog1, pulse, weatherFade);
			int[] c0 = color(prog0, pulse);
			int[] c1 = color(prog1, pulse);

			buf.vertex(mat, PX[step]     - nx, MOON_PLANE_Y, PZ[step]     - nz).color(c0[0], c0[1], c0[2], a0).endVertex();
			buf.vertex(mat, PX[step]     + nx, MOON_PLANE_Y, PZ[step]     + nz).color(c0[0], c0[1], c0[2], a0).endVertex();
			buf.vertex(mat, PX[step + 1] + nx, MOON_PLANE_Y, PZ[step + 1] + nz).color(c1[0], c1[1], c1[2], a1).endVertex();
			buf.vertex(mat, PX[step + 1] - nx, MOON_PLANE_Y, PZ[step + 1] - nz).color(c1[0], c1[1], c1[2], a1).endVertex();
		}
	}

	private static int alpha(float progress, float pulse, float weatherFade) {
		// Opaque at the root, fade to transparent at the tip
		float fade = 1.0f - progress;
		return (int) Mth.clamp(fade * pulse * 180f * weatherFade, 0f, 200f);
	}

	private static int[] color(float progress, float pulse) {
		// Dark red at root, dimmer toward the tip
		float bright = (1.0f - progress * 0.55f) * pulse;
		return new int[] {
			(int) Mth.clamp(200 * bright, 0, 255),
			(int) Mth.clamp(12  * bright, 0, 255),
			(int) Mth.clamp(12  * bright, 0, 255)
		};
	}
}
