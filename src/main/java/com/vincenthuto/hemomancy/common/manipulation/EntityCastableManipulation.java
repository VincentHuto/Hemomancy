package com.vincenthuto.hemomancy.common.manipulation;

@FunctionalInterface
public interface EntityCastableManipulation {
	boolean castFromEntity(ManipulationCastContext context);
}
