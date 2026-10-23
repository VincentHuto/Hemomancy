package com.vincenthuto.hemomancy.common.manipulation;

/** What a manipulation does when an entity casts it; see {@link EntityCasts}. */
@FunctionalInterface
public interface EntityCast {
	/** @return whether the cast went off */
	boolean cast(ManipulationCastContext context);
}
