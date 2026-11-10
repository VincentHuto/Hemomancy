package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public enum EnumSaintType {

	HEMORATH(EnumBloodTemper.ANIMUS, EnumBloodTemper.FERRIC),

	SERAPHAE(EnumBloodTemper.LUX, EnumBloodTemper.DUCTILIS),

	PUTRICIEL(EnumBloodTemper.MORTEM, EnumBloodTemper.FLAMMEUS),

	VELORUM(EnumBloodTemper.CONGEATIO, EnumBloodTemper.TENEBRIS);

	private final EnumBloodTemper primaryTemper;
	private final EnumBloodTemper secondaryTemper;

	EnumSaintType(EnumBloodTemper primaryTemper, EnumBloodTemper secondaryTemper) {
		this.primaryTemper = primaryTemper;
		this.secondaryTemper = secondaryTemper;
	}

	public MutableComponent getDisplayName() {
		return Component.translatable("saint.hemomancy." + name().toLowerCase(Locale.ROOT));
	}

	public EnumBloodTemper getPrimaryTemper() {
		return primaryTemper;
	}

	public EnumBloodTemper getSecondaryTemper() {
		return secondaryTemper;
	}

	/**
	 * Returns true if the given temper is aligned (same as primary or secondary)
	 * with this saint.
	 */
	public boolean isAligned(EnumBloodTemper temper) {
		return temper == primaryTemper || temper == secondaryTemper;
	}

}
