package com.vincenthuto.hemomancy.client.screen.item.living;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.morphling.PacketUpdateLivingStaffMorph;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Morph-selection overlay opened by right-clicking the Morphling Jar.
 * Each stored morphling floats and swims around inside a jar-shaped region.
 * Hover to see a glow + tooltip.  Click to equip it on the player.
 * The currently-equipped morphling gets a gold glow + star.
 */
@OnlyIn(Dist.CLIENT)
public class MorphlingJarViewerScreen extends Screen {

	// ── Static open helpers (always create a fresh instance) ─────────────────────

	public static void openScreenViaItem() {
		openScreen();
	}

	public static void openScreen() {
		Minecraft.getInstance().setScreen(new MorphlingJarViewerScreen());
	}

	// ── Layout constants ─────────────────────────────────────────────────────────

	private static final int JAR_W = 120;
	private static final int JAR_H = 90;
	private static final int ICON  = 16;
	private static final int HIT_R = 18;

	/** Number of animated vein tendrils swimming across the jar background. */
	private static final int VEIN_COUNT = 22;

	/** Number of small bubbles floating upward through the background. */
	private static final int BUBBLE_COUNT = 14;

	// ── Pixel-outline cache (lazily built per item) ──────────────────────────────

	private static final Map<Item, boolean[][]> PIXEL_MASK_CACHE = new HashMap<>();

	/**
	 * Gets or builds a 16x16 boolean mask where true = non-transparent pixel
	 * by reading the item's baked model sprite.
	 */
	private static boolean[][] getPixelMask(ItemStack stack) {
		Item item = stack.getItem();
		if (PIXEL_MASK_CACHE.containsKey(item)) {
			return PIXEL_MASK_CACHE.get(item);
		}
		boolean[][] mask = new boolean[16][16];
		boolean anyOpaque = false;
		try {
			Minecraft mc = Minecraft.getInstance();
			BakedModel model = mc.getItemRenderer().getModel(stack, null, null, 0);
			TextureAtlasSprite sprite = model.getParticleIcon();
			int spriteW = sprite.contents().width();
			int spriteH = sprite.contents().height();
			for (int px = 0; px < 16; px++) {
				for (int py = 0; py < 16; py++) {
					int sampleX = Math.min((px * spriteW) / 16, spriteW - 1);
					int sampleY = Math.min((py * spriteH) / 16, spriteH - 1);
					int pixel = sprite.getPixelRGBA(0, sampleX, sampleY);
					// getPixelRGBA returns ABGR; alpha in bits 24-31
					// but also check bits 0-7 in case format differs
					int alphaHigh = (pixel >> 24) & 0xFF;
					int alphaLow = pixel & 0xFF;
					boolean opaque = alphaHigh > 10 || (alphaHigh == 0 && alphaLow > 10 && pixel != 0);
					mask[px][py] = opaque;
					if (opaque) anyOpaque = true;
				}
			}
		} catch (Exception e) {
			anyOpaque = false;
		}
		// If nothing was detected as opaque, the sprite read probably failed — fill all
		if (!anyOpaque) {
			for (int px = 0; px < 16; px++)
				for (int py = 0; py < 16; py++)
					mask[px][py] = true;
		}
		PIXEL_MASK_CACHE.put(item, mask);
		return mask;
	}

	/**
	 * Draws a colored outline just outside the non-transparent pixels of the item.
	 * For each transparent pixel that has at least one opaque 8-neighbor, draws a colored dot.
	 * Also draws on out-of-bounds neighbors (the 1px ring around the 16x16 area).
	 */
	private static void drawPixelOutline(GuiGraphics g, int screenX, int screenY, boolean[][] mask, int color) {
		// Check the 18x18 area (1px border around the 16x16 icon)
		for (int ox = -1; ox <= 16; ox++) {
			for (int oy = -1; oy <= 16; oy++) {
				// This position must be transparent (or outside the icon)
				boolean thisOpaque = (ox >= 0 && ox < 16 && oy >= 0 && oy < 16) && mask[ox][oy];
				if (thisOpaque) continue;

				// Check if any 8-neighbor is opaque
				boolean adjacentToOpaque = false;
				for (int dx = -1; dx <= 1 && !adjacentToOpaque; dx++) {
					for (int dy = -1; dy <= 1 && !adjacentToOpaque; dy++) {
						if (dx == 0 && dy == 0) continue;
						int nx = ox + dx;
						int ny = oy + dy;
						if (nx >= 0 && nx < 16 && ny >= 0 && ny < 16 && mask[nx][ny]) {
							adjacentToOpaque = true;
						}
					}
				}
				if (adjacentToOpaque) {
					g.fill(screenX + ox, screenY + oy, screenX + ox + 1, screenY + oy + 1, color);
				}
			}
		}
	}

	// ── Per-morphling swim parameters (set once in init) ─────────────────────────

	private float[] speedX;
	private float[] speedY;
	private float[] phaseX;
	private float[] phaseY;

	// ── State ────────────────────────────────────────────────────────────────────

	private MorphlingJarItemHandler jarHandler;
	private int slotCount;
	private int activeIndex = -1;
	private int jarLeft;
	private int jarTop;
	private int hoveredIndex = -1;
	private long openTick;

	/** Per-vein parameters for the animated background tendrils. */
	private float[][] veinParams;

	/** Per-bubble parameters: [i][0]=xRatio, [1]=speed, [2]=wobbleAmp, [3]=wobbleFreq, [4]=radius, [5]=phase */
	private float[][] bubbleParams;

	// ── Constructor ──────────────────────────────────────────────────────────────

	public MorphlingJarViewerScreen() {
		super(Component.translatable("hemomancy.screen.morphling_viewer"));
	}

	// ── Lifecycle ────────────────────────────────────────────────────────────────

	@Override
	protected void init() {
		super.init();

		Minecraft mc = Minecraft.getInstance();
		Player player = HLClientUtils.getClientPlayer();
		if (player == null || mc.level == null) {
			onClose();
			return;
		}

		openTick = mc.level.getGameTime();

		// Find the jar (inventory first, then scar slot 7)
		ItemStack jarStack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
		if (jarStack.isEmpty()) {
			jarStack = HemoCapabilityAccess.getScars(player)
					.map(r -> r.getStackInSlot(7))
					.filter(s -> s.getItem() instanceof ItemMorphlingJar)
					.orElse(ItemStack.EMPTY);
		}
		IItemHandler raw = jarStack.getCapability(Capabilities.ItemHandler.ITEM);
		if (!(raw instanceof MorphlingJarItemHandler handler)) {
			onClose();
			return;
		}
		jarHandler = handler;
		jarHandler.load();
		slotCount = jarHandler.getSlots();

		// Discover which jar slot matches the player's currently equipped morphling
		activeIndex = -1;
		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
			ItemStack equipped = cap.getEquippedMorphling();
			if (!equipped.isEmpty()) {
				for (int i = 0; i < slotCount; i++) {
					if (ItemStack.isSameItemSameTags(jarHandler.getStackInSlot(i), equipped)) {
						activeIndex = i;
						break;
					}
				}
			}
		});

		// Centre the swim area
		jarLeft = (width - JAR_W) / 2;
		jarTop  = (height - JAR_H) / 2;

		// Per-morphling orbit parameters
		speedX = new float[slotCount];
		speedY = new float[slotCount];
		phaseX = new float[slotCount];
		phaseY = new float[slotCount];
		for (int i = 0; i < slotCount; i++) {
			speedX[i] = 0.4f + i * 0.15f;
			speedY[i] = 0.3f + i * 0.12f;
			phaseX[i] = i * 2.399f;
			phaseY[i] = i * 3.883f;
		}

		// Seed vein background tendrils
		Random rand = new Random(71L);
		veinParams = new float[VEIN_COUNT][10];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();                          // startX ratio
			veinParams[i][1] = rand.nextFloat();                          // startY ratio
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2); // base angle
			veinParams[i][3] = 0.4f + rand.nextFloat() * 0.8f;           // speed mult
			veinParams[i][4] = 14f + rand.nextFloat() * 28f;             // amplitude (high = wide swirls)
			veinParams[i][5] = 0.08f + rand.nextFloat() * 0.16f;         // frequency (high = tight curves)
			veinParams[i][6] = 70 + rand.nextInt(120);                    // length
			veinParams[i][7] = 1 + rand.nextInt(2);                       // thickness
			veinParams[i][8] = rand.nextFloat();                           // brightness
			veinParams[i][9] = (rand.nextFloat() - 0.5f) * 0.06f;        // curvature — how much the heading rotates per step
		}

		// Seed bubble parameters
		bubbleParams = new float[BUBBLE_COUNT][6];
		for (int i = 0; i < BUBBLE_COUNT; i++) {
			bubbleParams[i][0] = rand.nextFloat();                   // x position ratio (0-1 across panel width)
			bubbleParams[i][1] = 0.015f + rand.nextFloat() * 0.035f; // rise speed (fraction of height per second)
			bubbleParams[i][2] = 2f + rand.nextFloat() * 4f;         // horizontal wobble amplitude (pixels)
			bubbleParams[i][3] = 1.5f + rand.nextFloat() * 2.5f;     // wobble frequency
			bubbleParams[i][4] = 1f + rand.nextFloat() * 2f;         // radius (1-3 pixels)
			bubbleParams[i][5] = rand.nextFloat();                    // time phase offset (0-1)
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	// ── Animation helpers ────────────────────────────────────────────────────────

	private float getAnimTime(float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return 0f;
		long elapsed = mc.level.getGameTime() - openTick;
		return (elapsed + partialTicks) / 20.0f;
	}

	private int[] getMorphPos(int i, float time) {
		float sx = Mth.sin(time * speedX[i] + phaseX[i]);
		float sy = Mth.sin(time * speedY[i] + phaseY[i]);
		float wx = 0.03f * Mth.sin(time * speedX[i] * 2.0f + phaseX[i] + 1.0f);
		float wy = 0.03f * Mth.sin(time * speedY[i] * 1.8f + phaseY[i] + 2.0f);
		float nx = Mth.clamp(0.5f + 0.42f * sx + wx, 0.02f, 0.98f);
		float ny = Mth.clamp(0.5f + 0.40f * sy + wy, 0.02f, 0.98f);
		int cx = jarLeft + (int) (nx * (JAR_W - ICON));
		int cy = jarTop  + (int) (ny * (JAR_H - ICON));
		return new int[]{cx, cy};
	}

	// ── Rendering ────────────────────────────────────────────────────────────────

	@Override
	public void render(GuiGraphics g, int mx, int my, float partialTicks) {
		renderBackground(g);

		if (jarHandler == null || speedX == null) return;

		float time = getAnimTime(partialTicks);

		// Panel dimensions
		int pad = 16;
		int px = jarLeft - pad;
		int py = jarTop  - pad - 14;
		int pw = JAR_W + pad * 2;
		int ph = JAR_H + pad * 2 + 28;

		// ── Vein background (green-tinted, swirly) clipped to panel ──
		renderVeinBackground(g, px, py, pw, ph);

		// ── Gradient red border ──
		drawGradientBorder(g, px, py, pw, ph);

		// Title
		g.drawCenteredString(font, getTitle(), width / 2, py + 5, 0xFFBB4444);

		// Jar belly area — subtle inner glow instead of flat fill
		g.renderOutline(jarLeft - 1, jarTop - 1, JAR_W + 2, JAR_H + 2, 0x66225522);

		// Swimming morphlings
		hoveredIndex = -1;
		for (int i = 0; i < slotCount; i++) {
			ItemStack morphStack = jarHandler.getStackInSlot(i);
			if (morphStack.isEmpty()) continue;

			int[] pos = getMorphPos(i, time);
			int cx = pos[0];
			int cy = pos[1];

			float dx = mx - (cx + ICON / 2f);
			float dy = my - (cy + ICON / 2f);
			boolean hovered = (dx * dx + dy * dy) <= (HIT_R * HIT_R);
			boolean active  = (i == activeIndex);

			if (hovered) hoveredIndex = i;

			g.renderItem(morphStack, cx, cy);

			if (active || hovered) {
				int borderColor = active ? 0xFFFFD700 : 0xFF33CC33;
				boolean[][] mask = getPixelMask(morphStack);
				drawPixelOutline(g, cx, cy, mask, borderColor);
			}

			if (active) {
				g.drawString(font, "\u2605", cx + ICON - 2, cy - 5, 0xFFFFD700, true);
			}
		}

		// Tooltip (drawn last, on top)
		if (hoveredIndex >= 0) {
			ItemStack hoverStack = jarHandler.getStackInSlot(hoveredIndex);
			if (!hoverStack.isEmpty()) {
				g.renderTooltip(font, hoverStack, mx, my);
			}
		}

		g.drawCenteredString(font, "Click a morphling to equip", width / 2, py + ph - 12, 0xFF666666);
	}

	// ── Input ────────────────────────────────────────────────────────────────────

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (jarHandler == null || speedX == null)
			return super.mouseClicked(mx, my, button);

		float time = getAnimTime(Minecraft.getInstance().getPartialTick());

		for (int i = 0; i < slotCount; i++) {
			ItemStack morphStack = jarHandler.getStackInSlot(i);
			if (morphStack.isEmpty() || !(morphStack.getItem() instanceof MorphlingItem))
				continue;

			int[] pos = getMorphPos(i, time);
			int cx = pos[0] + ICON / 2;
			int cy = pos[1] + ICON / 2;

			float dx = (float) (mx - cx);
			float dy = (float) (my - cy);
			if (dx * dx + dy * dy <= HIT_R * HIT_R) {
				if (i == activeIndex) {
					// Clicking the already-equipped morphling unequips it
					PacketHandler.sendToServer(new PacketUpdateLivingStaffMorph(-1));
				} else {
					PacketHandler.sendToServer(new PacketUpdateLivingStaffMorph(i));
				}
				Player player = Minecraft.getInstance().player;
				if (player != null)
					player.playSound(SoundEvents.GLASS_PLACE, 0.4f, 1.2f);
				onClose();
				return true;
			}
		}

		// Click outside panel → close
		int pad = 16;
		int ppx = jarLeft - pad;
		int ppy = jarTop  - pad - 14;
		int ppw = JAR_W + pad * 2;
		int pph = JAR_H + pad * 2 + 28;
		if (mx < ppx || mx > ppx + ppw || my < ppy || my > ppy + pph) {
			onClose();
			return true;
		}

		return super.mouseClicked(mx, my, button);
	}

	// ───── Gradient Red Border ─────

	/**
	 * Draws a multi-layer gradient border that fades from bright crimson on the
	 * outside edge to a darker maroon on the inside, giving a rich blood-framed look.
	 */
	private void drawGradientBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		// 4-pixel thick gradient border, bright outside → dark inside
		int[] colors = {
			0xFF6B1010,  // outermost — bright crimson
			0xFF4A0C0C,  // mid-outer
			0xFF300808,  // mid-inner
			0xFF1A0404,  // innermost — near-black maroon
		};
		for (int i = 0; i < colors.length; i++) {
			int c = colors[i];
			int xi = x + i;
			int yi = y + i;
			int wi = w - i * 2;
			int hi = h - i * 2;
			// Top
			gfx.fill(xi, yi, xi + wi, yi + 1, c);
			// Bottom
			gfx.fill(xi, yi + hi - 1, xi + wi, yi + hi, c);
			// Left
			gfx.fill(xi, yi, xi + 1, yi + hi, c);
			// Right
			gfx.fill(xi + wi - 1, yi, xi + wi, yi + hi, c);
		}

		// Corner highlights — subtle brighter dots at the 4 outer corners
		int highlight = 0xFF8A1818;
		gfx.fill(x, y, x + 2, y + 2, highlight);
		gfx.fill(x + w - 2, y, x + w, y + 2, highlight);
		gfx.fill(x, y + h - 2, x + 2, y + h, highlight);
		gfx.fill(x + w - 2, y + h - 2, x + w, y + h, highlight);
	}

	// ───── Procedural Animated Vein Background (Green / Swirly) ─────

	/**
	 * Renders a dark green/black background covered in swimming, swirly vein tendrils.
	 * Similar to the red vein background used in TendencyView and VascularStatus,
	 * but tinted green with higher-frequency curves for a more swirly, organic feel.
	 */
	private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
		graphics.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: solid dark green-black base
		graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF040A04);

		// Layer 2: green radial glow centered in the panel
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 3) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (30 * (1f - t));
			int green = (int) (35 * (1f - t));
			int red = (int) (8 * (1f - t));
			int color = (alpha << 24) | (red << 16) | (green << 8);
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		// Layer 3: animated green vein tendrils (swirly)
		float time = (System.nanoTime() / 1_000_000_000f);
		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawGreenVeinTendril(graphics, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: organic speckles — green-tinted noise
		Random speckRand = new Random(54321L);
		for (int s = 0; s < 80; s++) {
			int sx = gx + speckRand.nextInt(gw);
			int sy = gy + speckRand.nextInt(gh);
			int sg = 12 + speckRand.nextInt(20);
			int sr = speckRand.nextInt(6);
			int sa = 12 + speckRand.nextInt(20);
			graphics.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
		}

		// Layer 5: floating bubbles
		if (bubbleParams != null) {
			drawBubbles(graphics, time, gx, gy, gw, gh);
		}

		RenderSystem.disableBlend();
		graphics.disableScissor();
	}

	/**
	 * Draws small translucent bubbles that float upward with a gentle side-to-side
	 * wobble, looping back to the bottom when they reach the top.
	 * Each bubble is drawn as a filled circle (a few concentric rings) with a
	 * brighter highlight pixel for a glassy look.
	 */
	private void drawBubbles(GuiGraphics graphics, float time,
							 int gx, int gy, int gw, int gh) {
		for (int i = 0; i < BUBBLE_COUNT; i++) {
			float[] b = bubbleParams[i];
			float xRatio   = b[0];
			float riseSpeed = b[1];
			float wobbleAmp = b[2];
			float wobbleFreq = b[3];
			float radius    = b[4];
			float phase     = b[5];

			// Y position: rises from bottom to top, loops via modulo
			float cycle = (time * riseSpeed + phase) % 1.0f;
			float by = gy + gh - cycle * (gh + radius * 4); // starts below, exits above

			// X position: base + sine wobble
			float bx = gx + xRatio * gw + wobbleAmp * Mth.sin(time * wobbleFreq + phase * 6.28f);

			int ix = (int) bx;
			int iy = (int) by;

			// Skip if out of bounds
			if (iy < gy - 4 || iy > gy + gh + 4) continue;
			if (ix < gx - 4 || ix > gx + gw + 4) continue;

			// Fade in at bottom, fade out at top
			float fadeY = 1f;
			float relY = (float)(iy - gy) / gh;
			if (relY > 0.85f) fadeY = (1f - relY) / 0.15f;
			else if (relY < 0.1f) fadeY = relY / 0.1f;
			fadeY = Mth.clamp(fadeY, 0f, 1f);

			// Draw the bubble as concentric rings
			int rad = Math.max(1, (int) radius);
			for (int ring = rad; ring >= 0; ring--) {
				float t = (rad > 0) ? (float) ring / rad : 0f;
				// Outer = more transparent, inner = brighter
				int alpha = (int) (Mth.clamp((50 + 80 * (1f - t)) * fadeY, 0, 200));
				int green = (int) (40 + 60 * (1f - t));
				int red   = (int) (10 + 20 * (1f - t));
				int blue  = (int) (15 + 15 * (1f - t));
				int color = (alpha << 24) | (red << 16) | (green << 8) | blue;
				graphics.fill(ix - ring, iy - ring, ix + ring + 1, iy + ring + 1, color);
			}

			// Specular highlight — a brighter pixel offset up-left
			if (rad >= 2) {
				int hlAlpha = (int) (Mth.clamp(100 * fadeY, 0, 180));
				int highlight = (hlAlpha << 24) | (0x30 << 16) | (0x70 << 8) | 0x30;
				graphics.fill(ix - rad / 2, iy - rad / 2, ix - rad / 2 + 1, iy - rad / 2 + 1, highlight);
			}
		}
	}

	/**
	 * Draws a single animated green vein tendril that curls and flows like liquid.
	 * Instead of traveling in a straight line with perpendicular displacement,
	 * the heading itself rotates per step (curvature), producing spirals and loops.
	 * Multiple sine layers on top add fine swirly detail.
	 */
	private void drawGreenVeinTendril(GuiGraphics graphics, int index, float time,
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
		float curvature = p[9];

		float timeOffset = time * speed * 2.5f;

		// Animate the base curvature so the curl drifts over time
		float liveCurvature = curvature + 0.012f * Mth.sin(time * speed * 0.35f + index * 2.1f);

		// Green-tinted base colors
		int baseGreen = (int) (35 + 55 * brightness);
		int baseRed   = (int) (4 + 10 * brightness);
		int baseBlue  = (int) (5 + 8 * brightness);

		// Walk the tendril step by step, rotating the heading each step
		float px = startX;
		float py = startY;
		float heading = baseAngle + 0.3f * Mth.sin(time * speed * 0.25f + index);

		for (int step = 0; step < length; step++) {
			// Rotate heading per step — this is what makes it curl
			heading += liveCurvature;

			// Add a sine wobble to the heading for extra liquidy sway
			float headingWobble = 0.08f * Mth.sin(frequency * 2.0f * step + timeOffset)
					+ 0.05f * Mth.sin(frequency * 4.3f * step + timeOffset * 1.7f + index);
			float currentHeading = heading + headingWobble;

			// Lateral displacement (perpendicular squiggle on top of the curling path)
			float squiggle = amplitude * 0.3f * Mth.sin(frequency * step + timeOffset);
			float micro    = amplitude * 0.12f * Mth.sin(frequency * 3.4f * step + timeOffset * 1.5f + index);

			float cosH = Mth.cos(currentHeading);
			float sinH = Mth.sin(currentHeading);

			// Step forward along the curving heading + lateral displacement
			float stepSize = 1.2f;
			px += cosH * stepSize - (squiggle + micro) * sinH * 0.15f;
			py += sinH * stepSize + (squiggle + micro) * cosH * 0.15f;

			int ix = (int) px;
			int iy = (int) py;

			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) {
				continue;
			}

			// Tip fade
			float tipFade = 1f;
			if (step < 10) tipFade = step / 10f;
			else if (step > length - 10) tipFade = (length - step) / 10f;

			// Pulsing brightness
			float pulse = 0.6f + 0.4f * Mth.sin(time * 1.8f + index * 0.6f + step * 0.03f);

			int alpha = (int) (Mth.clamp(tipFade * pulse * 170, 15, 190));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.4f, 0, 255);

			int color = (alpha << 24) | (r << 16) | (g << 8) | b;
			graphics.fill(ix, iy, ix + thickness, iy + thickness, color);
		}
	}
}


