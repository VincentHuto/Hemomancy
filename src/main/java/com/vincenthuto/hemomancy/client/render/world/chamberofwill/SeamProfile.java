package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

/**
 * Radius profiles for the dome seam's tissue, so everything that grows from the lip is part of one surface: knots
 * swell the lip itself, veins flare out of it, and beads of blood are swellings travelling inside a vein, never
 * separate shapes resting on or poking through it.
 */
final class SeamProfile {
	/** How much of the lip's thickness a vein's root takes as it leaves the lip. */
	static final double ROOT_FLARE = 0.75;
	/** The share of a vein's length over which its root narrows from the lip to its own width. */
	private static final double FLARE_LENGTH = 0.25;

	private SeamProfile() {
	}

	/** A smooth swelling, 1 at its centre, fading over `width` samples either side. */
	static double knot(double distance, double width) {
		double x = distance / width;
		return Math.exp(-x * x);
	}

	/**
	 * Radii along a vein of `points` rings: flaring from the lip at its root, tapering to a thin tip, and swelling
	 * where a bead of blood sits (`beadAt` 0..1 along it, or negative for none).
	 */
	static double[] veinRadii(int points, double lipRadius, double width, double beadAt, double beadSize) {
		double[] radii = new double[points];
		double root = lipRadius * ROOT_FLARE;
		for (int i = 0; i < points; i++) {
			double t = i / (double) (points - 1);
			double r;
			if (t < FLARE_LENGTH) {
				double s = t / FLARE_LENGTH;
				s = s * s * (3 - 2 * s);
				r = root + (width - root) * s;
			} else {
				r = width * (1 - 0.8 * (t - FLARE_LENGTH) / (1 - FLARE_LENGTH));
			}
			if (beadAt >= 0) r += beadSize * knot((t - beadAt) * (points - 1), 1.6);
			radii[i] = r;
		}
		return radii;
	}
}
