package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterClientState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathPhaseRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Saint Hemorath, the Hollow Vessel: riveted hematic-iron rails around a well drained to bare ash, with an empty urn at
 * each end. When the vessel empties (phase 2) the iron tarnishes violet and the urns crack; in the Last Thirst (phase
 * 3 of the frame, read from the bar's name) the rails go back to iron and both urns fill with blood rising from their
 * base as he drinks. Notches at half health (phase 2) and a fifth (the Last Thirst).
 */
public final class HemorathFrame implements BossBarFrame {
	private static final String EMPTIES_KEY = HemorathEntity.EMPTIES_KEY;
	private static final String THIRST_KEY = HemorathEntity.THIRST_KEY;
	private static final int IRON_DARK = 0xFF141012, IRON = 0xFF3A3236, IRON_LIP = 0xFF6A5A5E;
	private static final int TARNISH_DARK = 0xFF140A1C, TARNISH = 0xFF3A2448, TARNISH_LIP = 0xFF6A4A80;
	private static final int RUST = 0x7A2A1A;
	private static final int BLOOD_TOP = 0xFFB0202A, BLOOD_BOTTOM = 0xFF4A0A12, CORE = 0xFFD2C4;

	public static int phaseOf(Component name) {
		String key = BossBarFramePainter.nameKey(name);
		if (key.equals(THIRST_KEY)) return 3;
		return key.equals(EMPTIES_KEY) ? 2 : 1;
	}

	@Override
	public int phase(LerpingBossEvent event) {
		return phaseOf(event.getName());
	}

	@Override
	public float[] notches() {
		return new float[] { HemorathPhaseRules.THIRST_HEALTH, HemorathPhaseRules.PHASE_2_HEALTH };
	}

	@Override
	public int fillTop(int phase) {
		return phase == 2 ? 0xFF8A3AA8 : BLOOD_TOP;
	}

	@Override
	public int fillBottom(int phase) {
		return phase == 2 ? 0xFF2A0C3A : BLOOD_BOTTOM;
	}

	@Override
	public int wellColor() {
		return 0xF04A4442;
	}

	@Override
	public int nameColor(int phase) {
		return phase == 2 ? 0xC8A8E0 : 0xD8CCC8;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		boolean emptied = c.phase() == 2;
		BossBarFramePainter.rails(c, 1, emptied ? TARNISH_DARK : IRON_DARK, emptied ? TARNISH : IRON,
				emptied ? TARNISH_LIP : IRON_LIP);
		for (float rx = c.x() + 4; rx < c.right() - 2; rx += 16.0F) {
			for (int row = 0; row < 2; row++) {
				float ry = row == 0 ? c.y() + 1.0F : c.y() + 12.0F;
				g.disc(rx, ry, 0.9F, 0.9F, 6, emptied ? 0xFF9A7AB8 : 0xFF8A7A7E, 0xFF201A1C);
			}
		}
		for (int i = 0; i < 5; i++) {
			float sx = c.x() + GuiCanvas.hash(41, i, 0) * BossBarFrame.WIDTH;
			g.rect(sx, c.y() + 12.0F, sx + 1.0F, c.y() + 13.0F + GuiCanvas.hash(41, i, 1) * 2.5F,
					GuiCanvas.argb(0.7F, RUST), GuiCanvas.argb(0.0F, RUST));
		}
		float fill = c.phase() == 3 ? thirstProgress() : 0.0F;
		urn(c, c.x() - 10.0F, emptied, fill);
		urn(c, c.right() + 10.0F, emptied, fill);
	}

	/** How far through the Last Thirst's draw he is, 0 to 1, from the Saint this client fights. */
	private static float thirstProgress() {
		if (!(SaintMeterClientState.saint() instanceof HemorathEntity saint)) return 0.5F;
		return HemoEasing.clamp01(saint.syncedThirst() / (float) HemorathPhaseRules.THIRST_TICKS);
	}

	/** An urn: a dark mouth and an iron body; cracked in phase 2; filling with blood from its base in the Last Thirst. */
	private static void urn(BossBarFrameContext c, float cx, boolean cracked, float fill) {
		GuiCanvas g = c.canvas();
		float cy = c.midY() + 1.0F;
		int body = cracked ? TARNISH : IRON, dark = cracked ? TARNISH_DARK : IRON_DARK;
		g.disc(cx, cy, 6.2F, 6.8F, 18, body, dark);
		g.rect(cx - 3.0F, cy - 9.0F, cx + 3.0F, cy - 5.0F, body, dark);
		g.disc(cx, cy - 9.0F, 3.6F, 1.2F, 12, 0xFF050304, 0xFF050304);
		if (fill > 0.0F) {
			// blood rising from the base, a meniscus of the pale core on top
			float level = cy + 6.0F - 12.0F * HemoEasing.smoothstep(fill);
			float halfWidth = 4.8F * Mth.sqrt(Math.max(0.0F, 1.0F - ((level - cy) / 6.8F) * ((level - cy) / 6.8F)));
			g.rect(cx - 4.4F, level, cx + 4.4F, cy + 5.5F, BLOOD_TOP, BLOOD_BOTTOM);
			g.rect(cx - halfWidth, level, cx + halfWidth, level + 1.0F, GuiCanvas.argb(0.7F, CORE), GuiCanvas.argb(0.7F, CORE));
		}
		g.disc(cx - 2.0F, cy - 1.5F, 1.2F, 2.6F, 8, 0x40FFFFFF, 0x00FFFFFF);
		if (cracked) {
			float flicker = 0.6F + Mth.sin(c.time() * 0.2F + cx) * 0.2F;
			g.segment(cx + 1.0F, cy - 6.0F, cx - 1.5F, cy, 0.8F, 0.6F, GuiCanvas.argb(flicker, 0xB080E0));
			g.segment(cx - 1.5F, cy, cx + 1.5F, cy + 5.0F, 0.6F, 0.2F, GuiCanvas.argb(flicker, 0xB080E0));
		}
	}
}
