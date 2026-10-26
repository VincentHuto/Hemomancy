package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * The player's side of being taken by the Mycophant, as one continuous scene:
 * <ul>
 * <li><b>Capture</b> (the Gardens root claim): hyphae creep in from the edges of the view and lace across it, the
 * player is pulled to their knees and then down, and their eyes close.</li>
 * <li><b>Waking</b> (the Nursery): eyes open in heavy blinks onto the same webbing, the player lies, kneels and rises,
 * the fibers pull taut and snap strand by strand, control returns, and her roar shakes the view.</li>
 * </ul>
 * Also carries the hunt's watching pulses and the Pulp Bloom veil. Pure timing so it is JVM-tested; the client events
 * and {@code MycophantMyceliumOverlay} only read it.
 */
public final class MycophantIntroClientState {
	public static final int WAKING = 0;
	public static final int REMATCH = 1;
	public static final int BLOOM = 2;
	public static final int CLEAR = 3;
	public static final int CAPTURE = 4;
	public static final int WATCH = 5;

	/** Body stages the client forces on the player: none, kneeling, lying in the nectar. */
	public static final int STAND = 0;
	public static final int KNEEL = 1;
	public static final int LIE = 2;

	public static final int FIBERS = 9;

	public static final MycophantIntroClientState INSTANCE = new MycophantIntroClientState();

	private static final int FIBER_SNAP = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.FIBERS_SNAP, false);
	private static final int CONTROL = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.CONTROL, false);
	private static final int ROAR_WAKING = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.ROAR, false);
	private static final int ROAR_REMATCH = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.ROAR, true);
	private static final int SNAP_STAGGER = 9;
	private static final int SNAP_TICKS = 12;
	private static final int ROAR_SHAKE_TICKS = 26;
	public static final int WAKING_LENGTH = 220;
	/** The root claim's length on the server (MycophantEncounterManager); the dark then holds until the waking. */
	public static final int CAPTURE_TICKS = 100;
	public static final int CAPTURE_LENGTH = CAPTURE_TICKS + 80;
	private static final int BLOOM_TICKS = 40;
	private static final int WATCH_TICKS = 70;

	private int kind = CLEAR;
	private int elapsed = -1;
	private int bloomLeft;
	private int watchLeft;
	private int watchStage;
	private int seed;

	public void start(int kind, int seed) {
		if (kind == BLOOM) {
			this.bloomLeft = BLOOM_TICKS;
			return;
		}
		if (kind == WATCH) {
			this.watchLeft = WATCH_TICKS;
			this.watchStage = Math.max(1, seed);
			return;
		}
		this.kind = kind;
		this.seed = seed;
		this.elapsed = kind == CLEAR ? -1 : 0;
		if (kind == CLEAR) {
			this.bloomLeft = 0;
			this.watchLeft = 0;
		}
	}

	public void tick() {
		if (elapsed >= 0 && ++elapsed > (kind == CAPTURE ? CAPTURE_LENGTH : WAKING_LENGTH)) {
			elapsed = -1;
			kind = CLEAR;
		}
		if (bloomLeft > 0) bloomLeft--;
		if (watchLeft > 0) watchLeft--;
	}

	public boolean isActive() {
		return elapsed >= 0 || bloomLeft > 0 || watchLeft > 0;
	}

	public int seed() {
		return seed;
	}

	public int elapsed() {
		return elapsed;
	}

	public boolean waking() {
		return kind == WAKING && elapsed >= 0;
	}

	public boolean capturing() {
		return kind == CAPTURE && elapsed >= 0;
	}

	private float time(float partial) {
		return elapsed + partial;
	}

	// ------------------------------------------------------------------ eyes and murk

	/** How far the eyelids are open: 1 = fully, 0 = shut. */
	public float lidOpen(float partial) {
		float t = time(partial);
		if (waking()) return keys(t, 0, 0, 12, 0, 24, 0.42F, 32, 0.1F, 48, 0.78F, 56, 0.55F, 72, 1);
		if (capturing()) return keys(t, 0, 1, 68, 1, 80, 0.45F, 86, 0.62F, CAPTURE_TICKS, 0);
		return 1.0F;
	}

	/** A flat murk over the view, under the lids. */
	public float dimAlpha(float partial) {
		float t = time(partial);
		if (waking()) return keys(t, 0, 0.6F, 72, 0.25F, 95, 0.25F, 110, 0);
		if (capturing()) return keys(t, 0, 0, 60, 0.15F, CAPTURE_TICKS, 0.5F);
		return 0.0F;
	}

	// ------------------------------------------------------------------ the body and the camera

	/** {@link #STAND}, {@link #KNEEL} or {@link #LIE}: the pose the client holds the player in. */
	public int bodyStage() {
		if (waking()) return elapsed < 34 ? LIE : elapsed < 56 ? KNEEL : STAND;
		if (capturing()) return elapsed >= 86 ? LIE : elapsed >= 62 ? KNEEL : STAND;
		return STAND;
	}

	public float rollDegrees(float partial) {
		float t = time(partial);
		if (waking()) return keys(t, 0, 26, 30, 26, 72, 0);
		if (capturing()) return keys(t, 0, 0, 60, 4, CAPTURE_TICKS, 22);
		return 0.0F;
	}

	/** Positive tips the view up (lying on one's back), negative pulls it down toward the roots. */
	public float pitchDegrees(float partial) {
		float t = time(partial);
		if (waking()) return keys(t, 0, 12, 30, 12, 72, 0);
		if (capturing()) return keys(t, 0, 0, CAPTURE_TICKS, -16);
		return 0.0F;
	}

	/** A slow dazed drift, in degrees. */
	public float swayDegrees(float partial) {
		float t = time(partial);
		if (waking()) return keys(t, 0, 1.4F, 60, 1.0F, 110, 0);
		if (capturing()) return keys(t, 0, 0.3F, CAPTURE_TICKS, 1.2F);
		return 0.0F;
	}

	/** Fast shake: a jolt as each fiber snaps, the roots tugging during capture, and the roar. */
	public float shakeAmplitude(float partial) {
		if (elapsed < 0) return 0.0F;
		float t = time(partial);
		float shake = 0.0F;
		if (kind == WAKING) {
			for (int f = 0; f < FIBERS; f++) {
				float since = t - fiberSnapTick(f);
				if (since >= 0.0F && since < 8.0F) shake = Math.max(shake, 0.9F * (1.0F - since / 8.0F));
			}
		}
		if (kind == CAPTURE) shake = keys(t, 0, 0.15F, 60, 0.35F, 86, 0.8F, CAPTURE_TICKS, 0);
		if (kind == WAKING || kind == REMATCH) {
			int roar = kind == WAKING ? ROAR_WAKING : ROAR_REMATCH;
			if (t >= roar && t < roar + ROAR_SHAKE_TICKS)
				shake = Math.max(shake, 2.2F * (1.0F - (t - roar) / ROAR_SHAKE_TICKS * 0.6F));
		}
		return shake;
	}

	public boolean inputBound() {
		return (waking() && elapsed < CONTROL) || capturing();
	}

	// ------------------------------------------------------------------ the webbing

	/** Overall opacity of the binding fibers. */
	public float fiberAlpha(float partial) {
		float t = time(partial);
		if (waking()) return keys(t, 0, 1, FIBER_SNAP + SNAP_STAGGER + SNAP_TICKS, 1, FIBER_SNAP + SNAP_STAGGER + SNAP_TICKS + 14, 0);
		if (capturing()) return 1.0F;
		return 0.0F;
	}

	/** How far fiber {@code f} has crept in from its anchors (0..1). Waking fibers are already whole. */
	public float fiberGrowth(int f, float partial) {
		if (waking()) return 1.0F;
		if (!capturing()) return 0.0F;
		float start = 4.0F + (f * 7 % FIBERS) * 5.0F;
		return HemoEasing.clamp01((time(partial) - start) / 46.0F);
	}

	/** The pull before the snap: fibers straighten and tremble as the player tries to rise (0..1). */
	public float fiberTension(float partial) {
		if (waking()) return keys(time(partial), 0, 0.15F, 34, 0.3F, 56, 0.6F, FIBER_SNAP, 1);
		if (capturing()) return keys(time(partial), 0, 0, CAPTURE_TICKS, 0.7F);
		return 0.0F;
	}

	/** The tick fiber {@code f} snaps; they go one after another, not all at once. */
	public static int fiberSnapTick(int f) {
		return FIBER_SNAP + (f * 4 % SNAP_STAGGER);
	}

	/** 0 while fiber {@code f} holds, rising to 1 as its halves recoil. */
	public float fiberBreak(int f, float partial) {
		if (!waking()) return 0.0F;
		return HemoEasing.clamp01((time(partial) - fiberSnapTick(f)) / SNAP_TICKS);
	}

	/** The mycelial vignette at the edge of the view: the webbing's bed, the hunt's watching. */
	public float vignette(float partial) {
		return Math.max(sceneVignette(partial), watch(partial));
	}

	/** The vignette's waking and capture part alone, which their scenes draw. */
	public float sceneVignette(float partial) {
		float t = time(partial);
		if (waking()) return keys(t, 0, 1, FIBER_SNAP, 1, 110, 0);
		if (capturing()) return keys(t, 0, 0, 70, 0.85F, CAPTURE_TICKS, 1);
		return 0.0F;
	}

	/** The hunt's watching pulse: the edges of the Gardens creep in for a few seconds (0..1, stronger later). */
	public float watch(float partial) {
		if (watchLeft <= 0) return 0.0F;
		float t = WATCH_TICKS - watchLeft + partial;
		float strength = watchStage >= 2 ? 0.75F : 0.5F;
		return strength * keys(t, 0, 0, 18, 1, 44, 0.85F, WATCH_TICKS, 0);
	}

	public float bloomVeil(float partial) {
		if (bloomLeft <= 0) return 0.0F;
		return HemoEasing.clamp01((bloomLeft - partial) / BLOOM_TICKS);
	}

	/** Piecewise smooth curve through (time, value) pairs; flat before the first and after the last. */
	static float keys(float t, float... pairs) {
		return BossSceneTracks.keys(t, pairs);
	}
}
