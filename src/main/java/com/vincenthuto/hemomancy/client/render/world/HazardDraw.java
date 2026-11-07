package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;

/**
 * One ground hazard as the telegraph pass draws it: its synced shape and frame, its {@code age} (its own tick count
 * plus the partial tick), its {@code life}, and {@code fade} (1 while it stands, running to 0 over its fade once its
 * owner expires it; it also eases in over its first ticks).
 */
public record HazardDraw(BossGroundHazardEntity hazard, BossFootprint shape, FootprintFrame frame, float age, float life,
		float fade, float partialTick) {
}
