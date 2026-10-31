package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import net.minecraft.util.StringRepresentable;

/**
 * The eight Brined Court sites and the water each one lives in. The Midnight houses share a layer and split by the
 * climate of the surface above them, so the Screened Cleft and the Bell never meet.
 */
public enum CourtSite implements StringRepresentable {
	LIGHTHOUSE_GARDEN("lighthouse_garden", "giving_light", Tier.SEAT, PelagicLayer.REEF, 48, SurfaceClimate.ANY, 24, 8, 1),
	VENT_SANCTUM("vent_sanctum", "kindling", Tier.SEAT, PelagicLayer.HYDROTHERMAL, -42, SurfaceClimate.ANY, 24, 10, 1),
	TIDE_REFUGE("tide_refuge", "holding_stone", Tier.OUTPOST, PelagicLayer.SHORE, 62, SurfaceClimate.ANY, 8, 6, 1),
	WAKE_NURSERY("wake_nursery", "shared_wake", Tier.OUTPOST, PelagicLayer.OPEN, 48, SurfaceClimate.ANY, 8, 8, 6),
	LINE_CAMP("line_camp", "woven_current", Tier.OUTPOST, PelagicLayer.TWILIGHT, 23, SurfaceClimate.ANY, 8, 6, 1),
	SCREENED_CLEFT("screened_cleft", "veiled_fold", Tier.OUTPOST, PelagicLayer.MIDNIGHT, 2,
			SurfaceClimate.TEMPERATE_OR_WARM, 8, 12, 1),
	STILL_BELL("still_bell", "still_bell", Tier.OUTPOST, PelagicLayer.MIDNIGHT, 2, SurfaceClimate.COLD_OR_FROZEN, 8, 8, 1),
	WHALE_FALL("whale_fall", "last_marrow", Tier.OUTPOST, PelagicLayer.CARRION, -19, SurfaceClimate.ANY, 8, 6, 1);

	public static final Codec<CourtSite> CODEC = StringRepresentable.fromEnum(CourtSite::values);

	public enum Tier { SEAT, OUTPOST }

	/** Read at sea level from the climate sampler; cold and frozen are vanilla's two lowest temperature bands. */
	public enum SurfaceClimate { ANY, TEMPERATE_OR_WARM, COLD_OR_FROZEN }

	private final String id;
	private final String house;
	private final Tier tier;
	private final PelagicLayer layer;
	private final int sampleY;
	private final SurfaceClimate climate;
	private final int footprintRadius;
	private final int maxFloorVariation;
	private final int rarity;

	CourtSite(String id, String house, Tier tier, PelagicLayer layer, int sampleY, SurfaceClimate climate,
			int footprintRadius, int maxFloorVariation, int rarity) {
		this.id = id;
		this.house = house;
		this.tier = tier;
		this.layer = layer;
		this.sampleY = sampleY;
		this.climate = climate;
		this.footprintRadius = footprintRadius;
		this.maxFloorVariation = maxFloorVariation;
		this.rarity = rarity;
	}

	@Override
	public String getSerializedName() {
		return id;
	}

	public String id() {
		return id;
	}

	public String house() {
		return house;
	}

	public Tier tier() {
		return tier;
	}

	/** The biome the site's centre must stand in, at the band's middle and at the chosen floor. */
	public PelagicLayer layer() {
		return layer;
	}

	/** The middle of the house's depth band, where the first biome-source check samples. */
	public int sampleY() {
		return sampleY;
	}

	public SurfaceClimate climate() {
		return climate;
	}

	/** Half-width of the square whose corners and edge midpoints the floor check reads. */
	public int footprintRadius() {
		return footprintRadius;
	}

	public int maxFloorVariation() {
		return maxFloorVariation;
	}

	/** One in this many otherwise valid cells keeps the site; 1 keeps every one. */
	public int rarity() {
		return rarity;
	}
}
