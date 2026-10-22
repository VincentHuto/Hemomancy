package com.vincenthuto.hemomancy.common.item;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * An item whose client-side renderer or pose is built in {@link #hemomancy$getClientItemExtensions()}. The client
 * registers it through {@code ClientExtensionsRegistrar}; the body is the only place a common item may name client
 * classes.
 */
public interface HemoClientItemExtensionsProvider {
    IClientItemExtensions hemomancy$getClientItemExtensions();
}

