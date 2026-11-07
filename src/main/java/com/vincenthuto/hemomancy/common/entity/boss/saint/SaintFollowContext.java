package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;

import javax.annotation.Nullable;

/** What a follow-up's condition reads when the move before it ends. */
public record SaintFollowContext(@Nullable SaintTrialEntity saint, int phase, boolean hitLanded, BossBands.Band band,
		BossBands.Sector sector) {
}
