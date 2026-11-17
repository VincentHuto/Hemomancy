package com.vincenthuto.hemomancy.client.render;

import javax.annotation.Nullable;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Animation time for client code: game time or an entity's age plus the frame's partial tick. Each method is the exact
 * expression it replaced, so a float partial tick gives float arithmetic and a double one double. Every animation in the
 * world, on an item or on armour freezes under {@code /tick freeze} (A-4), inventory slots included; only screen chrome,
 * the dimension veil, pose cache stamps and the rite bar's ease read the wall clock. Render events and HUD layers pass
 * their own partial tick.
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

	/** Game ticks at a partial tick, or the wall clock in ticks when nothing is loaded (an item drawn on the title screen). */
	public static double ticksOrWallClock(@Nullable Level level, float partialTick) {
		return level != null ? level.getGameTime() + (double) partialTick : Util.getMillis() / 50.0;
	}

	/** Item and armour animation time in milliseconds: game time at this frame, in a slot as in a hand. */
	public static double itemMillis() {
		return ticksOrWallClock(Minecraft.getInstance().level, partialTick()) * 50.0;
	}
}
