package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

/**
 * Every number the lash's feel hangs on, in one place so a review client can swap the set while the game runs
 * ({@link LashRules#setTuning}). Angles are degrees, speeds are a tick's worth.
 *
 * @param flickTurn       whip-aim turn in one tick that counts as a flick
 * @param restTurn        turn below which the aim is at rest
 * @param restTicksToFade ticks at rest before a live lash starts to fade
 * @param fadeTicks       ticks a full lash takes to dissolve back into the hand
 * @param regrowTicks     ticks a revived lash takes to reach full length again
 * @param arcDegrees      the throw's arc, centred on the look
 * @param throwTicks      ticks the throw takes to cross its arc
 * @param damping         share of its velocity a node keeps each tick
 * @param aimPull         share of the way to the aim line the tip is pulled each tick
 * @param maxNodeSpeed    the most a node may travel in a tick, in blocks
 * @param slackSpeed      tip speed below which a sweep does not hurt
 * @param fullSpeed       tip speed at which a sweep hits for the cap
 * @param crackSpeed      tip speed a reversal or a stroke's end needs to crack
 * @param cameraCap       the most the view may turn in a tick while the lash is out
 * @param cameraDrag      share of its turn rate the view keeps each tick once the hand stops
 * @param maxAimOffset    how far the whip aim may sit from the caster's look, as the server allows
 * @param maxAimStep      how far the server lets the whip aim move in a tick
 * @param releaseMinCharge how full the orb must be before the attack button will let it go
 * @param maxLead         how far to either side the casting client lets the whip aim lead the view (less up and down)
 * @param leadKeep        share of its lead over the view the whip aim keeps each tick: the rest comes back to the crosshair
 */
public record LashTuning(double flickTurn, double restTurn, int restTicksToFade, int fadeTicks, int regrowTicks,
		double arcDegrees, int throwTicks, double damping, double aimPull, double maxNodeSpeed, double slackSpeed,
		double fullSpeed, double crackSpeed, double cameraCap, double cameraDrag, double maxAimOffset, double maxAimStep,
		double releaseMinCharge, double maxLead, double leadKeep) {
	public static final LashTuning DEFAULT = new LashTuning(6.0D, 1.5D, 6, 30, 4, 60.0D, 4, 0.92D, 0.2D, 4.0D, 0.35D,
			1.0D, 0.9D, 4.0D, 0.6D, 85.0D, 60.0D, 0.5D, 70.0D, 0.88D);

	public LashTuning {
		restTicksToFade = Math.max(1, restTicksToFade);
		fadeTicks = Math.max(1, fadeTicks);
		regrowTicks = Math.max(1, regrowTicks);
		throwTicks = Math.max(1, throwTicks);
		damping = Math.max(0.0D, Math.min(0.99D, damping));
		aimPull = Math.max(0.01D, Math.min(1.0D, aimPull));
		maxNodeSpeed = Math.max(0.1D, maxNodeSpeed);
		fullSpeed = Math.max(slackSpeed + 1.0E-3D, fullSpeed);
		cameraDrag = Math.max(0.0D, Math.min(0.99D, cameraDrag));
		releaseMinCharge = Math.max(0.0D, Math.min(1.0D, releaseMinCharge));
		maxLead = Math.max(0.0D, Math.min(maxAimOffset, maxLead));
		leadKeep = Math.max(0.0D, Math.min(0.99D, leadKeep));
	}
}
