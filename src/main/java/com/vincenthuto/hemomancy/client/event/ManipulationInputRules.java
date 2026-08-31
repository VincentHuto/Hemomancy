package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

final class ManipulationInputRules {
	private ManipulationInputRules() {
	}

	static InputResult tick(EnumManipulationType type, boolean down, boolean clicked, int heldTicks,
			int fullChargeTicks) {
		int previous = Math.max(0, heldTicks);
		return switch (type) {
			case QUICK, PASSIVE -> new InputResult(clicked ? Action.CAST : Action.NONE, 0, 0);
			case CONTINUOUS -> down
					? new InputResult(previous == 0 ? Action.START_CONTINUOUS : Action.NONE, 0, 1)
					: new InputResult(previous > 0 ? Action.STOP_CONTINUOUS : Action.NONE, 0, 0);
			case CHARGED -> {
				if (down) {
					int charge = Math.min(previous + 1, Math.max(1, fullChargeTicks));
					yield charge >= Math.max(1, fullChargeTicks)
							? new InputResult(Action.CAST, charge, charge)
							: new InputResult(Action.NONE, 0, charge);
				}
				yield new InputResult(previous > 0 ? Action.CAST : Action.NONE, previous, 0);
			}
		};
	}

	enum Action {
		NONE,
		CAST,
		START_CONTINUOUS,
		STOP_CONTINUOUS
	}

	record InputResult(Action action, int castTicks, int nextHeldTicks) {
		boolean cast() {
			return action == Action.CAST;
		}
	}
}
