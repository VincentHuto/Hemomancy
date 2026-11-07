package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;

/**
 * Saint Seraphae's slot in the kit's telegraph pass (visual pass 2, 2026-11-06). Her footprints and her healed pads are
 * drawn by her own pass, {@link SeraphaeTelegraphPass}, which reads the same synced move, frame, timeline and reveal
 * and draws them as the floor healing over too fast (furrows swelling shut, overgrowth, vessels of light, gold nerve
 * fibres), bone and light with body; so this style claims every move and every pad and paints nothing, and the kit's
 * default painter (flat tints and hairline edges) never shows for her. The kit itself is unchanged for the other Saints.
 */
public final class SeraphaeTelegraphStyle implements SaintTelegraphStyle {
	@Override
	public boolean paint(TelegraphDraw draw, TelegraphCanvas canvas) {
		return draw.move() instanceof SeraphaeMove;
	}

	@Override
	public boolean paintHazard(HazardDraw draw, TelegraphCanvas canvas) {
		return draw.hazard().kind() == SeraphaeEntity.PAD_KIND;
	}
}
