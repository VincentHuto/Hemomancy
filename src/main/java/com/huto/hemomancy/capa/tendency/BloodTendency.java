package com.huto.hemomancy.capa.tendency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hutoslib.client.particle.ParticleColor;

public class BloodTendency implements IBloodTendency {
	@SuppressWarnings("serial")
	private Map<EnumBloodTendency, Float> tendency = new HashMap<EnumBloodTendency, Float>() {
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

	@Override
	public Map<EnumBloodTendency, Float> getTendency() {
		return tendency;
	}

	@Override
	public void setTendency(Map<EnumBloodTendency, Float> tendency) {
		this.tendency = tendency;
	}

	@Override
	public void setTendencyAlignment(EnumBloodTendency tendencyIn, float value) {
		if (tendency != null) {
			if (getOpposingTendency(tendencyIn) != null) {
				Map<EnumBloodTendency, Float> newDevo = tendency;
				newDevo.put(tendencyIn, value);
				setTendency(newDevo);
			}
		}
	}

	@Override
	public void addTendencyAlignment(EnumBloodTendency tendencyIn, float value) {
		if (tendency != null) {
			if (getOpposingTendency(tendencyIn) != null) {
				Map<EnumBloodTendency, Float> newDevo = tendency;
				newDevo.put(tendencyIn, getAlignmentByTendency(tendencyIn) + value);
				setTendency(newDevo);
			}
		}
	}

	@Override
	public float getAlignmentByTendency(EnumBloodTendency tendencyIn) {
		if (tendency != null && tendency.get(tendencyIn) != null) {
			return tendency.get(tendencyIn);
		} else {
			return 0;
		}
	}

	@Override
	public float getPercentByTendency(EnumBloodTendency tendencyIn) {
		float total = getTotalAlignment();
		float current = getAlignmentByTendency(tendencyIn);
		float per = (current / total) * 100;
		return per;
	}

	@Override
	public float getTotalAlignment() {
		float runningTotal = 0;
		for (EnumBloodTendency key : EnumBloodTendency.values()) {
			runningTotal += tendency.get(key);
		}
		return runningTotal;

	}

	@Override
	public ParticleColor getAvgBloodColor() {
		List<ParticleColor> colors = new ArrayList<ParticleColor>();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			float percent = getPercentByTendency(tend);
			colors.add(new ParticleColor(tend.getColor().getRed() * percent, tend.getColor().getBlue() * percent,
					tend.getColor().getGreen() * percent));
		}
		return ParticleColor.averageFromList(colors);

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
