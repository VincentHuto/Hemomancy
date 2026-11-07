package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import net.minecraft.client.resources.sounds.SoundInstance;

/**
 * One Saint's meter art under its bar, painted like the bar frames ({@code SaintClientStyles} maps each Saint to one).
 * {@link #paint} draws in the rows under the bar; {@link #paintScreen} is a full-screen pass drawn first (Velorum's frost
 * edge); {@link #muffle} scales the volume of sounds the Saint did not make (Velorum's darkness). The context's
 * {@code saint} is null when the bar is hidden or the Saint is out of render range: draw from the packet alone then.
 */
public interface SaintMeterStyle {
	/** Rows the style needs under the bar; the bars below make room for them. */
	int height();

	void paint(SaintMeterContext c);

	default void paintScreen(SaintMeterContext c) {
	}

	/** Volume factor for a sound the Saint did not make, 1 for none. The context's canvas is null here. */
	default float muffle(SaintMeterContext c, SoundInstance sound) {
		return 1.0F;
	}
}
