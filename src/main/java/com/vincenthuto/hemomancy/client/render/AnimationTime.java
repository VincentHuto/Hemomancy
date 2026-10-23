package com.vincenthuto.hemomancy.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Animation time for client code: game time or an entity's age plus the frame's partial tick. Each method is the exact
 * expression it replaced, so a float partial tick gives float arithmetic and a double one double. Code that animates on
 * wall-clock time, or counts frames, does so on purpose until decision A-4 settles which animations may freeze under
 * {@code /tick freeze}; render events and HUD layers pass their own partial tick.
 */
public final class AnimationTime {
	private AnimationTime() {
	}

	/** The frame's partial tick; it reads 1 while the game's ticking is frozen. */
	public static float partialTick() {
		return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
	}

	/** The frame's partial tick, which keeps moving while the game's ticking is frozen. */
	public static float partialTickWhileFrozen() {
		return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
	}

	public static float gameTime(Level level, float partialTick) {
		return level.getGameTime() + partialTick;
	}

	public static double gameTime(Level level, double partialTick) {
		return level.getGameTime() + partialTick;
	}

	/** Game time at this frame's {@link #partialTick()}. */
	public static float gameTime(Level level) {
		return level.getGameTime() + partialTick();
	}

	/** Game time at this frame's {@link #partialTick()}, added in double precision. */
	public static double gameTimeAsDouble(Level level) {
		return level.getGameTime() + (double) partialTick();
	}

	/** An entity's age in ticks at a partial tick. */
	public static float age(Entity entity, float partialTick) {
		return entity.tickCount + partialTick;
	}

	public static double age(Entity entity, double partialTick) {
		return entity.tickCount + partialTick;
	}
}
