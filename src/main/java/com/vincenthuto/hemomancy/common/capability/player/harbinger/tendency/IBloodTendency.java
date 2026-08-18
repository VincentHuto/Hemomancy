package com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency;

import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import java.util.Map;

public interface IBloodTendency {

	void addTendencyAlignment(EnumBloodTendency tendencyIn, float value);

	void addTransientAlignment(EnumBloodTendency tendencyIn, float value);

	public float getAlignmentByTendency(EnumBloodTendency tendencyIn);

	ParticleColor getAvgBloodColor();

	public EnumBloodTendency getOpposingTendency(EnumBloodTendency tendencyIn);

	float getPercentByTendency(EnumBloodTendency tendencyIn);

	public Map<EnumBloodTendency, Float> getTendency();

	float getTotalAlignment();

	public void setTendency(Map<EnumBloodTendency, Float> Tendency);

	public void setTendencyAlignment(EnumBloodTendency tendencyIn, float value);
}
