package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCulmination;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTermination.Outcome;
import com.vincenthuto.hemomancy.common.succession.SuccessionRites;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

/**
 * What kind of Cardinal Rite is running, and the parts of the shared tick, completion and ending that differ by kind.
 * {@link HarbingerCardinalRiteEvents} runs every rite through the same course and asks its kind at each step that
 * differs: whether it may tick at all, whether it pauses rather than binds its caster, how it advances, how it
 * completes and what it gives back when it ends.
 *
 * <p>A rite's kind follows from its saved state and its recipe, in the order the constants are declared: a
 * Succession, a station upgrade or a Working craft is known by its own state or recipe id; a Puppeteer trial by its
 * recipe; a legacy or Unstained rite without an interactive ceremony counts down; every other rite is a ceremony.
 */
public enum RiteKind {
	/** A bloodline successor is made or restored; it runs its own fixed course and ends itself. */
	SUCCESSION {
		@Override
		boolean matches(ActiveCardinalRite rite, @Nullable CardinalRiteRecipe recipe) {
			return SuccessionRites.is(rite);
		}

		@Override
		Admission admit(ServerLevel level, @Nullable ServerPlayer caster, ActiveCardinalRite rite) {
			return SuccessionRites.chunksReady(level, rite) ? Admission.TICK : Admission.WAIT;
		}

		@Override
		Course runOwnCourse(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
				@Nullable CardinalRiteRecipe recipe) {
			if (SuccessionRites.tick(level, caster, rite, recipe)) return Course.RUNNING;
			if (rite.isComplete()) {
				CardinalRiteTermination.end(level, caster, rite, Outcome.COMPLETED, CardinalRiteCulmination.BINDING.id());
			} else {
				CardinalRiteTermination.end(level, caster, rite, Outcome.COLLAPSED);
			}
			return Course.ENDED;
		}

		@Override
		void release(ServerLevel level, ActiveCardinalRite rite) {
			SuccessionRites.cleanup(level, rite);
		}
	},

	/** A station is upgraded in place; it waits for its caster and pauses on a disturbed floor. */
	STATION_UPGRADE {
		@Override
		boolean matches(ActiveCardinalRite rite, @Nullable CardinalRiteRecipe recipe) {
			return StationUpgradeRites.isRite(rite.getRecipeId());
		}

		@Override
		Admission admit(ServerLevel level, @Nullable ServerPlayer caster, ActiveCardinalRite rite) {
			if (!level.hasChunkAt(rite.getCenterPos()) || !level.hasChunkAt(StationUpgradeRites.seat(rite))) {
				return Admission.WAIT;
			}
			if (!StationUpgradeRites.focusPresent(level, rite) || !StationUpgradeRites.subjectPresent(level, rite)) {
				return Admission.BREAK;
			}
			return waitsForCaster(level, caster);
		}

		@Override
		boolean isWorking() {
			return true;
		}

		@Override
		boolean holdsOnlyItsStructure(ActiveCardinalRite rite) {
			return true;
		}

		@Override
		boolean holdsBeforeFinale() {
			return false;
		}

		@Override
		boolean complete(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
			boolean completed = StationUpgradeRites.complete(level, rite);
			if (completed) CardinalRiteSavedData.get(level.getServer().overworld()).deliverRecovery(caster);
			return completed;
		}

		@Override
		void returnEscrow(ServerLevel level, ActiveCardinalRite rite) {
			StationUpgradeRites.recover(level, rite);
		}

		@Override
		void release(ServerLevel level, ActiveCardinalRite rite) {
			StationUpgradeRites.cleanup(level, rite);
		}
	},

	/** A machine or construct is raised from its built subject; it waits for its caster and fails if the subject breaks. */
	WORKING_CRAFT {
		@Override
		boolean matches(ActiveCardinalRite rite, @Nullable CardinalRiteRecipe recipe) {
			return WorkingCraftRites.isRite(rite);
		}

		@Override
		Admission admit(ServerLevel level, @Nullable ServerPlayer caster, ActiveCardinalRite rite) {
			if (!level.hasChunkAt(rite.getCenterPos())) return Admission.WAIT;
			if (!StationUpgradeRites.focusPresent(level, rite)) return Admission.BREAK;
			return waitsForCaster(level, caster);
		}

		@Override
		boolean isWorking() {
			return true;
		}

		@Override
		boolean intact(ServerLevel level, ActiveCardinalRite rite) {
			return WorkingCraftRites.subjectIntact(level, rite);
		}

		@Override
		boolean holdsOnlyItsStructure(ActiveCardinalRite rite) {
			return true;
		}

		@Override
		boolean complete(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
			// Working crafts are Harbinger rites: the final gate is the Harbinger refusal check.
			return CardinalRiteCompletion.passesFinalGates(level, caster, rite, recipe)
					&& WorkingCraftRites.complete(level, caster, rite, recipe);
		}

		@Override
		void returnEscrow(ServerLevel level, ActiveCardinalRite rite) {
			WorkingCraftRites.recover(level, rite);
		}
	},

	/** A Puppeteer trial: a summoned puppet must be beaten inside the boundary before the ceremony may close. */
	PUPPETEER_TRIAL {
		@Override
		boolean matches(ActiveCardinalRite rite, @Nullable CardinalRiteRecipe recipe) {
			return recipe != null && recipe.isPuppeteerTrial();
		}

		@Override
		boolean capturesOfferingsAsItRuns() {
			return false;
		}

		@Override
		boolean holdsBeforeFinale() {
			return false;
		}

		@Override
		void advanceCeremony(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
			if (rite.getPhase() == CardinalRitePhase.PUPPET_TRIAL) {
				PuppeteerTrialRiteController.tick(level, caster, rite, recipe);
			} else {
				super.advanceCeremony(level, caster, rite, recipe);
			}
		}

		@Override
		boolean complete(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
			return CardinalRiteCompletion.resolve(level, caster, rite, recipe, CardinalRiteCompletion::claimPuppeteerTrial);
		}
	},

	/** A legacy or Unstained rite without an interactive ceremony: it counts down to its resolution. */
	COUNTDOWN {
		@Override
		boolean matches(ActiveCardinalRite rite, @Nullable CardinalRiteRecipe recipe) {
			return countsDown(rite, recipe);
		}
	},

	/** Every other rite: an interactive Harbinger ceremony of anchors, sigils, ordeals and a finale. */
	CEREMONY {
		@Override
		boolean matches(ActiveCardinalRite rite, @Nullable CardinalRiteRecipe recipe) {
			return true;
		}
	};

	/** Whether a rite may tick this tick. */
	enum Admission {
		TICK,
		/** Skip this tick and keep the rite. */
		WAIT,
		/** Its focus or subject is gone: end it as broken. */
		BREAK
	}

	/** What a kind that runs its own course did this tick. */
	enum Course {
		/** The kind follows the shared ceremony course. */
		SHARED,
		/** It ran its own course and goes on. */
		RUNNING,
		/** It ran its own course and ended itself. */
		ENDED
	}

	private static final RiteKind[] RESOLUTION_ORDER = values();

	/** The rite's kind, from its saved state and its recipe (which may be missing). */
	public static RiteKind of(ActiveCardinalRite rite, @Nullable CardinalRiteRecipe recipe) {
		for (RiteKind kind : RESOLUTION_ORDER) {
			if (kind.matches(rite, recipe)) return kind;
		}
		return CEREMONY;
	}

	/** A legacy or Unstained rite, or one whose recipe is gone, counts down rather than running a ceremony. */
	static boolean countsDown(ActiveCardinalRite rite, @Nullable CardinalRiteRecipe recipe) {
		return rite.getPhase() == CardinalRitePhase.LEGACY || recipe == null || !recipe.hasInteractiveCeremony();
	}

	abstract boolean matches(ActiveCardinalRite rite, @Nullable CardinalRiteRecipe recipe);

	/** Checked before anything else each tick, with the caster when they are online. */
	Admission admit(ServerLevel level, @Nullable ServerPlayer caster, ActiveCardinalRite rite) {
		return Admission.TICK;
	}

	/**
	 * A working rite (a station upgrade or a Working craft) pauses while its caster is away, outside the leash or its
	 * floor is disturbed, instead of binding the caster in blood; it lets go without collapse backlash, and waits
	 * rather than fails when its structure is incomplete at the finish.
	 */
	boolean isWorking() {
		return false;
	}

	/** A working rite's own once-a-second check while it runs; false fails the rite. */
	boolean intact(ServerLevel level, ActiveCardinalRite rite) {
		return true;
	}

	/** Whether a layered station that matched late (its offerings not yet captured) records them as it runs. */
	boolean capturesOfferingsAsItRuns() {
		return true;
	}

	/**
	 * Whether only the floor and upper structure must still stand, not the braziers' offerings: always for a working
	 * rite, and for any rite whose Puppeteer trial puppet has manifested.
	 */
	boolean holdsOnlyItsStructure(ActiveCardinalRite rite) {
		return rite.puppeteerTrial().isManifested();
	}

	/** Whether a finale that cannot resolve is held in a still interval before any offering is drawn. */
	boolean holdsBeforeFinale() {
		return true;
	}

	/** A kind with its own course runs it here, after the cancellation and leash checks. */
	Course runOwnCourse(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			@Nullable CardinalRiteRecipe recipe) {
		return Course.SHARED;
	}

	/**
	 * Advances the rite one tick: a countdown ticks down; a ceremony checks its structure once a second and runs its
	 * current phase.
	 *
	 * @return false when the structure was found broken; the caller fails the rite
	 */
	final boolean advance(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			@Nullable CardinalRiteRecipe recipe, long gameTime) {
		if (countsDown(rite, recipe)) {
			rite.tick();
			return true;
		}
		if (gameTime % 20 == 0 && !CardinalRiteStructure.verify(level, rite)) return false;
		advanceCeremony(level, caster, rite, recipe);
		return true;
	}

	void advanceCeremony(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		CardinalRiteOrdealEngine.tick(level, caster, rite, recipe);
	}

	/**
	 * Resolves the finished rite.
	 *
	 * @return false when a final check refused it; nothing was granted
	 */
	boolean complete(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		return CardinalRiteCompletion.resolve(level, caster, rite, recipe, CardinalRiteCompletion.Claim.NONE);
	}

	/** Returns what an unfinished rite escrowed. Called before the brazier displays are cleared. */
	void returnEscrow(ServerLevel level, ActiveCardinalRite rite) {
	}

	/** Releases whatever else the rite held, however it ended. */
	void release(ServerLevel level, ActiveCardinalRite rite) {
	}

	private static Admission waitsForCaster(ServerLevel level, @Nullable ServerPlayer caster) {
		return caster == null || caster.level() != level || !caster.isAlive() ? Admission.WAIT : Admission.TICK;
	}
}
