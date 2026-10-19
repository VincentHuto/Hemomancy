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
	/** Special floors: one per special rite. */
	public static final int STYLE_APOTHEOS = 4;
	public static final int STYLE_QLIPHOTH = 5;
	public static final int STYLE_ANCESTRAL = 6;

	private static final Palette[] PALETTES = {
			// Threshold: arterial red over iron-dark crust.
			new Palette(0xFF2A0A0C, 0xFFB3141E, 0xFFFF8A72),
			// Dominion: near-black clot with deep venous seams.
			new Palette(0xFF120507, 0xFF7A1020, 0xFFE0303C),
			// Communion: warm flesh membrane laced with capillaries.
			new Palette(0xFF4A1A1E, 0xFFC23A44, 0xFFFFC2B0),
			// Working: lacquered hematic iron plates, crimson conduits, a cherry glow of blood at work.
			new Palette(0xFF1A1620, 0xFFC8102E, 0xFFFF4F6E),
			// Apotheos: the Chamber of Will's slate wall membrane, its ceiling hivemind's orange, its portal's magenta.
			new Palette(0xFF1C2430, 0xFFE0560E, 0xFFFF3CC0),
			// Qliphoth: monolith black glass, arterial fault-red, a hot red horizon glow (its violet is the corona's).
			new Palette(0xFF07030A, 0xFF8E0A16, 0xFFFF3A26),
			// Ancestral: dark amber with ochre vessels and a honey glow of remembered blood.
			new Palette(0xFF3A2410, 0xFFB0581C, 0xFFFFCC78),
	};

	private CardinalRiteFloorVisualRules() {
	}

	public static int styleIndex(String style) {
		if (style == null) return STYLE_THRESHOLD;
		return switch (style) {
			case "dominion" -> STYLE_DOMINION;
			case "communion" -> STYLE_COMMUNION;
			case "working" -> STYLE_WORKING;
			case "apotheos" -> STYLE_APOTHEOS;
			case "qliphoth" -> STYLE_QLIPHOTH;
			case "ancestral" -> STYLE_ANCESTRAL;
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

	/** The pad over the Focus mouth swells in over the first second of formation, before the membrane settles. */
	public static float padGrowth(float ticksSinceFormed) {
		return smoothstep(clamp01(ticksSinceFormed / 20.0F));
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

	// ---- The floor as the rite's body: how the membrane answers each beat of a rite held on it ----

	/** A staff strike's shock leaves the Focus and runs past the floor's edge over this many ticks. */
	public static final float SHOCK_TICKS = 20.0F;
	/** A filled anchor's blood pulse runs from the Focus out to the anchor over this many ticks. */
	public static final float SURGE_TICKS = 18.0F;
	/** A drawn offering lights its stretch of trench, then runs in across the membrane to the Focus. */
	public static final float OFFERING_TICKS = 24.0F;
	/** The membrane's flinch when a wave arrives. */
	public static final float FLINCH_TICKS = 12.0F;
	/** The flush of relief when a wave is answered. */
	public static final float RELIEF_TICKS = 40.0F;
	/** The culmination swell reaches its fullest after this many ticks of the phase. */
	public static final float CULMINATION_SWELL_TICKS = 100.0F;
	/** A completed rite's flush settles over this many ticks. */
	public static final float COMPLETION_SETTLE_TICKS = 90.0F;
	/** A grounding rite's ring runs out of the floor and this far into the surrounding ground. */
	public static final float GROUND_RING_BEYOND = 8.0F;
	public static final float GROUND_RING_TICKS = 40.0F;
	/**
	 * A collapsed or broken rite tears the membrane back toward the Focus at about three times the collapse rate,
	 * down to this share of its spread, holds a moment, then regrows: the floor itself is still formed.
	 */
	public static final float TEAR_EXTENT = 0.4F;
	public static final float TEAR_TICKS = 13.0F;
	public static final float TEAR_HOLD_TICKS = 5.0F;
	public static final float TEAR_REGROW_TICKS = 60.0F;
	/** A cancelled rite dims the membrane gently, and it recovers over this many ticks. */
	public static final float CANCEL_TICKS = 60.0F;

	/** No rite, a rite in progress, and a rite at its most demanding (inscription or an ordeal). */
	public static final int TEMPO_IDLE = 0;
	public static final int TEMPO_ACTIVE = 1;
	public static final int TEMPO_INTENSE = 2;

	/** Formation age at which the membrane has spread {@link #TEAR_EXTENT} of the way. */
	private static final float TEAR_AGE = FORM_TICKS * inverseSmoothstep(TEAR_EXTENT);

	/** How hard the floor's heart works during a rite phase, by the phase's name. */
	public static int riteTempo(String phase) {
		if (phase == null || phase.isEmpty()) return TEMPO_IDLE;
		return switch (phase) {
			case "COMPLETE", "COLLAPSED" -> TEMPO_IDLE;
			case "INSCRIPTION", "ORDEAL", "PUPPET_TRIAL", "CULMINATION" -> TEMPO_INTENSE;
			default -> TEMPO_ACTIVE;
		};
	}

	/** Heartbeat rate relative to rest: a live rite quickens it by half, a demanding phase doubles it. */
	public static float tempoRate(int tempo) {
		return tempo >= TEMPO_INTENSE ? 2.0F : tempo == TEMPO_ACTIVE ? 1.5F : 1.0F;
	}

	/** How much brighter the vessels run at a tempo. */
	public static float tempoVigor(int tempo) {
		return tempo >= TEMPO_INTENSE ? 0.55F : tempo == TEMPO_ACTIVE ? 0.3F : 0.0F;
	}

	/**
	 * The floor's heartbeat during a rite: the resting beat crossfaded toward the quickened one by {@code quick}, which
	 * leans toward the racing one by {@code intense}. The rates are 1, 1.5 and 2 times rest, so all three line up every
	 * two resting beats, and the time is reduced to that span first so large game times keep their precision.
	 */
	public static float riteHeartbeat(double time, float quick, float intense) {
		float local = (float) (time - Math.floor(time / (HEARTBEAT_TICKS * 2.0D)) * (HEARTBEAT_TICKS * 2.0D));
		float rest = heartbeat(local);
		if (quick <= 0.0F) return rest;
		float active = mix(heartbeat(local * tempoRate(TEMPO_ACTIVE)), heartbeat(local * tempoRate(TEMPO_INTENSE)),
				clamp01(intense));
		return mix(rest, active, clamp01(quick));
	}

	/** Radius of a staff strike's shock ring: it bursts from the Focus and slows as it outruns the floor's edge. */
	public static float shockRadius(float ticksSinceImpact, float reach) {
		float t = clamp01(ticksSinceImpact / SHOCK_TICKS);
		return reach * 1.15F * (1.0F - (1.0F - t) * (1.0F - t));
	}

	/** Strength of the shock: a hard attack in a tick and a half, then a long fall, gone by {@link #SHOCK_TICKS}. */
	public static float shockStrength(float ticksSinceImpact) {
		if (ticksSinceImpact < 0.0F || ticksSinceImpact >= SHOCK_TICKS) return 0.0F;
		float fall = 1.0F - ticksSinceImpact / SHOCK_TICKS;
		return clamp01(ticksSinceImpact / 1.5F) * fall * fall;
	}

	/** How far from the Focus a filled anchor's blood pulse has run toward it. */
	public static float surgeRadius(float ticksSinceFilled, float anchorDistance) {
		return anchorDistance * smoothstep(clamp01(ticksSinceFilled / SURGE_TICKS));
	}

	/** Strength of the anchor pulse: quick to rise, fading as it arrives. */
	public static float surgeStrength(float ticksSinceFilled) {
		float end = SURGE_TICKS + 6.0F;
		if (ticksSinceFilled < 0.0F || ticksSinceFilled >= end) return 0.0F;
		float release = clamp01((ticksSinceFilled - SURGE_TICKS * 0.7F) / (end - SURGE_TICKS * 0.7F));
		return clamp01(ticksSinceFilled / 2.0F) * (1.0F - smoothstep(release));
	}

	/**
	 * Glow of the trench at {@code arcDistance} blocks along its loop from the brazier an offering was drawn from: the
	 * stretch beside it lights at once and the glow runs a little way both ways along the trench as it fades.
	 */
	public static float offeringTrenchGlow(float ticksSinceDrawn, float arcDistance) {
		if (ticksSinceDrawn < 0.0F || ticksSinceDrawn >= OFFERING_TICKS) return 0.0F;
		float envelope = clamp01(ticksSinceDrawn / 2.0F) * (1.0F - smoothstep(clamp01((ticksSinceDrawn - 6.0F) / 14.0F)));
		float run = 0.6F + 2.4F * smoothstep(clamp01(ticksSinceDrawn / 10.0F));
		float beyond = Math.max(0.0F, Math.abs(arcDistance) - run);
		return envelope * (float) Math.exp(-beyond * beyond / 0.35F) * (1.0F - 0.35F * clamp01(Math.abs(arcDistance) / 3.5F));
	}

	/** Where the offering's pulse is on its run in across the membrane, from the trench to the Focus. */
	public static float offeringDrawRadius(float ticksSinceDrawn, float startRadius) {
		return startRadius * (1.0F - smoothstep(clamp01((ticksSinceDrawn - 5.0F) / (OFFERING_TICKS - 8.0F))));
	}

	/** Strength of the offering's inward pulse: it leaves the trench once that has lit, and is gone on arrival. */
	public static float offeringDrawStrength(float ticksSinceDrawn) {
		if (ticksSinceDrawn < 3.0F || ticksSinceDrawn >= OFFERING_TICKS + 4.0F) return 0.0F;
		return clamp01((ticksSinceDrawn - 3.0F) / 3.0F) * (1.0F - smoothstep(clamp01((ticksSinceDrawn - 19.0F) / 9.0F)));
	}

	/** The Focus mouth flares as the offering's pulse reaches it. */
	public static float offeringFocusFlare(float ticksSinceDrawn) {
		float arrival = OFFERING_TICKS - 3.0F;
		float distance = Math.abs(ticksSinceDrawn - arrival) / 6.0F;
		return distance >= 1.0F ? 0.0F : 1.0F - smoothstep(distance);
	}

	/** The membrane blanches for a moment when a wave arrives. */
	public static float flinchBlanch(float ticksSinceWave) {
		if (ticksSinceWave < 0.0F || ticksSinceWave >= FLINCH_TICKS) return 0.0F;
		return clamp01(ticksSinceWave / 1.5F) * (1.0F - smoothstep(ticksSinceWave / FLINCH_TICKS));
	}

	/** The flinch's contraction ripple runs in from the floor's edge to the Focus. */
	public static float flinchRippleRadius(float ticksSinceWave, float reach) {
		return reach * (1.0F - smoothstep(clamp01(ticksSinceWave / FLINCH_TICKS)));
	}

	public static float flinchRippleStrength(float ticksSinceWave) {
		if (ticksSinceWave < 0.0F || ticksSinceWave >= FLINCH_TICKS) return 0.0F;
		return (float) Math.sin(Math.PI * ticksSinceWave / FLINCH_TICKS);
	}

	/** A flush of relief through the vessels when a wave is answered. */
	public static float reliefFlush(float ticksSinceCleared) {
		if (ticksSinceCleared < 0.0F || ticksSinceCleared >= RELIEF_TICKS) return 0.0F;
		float fall = 1.0F - ticksSinceCleared / RELIEF_TICKS;
		return clamp01(ticksSinceCleared / 3.0F) * fall * fall;
	}

	/** The whole membrane swells and brightens as culmination goes on. */
	public static float culminationSwell(float ticksInCulmination) {
		return smoothstep(clamp01(ticksInCulmination / CULMINATION_SWELL_TICKS));
	}

	/** A completed rite's flush: a fast rise, then a slow settle. */
	public static float completionFlush(float ticksSinceCompleted) {
		if (ticksSinceCompleted < 0.0F || ticksSinceCompleted >= COMPLETION_SETTLE_TICKS) return 0.0F;
		return clamp01(ticksSinceCompleted / 4.0F) * (1.0F - smoothstep(ticksSinceCompleted / COMPLETION_SETTLE_TICKS));
	}

	/** The culmination's swell easing back down after completion. */
	public static float completionSwell(float ticksSinceCompleted) {
		if (ticksSinceCompleted < 0.0F) return 0.0F;
		return 1.0F - smoothstep(clamp01(ticksSinceCompleted / (COMPLETION_SETTLE_TICKS * 0.75F)));
	}

	/** Radius of a grounding rite's ring: from the Focus, out past the floor's edge into the ground around it. */
	public static float groundRingRadius(float ticksSinceCompleted, float reach) {
		float t = clamp01(ticksSinceCompleted / GROUND_RING_TICKS);
		return (reach + GROUND_RING_BEYOND) * (1.0F - (1.0F - t) * (1.0F - t));
	}

	public static float groundRingStrength(float ticksSinceCompleted) {
		if (ticksSinceCompleted < 0.0F || ticksSinceCompleted >= GROUND_RING_TICKS) return 0.0F;
		float fade = clamp01((ticksSinceCompleted - GROUND_RING_TICKS * 0.55F) / (GROUND_RING_TICKS * 0.45F));
		return clamp01(ticksSinceCompleted / 2.0F) * (1.0F - smoothstep(fade));
	}

	/** Whether a collapse or break this long ago is still tearing or regrowing the membrane. */
	public static boolean tearing(float ticksSinceEnded) {
		return ticksSinceEnded >= 0.0F && ticksSinceEnded < TEAR_TICKS + TEAR_HOLD_TICKS + TEAR_REGROW_TICKS;
	}

	/**
	 * The formation age a torn membrane shows: it plays formation backwards at about three times the collapse rate
	 * down to {@link #TEAR_EXTENT} of its spread, holds, then plays it forwards again until fully formed.
	 */
	public static float tearAge(float ticksSinceEnded) {
		if (!tearing(ticksSinceEnded)) return FORM_TICKS;
		if (ticksSinceEnded < TEAR_TICKS) return FORM_TICKS - (FORM_TICKS - TEAR_AGE) * ticksSinceEnded / TEAR_TICKS;
		float regrow = clamp01((ticksSinceEnded - TEAR_TICKS - TEAR_HOLD_TICKS) / TEAR_REGROW_TICKS);
		return TEAR_AGE + (FORM_TICKS - TEAR_AGE) * regrow;
	}

	/** Dark bruising over a torn membrane: it floods in as it tears and drains away as it regrows. */
	public static float tearBruise(float ticksSinceEnded) {
		if (!tearing(ticksSinceEnded)) return 0.0F;
		float heal = clamp01((ticksSinceEnded - TEAR_TICKS - TEAR_HOLD_TICKS) / TEAR_REGROW_TICKS);
		return 0.85F * clamp01(ticksSinceEnded / 4.0F) * (1.0F - smoothstep(heal));
	}

	/** A cancelled rite dims the membrane a little, and it recovers. */
	public static float cancelDim(float ticksSinceCancelled) {
		if (ticksSinceCancelled < 0.0F || ticksSinceCancelled >= CANCEL_TICKS) return 0.0F;
		return 0.35F * clamp01(ticksSinceCancelled / 8.0F)
				* (1.0F - smoothstep(clamp01((ticksSinceCancelled - 8.0F) / (CANCEL_TICKS - 8.0F))));
	}

	/** How bruised the whole membrane is at a rite's instability, 0 to 100. */
	public static float instabilityBruise(int instability) {
		return 0.85F * smoothstep(clamp01((instability - 5) / 90.0F));
	}

	/** A local bruise toward an anchor from its integrity, 0 broken to 1 whole. */
	public static float anchorWound(float integrity) {
		return clamp01(1.0F - integrity);
	}

	/** A freshly broken anchor flares a bruise toward it that eases off; a lasting break is carried by its integrity. */
	public static float brokenWound(float ticksSinceBroken) {
		if (ticksSinceBroken < 0.0F || ticksSinceBroken >= 160.0F) return 0.0F;
		return 1.0F - 0.5F * smoothstep(clamp01(ticksSinceBroken / 40.0F))
				- 0.5F * smoothstep(clamp01((ticksSinceBroken - 40.0F) / 120.0F));
	}

	static float inverseSmoothstep(float y) {
		return 0.5F - (float) Math.sin(Math.asin(1.0D - 2.0D * clamp01(y)) / 3.0D);
	}

	static float mix(float a, float b, float t) {
		return a + (b - a) * t;
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
