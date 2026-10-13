package com.vincenthuto.hemomancy.common.rite.floor;

/**
 * Timeline and palette for a consecrated Cardinal Rite floor. The server only decides that a floor is formed; this
 * shapes how the client shows it: a membrane spreading out from the Focus, then a slow heartbeat across the floor.
 */
public final class CardinalRiteFloorVisualRules {
	/** Ticks for the membrane to spread from the Focus to the floor's corners. */
	public static final int FORM_TICKS = 70;
	/** Ticks for a broken floor's membrane to draw back into the Focus: formation played in reverse. */
	public static final int COLLAPSE_TICKS = FORM_TICKS;
	/** Ticks between heartbeats once the floor has settled. */
	public static final int HEARTBEAT_TICKS = 36;
	/** Extra reach past the corner cell so the ragged front clears the edge lobes that spill past the floor. */
	public static final float REACH_MARGIN = 2.2F;

	public static final int STYLE_THRESHOLD = 0;
	public static final int STYLE_DOMINION = 1;
	public static final int STYLE_COMMUNION = 2;
	public static final int STYLE_WORKING = 3;

	private static final Palette[] PALETTES = {
			// Threshold: arterial red over iron-dark crust.
			new Palette(0xFF2A0A0C, 0xFFB3141E, 0xFFFF8A72),
			// Dominion: near-black clot with deep venous seams.
			new Palette(0xFF120507, 0xFF7A1020, 0xFFE0303C),
			// Communion: warm flesh membrane laced with capillaries.
			new Palette(0xFF4A1A1E, 0xFFC23A44, 0xFFFFC2B0),
			// Working: ivory bone fibre tied with red sinew.
			new Palette(0xFF6B5A4C, 0xFF9E1622, 0xFFFFD2C4),
	};

	private CardinalRiteFloorVisualRules() {
	}

	public static int styleIndex(String style) {
		if (style == null) return STYLE_THRESHOLD;
		return switch (style) {
			case "dominion" -> STYLE_DOMINION;
			case "communion" -> STYLE_COMMUNION;
			case "working" -> STYLE_WORKING;
			default -> STYLE_THRESHOLD;
		};
	}

	public static Palette palette(int styleIndex) {
		return PALETTES[Math.floorMod(styleIndex, PALETTES.length)];
	}

	/** Distance from the Focus centre to the far corner of a square floor, plus the settling margin. */
	public static float reach(int floorHalfWidth) {
		return (float) ((Math.max(0, floorHalfWidth) + 0.5D) * Math.sqrt(2.0D)) + REACH_MARGIN;
	}

	/** 0 at formation, 1 once the membrane has covered the floor; growth eases in and settles. */
	public static float formProgress(float ticksSinceFormed) {
		return smoothstep(clamp01(ticksSinceFormed / FORM_TICKS));
	}

	public static float spreadRadius(float ticksSinceFormed, int floorHalfWidth) {
		return formProgress(ticksSinceFormed) * reach(floorHalfWidth);
	}

	/** Brightness of the spreading edge: strongest mid-growth, gone once the floor settles. */
	public static float frontIntensity(float ticksSinceFormed) {
		float t = clamp01(ticksSinceFormed / FORM_TICKS);
		return (float) Math.sin(Math.PI * t) * (1.0F - t * 0.35F);
	}

	/** The braid around the Focus mouth swells in over the first second of formation, before the membrane settles. */
	public static float braidGrowth(float ticksSinceFormed) {
		return smoothstep(clamp01(ticksSinceFormed / 20.0F));
	}

	/** Width, in radians, of the arc over which a braid strand sinks into the floor. */
	public static final float BRAID_DIVE_WIDTH = 0.6F;

	/**
	 * Where the Focus braid's strands sink into the floor: strand {@code i} of four dives at {@code i * PI/4} and
	 * opposite it, so the eight dives are evenly spaced. The membrane shader draws each strand's tributary from the
	 * same angles, so keep the two in step.
	 *
	 * @return 0 away from a dive, rising to 1 at its deepest point
	 */
	public static float braidDive(int strand, float theta) {
		float deepest = 0.0F;
		for (int side = 0; side < 2; side++) {
			double centre = strand * Math.PI / 4.0D + side * Math.PI;
			double offset = Math.abs(Math.IEEEremainder(theta - centre, Math.PI * 2.0D));
			if (offset >= BRAID_DIVE_WIDTH) continue;
			float t = (float) (offset / BRAID_DIVE_WIDTH);
			deepest = Math.max(deepest, (1.0F - t * t) * (1.0F - t * t));
		}
		return deepest;
	}

	/** The mound's inner lip, just inside the Focus basin's square opening, where the iron rim sits. */
	public static final float MOUND_INNER = 0.40F;
	/**
	 * Where the mound settles back to the floor. The membrane shader cuts the flat surface away inside this radius so
	 * the two never stack; keep its MOUND_OUTER in step.
	 */
	public static final float MOUND_OUTER = 1.45F;
	private static final float MOUND_LIP = 0.035F;
	private static final float MOUND_PEAK = 0.11F;
	private static final float MOUND_CREST = 0.58F;

	/**
	 * Height of the swelling the Focus braid sits in, above the flat membrane: a lip at the iron rim, a crest under
	 * the braid, and an easing back to the floor. It is lumpy around its circumference and bulges where the braid's
	 * strands dive in.
	 */
	public static float moundHeight(float radius, float theta) {
		if (radius >= MOUND_OUTER) return 0.0F;
		float profile;
		if (radius <= MOUND_CREST) {
			float t = clamp01((radius - MOUND_INNER) / (MOUND_CREST - MOUND_INNER));
			profile = MOUND_LIP + (MOUND_PEAK - MOUND_LIP) * smoothstep(t);
		} else {
			float t = (radius - MOUND_CREST) / (MOUND_OUTER - MOUND_CREST);
			profile = MOUND_PEAK * 0.5F * (1.0F + (float) Math.cos(Math.PI * t));
		}
		float lumps = 1.0F + 0.18F * (float) Math.sin(theta * 3.0F + 0.7F) + 0.08F * (float) Math.sin(theta * 7.0F);
		float dive = 0.0F;
		for (int strand = 0; strand < 4; strand++) dive = Math.max(dive, braidDive(strand, theta));
		float divePoint = (radius - 0.72F) / 0.22F;
		float swell = 0.035F * dive * (float) Math.exp(-divePoint * divePoint);
		return profile * lumps + swell * (1.0F - smoothstep(clamp01((radius - 1.1F) / 0.35F)));
	}

	/**
	 * A collapsing floor replays its formation backwards: this is the formation age it shows, falling from fully formed
	 * to nothing over {@link #COLLAPSE_TICKS}.
	 */
	public static float collapseAge(float ticksSinceBroken) {
		return FORM_TICKS * (1.0F - clamp01(ticksSinceBroken / COLLAPSE_TICKS));
	}

	public static boolean collapsed(float ticksSinceBroken) {
		return ticksSinceBroken >= COLLAPSE_TICKS;
	}

	/** How risen a seated brazier's mound is, 0 to 1, as the membrane's edge reaches and covers it. */
	public static float seatRise(float spread, float seatDistance) {
		return smoothstep(clamp01((spread - seatDistance + 0.2F) / 0.9F));
	}

	/** A lub-dub pulse in [0,1]: a strong beat, a softer echo, then rest. */
	public static float heartbeat(float time) {
		float phase = (float) (Math.floorMod((long) Math.floor(time * 100.0D), HEARTBEAT_TICKS * 100L) / 100.0D)
				/ HEARTBEAT_TICKS;
		return beat(phase, 0.0F, 0.09F) + 0.6F * beat(phase, 0.17F, 0.08F);
	}

	/** Ticks for the dig front to carve one point of the brazier trench from a closed seam to full depth. */
	public static final float DIG_TICKS = 6.0F;

	/**
	 * How far the brazier trench has been carved at a point along its loop, 0 to 1. A dig front runs once around the
	 * loop in the second half of formation; a collapse, replaying formation backwards, fills it in the other way.
	 */
	public static float digProgress(float ticksSinceFormed, int sample, int samples) {
		float start = FORM_TICKS * 0.5F + (samples <= 0 ? 0.0F : FORM_TICKS * 0.4F * sample / (float) samples);
		return smoothstep(clamp01((ticksSinceFormed - start) / DIG_TICKS));
	}

	/** How brightly the dig front glows where the trench is mid-carve: nothing before it or once dug. */
	public static float digGlow(float progress) {
		return 4.0F * progress * (1.0F - progress);
	}

	private static float beat(float phase, float centre, float width) {
		float distance = Math.abs(phase - centre) / width;
		return distance >= 1.0F ? 0.0F : 1.0F - smoothstep(distance);
	}

	static float smoothstep(float t) {
		return t * t * (3.0F - 2.0F * t);
	}

	static float clamp01(float value) {
		return value < 0.0F ? 0.0F : Math.min(1.0F, value);
	}

	/** ARGB colours: the membrane base, the vein network, and the hot edge or socket glow. */
	public record Palette(int base, int vein, int glow) {
	}
}
