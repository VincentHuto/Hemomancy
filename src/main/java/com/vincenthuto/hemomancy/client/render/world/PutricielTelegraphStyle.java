package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphFamily;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielHazard;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;

/**
 * Saint Putriciel's slot in the kit's telegraph pass (visual pass 2, 2026-11-06). Their footprints and hazards are
 * drawn by their own pass, {@link PutricielTelegraphPass}, which reads the same synced move, frame, timeline and
 * reveal and draws them as the floor colonised by the rot (mould spreading, trenches of ooze, bracket fungus and sacs
 * pushing up) or scorched black by the purifying fire; so this style claims every move and every hazard (the rot
 * sludge, its burning pools and the fire runners of revision 2026-11-06 included, by their kind) and paints nothing,
 * and the kit's default painter (flat tints and hairline edges) never shows for them. The kit itself is unchanged for
 * the other Saints.
 */
public final class PutricielTelegraphStyle implements SaintTelegraphStyle {
	@Override
	public boolean paint(TelegraphDraw draw, TelegraphCanvas canvas) {
		return draw.move() instanceof PutricielMove;
	}

	@Override
	public boolean paintHazard(HazardDraw draw, TelegraphCanvas canvas) {
		return PutricielHazard.byId(draw.hazard().kind()) != null;
	}

	/** The family a move's footprint belongs to: fire moves in flame, the rest in rot (their own pass's two looks). */
	static TelegraphFamily familyOf(PutricielMove move) {
		return move.fire() ? TelegraphFamily.FLAME : TelegraphFamily.PUTRICIEL_ROT;
	}
}
