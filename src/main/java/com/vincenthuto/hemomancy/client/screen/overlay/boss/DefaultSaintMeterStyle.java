package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * The kit's generic meter look, which each Saint's placeholder draws until its builder paints its own: a recessed
 * strip for the Saint's meter, a hairline under it for the challenger's own value, and a short tick that drains while an
 * opening is open. Colours are the Saint's; the shapes are the same for all four.
 */
public class DefaultSaintMeterStyle implements SaintMeterStyle {
	private static final int HEIGHT = 8;
	private final int fillTop;
	private final int fillBottom;
	private final int own;
	private final int well;

	public DefaultSaintMeterStyle(int fillTop, int fillBottom, int own, int well) {
		this.fillTop = fillTop;
		this.fillBottom = fillBottom;
		this.own = own;
		this.well = well;
	}

	@Override
	public int height() {
		return HEIGHT;
	}

	@Override
	public void paint(SaintMeterContext c) {
		GuiCanvas g = c.canvas();
		if (g == null) return;
		float open = HemoEasing.smoothstep(HemoEasing.clamp01(c.reveal() / 0.6F));
		float rise = HemoEasing.smoothstep(HemoEasing.clamp01((c.reveal() - 0.4F) / 0.6F));
		float half = 91.0F * open;
		float x0 = c.centerX() - half, x1 = c.centerX() + half;
		float top = c.y(), bottom = c.y() + 4;
		g.rect(x0 - 1, top - 1, x1 + 1, bottom + 1, GuiCanvas.fade(well, 0.85F), GuiCanvas.fade(well, 0.85F));
		float end = c.x() + 182.0F * c.saintFraction() * rise;
		if (end > c.x() + 0.01F) {
			g.rect(Math.max(x0, c.x()), top, Math.min(x1, end), bottom, fillTop, fillBottom);
			g.rect(Math.max(x0, c.x()), top, Math.min(x1, end), top + 1, 0x30FFFFFF, 0x30FFFFFF);
		}
		float ownEnd = c.x() + 182.0F * c.ownFraction() * rise;
		if (c.ownValue() > 0.0F && ownEnd > c.x() + 0.01F) {
			g.rect(Math.max(x0, c.x()), bottom + 2, Math.min(x1, ownEnd), bottom + 3, own, GuiCanvas.fade(own, 0.6F));
		}
		float opening = c.openingFraction();
		if (c.openingId() != 0) {
			float width = 24.0F * (1.0F - opening);
			g.rect(c.centerX() - width, bottom + 2, c.centerX() + width, bottom + 3, 0xC0F2E8D0, 0x60F2E8D0);
		}
	}
}
