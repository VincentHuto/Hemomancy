package com.vincenthuto.hemomancy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumBloodFlow;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.vascular.VascularSystemProvider;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A standalone screen that displays the player's Vascular System status.
 * Each body section is rendered as a semi-transparent rectangle overlaid
 * on a player model, colored from green (100%) → yellow → red → black (0%/dead).
 */
public class VascularStatusScreen extends Screen {

	private static final ResourceLocation BACKGROUND = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/vascular_border.png");

	private static final int GUI_WIDTH = 190;
	private static final int GUI_HEIGHT = 254;

	/** Number of animated vein tendrils swimming across the background. */
	private static final int VEIN_COUNT = 28;

	/**
	 * Stores per-vein parameters seeded once at init:
	 * [i][0] = startX ratio (0-1), [i][1] = startY ratio (0-1),
	 * [i][2] = base angle (radians), [i][3] = speed multiplier,
	 * [i][4] = amplitude, [i][5] = frequency, [i][6] = length (steps),
	 * [i][7] = thickness (1-3), [i][8] = red tint (0-1 extra brightness)
	 */
	private float[][] veinParams;

	public VascularStatusScreen() {
		super(Component.literal("Vascular Status"));
	}

	public static void openScreen() {
		Minecraft.getInstance().setScreen(new VascularStatusScreen());
	}

	@Override
	protected void init() {
		super.init();
		// Seed vein parameters deterministically so they stay stable while the screen is open
		Random rand = new Random(42L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();                      // startX ratio
			veinParams[i][1] = rand.nextFloat();                      // startY ratio
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2); // base angle
			veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;       // speed mult
			veinParams[i][4] = 8f + rand.nextFloat() * 18f;          // amplitude
			veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;     // frequency
			veinParams[i][6] = 60 + rand.nextInt(120);                // length (steps)
			veinParams[i][7] = 1 + rand.nextInt(3);                   // thickness
			veinParams[i][8] = rand.nextFloat();                      // red tint brightness
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Default dim overlay for the area outside the GUI
		this.renderBackground(graphics);

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		int guiLeft = centerX - GUI_WIDTH / 2;
		int guiTop = centerY - GUI_HEIGHT / 2;

		// Animated vein background clipped to the GUI bounds
		renderVeinBackground(graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

		// Programmatic dark-red border frame on top of the vein background
		drawBorder(graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

		// Title
		graphics.drawCenteredString(this.font, this.title, centerX, guiTop + 6, 0xFFCC3344);

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		// Render vascular body diagram and labels
		player.getCapability(VascularSystemProvider.VASCULAR_CAPA).ifPresent(vascular -> {
			renderBodyPartOverlays(graphics, vascular, centerX, centerY);
			renderSectionLabels(graphics, vascular, centerX, centerY, mouseX, mouseY);
		});
	}

	// ───── Programmatic Dark-Red Border ─────

	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		int outer = 0xFF330808;
		gfx.fill(x, y, x + w, y + 1, outer);
		gfx.fill(x, y + h - 1, x + w, y + h, outer);
		gfx.fill(x, y, x + 1, y + h, outer);
		gfx.fill(x + w - 1, y, x + w, y + h, outer);

		int inner = 0xFF220606;
		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, inner);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, inner);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, inner);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, inner);
	}

	// ───── Procedural Animated Vein Background ─────

	/**
	 * Renders a dark red/black background covered in swimming, squiggling vein tendrils.
	 * Each vein is a sine-wave curve that drifts over time, giving an organic pulsing feel.
	 * All rendering is clipped to the given GUI bounds.
	 */
	private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
		// Scissor clip so nothing bleeds outside the GUI panel
		graphics.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: solid near-black base filling GUI area
		graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF0A0204);

		// Layer 2: subtle dark-red radial glow in the center of the GUI
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius; // 1.0 at edge, 0.0 at center
			int alpha = (int) (35 * (1f - t));     // brighter toward center
			int red = (int) (40 * (1f - t));
			int color = (alpha << 24) | (red << 16);
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		// Layer 3: animated vein tendrils
		float time = (System.nanoTime() / 1_000_000_000f); // seconds since epoch, smooth

		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(graphics, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: very subtle noise-like speckles for texture (static, cheap)
		Random speckRand = new Random(12345L);
		for (int s = 0; s < 120; s++) {
			int sx = gx + speckRand.nextInt(gw);
			int sy = gy + speckRand.nextInt(gh);
			int sr = 10 + speckRand.nextInt(20);
			int sg = speckRand.nextInt(6);
			int sa = 15 + speckRand.nextInt(25);
			graphics.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
		}

		RenderSystem.disableBlend();
		graphics.disableScissor();
	}

	/**
	 * Draws a single animated vein tendril as a squiggling curve within the GUI bounds.
	 * The curve is built from small filled rectangles placed along a parametric path:
	 *   x(t) = startX + t * cos(angle) - amplitude * sin(freq * t + timeOffset) * sin(angle)
	 *   y(t) = startY + t * sin(angle) + amplitude * sin(freq * t + timeOffset) * cos(angle)
	 * This makes a sine-wave that "swims" perpendicular to its travel direction.
	 */
	private void drawVeinTendril(GuiGraphics graphics, int index, float time,
								 int gx, int gy, int gw, int gh) {
		float[] p = veinParams[index];
		float startX = gx + p[0] * gw;
		float startY = gy + p[1] * gh;
		float baseAngle = p[2];
		float speed = p[3];
		float amplitude = p[4];
		float frequency = p[5];
		int length = (int) p[6];
		int thickness = (int) p[7];
		float brightness = p[8];

		// The whole vein drifts slowly: its angle oscillates over time
		float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);

		// Time offset makes the wave swim along the tendril
		float timeOffset = time * speed * 2.0f;

		// Color: dark red with varying brightness, semi-transparent
		int baseRed = (int) (40 + 50 * brightness);
		int baseGreen = (int) (2 + 8 * brightness);
		int baseBlue = (int) (5 + 5 * brightness);

		for (int step = 0; step < length; step++) {
			float t = step;

			// Lateral displacement (the squiggle)
			float squiggle = amplitude * Mth.sin(frequency * t + timeOffset);

			// A secondary smaller squiggle for organic feel
			float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * t + timeOffset * 1.4f + index);

			float displacement = squiggle + microSquiggle;

			// Position along the main travel direction + perpendicular displacement
			float px = startX + t * cosA * 1.5f - displacement * sinA;
			float py = startY + t * sinA * 1.5f + displacement * cosA;

			int ix = (int) px;
			int iy = (int) py;

			// Skip if outside GUI bounds (scissor handles clipping, but skip for perf)
			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) {
				continue;
			}

			// Fade at the tips of the tendril
			float tipFade = 1f;
			if (step < 10) tipFade = step / 10f;
			else if (step > length - 10) tipFade = (length - step) / 10f;

			// Pulsing brightness over time
			float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);

			int alpha = (int) (Mth.clamp(tipFade * pulse * 180, 20, 200));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

			int color = (alpha << 24) | (r << 16) | (g << 8) | b;
			graphics.fill(ix, iy, ix + thickness, iy + thickness, color);
		}
	}

	/**
	 * Renders a human body silhouette made of semi-transparent colored rectangles.
	 * Each rectangle represents a vein section, colored green → yellow → red → black
	 * based on that section's health. Sized to be clearly readable without a 3D model.
	 *
	 * Layout (approximate pixel positions, centered on cx/cy):
	 * <pre>
	 *         [  HEAD  ]
	 *    [RA] [HRT/BODY] [LA]
	 *         [  BODY  ]
	 *      [R LEG][L LEG]
	 * </pre>
	 */
	private void renderBodyPartOverlays(GuiGraphics graphics, IVascularSystem vascular, int cx, int cy) {
		// The body diagram is centered vertically a bit above screen center
		int bodyTop = cy - 65;

		// HEAD — 20 wide, 20 tall
		drawSectionRect(graphics, vascular, EnumVeinSections.HEAD,
				cx - 10, bodyTop, 20, 20);

		// HEART — upper chest area, 24 wide, 14 tall
		drawSectionRect(graphics, vascular, EnumVeinSections.HEART,
				cx - 12, bodyTop + 20, 24, 14);

		// BODY — lower torso, 24 wide, 22 tall
		drawSectionRect(graphics, vascular, EnumVeinSections.BODY,
				cx - 12, bodyTop + 34, 24, 22);

		// RIGHT ARM (player's right = screen left) — 10 wide, 36 tall
		drawSectionRect(graphics, vascular, EnumVeinSections.RIGHTARM,
				cx - 22, bodyTop + 20, 10, 36);

		// LEFT ARM (player's left = screen right) — 10 wide, 36 tall
		drawSectionRect(graphics, vascular, EnumVeinSections.LEFTARM,
				cx + 12, bodyTop + 20, 10, 36);

		// RIGHT LEG (player's right = screen left) — 11 wide, 34 tall
		drawSectionRect(graphics, vascular, EnumVeinSections.RIGHTLEG,
				cx - 12, bodyTop + 56, 11, 34);

		// LEFT LEG (player's left = screen right) — 11 wide, 34 tall
		drawSectionRect(graphics, vascular, EnumVeinSections.LEFTLEG,
				cx + 1, bodyTop + 56, 11, 34);

		// Draw 1px dark outline around the whole silhouette for definition
		drawBodyOutline(graphics, cx, bodyTop);
	}

	/**
	 * Draws a thin dark outline around the body silhouette to give it shape definition.
	 */
	private void drawBodyOutline(GuiGraphics graphics, int cx, int bodyTop) {
		int outline = 0xAA221111;

		// Head outline
		drawRectOutline(graphics, cx - 10, bodyTop, 20, 20, outline);
		// Heart/upper torso
		drawRectOutline(graphics, cx - 12, bodyTop + 20, 24, 14, outline);
		// Lower body
		drawRectOutline(graphics, cx - 12, bodyTop + 34, 24, 22, outline);
		// Arms
		drawRectOutline(graphics, cx - 22, bodyTop + 20, 10, 36, outline);
		drawRectOutline(graphics, cx + 12, bodyTop + 20, 10, 36, outline);
		// Legs
		drawRectOutline(graphics, cx - 12, bodyTop + 56, 11, 34, outline);
		drawRectOutline(graphics, cx + 1, bodyTop + 56, 11, 34, outline);
	}

	/**
	 * Draws a 1px outline around a rectangle.
	 */
	private void drawRectOutline(GuiGraphics graphics, int x, int y, int w, int h, int color) {
		graphics.fill(x, y, x + w, y + 1, color);         // top
		graphics.fill(x, y + h - 1, x + w, y + h, color); // bottom
		graphics.fill(x, y, x + 1, y + h, color);         // left
		graphics.fill(x + w - 1, y, x + w, y + h, color); // right
	}

	/**
	 * Draws a single semi-transparent rectangle for a vein section.
	 * Color transitions: 100% = bright green → 50% = yellow → 15% = red → 0% = black
	 */
	private void drawSectionRect(GuiGraphics graphics, IVascularSystem vascular,
								 EnumVeinSections section, int x, int y, int w, int h) {
		float health = vascular.getHealthBySection(section);
		health = Math.max(0f, Math.min(100f, health));

		int color = getColorForHealth(health);

		// Draw filled rectangle with alpha blending
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		graphics.fill(x, y, x + w, y + h, color);
		RenderSystem.disableBlend();
	}

	/**
	 * Maps health (0-100) to an ARGB color.
	 * 100 = bright green (semi-transparent)
	 * 50  = yellow
	 * 15  = red
	 * 0   = black (more opaque)
	 */
	private int getColorForHealth(float health) {
		float t = health / 100f; // 0.0 to 1.0

		int r, g, b;
		int alpha;

		if (t > 0.5f) {
			// Green → Yellow (100% → 50%)
			float localT = (t - 0.5f) / 0.5f; // 1.0 at 100%, 0.0 at 50%
			r = (int) (255 * (1f - localT));    // 0 at 100%, 255 at 50%
			g = 255;                             // always full green
			b = 0;
			alpha = (int) (100 + 55 * (1f - localT)); // 100 at 100%, 155 at 50%
		} else if (t > 0.15f) {
			// Yellow → Red (50% → 15%)
			float localT = (t - 0.15f) / 0.35f; // 1.0 at 50%, 0.0 at 15%
			r = 255;
			g = (int) (255 * localT);            // 255 at 50%, 0 at 15%
			b = 0;
			alpha = (int) (155 + 50 * (1f - localT)); // 155 at 50%, 205 at 15%
		} else {
			// Red → Black (15% → 0%)
			float localT = t / 0.15f;           // 1.0 at 15%, 0.0 at 0%
			r = (int) (255 * localT);
			g = 0;
			b = 0;
			alpha = (int) (205 + 50 * (1f - localT)); // 205 at 15%, 255 at 0%
		}

		return (alpha << 24) | (r << 16) | (g << 8) | b;
	}

	/**
	 * Renders labels on either side of the body diagram showing section name, health %, and flow state.
	 * Labels are positioned to the left/right of their corresponding body parts.
	 */
	private void renderSectionLabels(GuiGraphics graphics, IVascularSystem vascular,
									 int cx, int cy, int mouseX, int mouseY) {
		int bodyTop = cy - 65;
		int leftLabelX = cx - 80;   // labels for right-side body parts (screen left)
		int rightLabelX = cx + 28;  // labels for left-side body parts (screen right)

		// Head — centered above
		drawLabel(graphics, vascular, EnumVeinSections.HEAD, cx - 20, bodyTop - 16, mouseX, mouseY);

		// Heart — to the right
		drawLabel(graphics, vascular, EnumVeinSections.HEART, rightLabelX, bodyTop + 22, mouseX, mouseY);

		// Body — to the left
		drawLabel(graphics, vascular, EnumVeinSections.BODY, leftLabelX, bodyTop + 38, mouseX, mouseY);

		// Right arm — to the left
		drawLabel(graphics, vascular, EnumVeinSections.RIGHTARM, leftLabelX, bodyTop + 22, mouseX, mouseY);

		// Left arm — to the right
		drawLabel(graphics, vascular, EnumVeinSections.LEFTARM, rightLabelX, bodyTop + 38, mouseX, mouseY);

		// Right leg — to the left
		drawLabel(graphics, vascular, EnumVeinSections.RIGHTLEG, leftLabelX, bodyTop + 66, mouseX, mouseY);

		// Left leg — to the right
		drawLabel(graphics, vascular, EnumVeinSections.LEFTLEG, rightLabelX, bodyTop + 66, mouseX, mouseY);
	}

	private void drawLabel(GuiGraphics graphics, IVascularSystem vascular,
						   EnumVeinSections section, int x, int y, int mouseX, int mouseY) {
		float health = Math.max(0f, Math.min(100f, vascular.getHealthBySection(section)));
		EnumBloodFlow flow = vascular.getBloodFlowBySection(section);
		String name = HLTextUtils.toProperCase(section.toString());
		String percent = String.format("%.0f%%", health);
		String flowName = HLTextUtils.toProperCase(flow.toString());

		int nameColor = getTextColorForHealth(health);

		// Section name
		graphics.drawString(this.font, name, x, y, nameColor, true);
		// Percentage
		graphics.drawString(this.font, percent, x, y + 10, nameColor, true);

		// Tooltip on hover over label area
		int labelWidth = Math.max(this.font.width(name), this.font.width(percent));
		if (mouseX >= x && mouseX <= x + labelWidth && mouseY >= y && mouseY <= y + 20) {
			List<Component> tooltip = new ArrayList<>();
			tooltip.add(Component.literal(name).withStyle(s -> s.withColor(nameColor)));
			tooltip.add(Component.literal("Health: " + String.format("%.1f", health) + " / 100.0")
					.withStyle(s -> s.withColor(0xAAAAAA)));
			tooltip.add(Component.literal("Flow: " + flowName)
					.withStyle(s -> s.withColor(getFlowColor(flow))));
			graphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
		}
	}

	/**
	 * Returns a solid text color based on health percentage.
	 */
	private int getTextColorForHealth(float health) {
		if (health >= 75) return 0x55FF55;  // Green
		if (health >= 50) return 0xFFFF55;  // Yellow
		if (health >= 15) return 0xFF5555;  // Red
		return 0x555555;                     // Dark gray / black
	}

	/**
	 * Returns a color for the flow state name.
	 */
	private int getFlowColor(EnumBloodFlow flow) {
		return switch (flow) {
			case RAGING -> 0x55FF55;
			case FLOWING -> 0xAAFF55;
			case STABLE -> 0xFFFF55;
			case VARICOSE -> 0xFFAA00;
			case ClOTTED -> 0xFF5555;
			case DEAD -> 0x555555;
		};
	}
}
