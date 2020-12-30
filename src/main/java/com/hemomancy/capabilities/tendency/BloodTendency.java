package com.hemomancy.capabilities.tendency;

import java.util.HashMap;
import java.util.Map;

public class BloodTendency implements IBloodTendency {
	private Map<EnumBloodTendency, Float> devotion = new HashMap<>();

	public Map<EnumBloodTendency, Float> getDevotion() {
		return devotion;
	}

	public void setTendency(Map<EnumBloodTendency, Float> devotion) {
		this.devotion = devotion;
	}

	public void setTendencyDevotion(EnumBloodTendency tendencyIn, float value) {
		if (devotion != null) {
			if (getOpposingTendency(tendencyIn) != null) {
				Map<EnumBloodTendency, Float> newDevo = devotion;
				newDevo.put(tendencyIn, getDevotionByTendency(tendencyIn) + value);
				newDevo.put(getOpposingTendency(tendencyIn),
						getDevotionByTendency(getOpposingTendency(tendencyIn)) - value);
				setTendency(newDevo);
			}
		}
	}

	public float getDevotionByTendency(EnumBloodTendency tendencyIn) {
		if (devotion != null && devotion.get(tendencyIn) != null) {
			return devotion.get(tendencyIn);
		} else {
			return 0;
		}
	}

	@Override
	public EnumBloodTendency getOpposingTendency(EnumBloodTendency tendencyIn) {
		switch (tendencyIn) {
		case ANIMUS:
			return EnumBloodTendency.MORTEM;
		case MORTEM:
			return EnumBloodTendency.ANIMUS;
		case DUCTILIS:
			return EnumBloodTendency.FERRIC;
		case FERRIC:
			return EnumBloodTendency.DUCTILIS;
		case LUX:
			return EnumBloodTendency.TENEBRIS;
		case TENEBRIS:
			return EnumBloodTendency.LUX;
		case FLAMMEUS:
			return EnumBloodTendency.CONGEATIO;
		case CONGEATIO:
			return EnumBloodTendency.FLAMMEUS;
		default:
			return null;
		}
	}

}
