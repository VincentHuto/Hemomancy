package com.vincenthuto.hemomancy.client.render.cinematic;

/** Offsets on the player's own view, in degrees, and a shake amplitude (two detuned waves, scaled by the director). */
public record CameraOffsets(float roll, float pitch, float yaw, float shake) {
	public static final CameraOffsets NONE = new CameraOffsets(0.0F, 0.0F, 0.0F, 0.0F);

	public static CameraOffsets shake(float amplitude) {
		return new CameraOffsets(0.0F, 0.0F, 0.0F, amplitude);
	}
}
