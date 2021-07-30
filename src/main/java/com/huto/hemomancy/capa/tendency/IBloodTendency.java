package com.huto.hemomancy.capa.tendency;

import java.util.Map;

import com.hutoslib.client.particle.util.ParticleColor;

import ParticleColor;

public interface IBloodTendency {

	public Map<EnumBloodTendency, Float> getTendency();

	public void setTendency(Map<EnumBloodTendency, Float> Tendency);

	public void setTendencyAlignment(EnumBloodTendency tendencyIn, float value);

	public float getAlignmentByTendency(EnumBloodTendency tendencyIn);

	public EnumBloodTendency getOpposingTendency(EnumBloodTendency tendencyIn);

	ParticleColor getAvgBloodColor();

	float getTotalAlignment();

	float getPercentByTendency(EnumBloodTendency tendencyIn);

	void addTendencyAlignment(EnumBloodTendency tendencyIn, float value);
}
