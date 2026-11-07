package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphSpec;

/**
 * One kit Saint's running move as the telegraph pass draws it, all from synced data: the footprint and timeline the
 * move gives for the synced inputs (wind-up floored as the server runs it), the frame the server tests, the move's
 * time ({@code moveTime}, ticks since its start) and {@code age} (ticks since the commit, negative in the wind-up),
 * {@code reveal} (0 at the reveal tick to 1 at the commit) and {@code commitFlash} (1 at the commit, fading over 4 ticks).
 */
public record TelegraphDraw(SaintTrialEntity saint, SaintMove move, BossFootprint footprint, FootprintFrame frame,
		BossMoveTimeline timeline, BossMoveTimeline.Stage stage, float moveTime, float age, TelegraphSpec spec,
		float reveal, float commitFlash, float partialTick) {
	public boolean committed() {
		return age >= 0.0F;
	}
}
