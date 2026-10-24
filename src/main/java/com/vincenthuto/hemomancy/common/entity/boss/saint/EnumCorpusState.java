package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public enum EnumCorpusState {

	DORMANT,
	RESPONSIVE,
	AWAKENED;

	public MutableComponent getDisplayName() {
		return Component.translatable("saint_corpus_state.hemomancy." + name().toLowerCase(Locale.ROOT));
	}
}
