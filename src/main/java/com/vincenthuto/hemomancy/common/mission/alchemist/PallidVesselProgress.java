package com.vincenthuto.hemomancy.common.mission.alchemist;

/**
 * The Pallid Vessel, the Alchemist's optional third-Degree Vocation: hear the briefing, perform the Rite of the Pallid
 * Vessel, put blood in the white gourd it makes, then report. None of it gates a Degree.
 */
public record PallidVesselProgress(boolean briefed, boolean riteDone, boolean filled, boolean reported) {
	public static final PallidVesselProgress NONE = new PallidVesselProgress(false, false, false, false);
	public static final int STEPS = 4;

	public enum Stage { NOT_BRIEFED, NEEDS_RITE, NEEDS_FILL, READY, COMPLETE }

	/** Where the dialogue stands. The rite and the fill can come before the briefing; the report can't. */
	public Stage stage() {
		if (reported) return Stage.COMPLETE;
		if (!briefed) return Stage.NOT_BRIEFED;
		if (!riteDone) return Stage.NEEDS_RITE;
		if (!filled) return Stage.NEEDS_FILL;
		return Stage.READY;
	}

	public int completedSteps() {
		return (briefed ? 1 : 0) + (riteDone ? 1 : 0) + (filled ? 1 : 0) + (reported ? 1 : 0);
	}

	public boolean canReport() {
		return stage() == Stage.READY;
	}

	/** The fill counts only once the player's own rite is on record, so a gourd made elsewhere proves nothing. */
	public boolean acceptsFill() {
		return riteDone && !filled && !reported;
	}

	public PallidVesselProgress withBriefed() {
		return new PallidVesselProgress(true, riteDone, filled, reported);
	}

	public PallidVesselProgress withRite() {
		return new PallidVesselProgress(briefed, true, filled, reported);
	}

	public PallidVesselProgress withFill() {
		return acceptsFill() ? new PallidVesselProgress(briefed, riteDone, true, reported) : this;
	}

	public PallidVesselProgress withReport() {
		return canReport() ? new PallidVesselProgress(briefed, riteDone, filled, true) : this;
	}
}
