package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import javax.annotation.Nullable;
import java.util.List;

import static com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAction.*;

/**
 * The possession wheel: the visit's controls every form shares, the gifts each body lends, which of them toggle, how
 * long a gift cools, and what the server refuses while a will is away. Pure, so the wheel and the server agree.
 */
public final class PossessionWheelRules {
	/** Blocks the body may drift from where the return hold began before the hold breaks. */
	public static final double RETURN_DRIFT = 0.6;

	public enum Verdict { USE, NOT_POSSESSING, NOT_OFFERED, COOLING }

	private PossessionWheelRules() {}

	/** The inner ring. Track 3's Linger joins here if the cinematics need it. */
	public static List<PossessionAction> visit(SurrogateForm form) {
		return List.of(RETURN, STILL);
	}

	/** The outer ring: the borrowed body's gifts, triggers before toggles. */
	public static List<PossessionAction> body(SurrogateForm form) {
		return switch (form) {
			case BAT -> List.of(ECHOLOCATE, HOVER);
			case ENDERMAN -> List.of(STEP);
			case BLAZE -> List.of(SPIT, HOVER);
			case GOLEM -> List.of(SLAM);
			case HORSE -> List.of(LEAP);
			case WOLF -> List.of(LUNGE);
			case SPIDER -> List.of(CLIMB);
			case DONKEY, ARMOR_STAND -> List.of();
		};
	}

	public static boolean offers(SurrogateForm form, PossessionAction action) {
		return visit(form).contains(action) || body(form).contains(action);
	}

	public static boolean isToggle(PossessionAction action) {
		return action == STILL || action == CLIMB || action == HOVER;
	}

	/** Ticks a used gift rests; 0 for the visit controls, toggles and anything the form does not offer. */
	public static int cooldownTicks(SurrogateForm form, PossessionAction action) {
		return body(form).contains(action) && !isToggle(action) ? form.abilityCooldown() : 0;
	}

	public static Verdict check(@Nullable SurrogateForm form, PossessionAction action, long now, long readyAt) {
		if (form == null) return Verdict.NOT_POSSESSING;
		if (!offers(form, action)) return Verdict.NOT_OFFERED;
		if (now < readyAt) return Verdict.COOLING;
		return Verdict.USE;
	}

	/** A possessed will casts nothing of its own; letting a running channel go is always allowed. */
	public static boolean refusesManipulation(boolean possessing, boolean stopping) {
		return possessing && !stopping;
	}

	public static boolean returnBroken(double drift) {
		return drift > RETURN_DRIFT;
	}
}
