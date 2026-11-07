package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * A running move on one server tick: the tick counted from its start, its stage, whether this is the commit tick, the
 * footprint frame as it stands (aim locked from the commit on), the target and the arena.
 */
public record SaintMoveContext(SaintTrialEntity saint, ServerLevel level, SaintMove move, SaintMoveInputs inputs,
		int tick, BossMoveTimeline.Stage stage, boolean committedNow, FootprintFrame frame,
		@Nullable LivingEntity target, SaintArena arena) {
	/** Ticks since the commit tick, negative through the wind-up. */
	public int age() {
		return tick - saint.controller().timeline().commitTick();
	}
}
