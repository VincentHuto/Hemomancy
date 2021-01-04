package com.huto.hemomancy.capabilities.tendency;

import java.util.Map;

public interface IBloodTendency {

	public Map<EnumBloodTendency, Float> getTendency();

	public void setTendency(Map<EnumBloodTendency, Float> Tendency);

	public void setTendencyTendency(EnumBloodTendency tendencyIn, float value);

	public float getTendencyByTendency(EnumBloodTendency tendencyIn);

	public EnumBloodTendency getOpposingTendency(EnumBloodTendency tendencyIn);
}
