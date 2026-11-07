package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;

/**
 * Saint Velorum's slot in the kit's telegraph pass. His footprints and his standing hazards are drawn by his own pass,
 * {@link VelorumTelegraphPass}, which reads the same synced move, frame, timeline and reveal and draws them as blood
 * arrested into Frozen Cruor (seams welling and setting, trenches the cruor wells up to fill, blades and slabs standing
 * out of it) and the liquid dark that thins sight; so this style claims every move and every hazard of his (the cruor
 * field, the pools of dark, the Eclipse's shapes and reaches) and paints nothing, and the kit's default painter (flat
 * tints and hairline edges) never shows for him. The kit itself is unchanged for the other Saints.
 */
public final class VelorumTelegraphStyle implements SaintTelegraphStyle {
	@Override
	public boolean paint(TelegraphDraw draw, TelegraphCanvas canvas) {
		return draw.move() instanceof VelorumMove;
	}

	@Override
	public boolean paintHazard(HazardDraw draw, TelegraphCanvas canvas) {
		return VelorumDarkSight.field(draw.hazard()) || VelorumDarkSight.pool(draw.hazard()) || VelorumDarkSight.shape(draw.hazard())
				|| VelorumDarkSight.reach(draw.hazard());
	}
}
