package com.vincenthuto.hemomancy.common.world.ground;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * What a lit gouge run does, given by the catalyst dissolved into it ({@code data/hemomancy/gouge_catalyst/}). A run
 * with no catalyst glows faintly and does nothing else. The function is a block state, so the client draws it and the
 * light engine reads it without a block entity.
 */
public enum GroundFunction implements StringRepresentable {
	/** No catalyst: a faint glow along the way it was drawn. */
	NONE(3, 0xB0141E),
	/** Real light, as bright as a lantern. */
	LIGHT(13, 0xFFE6A8),
	/** Hot: items lying on it smelt slowly; campfires and candles beside it catch, and furnaces beside it burn. */
	HOT(7, 0xFF6A1E),
	/** Points: the far end nearest the compass's place flares. */
	POINT(4, 0xC9B6FF),
	/** Cold: still water beside it freezes; crops beside it stop growing. */
	COLD(4, 0x9ED8FF),
	/** Pulls dropped items along the run toward the pulse's origin. */
	PULL(4, 0xB9C2CC),
	/** Feeds what grows beside it. */
	FEED(4, 0x8EE06A),
	/** Dark: the run hides itself and dims the sight of creatures along it. */
	DARK(0, 0x000000);

	private final int light;
	private final int color;

	GroundFunction(int light, int color) {
		this.light = light;
		this.color = color;
	}

	/** The block light a lit run gives. */
	public int light() {
		return light;
	}

	/** The colour the client draws a lit run in (RGB). */
	public int color() {
		return color;
	}

	/** Whether a lit run of this function is drawn at all: a dark run hides itself. */
	public boolean drawn() {
		return this != DARK;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	/** The function named {@code id} ({@code "light"}, {@code "hemomancy:light"}), or null. */
	public static GroundFunction byName(String id) {
		if (id == null) return null;
		String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
		for (GroundFunction function : values()) {
			if (function.getSerializedName().equals(path)) return function;
		}
		return null;
	}
}
