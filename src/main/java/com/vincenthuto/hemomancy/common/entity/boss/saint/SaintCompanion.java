package com.vincenthuto.hemomancy.common.entity.boss.saint;

import java.util.UUID;

/**
 * An entity that fights in a Saint's trial: a woken devotee, an anchor, a fragment. It names its Saint and whose side
 * it is on. {@code ManipulationCombatHelper} reads it: a CHALLENGERS companion is every player's ally and harms only its
 * Saint; a Saint never harms its own SAINT companions, nor they it. One that loads with no live Saint discards itself.
 */
public interface SaintCompanion {
	enum Allegiance { CHALLENGERS, SAINT }

	UUID saintId();

	Allegiance allegiance();
}
