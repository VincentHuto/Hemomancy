package com.vincenthuto.hemomancy.client.render.world.circus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgressRules.Stage;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * What the phantom show looks like at each acclimation stage: a newcomer catches unsettling fragments, an attuned
 * visitor sits in the audience of the whole performance (the owner's arc, 2026-11-16). A ruined pavilion is a hollow
 * echo, the Disturbed look with no performers, whatever the stage. Pure numbers, read by the client drawing only.
 */
public final class PhantomShowRules {
	/** The crosshair cone a Disturbed fragment refuses to be seen in, and where it is fully back. */
	private static final float GLANCE_HIDDEN = 20.0F;
	private static final float GLANCE_SEEN = 35.0F;
	private static final double TOUCH_HIDDEN = 1.5D;
	private static final double TOUCH_SEEN = 3.0D;
	/** The fog never closes nearer than this, so the ring and anything in it stays readable. */
	private static final float FOG_FLOOR = 32.0F;
	/** How near a phantom may stand to a creature, and the half size of a phantom's body for the line of sight. */
	private static final double TOUCH = 0.8D;
	private static final double BODY_HALF_WIDTH = 0.65D;
	private static final double BODY_HALF_HEIGHT = 1.3D;

	private static final Look NONE = new Look(0, 0, 0, 0, 0, false, 0, 0, 0, 0, false);
	private static final Look DISTURBED = new Look(0.0F, 3, 1, 0.25F, 0.15F, false, 0, 0.2F, 0.25F, 0.18F, true);
	private static final Look ACCLIMATING = new Look(0.33F, 0, 1, 0.8F, 0.3F, false, 1, 0.55F, 0.35F, 0.24F, false);
	private static final Look ATTUNED = new Look(1.0F, 0, 3, 1.0F, 0.0F, true, 3, 0.85F, 0.5F, 0.3F, false);
	private static final Look RUIN = new Look(0.0F, 3, 0, 0.25F, 0.15F, false, 0, 0.2F, 0.25F, 0.18F, true);

	private PhantomShowRules() {
	}

	/**
	 * @param audienceShare the share of seats a phantom sits in
	 * @param glimpses      Disturbed fragments: single phantoms in seats that hide from the crosshair
	 * @param performers    phantom performers at once (a Disturbed one is a brief, still glimpse)
	 * @param bulbLit       the share of string-light bulbs that are not dead
	 * @param flicker       the share of bulbs that flicker
	 * @param chase         whether the lit bulbs run a chase on an act's beat
	 * @param spotlights    cones on the ring
	 * @param house         0 no darkening to 1 a dark house
	 * @param vignette      the screen vignette's strength
	 * @param phantomAlpha  a phantom's alpha before fades, never opaque
	 * @param peripheralOnly whether phantoms hide from the crosshair
	 */
	public record Look(float audienceShare, int glimpses, int performers, float bulbLit, float flicker, boolean chase,
			int spotlights, float house, float vignette, float phantomAlpha, boolean peripheralOnly) {
	}

	public enum Bulb {
		DEAD, FLICKER, LIT
	}

	public static Look look(Stage stage, boolean ruined) {
		if (!stage.hasPresentation()) return NONE;
		if (ruined) return RUIN;
		return switch (stage) {
			case DISTURBED -> DISTURBED;
			case ACCLIMATING -> ACCLIMATING;
			default -> ATTUNED;
		};
	}

	/** Numbers lerp; counts and switches take the new look from halfway. */
	public static Look blend(Look from, Look to, float t) {
		if (t <= 0.0F) return from;
		if (t >= 1.0F) return to;
		Look discrete = t < 0.5F ? from : to;
		return new Look(HemoEasing.lerp(from.audienceShare(), to.audienceShare(), t), discrete.glimpses(),
				discrete.performers(), HemoEasing.lerp(from.bulbLit(), to.bulbLit(), t),
				HemoEasing.lerp(from.flicker(), to.flicker(), t), discrete.chase(), discrete.spotlights(),
				HemoEasing.lerp(from.house(), to.house(), t), HemoEasing.lerp(from.vignette(), to.vignette(), t),
				HemoEasing.lerp(from.phantomAlpha(), to.phantomAlpha(), t), discrete.peripheralOnly());
	}

	public static float peripheralFade(float degreesFromCrosshair) {
		return HemoEasing.smoothstep(GLANCE_HIDDEN, GLANCE_SEEN, degreesFromCrosshair);
	}

	public static float nearFade(double distance) {
		return (float) HemoEasing.smoothstep(TOUCH_HIDDEN, TOUCH_SEEN, distance);
	}

	/** A bulb's state from its seed: the dead share first, then the flickering share, the rest lit. */
	public static Bulb bulb(Look look, long seed) {
		float roll = unit(seed);
		if (roll < 1.0F - look.bulbLit()) return Bulb.DEAD;
		if (roll < 1.0F - look.bulbLit() + look.flicker()) return Bulb.FLICKER;
		return Bulb.LIT;
	}

	public static float fogFar(float vanillaFar, float house, double darknessScale) {
		float target = Math.max(FOG_FLOOR, vanillaFar * 0.45F);
		if (target >= vanillaFar) return vanillaFar;
		float amount = HemoEasing.clamp01(house * (float) darknessScale);
		return HemoEasing.lerp(vanillaFar, target, amount);
	}

	/**
	 * Whether a phantom centred at {@code phantom} would stand over a living creature: touching it, or between the eye
	 * and it (a phantom behind a creature is already hidden by depth). No allocation; called per phantom per frame.
	 */
	public static boolean covers(Vec3 camera, Vec3 phantom, AABB creature) {
		double m = TOUCH;
		if (phantom.x > creature.minX - m && phantom.x < creature.maxX + m && phantom.y > creature.minY - m
				&& phantom.y < creature.maxY + m && phantom.z > creature.minZ - m && phantom.z < creature.maxZ + m)
			return true;
		double tx = (creature.minX + creature.maxX) * 0.5D, ty = (creature.minY + creature.maxY) * 0.5D,
				tz = (creature.minZ + creature.maxZ) * 0.5D;
		double dx = tx - camera.x, dy = ty - camera.y, dz = tz - camera.z;
		double length2 = dx * dx + dy * dy + dz * dz;
		if (length2 < 1.0E-6D) return false;
		double along = ((phantom.x - camera.x) * dx + (phantom.y - camera.y) * dy + (phantom.z - camera.z) * dz) / length2;
		if (along <= 0.0D || along >= 1.0D) return false;
		double px = camera.x + dx * along - phantom.x, py = camera.y + dy * along - phantom.y,
				pz = camera.z + dz * along - phantom.z;
		return Math.abs(px) < BODY_HALF_WIDTH && Math.abs(pz) < BODY_HALF_WIDTH && Math.abs(py) < BODY_HALF_HEIGHT;
	}

	/** A seed's stable value in [0, 1). */
	public static float unit(long seed) {
		long h = seed * 0x9E3779B97F4A7C15L;
		h ^= h >>> 31;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 29;
		return (h >>> 40) / (float) (1L << 24);
	}
}
