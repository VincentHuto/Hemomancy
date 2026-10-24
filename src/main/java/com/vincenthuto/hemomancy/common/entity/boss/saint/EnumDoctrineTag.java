package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public enum EnumDoctrineTag {

	MARTYRDOM,
	ABSOLUTION,
	WITNESS,
	SILENCE,
	JUDGMENT;

	public MutableComponent getDisplayName() {
		return Component.translatable("saint_doctrine.hemomancy." + name().toLowerCase(Locale.ROOT));
	}
}
