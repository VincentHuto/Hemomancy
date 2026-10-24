package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

/**
 * Local corpus state enum used by SaintSarcophagusBlockEntity.
 * Kept in the same package as the block entity to avoid cross-package classloading issues.
 */
public enum EnumCorpusState {
	DORMANT,
	RESPONSIVE,
	AWAKENED;

	public MutableComponent getDisplayName() {
		return Component.translatable("saint_corpus_state.hemomancy." + name().toLowerCase(Locale.ROOT));
	}
}

