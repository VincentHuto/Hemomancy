package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMove;

/**
 * Saint Hemorath's slot in the kit's telegraph pass (visual pass 2, 2026-11-06). His footprints and hazards are drawn
 * by his own pass, {@link HemorathTelegraphPass}, which reads the same synced move, frame, timeline and reveal and
 * draws them as the floor cracking under him, iron and blood; so this style claims every move and both hazards and
 * paints nothing, and the kit's default painter (flat tints and hairline edges) never shows for him. The kit itself is
 * unchanged for the other Saints.
 */
public final class HemorathTelegraphStyle implements SaintTelegraphStyle {
	@Override
	public boolean paint(TelegraphDraw draw, TelegraphCanvas canvas) {
		return draw.move() instanceof HemorathMove;
	}

	@Override
	public boolean paintHazard(HazardDraw draw, TelegraphCanvas canvas) {
		int kind = draw.hazard().kind();
		return kind == HemorathEntity.HAZARD_THICKET || kind == HemorathEntity.HAZARD_TIDE;
	}
}
