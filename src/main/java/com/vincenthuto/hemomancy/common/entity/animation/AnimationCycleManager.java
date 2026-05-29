package com.vincenthuto.hemomancy.common.entity.animation;

import net.minecraft.world.entity.AnimationState;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic animation cycle completion system for any entity.
 * Manages smooth animation transitions by allowing animations to finish their current cycle
 * before switching to another animation, rather than snapping immediately.
 *
 * Usage:
 * 1. Create an instance in your entity: {@code animationManager = new AnimationCycleManager();}
 * 2. Toggle on/off anytime: {@code animationManager.setForceFinish(true/false);}
 * 3. In your tick() method, use the helper methods to manage animations.
 *
 * Example:
 * <pre>
 * if (shouldWalk) {
 *     animationManager.startAnimation(walkState, "walk", 20);
 * } else if (animationManager.isAnimationFinishing("walk")) {
 *     animationManager.updateAnimationFinish("walk");
 *     if (!animationManager.isAnimationFinishing("walk")) {
 *         walkState.stop();
 *         idleState.start(tickCount);
 *     }
 * } else if (walkState.isStarted()) {
 *     animationManager.initiateAnimationFinish("walk", 20);
 * }
 * </pre>
 */
public class AnimationCycleManager {

	/**
	 * Global toggle to enable/disable animation cycle completion.
	 * When true, animations will finish their current cycle before transitioning.
	 * When false, animations will stop immediately (snap behavior).
	 */
	private boolean forceAnimationFinish = true;

	/**
	 * Tracks animation state for cycle completion.
	 */
	private static class AnimationTracker {
		int startTick;
		int cycleLength;
		boolean isFinishing;
		int finishCountdown;
	}

	/**
	 * Maps animation name to its cycle tracking data.
	 */
	private final Map<String, AnimationTracker> animationTrackers = new HashMap<>();

	/**
	 * Set whether animations should finish their cycles before transitioning.
	 *
	 * @param forceFinish true to finish cycles, false to snap immediately
	 */
	public void setForceFinish(boolean forceFinish) {
		this.forceAnimationFinish = forceFinish;
	}

	/**
	 * Check if animation cycle completion is enabled.
	 *
	 * @return true if animations finish their cycles
	 */
	public boolean isForceFinishEnabled() {
		return forceAnimationFinish;
	}

	/**
	 * Start an animation and register it for cycle tracking.
	 *
	 * @param animationState     The AnimationState to start
	 * @param animationName      Unique name for this animation (e.g., "walk", "attack", "spell")
	 * @param cycleLengthTicks   The length of one animation cycle in ticks
	 * @param currentTickCount   Current game tick (usually entity.tickCount)
	 */
	public void startAnimation(AnimationState animationState, String animationName, int cycleLengthTicks, int currentTickCount) {
		if (!animationState.isStarted()) {
			animationState.start(currentTickCount);
			AnimationTracker tracker = new AnimationTracker();
			tracker.startTick = currentTickCount;
			tracker.cycleLength = cycleLengthTicks;
			tracker.isFinishing = false;
			animationTrackers.put(animationName, tracker);
		}
	}

	/**
	 * Cancel any pending finish countdown for an animation.
	 * Call this when the entity wants to continue or restart an animation.
	 *
	 * @param animationName Unique name for the animation
	 */
	public void resetAnimationFinish(String animationName) {
		AnimationTracker tracker = animationTrackers.get(animationName);
		if (tracker != null) {
			tracker.isFinishing = false;
		}
	}

	/**
	 * Initiate the finish sequence for an animation.
	 * Calculates how many ticks remain in the current cycle and sets a countdown.
	 * If forceAnimationFinish is disabled, stops the animation immediately.
	 *
	 * @param animationName    Unique name for the animation
	 * @param cycleLengthTicks The length of one animation cycle in ticks
	 * @param currentTickCount Current game tick (usually entity.tickCount)
	 */
	public void initiateAnimationFinish(String animationName, int cycleLengthTicks, int currentTickCount) {
		if (!forceAnimationFinish) {
			// If toggle is off, stop animation immediately
			animationTrackers.remove(animationName);
			return;
		}

		AnimationTracker tracker = animationTrackers.get(animationName);
		if (tracker == null) {
			tracker = new AnimationTracker();
			animationTrackers.put(animationName, tracker);
		}

		int posInCycle = (currentTickCount - tracker.startTick) % cycleLengthTicks;
		if (posInCycle == 0) {
			// Already at cycle boundary, no need to finish
			tracker.isFinishing = false;
			animationTrackers.remove(animationName);
		} else {
			// Let cycle finish
			tracker.finishCountdown = cycleLengthTicks - posInCycle;
			tracker.isFinishing = true;
		}
	}

	/**
	 * Check if an animation is currently in the finish sequence.
	 *
	 * @param animationName Unique name for the animation
	 * @return true if the animation is waiting to finish its cycle
	 */
	public boolean isAnimationFinishing(String animationName) {
		AnimationTracker tracker = animationTrackers.get(animationName);
		return tracker != null && tracker.isFinishing;
	}

	/**
	 * Update the finish countdown for an animation.
	 * Call this every tick while the animation is finishing.
	 * The countdown decrements, and when it reaches 0, the finish state is cleared.
	 *
	 * @param animationName Unique name for the animation
	 */
	public void updateAnimationFinish(String animationName) {
		AnimationTracker tracker = animationTrackers.get(animationName);
		if (tracker != null && tracker.isFinishing) {
			tracker.finishCountdown--;
			if (tracker.finishCountdown <= 0) {
				tracker.isFinishing = false;
			}
		}
	}

	/**
	 * Clear all animation tracking data.
	 * Useful if you need to reset the manager or when an entity dies.
	 */
	public void clearAllAnimations() {
		animationTrackers.clear();
	}
}

