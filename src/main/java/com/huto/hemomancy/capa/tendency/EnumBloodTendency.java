package com.huto.hemomancy.capa.tendency;

import com.huto.hemomancy.particle.util.ParticleColor;

public enum EnumBloodTendency {
	TENEBRIS(new ParticleColor(70, 0, 110)), LUX(new ParticleColor(255, 255, 255)),
	FLAMMEUS(new ParticleColor(255, 100, 0)), CONGEATIO(new ParticleColor(0, 100, 255)),
	ANIMUS(new ParticleColor(255, 0, 0)), MORTEM(new ParticleColor(0, 58, 0)), FERRIC(new ParticleColor(53, 53, 53)),
	DUCTILIS(new ParticleColor(255, 255, 0));

	ParticleColor color;

	EnumBloodTendency(ParticleColor color) {
		this.color = color;
	}

	public ParticleColor getColor() {
		return color;
	}

}
