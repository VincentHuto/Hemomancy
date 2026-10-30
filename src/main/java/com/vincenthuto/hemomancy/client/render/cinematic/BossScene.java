package com.vincenthuto.hemomancy.client.render.cinematic;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;

/**
 * One boss scene as the client plays it: a length in ticks and a set of tracks, each a function of scene time
 * {@code t} (ticks plus the partial tick). The director reads the tracks and does the work: letterbox and fade, hiding
 * the HUD, holding input, the title card, a cinematic shot or first-person camera offsets, and the scene's own screen
 * art. The server's timing decides what the boss can do; a scene only presents it.
 */
public interface BossScene {
	/**
	 * Ticks a scene hands the player back before the boss's own server window ends. The client's clock starts when
	 * the packet lands, so a scene that ended exactly with the window would still hold the player after the boss could
	 * act; this covers the delay.
	 */
	int HANDBACK = 8;

	int length();

	/** 0..1: how far the letterbox bars are in. */
	default float letterbox(float t) {
		return 0.0F;
	}

	/** 0..1: black over the whole view. */
	default float fade(float t) {
		return 0.0F;
	}

	default boolean hudHidden(float t) {
		return false;
	}

	default boolean inputLocked(float t) {
		return false;
	}

	/** Whether input stays held after the player skips: true while the server itself still binds them. */
	default boolean keepsInputAfterSkip(float t) {
		return false;
	}

	/** The scene's title card; {@code boss} is null until the client has the boss. */
	@Nullable
	default TitleCard card(@Nullable Entity boss) {
		return null;
	}

	/** A camera move around the boss, or null to keep the player's own view. */
	@Nullable
	default CinematicShot shot() {
		return null;
	}

	/** Whether the shot holds the camera at {@code t}; outside it the player's own view (with offsets) is used. */
	default boolean shotActive(float t) {
		return true;
	}

	/** Angle offsets on the player's own view, in degrees, plus a shake amplitude. */
	default CameraOffsets offsets(float t) {
		return CameraOffsets.NONE;
	}

	/** Once per tick, for one-shot cues (sounds, held poses). */
	default void tick(int t) {
	}

	/** Called once when the player skips the scene, for a scene whose server sequence follows the skip. */
	default void skipped() {
	}

	/** Called once when the scene ends, is replaced or is cleared, to undo anything {@link #tick} holds. */
	default void end() {
	}

	/** The scene's own screen art, drawn under the letterbox and card. */
	default void overlay(GuiGraphics graphics, int width, int height, float t, float time) {
	}

	default boolean skippable(float t) {
		return true;
	}

	/** Whether the scene carries on through a respawn or dimension change, as a capture that ends in a transfer does. */
	default boolean keepsAcrossDimensions() {
		return false;
	}
}
