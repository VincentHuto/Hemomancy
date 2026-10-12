package com.vincenthuto.hemomancy.client.player;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Counts consecutive swings per entity and arm so weapons can alternate strokes. Renders observe swing
 * progress; a swing starts when progress rises from rest or restarts mid-swing. A pause resets the combo.
 */
public final class SwingComboTracker {
	private static final int RESET_TICKS = 30;
	private static final Map<Key, State> STATES = new HashMap<>();
	private static Level activeLevel;

	private SwingComboTracker() { }

	public static int strike(LivingEntity entity, HumanoidArm arm, float progress) {
		if (entity.level() != activeLevel) {
			STATES.clear();
			activeLevel = entity.level();
		}
		State state = STATES.computeIfAbsent(new Key(entity.getId(), arm), key -> new State());
		boolean started = progress > 0 && (state.lastProgress <= 0 || progress < state.lastProgress - .25F);
		if (started) {
			state.index = entity.tickCount - state.startTick > RESET_TICKS ? 0 : state.index + 1;
			state.startTick = entity.tickCount;
		}
		state.lastProgress = progress;
		return state.index;
	}

	private record Key(int entityId, HumanoidArm arm) { }

	private static final class State {
		private float lastProgress;
		private int index;
		private int startTick = Integer.MIN_VALUE / 2;
	}
}
