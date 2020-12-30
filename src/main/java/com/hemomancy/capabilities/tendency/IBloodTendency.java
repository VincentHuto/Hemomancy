package com.hemomancy.capabilities.tendency;

import java.util.Map;

public interface IBloodTendency {

	public Map<EnumBloodTendency, Float> getDevotion();

	public void setTendency(Map<EnumBloodTendency, Float> devotion);

	public void setTendencyDevotion(EnumBloodTendency tendencyIn, float value);

	public float getDevotionByTendency(EnumBloodTendency tendencyIn);

	public EnumBloodTendency getOpposingTendency(EnumBloodTendency tendencyIn);
}
