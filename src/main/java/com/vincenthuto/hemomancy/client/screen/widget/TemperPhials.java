package com.vincenthuto.hemomancy.client.screen.widget;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.gui.GuiGraphics;

/**
 * The small pixel phial that marks a Temper (core loop Track 4), shared by the HUD's Temper marks, a Specimen vessel's
 * screen and the Living Bestiary (P6). A phial fills from the bottom in its school's colour; an unread one is hollow and
 * grey, the mark of a humour the player has not examined. Drawn pixel by pixel at {@code scale} GUI pixels per pixel.
 */
public final class TemperPhials {
	public static final int WIDTH = 5;
	public static final int HEIGHT = 9;
	private static final int RIM = 0xFF1A0C0C;
	private static final int DRY_RIM = 0xFF7A1E1E;
	private static final int EMPTY = 0xFF2C1A1A;
	private static final int UNREAD_RIM = 0xFF6E6468;
	private static final int UNREAD_STOPPER = 0xFF8E8488;
	private static final int UNREAD_BODY = 0x66140E10;
	private static final int UNREAD_MARK = 0xFF4A4246;
	/** A 5x9 phial: '.' blank, '1' rim, '2' body (fills from the bottom), '3' the stopper. */
	private static final String[] ICON = {
			".333.",
			"..1..",
			".121.",
			"12221",
			"12221",
			"12221",
			"12221",
			"12221",
			".111."};
	private static final int BODY_TOP = 2;
	private static final int BODY_BOTTOM = 7;

	private TemperPhials() {
	}

	/** A school's phial at one GUI pixel per pixel. */
	public static void draw(GuiGraphics graphics, int x, int y, EnumBloodTemper school, float fraction, boolean dry) {
		draw(graphics, x, y, 1, school, fraction, dry);
	}

	/** A school's phial filled to {@code fraction}, with a dull red rim when it is too low to pay a cast. */
	public static void draw(GuiGraphics graphics, int x, int y, int scale, EnumBloodTemper school, float fraction,
			boolean dry) {
		int bodyRows = BODY_BOTTOM - BODY_TOP + 1;
		int filledRows = fraction <= 0 ? 0 : Math.max(1, Math.round(fraction * bodyRows));
		int colour = schoolColour(school, fraction);
		for (int row = 0; row < ICON.length; row++) {
			for (int col = 0; col < ICON[row].length(); col++) {
				char c = ICON[row].charAt(col);
				if (c == '.') continue;
				int pixel;
				if (c == '1') {
					pixel = dry ? DRY_RIM : RIM;
				} else if (c == '3') {
					pixel = blend(0xFF000000 | (schoolColour(school, 1.0F) & 0x00FFFFFF), RIM, 0.55F);
				} else {
					boolean filled = row > BODY_BOTTOM - filledRows && row >= BODY_TOP;
					pixel = filled ? colour : EMPTY;
				}
				pixel(graphics, x, y, scale, col, row, pixel);
			}
		}
	}

	/**
	 * An unread humour: a hollow grey phial with a dark seam across its middle, so it reads as a mark left blank rather
	 * than an empty pool.
	 */
	public static void drawUnread(GuiGraphics graphics, int x, int y, int scale) {
		for (int row = 0; row < ICON.length; row++) {
			for (int col = 0; col < ICON[row].length(); col++) {
				char c = ICON[row].charAt(col);
				if (c == '.') continue;
				int pixel = c == '1' ? UNREAD_RIM : c == '3' ? UNREAD_STOPPER : row == 5 ? UNREAD_MARK : UNREAD_BODY;
				pixel(graphics, x, y, scale, col, row, pixel);
			}
		}
	}

	private static void pixel(GuiGraphics graphics, int x, int y, int scale, int col, int row, int colour) {
		graphics.fill(x + col * scale, y + row * scale, x + (col + 1) * scale, y + (row + 1) * scale, colour);
	}

	/** The school's colour, lifted toward its own brightness as the pool fills, so a full phial reads lit. */
	public static int schoolColour(EnumBloodTemper school, float fraction) {
		ParticleColor colour = school.getColor();
		int r = Math.round(colour.getRed());
		int g = Math.round(colour.getGreen());
		int b = Math.round(colour.getBlue());
		// Dark schools (Mortem, Ferric, Tenebris) would vanish on the dark phial; lift them to a readable floor.
		int max = Math.max(r, Math.max(g, b));
		if (max < 140) {
			float lift = 140.0F / Math.max(1, max);
			r = Math.min(255, Math.round(r * lift + 30));
			g = Math.min(255, Math.round(g * lift + 30));
			b = Math.min(255, Math.round(b * lift + 30));
		}
		int full = 0xFF000000 | (r << 16) | (g << 8) | b;
		return blend(full, EMPTY, 0.35F + 0.65F * Math.clamp(fraction, 0.0F, 1.0F));
	}

	private static int blend(int a, int b, float towardA) {
		float t = Math.clamp(towardA, 0.0F, 1.0F);
		int r = Math.round(((a >> 16) & 0xFF) * t + ((b >> 16) & 0xFF) * (1 - t));
		int g = Math.round(((a >> 8) & 0xFF) * t + ((b >> 8) & 0xFF) * (1 - t));
		int bl = Math.round((a & 0xFF) * t + (b & 0xFF) * (1 - t));
		return 0xFF000000 | (r << 16) | (g << 8) | bl;
	}
}
