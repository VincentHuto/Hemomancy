package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import java.util.Map;

public interface IBloodTemperament {

	void addTemperAlignment(EnumBloodTemper temperIn, float value);

	void addTransientAlignment(EnumBloodTemper temperIn, float value);

	public float getAlignmentByTemper(EnumBloodTemper temperIn);

	/** The grown alignment alone, without what carved scars lend for as long as they are worn. */
	float getBaseAlignment(EnumBloodTemper temperIn);

	ParticleColor getAvgBloodColor();

	public EnumBloodTemper getOpposingTemper(EnumBloodTemper temperIn);

	float getPercentByTemper(EnumBloodTemper temperIn);

	public Map<EnumBloodTemper, Float> getTemper();

	float getTotalAlignment();

	public void setTemper(Map<EnumBloodTemper, Float> Temper);

	public void setTemperAlignment(EnumBloodTemper temperIn, float value);
}
