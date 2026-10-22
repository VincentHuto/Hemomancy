package com.vincenthuto.hemomancy.common.util;

import java.util.Optional;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

/**
 * Finds an entry of one of Hemomancy's own registries by its name, the registry path in the
 * {@code hemomancy} namespace that the entry also carries as its name string.
 */
public final class RegistryLookup {

	private RegistryLookup() {
	}

	/**
	 * Empty for a null, blank or malformed name and for an unregistered one. A defaulted registry's
	 * {@code get} answers an unknown id with its default entry; this never does.
	 */
	public static <T> Optional<T> byName(Registry<T> registry, String name) {
		if (name == null || name.isEmpty()) {
			return Optional.empty();
		}
		ResourceLocation id = ResourceLocation.tryBuild(Hemomancy.MOD_ID, name);
		return id == null ? Optional.empty() : registry.getOptional(id);
	}
}
