package com.huto.hemomancy.capabilities.tendency;

import java.util.Map;

public interface IBloodTendency {

	public Map<EnumBloodTendency, Float> getTendency();

	public void setTendency(Map<EnumBloodTendency, Float> Tendency);

	public void setTendencyAlignment(EnumBloodTendency tendencyIn, float value);

	public float getAlignmentByTendency(EnumBloodTendency tendencyIn);

	public EnumBloodTendency getOpposingTendency(EnumBloodTendency tendencyIn);
}
