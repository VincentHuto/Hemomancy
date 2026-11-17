package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import javax.annotation.Nullable;
import java.util.Map;

public interface IBloodTemperament {

	void addTemperAlignment(EnumBloodTemper temperIn, float value);

	void addTransientAlignment(EnumBloodTemper temperIn, float value);

	/** Client mirror: replaces what worn scars lend with the server's figures. */
	void setTransientAlignments(Map<EnumBloodTemper, Float> scars);

	public float getAlignmentByTemper(EnumBloodTemper temperIn);

	/** The grown alignment alone, without what carved scars lend for as long as they are worn. */
	float getBaseAlignment(EnumBloodTemper temperIn);

	ParticleColor getAvgBloodColor();

	/** This temper's share of the whole profile, 0 to 100; 0 for an empty profile. */
	float getPercentByTemper(EnumBloodTemper temperIn);

	public Map<EnumBloodTemper, Float> getTemper();

	float getTotalAlignment();

	public void setTemper(Map<EnumBloodTemper, Float> Temper);

	public void setTemperAlignment(EnumBloodTemper temperIn, float value);

	/**
	 * The temper with the highest effective alignment, or null when none is above zero. A tie goes to the temper
	 * declared first in {@link EnumBloodTemper} (Animus, Flammeus, Ductilis, Lux, Mortem, Congeatio, Ferric,
	 * Tenebris); that order is the rule, not an accident.
	 */
	@Nullable
	default EnumBloodTemper strongestTemper() {
		EnumBloodTemper strongest = null;
		float highest = 0.0F;
		for (EnumBloodTemper candidate : EnumBloodTemper.values()) {
			float value = getAlignmentByTemper(candidate);
			if (value > highest) {
				highest = value;
				strongest = candidate;
			}
		}
		return strongest;
	}
}
