package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * What a Specimen Vat draws (core-loop Track 5). A lone vat draws its own octagonal column. A joined vessel is drawn as
 * one octagonal vat grown to its size, and one vat of each layer carries that layer's slice of it (the middle vat of a
 * vessel three across, the corner vat otherwise; see specimen_vat.py); every other vat of the vessel draws nothing.
 */
public enum VatPart implements StringRepresentable {
	SINGLE, HIDDEN,
	W1_BOTTOM, W1_TOP,
	W2_ONLY, W2_BOTTOM, W2_TOP,
	W3_ONLY, W3_BOTTOM, W3_TOP;

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	/** Which vat of a layer carries the layer's slice, counted from the vessel's corner along x and along z. */
	public static int carrier(int width) {
		return width == 3 ? 1 : 0;
	}

	/**
	 * What the vat {@code dx, dz} vats from the corner of layer {@code layer} draws in a vessel {@code width} across and
	 * {@code layers} high.
	 */
	public static VatPart of(int width, int layers, int layer, int dx, int dz) {
		if (width == 1 && layers == 1) return SINGLE;
		int carrier = carrier(width);
		if (dx != carrier || dz != carrier) return HIDDEN;
		String role = layers == 1 ? "ONLY" : layer == 0 ? "BOTTOM" : "TOP";
		return valueOf("W" + width + "_" + role);
	}
}
