package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public enum EnumSaintType {

	HEMORATH(EnumBloodTendency.ANIMUS, EnumBloodTendency.FERRIC),

	SERAPHAE(EnumBloodTendency.LUX, EnumBloodTendency.DUCTILIS),

	PUTRICIEL(EnumBloodTendency.MORTEM, EnumBloodTendency.FLAMMEUS),

	VELORUM(EnumBloodTendency.CONGEATIO, EnumBloodTendency.TENEBRIS);

	private final EnumBloodTendency primaryTendency;
	private final EnumBloodTendency secondaryTendency;

	EnumSaintType(EnumBloodTendency primaryTendency, EnumBloodTendency secondaryTendency) {
		this.primaryTendency = primaryTendency;
		this.secondaryTendency = secondaryTendency;
	}

	public MutableComponent getDisplayName() {
		return Component.translatable("saint.hemomancy." + name().toLowerCase(Locale.ROOT));
	}

	public EnumBloodTendency getPrimaryTendency() {
		return primaryTendency;
	}

	public EnumBloodTendency getSecondaryTendency() {
		return secondaryTendency;
	}

	/**
	 * Returns true if the given tendency is aligned (same as primary or secondary)
	 * with this saint.
	 */
	public boolean isAligned(EnumBloodTendency tendency) {
		return tendency == primaryTendency || tendency == secondaryTendency;
	}

	/**
	 * Returns true if the given tendency is opposed to this saint's tendencies.
	 * Opposition means neither the primary nor secondary tendency matches.
	 */
	public boolean isOpposed(EnumBloodTendency tendency) {
		return !isAligned(tendency);
	}
}
