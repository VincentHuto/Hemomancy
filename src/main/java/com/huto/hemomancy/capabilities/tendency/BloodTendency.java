package com.huto.hemomancy.capabilities.tendency;

import java.util.HashMap;
import java.util.Map;

public class BloodTendency implements IBloodTendency {
	@SuppressWarnings("serial")
	private Map<EnumBloodTendency, Float> tendency = new HashMap<EnumBloodTendency, Float>(){
		{
			put(EnumBloodTendency.ANIMUS, 0f);
			put(EnumBloodTendency.MORTEM, 0f);
			put(EnumBloodTendency.DUCTILIS, 0f);
			put(EnumBloodTendency.FERRIC, 0f);
			put(EnumBloodTendency.LUX, 0f);
			put(EnumBloodTendency.TENEBRIS, 0f);
			put(EnumBloodTendency.FLAMMEUS, 0f);
			put(EnumBloodTendency.CONGEATIO, 0f);

		}
	};

	public Map<EnumBloodTendency, Float> getTendency() {
		return tendency;
	}

	public void setTendency(Map<EnumBloodTendency, Float> tendency) {
		this.tendency = tendency;
	}

	public void setTendencyAlignment(EnumBloodTendency tendencyIn, float value) {
		if (tendency != null) {
			if (getOpposingTendency(tendencyIn) != null) {
				Map<EnumBloodTendency, Float> newDevo = tendency;
				newDevo.put(tendencyIn, getAlignmentByTendency(tendencyIn) + value);
				newDevo.put(getOpposingTendency(tendencyIn),
						getAlignmentByTendency(getOpposingTendency(tendencyIn)) - value);
				setTendency(newDevo);
			}
		}
	}

	public float getAlignmentByTendency(EnumBloodTendency tendencyIn) {
		if (tendency != null && tendency.get(tendencyIn) != null) {
			return tendency.get(tendencyIn);
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
