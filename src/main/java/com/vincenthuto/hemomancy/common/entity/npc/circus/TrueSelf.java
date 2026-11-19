package com.vincenthuto.hemomancy.common.entity.npc.circus;

/**
 * A creature drawn as its true self while in a professor's Chamber (the Chamber ordeals, 2026-11-17): the professor
 * and the ordeal's puppet wear separate, more dramatic models there. Synced, so the renderer picks the model.
 */
public interface TrueSelf {
	boolean isTrueSelf();

	void setTrueSelf(boolean trueSelf);
}
