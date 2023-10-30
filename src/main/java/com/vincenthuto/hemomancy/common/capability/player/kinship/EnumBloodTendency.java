package com.vincenthuto.hemomancy.common.capability.player.kinship;

import com.vincenthuto.hemomancy.common.registry.ItemInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.world.item.Item;

public enum EnumBloodTendency {

	ANIMUS(new ParticleColor(255, 0, 0)), FLAMMEUS(new ParticleColor(255, 100, 0)),
	DUCTILIS(new ParticleColor(255, 255, 0)), LUX(new ParticleColor(255, 255, 255)),
	MORTEM(new ParticleColor(0, 58, 0)), CONGEATIO(new ParticleColor(0, 100, 255)),
	FERRIC(new ParticleColor(53, 53, 53)), TENEBRIS(new ParticleColor(70, 0, 110));

	public static Item getRepEnzyme(EnumBloodTendency tend) {
		switch (tend) {
		case ANIMUS:
			return ItemInit.vivacious_enzyme.get();
		case CONGEATIO:
			return ItemInit.frigid_enzyme.get();
		case DUCTILIS:
			return ItemInit.neurotic_enzyme.get();
		case FERRIC:
			return ItemInit.ferric_enzyme.get();
		case FLAMMEUS:
			return ItemInit.fervent_enzyme.get();
		case LUX:
			return ItemInit.incandescent_enzyme.get();
		case MORTEM:
			return ItemInit.ruinous_enzyme.get();
		case TENEBRIS:
			return ItemInit.umbral_enzyme.get();
		default:
			return ItemInit.recycled_enzyme.get();

		}

	}

	ParticleColor color;

	EnumBloodTendency(ParticleColor color) {
		this.color = color;
	}

	public ParticleColor getColor() {
		return color;
	}

}