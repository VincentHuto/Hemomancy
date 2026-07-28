package com.vincenthuto.hemomancy.common.rite.harbinger;

import java.util.List;

public final class CardinalRiteInteractionMarker {
	private static final List<Layer> LAYERS = List.of(Layer.BLOOD_CELL, Layer.GLOW);

	private CardinalRiteInteractionMarker() {
	}

	public static List<Layer> layers() {
		return LAYERS;
	}

	public enum Layer {
		BLOOD_CELL,
		GLOW
	}
}
