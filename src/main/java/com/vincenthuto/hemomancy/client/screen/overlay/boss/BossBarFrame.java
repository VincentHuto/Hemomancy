package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import net.minecraft.client.gui.components.LerpingBossEvent;

/**
 * One boss's bar art. The shared {@link BossBarFramePainter} draws the well, fill, chip, notches and name; a frame
 * supplies the colours and paints its rails and ornaments behind ({@link #paintBack}) and over ({@link #paintFront})
 * them. Phase comes from what the client already knows about the bar: its progress, colour and name.
 */
public interface BossBarFrame {
	int WIDTH = 182;

	/** Rows from this bar's origin to the next bar's. */
	default int increment() {
		return 30;
	}

	/** Bar fractions marked with a notch: phase thresholds, sorted, inside (0, 1). */
	default float[] notches() {
		return new float[0];
	}

	/** 1 for the opening phase; a frame that changes look counts up from there. */
	default int phase(LerpingBossEvent event) {
		return 1;
	}

	int fillTop(int phase);

	int fillBottom(int phase);

	/** The lost stretch, lit before it drains. */
	default int chipColor(int phase) {
		return 0xFFEFE2C0;
	}

	default int wellColor() {
		return 0xF0100406;
	}

	default int notchColor(int phase) {
		return 0xC0F2E8D0;
	}

	/** The name's colour, RGB. */
	int nameColor(int phase);

	default void paintBack(BossBarFrameContext c) {
	}

	default void paintFront(BossBarFrameContext c) {
	}
}
