package com.vincenthuto.hemomancy.client.screen.overlay;

import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.worldevent.BloodMoonClientState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * HUD overlay that renders animated blood-vein tendrils radiating from the
 * moon's approximate screen position when a Blood Moon is active.
 *
 * <p>Uses the same pixel-art squiggle algorithm as {@link DialogueScreen}'s
 * blood theme background, but tendrils are anchored to the top-centre of the
 * screen and fan outward in a downward arc, giving the impression of veins
 * growing from the moon into the sky.
 */
@OnlyIn(Dist.CLIENT)
public class BloodMoonVeinOverlay {

	public static BloodMoonVeinOverlay instance;

	private static final int TENDRIL_COUNT = 22;

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
			// Angles: full circle so each tendril picks a direction; those going above
			// the moon just leave the screen immediately, naturally culling them.
			TENDRIL_PARAMS[i][2] = (float) (rand.nextFloat() * Math.PI * 2.0);
			TENDRIL_PARAMS[i][3] = 0.12f + rand.nextFloat() * 0.35f;
			TENDRIL_PARAMS[i][4] = 2.5f + rand.nextFloat() * 8f;
			TENDRIL_PARAMS[i][5] = 0.035f + rand.nextFloat() * 0.055f;
			TENDRIL_PARAMS[i][6] = 90 + rand.nextInt(130);
			TENDRIL_PARAMS[i][7] = 1f;
			TENDRIL_PARAMS[i][8] = 0.35f + rand.nextFloat() * 0.65f;
		}
	}

	public void renderHUD(GuiGraphics gfx, int screenWidth, int screenHeight, float partialTick) {
		if (!BloodMoonClientState.isActive()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		if (mc.player == null) return;

		// Moon world-space direction derived from the sky's celestial angle.
		// The sky is rotated by Ry(-90°) * Rx(skyAngle*360°); the moon sits at
		// (0,-1,0) in that local frame, which maps to (sin α, -cos α, 0) in world
		// space where α = sunAngle (radians).
		float alpha = mc.level.getSunAngle(partialTick);
		float moonDirX = Mth.sin(alpha);
		float moonDirY = -Mth.cos(alpha);
		// moonDirZ = 0 — celestial plane runs along world X/Y

		// Build camera basis from player yaw/pitch directly, bypassing camera bob.
		// Bob is a post-rotation applied to the view matrix after yaw/pitch; using the
		// raw player angles produces a stable anchor point that does not oscillate.
		float yaw   = Mth.lerp(partialTick, mc.player.yRotO, mc.player.getYRot());
		float pitch = Mth.lerp(partialTick, mc.player.xRotO, mc.player.getXRot());
		float sy = Mth.sin(yaw   * (float) (Math.PI / 180.0));
		float cy = Mth.cos(yaw   * (float) (Math.PI / 180.0));
		float sp = Mth.sin(pitch * (float) (Math.PI / 180.0));
		float cp = Mth.cos(pitch * (float) (Math.PI / 180.0));

		// moonDirZ = 0, so Z components of each basis vector vanish from dot products.
		// forward = (-sy·cp, -sp, …)  right = (-cy, 0, …)  up = (-sy·sp, cp, …)
		float dotForward = -moonDirX * sy * cp - moonDirY * sp;
		float dotRight   = -moonDirX * cy;
		float dotUp      = -moonDirX * sy * sp + moonDirY * cp;

		// Moon is behind or beside the camera — nothing to draw.
		if (dotForward <= 0.01f) return;

		// Project to GUI-scaled screen coords using the vertical FOV.
		float tanHalfFov = (float) Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
		float scale = (screenHeight / 2f) / tanHalfFov;
		int moonX = (int) (screenWidth  / 2f + (dotRight / dotForward) * scale);
		int moonY = (int) (screenHeight / 2f - (dotUp    / dotForward) * scale);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		float time = System.nanoTime() / 1_000_000_000f;

		// Soft radial glow around the moon — crimson halo.
		drawMoonGlow(gfx, moonX, moonY);

		// Animated vein tendrils anchored to the moon's true screen position.
		for (int i = 0; i < TENDRIL_COUNT; i++) {
			drawTendril(gfx, i, time, moonX, moonY, screenWidth, screenHeight);
		}

		RenderSystem.disableBlend();
	}

	private void drawMoonGlow(GuiGraphics gfx, int cx, int cy) {
		int maxRadius = 55;
		for (int ring = maxRadius; ring > 0; ring -= 3) {
			float t = (float) ring / maxRadius;
			float intensity = (1f - t) * (1f - t);
			int alpha = (int) (18 * intensity);
			int red   = (int) (70 * intensity);
			int green = (int) (3  * intensity);
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring,
					(alpha << 24) | (red << 16) | (green << 8));
		}
	}

	private void drawTendril(GuiGraphics gfx, int index, float time,
							  int moonX, int moonY, int screenW, int screenH) {
		float[] p = TENDRIL_PARAMS[index];
		float startX = moonX + p[0];
		float startY = moonY + p[1];
		float baseAngle = p[2];
		float speed    = p[3];
		float amplitude = p[4];
		float frequency = p[5];
		int   length   = (int) p[6];
		float brightness = p[8];

		// Slow angular drift to make each tendril lazily rotate over time.
		float angleDrift = baseAngle + 0.10f * Mth.sin(time * speed * 0.25f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);
		float timeOffset = time * speed * 1.6f;

		int baseRed   = (int) (30 + 55 * brightness);
		int baseGreen = (int) (1  +  5 * brightness);
		int baseBlue  = (int) (3  +  5 * brightness);

		for (int step = 0; step < length; step++) {
			// Two-frequency squiggle: main wave + fine micro-detail.
			float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
			float micro    = (amplitude * 0.28f)
					* Mth.sin(frequency * 2.4f * step + timeOffset * 1.35f + index);
			float disp = squiggle + micro;

			float px = startX + step * cosA * 1.7f - disp * sinA;
			float py = startY + step * sinA * 1.7f + disp * cosA;

			int ix = (int) px;
			int iy = (int) py;
			if (ix < 0 || ix >= screenW || iy < 0 || iy >= screenH) continue;

			// Fade in at base, fade out at tip.
			float tipFade = 1f;
			if (step < 12) tipFade = step / 12f;
			else if (step > length - 12) tipFade = (length - step) / 12f;

			// Breathing pulse.
			float pulse = 0.65f + 0.35f * Mth.sin(time * 1.3f + index * 0.55f + step * 0.018f);

			int a = (int) Mth.clamp(tipFade * pulse * 95f, 4f, 100f);
			int r = (int) Mth.clamp(baseRed   * pulse,        0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue  * pulse * 0.4f, 0, 255);

			gfx.fill(ix, iy, ix + 1, iy + 1, (a << 24) | (r << 16) | (g << 8) | b);
		}
	}
}
